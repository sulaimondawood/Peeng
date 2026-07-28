package com.dawood.peeng.identity.service;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.common.exceptions.BadRequestException;
import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.identity.dtos.request.CompleteInviteRegistrationDTO;
import com.dawood.peeng.identity.dtos.request.MemberInviteDTO;
import com.dawood.peeng.identity.dtos.request.MemberRoleDTO;
import com.dawood.peeng.identity.dtos.response.InvitePreviewResponseDTO;
import com.dawood.peeng.identity.dtos.response.TeamOverview;
import com.dawood.peeng.identity.enums.RoleType;
import com.dawood.peeng.identity.enums.Status;
import com.dawood.peeng.identity.event.MemberInviteEvent;
import com.dawood.peeng.identity.exceptions.UnauthorizedException;
import com.dawood.peeng.identity.models.User;
import com.dawood.peeng.identity.repository.UserRepository;
import com.dawood.peeng.membership.dtos.responses.MembershipDTO;
import com.dawood.peeng.membership.enums.MembershipStatus;
import com.dawood.peeng.membership.exceptions.MembershipException;
import com.dawood.peeng.membership.mapper.MembershipMapper;
import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.membership.repository.MembershipRepository;
import com.dawood.peeng.notification.enums.NotificationChannel;
import com.dawood.peeng.notification.model.NotificationChannelConfig;
import com.dawood.peeng.notification.respository.NotificationChannelConfigRepository;
import com.dawood.peeng.tenant.context.TenantContext;
import com.dawood.peeng.tenant.exceptions.TenantException;
import com.dawood.peeng.tenant.model.Tenant;
import com.dawood.peeng.tenant.model.WorkspaceInviteToken;
import com.dawood.peeng.tenant.repository.TenantRepository;
import com.dawood.peeng.tenant.repository.WorkspaceInviteTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final UserRepository userRepository;
    private final IdentityService identityService;
    private final MembershipRepository membershipRepository;
    private final TenantRepository tenantRepository;
    private final RabbitTemplate rabbitTemplate;
    private final PasswordEncoder passwordEncoder;
    private final NotificationChannelConfigRepository notificationChannelConfigRepository;
    private final WorkspaceInviteTokenRepository inviteTokenRepository;

    @Transactional
    public void sendInvite(MemberInviteDTO request) {
        UUID tenantId = TenantContext.getTenantId();
        User currentLoggedInUser = identityService.getCurrentLoggedInUser();

        if (request.email().equalsIgnoreCase(currentLoggedInUser.getEmail())) {
            throw new MembershipException(
                    "You cannot invite yourself",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.BAD_REQUEST
                    );
        }
        if (request.role() == RoleType.OWNER) {
            throw new BadRequestException(
                    "Cannot invite as OWNER",
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN
                    );
        }

        Membership actorMembership = membershipRepository.findByUser_IdAndTenant_Id(currentLoggedInUser.getId(), tenantId)
                .orElseThrow(() -> new MembershipException(
                        "You do not belong to this workspace",
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST
                ));

        if (actorMembership.getRole() != RoleType.OWNER && actorMembership.getRole() != RoleType.ADMIN) {
            throw new UnauthorizedException(
                    "You're not authorized to perform this action",
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN);
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantException(
                        "Workspace does not exist",
                        HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND));

        User targetUser = userRepository.findByEmailIgnoreCase(request.email()).orElseGet(() -> {
            User newUser = new User();
            newUser.setStatus(Status.ACTIVE);
            newUser.setEmail(request.email());
            newUser.setEmailVerified(false);
            newUser.setName("Invited User");
            newUser.setLastActiveTenantId(tenantId);
            return userRepository.save(newUser);
        });

        Optional<Membership> existingMembershipOpt = membershipRepository
                .findByUser_IdAndTenant_Id(targetUser.getId(), tenantId);

        Membership membership;
        if (existingMembershipOpt.isPresent()) {
            membership = existingMembershipOpt.get();

            if (membership.getStatus() == MembershipStatus.ACTIVE) {
                throw new MembershipException(
                        "User is already a member of this workspace",
                        HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
            } else if (membership.getStatus() == MembershipStatus.REMOVED) {
                membership.setStatus(MembershipStatus.INVITED);
                membership.setInvitedByUserId(currentLoggedInUser.getId());
                membership.setRemovedBy(null);
                membership.setRemovedAt(null);
                membership.setRole(request.role());
            } else {
                throw new MembershipException(
                        "User is already invited to this workspace",
                        HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
            }
        } else {
            membership = new Membership();
            membership.setUser(targetUser);
            membership.setRole(request.role());
            membership.setTenant(tenant);
            membership.setInvitedByUserId(currentLoggedInUser.getId());
            membership.setStatus(MembershipStatus.INVITED);
        }

        membershipRepository.save(membership);

        inviteTokenRepository.deleteByUser_IdAndTenant_Id(targetUser.getId(), tenantId);

        String tokenValue = UUID.randomUUID().toString();

        WorkspaceInviteToken inviteToken = WorkspaceInviteToken.builder()
                .token(tokenValue)
                .user(targetUser)
                .tenant(tenant)
                .invitedBy(currentLoggedInUser)
                .role(request.role())
                .expiresAt(LocalDateTime.now().plusHours(48))
                .build();

        inviteTokenRepository.save(inviteToken);

        final String workspaceName = tenant.getWorkspaceName();
        final String inviterName = currentLoggedInUser.getName();
        final String inviteeEmail = request.email().trim().toLowerCase();
        final String tokenForEmail = tokenValue;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE,
                        RabbitMQConfig.EMAIL_INVITATION_ROUTING_KEY,
                        new MemberInviteEvent(
                                workspaceName,
                                inviterName,
                                inviteeEmail,
                                tokenForEmail
                        )
                );
            }
        });
    }
    @Transactional
    public void resendInvite(UUID memberId) {
        User user = identityService.getCurrentLoggedInUser();
        UUID tenantId = TenantContext.getTenantId();

        Membership currentUsermembership = membershipRepository.findByUser_IdAndTenant_Id(user.getId(), tenantId)
                .orElseThrow(() -> new MembershipException(
                        "You do not belong to this workspace",
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST
                ));

        if (currentUsermembership.getRole() != RoleType.OWNER && currentUsermembership.getRole() != RoleType.ADMIN) {
            throw new UnauthorizedException(
                    "You're not authorized to perform this action",
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN);
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantException(
                        "Workspace does not exist",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND
                ));

        Membership targetMembership = membershipRepository.findByIdAndTenantId(memberId, tenantId)
                .orElseThrow(() -> new MembershipException(
                        "Membership not found",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND));

        if (targetMembership.getStatus() != MembershipStatus.INVITED) {
            throw new MembershipException("Can only resend invitation for pending invites",
                    HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }

        User targetUser = targetMembership.getUser();

        inviteTokenRepository.deleteByUser_IdAndTenant_Id(targetUser.getId(), tenantId);
        String tokenValue = UUID.randomUUID().toString();

        WorkspaceInviteToken inviteToken = WorkspaceInviteToken.builder()
                .token(tokenValue)
                .user(targetUser)
                .tenant(tenant)
                .invitedBy(user)
                .role(targetMembership.getRole())
                .expiresAt(LocalDateTime.now().plusHours(48))
                .build();

        inviteTokenRepository.save(inviteToken);

        final String workspaceName = tenant.getWorkspaceName();
        final String inviterName = user.getName();
        final String inviteeEmail = targetUser.getEmail();
        final String tokenForEmail = tokenValue;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE,
                        RabbitMQConfig.EMAIL_INVITATION_ROUTING_KEY,
                        new MemberInviteEvent(
                                workspaceName,
                                inviterName,
                                inviteeEmail,
                                tokenForEmail
                        )
                );
            }
        });
    }
    @Transactional
    public void deleteMember(UUID memberId){
        UUID tenantId = TenantContext.getTenantId();
        Membership targetMembership = membershipRepository.findByIdAndTenantId(memberId, tenantId)
                .orElseThrow(() -> new MembershipException(
                        "Member not found",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND ));

        User currentLoggedInUser = identityService.getCurrentLoggedInUser();

        Membership currentLoggedInUserMembership = membershipRepository.findByUser_IdAndTenant_Id(currentLoggedInUser.getId(),tenantId)
                .orElseThrow(()->new MembershipException(
                        "You do not belong to this workspace",
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST
                ));

        if(currentLoggedInUserMembership.getRole() != RoleType.OWNER && currentLoggedInUserMembership.getRole() !=RoleType.ADMIN){
            throw new UnauthorizedException(
                    "You're not authorized to perform this action",
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN);
        }

        if((currentLoggedInUserMembership.getRole() == RoleType.ADMIN && targetMembership.getRole() ==RoleType.ADMIN)
                || (currentLoggedInUserMembership.getRole() == RoleType.ADMIN && targetMembership.getRole() ==RoleType.OWNER)){
            throw new UnauthorizedException(
                    "You're not authorized to perform this action",
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN);
        }

        if(currentLoggedInUserMembership.getRole() == RoleType.OWNER && targetMembership.getRole() ==RoleType.OWNER){
            throw new UnauthorizedException(
                    "You're not authorized to perform this action, kindly transfer ownership.",
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN);
        }

        if (targetMembership.getStatus() == MembershipStatus.INVITED) {
            inviteTokenRepository.deleteByUser_IdAndTenant_Id(
                    targetMembership.getUser().getId(),
                    tenantId
            );
        }

        targetMembership.setStatus(MembershipStatus.REMOVED);
        targetMembership.setRemovedBy(currentLoggedInUserMembership.getUser().getId());
        targetMembership.setRemovedAt(LocalDateTime.now());
        membershipRepository.save(targetMembership);
    }

    @Transactional
    public void modifyMemberRole(UUID memberId, MemberRoleDTO role) {

        UUID tenantId = TenantContext.getTenantId();

        Membership targetMembership = membershipRepository.findByIdAndTenantId(memberId, tenantId)
                .orElseThrow(() -> new MembershipException(
                        "Member not found",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND ));

        User currentLoggedInUser = identityService.getCurrentLoggedInUser();
        Membership currentLoggedInUserMembership = membershipRepository.findByUser_IdAndTenant_Id(currentLoggedInUser.getId(), tenantId)
                .orElseThrow(() -> new MembershipException(
                        "You do not belong to this workspace",
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST
                ));

        if (currentLoggedInUserMembership.getRole() != RoleType.OWNER && currentLoggedInUserMembership.getRole() != RoleType.ADMIN) {
            throw new UnauthorizedException(
                    "You're not authorized to perform this action",
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN);
        }

        if (currentLoggedInUserMembership.getRole() == RoleType.ADMIN &&
                (targetMembership.getRole() == RoleType.ADMIN || targetMembership.getRole() == RoleType.OWNER)) {
            throw new UnauthorizedException(
                    "You're not authorized to modify this operator's tier",
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN);
        }

        if (currentLoggedInUserMembership.getRole() == RoleType.ADMIN &&
                (role.role() == RoleType.ADMIN || role.role() == RoleType.OWNER)) {
            throw new UnauthorizedException(
                    "Administrators cannot provision Admin or Owner administrative authorities",
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN);
        }

        if (currentLoggedInUserMembership.getRole() == RoleType.OWNER && targetMembership.getRole() == RoleType.OWNER) {
            throw new UnauthorizedException(
                    "You're not authorized to perform this action, kindly transfer ownership.",
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN);
        }

        targetMembership.setRole(role.role());
        membershipRepository.save(targetMembership);
    }

    @Transactional(readOnly = true)
    public InvitePreviewResponseDTO previewInvite(String tokenString) {
        WorkspaceInviteToken inviteToken = inviteTokenRepository
                .findByToken(tokenString.trim())
                .orElseThrow(() -> new BadRequestException(
                        "Invalid or expired invitation link",
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST
                ));

        if (inviteToken.isAccepted()) {
            throw new BadRequestException(
                    "This invitation has already been accepted",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.BAD_REQUEST
            );
        }

        if (inviteToken.isExpired()) {
            throw new BadRequestException(
                    "This invitation link has expired",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.BAD_REQUEST
            );
        }

        User invitedUser = inviteToken.getUser();
        Tenant tenant = inviteToken.getTenant();

        Membership membership = membershipRepository
                .findByUser_IdAndTenant_Id(invitedUser.getId(), tenant.getId())
                .orElseThrow(() -> new MembershipException(
                        "Invitation not found",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND
                ));

        if (membership.getStatus() != MembershipStatus.INVITED) {
            throw new MembershipException(
                    "This invitation has already been accepted or processed",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.BAD_REQUEST
            );
        }

        boolean isAlreadyRegistered =
                invitedUser.isEmailVerified() && invitedUser.getPasswordHash() != null;

        return new InvitePreviewResponseDTO(
                invitedUser.getEmail(),
                isAlreadyRegistered,
                tenant.getWorkspaceName()
        );
    }

    @Transactional
    public void completeRegistrationAndAcceptInvite(CompleteInviteRegistrationDTO request) {
        WorkspaceInviteToken inviteToken = inviteTokenRepository
                .findByToken(request.token().trim())
                .orElseThrow(() -> new BadRequestException(
                        "Invalid or expired invitation link",
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST
                ));

        if (inviteToken.isAccepted()) {
            throw new BadRequestException(
                    "This invitation has already been accepted",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.BAD_REQUEST
            );
        }

        if (inviteToken.isExpired()) {
            inviteTokenRepository.delete(inviteToken);
            throw new BadRequestException(
                    "This invitation link has expired",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.BAD_REQUEST
            );
        }

        User invitedUser = inviteToken.getUser();
        Tenant targetTenant = inviteToken.getTenant();

        Membership membership = membershipRepository
                .findByUser_IdAndTenant_Id(invitedUser.getId(), targetTenant.getId())
                .orElseThrow(() -> new MembershipException(
                        "Invitation not found",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND
                ));

        if (membership.getStatus() != MembershipStatus.INVITED) {
            throw new MembershipException(
                    "This invitation has already been accepted or processed",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.BAD_REQUEST
            );
        }

        boolean isAlreadyRegistered =
                invitedUser.isEmailVerified() && invitedUser.getPasswordHash() != null;

        if (!isAlreadyRegistered) {
            if (request.name() == null || request.name().isBlank()) {
                throw new BadRequestException(
                        "Name is required for account setup",
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST
                );
            }
            if (request.password() == null || request.password().isBlank()) {
                throw new BadRequestException(
                        "Password is required for account setup",
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST
                );
            }

            invitedUser.setName(request.name().trim());
            invitedUser.setPasswordHash(passwordEncoder.encode(request.password()));
            invitedUser.setStatus(Status.ACTIVE);
            invitedUser.setEmailVerified(true);
        } else if (request.name() != null && !request.name().isBlank()) {
            invitedUser.setName(request.name().trim());
        }

        invitedUser.setLastActiveTenantId(targetTenant.getId());
        invitedUser.setLastLoginAt(LocalDateTime.now());
        userRepository.save(invitedUser);

        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setRole(inviteToken.getRole());
        membership.setJoinedAt(LocalDateTime.now());
        membershipRepository.save(membership);

        boolean channelExists = notificationChannelConfigRepository
                .existsByTenantIdAndChannelAndDestination(
                        targetTenant.getId(),
                        NotificationChannel.EMAIL,
                        invitedUser.getEmail()
                );

        if (!channelExists) {
            NotificationChannelConfig channelConfig = NotificationChannelConfig.builder()
                    .tenant(targetTenant)
                    .channel(NotificationChannel.EMAIL)
                    .destination(invitedUser.getEmail())
                    .enabled(true)
                    .build();
            notificationChannelConfigRepository.save(channelConfig);
        }

        inviteToken.setAcceptedAt(LocalDateTime.now());
        inviteTokenRepository.save(inviteToken);
    }

    public TeamOverview teamMemberOverview(){
        TeamOverview overview = membershipRepository.getTeamOverview(TenantContext.getTenantId());

        if(overview==null){
            return new TeamOverview(0, 0, 0, 0);
        }

        return  overview;
    }

    public List<MembershipDTO> allMembers(){
        UUID tenantId = TenantContext.getTenantId();

        Collection<MembershipStatus> statuses = List.of(
                MembershipStatus.ACTIVE,
                MembershipStatus.INVITED,
                MembershipStatus.SUSPENDED
                );

        return membershipRepository.findAllByTenantIdAndStatusIn(tenantId, statuses)
                .stream()
                .map(MembershipMapper::toDTO)
                .toList();
    }

}
