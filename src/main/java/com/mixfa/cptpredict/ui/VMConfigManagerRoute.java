package com.mixfa.cptpredict.ui;

import com.mixfa.cptpredict.Utils;
import com.mixfa.cptpredict.model.VMBenchmarkResult;
import com.mixfa.cptpredict.model.VMConfig;
import com.mixfa.cptpredict.model.benchmark.BenchmarkAppResult;
import com.mixfa.cptpredict.model.finance.Money;
import com.mixfa.cptpredict.model.pricingPolicy.PricingPolicy;
import com.mixfa.cptpredict.model.pricingPolicy.ReservedPricingPolicy;
import com.mixfa.cptpredict.service.VMBenchmarker;
import com.mixfa.cptpredict.service.repo.CustomizableRepo;
import com.mixfa.cptpredict.service.repo.RepoHolder;
import com.mixfa.cptpredict.ui.components.DeleteConfirmDialog;
import com.mixfa.cptpredict.ui.components.DialogCloseButton;
import com.mixfa.cptpredict.ui.components.DurationPicker;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableSupplier;
import org.vaadin.addons.componentfactory.spinner.Spinner;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Route("/vmconfigs")
public class VMConfigManagerRoute extends BasicAppLayout {
    private final VMBenchmarker vmBenchmarker;
    private final CustomizableRepo<VMConfig, String> vmConfigRepo;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public VMConfigManagerRoute(RepoHolder repoHolder, VMBenchmarker vmBenchmarker) {
        this.vmConfigRepo = repoHolder.getRepository(VMConfig.class);
        this.vmBenchmarker = vmBenchmarker;

        setContent(makeContent());
    }


