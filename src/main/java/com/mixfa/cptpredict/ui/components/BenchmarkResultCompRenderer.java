package com.mixfa.cptpredict.ui.components;

import com.mixfa.cptpredict.model.VMBenchmarkResult;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public class BenchmarkResultCompRenderer extends ComponentRenderer<Component, VMBenchmarkResult> {
    private static final BenchmarkResultCompRenderer INSTANCE = new BenchmarkResultCompRenderer();

    public static BenchmarkResultCompRenderer getInstance() {
        return INSTANCE;
    }

    private BenchmarkResultCompRenderer() {
        super(benchmarkResult -> new VerticalLayout() {{
            add(
                    new Span("CPU:" + benchmarkResult.cpuName()),
                    new Span("Cores: " + benchmarkResult.coreCount())
            );
        }});
    }
}
