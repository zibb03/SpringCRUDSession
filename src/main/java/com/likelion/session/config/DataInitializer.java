package com.likelion.session.config;

import com.likelion.session.domain.User;
import com.likelion.session.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public DataInitializer(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("boo")) return; // 이미 boo가 있으면 생성 안 함

        User user = new User();
        user.setUsername("boo");
        user.setPassword(bCryptPasswordEncoder.encode("1105"));
        user.setRole("ROLE_ADMIN");
        userRepository.save(user);

        System.out.println("✅ 테스트 사용자 생성");
    }
}