package com.mixfa.cptpredict.model.estimation;

import com.mixfa.cptpredict.model.VMConfig;
import com.mixfa.cptpredict.model.finance.Money;
import com.mixfa.cptpredict.model.pricingPolicy.PricingPolicy;

import java.time.Duration;

public record EstimationResult(
        VMConfig targetVM,
        Duration duration,
        Money bill
) {
    public EstimationResult(VMConfig vm, Duration duration, PricingPolicy pricingPolicy) {
        this(vm, duration, pricingPolicy.estimateBill(duration));
    }
}
