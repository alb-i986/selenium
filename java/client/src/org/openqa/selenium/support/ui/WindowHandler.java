// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.support.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openqa.selenium.support.ui.ExpectedConditions.newWindowsToBeOpened;

import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;

import java.util.Set;

/**
 * A simple handler of windows, able to manage the life cycle of a single new window.
 * <p/>
 * On instantiation, record the windows currently open.
 * Then, on {@link #switchToNewWindow()}, expect to find exactly one new window open, and switch to it.
 * Optionally, it is possible to {@link #switchBack()} to the initial window, and then again to the
 * new window.
 * Finally, on {@link #close()} the new window is closed, and the driver is switched back to the
 * window which was active before closing (which may or may not be the initial window).
 * <p/>
 * As an {@link AutoCloseable}, it can be used in a
 * <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">
 * try-with-resources statement</a>. Example:
 *
 * <pre>{@code
 *   try (WindowHandler wh = new WindowHandler(driver)) {
 *      element.click(); // brings up a new window
 *      wh.switchToNewWindow();
 *      // do something on the new window
 *   } // finally, close the new window and switch back to the initial window
 * }</pre>
 */
public class WindowHandler implements AutoCloseable {

  private final WebDriver driver;
  private final Set<String> initialWindows;
  private final String initialWindow;
  private String theNewWindow;

  /**
   * Record the current state of the open windows.
   */
  public WindowHandler(WebDriver driver) {
    checkNotNull(driver);
    this.driver = driver;
    this.initialWindows = driver.getWindowHandles();
    this.initialWindow = driver.getWindowHandle();
  }

  /**
   * Switch to <i>the</i> new window that was opened.
   *
   * @see #switchToNewWindow(long)
   */
  public void switchToNewWindow() {
    switchToNewWindow(0);
  }

  /**
   * Wait until <i>the</i> new window is open and switch to it.
   *
   * @param timeOutInSeconds time to wait for the new window to open
   *
   * @throws NoSuchWindowException if no new window is present
   * @throws IllegalWindowsStateException if more than one new window is found since instantiation
   */
  public void switchToNewWindow(long timeOutInSeconds) {
    if (theNewWindow == null) {
      theNewWindow = findTheNewWindow(timeOutInSeconds);
    }
    driver.switchTo().window(theNewWindow);
  }

  private String findTheNewWindow(long timeOutInSeconds) {
    Set<String> newWindows = new WebDriverWait(driver, timeOutInSeconds)
      .until(newWindowsToBeOpened(initialWindows));
    if (newWindows.size() == 0) {
      throw new NoSuchWindowException("No new window present");
    } else if (newWindows.size() > 1) {
      throw new IllegalWindowsStateException(
        String.format("Cannot switch: too many new windows present (%d)", newWindows.size()));
    }
    return newWindows.iterator().next();
  }

  /**
   * Switch to the initial window.
   */
  public void switchBack() {
    driver.switchTo().window(initialWindow);
  }

  /**
   * Close the new window that was previously discovered.
   * If that is currently the active window, close it and then switch back to the initial window.
   * Otherwise, first switch to it, close it, and finally switch back to the window that was active
   * right before calling this method.
   */
  @Override
  public void close() throws Exception {
    if (theNewWindow == null || !isWindowOpen(theNewWindow)) {
      return; // never switched or already closed
    }

    if (isOnWindow(theNewWindow)) {
      driver.close();
      driver.switchTo().window(initialWindow);
    } else {
      String activeWindowBeforeClosing = driver.getWindowHandle();
      driver.switchTo().window(theNewWindow);
      driver.close();
      driver.switchTo().window(activeWindowBeforeClosing);
    }
  }

  private boolean isWindowOpen(String window) {
    Set<String> currentWindows = driver.getWindowHandles();
    return currentWindows.contains(window);
  }

  private boolean isOnWindow(String window) {
    return driver.getWindowHandle().equals(window);
  }

}
