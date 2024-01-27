package com.shaft.driver.internal;

import com.shaft.gui.element.SikuliActions;
import org.sikuli.script.App;

public class FluentWebDriverAction {
    public SikuliActions performSikuliAction() {
        return new SikuliActions();
    }
    public SikuliActions performSikuliAction(App applicationWindow) {
        return new SikuliActions(applicationWindow);
    }
    public SikuliActions sikulix() {
        return new SikuliActions();
    }
    public SikuliActions sikulix(App applicationWindow) {
        return new SikuliActions(applicationWindow);
    }
}