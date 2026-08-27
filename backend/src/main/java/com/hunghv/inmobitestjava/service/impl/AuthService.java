package com.hunghv.inmobitestjava.service.impl;

import com.hunghv.inmobitestjava.entity.UserAccount;
import com.hunghv.inmobitestjava.exception.BadRequestException;
import com.hunghv.inmobitestjava.exception.ConflictException;
import com.hunghv.inmobitestjava.exception.ResourceNotFoundException;
import com.hunghv.inmobitestjava.exception.UnauthorizedException;
import com.hunghv.inmobitestjava.generated.model.AuthResponse;
import com.hunghv.inmobitestjava.generated.model.ForgotPasswordRequest;
import com.hunghv.inmobitestjava.generated.model.ForgotPasswordResponse;
import com.hunghv.inmobitestjava.generated.model.LoginRequest;
import com.hunghv.inmobitestjava.generated.model.RegisterRequest;
import com.hunghv.inmobitestjava.generated.model.RegisterResponse;
import com.hunghv.inmobitestjava.generated.model.ResetPasswordRequest;
import com.hunghv.inmobitestjava.mapper.UserMapper;
import com.hunghv.inmobitestjava.repository.UserRepository;
import com.hunghv.inmobitestjava.security.UserPrincipal;
import com.hunghv.inmobitestjava.service.IAuthService;
import com.hunghv.inmobitestjava.service.IOtpService;
import com.hunghv.inmobitestjava.service.JwtService;
import com.hunghv.inmobitestjava.utils.EmailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final IOtpService otpService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = EmailUtils.normalizeEmail(request.getEmail());
        try {
            UserAccount user = userRepository.save(new UserAccount(email, passwordEncoder.encode(request.getPassword())));
            log.info("Registered user successfully: userId={}, email={}", user.getId(), user.getEmail());
            return userMapper.toRegisterResponse(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Email is already registered");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = EmailUtils.normalizeEmail(request.getEmail());
        UserAccount user = userRepository.findByEmail(email)
            .orElseThrow(AuthService::invalidCredentials);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        log.info("User login successfully: userId={}, email={}", user.getId(), user.getEmail());
        return userMapper.toAuthResponse(jwtService.generateToken(UserPrincipal.from(user)));
    }

    @Override
    @Transactional
    public ForgotPasswordResponse requestForgotPasswordOtp(ForgotPasswordRequest request) {
        String email = EmailUtils.normalizeEmail(request.getEmail());
        UserAccount user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User email not found"));

        String otp = otpService.generateOtp(user.getEmail());
        String msg = otpService.isBypassEnabled()
            ? "OTP sent to email (Bypass enabled: You can use any 6-digit code or check server logs)"
            : "OTP has been sent to your email (Simulated in server console logs)";

        return new ForgotPasswordResponse().email(user.getEmail()).message(msg);
    }

    @Override
    @Transactional
    public ForgotPasswordResponse resetPasswordWithOtp(ResetPasswordRequest request) {
        String email = EmailUtils.normalizeEmail(request.getEmail());
        UserAccount user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User email not found"));

        if (!otpService.validateOtp(user.getEmail(), request.getOtp())) {
            throw new BadRequestException("Invalid or expired OTP code");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpService.clearOtp(user.getEmail());

        log.info("Reset password successfully for user: userId={}, email={}", user.getId(), user.getEmail());
        return new ForgotPasswordResponse()
            .email(user.getEmail())
            .message("Password reset successfully. You can now login with your new password.");
    }

    private static UnauthorizedException invalidCredentials() {
        return new UnauthorizedException("Email or password is incorrect");
    }
}
