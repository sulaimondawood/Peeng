package com.dawood.peeng.monitor.dtos.requests;

import java.util.concurrent.TimeUnit;

import org.hibernate.validator.constraints.URL;

import com.dawood.peeng.monitor.enums.MonitorHttpType;
import com.dawood.peeng.monitor.enums.MonitorType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateMonitorRequest {

  @NotBlank(message = "Provide the Monitor name")
  private String name;

  @NotBlank(message = "Provide the Monitor URL")
  @URL(message = "Provide a valid url")
  private String url;

  @NotNull(message = "Provide the HTTP method")
  @Builder.Default
  private MonitorHttpType method = MonitorHttpType.GET;

  @Builder.Default
  private MonitorType monitorType = MonitorType.HTTP;

  @Builder.Default
  private long intervalValue = 60;

  @Builder.Default
  private TimeUnit intervalUnit = TimeUnit.SECONDS;

  @Builder.Default
  private long timeoutSeconds = 10;

  @Builder.Default
  private Integer failureThreshold = 3;

  @Builder.Default
  private Integer recoveryThreshold = 2;

  @Builder.Default
  private Integer expectedStatusCode = 200;

  private String expectedKeyword;

  public long getCalculatedIntervalSeconds() {
    return this.intervalUnit.toSeconds(this.intervalValue);
  }

}
