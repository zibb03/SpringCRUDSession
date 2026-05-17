package com.likelion.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DeleteLogResponse {
    private Long id;
    private Long boardId;
    private String boardTitle;
    private String writer;
    private LocalDateTime deletedAt;
}