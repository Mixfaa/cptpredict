package com.mixfa.cptpredict.model.program;

import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.FastMath;

import java.util.function.DoubleUnaryOperator;

public record ComplexityModel(
        double c,
        double b,
        Type type
) {
    @RequiredArgsConstructor
    public enum Type {
        O1("O(1)", "T(N) = %.2e", 1),
        OlogN("O(log N)", "T(N) = %.2e * log2(N) + %.2e", 2),
        OsqrtN("O(sqrt N)", "T(N) = %.2e * sqrt(N) + %.2e", 2),
        ON("O(N)", "T(N) = %.2e * N + %.2e", 2),
        ONlogN("O(N log N)", "T(N) = %.2e * N*log2(N) + %.2e", 2),
        ONpow2("O(N^2)", "T(N) = %.2e * N^2 + %.2e", 2),
        ONpow3("O(N^3)", "T(N) = %.2e * N^3 + %.2e", 2);

        public final String name;
        public final String format;
        public final int paramsCount;

        public String format(double c, double b) {
            if (paramsCount == 1)
                return String.format(format, c);
            else if (paramsCount == 2)
                return String.format(format, c, b);

            return format;
        }
    }

    public interface Model {
        double value(double n, double c, double b);
    }

    public static Model getModel(Type type) {
        return switch (type) {
            case O1 -> (_, c, _) -> c;
            case OlogN -> (n, c, b) -> c * FastMath.log(2, n + 1e-9) + b;
            case OsqrtN -> (n, c, b) -> c * FastMath.sqrt(n) + b;
            case ON -> (n, c, b) -> c * n + b;
            case ONlogN -> (n, c, b) -> c * n * FastMath.log(2, n + 1e-9) + b;
            case ONpow2 -> (n, c, b) -> c * FastMath.pow(n, 2) + b;
            case ONpow3 -> (n, c, b) -> c * FastMath.pow(n, 3) + b;
        };
    }

    public DoubleUnaryOperator getFunction() {
        final var model = getModel(type);
        return n -> model.value(n, c, b);
    }

    public String formula() {
        return type.format(c, b);
    }
}
