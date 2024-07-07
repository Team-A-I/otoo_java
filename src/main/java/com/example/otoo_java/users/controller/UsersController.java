package com.example.otoo_java.users.controller;

import com.example.otoo_java.login.model.AccountService;
import com.example.otoo_java.users.model.dto.UsersDto;
import com.example.otoo_java.users.model.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
public class UsersController {
    private final UsersService usersService;
    private final AccountService accountService;

    //회원 등록
    @Operation(summary = "회원가입", description = "회원가입 API")
    @PostMapping("/join")
    public ResponseEntity<UsersDto> insertMember(@RequestBody UsersDto usersDto) {

        try {
            return new ResponseEntity<>(usersService.insertUser(usersDto), HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 등록된 이메일 주소입니다");
        }

    }
    @PatchMapping("/users")
    public ResponseEntity<?> updatePassword(@RequestBody UsersDto usersDto) throws Exception {
        try {
            // 현재 비밀번호와 같은 비밀번호라면 Exception
            accountService.checkFormerPwd(usersDto);
            // 위 코드를 통과했다면 서비스 호출
            usersService.updatePassword(usersDto);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    //비밀번호 변경
    @PostMapping("/changePwd")
    public ResponseEntity<?> changePwd(@RequestBody UsersDto usersDto) throws Exception {
        try {
            usersService.updatePasswordByEmail(usersDto);
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
