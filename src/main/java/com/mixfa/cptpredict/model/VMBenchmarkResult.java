package com.mixfa.cptpredict.model;

import com.mixfa.cptpredict.model.benchmark.BenchmarkAppResult;
import com.mixfa.cptpredict.model.benchmark.IPCBenchmarkApp;

import java.util.Arrays;
import java.util.function.ToIntFunction;

public record VMBenchmarkResult(
        String cpuName,
        int coreCount,
        double[] efficientFreqKhz,
        BenchmarkAppResult[] benchmarkResults
) {
    public int highestFreqCore() {
        if (efficientFreqKhz == null || efficientFreqKhz.length == 0)
            return 0;

        var maxFreqCore = 0;
        var maxFreq = efficientFreqKhz[0];
        for (var i = 1; i < efficientFreqKhz.length; i++)
            if (efficientFreqKhz[i] > maxFreq)
                maxFreqCore = i;

        return maxFreqCore;
    }

    public double avgIPC(IPCBenchmarkApp.Type benchmarkType, ToIntFunction<VMBenchmarkResult> coreSelector) {
        var avgInstrPerMs = Arrays.stream(benchmarkResults)
                .filter(result -> result.app().type() == benchmarkType)
                .mapToDouble(BenchmarkAppResult::instrPerMs)
                .average().getAsDouble();

        var freqKhz = efficientFreqKhz[coreSelector.applyAsInt(this)];

        return avgInstrPerMs / freqKhz;
    }

    public double avgIPC(IPCBenchmarkApp.Type benchmarkType) {
        return avgIPC(benchmarkType, VMBenchmarkResult::highestFreqCore);
    }
}
