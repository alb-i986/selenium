package org.openqa.selenium.support.ui;

import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * @author ascotto
 */
public interface ISelect {

  boolean isMultiple();

  List<WebElement> getOptions();

  List<WebElement> getAllSelectedOptions();

  WebElement getFirstSelectedOption();

  void selectByVisibleText(String text);

  void selectByIndex(int index);

  void selectByValue(String value);

  void deselectAll();

  void deselectByValue(String value);

  void deselectByIndex(int index);

  void deselectByVisibleText(String text);
}
