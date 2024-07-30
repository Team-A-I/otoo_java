package com.project.otoo_java.users.controller;

import com.project.otoo_java.login.model.AccountService;
import com.project.otoo_java.users.model.dto.UsersDto;
import com.project.otoo_java.users.model.entity.Users;
import com.project.otoo_java.users.model.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

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

    // 유저 밴 상태 변경(밴X -> 밴O)
    @PostMapping("/admin/changeStatusBan")
    public ResponseEntity<?> changeStatusBan(@RequestBody UsersDto usersDto) throws Exception {
        try {
            usersService.updateStatusBan(usersDto.getUsersCode());
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 유저 밴 상태 변경(밴O -> 밴X)
    @PostMapping("/admin/changeStatusNotBan")
    public ResponseEntity<?> changeStatusNotBan(@RequestBody UsersDto usersDto) throws Exception {
        try {
            usersService.updateStatusNotBan(usersDto.getUsersCode());
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 유저 정보 전체 가져오기
    @GetMapping("/getAllUser")
    public List<Users> userList(){
        return usersService.usersList();
    }

    // check box 따라 선택지 다르게 출력

    // 성별 1개 선택 시 출력
    @GetMapping("/admin/getGenderOne/{usersGender}")
    public List<Users> userGenderList(@PathVariable(value="usersGender") String usersGender){
        log.info("유저 성별 1개 선택 시 조회 메소드 시작");
        log.info(usersGender);
        return usersService.usersGenderOneList(usersGender);
    }

    // 계정 상태 1개 선택 시 출력
    @GetMapping("/admin/getBanOne/{usersBan}")
    public List<Users> userBanList(@PathVariable(value="usersBan") String usersBan){
        log.info("유저 밴 상태 1개 선택 시 조회 메소드 시작");
        log.info(usersBan);
        return usersService.usersBanOneList(usersBan);
    }

    // 마이페이지 - 로그인한 계정 정보 조회 by usersCode
    @GetMapping("/user/getOneUser/{usersCode}")
    public Optional<Users> userOneList(@PathVariable(value="usersCode") String usersCode){
        return usersService.getOneUser(usersCode);
    }

    // 마이페이지 - 로그인한 계정 정보 조회 by usersCode
    @GetMapping("/user/deleteUser/{usersCode}")
    public void userDelete(@PathVariable(value="usersCode") String usersCode){
        usersService.deleteUser(usersCode);
    }

    // 마이페이지 - 로그인한 계정 정보 수정
    @PostMapping("/user/changeUser")
    public Users userUpdate(@RequestBody Users user){
        return usersService.updateUser(user);
    }
}
