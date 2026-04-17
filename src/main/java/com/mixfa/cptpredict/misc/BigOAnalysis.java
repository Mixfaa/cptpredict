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
            new ModelInfo(ComplexityModel.Type.O1),
            new ModelInfo(ComplexityModel.Type.OlogN),
            new ModelInfo(ComplexityModel.Type.OsqrtN),
            new ModelInfo(ComplexityModel.Type.ON),
            new ModelInfo(ComplexityModel.Type.ONlogN),
            new ModelInfo(ComplexityModel.Type.ONpow2),
            new ModelInfo(ComplexityModel.Type.ONpow3)
    );

    static class ModelInfo {
        ComplexityModel.Type type;
        ParametricUnivariateFunction func;
        ComplexityModel.Model model;
        String format;
        int pCount;

        ModelInfo(ComplexityModel.Type complexityModelType) {
            this.type = complexityModelType;
            this.model = ComplexityModel.getModel(complexityModelType);
            this.format = complexityModelType.format;
            this.pCount = complexityModelType.paramsCount;

            this.func = new ParametricUnivariateFunction() {
                @Override
                public double value(double n, double... p) {
                    return model.value(n, p[0], p.length > 1 ? p[1] : 0.0);
                }

                @Override
                public double[] gradient(double n, double... p) {
                    // ИСПРАВЛЕНИЕ 1: Точный аналитический градиент вместо h = 1e-8.
                    // Поскольку формула всегда имеет вид T = C * f(N) + B,
                    // Производная по C (p[0]) - это просто само значение f(N).
                    // Производная по B (p[1]) - это всегда 1.0.
                    double[] grad = new double[p.length];

                    // Чтобы получить чистую f(N), подставляем C=1 и B=0
                    grad[0] = model.value(n, 1.0, 0.0);

                    if (p.length > 1) {
                        grad[1] = 1.0;
                    }
                    return grad;
                }
            };
        }
    }

    public static ComplexityModel analyze(double[] N, double[] T) {
        if (N.length != T.length || N.length == 0) {
            throw new RuntimeException("N and T size mismatch or empty");
        }

        double meanT = StatUtils.mean(T);
        double stdT = FastMath.sqrt(StatUtils.variance(T));
        double cv = stdT / (meanT + 1e-15);

        WeightedObservedPoints points = new WeightedObservedPoints();
        for (int i = 0; i < N.length; i++) points.add(N[i], T[i]);

        ModelInfo bestInfo = null;
        double minAIC = Double.POSITIVE_INFINITY;
        double[] bestParams = null;

        for (ModelInfo m : MODELS) {
            try {
                double[] startGuess;
                if (m.pCount == 1) {
                    startGuess = new double[]{meanT};
                } else {
                    double lastN = N[N.length - 1];
                    double lastT = T[T.length - 1];

                    // ИСПРАВЛЕНИЕ 2: Добавлен ONlogN и улучшена защита от NaN
                    double estimatedC = switch (m.type) {
                        case OlogN -> lastT / FastMath.max(1e-9, FastMath.log(2, lastN));
                        case OsqrtN -> lastT / FastMath.max(1e-9, FastMath.sqrt(lastN));
                        case ON -> lastT / FastMath.max(1e-9, lastN);
                        case ONlogN -> lastT / FastMath.max(1e-9, lastN * FastMath.log(2, lastN));
                        case ONpow2 -> lastT / FastMath.max(1e-9, FastMath.pow(lastN, 2));
                        case ONpow3 -> lastT / FastMath.max(1e-9, FastMath.pow(lastN, 3));
                        default -> 1e-10;
                    };

                    // Грубая оценка стартового B (смещения)
                    double startB = T[0] - estimatedC * m.func.value(N[0], 1.0, 0.0);
                    startGuess = new double[]{estimatedC, startB};
                }

                // ИСПРАВЛЕНИЕ 3: 100 млн итераций — это слишком много.
                // С правильным градиентом алгоритму хватит и 1000 итераций.
                SimpleCurveFitter fitter = SimpleCurveFitter.create(m.func, startGuess)
                        .withMaxIterations(5000);

                double[] params = fitter.fit(points.toList());

                // Вычисляем MSE
                double mse = 0;
                for (int i = 0; i < N.length; i++) {
                    double pred = m.func.value(N[i], params);
                    mse += FastMath.pow(T[i] - pred, 2);
                }
                mse /= N.length;
                if (mse <= 0) mse = 1e-15; // Защита от логарифма нуля или отрицательного числа

                // Критерий AIC
                double aic = N.length * FastMath.log(mse) + 2 * m.pCount;

                // Бонус константе (если график абсолютно плоский)
                if (m.type == ComplexityModel.Type.O1 && cv < 1e-2) aic -= 1000;

                if (aic < minAIC) {
                    bestInfo = m;
                    minAIC = aic;
                    bestParams = params;
                }

            } catch (Exception e) {
                // Игнорируем модели, которые не смогли сойтись
            }
        }

        if (bestInfo == null) {
            throw new RuntimeException("Cannot describe model: no algorithms converged");
        }

        return new ComplexityModel(
                bestParams[0],
                bestParams.length > 1 ? bestParams[1] : 0.0,
                bestInfo.type
        );
    }
}