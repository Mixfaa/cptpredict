package com.mixfa.cptpredict.model.configuration;

import lombok.With;

@With
public record ConfigurationPojo(
        RepoMode databaseMode,
        String databaseUri,
        String databaseName,
        String localDatabasePath
) {
    private static final ConfigurationPojo defaultConfiguration = new ConfigurationPojo(
            RepoMode.LOCAL,
            "", "",
            "local-database"
    );

    public static ConfigurationPojo getDefault() {
        return defaultConfiguration;
    }
}
