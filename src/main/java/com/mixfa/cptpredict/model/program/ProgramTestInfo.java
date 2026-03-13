package com.mixfa.cptpredict.model.program;

import com.mixfa.cptpredict.model.VMBenchmarkResult;

public record ProgramTestInfo(
        VMBenchmarkResult vmBenchmarkResult,
        double appIpc
) {
    @Override
    public String toString() {
        return String.format("IPC: %.5f on CPU: %s (cores %d)", appIpc, vmBenchmarkResult.cpuName(), vmBenchmarkResult.coreCount());
    }
}
