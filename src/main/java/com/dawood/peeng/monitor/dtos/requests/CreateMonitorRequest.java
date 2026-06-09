package com.dawood.peeng.monitor.dtos.requests;

import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import com.dawood.peeng.monitor.enums.MonitorHttpType;
import com.dawood.peeng.monitor.enums.MonitorType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMonitorRequest {

  @NotBlank(message = "Provide the Monitor name")
  private String name;

  @NotBlank(message = "Provide the Monitor URL")
  @URL(message = "Provide a valid url")
  private String url;

  @NotNull(message = "Provide the HTTP method")
  @JsonProperty(defaultValue = "GET") // Tells Jackson what to use if missing
  private MonitorHttpType method;

  @JsonProperty(defaultValue = "HTTP")
  private MonitorType monitorType;

  @JsonProperty(defaultValue = "60")
  private Long intervalValue;

  @JsonProperty(defaultValue = "SECONDS")
  private TimeUnit intervalUnit;

  @JsonProperty(defaultValue = "10")
  private Long timeoutSeconds;

  @JsonProperty(defaultValue = "3")
  private Integer failureThreshold;

  @Max(message = "Threshold value cannot exceed 3", value = 3)
  @JsonProperty(defaultValue = "1")
  private Integer recoveryThreshold;

  @JsonProperty(defaultValue = "200")
  private Integer expectedStatusCode;

  private String expectedKeyword;

  public long getCalculatedIntervalSeconds() {
    if (this.intervalUnit == null || this.intervalValue == null) {
      return 60L;
    }
    return this.intervalUnit.toSeconds(this.intervalValue);
  }

  public long getTimeoutSeconds() {
    return this.timeoutSeconds == null ? 10L : this.timeoutSeconds;
  }

}
