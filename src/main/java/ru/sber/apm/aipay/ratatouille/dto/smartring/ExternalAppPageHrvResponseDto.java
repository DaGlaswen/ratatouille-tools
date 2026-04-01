package ru.sber.apm.aipay.ratatouille.dto.smartring;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalAppPageHrvResponseDto {

    private List<ExternalDataHrvResponseDto> content;
    private Long totalElements;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;
    private Boolean isFirst;
    private Boolean isLast;
}