package com.shaft.gui.element.internal;

import com.google.common.base.Throwables;
import com.shaft.cli.FileActions;
import com.shaft.driver.SHAFT;
import com.shaft.driver.internal.DriverFactory.DriverFactoryHelper;
import com.shaft.enums.internal.ClipboardAction;
import com.shaft.enums.internal.ElementAction;
import com.shaft.gui.browser.internal.BrowserActionsHelper;
import com.shaft.gui.element.ElementActions;
import com.shaft.gui.element.SikuliActions;
import com.shaft.gui.internal.exceptions.MultipleElementsFoundException;
import com.shaft.gui.internal.image.ImageProcessingActions;
import com.shaft.gui.internal.image.ScreenshotManager;
import com.shaft.gui.internal.locator.LocatorBuilder;
import com.shaft.gui.internal.locator.ShadowLocatorBuilder;
import com.shaft.tools.internal.support.JavaHelper;
import com.shaft.tools.internal.support.JavaScriptHelper;
import com.shaft.tools.io.ReportManager;
import com.shaft.tools.io.internal.FailureReporter;
import com.shaft.tools.io.internal.ReportHelper;
import com.shaft.tools.io.internal.ReportManagerHelper;
import com.shaft.validation.internal.ValidationsHelper;
import io.appium.java_client.AppiumDriver;
import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.jsoup.Jsoup;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.sikuli.script.App;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.testng.Assert;

import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ElementActionsHelper {
    public static final String OBFUSCATED_STRING = "•";
    public static final int ELEMENT_IDENTIFICATION_TIMEOUT_INTEGER = (int) SHAFT.Properties.timeouts.defaultElementIdentificationTimeout();
    private static final boolean GET_ELEMENT_HTML = true; //TODO: expose parameter
    private static final boolean FORCE_CHECK_FOR_ELEMENT_VISIBILITY = SHAFT.Properties.flags.forceCheckForElementVisibility();
    private static final int ELEMENT_IDENTIFICATION_POLLING_DELAY = 100; // milliseconds
    private static final String WHEN_TO_TAKE_PAGE_SOURCE_SNAPSHOT = SHAFT.Properties.visuals.whenToTakePageSourceSnapshot();

    public static void passAction(Screen screen, App applicationWindow, Pattern element, String testData) {
        String actionName = Thread.currentThread().getStackTrace()[2].getMethodName();
        List<List<Object>> attachments = new LinkedList<>();
        attachments.add(SikuliActions.prepareElementScreenshotAttachment(screen, applicationWindow, element, actionName, true));
        passAction(null, null, actionName, testData, attachments, null);
    }
    public static void passAction(WebDriver driver, By elementLocator, String actionName, String testData, List<List<Object>> screenshots, String elementName) {
        com.shaft.gui.element.internal.ElementActionsHelper.reportActionResult(driver, actionName, testData, elementLocator, screenshots, elementName, true);
    }

    public static void failAction(Screen screen, App applicationWindow, Pattern element, String testData, Throwable... rootCauseException) {
        String actionName = Thread.currentThread().getStackTrace()[2].getMethodName();
        List<List<Object>> attachments = new LinkedList<>();
        attachments.add(SikuliActions.prepareElementScreenshotAttachment(screen, applicationWindow, element, actionName, false));
        failAction(null, actionName, testData, null, attachments, rootCauseException);
    }

    public static void failAction(WebDriver driver, String actionName, String testData, By elementLocator, List<List<Object>> screenshots, Throwable... rootCauseException) {
        //TODO: merge all fail actions, make all methods call this one, get elementName where applicable instead of reporting null
        //this condition works if this is the first level of failure, but the first level is usually caught by the calling method

//        boolean skipPageScreenshot = rootCauseException.length >= 1 && (
//                TimeoutException.class.getName().equals(rootCauseException[0].getClass().getName()) //works to capture fluent wait failure
//                        || (
//                        rootCauseException[0].getMessage().contains("Identify unique element")
//                                && isFoundInStacktrace(ValidationsHelper.class, rootCauseException[0])
//                )//works to capture calling elementAction failure in case this is an assertion
//        );

        //don't take a second screenshot in case of validation failure  because the original element action will have always failed first
        boolean skipPageScreenshot = rootCauseException.length >= 1 && (com.shaft.gui.element.internal.ElementActionsHelper.isFoundInStacktrace(ValidationsHelper.class, rootCauseException[0]) && isFoundInStacktrace(ElementActionsHelper.class, rootCauseException[0]));

        String elementName = elementLocator != null ? com.shaft.gui.element.internal.ElementActionsHelper.formatLocatorToString(elementLocator) : "";
        if (elementLocator != null && (rootCauseException.length >= 1 && Throwables.getRootCause(rootCauseException[0]).getClass() != MultipleElementsFoundException.class && Throwables.getRootCause(rootCauseException[0]).getClass() != NoSuchElementException.class && Throwables.getRootCause(rootCauseException[0]).getClass() != InvalidSelectorException.class)) {
            try {
                var accessibleName = ((WebElement) com.shaft.gui.element.internal.ElementActionsHelper.identifyUniqueElement(driver, elementLocator).get(1)).getAccessibleName();
                if (accessibleName != null && !accessibleName.isBlank()) {
                    elementName = accessibleName;
                }
            } catch (WebDriverException e) {
                //happens on some elements that show unhandled inspector error
                //this exception is thrown on some older selenium grid instances, I saw it with firefox running over selenoid
                //ignore
            }
        }

        String message;
        if (skipPageScreenshot) {
            //don't try to take a screenshot again and set element locator to null in case element was not found by timeout or by nested element actions call
            message = com.shaft.gui.element.internal.ElementActionsHelper.createReportMessage(actionName, testData, elementName, false);
            ReportManager.logDiscrete(message);
        } else {
            if (rootCauseException.length >= 1) {
                message = com.shaft.gui.element.internal.ElementActionsHelper.reportActionResult(driver, actionName, testData, elementLocator, screenshots, elementName, false, rootCauseException[0]);
            } else {
                message = com.shaft.gui.element.internal.ElementActionsHelper.reportActionResult(driver, actionName, testData, null, screenshots, elementName, false);
            }
        }
        if (rootCauseException.length >= 1) {
            Assert.fail(message, rootCauseException[0]);
        } else {
            Assert.fail(message);
        }
    }
}