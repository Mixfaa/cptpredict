package com.mixfa.cptpredict.model.finance;

import lombok.With;

import java.util.Currency;

@With
public record Money(long amount, Currency currency) implements Comparable<Money> {
    public static Money usd(long amount) {
        return new Money(amount, Currency.getInstance("USD"));
    }

    public String toPrettyString() {
        return amount / Math.pow(currency.getDefaultFractionDigits(), 10) + " " + currency.getSymbol();
    }

    @Override
    public int compareTo(Money o) {
        if (!currency.equals(o.currency))
            throw new RuntimeException("Uncomparable Currency");

        return Long.compare(amount, o.amount);
    }
}
