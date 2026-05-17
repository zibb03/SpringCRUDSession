package com.likelion.session.controller;

import com.likelion.session.dto.BoardCreateRequest;
import com.likelion.session.dto.BoardResponse;
import com.likelion.session.dto.BoardUpdateRequest;
import com.likelion.session.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 6주차 추가
import com.likelion.session.dto.BoardPageResponse;

import com.likelion.session.dto.DeleteLogResponse;

import java.util.List;

@RestController // JSON 형태로 응답을 보내는 컨트롤러
@RequestMapping("/boards") // 이 컨트롤러의 모든 URL에 /boards를 붙여줌
@RequiredArgsConstructor // final이 붙은 필드(boardService)를 생성자로 자동 주입해줌
public class BoardController {

    private final BoardService boardService;

    /*
        게시글 생성

        [요청 흐름]
        Client
        -> DispatcherServlet
        -> HandlerMapping
        -> BoardController의 create() 메서드 선택
        -> Service 호출
        -> Repository 호출
        -> DB 저장
        -> 결과 반환
        -> JSON 응답
     */
    @Operation(
            summary = "게시글 생성",
            description = "새로운 게시글을 생성합니다."
    )
    @PostMapping // 데이터 생성
    public ResponseEntity<BoardResponse> create(@RequestBody BoardCreateRequest request) {
        // @RequestBody: 클라이언트가 보낸 JSON 데이터를 자바 객체로 변환해줌
        BoardResponse response = boardService.create(request);
        return ResponseEntity.ok(response);
    }

    // 게시글 전체 조회
    @Operation(
            summary = "게시글 전체 조회",
            description = "등록된 모든 게시글을 조회합니다."
    )
    @GetMapping // 데이터 조회
    public ResponseEntity<List<BoardResponse>> findAll() {
        List<BoardResponse> response = boardService.findAll();
        return ResponseEntity.ok(response);
    }

    // 게시글 단건 조회
    @Operation(
            summary = "게시글 단건 조회",
            description = "id로 특정 게시글을 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> findById(@PathVariable Long id) {
        // @PathVariable: URL 경로에 있는 {id} 값을 파라미터 변수로 받아옴
        BoardResponse response = boardService.findById(id);
        return ResponseEntity.ok(response);
    }

    // 게시글 수정
    @Operation(
            summary = "게시글 수정",
            description = "id로 특정 게시글의 제목과 내용을 수정합니다."
    )
    @PutMapping("/{id}") // 데이터 수정
    public ResponseEntity<BoardResponse> update(@PathVariable Long id,
                                                @RequestBody BoardUpdateRequest request) {
        BoardResponse response = boardService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // 게시글 삭제
    @Operation(
            summary = "게시글 삭제",
            description = "id로 특정 게시글을 삭제합니다."
    )
    @DeleteMapping("/{id}") // 데이터 삭제
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // BoardController.java 에 아래 메서드들 추가

    // 제목으로 검색
    @Operation(
            summary = "제목으로 게시글 검색",
            description = "제목에 키워드가 포함된 게시글을 조회합니다."
    )
    @GetMapping("/search/title") // 경로만 다르고 패턴 동일
    public ResponseEntity<List<BoardResponse>> searchByTitle(@RequestParam String keyword) {
        List<BoardResponse> response = boardService.searchByTitle(keyword);
        return ResponseEntity.ok(response);
    }

    // 작성자로 검색
    @Operation(
            summary = "작성자로 게시글 검색",
            description = "특정 작성자의 게시글을 조회합니다."
    )
    @GetMapping("/search/writer") // 경로만 다르고 패턴 동일
    public ResponseEntity<List<BoardResponse>> searchByWriter(@RequestParam String writer) {
        List<BoardResponse> response = boardService.searchByWriter(writer);
        return ResponseEntity.ok(response);
    }

    // 통합 검색
    @Operation(
            summary = "게시글 통합 검색",
            description = "제목 또는 내용에 키워드가 포함된 게시글을 조회합니다."
    )
    @GetMapping("/search") // 경로만 다르고 패턴 동일
    public ResponseEntity<List<BoardResponse>> search(@RequestParam String keyword) {
        List<BoardResponse> response = boardService.search(keyword);
        return ResponseEntity.ok(response);
    }

    // 페이지네이션 전체 조회
    @Operation(
            summary = "게시글 페이지 조회",
            description = "페이지네이션이 적용된 게시글 목록을 조회합니다."
    )
    @GetMapping("/page") // 경로만 다르고 패턴 동일
    public ResponseEntity<BoardPageResponse> findAllWithPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        BoardPageResponse response = boardService.findAllWithPage(page, size);
        return ResponseEntity.ok(response);
    }

    // 삭제 이력 조회
    @Operation(summary = "삭제 이력 조회", description = "삭제된 게시글의 이력을 조회합니다.")
    @GetMapping("/delete-logs")
    public ResponseEntity<List<DeleteLogResponse>> findAllDeleteLogs() {
        return ResponseEntity.ok(boardService.findAllDeleteLogs());
    }
}