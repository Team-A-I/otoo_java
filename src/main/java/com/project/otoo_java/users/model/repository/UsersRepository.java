package com.project.otoo_java.users.model.repository;

import com.project.otoo_java.users.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, String> {
    Optional<Users> findByUsersEmail(String usersEmail);
    boolean existsUsersByUsersEmail(String usersEmail);

    @Modifying
    @Query("UPDATE Users m Set m.usersPw = :cryptedPwd WHERE m.usersCode = :usersCode")
    void updateUsersPw(@Param("usersCode") String usersId, @Param("cryptedPwd") String cryptedPwd);

    @Modifying
    @Query("UPDATE Users m Set m.usersPw = :usersPw WHERE m.usersEmail = :usersEmail")
    void updatePwdByEmail(@Param("usersPw") String usersPw, @Param("usersEmail") String usersEmail);

    @Modifying
    @Query("UPDATE Users m Set m.usersBan = :usersBan WHERE m.usersCode = :usersCode")
    void updateStatus(@Param("usersCode") String usersId, @Param("usersBan") String usersBan);

    List<Users> findByUsersRoleNot(String usersRole);

    // usersGender 남자, 여자 선택에 사용할 Query
    List<Users> findByUsersGender(String usersGender);

    // usersBan Y, N 선택에 사용할 Query
    List<Users> findByUsersBan(String usersBan);

    //마이페이지 - 유저 조회
    Optional<Users> findByUsersCode(String usersCode);

    //마이페이지 - 유저 삭제
    void deleteByUsersCode(String usersCode);

    //마이페이지 - 유저 수정
    //Optional<Users> updateUsers(Users users);
}
