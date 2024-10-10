package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoDetailResponse;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    public Page<TodoResponse> getTodos(int page, int size, String weather, String startDate, String endDate) {
        Pageable pageable = PageRequest.of(page - 1, size);
        LocalDateTime formatStartDate = null;
        LocalDateTime formatEndDate = null;

        if(startDate != null) {
            formatStartDate = LocalDate.parse(startDate).atStartOfDay();
        }

        if(endDate != null) {
            formatEndDate = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        }

        Page<Todo> todos = todoRepository.findAllByOrderByModifiedAtDesc(weather, formatStartDate, formatEndDate, pageable);

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    public TodoResponse getTodo(long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }

    public Page<TodoDetailResponse> getDetailTodos(int page, int size, String keyword, String startDate, String endDate, String manager) {
        Pageable pageable = PageRequest.of(page - 1, size);
        LocalDateTime formatStartDate =
                (startDate != null) ? LocalDateTime.of(LocalDate.parse(startDate), LocalTime.MIN)
                    : LocalDateTime.of(LocalDate.parse("1000-01-01"), LocalTime.MIN);

        // LocalTime.MAX 값은 '23:59:59.9999999....' 이라서 그대로 사용하면 올림이 되어 00이 됨.
        // withNano로 밀리세컨드 단위 지움
        LocalDateTime formatEndDate =
                (endDate != null) ? LocalDateTime.of(LocalDate.parse(endDate), LocalTime.MAX)
                    : LocalDateTime.of(LocalDate.parse("9999-12-31"), LocalTime.MAX).withNano(0);

        return todoRepository.findDetailRequest(keyword, formatStartDate, formatEndDate, manager, pageable);
    }
}
