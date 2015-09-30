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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NeedsFreshDriver;
import org.openqa.selenium.NoDriverAfterTest;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.testing.JUnit4TestBase;

import java.util.Set;

public class WindowHandlerIntegrationTest extends JUnit4TestBase {

  @NoDriverAfterTest
  @NeedsFreshDriver
  @Test
  public void testHappyPath() throws Exception {
    driver.get(pages.xhtmlTestPage);

    WindowHandler sut = new WindowHandler(driver);

    driver.findElement(By.name("windowOne")).click();

    sut.switchToNewWindow();
    assertThat(driver.getTitle(), is("We Arrive Here"));

    sut.switchBack();
    assertThat(driver.getTitle(), is("XHTML Test Page"));

    sut.switchToNewWindow();
    assertThat(driver.getTitle(), is("We Arrive Here"));

    sut.close();
    assertThat(driver.getTitle(), is("XHTML Test Page"));
  }

  @NeedsFreshDriver
  @NoDriverAfterTest
  @Test
  public void testWindowHandler() throws Exception {
    driver.get(appServer.whereIs("window_switching_tests/page_with_frame.html"));

    WindowHandler sut = new WindowHandler(driver);

    driver.findElement(By.id("a-link-that-opens-a-new-window")).click();

    sut.switchToNewWindow();
    assertThat(driver.getTitle(), containsString("Simple Page"));
    String theNewWindow = driver.getWindowHandle();

    sut.switchBack();

    sut.close();
    assertThat(driver.getTitle(), containsString("WindowSwitchingTest"));
    assertThat(driver.getWindowHandles(), not(hasItem(theNewWindow)));
  }

  @NeedsFreshDriver
  @NoDriverAfterTest
  @Test(expected = IllegalWindowsStateException.class)
  public void testWindowHandlerGivenTwoNewWindows() {
    driver.get(appServer.whereIs("window_switching_tests/page_with_frame.html"));

    WindowHandler sut = new WindowHandler(driver);

    WebElement link = driver.findElement(By.id("a-link-that-opens-a-blank-window"));
    Set<String> initialWindowHandles = driver.getWindowHandles();
    link.click();
    link.click();
//    wait.until(newWindowsAreOpened(2, initialWindowHandles));

    // when
    sut.switchToNewWindow();

    // then exception is thrown
  }

  @NeedsFreshDriver
  @NoDriverAfterTest
  @Test
  public void whenNoSwitchCloseShouldDoNothing() throws Exception {
    driver.get(appServer.whereIs("window_switching_tests/page_with_frame.html"));

    WindowHandler sut = new WindowHandler(driver);

    // when
    sut.close();

    // then nop
  }
}
