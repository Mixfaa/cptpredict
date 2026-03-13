package com.mixfa.cptpredict.ui.components;

import com.mixfa.cptpredict.model.VMConfig;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public class VmConfigCompRenderer extends ComponentRenderer<Component, VMConfig> {
    private static final VmConfigCompRenderer INSTANCE = new VmConfigCompRenderer();

    public static VmConfigCompRenderer getInstance() {
        return INSTANCE;
    }

    private VmConfigCompRenderer() {
        super(vmConfig -> new VerticalLayout() {{
            add(
                    new Span("VM Name: " + vmConfig.name()),
                    new Span("CPU:" + vmConfig.benchmarkResult().cpuName()),
                    new Span("Pricing policy: " + vmConfig.pricingPolicy().toString())
            );
        }});
    }
}
