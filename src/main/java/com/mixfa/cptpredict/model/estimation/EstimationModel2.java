package com.mixfa.cptpredict.model.estimation;

import com.mixfa.cptpredict.model.VMBenchmarkResult;
import com.mixfa.cptpredict.model.VMConfig;
import com.mixfa.cptpredict.model.benchmark.IPCBenchmarkApp;
import com.mixfa.cptpredict.model.benchmark.BenchmarkAppResult;
import com.mixfa.cptpredict.model.program.ProgramInfo;
import org.apache.commons.numbers.core.Precision;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public final class EstimationModel2 implements EstimationModel<EstimationModel2.Parameters> {
    private static final EstimationModel2 INSTANCE = new EstimationModel2();

    public static EstimationModel2 getInstance() {
        return INSTANCE;
    }

    private EstimationModel2() {
    }

    @Override
    public String name() {
        return "EstimationModel2";
    }

    @Override
    public EstimationResult estimate(VMConfig vmConfig, Parameters parameters) {
        var targetMachineBenchmarkResult = vmConfig.benchmarkResult();
        var targetMachineFreqKhz = targetMachineBenchmarkResult.efficientFreqKhz()[parameters.targetMachineCore]; // hz to c per ms

        var appIpc = parameters.ipcCalculator.calculate(
                parameters.testMachineResult,
                vmConfig.benchmarkResult(),
                parameters.testMachineCore,
                parameters.targetMachineCore,
                parameters.testMachineAppIpc
        );

        var appComplexityFunc = parameters.programInfo.model().getFunction();

        var instructions = appComplexityFunc.applyAsDouble(parameters.appIterations);

        var time = (long) (instructions / (targetMachineFreqKhz * appIpc));

        return new EstimationResult(
                vmConfig,
                Duration.ofMillis(time),
                vmConfig.pricingPolicy()
        );
    }

    @Override
    public Class<Parameters> parametersType() {
        return Parameters.class;
    }

    public interface IpcCalculator {
        double calculate(
                VMBenchmarkResult testMachine,
                VMBenchmarkResult targetMachine,
                int testMachineCore,
                int targetMachineCore,
                double testMachineAppIpc
        );

        class DefaultIpcCalculator implements IpcCalculator {
            private static final DefaultIpcCalculator INSTANCE = new DefaultIpcCalculator();

            public static DefaultIpcCalculator getInstance() {
                return INSTANCE;
            }

            private DefaultIpcCalculator() {
            }

            public static double calcAvgIpc(VMBenchmarkResult vmBenchmarkResult, int core) {
                var coreFreqkhz = vmBenchmarkResult.efficientFreqKhz()[core];
                return Arrays.stream(vmBenchmarkResult.benchmarkResults())
                        .mapToDouble(BenchmarkAppResult::instrPerMs)
                        .map(it -> it / coreFreqkhz)
                        .average().getAsDouble();
            }

            @Override
            public double calculate(VMBenchmarkResult testMachine, VMBenchmarkResult targetMachine, int testMachineCore, int targetMachineCore, double testMachineAppIpc) {
                var testMachineIpc = calcAvgIpc(testMachine, testMachineCore);
                var targetMachineIpc = calcAvgIpc(targetMachine, targetMachineCore);

                return (targetMachineIpc * testMachineAppIpc) / testMachineIpc;
            }
        }

        record WeightedIpcCalculator(
                Map<IPCBenchmarkApp.Type, Double> weight
        ) implements IpcCalculator {
            public double calcAvgIpc(VMBenchmarkResult vmBenchmarkResult, int core) {
                var coreFreqKhz = vmBenchmarkResult.efficientFreqKhz()[core];
                return Arrays.stream(vmBenchmarkResult.benchmarkResults())
                        .filter(b -> !Precision.equals(Optional.ofNullable(weight.get(b.app().type())).orElse(0.0), 0.0))
                        .mapToDouble(b -> (b.instrPerMs() / coreFreqKhz) * weight.get(b.app().type()))
                        .average().getAsDouble();
            }

            @Override
            public double calculate(VMBenchmarkResult testMachine, VMBenchmarkResult targetMachine, int testMachineCore, int targetMachineCore, double testMachineAppIpc) {
                var testMachineIpc = calcAvgIpc(testMachine, testMachineCore);
                var targetMachineIpc = calcAvgIpc(targetMachine, targetMachineCore);

                return (targetMachineIpc * testMachineAppIpc) / testMachineIpc;
            }
        }
    }

    public record Parameters(
            ProgramInfo programInfo,
            VMBenchmarkResult testMachineResult,
            int testMachineCore,
            int targetMachineCore,
            IpcCalculator ipcCalculator,
            double testMachineAppIpc,
            long appIterations
    ) {
    }
}
