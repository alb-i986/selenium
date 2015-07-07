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

package org.openqa.selenium.support.pagefactory;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByLinkText;
import org.openqa.selenium.internal.FindsByName;
import org.openqa.selenium.internal.FindsByXPath;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(JUnit4.class)
public class ByChainedTest {

  private static final List<WebElement> NO_ELEMENTS = Collections.emptyList();

  @Test
  public void findElementZeroBy() {
    final AllDriver driver = mock(AllDriver.class);

    ByChained by = new ByChained();
    try {
      by.findElement(driver);
      fail("Expected NoSuchElementException!");
    } catch (NoSuchElementException e) {
      // Expected
    }
  }

  @Test
  public void findElementsZeroBy() {
    final AllDriver driver = mock(AllDriver.class);

    ByChained by = new ByChained();
    assertThat(by.findElements(driver), is(empty()));
  }

  @Test
  public void findElementOneBy() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(WebElement.class, "webElement1");

    when(driver.findElementByName("cheese")).thenReturn(elem1);

    ByChained by = new ByChained(By.name("cheese"));
    assertThat(by.findElement(driver), equalTo(elem1));
  }

  @Test
  public void findElementsOneBy() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(WebElement.class, "webElement1");
    final WebElement elem2 = mock(WebElement.class, "webElement2");

    when(driver.findElementsByName("cheese")).thenReturn(Arrays.asList(elem1, elem2));

    ByChained by = new ByChained(By.name("cheese"));
    assertThat(by.findElements(driver), contains(elem1, elem2));
  }

  @Test
  public void findElementOneByEmpty() {
    final AllDriver driver = mock(AllDriver.class);

    when(driver.findElementByName("cheese")).thenThrow(NoSuchElementException.class);

    ByChained by = new ByChained(By.name("cheese"));
    try {
      by.findElement(driver);
      fail("Expected NoSuchElementException!");
    } catch (NoSuchElementException e) {
      // Expected
    }
  }

  @Test
  public void findElementsOneByEmpty() {
    final AllDriver driver = mock(AllDriver.class);

    when(driver.findElementsByName("cheese")).thenReturn(NO_ELEMENTS);

    ByChained by = new ByChained(By.name("cheese"));
    assertThat(by.findElements(driver), is(empty()));
  }

  @Test
  public void findElementTwoBy() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(AllElement.class, "webElement1");
    final WebElement elem2 = mock(AllElement.class, "webElement2");

    when(driver.findElementByName("cheese")).thenReturn(elem1);
    when(elem1.findElement(By.name("photo"))).thenReturn(elem2);

    ByChained by = new ByChained(By.name("cheese"), By.name("photo"));
    assertThat(by.findElement(driver), equalTo(elem2));
  }

  @Test
  public void findElementTwoByEmptyParent() {
    final AllDriver driver = mock(AllDriver.class);

    when(driver.findElementByName("cheese")).thenThrow(NoSuchElementException.class);

    ByChained by = new ByChained(By.name("cheese"), By.name("photo"));
    try {
      by.findElement(driver);
      fail("Expected NoSuchElementException!");
    } catch (NoSuchElementException e) {
      // Expected
    }
  }

  @Test
  public void findElementsTwoByEmptyParent() {
    final AllDriver driver = mock(AllDriver.class);

    when(driver.findElementsByName("cheese")).thenReturn(NO_ELEMENTS);

    ByChained by = new ByChained(By.name("cheese"), By.name("photo"));
    assertThat(by.findElements(driver), is(empty()));
  }

  @Test
  public void findElementTwoByEmptyChild() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(WebElement.class, "webElement1");

    when(driver.findElementByName("cheese")).thenReturn(elem1);
    when(elem1.findElement(By.name("photo"))).thenThrow(NoSuchElementException.class);

    ByChained by = new ByChained(By.name("cheese"), By.name("photo"));

    try {
      by.findElement(driver);
      fail("Expected NoSuchElementException!");
    } catch (NoSuchElementException e) {
      // Expected
    }
  }

  @Test
  public void findElementsTwoByEmptyChild() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(WebElement.class, "webElement1");
    final WebElement elem2 = mock(AllElement.class, "webElement2");
    final WebElement elem3 = mock(AllElement.class, "webElement3");

    when(driver.findElementsByName("cheese")).thenReturn(Arrays.asList(elem1, elem2));
    when(elem1.findElements(By.name("photo"))).thenReturn(NO_ELEMENTS);
    when(elem2.findElements(By.name("photo"))).thenReturn(Arrays.asList(elem3));

    ByChained by = new ByChained(By.name("cheese"), By.name("photo"));
    assertThat(by.findElements(driver), contains(elem3));
  }

  @Test
  public void findElementsThreeBy_firstFindsOne_secondEmpty() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(WebElement.class, "webElement1");

    String by1Name = "by1";
    By by1 = By.name(by1Name);
    By by2 = By.name("by2");
    By by3 = By.name("by3");

    when(driver.findElementsByName(by1Name)).thenReturn(Arrays.asList(elem1));
    when(elem1.findElements(by2)).thenReturn(NO_ELEMENTS);

    ByChained by = new ByChained(by1, by2, by3);

    assertThat(by.findElements(driver), is(empty()));
    verify(elem1, never()).findElements(by3);
  }

  @Test
  public void findElementThreeBy_firstFindsTwo_secondEmpty() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(WebElement.class, "webElement1");
    final WebElement elem2 = mock(WebElement.class, "webElement2");

    String by1Name = "by1";
    By by1 = By.name(by1Name);
    By by2 = By.name("by2");
    By by3 = By.name("by3");

    when(driver.findElementsByName(by1Name)).thenReturn(Arrays.asList(elem1, elem2));
    when(elem1.findElements(by2)).thenReturn(NO_ELEMENTS);
    when(elem2.findElements(by2)).thenReturn(NO_ELEMENTS);

    ByChained by = new ByChained(by1, by2, by3);

    assertThat(by.findElements(driver), is(empty()));
    verify(elem1, never()).findElements(by3);
  }

  @Test
  public void testEquals() {
    assertThat(new ByChained(By.id("cheese"), By.name("photo")),
        equalTo(new ByChained(By.id("cheese"), By.name("photo"))));
  }

  private interface AllDriver extends
      FindsById, FindsByLinkText, FindsByName, FindsByXPath, SearchContext {
    // Place holder
  }

  private interface AllElement extends WebElement {
    // Place holder
  }
}
