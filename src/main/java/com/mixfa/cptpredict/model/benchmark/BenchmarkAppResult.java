package com.mixfa.cptpredict.model.benchmark;

public record BenchmarkAppResult(
        IPCBenchmarkApp app,
        double instrPerMs
) {
}
