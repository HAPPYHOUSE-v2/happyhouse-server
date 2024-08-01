package com.ormi.happyhouse.member.repository;

import com.ormi.happyhouse.member.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    //닉네임 중복 확인
    boolean existsByNickname(String nickname);

}
