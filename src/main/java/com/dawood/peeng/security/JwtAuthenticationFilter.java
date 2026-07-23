package com.dawood.peeng.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.dawood.peeng.common.ResponseBuilder;
import com.dawood.peeng.common.dto.ApiError;
import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.membership.repository.MembershipRepository;
import com.dawood.peeng.tenant.context.TenantContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final MembershipRepository membershipRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String tenantHeader = request.getHeader("X-Tenant-Id");

        if (tenantHeader == null || tenantHeader.isBlank()) {
            sendErrorResponse( request, response, HttpStatus.BAD_REQUEST,"Missing or invalid X-Tenant-Id header");
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("Invalid auth header");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractSubject(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails user = userDetailsService.loadUserByUsername(email);

                UUID tenantId = UUID.fromString(tenantHeader);

                Membership membership = membershipRepository
                        .findByUser_EmailAndTenant_Id(email, tenantId)
                        .orElseThrow(() -> new RuntimeException(
                                "No active membership found for this tenant"));

                String role = "ROLE_" + membership.getRole().name();

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority(role)));

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);

                TenantContext.set(tenantId);

                filterChain.doFilter(request, response);

            }

        } catch (TokenExpiredException e) {
            sendErrorResponse(request, response, HttpStatus.UNAUTHORIZED, "Authorization token has expired");
        } catch (JWTVerificationException e) {
            sendErrorResponse(request, response, HttpStatus.UNAUTHORIZED, "Invalid authorization token");
        } catch (Exception e) {
            log.error("Unexpected error during JWT authentication", e);
            sendErrorResponse(request, response, HttpStatus.INTERNAL_SERVER_ERROR, "Authentication failed");
        } finally {
            TenantContext.clear();
        }

    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message) throws IOException {
        ApiError err = ResponseBuilder.buildError(request, response, message, status, status.getReasonPhrase());
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(mapper.writeValueAsString(err));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.startsWith("/auth");
    }
}
