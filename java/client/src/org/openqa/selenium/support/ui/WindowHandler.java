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

import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;

import java.util.HashSet;
import java.util.Set;

/**
 * A simple handler of windows, able to manage the life cycle of a single new window.
 * <p>
 * On instantiation, it records the windows currently open.
 * Then, on {@link #switchToNewWindow()}, it expects to find exactly one new
 * window open, and switches to it.
 * Finally, on {@link #close()} the window previously discovered is closed, and the driver is
 * switched back to the initial window or to the window which was active before closing.
 * <p>
 * Example:
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
  private Set<String> initialWindows;
  private String initialWindow;
  private String theNewWindow;

  /**
   * Record the current state of open windows.
   */
  public WindowHandler(WebDriver driver) {
    checkNotNull(driver);
    this.driver = driver;
    this.initialWindows = driver.getWindowHandles();
    this.initialWindow = driver.getWindowHandle();
  }

  /**
   * Switch to <i>the</i> new window that opened up.
   *
   * @throws NoSuchWindowException if no new window is present
   * @throws IllegalWindowsStateException if more than one new window was opened since instantiation,
   *         in which case we wouldn't know where to switch to
   */
  public void switchToNewWindow() {
    Set<String> newWindows = newWindows();
    if (newWindows.size() == 0) {
      throw new NoSuchWindowException("No new window present");
    } else if (newWindows.size() > 1) {
      throw new IllegalWindowsStateException("Cannot switch: too many new windows present");
    }

    theNewWindow = newWindows.iterator().next();
    driver.switchTo().window(theNewWindow);
  }

  private Set<String> newWindows() {
    Set<String> newWindows = new HashSet<>(driver.getWindowHandles());
    newWindows.removeAll(initialWindows);
    return newWindows;
  }

  /**
   * Close the new window we previously discovered and switched to.
   * If it is currently the active window, close it and then switch back to the initial window.
   * Otherwise, first switch to it, close it, and finally switch back to the
   * window that was active right before this call.
   * <p/>
   * Warning: the operation is not atomic, therefore it might fail under particular circumstances.
   */
  @Override
  public void close() throws Exception {
    if (theNewWindow == null || !isWindowOpen(theNewWindow)) {
      return; // never opened or already closed
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
