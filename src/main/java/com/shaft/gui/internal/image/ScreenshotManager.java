package com.shaft.gui.internal.image;

import com.epam.healenium.SelfHealingDriver;
import com.shaft.driver.SHAFT;
import com.shaft.driver.internal.DriverFactory.DriverFactoryHelper;
import com.shaft.enums.internal.Screenshots;
import com.shaft.gui.browser.internal.JavaScriptWaitManager;
import com.shaft.gui.element.internal.ElementActionsHelper;
import com.shaft.gui.element.internal.ElementInformation;
import com.shaft.tools.io.ReportManager;
import com.shaft.tools.io.internal.ReportManagerHelper;
import lombok.SneakyThrows;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.*;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.sikuli.script.App;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScreenshotManager {

    public static List<Object> takeScreenshotUsingSikuliX(Screen screen, App applicationWindow, Pattern element, String actionName,
                                                          boolean passFailStatus) {
        com.shaft.gui.internal.image.ScreenshotManager.globalPassFailStatus = passFailStatus;
        if (passFailStatus) {
            com.shaft.gui.internal.image.ScreenshotManager.globalPassFailAppendedText = "passed";
        } else {
            com.shaft.gui.internal.image.ScreenshotManager.globalPassFailAppendedText = "failed";
        }
        boolean takeScreenshot = "Always".equals(SHAFT.Properties.visuals.screenshotParamsWhenToTakeAScreenshot())
                || ("ValidationPointsOnly".equals(SHAFT.Properties.visuals.screenshotParamsWhenToTakeAScreenshot())
                && (actionName.toLowerCase().contains("assert")
                || actionName.toLowerCase().contains("verify")))
                || !passFailStatus;
        if (takeScreenshot || (SHAFT.Properties.visuals.createAnimatedGif() && (AnimatedGifManager.DETAILED_GIF || actionName.matches(AnimatedGifManager.DETAILED_GIF_REGEX)))) {
            /*
             * Take the screenshot and store it as a file
             */
            byte[] src = null;
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                switch (Screenshots.getType()) {
                    case ELEMENT:
                        if (element != null) {
                            try {
                                ImageIO.write(screen.capture(screen.wait(element).getRect()).getImage(), "png", byteArrayOutputStream);
                                src = byteArrayOutputStream.toByteArray();
                                break;
                            } catch (FindFailed e) {
                                //do nothing and fall into the next type of screenshot
                            }
                        }
                    case VIEWPORT:
                        if (applicationWindow != null) {
                            ImageIO.write(screen.capture(applicationWindow.waitForWindow()).getImage(), "png", byteArrayOutputStream);
                            src = byteArrayOutputStream.toByteArray();
                            break;
                        }
                    case FULL:
                        ImageIO.write(screen.capture().getImage(), "png", byteArrayOutputStream);
                        src = byteArrayOutputStream.toByteArray();
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                ReportManager.logDiscrete("Failed to create attachment.");
                ReportManagerHelper.logDiscrete(e);
            }

            AnimatedGifManager.startOrAppendToAnimatedGif(src);
            if (takeScreenshot) {
                return com.shaft.gui.internal.image.ScreenshotManager.prepareImageForReport(src, actionName);
            } else {
                return null;
            }
        }
        return null;
    }
}