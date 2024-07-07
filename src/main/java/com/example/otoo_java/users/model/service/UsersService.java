package com.example.otoo_java.users.model.service;

import com.example.otoo_java.users.model.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.example.otoo_java.users.model.dto.UsersDto;
import com.example.otoo_java.users.model.entity.Users;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UsersService {

    private final UsersRepository usersRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${oauth.kakao.client_id}")
    private String kakaoClientId;

    //로그인
    //User 전체 조회
    public List<Users> usersList() {
        return usersRepository.findByUsersRoleNot("ROLE_ADMIN");
    }

    //Users 유저 코드로 조회
    public  Users getUsersByUsersCode(String usersCode) {
        Users usersEntity = usersRepository.findById(usersCode).orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));

        return Users.builder()
                .usersCode(usersEntity.getUsersCode())
                .usersId(usersEntity.getUsersId())
                .usersPw(usersEntity.getUsersPw())
                .usersEmail(usersEntity.getUsersEmail())
                .usersGender(usersEntity.getUsersGender())
                .usersName(usersEntity.getUsersName())
                .usersRole(usersEntity.getUsersRole())
                .usersBan(usersEntity.getUsersBan())
                .oAuthProvider(usersEntity.getOAuthProvider())
                .build();
    }

    //user 등록
    @Transactional
    public UsersDto insertUser(UsersDto userDto) {
        boolean existEmail = usersRepository.existsUsersByUsersEmail(userDto.getUsersEmail());
        if (existEmail) {
            throw new IllegalArgumentException("이미 등록된 이메일 주소입니다.");
        }

        userDto.setUsersPw(bCryptPasswordEncoder.encode(userDto.getUsersPw()));

        Users usersEntity = Users.builder()
                .usersCode(userDto.getUsersCode())
                .usersId(userDto.getUsersId())
                .usersPw(userDto.getUsersPw())
                .usersEmail(userDto.getUsersEmail())
                .usersGender(userDto.getUsersGender())
                .usersName(userDto.getUsersName())
                .usersRole(userDto.getUsersRole())
                .usersBan(userDto.getUsersBan())
                .oAuthProvider(userDto.getOAuthProvider())
                .build();

        Users savedUser = usersRepository.save(usersEntity);

        return UsersDto.builder()
                .usersCode(savedUser.getUsersCode())
                .usersId(savedUser.getUsersId())
                .usersPw(savedUser.getUsersPw())
                .usersEmail(savedUser.getUsersEmail())
                .usersName(savedUser.getUsersName())
                .usersRole(savedUser.getUsersRole())
                .usersBan(savedUser.getUsersBan())
                .usersGender(savedUser.getUsersGender())
                .oAuthProvider(savedUser.getOAuthProvider())
                .build();
    }

    //비밀번호 변경
    @Transactional
    public void updatePassword(UsersDto usersDto) {
        String cryptedPwd = bCryptPasswordEncoder.encode(usersDto.getUsersPw());
        usersDto.setUsersPw(cryptedPwd);
        try {
            usersRepository.updateUsersPw(usersDto.getUsersCode(), cryptedPwd);
        } catch (Exception e) {
            log.info("실패\n" + e.getMessage());
        }
    }

    //비밀번호 변경
    @Transactional
    public void updatePasswordByEmail(UsersDto usersDto) {
        log.info(usersDto.getUsersEmail() + " " + usersDto.getUsersPw());
        String cryptedPwd = bCryptPasswordEncoder.encode(usersDto.getUsersPw());
        usersDto.setUsersPw(cryptedPwd);
        try {
            log.info(usersDto.getUsersEmail() + " " + usersDto.getUsersPw());
            usersRepository.updatePwdByEmail(usersDto.getUsersPw(), usersDto.getUsersEmail());
        } catch (Exception e) {
            log.info("실패\n" + e.getMessage());
        }
    }

    //회원 상태 변경
    @Transactional
    public void updateStatus(String usersCode) {

        Users users = usersRepository.findById(usersCode).orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));

        if (users.getUsersBan().equals("Y")) {
            users.setUsersBan("N");
        } else {
            users.setUsersBan("Y");
        }
        usersRepository.updateStatus(usersCode, users.getUsersBan());

    }
}
