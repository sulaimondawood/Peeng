package com.dawood.peeng.monitor.dtos.requests;

import com.dawood.peeng.monitor.enums.MonitorHttpType;
import com.dawood.peeng.monitor.enums.MonitorType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

  @Min(value = 10, message = "Interval value cannot be less than 10 seconds")
  private Long intervalValue = 60L;

  private TimeUnit intervalUnit = TimeUnit.SECONDS;

  @Min(value = 1, message = "Timeout can not be below 1sec")
  @Max(value = 15, message = "Timeout can not be more than 15secs")
  private Long timeoutSeconds = 5L;

  @Min(value = 1, message = "Failure threshold must be at least 1")
  @Max(value = 10, message = "Failure threshold cannot exceed 10")
  private Integer failureThreshold = 3;

  @Min(value = 1, message = "Recovery threshold must be at least 1")
  @Max(value = 3, message = "Recovery threshold cannot exceed 3")
  private Integer recoveryThreshold = 1;

  private Integer expectedStatusCode = 200;

  private String expectedKeyword;

  public long getCalculatedIntervalSeconds() {
    if (this.intervalUnit == null || this.intervalValue == null) {
      return 60L;
    }
    return this.intervalUnit.toSeconds(this.intervalValue);
  }


}
