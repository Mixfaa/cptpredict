package com.mixfa.cptpredict.ui;

import com.mixfa.cptpredict.misc.BigOAnalysis;
import com.mixfa.cptpredict.model.VMConfig;
import com.mixfa.cptpredict.model.program.ComplexityModel;
import com.mixfa.cptpredict.model.program.ProgramInfo;
import com.mixfa.cptpredict.model.program.ProgramTestInfo;
import com.mixfa.cptpredict.service.repo.CustomizableRepo;
import com.mixfa.cptpredict.service.repo.RepoHolder;
import com.mixfa.cptpredict.ui.components.DialogCloseButton;
import com.mixfa.cptpredict.ui.components.VmConfigCompRenderer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Route("/programs")
public class ProgramsManagerRoute extends BasicAppLayout {
    private final CustomizableRepo<ProgramInfo, String> appRepo;
    private final CustomizableRepo<VMConfig, String> vmConfigRepo;

    public ProgramsManagerRoute(RepoHolder repoHolder) {
        this.appRepo = repoHolder.getRepository(ProgramInfo.class);
        this.vmConfigRepo = repoHolder.getRepository(VMConfig.class);

        setContent(makeContent());
    }

    record NTEnter(
            Component component,
            NumberField nField,
            NumberField tField
    ) {
        public NTEnter(Consumer<NTEnter> onRemove) {
            var nField = new NumberField("n");
            var tField = new NumberField("t");
            var layout = new HorizontalLayout();

            this(
                    layout,
                    nField,
                    tField
            );

            layout.add(
                    nField,
                    tField,
                    new Button("remove", _ -> onRemove.accept(this))
            );
        }
    }

    private static NTEnter makeNTEnter(Consumer<NTEnter> onRemove) {
        return new NTEnter(onRemove);
    }

