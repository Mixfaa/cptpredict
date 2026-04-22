package com.mixfa.cptpredict.model.benchmark;

import com.mixfa.cptpredict.misc.Utils;

public record IPCBenchmarkApp(
        String executableName,
        Type type,
        double testedInstructions
) {
    public enum Type {
        CPU,
        RAM,
        DISK
    }

    public String executableName(String os, String arch) {
        return Utils.executableName(executableName, os, arch);
    }
}
