package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Ответ на запрос истории транзакций
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionHistoryResponse {

    /**
     * Флаг существования следующих записей (для пагинации)
     */
    private Boolean isExist;

    /**
     * Массив записей истории транзакций
     */
    @Valid
    private List<TransactionHistoryRecord> transactionHistoryRecords;
}
