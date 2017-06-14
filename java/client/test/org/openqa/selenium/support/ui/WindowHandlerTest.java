package org.openqa.selenium.support.ui;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.testing.JUnit4TestBase;
import org.openqa.selenium.testing.NeedsFreshDriver;

public class WindowHandlerTest extends JUnit4TestBase {

  @Test
  @NeedsFreshDriver
  public void oneNewWindowOpensUp() {
    driver.get(pages.xhtmlTestPage);

    try (WindowHandler windowHandler = new WindowHandler(driver)) {
      assertThat(driver.getTitle(), equalTo("XHTML Test Page"));

      // one new window opens up
      driver.findElement(By.linkText("Open new window")).click();

      // when:
      windowHandler.switchToNewWindow();

      // I can now interact with elements on the new window
      assertThat(driver.getTitle(), equalTo("We Arrive Here"));
    }

    // the old window should be back active
    assertThat(driver.getTitle(), equalTo("XHTML Test Page"));
  }

  @Test
  @NeedsFreshDriver
  public void zeroNewWindowsOpenUp() {
    driver.get(pages.xhtmlTestPage);

    try (WindowHandler windowHandler = new WindowHandler(driver)) {
      assertThat(driver.getTitle(), equalTo("XHTML Test Page"));

      try {
        windowHandler.switchToNewWindow();
        fail("exception was expected");
      } catch (IllegalStateException e) {
        // expected
      }
    }
  }

  @Test
  @NeedsFreshDriver
  public void twoNewWindowsOpenUp() {
    driver.get(pages.xhtmlTestPage);

    try (WindowHandler windowHandler = new WindowHandler(driver)) {
      assertThat(driver.getTitle(), equalTo("XHTML Test Page"));

      // two new windows open up
      driver.findElement(By.linkText("Open new window")).click();
      driver.findElement(By.linkText("Open new window")).click();

      try {
        windowHandler.switchToNewWindow();
        fail("exception was expected");
      } catch (IllegalStateException e) {
        // expected
      }
    }
    // the old window should be still active
    assertThat(driver.getTitle(), equalTo("XHTML Test Page"));
  }
}
