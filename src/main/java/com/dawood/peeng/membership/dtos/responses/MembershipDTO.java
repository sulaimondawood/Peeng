package com.dawood.peeng.membership.dtos.responses;

import com.dawood.peeng.identity.enums.RoleType;
import com.dawood.peeng.membership.enums.MembershipStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MembershipDTO {

    private UUID id;

    private String name;

    private String email;

    private String avatarUrl;

    private RoleType role;

    private MembershipStatus status;

}
