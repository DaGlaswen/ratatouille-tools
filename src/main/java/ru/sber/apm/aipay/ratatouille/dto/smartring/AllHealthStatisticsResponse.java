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
public class AllHealthStatisticsResponse {

    private ExternalAppPageHeartRateResponseDto heartRate;
    private ExternalAppPageSleepResponseDtoV3 sleep;
    private ExternalAppPageStepResponseDto steps;
    private ExternalAppPageStressResponseDto stress;
    private ExternalAppPageSpo2ResponseDto spo2;
    private ExternalAppPageHrvResponseDto hrv;
    private ExternalAppPageTemperatureResponseDto temperature;
    
    /** Замаскированный UUID, использованный для запроса */
    private String requestedUuid;
    
    /** Timestamp запроса (Unix seconds) */
    private Long timestamp;
    
    /** Информация о том, есть ли ещё данные для каждого типа */
    private HasMoreDataInfo hasMoreData;
    
    @Getter
    @Setter
    @Accessors(chain = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HasMoreDataInfo {
        private Boolean heartRate;
        private Boolean sleep;
        private Boolean steps;
        private Boolean stress;
        private Boolean spo2;
        private Boolean hrv;
        private Boolean temperature;
        
        /** true если хотя бы один endpoint имеет ещё данные */
        private Boolean any;
    }
}
