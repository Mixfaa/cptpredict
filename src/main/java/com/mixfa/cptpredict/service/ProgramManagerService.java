package com.mixfa.cptpredict.service;

import com.mixfa.cptpredict.model.program.ProgramInfo;
import com.mixfa.cptpredict.model.program.ProgramStructureDataRecord;
import com.mixfa.cptpredict.model.program.ProgramTestInfo;

import java.util.List;

public interface ProgramManagerService {
    ProgramInfo save(
            String name,
            String description,
            List<ProgramTestInfo> programTests,
            List<ProgramStructureDataRecord> programStructureDataList
    );

    void delete(ProgramInfo programInfo);

    List<ProgramInfo> findAll();
}
