package com.mixfa.cptpredict.model;

import com.mixfa.cptpredict.model.pricingPolicy.PricingPolicy;
import lombok.experimental.FieldNameConstants;
import org.dizitart.no2.repository.annotations.Entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Entity
@FieldNameConstants
public record VMConfig(
        @org.dizitart.no2.repository.annotations.Id @Id String name,
        VMBenchmarkResult benchmarkResult,
        PricingPolicy pricingPolicy
) {
}
