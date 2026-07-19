package com.dawood.peeng.identity.controller;

import com.dawood.peeng.common.dto.ApiResponse;
import com.dawood.peeng.identity.dtos.request.MemberInviteDTO;
import com.dawood.peeng.identity.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;

    @PostMapping("/invitations")
    public ResponseEntity<ApiResponse<Void>> inviteTeamMember(
            @Valid @RequestBody MemberInviteDTO request) {

        teamService.sendInvite(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Invitation dispatched successfully",
                null));
    }

    @PostMapping("/invitations/{membershipId}/resend")
    public ResponseEntity<ApiResponse<Void>> resendWorkspaceInvite(@PathVariable UUID membershipId) {
        teamService.resendInvite(membershipId);
        return ResponseEntity.ok(ApiResponse.success("A fresh invitation link has been successfully dispatched.", null));
    }


}
