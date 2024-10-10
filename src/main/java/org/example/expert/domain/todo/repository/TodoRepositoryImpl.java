package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoDetailResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        return Optional.ofNullable(jpaQueryFactory
                .select(todo)
                .from(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne());
    }

    @Override
    public Page<TodoDetailResponse> findDetailRequest(String keyword, LocalDateTime startDate, LocalDateTime endDate, String manager, Pageable pageable) {

        List<TodoDetailResponse> query = jpaQueryFactory
                .select(
                        Projections.fields(TodoDetailResponse.class,
                                todo.title.as("todoTitle"),
                                todo.managers.size().as("managerNumber"),
                                todo.comments.size().as("commentNumber")
                        )
                )
                .from(todo)
                .where(
                        containKeyword(keyword),
                        searchDate(startDate, endDate),
                        containManager(manager)
                )
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(todo.count())
                .from(todo)
                .where(containKeyword(keyword),
                       searchDate(startDate, endDate),
                       containManager(manager)
                )
                .fetchOne();

        if(total == null) total = 0L;

        return new PageImpl<>(query, pageable, total);
    }

    private BooleanExpression containKeyword(String keyword) {
        return (keyword != null && !keyword.isEmpty()) ? todo.title.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression searchDate(LocalDateTime startDate, LocalDateTime endDate) {
        // goe : '.goe' 앞의 값이 뒤의 값보다 크거나 같다.
        // BooleanExpression isGoeStartDate = todo.createdAt.goe(startDate);
        // loe : '.loe' 뒤의 값이 앞의 값보다 크거나 같다.
        // BooleanExpression isLoeEndDate = todo.createdAt.loe(endDate);
        // return Expressions.allOf(isGoeStartDate, isLoeEndDate);

        return (startDate != null && endDate != null) ? todo.createdAt.between(startDate, endDate) : null;
    }

    private BooleanExpression containManager(String manager) {
        return (manager != null && !manager.isEmpty()) ? todo.managers.any().user.nickname.containsIgnoreCase(manager) : null;
    }
}