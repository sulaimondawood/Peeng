package com.dawood.peeng.identity.service;

import org.springframework.stereotype.Service;

import com.dawood.peeng.identity.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdentityService {

  private final UserRepository userRepository;

}
