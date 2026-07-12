package com.orderplatform.auth_service.service;

import com.orderplatform.auth_service.DTO.AuthResponse;
import com.orderplatform.auth_service.DTO.LoginRequest;
import com.orderplatform.auth_service.DTO.RegisterRequest;
import com.orderplatform.auth_service.entity.Role;
import com.orderplatform.auth_service.entity.User;
import com.orderplatform.auth_service.exception.EmailAlreadyExistsException;
import com.orderplatform.auth_service.exception.InvalidCredentialException;
import com.orderplatform.auth_service.repository.UserRepository;
import com.orderplatform.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public void register(RegisterRequest request){
        log.info("register request received with email: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            log.debug("email {} already exists", request.email());
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);
        log.info("User registered successfully with email: {}", request.email());
    }

    public AuthResponse login(LoginRequest request){
        log.info("login request received with email: {}", request.email());
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new InvalidCredentialException();
        }

        return new AuthResponse(jwtService.generateToken(user), expirationMs);
    }
}
