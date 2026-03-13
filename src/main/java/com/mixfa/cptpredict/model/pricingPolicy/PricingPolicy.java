package com.mixfa.cptpredict.model.pricingPolicy;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mixfa.cptpredict.model.finance.Money;

import java.time.Duration;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PayAsYouGoPricingPolicy.class),
        @JsonSubTypes.Type(value = ReservedPricingPolicy.class),
})
sealed public interface PricingPolicy permits PayAsYouGoPricingPolicy, ReservedPricingPolicy {
    Money estimateBill(Duration duration);
}
