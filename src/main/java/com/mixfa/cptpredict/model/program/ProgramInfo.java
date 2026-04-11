package com.mixfa.cptpredict.model.program;

import com.mixfa.cptpredict.Utils;
import com.mixfa.cptpredict.model.benchmark.IPCBenchmarkApp;
import lombok.experimental.FieldNameConstants;
import org.dizitart.no2.repository.annotations.Entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document
@Entity
@FieldNameConstants
public record ProgramInfo(
        @org.dizitart.no2.repository.annotations.Id @Id String name,
        String description,
        ComplexityModel instructionModel,
        ComplexityModel cacheMissesModel,
        ComplexityModel dataReadModel,
        ComplexityModel timeModel,
        List<ProgramTestInfo> programTests,
        List<ProgramStructureDataRecord> programStructureDataList
// just to save input data, not used for any calculations
) {
    // to see what app is using more
    public Map<IPCBenchmarkApp.Type, Double> calculateWeights() {
        var instrGrowthRate = instructionModel.growthRate();
        var cacheMissesGrowthRate = cacheMissesModel.growthRate();
        var dataReadGrowthRate = dataReadModel.growthRate();

        var maxGrowth = Math.max(
                instrGrowthRate,
                Math.max(cacheMissesGrowthRate, dataReadGrowthRate)
        );

        var minGrowth = Math.min(
                instrGrowthRate,
                Math.min(cacheMissesGrowthRate, dataReadGrowthRate)
        );

        return Map.of(
                IPCBenchmarkApp.Type.CPU, Utils.map(0.0, 1.0, minGrowth, maxGrowth, instrGrowthRate),
                IPCBenchmarkApp.Type.RAM, Utils.map(0.0, 1.0, minGrowth, maxGrowth, cacheMissesGrowthRate),
                IPCBenchmarkApp.Type.DISK, Utils.map(0.0, 1.0, minGrowth, maxGrowth, dataReadGrowthRate)
        );
    }
}
