package com.dawood.peeng.identity.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MemberInviteEvent {
    private String workspaceName;
    private String user;
    private String email;
    private String token;
}
