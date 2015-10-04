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

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.inOrder;
import static org.mockito.BDDMockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;

import java.util.Collections;
import java.util.Set;

@RunWith(JUnit4.class)
public class WindowHandlerTest {

  @Mock private WebDriver mockDriver;
  @Mock private WebDriver.TargetLocator mockTargetLocator;
  private WindowHandlerSubclass sut;

  @Before
  public void createMocks() {
    MockitoAnnotations.initMocks(this);
    sut = new WindowHandlerSubclass(mockDriver);
    given(mockDriver.switchTo()).willReturn(mockTargetLocator);
  }

  @Test
  public void closeShouldDoNothingGivenNoSwitch() throws Exception {
    reset(mockDriver);
    // given no switch

    // when
    sut.close();

    // then should do nothing
    verifyNoMoreInteractions(mockDriver);
  }

  @Test
  public void whenCloseGivenTheNewWindowIsOpenButNotActive() throws Exception {
    String newWindow = "newWindow";
    String otherWindow = "otherWindow";
    given(mockDriver.getWindowHandle()).willReturn(otherWindow);
    given(mockDriver.getWindowHandles()).willReturn(newHashSet(newWindow, otherWindow));
    sut.setTheNewWindow(newWindow);

    // when
    sut.close();

    InOrder inOrder = inOrder(mockDriver, mockTargetLocator);
    inOrder.verify(mockTargetLocator).window(newWindow);
    inOrder.verify(mockDriver).close();
    inOrder.verify(mockTargetLocator).window(otherWindow);
  }

  @Test
  public void whenCloseGivenTheNewWindowIsOpenAndActive() throws Exception {
    String originalWindow = "originalWindow";
    given(mockDriver.getWindowHandle()).willReturn(originalWindow);
    given(mockDriver.getWindowHandles()).willReturn(Collections.singleton(originalWindow));
    sut = new WindowHandlerSubclass(mockDriver);

    String newWindow = "newWindow";
    given(mockDriver.getWindowHandle()).willReturn(newWindow);
    given(mockDriver.getWindowHandles()).willReturn(newHashSet(newWindow, originalWindow));
    sut.setTheNewWindow(newWindow);

    // when
    sut.close();

    InOrder inOrder = inOrder(mockDriver, mockTargetLocator);
    inOrder.verify(mockDriver).close();
    inOrder.verify(mockTargetLocator).window(originalWindow);
  }

  @Test
  public void whenCloseGivenTheNewWindowIsClosedAlready() throws Exception {
    String originalWindow = "originalWindow";
    String newWindow = "newWindow";
    given(mockDriver.getWindowHandle()).willReturn(originalWindow);
    given(mockDriver.getWindowHandles()).willReturn(newHashSet(originalWindow));
    sut.setTheNewWindow(newWindow);

    // when
    sut.close();

    verifyNoMoreInteractions(mockTargetLocator);
  }

  @Test(expected = IllegalWindowsStateException.class)
  public void switchToNewWindowShouldThrowWhenTwoNewWindowsAreOpen() {
    String originalWindow = "w0";
    given(mockDriver.getWindowHandle()).willReturn(originalWindow);
    given(mockDriver.getWindowHandles()).willReturn(newHashSet(originalWindow));
    sut = new WindowHandlerSubclass(mockDriver);
    given(mockDriver.getWindowHandles()).willReturn(newHashSet(originalWindow, "newWin1", "newWin2"));

    // when
    sut.switchToNewWindow();

    // then throw
  }

  @Test(expected = NoSuchWindowException.class)
  public void switchToNewWindowShouldThrowWhenNoNewWindowsAreOpen() {
    String initialWindow = "w0";
    Set<String> initialWindows = newHashSet(initialWindow);
    given(mockDriver.getWindowHandle()).willReturn(initialWindow);
    given(mockDriver.getWindowHandles()).willReturn(initialWindows);
    sut = new WindowHandlerSubclass(mockDriver);
    given(mockDriver.getWindowHandles()).willReturn(initialWindows);

    // when
    sut.switchToNewWindow();

    // then throw
  }

  @Test
  public void switchToNewWindowHappyPath() {
    String initialWindow = "w0";
    given(mockDriver.getWindowHandle()).willReturn(initialWindow);
    given(mockDriver.getWindowHandles()).willReturn(newHashSet(initialWindow));
    sut = new WindowHandlerSubclass(mockDriver);

    String newWin = "newWin";
    given(mockDriver.getWindowHandles()).willReturn(newHashSet(initialWindow, newWin));

    // when
    sut.switchToNewWindow();

    verify(mockTargetLocator).window(newWin);
  }

  @Test
  public void switchBack() {
    String originalWin = "originalWin";
    given(mockDriver.getWindowHandle()).willReturn(originalWin);
    sut = new WindowHandlerSubclass(mockDriver);
    sut.setTheNewWindow("newWin");

    // when
    sut.switchBack();

    verify(mockTargetLocator).window(originalWin);
  }

  @Test
  public void windowHandlerShouldBeAutoCloseable() {
    assertThat(sut, instanceOf(AutoCloseable.class));
  }

  private static class WindowHandlerSubclass extends WindowHandler {

    public WindowHandlerSubclass(WebDriver driver) {
      super(driver);
    }

    void setTheNewWindow(String theNewWindow) {
      this.theNewWindow = theNewWindow;
    }
  }
}

