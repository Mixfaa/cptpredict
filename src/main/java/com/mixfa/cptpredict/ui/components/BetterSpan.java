package com.mixfa.cptpredict.ui.components;

import com.vaadin.flow.component.html.Span;

public class BetterSpan extends Span {
    public BetterSpan() {
        super();
        applyStyle(this);
    }

    public BetterSpan(String text) {
        super(text);
        applyStyle(this);
    }

    public static void applyStyle(Span span) {
        span.getStyle().set("white-space", "pre-line");
    }
}
