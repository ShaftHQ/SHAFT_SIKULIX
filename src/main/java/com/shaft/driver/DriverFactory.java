package com.shaft.driver;

import com.shaft.tools.io.ReportManager;
import lombok.Setter;
import org.sikuli.script.App;

@Setter
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class DriverFactory {
    /**
     * Attaches your SikuliActions to a specific Application instance
     *
     * @param applicationName the name or partial name of the currently opened application window that you want to attach to
     * @return a sikuli App instance that can be used to perform SikuliActions
     */
    public static App getSikuliApp(String applicationName) {
//        DriverFactoryHelper.initializeSystemProperties();
        var myapp = new App(applicationName);
        myapp.waitForWindow(SHAFT.Properties.timeouts.browserNavigationTimeout());
        myapp.focus();
        ReportManager.log("Opened app: [" + myapp.getName() + "]...");
        return myapp;
    }

    /**
     * Terminates the desired sikuli app instance
     *
     * @param application a sikuli App instance that can be used to perform SikuliActions
     */
    public static void closeSikuliApp(App application) {
        ReportManager.log("Closing app: [" + application.getName() + "]...");
        application.close();
    }
}