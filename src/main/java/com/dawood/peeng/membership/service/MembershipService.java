package com.dawood.peeng.membership.service;

import com.dawood.peeng.membership.dtos.responses.MembershipResponseDTO;
import com.dawood.peeng.membership.enums.MembershipStatus;
import com.dawood.peeng.membership.repository.MembershipRepository;
import com.dawood.peeng.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;

    public List<MembershipResponseDTO> getAllMembersByTenant() {

        UUID tenantId = TenantContext.getTenantId();

        return membershipRepository.findByTenantIdAndMembershipStatus(tenantId, MembershipStatus.ACTIVE)
                .stream()
                .map(membership -> new MembershipResponseDTO(membership.getId(), membership.getUser().getName())
                )
                .toList();

    }

}
