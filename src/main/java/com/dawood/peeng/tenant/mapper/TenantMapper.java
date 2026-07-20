package com.dawood.peeng.tenant.mapper;

import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.tenant.dtos.response.TenantSessionDTO;
import com.dawood.peeng.tenant.model.Tenant;

public class TenantMapper {
    public static TenantSessionDTO toTenantSessionDTO(Tenant tenant){
        return TenantSessionDTO.builder()
                .id(tenant.getId())
                .slug(tenant.getSlug())
                .workspaceName(tenant.getWorkspaceName())
                .build();
    }

    public static TenantSessionDTO toTenantSessionDTO(Membership membership){
        return TenantSessionDTO.builder()
                .id(membership.getTenant().getId())
                .slug(membership.getTenant().getSlug())
                .workspaceName(membership.getTenant().getWorkspaceName())
                .build();
    }
}
