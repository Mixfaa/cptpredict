package com.mixfa.cptpredict.service.repo;

import com.mixfa.cptpredict.model.configuration.RepoMode;
import com.mixfa.cptpredict.service.configuraion.ConfigurationManager;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mapper.jackson.JacksonMapperModule;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RepoHolder {
    private final ConfigurationManager configurationManager;
    private final Map<Class<?>, CustomizableRepo.Proxy<?, ?>> repositories = new ConcurrentHashMap<>();

    private volatile RepoMode currentMode;
    private volatile MongoTemplate mongoTemplate;
    private volatile MongoClient mongoClient;
    private volatile Nitrite database;

    private <T, ID> MongoDbRepo<T, ID> makeMongoDBRepo(Class<T> typeClass) {
        ensureMongoDBConnected();
        return new MongoDbRepo<>(typeClass, mongoTemplate);
    }

    private <T, ID> LocalStoreRepo<T, ID> makeLocalStoreRepo(Class<T> typeClass) {
        ensureNitriteInitialized();
        return new LocalStoreRepo<>(typeClass, database);
    }

    private <T, ID> CustomizableRepo<T, ID> makeRepo(Class<T> typeClass, RepoMode mode) {
        return switch (mode) {
            case LOCAL -> makeLocalStoreRepo(typeClass);
            case MONGODB -> makeMongoDBRepo(typeClass);
        };
    }

    public void switchMode(RepoMode mode) {
        if (mode == currentMode) return;

        repositories.forEach((_, repo) -> {
            try {
                repo.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            repo.setImpl(CustomizableRepo.Stub.getInstance());
        });
        switch (currentMode) {
            case LOCAL -> {
                try {
                    database.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    database = null;
                }
            }
            case MONGODB -> {
                try {
                    mongoClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mongoClient = null;
                    mongoTemplate = null;
                }
            }
        }
        currentMode = mode;

        repositories.forEach((type, repo) -> repo.setImpl(makeRepo(type, mode)));

        configurationManager.writeConfiguration(configurationManager.readOrCreateConfiguration().withDatabaseMode(mode));
    }

    private void ensureMongoDBConnected() {
        if (mongoClient != null && mongoTemplate != null)
            return;

        try {
            var config = configurationManager.readOrCreateConfiguration();

            mongoClient = MongoClients.create(config.databaseUri());
            mongoTemplate = new MongoTemplate(mongoClient, config.databaseName());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                if (mongoClient != null)
                    mongoClient.close();
            } catch (Exception _) {
            }
            mongoTemplate = null;
            mongoClient = null;
        }
    }

    private void ensureNitriteInitialized() {
        if (database != null && !database.isClosed())
            return;

        try {
            var config = configurationManager.readOrCreateConfiguration();

            var storeModule = MVStoreModule.withConfig()
                    .filePath(config.localDatabasePath())
                    .build();

            var jacksonModule = new JacksonMapperModule();
            database = Nitrite.builder()
                    .loadModule(storeModule)
                    .loadModule(jacksonModule)
                    .openOrCreate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            database.close();
            database = null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T, ID> CustomizableRepo<T, ID> getRepository(Class<T> typeClass) {
        return getRepository(typeClass, currentMode);
    }

    @SuppressWarnings("unchecked")
    public <T, ID> CustomizableRepo<T, ID> getRepository(Class<T> typeClass, RepoMode mode) {
        return (CustomizableRepo<T, ID>) repositories.computeIfAbsent(typeClass, typeClass_ -> new CustomizableRepo.Proxy<>(makeRepo(typeClass_, mode)));
    }

    public RepoHolder(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
        this.currentMode = configurationManager.readOrCreateConfiguration().databaseMode();
    }
}
