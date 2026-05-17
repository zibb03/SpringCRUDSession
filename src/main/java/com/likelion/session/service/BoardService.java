package com.likelion.session.service;

import com.likelion.session.domain.Board;
import com.likelion.session.dto.BoardCreateRequest;
import com.likelion.session.dto.BoardResponse;
import com.likelion.session.dto.BoardUpdateRequest;
import com.likelion.session.repository.BoardRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// 6주차
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.likelion.session.dto.BoardPageResponse;

import com.likelion.session.repository.DeleteLogRepository;
import com.likelion.session.domain.DeleteLog;

import com.likelion.session.dto.DeleteLogResponse;

import java.util.List;

@Slf4j // 로그를 남기기 위한 Logger 객체를 자동으로 생성해줌 (log.info() 등 사용 가능)
@Service // 이 클래스가 비즈니스 로직 담당임을 Spring에 알려줌.
@RequiredArgsConstructor // final이 붙은 필드(boardRepository)를 생성자로 자동 주입해줌 / 의존성 주입, DI라고 부름
@Transactional // DB 작업 도중 에러가 나면 모든 작업을 이전으로 롤백함
public class BoardService {

    private final BoardRepository boardRepository;

    /*
        게시글 생성
        - Controller가 넘겨준 요청 DTO를 받아서
        - Entity로 바꾼 뒤
        - Repository를 통해 DB에 저장
        - 저장된 결과를 Response DTO로 변환해서 반환
     */
    public BoardResponse create(BoardCreateRequest request) {
        Board board = new Board(
                request.getTitle(),
                request.getContent(),
                request.getWriter()
        );

        Board savedBoard = boardRepository.save(board);

        return BoardResponse.builder()
                .id(savedBoard.getId())
                .title(savedBoard.getTitle())
                .content(savedBoard.getContent())
                .writer(savedBoard.getWriter())
                .createdAt(savedBoard.getCreatedAt())
                .updatedAt(savedBoard.getUpdatedAt())
                .build();
    }

    /*
        게시글 전체 조회
        - DB에 있는 모든 게시글을 가져옴
        - Entity 리스트를 Response DTO 리스트로 변환
     */
    @Transactional(readOnly = true) // 조회 전용 설정으로 성능 최적화에 씀.
    public List<BoardResponse> findAll() {
        return boardRepository.findAll()
                .stream()
                .map(board -> BoardResponse.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .writer(board.getWriter())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .build())
                .toList();
    }

    /*
        게시글 단건 조회
        - id로 게시글 조회
        - 없으면 예외 발생
     */
    @Transactional(readOnly = true)
    public BoardResponse findById(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        return BoardResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getWriter())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    /*
        게시글 수정
        - 기존 게시글을 찾음
        - 엔티티의 update 메서드로 값 변경
        - JPA 변경 감지로 update 반영
     */
    public BoardResponse update(Long id, BoardUpdateRequest request) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        board.update(request.getTitle(), request.getContent());

        return BoardResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getWriter())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    /*
        게시글 삭제
        - id로 게시글을 찾고
        - 있으면 삭제

    public void delete(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        boardRepository.delete(board);
    }
     */

    // BoardService에 DeleteLogRepository 의존성 추가
    private final DeleteLogRepository deleteLogRepository;

    // 기존 delete() 메서드 수정
    public void delete(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        // 삭제 이력 저장 (Service에서 두 Repository를 조합!)
        DeleteLog log = new DeleteLog(board.getId(), board.getTitle(), board.getWriter());
        deleteLogRepository.save(log);

        // 게시글 삭제
        boardRepository.delete(board);
    }

    // BoardService.java 에 아래 메서드들 추가

    /*
        제목으로 검색
        - keyword가 포함된 게시글 목록 반환
     */
    @Transactional(readOnly = true)
    public List<BoardResponse> searchByTitle(String keyword) {
        return boardRepository.findByTitleContaining(keyword)
                .stream()
                .map(board -> BoardResponse.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .writer(board.getWriter())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .build())
                .toList();
    }

    /*
        작성자로 검색
     */
    @Transactional(readOnly = true)
    public List<BoardResponse> searchByWriter(String writer) {
        return boardRepository.findByWriter(writer)
                .stream()
                .map(board -> BoardResponse.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .writer(board.getWriter())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .build())
                .toList();
    }

    /*
        통합 검색 (제목 + 내용)
     */
    @Transactional(readOnly = true)
    public List<BoardResponse> search(String keyword) {
        return boardRepository.searchByKeyword(keyword)
                .stream()
                .map(board -> BoardResponse.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .writer(board.getWriter())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .build())
                .toList();
    }

    /*
        페이지네이션 적용 전체 조회
        - page: 몇 번째 페이지 (0부터 시작)
        - size: 페이지당 몇 개
     */
    @Transactional(readOnly = true)
    public BoardPageResponse findAllWithPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Board> boardPage = boardRepository.findAll(pageable);

        List<BoardResponse> content = boardPage.getContent()
                .stream()
                .map(board -> BoardResponse.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .writer(board.getWriter())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .build())
                .toList();

        return BoardPageResponse.builder()
                .content(content)
                .totalPages(boardPage.getTotalPages())
                .totalElements(boardPage.getTotalElements())
                .currentPage(boardPage.getNumber())
                .size(boardPage.getSize())
                .build();
    }

    @Transactional(readOnly = true)
    public List<DeleteLogResponse> findAllDeleteLogs() {
        return deleteLogRepository.findAll()
                .stream()
                .map(log -> DeleteLogResponse.builder()
                        .id(log.getId())
                        .boardId(log.getBoardId())
                        .boardTitle(log.getBoardTitle())
                        .writer(log.getWriter())
                        .deletedAt(log.getDeletedAt())
                        .build())
                .toList();
    }
}