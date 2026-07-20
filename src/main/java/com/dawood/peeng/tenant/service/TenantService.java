package com.dawood.peeng.tenant.service;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.identity.enums.RoleType;
import com.dawood.peeng.identity.exceptions.UnauthorizedException;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    @Transactional
    public TenantSessionDTO switchWorkspace(UUID targetTenantId) {
        User currentUser = identityService.getCurrentLoggedInUser();

        Membership membership = membershipRepository.findByUser_IdAndTenant_Id(currentUser.getId(), targetTenantId)
                .orElseThrow(() -> new UnauthorizedException(
                        "You do not have access to this workspace",
                        HttpStatus.UNAUTHORIZED,
                        ErrorCode.UNAUTHORIZED
                ));

        if (membership.getStatus() == MembershipStatus.REMOVED) {
            throw new UnauthorizedException(
                    "Your access to this workspace has been revoked",
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED
            );
        }

        if (membership.getStatus() == MembershipStatus.INVITED) {
            throw new UnauthorizedException(
                    "Please accept the invitation before switching to this workspace",
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED
            );
        }

        if (membership.getStatus() == MembershipStatus.SUSPENDED) {
            throw new UnauthorizedException(
                    "Your account has been suspended in this workspace. Please contact the administrator.",
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED
            );
        }

        currentUser.setLastActiveTenantId(targetTenantId);
        userRepository.save(currentUser);

        return TenantMapper.toTenantSessionDTO(membership.getTenant());
    }
}
