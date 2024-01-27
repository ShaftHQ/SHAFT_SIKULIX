package com.shaft.driver;

import com.shaft.gui.element.SikuliActions;
import org.sikuli.script.App;

@SuppressWarnings("unused")
public class SHAFT {
    public static class GUI {

        public static class SikuliDriver {
            private final App sikuliApp;

            public SikuliDriver(String applicationName) {
                sikuliApp = DriverFactory.getSikuliApp(applicationName);
            }

            public static SikuliDriver getInstance(String applicationName) {
                return new SikuliDriver(applicationName);
            }

            public void quit() {
                DriverFactory.closeSikuliApp(sikuliApp);
            }

            public SikuliActions element() {
                return new SikuliActions(sikuliApp);
            }

            public App getDriver(String applicationName) {
                return sikuliApp;
            }
        }
    }
}