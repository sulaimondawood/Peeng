package com.dawood.peeng.identity.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dawood.peeng.identity.dtos.request.RegisterDTO;
import com.dawood.peeng.identity.models.User;
import com.dawood.peeng.identity.repository.UserRepository;
import com.dawood.peeng.tenant.model.Tenant;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdentityService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void register(RegisterDTO payload) {

    User newUser = User.builder()
        .email(payload.getEmail())
        .passwordHash(passwordEncoder.encode(payload.getPassword()))
        .name(payload.getName())
        .build();

    Tenant newTenant = Tenant.builder()
        .workspaceName("")
        .slug("")
        .owner(newUser)
        .settings("")
        .build();

  }

}
