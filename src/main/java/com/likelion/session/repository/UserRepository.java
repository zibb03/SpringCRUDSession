package com.likelion.session.repository;

import com.likelion.session.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByUsername(String username); // SQL 자동으로 메소드 이름 보고 만들음

    //username을 받아 DB 테이블에서 회원을 조회하는 메소드 작성
    User findByUsername(String username);
}
