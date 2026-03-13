package com.mixfa.cptpredict.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class DurationPicker extends Popover {
    private long millis = 0;

    public DurationPicker() {
        add(makeContent());
    }

    private Component makeAdd(long addingMillis, String text, Span displaySpan) {
        return new VerticalLayout() {{
            add(new Span(text), new Button(VaadinIcon.PLUS.create(), _ -> {
                millis += addingMillis;
                displaySpan.setText(DurationFormatUtils.formatDurationWords(millis, true, true));
            }), new Button(VaadinIcon.MINUS.create(), _ -> {
                millis -= addingMillis;
                if (millis <= 0) millis = 0;
                displaySpan.setText(DurationFormatUtils.formatDurationWords(millis, true, true));
            }));
        }};
    }

    private Component makeContent() {
        var layout = new VerticalLayout();
        var displaySpan = new Span("");

        var horizontalLayout = new HorizontalLayout();

        horizontalLayout.add(
                makeAdd(TimeUnit.DAYS.toMillis(1), "Days", displaySpan),
                makeAdd(TimeUnit.HOURS.toMillis(1), "Hours", displaySpan),
                makeAdd(TimeUnit.MINUTES.toMillis(1), "Minutes", displaySpan)
        );

        layout.add(new VerticalLayout(new Span("Duration: "), displaySpan), horizontalLayout);
        return layout;
    }

    public Duration getValue() {
        return Duration.ofMillis(millis);
    }
}