    private Button makeBenchmarkButton(AtomicReference<VMBenchmarkResult> benchmarkResultRef, String btnText, FailableSupplier<VMBenchmarkResult, Exception> benchmarkFunction) {
        AtomicBoolean benchmarkStarted = new AtomicBoolean(false);
        var benchmarkButton = new Button(btnText);
        benchmarkButton.addClickListener(_ -> {
            if (benchmarkStarted.get()) {
                Utils.showNotification("Benchmarking already started");
                return;
            }
            if (benchmarkResultRef.get() != null) {
                Utils.showNotification("Local machine already benchmarked");
                return;
            }
            Utils.showNotification("Benchmark started, please wait...");
            var ui = getUI().get();

            var notification = new Notification() {{
                add(new HorizontalLayout() {{
                    add(new Span("Benchmarking..."), new Spinner());
                }});
            }};
            notification.open();
            benchmarkButton.setText("Benchmarking...");
            executor.execute(() ->
            {
                try {
                    benchmarkStarted.set(true);
                    benchmarkResultRef.set(benchmarkFunction.get());
                    ui.access(() -> {
                        benchmarkButton.setIcon(VaadinIcon.CHECK_CIRCLE.create());
                        Utils.showNotification("Benchmark finished");
                    });
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage(), e);
                    ui.access(() -> Utils.showErrorNotification(e));
                } finally {
                    benchmarkStarted.set(false);
                    ui.access(() -> {
                        notification.close();
                        benchmarkButton.setText("Benchmark");
                    });
                }
            });
        });
        return benchmarkButton;
    }

    private Component makeContent() {
        var layout = new VerticalLayout();

        if (vmConfigRepo.isStub()) {
            layout.add(new Span("Connect to database first"));
            return layout;
        }

        var vmConfigGrid = new Grid<>(VMConfig.class, false);
        {
            var horizontalLayout = new HorizontalLayout();
            horizontalLayout.add(new Button("Add VM configuration", _ -> new Dialog("Add VM configuration") {
                {
                    var dialogLayout = new VerticalLayout();
                    dialogLayout.add(new Span("VM configuration:"));

                    var nameField = new TextField("Name");
                    dialogLayout.add(nameField);
                    var benchmarkResultRef = new AtomicReference<VMBenchmarkResult>();
                    {
                        var accordion = new Accordion();
                        accordion.add("Benchmark local machine", new VerticalLayout(
                                makeBenchmarkButton(benchmarkResultRef, "Benchmark localmachine", vmBenchmarker::benchmarkLocalMachine)
                        ));

                        accordion.add("Benchmark remote machine via SSH", new VerticalLayout() {{
                            var userField = new TextField("Username");
                            var hostField = new TextField("Host");
                            var portField = new IntegerField("Port") {{
                                setValue(22);
                            }};
                            var passwordField = new PasswordField("Password");

                            add(userField, hostField, portField, passwordField);

                            var button = makeBenchmarkButton(benchmarkResultRef, "Benchmark remote machine", () -> {
                                var username = userField.getValue();
                                var password = passwordField.getValue();
                                var host = hostField.getValue();
                                var port = portField.getValue();

                                return vmBenchmarker.benchmarkSSH(host, username, password, port);
                            });

                            add(button);
                        }});

                        dialogLayout.add(new Details("Benchmarking", accordion));
                    }

                    var pricingPolicyRef = new AtomicReference<PricingPolicy>();
                    {
                        var pricingPolicyAccordion = new Accordion();

                        pricingPolicyAccordion.add("Reserved pricing policy", new VerticalLayout() {{
                            add(new Span("Reserved pricing policy:"));
                            var rangePicker = new Button("Enter duration");

                            var durationPicker = new DurationPicker();
                            durationPicker.setTarget(rangePicker);

                            var moneyField = new IntegerField();
                            moneyField.setPlaceholder("Bill to pay");
                            moneyField.setPrefixComponent(new Div("$"));

                            var button = new Button("Set", _ -> {
                                var duration = durationPicker.getValue();
                                pricingPolicyRef.set(new ReservedPricingPolicy(duration.getSeconds(), Money.usd(moneyField.getValue())));
                                Utils.showNotification("Reserved pricing policy set");
                            });

                            add(rangePicker, moneyField, button, durationPicker);
                        }});

                        dialogLayout.add(new Details("Choose pricing policy", pricingPolicyAccordion));
                    }

                    this.add(dialogLayout);
                    getFooter().add(new DialogCloseButton(this));

                    getFooter().
                            add(new Button("Save", _ -> {
                                var name = nameField.getValue();
                                var benchmarkResult = benchmarkResultRef.get();
                                var pricingPolicy = pricingPolicyRef.get();
                                if (StringUtils.isBlank(name)) {
                                    Utils.showNotification("Please enter a name");
                                    return;
                                }
                                if (benchmarkResult == null) {
                                    Utils.showNotification("Please enter a benchmark instrPerMs");
                                    return;
                                }
                                if (pricingPolicy == null) {
                                    Utils.showNotification("Please enter a pricing policy");
                                    return;
                                }
                                vmConfigRepo.save(new VMConfig(
                                        nameField.getValue(),
                                        benchmarkResultRef.get(),
                                        pricingPolicyRef.get()
                                ));
                                vmConfigGrid.setItems(vmConfigRepo.findAll());
                                Utils.showNotification("Save done");
                            }));
                }
            }.open()));

            layout.add(horizontalLayout);
        }

        vmConfigGrid.setSizeFull();
        vmConfigGrid.addColumn(VMConfig::name).setHeader("Name");
        vmConfigGrid.addColumn(vmConfig -> vmConfig.benchmarkResult().cpuName()).setHeader("CPU");
        vmConfigGrid.addColumn(vmConfig -> vmConfig.pricingPolicy().getClass().getSimpleName()).setHeader("Pricing policy");
        vmConfigGrid.addComponentColumn(vmConfig -> new Button(VaadinIcon.COG.create(), _ -> {
            openInfoDialog(vmConfig);
        })).setHeader("Info");
        vmConfigGrid.addComponentColumn(vmConfig -> new Button(VaadinIcon.CLOSE_CIRCLE.create(), _ -> {
            new DeleteConfirmDialog(() -> {
                vmConfigRepo.delete(vmConfig);
                vmConfigGrid.setItems(vmConfigRepo.findAll());
            }).open();
        })).setHeader("Delete");

        vmConfigGrid.setItems(vmConfigRepo.findAll());

        layout.add(vmConfigGrid);

        return layout;
    }

    private void openInfoDialog(VMConfig vmConfig) {
        new Dialog("Info " + vmConfig.name()) {{
            var layout = new VerticalLayout();
            var benchmarkResult = vmConfig.benchmarkResult();
            layout.add(new Span("CPU: " + benchmarkResult.cpuName()));

            record CoreInfo(
                    int core,
                    double freq
            ) {
            }

            var coresInfo = new CoreInfo[benchmarkResult.coreCount()];

            for (int core = 0; core < benchmarkResult.coreCount(); core++)
                coresInfo[core] = new CoreInfo(
                        core + 1,
                        benchmarkResult.efficientFreqKhz()[core]
                );

            var coresGrid = new Grid<>(CoreInfo.class, false);
            coresGrid.addColumn(CoreInfo::core).setHeader("Core");
            coresGrid.addColumn(coreInfo -> MessageFormat.format("{0,number,#.##GHz}", coreInfo.freq / 1e+6)).setHeader("Freq");
            coresGrid.setItems(coresInfo);
            layout.add(coresGrid);

            var maxCoreFreq = Arrays.stream(benchmarkResult.efficientFreqKhz()).max().getAsDouble();

            var benchmarksGrid = new Grid<>(BenchmarkAppResult.class, false);
            benchmarksGrid.addColumn(b -> b.app().executableName()).setHeader("Benchmark");
            benchmarksGrid.addColumn(b -> b.app().type()).setHeader("Type");
            benchmarksGrid.addColumn(b -> String.format("%.3f", b.instrPerMs())).setHeader("Instr Per ms");
            benchmarksGrid.addColumn(b -> String.format("%.5f", b.instrPerMs() / maxCoreFreq)).setHeader("IPC");

            benchmarksGrid.setItems(vmConfig.benchmarkResult().benchmarkResults());
            layout.add(benchmarksGrid);

            this.setWidth(layout.getWidth());
            add(layout);
            getFooter().add(new DialogCloseButton(this));
        }}.open();
    }
}
