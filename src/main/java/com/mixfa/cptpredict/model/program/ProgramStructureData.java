package com.mixfa.cptpredict.model.program;

public record ProgramStructureData(
        double dataAmount,
        double instructions,
        double cacheMisses,
        double dataBytesRead,
        double timeInMs
) {
}
