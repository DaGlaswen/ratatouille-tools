package ru.sber.apm.aipay.ratatouille.dto.smartring;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalDataSleepResponseDtoV3 {

    private String startTime;
    private Integer totalSleepTime;
    private String sleepQuality;
    private Double sleepScore;
    private Integer sleepId;
    private String calculatedStartTime;
    private String source;
    private Integer minHeartRate;
    private Integer avgHrv;
    private Integer avgSpo2;
    private String date;
}