package com.mixfa.cptpredict.model;

import com.mixfa.cptpredict.model.benchmark.BenchmarkAppResult;

public record VMBenchmarkResult(
        String cpuName,
        int coreCount,
        double[] efficientFreqKhz,
        BenchmarkAppResult[] benchmarkResults
) {
    public int highestFreqCore() {
        if (efficientFreqKhz == null || efficientFreqKhz.length == 0)
            return -1;

        var maxFreqCore = 0;
        var maxFreq = efficientFreqKhz[0];
        for (var i = 1; i < efficientFreqKhz.length; i++)
            if (efficientFreqKhz[i] > maxFreq)
                maxFreqCore = i;

        return maxFreqCore;
    }
}
