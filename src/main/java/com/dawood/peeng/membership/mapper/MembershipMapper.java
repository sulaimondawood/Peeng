package com.dawood.peeng.membership.mapper;

import com.dawood.peeng.membership.dtos.responses.MembershipDTO;
import com.dawood.peeng.membership.dtos.responses.MembershipSessionDTO;
import com.dawood.peeng.membership.models.Membership;

public class MembershipMapper {

  public static MembershipSessionDTO toSessionDTO(Membership membership) {
    return MembershipSessionDTO.builder()
        .role(membership.getRole())
        .tenantId(membership.getTenant().getId())
        .workspaceName(membership.getTenant().getWorkspaceName())
        .slug(membership.getTenant().getSlug())
        .build();
  }

  public static MembershipDTO toDTO(Membership membership){
    return MembershipDTO.builder()
            .id(membership.getId())
            .name(membership.getUser().getName())
            .email(membership.getUser().getEmail())
            .avatarUrl(membership.getUser().getAvatarUrl())
            .role(membership.getRole())
            .status(membership.getStatus())
            .build();

  }

}
