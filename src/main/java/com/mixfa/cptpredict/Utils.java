package com.mixfa.cptpredict;

import com.vaadin.flow.component.notification.Notification;

final public class Utils {
    private Utils() {
    }

    public static void showNotification(String msg) {
        Notification.show(msg);
    }

    public static void showErrorNotification(Throwable e) {
        Notification.show(e.getMessage());
    }

    public static double map(
            double min, double max, double rangeMin, double rangeMax, double value
    ) {
        return (value - min) * (rangeMax - rangeMin) / (max - min) + rangeMin;
    }
}
