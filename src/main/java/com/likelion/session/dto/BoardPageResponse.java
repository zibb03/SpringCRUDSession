package com.likelion.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder // 만들어주는거니까
@AllArgsConstructor
public class BoardPageResponse {
    // 페이지네이션
    private List<BoardResponse> content;   // 게시글 목록
    private int totalPages;                // 전체 페이지 수
    private long totalElements;            // 전체 게시글 수
    private int currentPage;              // 현재 페이지
    private int size;                     // 페이지당 개수
}