package com.dawood.peeng.tenant.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record CreateTenantRequest(
       @NotBlank String workspaceName
) {
}
