package com.dawood.peeng.tenant.service;

import com.dawood.peeng.identity.enums.RoleType;
import com.dawood.peeng.identity.models.User;
import com.dawood.peeng.identity.repository.UserRepository;
import com.dawood.peeng.identity.service.IdentityService;
import com.dawood.peeng.membership.enums.MembershipStatus;
import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.membership.repository.MembershipRepository;
import com.dawood.peeng.tenant.dtos.request.CreateTenantRequest;
import com.dawood.peeng.tenant.dtos.response.TenantSessionDTO;
import com.dawood.peeng.tenant.mapper.TenantMapper;
import com.dawood.peeng.tenant.model.Tenant;
import com.dawood.peeng.tenant.repository.TenantRepository;
import com.dawood.peeng.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private  final TenantRepository tenantRepository;
    private final IdentityService identityService;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    @Transactional
    public TenantSessionDTO createTenant(CreateTenantRequest request){

        User owner = identityService.getCurrentLoggedInUser();

        Tenant newTenant = new Tenant();
        newTenant.setWorkspaceName(request.workspaceName());
        newTenant.setSlug(SlugUtils.makeUniqueSlug(request.workspaceName()));
        newTenant.setOwner(owner);
        Tenant savedTenant = tenantRepository.save(newTenant);

        Membership newMembership = new Membership();
        newMembership.setUser(owner);
        newMembership.setRole(RoleType.OWNER);
        newMembership.setTenant(savedTenant);
        newMembership.setJoinedAt(LocalDateTime.now());
        newMembership.setStatus(MembershipStatus.ACTIVE);
        membershipRepository.save(newMembership);

        owner.setLastActiveTenantId(savedTenant.getId());
        userRepository.save(owner);

        return TenantMapper.toTenantSessionDTO(savedTenant);

    }

    @Transactional(readOnly = true)
    public List<TenantSessionDTO> listMyWorkspaces() {
        User currentUser = identityService.getCurrentLoggedInUser();

        List<Membership> memberships = membershipRepository.findByUserIdAndStatusNot(
                currentUser.getId(),
                MembershipStatus.REMOVED
        );

        return memberships.stream()
                .map(TenantMapper::toTenantSessionDTO)
                .toList();
    }
}
