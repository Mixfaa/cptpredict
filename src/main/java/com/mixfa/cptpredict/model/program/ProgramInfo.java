package com.mixfa.cptpredict.model.program;

import com.mixfa.cptpredict.model.VMBenchmarkResult;
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
        ComplexityModel ramUsageModel,
        List<ProgramTestInfo> programTests,
        List<ProgramStructureDataRecord> programStructureDataList
// just to save input data, not used for any calculations
) {
    // to see what app is using more
    public Map<IPCBenchmarkApp.Type, Double> calculateWeights(VMBenchmarkResult vmBenchmark, double dataAmount) {
        var cacheMisses = cacheMissesModel.getFunction().applyAsDouble(dataAmount);
        var dataRead = dataReadModel.getFunction().applyAsDouble(dataAmount);
        var instructions = instructionModel.getFunction().applyAsDouble(dataAmount) - (cacheMisses + dataRead);

        var avgCpuIpc = vmBenchmark.avgIPC(IPCBenchmarkApp.Type.CPU);
        var avgRamIpc = vmBenchmark.avgIPC(IPCBenchmarkApp.Type.RAM);
        var avgDiskIpc = vmBenchmark.avgIPC(IPCBenchmarkApp.Type.DISK);

        var i = instructions / avgCpuIpc;
        var c = cacheMisses * (avgCpuIpc / (avgRamIpc * avgRamIpc));
        var d = dataRead * (avgCpuIpc / (avgDiskIpc * avgDiskIpc));

        var sum = i + c + d;

        var ratioI = i / sum;
        var ratioC = c / sum;
        var ratioD = d / sum;

        return Map.of(
                IPCBenchmarkApp.Type.CPU, ratioI,
                IPCBenchmarkApp.Type.RAM, ratioC,
                IPCBenchmarkApp.Type.DISK, ratioD
        );
    }

}
