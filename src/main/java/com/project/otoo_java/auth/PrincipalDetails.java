package com.project.otoo_java.auth;

import com.project.otoo_java.users.model.entity.Users;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@NoArgsConstructor
public class PrincipalDetails implements UserDetails {

    private Users users;

    public PrincipalDetails(Users users) {
        this.users = users;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                return users.getUsersRole();
            }
        });
        return collect;
    }

    @Override
    public String getPassword() {
        return users.getUsersPw();
    }

    @Override
    public String getUsername() {
        return users.getUsersName();
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }



    @Override
    public boolean isAccountNonLocked() {
        return true;
    }


    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }


    @Override
    public boolean isEnabled() {
        return false;
    }
}
