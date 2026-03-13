package com.mixfa.cptpredict.misc;

import com.mixfa.cptpredict.model.program.ComplexityModel;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.SimpleCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

import java.util.List;

public class BigOAnalysis {
    // Интерфейс для моделей, чтобы удобно считать предсказания
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
                    return model.value(n, p[0], p.length > 1 ? p[1] : 0);
                }

                @Override
                public double[] gradient(double n, double... p) {
                    // Численное дифференцирование для градиента (упрощенно)
                    double h = 1e-8;
                    double v = value(n, p);
                    double[] grad = new double[p.length];
                    for (int i = 0; i < p.length; i++) {
                        double oldP = p[i];
                        p[i] += h;
                        grad[i] = (value(n, p) - v) / h;
                        p[i] = oldP;
                    }
                    return grad;
                }
            };
        }
    }

    public static ComplexityModel analyze(double[] N, double[] T) {
        if (N.length != T.length) {
            throw new RuntimeException("N and T size mismatch");
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
                    // Рассчитываем примерный коэффициент C на основе последней точки
                    // Это поможет алгоритму не "теряться" в триллионах
                    double lastN = N[N.length - 1];
                    double lastT = T[T.length - 1];
                    double estimatedC = switch (m.type) {
                        case ComplexityModel.Type.OlogN -> lastT / FastMath.log(2, lastN);
                        case ComplexityModel.Type.ON -> lastT / lastN;
                        case ComplexityModel.Type.ONpow2 -> lastT / FastMath.pow(lastN, 2);
                        case ComplexityModel.Type.ONpow3 -> lastT / FastMath.pow(lastN, 3);
                        case ComplexityModel.Type.OsqrtN -> lastT / FastMath.sqrt(lastN);
                        default -> 1e-10;
                    };

                    startGuess = new double[]{estimatedC, T[0]}; // C и смещение B (берем первую точку)
                }

                SimpleCurveFitter fitter = SimpleCurveFitter.create(m.func, startGuess);
                // Увеличиваем количество итераций, чтобы алгоритм успел сойтись
                fitter = fitter.withMaxIterations(100000000);
                double[] params = fitter.fit(points.toList());

                // Вычисляем MSE
                double mse = 0;
                for (int i = 0; i < N.length; i++) {
                    double pred = m.func.value(N[i], params);
                    mse += FastMath.pow(T[i] - pred, 2);
                }
                mse /= N.length;
                if (mse == 0) mse = 1e-15;

                // Критерий AIC
                double aic = N.length * FastMath.log(mse) + 2 * m.pCount;

                // Бонус константе
                if (m.type == ComplexityModel.Type.O1 && cv < 1e-4) aic -= 1000;

                if (aic < minAIC) {
                    bestInfo = m;
                    minAIC = aic;
                    bestParams = params;
                }

            } catch (Exception e) {
                // Модель не сошлась — пропускаем
            }

        }

        if (bestInfo == null)
            throw new RuntimeException("Cannot describe model");

        return new ComplexityModel(
                bestParams[0],
                bestParams.length == 2 ? bestParams[1] : 0.0,
                bestInfo.type
        );
    }
}
