package com.dawood.peeng.messaging.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendVerificationEmailEvent {

  private String email;

  private String name;

  private String token;

}