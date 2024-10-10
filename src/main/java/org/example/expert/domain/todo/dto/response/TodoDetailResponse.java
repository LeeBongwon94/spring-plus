package org.example.expert.domain.todo.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class TodoDetailResponse {
    private String todoTitle;
    private int managerNumber;
    private int commentNumber;

    public TodoDetailResponse(String todoTitle, int managerNumber, int commentNumber) {
        this.todoTitle = todoTitle;
        this.managerNumber = managerNumber;
        this.commentNumber = commentNumber;
    }
}
