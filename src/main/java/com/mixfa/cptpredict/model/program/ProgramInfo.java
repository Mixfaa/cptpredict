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
//        var ramUsed = ramUsageModel.getFunction().applyAsDouble(dataAmount);
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

//    public Map<IPCBenchmarkApp.Type, Double> calculateWeights(
//            VMBenchmarkResult vmBenchmark,
//            double dataAmount) {
//
//
//        var totalInstructions = instructionModel.getFunction().applyAsDouble(dataAmount);
//        var memoryAccesses = cacheMissesModel.getFunction().applyAsDouble(dataAmount);
//        var maxMemoryKb = ramUsageModel.getFunction().applyAsDouble(dataAmount);
//        // 1. Извлекаем базовые IPC (инструкций за такт)
//        double ipcCpu = vmBenchmark.avgIPC(IPCBenchmarkApp.Type.CPU);
//        double ipcRam = vmBenchmark.avgIPC(IPCBenchmarkApp.Type.RAM);
//        double ipcDisk = vmBenchmark.avgIPC(IPCBenchmarkApp.Type.DISK);
//
//        // 2. Рассчитываем "чистые" вычислительные инструкции
//        // Если общее кол-во включает в себя инструкции доступа к памяти, вычитаем их
//        double pureComputeInstructions = Math.max(0, totalInstructions - memoryAccesses);
//
//        // 3. Рассчитываем условное время (в тактах), затрачиваемое на каждый тип операции
//        // Формула: Время = Инструкции / IPC (что эквивалентно Инструкции * CPI)
//
//        // Время на вычисления
//        double timeCpu = pureComputeInstructions / ipcCpu;
//
//        // Время на работу с памятью (используем количество доступов)
//        double timeRam = memoryAccesses / ipcRam;
//
//        // Время на диск (оставляем вашу логику dataRead или адаптируем под IPC)
//        // Допустим, dataRead — это количество низкоуровневых операций IO
//        double timeDisk = 100 / ipcDisk; // Здесь подставьте вашу переменную для Disk IO
//
//        // 4. Учет объема памяти (Memory Footprint)
//        // Чем больше памяти мы используем, тем сильнее это "давит" на RAM составляющую
//        // Коэффициент можно настроить: например, 1.0 + (kb / 1024 / 1024)
//        double memFactor = 1.0 + (maxMemoryKb / 1024.0 / 1024.0); // нормализация к Гб
//        timeRam *= memFactor;
//
//        // 5. Нормализация весов
//        double totalTime = timeCpu + timeRam + timeDisk;
//
//        return Map.of(
//                IPCBenchmarkApp.Type.CPU, 0.5,
//                IPCBenchmarkApp.Type.RAM, 0.5,
//                IPCBenchmarkApp.Type.DISK, 0.0
//        );
//    }
}
