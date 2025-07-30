package com.back.back9.domain.user.service;

import com.back.back9.domain.user.dto.UserRegisterDto;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import com.back.back9.global.exception.ServiceException;
import com.back.back9.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;


    public RsData<User> register(UserRegisterDto dto) {
        if (!dto.password().equals(dto.confirmPassword())) {
            return new RsData<>("400", "비밀번호 확인이 일치하지 않습니다.");
        }
        if (userRepository.findByUserLoginId(dto.userLoginId()).isPresent()) {
            return new RsData<>("400-1", "이미 존재하는 아이디입니다.");
        }
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            return new RsData<>("400-2", "이미 존재하는 유저이름입니다.");
        }

        String apiKey = UUID.randomUUID().toString();

        User user = User.builder()
                .userLoginId(dto.userLoginId())
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password()))
                .role(User.UserRole.MEMBER)
                .apiKey(apiKey)
                .build();

        userRepository.save(user);
        return new RsData<>("200-1", "회원가입이 완료되었습니다.", user);
    }

    public RsData<User> registerAdmin(UserRegisterDto dto) {
        if (!dto.password().equals(dto.confirmPassword())) {
            return new RsData<>("400-0", "비밀번호 확인이 일치하지 않습니다.");
        }
        if (userRepository.findByUserLoginId(dto.userLoginId()).isPresent()) {
            return new RsData<>("400-1", "이미 존재하는 아이디입니다.");
        }

        if (userRepository.findByUsername(dto.username()).isPresent()) {
            return new RsData<>("400-2", "이미 존재하는 유저이름입니다.");
        }

        String apiKey = UUID.randomUUID().toString();

        User user = User.builder()
                .userLoginId(dto.userLoginId())
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password()))
                .role(User.UserRole.ADMIN)
                .apiKey(apiKey)
                .build();

        userRepository.save(user);
        return new RsData<>("200-1", "관리자 회원가입이 완료되었습니다.", user);
    }

    public Optional<User> findByUserLoginId(String userLoginId) {
        return userRepository.findByUserLoginId(userLoginId);
    }

    public void deleteByUserLoginId(String userLoginId) {
        User user = userRepository.findByUserLoginId(userLoginId)
                .orElseThrow(() -> new ServiceException("404", "해당 아이디의 사용자가 없습니다."));
        userRepository.delete(user);
    }

    public Optional<User> findByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey);
    }

    public List<User> searchByUsername(String keyword) {
        return userRepository.findByUsernameContaining(keyword);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Map<String, Object> getPayloadFromToken(String accessToken) {
        return authTokenService.payload(accessToken);
    }

    public String genAccessToken(User user) {
        return authTokenService.genAccessToken(user);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void checkPassword(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new ServiceException("401-1", "비밀번호가 일치하지 않습니다.");
        }
    }
}