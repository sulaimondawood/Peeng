package com.dawood.peeng.identity.controller;

import com.dawood.peeng.common.dto.ApiResponse;
import com.dawood.peeng.identity.dtos.request.MemberInviteDTO;
import com.dawood.peeng.identity.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
