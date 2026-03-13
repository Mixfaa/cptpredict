package com.mixfa.cptpredict.service.configuraion;


import com.mixfa.cptpredict.model.configuration.ConfigurationPojo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

@Service
@RequiredArgsConstructor
public class ConfigurationManager {
    private static final String CONFIGURATION_FILE = "configuration.json";
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @SneakyThrows
    public ConfigurationPojo readOrCreateConfiguration() {
        try (var configFileStream = new FileInputStream(CONFIGURATION_FILE)) {
            return objectMapper.readValue(configFileStream, ConfigurationPojo.class);
        } catch (FileNotFoundException e) {
            try (var outputFileStream = new FileOutputStream(CONFIGURATION_FILE)) {
                objectMapper.writeValue(outputFileStream, ConfigurationPojo.getDefault());
            }
        }
        return ConfigurationPojo.getDefault();
    }

    @SneakyThrows
    public void writeConfiguration(ConfigurationPojo configurationPojo) {
        try (var configFileStream = new FileOutputStream(CONFIGURATION_FILE)) {
            objectMapper.writeValue(configFileStream, configurationPojo);
        }
    }
}
