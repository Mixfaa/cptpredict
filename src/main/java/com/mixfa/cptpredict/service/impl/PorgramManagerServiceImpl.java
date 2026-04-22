package com.mixfa.cptpredict.service.impl;

import com.mixfa.cptpredict.misc.BigOAnalysis;
import com.mixfa.cptpredict.model.program.ProgramInfo;
import com.mixfa.cptpredict.model.program.ProgramStructureDataRecord;
import com.mixfa.cptpredict.model.program.ProgramTestInfo;
import com.mixfa.cptpredict.service.ProgramManagerService;
import com.mixfa.cptpredict.service.repo.CustomizableRepo;
import com.mixfa.cptpredict.service.repo.RepoHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PorgramManagerServiceImpl implements ProgramManagerService {
    private final CustomizableRepo<ProgramInfo, String> programRepo;

    public PorgramManagerServiceImpl(RepoHolder repoHolder) {
        this.programRepo = repoHolder.getRepository(ProgramInfo.class);
    }

    @Override
    public ProgramInfo save(String name, String description, List<ProgramTestInfo> programTests, List<ProgramStructureDataRecord> programStructureDataList) {
        var dataAmountArray = programStructureDataList.stream().mapToDouble(ProgramStructureDataRecord::dataAmount).toArray();

        var instructionsComplxModel = BigOAnalysis.analyze(
                dataAmountArray,
                programStructureDataList.stream().mapToDouble(ProgramStructureDataRecord::instructions).toArray()
        );

        var cacheMissesComplxModel = BigOAnalysis.analyze(
                dataAmountArray,
                programStructureDataList.stream().mapToDouble(ProgramStructureDataRecord::cacheMisses).toArray()
        );

        var dataReadComplxModel = BigOAnalysis.analyze(
                dataAmountArray,
                programStructureDataList.stream().mapToDouble(ProgramStructureDataRecord::dataBytesRead).toArray()
        );

        var timeComplxModel = BigOAnalysis.analyze(
                dataAmountArray,
                programStructureDataList.stream().mapToDouble(ProgramStructureDataRecord::timeInMs).toArray()
        );

        var ramUsageComplxModel = BigOAnalysis.analyze(
                dataAmountArray,
                programStructureDataList.stream().mapToDouble(ProgramStructureDataRecord::ramUsedKb).toArray()
        );

        return programRepo.save(new ProgramInfo(name, description, instructionsComplxModel, cacheMissesComplxModel, dataReadComplxModel, timeComplxModel, ramUsageComplxModel, programTests, programStructureDataList));
    }

    @Override
    public void delete(ProgramInfo programInfo) {
        programRepo.delete(programInfo);
    }

    @Override
    public List<ProgramInfo> findAll() {
        return programRepo.findAll();
    }
}
