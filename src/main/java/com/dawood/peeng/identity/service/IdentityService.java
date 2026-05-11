package com.dawood.peeng.identity.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dawood.peeng.identity.dtos.request.RegisterDTO;
import com.dawood.peeng.identity.enums.RoleType;
import com.dawood.peeng.identity.exceptions.EmailAlreadyExistsException;
import com.dawood.peeng.identity.models.User;
import com.dawood.peeng.identity.repository.UserRepository;
import com.dawood.peeng.membership.enums.MembershipStatus;
import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.membership.repository.MembershipRepository;
import com.dawood.peeng.tenant.model.Tenant;
import com.dawood.peeng.tenant.repository.TenantRepository;
import com.dawood.peeng.utils.SlugUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdentityService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final MembershipRepository membershipRepository;
  private final TenantRepository tenantRepository;

  @Transactional
  public void register(RegisterDTO payload) {

    if (userRepository.existsByEmailAllIgnoreCase(payload.getEmail())) {
      throw new EmailAlreadyExistsException("Email address already exists");
    }

    User newUser = User.builder()
        .email(payload.getEmail())
        .passwordHash(passwordEncoder.encode(payload.getPassword()))
        .name(payload.getName())
        .build();

    userRepository.save(newUser);

    Tenant newTenant = Tenant.builder()
        .workspaceName(payload.getWorkspaceName())
        .slug(SlugUtils.makeUniqueSlug(payload.getWorkspaceName()))
        .owner(newUser)
        .settings("")
        .build();

    tenantRepository.save(newTenant);

    Membership newMembership = Membership.builder()
        .user(newUser)
        .role(RoleType.OWNER)
        .tenant(newTenant)
        .joinedAt(LocalDateTime.now())
        .status(MembershipStatus.ACTIVE)
        .build();

    membershipRepository.save(newMembership);

  }

}
