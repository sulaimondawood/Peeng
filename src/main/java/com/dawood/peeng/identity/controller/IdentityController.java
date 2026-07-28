package com.dawood.peeng.identity.controller;

import com.dawood.peeng.identity.dtos.request.*;
import com.dawood.peeng.identity.dtos.response.VerifyEmailResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dawood.peeng.common.dto.ApiResponse;
import com.dawood.peeng.identity.dtos.response.LoginResponseDTO;
import com.dawood.peeng.identity.dtos.response.RegisterResponseDTO;
import com.dawood.peeng.identity.service.IdentityService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class IdentityController {

    private final IdentityService identityService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDTO>> register(@RequestBody @Valid RegisterDTO payload) {

        RegisterResponseDTO response = identityService.register(payload);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response.getMessage(), response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody @Valid LoginDTO payload) {

        LoginResponseDTO response = identityService.login(payload);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Login successful", response));
    }

    @PatchMapping("/profile/name")
    public ResponseEntity<ApiResponse<Void>> updateName(@Valid @RequestBody UpdateNameRequest request) {
        identityService.updateName(request.getName());
        return ResponseEntity.ok(ApiResponse.success("Name updated successfully", null));
    }

    @PutMapping("/profile/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@RequestBody UpdatePasswordRequest request) {
        identityService.updatePassword(
                request.getCurrentPassword(),
                request.getNewPassword(),
                request.getConfirmNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully", null));
    }


    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> verifyEmail(@RequestParam("token") String token) {
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", identityService.verifyEmail(token)));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail() {
        identityService.resendVerificationEmail();
        return ResponseEntity.ok(ApiResponse.success("Verification email sent successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        identityService.forgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "If an account exists for this email, a reset link has been sent.",
                        null
                )
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        identityService.resetPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Password reset successfully. You can now sign in.", null)
        );
    }


}
