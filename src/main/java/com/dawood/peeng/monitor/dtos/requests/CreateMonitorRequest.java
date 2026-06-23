package com.dawood.peeng.monitor.dtos.requests;

import com.dawood.peeng.monitor.enums.MonitorHttpType;
import com.dawood.peeng.monitor.enums.MonitorType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
@NoArgsConstructor
public class CreateMonitorRequest {

  @NotBlank(message = "Provide the Monitor name")
  private String name;

  @NotBlank(message = "Provide the Monitor URL")
  @URL(message = "Provide a valid url")
  private String url;

  private MonitorHttpType method = MonitorHttpType.GET;
  private MonitorType monitorType = MonitorType.HTTP;

  private Long intervalValue = 60L;

  private TimeUnit intervalUnit = TimeUnit.SECONDS;

  private Long timeoutSeconds = 10L;

  private Integer failureThreshold = 3;

  @Max(message = "Threshold value cannot exceed 3", value = 3)
  private Integer recoveryThreshold = 1;

  private Integer expectedStatusCode = 200;

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
