package com.dawood.peeng.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.membership.repository.MembershipRepository;
import com.dawood.peeng.tenant.context.TenantContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final MembershipRepository membershipRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    String token;
    final String tenantHeader = request.getHeader("X-Tenant-Id");

    if (tenantHeader == null) {
      log.info("Tenant Id is missing");
      return;
    }

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.info("Invalid auth header");
      filterChain.doFilter(request, response);
      return;
    }

    try {

      token = authHeader.substring(7);

      String email = jwtService.extractSubject(token);

      if (email != null && SecurityContextHolder.getContext()
          .getAuthentication() == null) {

        UserDetails user = userDetailsService.loadUserByUsername(email);

        UUID tenantId = UUID.fromString(tenantHeader);

        Membership membership = membershipRepository
            .findByUser_EmailAndTenant_Id(user.getUsername(), tenantId)
            .orElseThrow(() -> new RuntimeException(
                "Membership not found"));

        String role = "ROLE_" + membership.getRole().name();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null,
            List.of(new SimpleGrantedAuthority(role)));

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);

        TenantContext.set(tenantId);

        filterChain.doFilter(request, response);

      }

    } catch (Exception e) {

    } finally {
      TenantContext.clear();

    }

  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getServletPath();
    return path.startsWith("/auth");
  }
}
