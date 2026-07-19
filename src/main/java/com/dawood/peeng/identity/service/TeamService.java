package com.dawood.peeng.identity.service;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.identity.dtos.request.MemberInviteDTO;
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

    @Transactional
    public void sendInvite(MemberInviteDTO request){

        UUID tenantId = TenantContext.getTenantId();

        User user = identityService.getCurrentLoggedInUser();

        Membership membership = membershipRepository.findByUser_IdAndTenant_Id(user.getId(),tenantId)
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

        User targetUser = userRepository.findByEmailIgnoreCase(request.email()).orElse(null);
        boolean isNewUser = (targetUser == null);

        if(isNewUser){
            targetUser =  new User();
            targetUser.setStatus(Status.INVITED);
            targetUser.setEmail(request.email());
            targetUser.setEmailVerified(true);
            targetUser.setName("Invited User");
            targetUser.setLastActiveTenantId(tenantId);
            targetUser = userRepository.save(targetUser);
        }else{
            boolean alreadyMember = membershipRepository.findByUser_IdAndTenant_Id(targetUser.getId(), tenantId).isPresent();
            if (alreadyMember) {
                throw new MembershipException("User is already a member or invited to this workspace", HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
            }
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
        newMembership.setInvitedByUserId(user.getId());
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
                                user.getName(),
                                request.email(),
                               tokenString
                        )
                        );
            }
        });
    }

    public void resendInvite(){
        
    }
}
