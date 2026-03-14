package com.mixfa.cptpredict.model.program;

public record ProgramStructureData(
        long dataAmount,
        long instructions,
        long cacheMisses,
        long dataBytesRead
) {
}
