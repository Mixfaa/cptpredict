package com.mixfa.cptpredict.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;

public class DialogCloseButton extends Button {
    public DialogCloseButton(Dialog dialog) {
        super("Close");
        addClickListener(_ -> dialog.close());
    }
}
