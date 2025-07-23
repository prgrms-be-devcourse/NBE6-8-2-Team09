package com.back.back9.domain.user.service;

import com.back.back9.domain.user.dto.UserDto;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import com.back.back9.global.rsData.RsData;
import com.back.back9.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RsData<User> register(UserDto userDto) {
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            return RsData.of("400", "비밀번호 확인이 일치하지 않습니다.");
        }
        if (userRepository.findByUserLoginId(userDto.getUserLoginId()).isPresent()) {
            return new RsData<>("400-1", "이미 존재하는 아이디입니다.");
        }

        User user = User.builder()
                .userLoginId(userDto.getUserLoginId())
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .role(User.UserRole.MEMBER)
                .build();

        userRepository.save(user);
        return new RsData<>("200-1", "회원가입이 완료되었습니다.", user);
    }

    @Override
    public UserDetails loadUserByUsername(String userLoginId) throws UsernameNotFoundException {
        User user = userRepository.findByUserLoginId(userLoginId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다: " + userLoginId));

        return new SecurityUser(user);
    }

    public Optional<User> findByUserLoginId(String userLoginId) {
        return userRepository.findByUserLoginId(userLoginId);
    }
}
