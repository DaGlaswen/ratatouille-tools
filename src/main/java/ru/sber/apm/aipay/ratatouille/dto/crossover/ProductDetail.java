package ru.sber.apm.aipay.ratatouille.dto.crossover;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductDetail extends Product {

    private String description;

    private Map<String, String> specifications;
}