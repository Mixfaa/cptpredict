package com.mixfa.cptpredict.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

public class BasicAppLayout extends AppLayout {
    public BasicAppLayout() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("Compute time predict app");
        title.getStyle().set("font-size", "1.125rem").set("margin", "0");

        SideNav nav = getSideNav();
        nav.getStyle().set("margin", "var(--vaadin-gap-s)");

        Scroller scroller = new Scroller(nav);

        addToDrawer(scroller);
        addToNavbar(toggle, title);

    }

    private SideNav getSideNav() {
        return new SideNav() {{
            addItem(
                    new SideNavItem("Manage VM configurations", "/vmconfigs", VaadinIcon.OPTION.create()),
                    new SideNavItem("Manage programs", "/programs", VaadinIcon.CALC.create()),
                    new SideNavItem("Make prediction", "/predict", VaadinIcon.FLASK.create()),
                    new SideNavItem("Settings", "/settings", VaadinIcon.COGS.create())
            );
        }};
    }
}
