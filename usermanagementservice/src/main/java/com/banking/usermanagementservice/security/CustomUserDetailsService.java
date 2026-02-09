package com.banking.usermanagementservice.security;

import com.banking.usermanagementservice.entity.User;
import com.banking.usermanagementservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("loading user by email: {}", email);

        User user = userRepository.findByEmailAndNotDeleted(email.toLowerCase())
                .orElseThrow(()-> new UsernameNotFoundException("User not found with email: " + email));

        if (!user.isActive()){
            throw new UsernameNotFoundException("User account is not active");
        }

        return new CustomUserDetails(user);
    }

    public static class CustomUserDetails implements UserDetails{

        private final User user;

        public CustomUserDetails(User user){
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                    .collect(Collectors.toSet());
        }

        @Override
        public @Nullable String getPassword() {
            return null;
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.isActive();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.isActive() && !user.isDeleted();
        }

        public User getUser(){
            return user;
        }
    }
}
