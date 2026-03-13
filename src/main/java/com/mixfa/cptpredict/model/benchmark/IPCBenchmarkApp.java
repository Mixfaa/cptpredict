package com.mixfa.cptpredict.model.benchmark;

import java.text.MessageFormat;

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

    private static final String LINUX_POSTFIX = "unknown-linux-gnu";
    private static final String WINDOWS_POSTFIX = "pc-windows-gnu.exe";
    private static final String x86_64_POSTFIX = "x86_64";
    private static final String AARCH64_POSTFIX = "aarch64";

    private static final MessageFormat FORMAT = new MessageFormat("{0}-{1}-{2}");

    public String executableName(String os, String arch) {
        var osPostfix = switch (os) {
            case "windows" -> WINDOWS_POSTFIX;
            case "linux" -> LINUX_POSTFIX;
            default -> null;
        };

        if (os.startsWith("windows"))
            osPostfix = WINDOWS_POSTFIX;
        else if (osPostfix == null)
            throw new IllegalArgumentException("Unknown OS: " + os);

        var archPostfix = switch (arch) {
            case "arm64" -> AARCH64_POSTFIX;
            case "amd64", "x86_64" -> x86_64_POSTFIX;
            default -> throw new IllegalStateException("Unexpected value: " + arch );
        };
        return FORMAT.format(new Object[]{executableName, archPostfix, osPostfix});
    }
}
