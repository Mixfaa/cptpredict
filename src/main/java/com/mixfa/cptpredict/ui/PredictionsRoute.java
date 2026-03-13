package com.mixfa.cptpredict.ui;

import com.mixfa.cptpredict.model.VMConfig;
import com.mixfa.cptpredict.model.estimation.EstimationModel;
import com.mixfa.cptpredict.model.estimation.EstimationModel2;
import com.mixfa.cptpredict.model.estimation.EstimationResult;
import com.mixfa.cptpredict.model.program.ProgramInfo;
import com.mixfa.cptpredict.model.program.ProgramTestInfo;
import com.mixfa.cptpredict.service.EstimationModelManager;
import com.mixfa.cptpredict.service.repo.CustomizableRepo;
import com.mixfa.cptpredict.service.repo.RepoHolder;
import com.mixfa.cptpredict.ui.components.VmConfigCompRenderer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.Comparator;
import java.util.Set;

@Route("/predict")
public class PredictionsRoute extends BasicAppLayout {
    private final CustomizableRepo<VMConfig, String> vmConfigRepo;
    private final CustomizableRepo<ProgramInfo, String> appRepo;
    private final EstimationModelManager estimationModelManager;

    public PredictionsRoute(RepoHolder repoHolder, EstimationModelManager estimationModelManager) {
        this.vmConfigRepo = repoHolder.getRepository(VMConfig.class);
        this.appRepo = repoHolder.getRepository(ProgramInfo.class);
        this.estimationModelManager = estimationModelManager;

        setContent(makeContent());
    }

    private Component makePredictionForm(Set<VMConfig> vmConfigs, ProgramInfo programInfo, EstimationModel<?> estimationModel, Grid<EstimationResult> resultGrid) {
        var formLayout = new FormLayout();

        switch (estimationModel) {
            case EstimationModel2 em2 -> {
                var testIpcSelect = new Select<ProgramTestInfo>("Select program test data");
                testIpcSelect.setItems(programInfo.programTests());
                testIpcSelect.setItemLabelGenerator(ProgramTestInfo::toString);

                var dataAmountField = new IntegerField("Data amount (N)");
                dataAmountField.setMin(1);


                var getResultsButton = new Button("Get results", _ -> {
                    var testResult = testIpcSelect.getValue();

                    var ipcCalculator = EstimationModel2.IpcCalculator.DefaultIpcCalculator.getInstance();

                    var results = vmConfigs.stream().map(vmConfig -> {
                        var params = new EstimationModel2.Parameters(
                                programInfo,
                                testResult.vmBenchmarkResult(),
                                testResult.vmBenchmarkResult().highestFreqCore(),
                                vmConfig.benchmarkResult().highestFreqCore(),
                                ipcCalculator,
                                testResult.appIpc(),
                                dataAmountField.getValue()
                        );

                        return em2.estimate(vmConfig, params);
                    }).toList();

                    resultGrid.setItems(results);
                });

                formLayout.add(testIpcSelect, dataAmountField, getResultsButton);
            }
        }

        return formLayout;
    }

    private Component makePredictionUI(Set<VMConfig> vmConfig, EstimationModel<?> estimationModel) {
        final VerticalLayout uiLayout = new VerticalLayout();
        final var resultGrid = new Grid<EstimationResult>();

        var appSelect = new Select<ProgramInfo>("Application");
        appSelect.setItemLabelGenerator(ProgramInfo::name);
        appSelect.setItems(appRepo.findAll());

        appSelect.addValueChangeListener(e -> {
            uiLayout.removeAll();
            uiLayout.add(makePredictionForm(vmConfig, e.getValue(), estimationModel, resultGrid));
        });

        resultGrid.addColumn(result -> result.targetVM().name()).setHeader("Target VM");
        resultGrid.addColumn(result -> DurationFormatUtils.formatDurationWords(result.duration().toMillis(), true, true) + String.format("(%d sec)", result.duration().toSeconds()))
                .setHeader("Duration")
                .setSortable(true)
                .setComparator(Comparator.comparing(EstimationResult::duration));
        resultGrid.addColumn(result -> result.bill().toPrettyString()).setHeader("Bill")
                .setSortable(true)
                .setComparator(Comparator.comparing(EstimationResult::bill));

        return new VerticalLayout(uiLayout, appSelect, resultGrid);
    }

    private void onSelectValueChanged(MultiSelectComboBox<VMConfig> vmConfigSelect, Select<EstimationModel<?>> estimationModelSelect, VerticalLayout estimationLayout) {
        var vmConfigs = vmConfigSelect.getValue();
        var estimationModel = estimationModelSelect.getValue();

        if (vmConfigs != null && !vmConfigs.isEmpty() && estimationModel != null) {
            estimationLayout.removeAll();
            estimationLayout.add(makePredictionUI(vmConfigs, estimationModel));
        }
    }

    private Component makeContent() {
        var layout = new VerticalLayout();
        layout.setSizeFull();

        if (vmConfigRepo.isStub()) {
            layout.add(new Span("Connect to database first"));
            return layout;
        }

        var horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();

        var vmConfigSelect = new MultiSelectComboBox<>("Choose VM Config", vmConfigRepo.findAll());
        vmConfigSelect.setWidthFull();
        vmConfigSelect.setItemLabelGenerator(VMConfig::name);
        vmConfigSelect.setRenderer(VmConfigCompRenderer.getInstance());

        Select<EstimationModel<?>> estimationModelSelect = new Select<>("Choose estimation model", estimationModelManager.findAll());
        estimationModelSelect.setWidthFull();
        estimationModelSelect.setRenderer(new ComponentRenderer<>(estimationModel -> new Span(estimationModel.name())));
        horizontalLayout.add(new VerticalLayout(vmConfigSelect) {{
            setWidth("40%");
        }}, new VerticalLayout(estimationModelSelect) {{
            setWidth("40%");
        }});
        layout.add(horizontalLayout);

        var estimationLayout = new VerticalLayout();

        vmConfigSelect.addValueChangeListener(_ -> onSelectValueChanged(vmConfigSelect, estimationModelSelect, estimationLayout));
        estimationModelSelect.addValueChangeListener(_ -> onSelectValueChanged(vmConfigSelect, estimationModelSelect, estimationLayout));
        layout.add(estimationLayout);
        return layout;
    }
}
