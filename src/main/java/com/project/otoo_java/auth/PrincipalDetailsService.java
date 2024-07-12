package com.project.otoo_java.auth;

import com.project.otoo_java.users.model.entity.Users;
import com.project.otoo_java.users.model.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

// 시큐리티 설정에서
@Slf4j
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername: {}", username);
        Optional<Users> usersEntity = usersRepository.findByUsersEmail(username);

        Users users  = usersEntity.orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수가 없습니다"));

        return new PrincipalDetails(users);
    }
}
