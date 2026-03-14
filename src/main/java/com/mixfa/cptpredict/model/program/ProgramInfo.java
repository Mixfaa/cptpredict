package com.mixfa.cptpredict.model.program;

import lombok.experimental.FieldNameConstants;
import org.dizitart.no2.repository.annotations.Entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

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
        List<ProgramStructureData> programStructureDataList // just to save input data, not used for any calculations
) {
}
