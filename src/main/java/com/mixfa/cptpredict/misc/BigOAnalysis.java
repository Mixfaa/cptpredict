package com.mixfa.cptpredict.misc;

import com.mixfa.cptpredict.model.program.ComplexityModel;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.SimpleCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

import java.util.List;

public class BigOAnalysis {
    private static final List<ModelInfo> MODELS = List.of(
            // --- Зростаючі ---
            new ModelInfo(ComplexityModel.Type.O1),
            new ModelInfo(ComplexityModel.Type.OlogN),
            new ModelInfo(ComplexityModel.Type.OsqrtN),
            new ModelInfo(ComplexityModel.Type.ON),
            new ModelInfo(ComplexityModel.Type.ONlogN),
            new ModelInfo(ComplexityModel.Type.ONpow2),
            new ModelInfo(ComplexityModel.Type.ONpow3),
            // --- Спадні  ---
            new ModelInfo(ComplexityModel.Type.O1overLogN),
            new ModelInfo(ComplexityModel.Type.O1overCbrtN),
            new ModelInfo(ComplexityModel.Type.O1overSqrtN),
            new ModelInfo(ComplexityModel.Type.O1overN)
    );

    static class ModelInfo {
        ComplexityModel.Type type;
        ParametricUnivariateFunction func;
        ComplexityModel.Model model;
        int pCount;

        ModelInfo(ComplexityModel.Type complexityModelType) {
            this.type = complexityModelType;
            this.model = ComplexityModel.getModel(complexityModelType);
            this.pCount = complexityModelType.paramsCount;

            this.func = new ParametricUnivariateFunction() {
                @Override
                public double value(double n, double... p) {
                    return model.value(n, p[0], p.length > 1 ? p[1] : 0.0);
                }

                @Override
                public double[] gradient(double n, double... p) {
                    // T = C * f(N) + B  =>  dT/dC = f(N),  dT/dB = 1
                    double[] grad = new double[p.length];
                    grad[0] = model.value(n, 1.0, 0.0); // f(N) при C=1, B=0
                    if (p.length > 1) grad[1] = 1.0;
                    return grad;
                }
            };
        }
    }

    public static ComplexityModel analyze(double[] N, double[] T) {
        if (N.length != T.length || N.length == 0)
            throw new RuntimeException("N and T size mismatch or empty");

        double meanT = StatUtils.mean(T);
        double stdT  = FastMath.sqrt(StatUtils.variance(T));
        double cv    = stdT / (meanT + 1e-15);

        // Визначаємо, чи дані загалом спадають (для пріоритизації моделей)
        boolean isDecreasing = T[T.length - 1] < T[0];

        WeightedObservedPoints points = new WeightedObservedPoints();
        for (int i = 0; i < N.length; i++) points.add(N[i], T[i]);

        ModelInfo bestInfo   = null;
        double    minAIC     = Double.POSITIVE_INFINITY;
        double[]  bestParams = null;

        for (ModelInfo m : MODELS) {
            // Пропускаємо зростаючі (крім O1) якщо дані явно спадають,
            // щоб уникнути некоректних підгонок з від'ємним C
            boolean isDecreasingModel = m.type.name().contains("over");
            if (isDecreasing && !isDecreasingModel && m.type != ComplexityModel.Type.O1) continue;

            try {
                double[] startGuess = buildStartGuess(m, N, T, meanT);

                SimpleCurveFitter fitter = SimpleCurveFitter.create(m.func, startGuess)
                        .withMaxIterations(5000);
                double[] params = fitter.fit(points.toList());

                // --- Перевірка: модель не повинна давати від'ємні значення ---
                boolean goesNegative = false;
                for (double n : N) {
                    if (m.func.value(n, params) < 0) {
                        goesNegative = true;
                        break;
                    }
                }

                // MSE
                double mse = 0;
                for (int i = 0; i < N.length; i++) {
                    double pred = m.func.value(N[i], params);
                    mse += FastMath.pow(T[i] - pred, 2);
                }
                mse /= N.length;
                if (mse <= 0) mse = 1e-15;

                // AIC = n * ln(MSE) + 2k
                double aic = N.length * FastMath.log(mse) + 2.0 * m.pCount;

                // Штраф, якщо модель передбачає від'ємні значення (T >= 0 завжди)
                if (goesNegative) aic += 10_000;

                // Бонус для O1 при плоскому графіку
                if (m.type == ComplexityModel.Type.O1 && cv < 1e-2) aic -= 1000;

                if (aic < minAIC) {
                    bestInfo   = m;
                    minAIC     = aic;
                    bestParams = params;
                }

            } catch (Exception ignored) {
                // Модель не змогла зійтись — пропускаємо
            }
        }

        if (bestInfo == null)
            throw new RuntimeException("Cannot describe model: no algorithms converged");

        return new ComplexityModel(
                bestParams[0],
                bestParams.length > 1 ? bestParams[1] : 0.0,
                bestInfo.type
        );
    }

    private static double[] buildStartGuess(ModelInfo m, double[] N, double[] T, double meanT) {
        if (m.pCount == 1) return new double[]{meanT};

        double lastN = N[N.length - 1];
        double lastT = T[T.length - 1];
        final double EPS = 1e-9;

        double estimatedC = switch (m.type) {
            case OlogN       -> lastT / FastMath.max(EPS, FastMath.log(2, lastN));
            case OsqrtN      -> lastT / FastMath.max(EPS, FastMath.sqrt(lastN));
            case ON          -> lastT / FastMath.max(EPS, lastN);
            case ONlogN      -> lastT / FastMath.max(EPS, lastN * FastMath.log(2, lastN));
            case ONpow2      -> lastT / FastMath.max(EPS, FastMath.pow(lastN, 2));
            case ONpow3      -> lastT / FastMath.max(EPS, FastMath.pow(lastN, 3));
            // Спадні: C ≈ lastT * f(lastN), бо T ≈ C / f(N)
            case O1overLogN  -> lastT * FastMath.max(EPS, FastMath.log(2, lastN));
            case O1overCbrtN -> lastT * FastMath.max(EPS, FastMath.cbrt(lastN));
            case O1overSqrtN -> lastT * FastMath.max(EPS, FastMath.sqrt(lastN));
            case O1overN     -> lastT * FastMath.max(EPS, lastN);
            default          -> 1e-10;
        };

        // Груба оцінка зміщення B
        double fAtFirst = m.func.value(N[0], 1.0, 0.0); // f(N[0]) при C=1
        double startB   = T[0] - estimatedC * fAtFirst;

        return new double[]{estimatedC, startB};
    }
}