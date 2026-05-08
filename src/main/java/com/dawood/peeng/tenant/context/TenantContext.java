package com.dawood.peeng.tenant.context;

import java.util.UUID;

public class TenantContext {

  private static final ThreadLocal<UUID> tenant = new ThreadLocal<>();

  public static void set(UUID tenantId) {
    tenant.set(tenantId);
  }

  public static UUID getTenantId() {
    return tenant.get();
  }

  public static void clear() {
    tenant.remove();
  }
}
