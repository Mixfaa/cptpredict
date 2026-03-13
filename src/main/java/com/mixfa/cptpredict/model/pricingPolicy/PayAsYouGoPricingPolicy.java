package com.mixfa.cptpredict.model.pricingPolicy;

import com.mixfa.cptpredict.model.finance.Money;

import java.time.Duration;

public record PayAsYouGoPricingPolicy() implements PricingPolicy {
    @Override
    public Money estimateBill(Duration duration) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
