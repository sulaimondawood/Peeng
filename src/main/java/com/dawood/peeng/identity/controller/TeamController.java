package com.dawood.peeng.identity.controller;

import com.dawood.peeng.common.dto.ApiResponse;
import com.dawood.peeng.identity.dtos.request.CompleteInviteRegistrationDTO;
import com.dawood.peeng.identity.dtos.request.MemberInviteDTO;
import com.dawood.peeng.identity.dtos.request.MemberRoleDTO;
import com.dawood.peeng.identity.dtos.response.InvitePreviewResponseDTO;
import com.dawood.peeng.identity.dtos.response.TeamOverview;
import com.dawood.peeng.identity.service.TeamService;
import com.dawood.peeng.membership.dtos.responses.MembershipDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MembershipDTO>>> allMembers() {
        List<MembershipDTO> res = teamService.allMembers();
        return ResponseEntity.ok(ApiResponse.success(
                "Team directory roster retrieved successfully.",
                res));
    }

    @PostMapping("/invitation")
    public ResponseEntity<ApiResponse<Void>> inviteTeamMember(
            @Valid @RequestBody MemberInviteDTO request) {

        teamService.sendInvite(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Invitation dispatched successfully",
                null));
    }

    @PostMapping("/invitation/{membershipId}/resend")
    public ResponseEntity<ApiResponse<Void>> resendWorkspaceInvite(@PathVariable UUID membershipId) {
        teamService.resendInvite(membershipId);
        return ResponseEntity.ok(ApiResponse.success(
                "A fresh invitation link has been successfully dispatched.",
                null));
    }
    @DeleteMapping("/{membershipId}/remove")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable UUID membershipId) {
        teamService.deleteMember(membershipId);
        return ResponseEntity.ok(ApiResponse.success(
                "Operator has been successfully removed from this workspace.",
                null));
    }

    @PatchMapping("/{membershipId}/role")
    public ResponseEntity<ApiResponse<Void>> modifyMemberRole(
            @PathVariable UUID membershipId,
            @RequestBody @Valid MemberRoleDTO request) {
        teamService.modifyMemberRole(membershipId, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Operator permissions tier updated successfully.",
                null));
    }

    @GetMapping("/{token}/preview-invite")
    public ResponseEntity<ApiResponse<InvitePreviewResponseDTO>> previewInvite(@PathVariable String token) {
        InvitePreviewResponseDTO res = teamService.previewInvite(token);
        return ResponseEntity.ok(ApiResponse.success(
                "Invitation tracking signature verified successfully.",
                res));
    }

    @PostMapping("/accept-invite")
    public ResponseEntity<ApiResponse<Void>> acceptInviteAndRegister(@RequestBody @Valid CompleteInviteRegistrationDTO request) {
        teamService.completeRegistrationAndAcceptInvite(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Account setup successful. Welcome aboard!",
                null));
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<TeamOverview>> teamOverview() {
        TeamOverview res = teamService.teamMemberOverview();
        return ResponseEntity.ok(ApiResponse.success(
                "Team metric aggregations loaded successfully.",
                res));
    }
}
