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

import org.openqa.selenium.WebDriver;

import java.io.Closeable;
import java.util.Set;

/**
 * Manages a new window which opens up after interacting with the UI.
 * <p>
 * Example:
 * <pre>
 * try (WindowHandler windowHandler = new WindowHandler(driver)) {
 *     link.click(); // opens a new window
 *
 *     windowHandler.switchToNewWindow();
 *
 *     // I can now interact with elements on the new window
 *     buttonOnNewWindow.click();
 *     [..]
 * } // close the new window and switch back to the original one
 *
 * // I can now interact again with the elements on the old window
 * buttonOnOldWindow.click();
 * </pre>
 */
public class WindowHandler implements Closeable {

  private final WebDriver driver;
  private final Set<String> initialWindows;
  private final String initialActiveWindow;
  private String newWindow;

  public WindowHandler(WebDriver driver) {
    this.driver = driver;
    initialWindows = driver.getWindowHandles();
    initialActiveWindow = driver.getWindowHandle();
  }

  /**
   * Switch to the new window which has opened after instantiating this handler.
   *
   * @throws IllegalStateException if the number of new windows detected is not 1
   *
   * @see WebDriver.TargetLocator#window(String)
   */
  public WindowHandler switchToNewWindow() {
    Set<String> newWindows = driver.getWindowHandles();
    newWindows.removeAll(initialWindows);
    if (newWindows.size() > 1) {
      throw new IllegalStateException("Can't switch to a new window: " +
                                      newWindows.size() + " new windows have been opened");
    }
    if (newWindows.size() == 0) {
      throw new IllegalStateException("Can't switch to a new window: no new windows detected");
    }
    newWindow = newWindows.iterator().next();
    driver.switchTo().window(newWindow);
    return this;
  }

  /**
   * Closes the new window and switches back to the initial one,
   * i.e. the one which was active at the time that this object was instantiated.
   */
  @Override
  public void close() {
    if (driver.getWindowHandles().contains(newWindow)) {
      if (!driver.getWindowHandle().equals(newWindow)) {
        driver.switchTo().window(newWindow);
      }
      driver.close();
    }
    driver.switchTo().window(initialActiveWindow);
  }
}
