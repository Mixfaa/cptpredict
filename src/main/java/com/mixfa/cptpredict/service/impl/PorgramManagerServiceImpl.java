package com.mixfa.cptpredict.service.impl;

import com.mixfa.cptpredict.misc.BigOAnalysis;
import com.mixfa.cptpredict.model.program.ProgramInfo;
import com.mixfa.cptpredict.model.program.ProgramStructureData;
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
    public ProgramInfo save(String name, String description, List<ProgramTestInfo> programTests, List<ProgramStructureData> programStructureDataList) {
        var dataAmountArray = programStructureDataList.stream().mapToDouble(ProgramStructureData::dataAmount).toArray();

        var instructionsComplxModel = BigOAnalysis.analyze(
                dataAmountArray,
                programStructureDataList.stream().mapToDouble(ProgramStructureData::instructions).toArray()
        );

        var cacheMissesComplxModel = BigOAnalysis.analyze(
                dataAmountArray,
                programStructureDataList.stream().mapToDouble(ProgramStructureData::cacheMisses).toArray()
        );

        var dataReadComplxModel = BigOAnalysis.analyze(
                dataAmountArray,
                programStructureDataList.stream().mapToDouble(ProgramStructureData::dataBytesRead).toArray()
        );


        return programRepo.save(new ProgramInfo(name, description, instructionsComplxModel, cacheMissesComplxModel, dataReadComplxModel, programTests));
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
