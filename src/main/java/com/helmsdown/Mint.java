package com.helmsdown;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Random;
import java.util.Scanner;

/**
 * @author rbolles on 8/10/17.
 */
public class Mint implements AutoCloseable {

    private static final String MINT_LOGIN_URL = "https://mint.intuit.com/login.event";
    private static final String USER_NAME_FIELD_ID = "ius-userid";
    private static final String PASSWORD_FIELD_ID = "ius-password";
    private static final String LOGIN_BUTTON_ID = "ius-sign-in-submit-btn";
    private static final String SUBMIT_AUTHENTICATOR_BUTTON_ID1 = "ius-mfa-soft-token-submit-btn";
    private static final String SUBMIT_AUTHENTICATOR_BUTTON_ID2 = "ius-mfa-otp-submit-btn";
    private static final String AUTHENTICATOR_FIELD_ID1 = "ius-mfa-soft-token";
    private static final String AUTHENTICATOR_FIELD_ID2 = "ius-mfa-confirm-code";
    private static final String OVERVIEW_PAGE_ID = "overview";
    private static final String TRANSACTIONS_PAGE_ID = "transaction";
    private static final String ACCOUNTS_TOTAL_ALL_TEXT_ID = "account-table-all-bank";
    private static final String ADD_TXN_BUTTON = "controls-add";
    private static final String TXN_SUBMIT_BUTTON_ID = "txnEdit-submit";
    private static final String TXN_EDIT_DATE_INPUT_ID = "txnEdit-date-input";
    private static final String TXN_DESC_INPUT_ID = "txnEdit-merchant_input";
    private static final String TXN_CATEGORY_INPUT_ID = "txnEdit-category_input";
    private static final String TXN_CATEGORY = "Uncategorized";
    private static final String TXN_AMOUNT_INPUT_ID = "txnEdit-amount_input";
    private static final String CUSTOM_TAG_NAME = "Amazon (Per-Item)";
    private static final String CACHE_SPLIT_CHECKBOX_ID = "txnEdit-mt-cash-split";
    public static final String TXN_NOTE_INPUT_ID = "txnEdit-note";

    private final WebDriver driver;
    private final Scanner scanner;
    private final Random random;

    public Mint() {
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setCapability("marionette", true);
        driver = new FirefoxDriver(capabilities);
        driver.manage().window().setPosition(new Point(0, 0));
        driver.manage().window().maximize();
        scanner = new Scanner(System.in);
        random = new Random();
    }

    public void login(String username, String password) {
        driver.get(MINT_LOGIN_URL);

        waitUntilElementIsDisplayed(USER_NAME_FIELD_ID);

        //attempting to see if sleeping a random amount while logging in can affect whether we get text msg code vs normal authenticator
        sleepRandomAmount(5);

        WebElement usernameField = driver.findElement(By.id(USER_NAME_FIELD_ID));
        usernameField.sendKeys(username);

        sleepRandomAmount(5);

        WebElement passwordField = driver.findElement(By.id(PASSWORD_FIELD_ID));
        passwordField.sendKeys(password);

        WebElement loginButton = driver.findElement(By.id(LOGIN_BUTTON_ID));
        loginButton.click();

        WebElement twoFactorElement;
        boolean appEnabledTwoFactor = true;
        /*
            mint can make you authenticate two ways. I suspect there is some sort of logic that tries to detect bots
            and shifts you over to the text message codes.
         */
        try {
            //the first way is via authenticator code
            waitUntilElementIsDisplayed(AUTHENTICATOR_FIELD_ID1, 6);
            twoFactorElement = driver.findElement(By.id(AUTHENTICATOR_FIELD_ID1));
            System.out.println("Enter your authenticator code: ");
        } catch(TimeoutException e) {
            //the second is text message code
            appEnabledTwoFactor = false;
            waitUntilElementIsDisplayed(AUTHENTICATOR_FIELD_ID2);
            twoFactorElement = driver.findElement(By.id(AUTHENTICATOR_FIELD_ID2));
            System.out.println("Enter your text message code: ");
        }

        String twoFactorCode = scanner.next();
        twoFactorElement.sendKeys(twoFactorCode);

        WebElement submit2faButton;
        if(appEnabledTwoFactor) {
            submit2faButton = driver.findElement(By.id(SUBMIT_AUTHENTICATOR_BUTTON_ID1));
        } else {
            submit2faButton = driver.findElement(By.id(SUBMIT_AUTHENTICATOR_BUTTON_ID2));
        }
        submit2faButton.click();

        waitUntilElementIsDisplayed(OVERVIEW_PAGE_ID);

        System.out.println("Logged in");
    }

