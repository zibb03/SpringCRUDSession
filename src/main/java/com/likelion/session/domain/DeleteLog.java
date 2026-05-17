package com.likelion.session.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "delete_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeleteLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long boardId;        // 삭제된 게시글 id
    private String boardTitle;   // 삭제된 게시글 제목
    private String writer;       // 작성자
    private LocalDateTime deletedAt;  // 삭제 시간

    public DeleteLog(Long boardId, String boardTitle, String writer) {
        this.boardId = boardId;
        this.boardTitle = boardTitle;
        this.writer = writer;
        this.deletedAt = LocalDateTime.now();
    }
}