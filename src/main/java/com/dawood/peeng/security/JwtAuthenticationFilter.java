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

import com.dawood.peeng.identity.models.User;
import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.membership.repository.MembershipRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final MembershipRepository membershipRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    String token;
    final String tenantHeader = request.getHeader("X-Tentant-Id");

    if (authHeader != null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

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

      filterChain.doFilter(request, response);

    }

  }

}