    private Dialog addAppDialog(Consumer<ProgramInfo> onSave, Optional<ProgramInfo> programInfo) {
        var dialog = new Dialog();
        var layout = new VerticalLayout();
        dialog.setWidth("800px");
        dialog.setHeight("800px");
        dialog.setHeaderTitle("Add program configuration");

        var accordion = new Accordion();
        accordion.setWidthFull();

        var complexityAnalysisLayout = new VerticalLayout();
        var complexityModelRef = new AtomicReference<ComplexityModel>();
        programInfo.ifPresent(p -> complexityModelRef.set(p.model()));
        {
            var ntEnterComps = new ArrayList<NTEnter>();
            var fields = new VerticalLayout();

            Consumer<NTEnter> onRemove = nt -> {
                ntEnterComps.remove(nt);
                fields.removeAll();
                fields.add(ntEnterComps.stream().map(NTEnter::component).toList());
            };

            var addNtEnterButton = new Button("add", _ -> {
                ntEnterComps.add(new NTEnter(onRemove));
                fields.removeAll();
                fields.add(ntEnterComps.stream().map(NTEnter::component).toList());
            });

            ntEnterComps.add(makeNTEnter(onRemove));
            fields.add(ntEnterComps.stream().map(NTEnter::component).toList());

            var complexityModelSpan = new Span();
            var complexityModel = complexityModelRef.get();
            if (complexityModel != null)
                complexityModelSpan.setText(complexityModel.formula());

            var analyzeButton = new Button("analyze", _ -> {
                var nList = ntEnterComps.stream().mapToDouble(nt -> nt.nField.getValue()).toArray();
                var tList = ntEnterComps.stream().mapToDouble(nt -> nt.tField.getValue()).toArray();

                complexityModelRef.set(BigOAnalysis.analyze(nList, tList));

                complexityModelSpan.setText(complexityModelRef.get().formula());
            });

            complexityAnalysisLayout.add(addNtEnterButton, fields, analyzeButton, complexityModelSpan);
        }

        var basicDataLayout = new VerticalLayout();

        var nameField = new TextField("Name");
        var descriptionField = new TextField("Description");

        basicDataLayout.add(nameField, descriptionField);

        var addTestsLayout = new VerticalLayout() {{
            setWidthFull();
        }};
        var programTestInfoList = new ArrayList<ProgramTestInfo>();
        programInfo.ifPresent(p -> {
            nameField.setValue(p.name());
            descriptionField.setValue(p.description());
            programTestInfoList.addAll(p.programTests());
        });
        {
            var nField = new NumberField("Data amount (N)") {{
                setMin(1.0);
            }};
            var timeInput = new NumberField("Time required (in milliseconds)") {{
                setMin(1.0);
            }};

            var ipcSpan = new Span("IPC: ");

            var vmSelect = new Select<VMConfig>("Select VM Configuration");
            vmSelect.setRenderer(VmConfigCompRenderer.getInstance());
            vmSelect.setItems(vmConfigRepo.findAll());

            Runnable onEdited = () -> {
                var n = nField.getValue();
                var t = timeInput.getValue();
                var vmConfig = vmSelect.getValue();

                if (n == null || t == null || vmConfig == null) {
                    return;
                }

                var coreFreqKhz = vmConfig.benchmarkResult().efficientFreqKhz()[vmConfig.benchmarkResult().highestFreqCore()];
                var ipc = (n / t) / coreFreqKhz;

                ipcSpan.setText("IPC: " + ipc);
            };

            nField.addValueChangeListener(_ -> onEdited.run());
            timeInput.addValueChangeListener(_ -> onEdited.run());

            var testsGrid = new Grid<>(ProgramTestInfo.class, false);
            testsGrid.setItems(programTestInfoList);
            testsGrid.setWidthFull();
            testsGrid.addColumn(info -> info.vmBenchmarkResult().cpuName()).setHeader("CPU");
            testsGrid.addColumn(info -> String.format("%.5f", info.appIpc())).setHeader("IPC");
            testsGrid.addComponentColumn(info -> new Button(VaadinIcon.CLOSE_CIRCLE.create(), _ -> {
                programTestInfoList.remove(info);
                testsGrid.setItems(programTestInfoList);
            }));
            var setBtn = new Button("add", _ -> {
                var vmConfig = vmSelect.getValue();
                if (vmConfig == null) {
                    Notification.show("Select vm configuration");
                    return;
                }

                var n = nField.getValue();
                var t = timeInput.getValue();

                if (n == null || t == null) {
                    Notification.show("Enter data amount and time required (in ms)");
                    return;
                }
                var coreFreqKhz = vmConfig.benchmarkResult().efficientFreqKhz()[vmConfig.benchmarkResult().highestFreqCore()];
                var ipc = (n / t) / coreFreqKhz;

                programTestInfoList.add(new ProgramTestInfo(
                        vmConfig.benchmarkResult(),
                        ipc
                ));
                testsGrid.setItems(programTestInfoList);
                Notification.show("Result added");
            });

            addTestsLayout.add(vmSelect, nField, timeInput, ipcSpan, setBtn, testsGrid);
        }

        accordion.add("Basic data", basicDataLayout);
        accordion.add("Complexity analysis", complexityAnalysisLayout);
        accordion.add("Add tests data", addTestsLayout);

        var saveButton = new Button("save", _ -> {
            var name = nameField.getValue();
            var description = descriptionField.getValue();

            var complexityModel = complexityModelRef.get();

            if (StringUtils.isBlank(name)) {
                Notification.show("Enter name");
                return;
            }
            if (StringUtils.isBlank(description)) {
                Notification.show("Enter description");
                return;
            }
            if (complexityModel == null) {
                Notification.show("Perform complexity analysis");
                return;
            }

            onSave.accept(new ProgramInfo(name, description, complexityModel, programTestInfoList));
            Notification.show("Program " + name + " saved");
        });

        layout.add(accordion);
        layout.setSizeFull();
        dialog.add(layout);
        dialog.getFooter().add(saveButton, new DialogCloseButton(dialog));
        return dialog;
    }

    private Component makeContent() {
        var layout = new VerticalLayout();

        if (vmConfigRepo.isStub() || appRepo.isStub()) {
            layout.add(new Span("Connect to database first"));
            return layout;
        }

        var appGrid = new Grid<>(ProgramInfo.class, false);
        var addAppDialog = addAppDialog(p -> {
            appRepo.save(p);
            appGrid.setItems(appRepo.findAll());
        }, Optional.empty());
        var addAppConfigButton = new Button("Add app config", _ -> addAppDialog.open());
        appGrid.addColumn(ProgramInfo::name).setHeader("Name");
        appGrid.addColumn(ProgramInfo::description).setHeader("Name");
        appGrid.addColumn(p -> p.model().formula()).setHeader("Model");
        appGrid.addComponentColumn(p -> new Button(VaadinIcon.COG.create(), _ -> addAppDialog(edited -> {
            appRepo.delete(p);
            appRepo.save(edited);
            appGrid.setItems(appRepo.findAll());
        }, Optional.of(p)).open()));
        appGrid.addComponentColumn(p -> new Button(VaadinIcon.CLOSE_CIRCLE.create(), _ -> {
            appRepo.delete(p);
            appGrid.setItems(appRepo.findAll());
        }));

        appGrid.setItems(appRepo.findAll());
        layout.add(addAppConfigButton, appGrid);

        return layout;
    }
}
