package com.mixfa.cptpredict.ui.components;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

public class DeleteConfirmDialog extends ConfirmDialog {
    public DeleteConfirmDialog(Runnable onDelete) {
        super();
        setHeader("Confirm deletion");
        setText("Are you sure you want to delete this?");
        setCancelable(true);
        setCancelText("Cancel");
        setConfirmText("Delete");
        addConfirmListener(_ -> onDelete.run());
    }
}
