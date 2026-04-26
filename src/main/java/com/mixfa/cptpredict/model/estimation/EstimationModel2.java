package com.mixfa.cptpredict.model.estimation;

import com.mixfa.cptpredict.misc.BigOAnalysis;
import com.mixfa.cptpredict.misc.datacollection.CollectableData;
import com.mixfa.cptpredict.misc.datacollection.DataCollector;
import com.mixfa.cptpredict.model.VMBenchmarkResult;
import com.mixfa.cptpredict.model.VMConfig;
import com.mixfa.cptpredict.model.benchmark.BenchmarkAppResult;
import com.mixfa.cptpredict.model.benchmark.IPCBenchmarkApp;
import com.mixfa.cptpredict.model.program.ProgramInfo;
import com.mixfa.cptpredict.model.program.ProgramTestInfo;
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
    public EstimationResult estimate(VMConfig vmConfig, Parameters parameters, DataCollector dataCollector) {
        final var dataAmount = parameters.dataAmount();
        var weights = parameters.programInfo.calculateWeights(parameters.testMachineResult, dataAmount);

        weights.forEach((type, weight) -> dataCollector.collectData(CollectableData.format("Weight %s: %.5f", type.name(), weight)));

        var ipcCalculator = new EstimationModel2.IpcCalculator.WeightedIpcCalculator(weights);

        var ramUsage = parameters.programInfo().ramUsageModel().getFunction().applyAsDouble(dataAmount);

        dataCollector.collectData(CollectableData.format("Ram Usage: %.2f mb", ramUsage / 1024));

        if (vmConfig.benchmarkResult().availableMemoryKb() <= ramUsage)
            dataCollector.collectData(new CollectableData.RamLimitExceeded(vmConfig.benchmarkResult().availableMemoryKb(), (long) ramUsage));

        var targetMachineBenchmarkResult = vmConfig.benchmarkResult();
        var targetMachineFreqKhz = targetMachineBenchmarkResult.efficientFreqKhz()[parameters.targetMachineCore]; // hz to c per ms

        dataCollector.collectData(CollectableData.format("Target Machine Freq Khz: %.5f", targetMachineFreqKhz));

        var appIpcModel = BigOAnalysis.analyze(
                parameters.programInfo.programTests().stream().mapToDouble(ProgramTestInfo::dataAmount).toArray(),
                parameters.programInfo.programTests().stream().mapToDouble(ProgramTestInfo::appIpc).toArray()
        );

        dataCollector.collectData(CollectableData.format("App Ipc Model: %s", appIpcModel.formula()));

        var testMachineAppIpc = appIpcModel.getFunction().applyAsDouble(dataAmount);

        dataCollector.collectData(CollectableData.format("Test machine app Ipc: %.5f", testMachineAppIpc));

        var appIpc = ipcCalculator.calculate(
                parameters.testMachineResult,
                vmConfig.benchmarkResult(),
                parameters.testMachineCore,
                parameters.targetMachineCore,
                testMachineAppIpc
        );

        dataCollector.collectData(CollectableData.format("Target machine app Ipc: %.5f", appIpc));

        var appComplexityFunc = parameters.programInfo.instructionModel().getFunction();

        var instructions = appComplexityFunc.applyAsDouble(dataAmount);

        dataCollector.collectData(CollectableData.format("App total instructions: %.1f", instructions));

        var time = (long) (instructions / (targetMachineFreqKhz * appIpc));

        dataCollector.collectData(CollectableData.format("Total time: %d ms", time));

        return new EstimationResult(
                vmConfig,
                Duration.ofMillis(time < 0 ? 0 : time),
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
            long dataAmount
    ) {
    }
}
