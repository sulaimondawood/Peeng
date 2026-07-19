package com.dawood.peeng.identity.service;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.common.exceptions.BadRequestException;
import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.identity.dtos.request.CompleteInviteRegistrationDTO;
import com.dawood.peeng.identity.dtos.request.MemberInviteDTO;
import com.dawood.peeng.identity.dtos.request.MemberRoleDTO;
import com.dawood.peeng.identity.dtos.response.InvitePreviewResponseDTO;
import com.dawood.peeng.identity.enums.RoleType;
import com.dawood.peeng.identity.enums.Status;
import com.dawood.peeng.identity.event.MemberInviteEvent;
import com.dawood.peeng.identity.exceptions.UnauthorizedException;
import com.dawood.peeng.identity.models.EmailVerificationToken;
import com.dawood.peeng.identity.models.User;
import com.dawood.peeng.identity.repository.EmailVerificationTokenRepository;
import com.dawood.peeng.identity.repository.UserRepository;
import com.dawood.peeng.membership.enums.MembershipStatus;
import com.dawood.peeng.membership.exceptions.MembershipException;
import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.membership.repository.MembershipRepository;
import com.dawood.peeng.tenant.context.TenantContext;
import com.dawood.peeng.tenant.exceptions.TenantException;
import com.dawood.peeng.tenant.model.Tenant;
import com.dawood.peeng.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final UserRepository userRepository;
    private final IdentityService identityService;
    private final MembershipRepository membershipRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final TenantRepository tenantRepository;
    private final RabbitTemplate rabbitTemplate;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendInvite(MemberInviteDTO request){

        UUID tenantId = TenantContext.getTenantId();

        User currentLoggedInUser = identityService.getCurrentLoggedInUser();

        Membership membership = membershipRepository.findByUser_IdAndTenant_Id(currentLoggedInUser.getId(),tenantId)
                .orElseThrow(()->new MembershipException(
                        "You do not belong to this workspace",
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST
                ));

        if(membership.getRole() != RoleType.OWNER && membership.getRole() !=RoleType.ADMIN){
            throw new UnauthorizedException(
                    "You're not authorized to perform this action",
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED);
        }

        User targetUser = userRepository.findByEmailIgnoreCase(request.email()).orElseGet(()->{
            User newUser = new User();
            newUser.setStatus(Status.INVITED);
            newUser.setEmail(request.email());
            newUser.setEmailVerified(false);
            newUser.setName("Invited User");
            newUser.setLastActiveTenantId(tenantId);
            return userRepository.save(newUser);
        });

        boolean alreadyMember = membershipRepository
                .findByUser_IdAndTenant_Id(targetUser.getId(), tenantId)
                .isPresent();

        if (alreadyMember) {
            throw new MembershipException(
                    "User is already a member or invited to this workspace",
                    HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }

        EmailVerificationToken token = EmailVerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(targetUser)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        EmailVerificationToken savedToken = tokenRepository.save(token);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(()->new TenantException(
                        "Workspace does not exists",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND
                ));

        Membership newMembership = new Membership();
        newMembership.setUser(targetUser);
        newMembership.setRole(request.role());
        newMembership.setTenant(tenant);
        newMembership.setInvitedByUserId(currentLoggedInUser.getId());
        newMembership.setStatus(MembershipStatus.INVITED);
        membershipRepository.save(newMembership);

        String tokenString = savedToken.getToken();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE,
                        RabbitMQConfig.EMAIL_INVITATION_ROUTING_KEY,
                        new MemberInviteEvent(
                                tenant.getWorkspaceName(),
                                currentLoggedInUser.getName(),
                                request.email(),
                               tokenString
                        )
                        );
            }
        });
    }

    @Transactional
    public void resendInvite(UUID memberId){

        User user = identityService.getCurrentLoggedInUser();
        UUID tenantId = TenantContext.getTenantId();
        Membership currentUsermembership = membershipRepository.findByUser_IdAndTenant_Id(user.getId(),tenantId)
                .orElseThrow(()->new MembershipException(
                        "You do not belong to this workspace",
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST
                ));

        if(currentUsermembership.getRole() != RoleType.OWNER && currentUsermembership.getRole() !=RoleType.ADMIN){
            throw new UnauthorizedException(
                    "You're not authorized to perform this action",
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED);
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(()->new TenantException(
                        "Workspace does not exists",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND
                ));

        Membership targetMembership = membershipRepository.findByIdAndTenantId(memberId, tenantId)
                .orElseThrow(() -> new MembershipException(
                        "Invitation not found",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND ));

        if (targetMembership.getStatus() != MembershipStatus.INVITED) {
            throw new MembershipException("Can only resend invitation for pending invites",
                    HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }

        User targetUser = targetMembership.getUser();
        tokenRepository.deleteByUser(targetUser);

        EmailVerificationToken newToken = EmailVerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(targetUser)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

       EmailVerificationToken savedToken = tokenRepository.save(newToken);

        String tokenString = savedToken.getToken();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE,
                        RabbitMQConfig.EMAIL_INVITATION_ROUTING_KEY,
                        new MemberInviteEvent(
                                tenant.getWorkspaceName(),
                                user.getName(),
                                targetUser.getEmail(),
                                tokenString
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
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED);
        }

        if((currentLoggedInUserMembership.getRole() == RoleType.ADMIN && targetMembership.getRole() ==RoleType.ADMIN)
                || (currentLoggedInUserMembership.getRole() == RoleType.ADMIN && targetMembership.getRole() ==RoleType.OWNER)){
            throw new UnauthorizedException(
                    "You're not authorized to perform this action",
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED);
        }

        if(currentLoggedInUserMembership.getRole() == RoleType.OWNER && targetMembership.getRole() ==RoleType.OWNER){
            throw new UnauthorizedException(
                    "You're not authorized to perform this action, kindly transfer ownership.",
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED);
        }

        if (targetMembership.getStatus() == MembershipStatus.INVITED) {
            tokenRepository.deleteByUser(targetMembership.getUser());
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
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED);
        }

        if (currentLoggedInUserMembership.getRole() == RoleType.ADMIN &&
                (targetMembership.getRole() == RoleType.ADMIN || targetMembership.getRole() == RoleType.OWNER)) {
            throw new UnauthorizedException(
                    "You're not authorized to modify this operator's tier",
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED);
        }

        if (currentLoggedInUserMembership.getRole() == RoleType.ADMIN &&
                (role.role() == RoleType.ADMIN || role.role() == RoleType.OWNER)) {
            throw new UnauthorizedException(
                    "Administrators cannot provision Admin or Owner administrative authorities",
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED);
        }

        if (currentLoggedInUserMembership.getRole() == RoleType.OWNER && targetMembership.getRole() == RoleType.OWNER) {
            throw new UnauthorizedException(
                    "You're not authorized to perform this action, kindly transfer ownership.",
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED);
        }

        targetMembership.setRole(role.role());
        membershipRepository.save(targetMembership);
    }

    @Transactional(readOnly = true)
    public InvitePreviewResponseDTO previewInvite(String tokenString) {
        EmailVerificationToken token = tokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new BadRequestException(
                        "Invalid or expired invitation link",
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(
                    "This invitation link has expired",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.BAD_REQUEST);
        }

        User invitedUser = token.getUser();
        Membership membership = membershipRepository.findByUser_IdAndTenant_Id(
                        invitedUser.getId(),
                        invitedUser.getLastActiveTenantId())
                .orElseThrow(() -> new MembershipException(
                        "Invitation not found",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND
                ));

        boolean isAlreadyRegistered = invitedUser.isEmailVerified() && invitedUser.getPasswordHash() != null;

        return new InvitePreviewResponseDTO(
                invitedUser.getEmail(),
                isAlreadyRegistered,
                membership.getTenant().getWorkspaceName()
        );
    }

    @Transactional
    public void completeRegistrationAndAcceptInvite(CompleteInviteRegistrationDTO request) {

        EmailVerificationToken token = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BadRequestException(
                        "Invalid or expired invitation link",
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(
                    "This invitation link has expired",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.BAD_REQUEST
                    );
        }

        User invitedUser = token.getUser();
        Membership membership = membershipRepository.findByUser_IdAndTenant_Id(
                        invitedUser.getId(),
                        invitedUser.getLastActiveTenantId())
                .orElseThrow(() -> new MembershipException(
                        "Invitation not found",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND
                        ));

        if (membership.getStatus() != MembershipStatus.INVITED) {
            throw new MembershipException(
                    "This invitation has already been processed",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.BAD_REQUEST
                    );
        }

        invitedUser.setName(request.name());
        invitedUser.setPasswordHash(passwordEncoder.encode(request.password()));
        invitedUser.setStatus(Status.ACTIVE);
        invitedUser.setEmailVerified(true);
        invitedUser.setLastActiveTenantId(membership.getTenant().getId());
        invitedUser.setLastLoginAt(LocalDateTime.now());
        userRepository.save(invitedUser);

        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setJoinedAt(LocalDateTime.now());
        membershipRepository.save(membership);

        tokenRepository.delete(token);
        // Send welcome notification / email
    }
}
