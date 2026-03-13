package com.mixfa.cptpredict.model.pricingPolicy;

import com.mixfa.cptpredict.model.finance.Money;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;

public record ReservedPricingPolicy(
        long durationInSeconds,
        Money price
) implements PricingPolicy {
    @Override
    public Money estimateBill(Duration duration) {
        return price.withAmount(duration.getSeconds() / durationInSeconds);
    }

    @Override
    public String toString() {
        return "ReservedPricingPolicy " + DurationFormatUtils.formatDurationWords(durationInSeconds * 1000, true, true) + " for " + price.toPrettyString();
    }
}
