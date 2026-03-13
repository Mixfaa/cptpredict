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
}
