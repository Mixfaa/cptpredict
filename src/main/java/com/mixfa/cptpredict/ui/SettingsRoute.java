package com.mixfa.cptpredict.ui;

import com.mixfa.cptpredict.Utils;
import com.mixfa.cptpredict.model.configuration.RepoMode;
import com.mixfa.cptpredict.service.configuraion.ConfigurationManager;
import com.mixfa.cptpredict.service.repo.RepoHolder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Route("/settings")
public class SettingsRoute extends BasicAppLayout {
    private final RepoHolder repoHolder;
    private final ConfigurationManager configurationManager;

    public SettingsRoute(RepoHolder repoHolder, ConfigurationManager configurationManager) {
        this.repoHolder = repoHolder;
        this.configurationManager = configurationManager;

        setContent(makeContent());
    }

    private Component makeContent() {
        var layout = new VerticalLayout();

        var tabs = new TabSheet();
        var tabDatabaseSettings = new Tab("Database Settings");
        tabs.add(tabDatabaseSettings, makeDatabaseSettingsTab());

        layout.add(tabs);
        return layout;
    }

    private Component makeDatabaseSettingsTab() {
        var layout = new VerticalLayout();
        layout.setSizeFull();
        var horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();

        var localFileForm = new FormLayout() {{
            add(new Span("Local database form"));

            var filePath = "";
            filePath = configurationManager.readOrCreateConfiguration().localDatabasePath();

            var filePathInput = new TextField("Local database path");
            filePathInput.setValue(filePath);
            var connectButton = new Button("Connect", _ -> {
                var path = filePathInput.getValue();

                var config = configurationManager.readOrCreateConfiguration()
                        .withLocalDatabasePath(path)
                        .withDatabaseMode(RepoMode.LOCAL);
                configurationManager.writeConfiguration(config);
                repoHolder.switchMode(RepoMode.LOCAL);

                Utils.showNotification("Mode switched");
            });
            add(filePathInput, connectButton);
        }};
        var mongoDBForm = new FormLayout() {{
            add(new Span("MongoDB Form"));

            var databaseUri = "";
            var databaseName = "";
            var config_ = configurationManager.readOrCreateConfiguration();

            databaseUri = config_.databaseUri();
            databaseName = config_.databaseName();

            var databaseUriField = new TextField("Database URI");
            databaseUriField.setValue(databaseUri);
            var databaseNameField = new TextField("Database Name");
            databaseNameField.setValue(databaseName);
            var connectButton = new Button("Connect", _ -> {
                var config = configurationManager.readOrCreateConfiguration()
                        .withDatabaseName(databaseNameField.getValue())
                        .withDatabaseUri(databaseUriField.getValue())
                        .withDatabaseMode(RepoMode.MONGODB);
                configurationManager.writeConfiguration(config);
                repoHolder.switchMode(RepoMode.MONGODB);

                Utils.showNotification("Mode switched");
            });

            add(databaseNameField, databaseUriField, connectButton);
        }};


        horizontalLayout.add(localFileForm, mongoDBForm);
        layout.add(horizontalLayout);
        return layout;
    }
}
