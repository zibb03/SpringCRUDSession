package com.likelion.session.dto;

import com.likelion.session.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

// Spring Security는 자기가 가진 규격에 대한 정보만 인식. 그 규격이 UserDetails
public class CustomUserDetails implements UserDetails {
    //User를 UserDetails 형식으로 바꾸어 줌. 바꾸어 주어서 어답터라고 부름

    private final User userEntity;

    public CustomUserDetails(User userEntity) {

        this.userEntity = userEntity;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // getAuthorities는 사용자의 권한 목록을 반환해주는 메소드

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {

                return userEntity.getRole();
            }
        });

        return collection;
    }

    @Override
    public String getPassword() {
        // User Password 반환

        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        // User Name 반환

        return userEntity.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료 여부 확인
        return true; // 메소드 이름에 non이 붙어 있어서 true 일 경우 계정 만료되지 않은 것
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠김 여부 확인
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 계정이 활성화 상태인지 확인
        return true;
    }

    @Override
    public boolean isEnabled() {

        return true;
    }
}