    private void sleepRandomAmount(int maxSeconds) {
        int numberOfSecondsToWait = random.nextInt(maxSeconds) + 2;

        try {
            System.out.println("Sleeping " + numberOfSecondsToWait + " seconds");
            Thread.sleep(numberOfSecondsToWait * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addTransactions(Collection<AmazonPurchase> amazonPurchases) throws InterruptedException {
        goToTransactionsPage();

        for (AmazonPurchase amazonPurchase : amazonPurchases) {

            if(amazonPurchase.getItemTotal().equals("0.00")) {
                System.out.println("Skipping zero cost purchase");
                continue;
            }

            addTransactionIfNotAlreadyPresent(amazonPurchase);
            Thread.sleep(2500);
        }
    }


    private void addTransactionIfNotAlreadyPresent(AmazonPurchase amazonPurchase) throws InterruptedException {
        boolean transactionExists = transactionExists(amazonPurchase);

        if(!transactionExists) {
            clearSearchFilter();
            Thread.sleep(2500);
            addTransaction(amazonPurchase);
        } else {
            System.out.println("Skipping transaction");
            clearSearchFilter();
        }

    }

    private void clearSearchFilter() {
        WebElement clearButton = driver.findElement(By.xpath("//*[@id=\"search-clear\"]/a"));
        clearButton.click();

        waitForClearTxnPage();
    }

    private boolean transactionExists(AmazonPurchase amazonPurchase) {
        boolean transactionExists = false;

        System.out.println("Checking to see if transaction exists: " + amazonPurchase);

        searchAndWait("tag: " + CUSTOM_TAG_NAME, "search-tag");
        searchAndWait("notes: " + amazonPurchase.getFormattedUniqueIdentifier(), "search-notes");

        try {
            waitUntilElementIsDisplayed("tip_no_search", 3);
        } catch(TimeoutException e) {
            transactionExists = true;
        }

        return transactionExists;
    }

    private void searchAndWait(String searchValue, String filterTag) {
        WebElement searchInput = driver.findElement(By.id("search-input"));
        searchInput.sendKeys(searchValue);

        WebElement searchForm = driver.findElement(By.id("search-form"));
        searchForm.submit();

        waitUntilElementIsDisplayed(filterTag, 9);
    }

    private void addTransaction(AmazonPurchase amazonPurchase) throws InterruptedException {
        System.out.println("Adding amazonPurchase: " + amazonPurchase);

        WebElement addTxButton = driver.findElement(By.id(ADD_TXN_BUTTON));
        addTxButton.click();

        waitUntilElementIsDisplayed(TXN_SUBMIT_BUTTON_ID);
        waitUntilElementIsDisplayed("txnEdit-mt-type-select");

        WebElement dateInput = driver.findElement(By.id(TXN_EDIT_DATE_INPUT_ID));
        dateInput.clear();
        String mintFormattedDate = amazonPurchase.getDate().format(DateTimeFormatter.ofPattern("M/d/u"));
        dateInput.sendKeys(mintFormattedDate);

        WebElement descriptionInput = driver.findElement(By.id(TXN_DESC_INPUT_ID));
        descriptionInput.clear();
        descriptionInput.sendKeys(CUSTOM_TAG_NAME);

        WebElement categoryInput = driver.findElement(By.id(TXN_CATEGORY_INPUT_ID));
        categoryInput.clear();
        categoryInput.sendKeys(TXN_CATEGORY);

        WebElement amountInput = driver.findElement(By.id(TXN_AMOUNT_INPUT_ID));
        String amount = amazonPurchase.getItemTotal();
        amountInput.clear();
        amountInput.sendKeys(amount);

        WebElement atmWithdrawalCheckBox = driver.findElement(By.id(CACHE_SPLIT_CHECKBOX_ID));
        if (atmWithdrawalCheckBox.isSelected()) {
            atmWithdrawalCheckBox.click();
        }

        String xpathExpression = String.format("//*[@id=\"txnEdit-tags-list\"]/li/label[@title=\"%s\"]", CUSTOM_TAG_NAME);
        WebElement tagsCheckBox = driver.findElement(By.xpath(xpathExpression));
        tagsCheckBox.click();

        WebElement notesInput = driver.findElement(By.id(TXN_NOTE_INPUT_ID));
        notesInput.sendKeys(amazonPurchase.getTitle());
        notesInput.sendKeys(Keys.ENTER);
        notesInput.sendKeys(amazonPurchase.getFormattedUniqueIdentifier());
        notesInput.sendKeys(Keys.ENTER);
        notesInput.sendKeys(amazonPurchase.getBuyerName());

        WebElement submitButton = driver.findElement(By.id(TXN_SUBMIT_BUTTON_ID));
        submitButton.click();
    }

    private void goToTransactionsPage() {
        WebElement transactionsButton = driver.findElement(By.id(TRANSACTIONS_PAGE_ID));
        transactionsButton.click();

        waitForClearTxnPage();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitForClearTxnPage() {
        waitUntilElementIsDisplayed(ACCOUNTS_TOTAL_ALL_TEXT_ID);
        waitUntilElementIsDisplayed(ADD_TXN_BUTTON);
    }

    private void waitUntilElementIsDisplayed(String fieldId) {
        waitUntilElementIsDisplayed(fieldId, 15);
    }

    private void waitUntilElementIsDisplayed(String fieldId, int waitInSeconds) {
        (new WebDriverWait(driver, waitInSeconds)).until((ExpectedCondition<Boolean>) d -> d.findElement(By.id(fieldId)).isDisplayed());
    }


    @Override
    public void close() throws Exception {
        driver.close();
    }

}
