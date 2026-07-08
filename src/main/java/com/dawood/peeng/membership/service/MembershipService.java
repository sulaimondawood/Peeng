package com.dawood.peeng.membership.service;

import com.dawood.peeng.membership.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;

    public void getAllMembersByTenant(){

    }

}
