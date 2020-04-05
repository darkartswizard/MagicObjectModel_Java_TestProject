//Version 4.0.0
// - Added is enabled.
// - Reestablished //button from WC.


import io.testproject.java.annotations.v2.Action;
import io.testproject.java.annotations.v2.Parameter;
import io.testproject.java.enums.ParameterDirection;
import io.testproject.java.sdk.v2.addons.helpers.WebAddonHelper;
import io.testproject.java.sdk.v2.drivers.WebDriver;
import io.testproject.java.sdk.v2.enums.ExecutionResult;
import io.testproject.java.sdk.v2.exceptions.FailureException;
import io.testproject.java.sdk.v2.reporters.ActionReporter;
import java.util.*;

import momNLProcess.Nlp;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.testng.asserts.SoftAssert;
import static momUtilities.Utils.print;

//import static momUtilities.Utils.*;
//import static momNLProcess.Nlp.*;


public class MomBase {

    final int HUNDREDTH_SECOND = 10;
    final int TENTH_SECOND = 100;
    final int THIRD_SECOND = 333;
    final int QUARTER_SECOND = 250;
    final int HALF_SECOND = 500;
    final int ONE_SECOND = 999;

    static WebDriver driver;

    @Parameter(description = "Type a plain English action to perfom\n Hover, Click (on the), Set, Clear, Select (from) Type, Verify\n 'named object/ list item', Esc\n Button, link, text, checkbox, radiobutton, list, tab\n exists, contains, does not exist\n 'some data'", direction = ParameterDirection.INPUT)
    private String sentence;
    //private final Logger LOGGER = LoggerFactory.getLogger(momAction.class);
    //private String sentence = "";

    public void setSentence(String sentence) {
        this.sentence = sentence;
        //runner.run(this);  //TODO
    }

    //Exists through the test
    public static final Framework fw = null;

    //Global variables
    private WebElement currentElement;
    private String currentElementText = "";
    private boolean alreadyFailed;
    // The prior button, link, field that was identified by getLink(), getButton(),
    // getField()
    // This is used for relative object to object position validation.
    // And I should see a 'Search' button
    // "And it is below the 'Search' field
    private boolean priorElementAvailable = false;

    //For object relative validation
    private WebElement priorElement;

    //For same named element navigation
    private WebElement priorClickHoverElement;


    private String lastElementName;
    private String lastElementType;


    private boolean found = false;
    private boolean find = false;  //Relative element location

    //Allow access from MOMAction wrapper in the event of an error
    protected String results;

    private int visibleElementIndex;
    //private int visibleImage = -1;

    private WebElement viewport;


    //Save off the last good DP to refresh of page updates unexpectedly
    private String dpElements;

    //Reuse when highlighting multiple matches
    private List<WebElement> elements = null;
    public static int viewportHeight;
    //private int viewportWidth;  //Future use for resizing if element is not displayed because browser is too small
    private boolean priorClickHoverElementAvailable;
    private boolean caseSensitiveMatch = false;
    private int cntElements;

    public String finalTestResults = "";

    //Natural Language Process Engine
    Nlp s = new Nlp();

    public MomBase() {
    }


    private String verb = "";
    private String elementName = "";
    private String elementType = "";
    //private String verify = "";
    //private String data = "";

    private String finalWord;

    private JavascriptExecutor js = null; //driver;
    private WebAddonHelper helper;

    //Global reporter to reduce code
    protected ActionReporter reporter;

    public static final int VERB = 0;
    public static final int ELEMENT_NAME = 1;
    public static final int ELEMENT_TYPE = 2;
    public static final int VALIDATION = 3;
    public static final int EXPECTED_RESULT = 4;


    private void parse(String sentence) {

        //Nothing to parse
        if (sentence == "") {
            debug(s.getVerb() + " '" + s.getElementName() + "' " + s.getElementType() + " " + s.getVerify() + " " + s.getData());
            return;
        }

        clearParsedText();

        String[] arrOfWords = sentence.split(" ");

        //Skip Feature:, Background: Scenario:

        if ((sentence.indexOf("Feature:") == 1) || (sentence.indexOf("Background:") == 1) || (sentence.indexOf("Scenario:") == 1)) {
            //Move Feature:, Background: or Scenario: to the verb or output
            s.setVerb(arrOfWords[0]);
            //Set the Element name to the description
            s.setElementName(s.getElementName().toLowerCase().replace("feature:", ""));
            s.setElementName(s.getElementName().toLowerCase().replace("background:", ""));
            s.setElementName(s.getElementName().toLowerCase().replace("scenario:", ""));
            //Nothing more to do
            return;
        }


/*        //Todo: Recover from a simulated code crash
        if (sentence.toLowerCase() == "crash"){
            //intention Div by zero
            framework.appendResults ("\n\n Throwing a div by zero...");

            try {
                int a = 1 / 0;
            }catch (Exception e) {
                //Eat the error
                return;
            }
            return;
        }*/

        //Avoid Null Pointer Exception if sentence is empty
        debug("\"" + sentence +"\"");

        boolean switchSelect = false;
        boolean switchElementnameWithData = false;
        boolean inQuotes = false;

        int locOfFirstQuote = -1;
        int locOfLastQuote = -1;

        int cnt = 0;
        String tmp;


        // Parse the individual words of the sentance
        // from Verb to ExpectedResult
        for (String word : arrOfWords) {

            locOfFirstQuote = word.indexOf("'");
            locOfLastQuote = word.lastIndexOf("'") + 1;

            //debug (word);


            switch (cnt) {

                case VERB:
                    //Verb: Open, Click, Select, Set, Clear, Enter, crash

                    s.setVerb(arrOfWords[cnt]);
                    //report("\nVerb        : " + s.setVerb);
                    //printParsedText();

                    //Move on to Element name
                    cnt++;
                    break;

                case ELEMENT_NAME:

                    //Element Name

                    if (!inQuotes) {
                        //Starts with single quote '


                        if (locOfFirstQuote == 0) {
                            //Adding first word

                            //One word surrounded in single quotes?
                            inQuotes = true;

                            if (locOfLastQuote != word.length()) {
                                //remove first quote
                                s.setElementName(word.substring(1, word.length()));


                            } else {
                                //Remove both quotes around single word
                                s.setElementName(word.substring(1, word.length() - 1));
                                inQuotes = false;

                                //printParsedText();
                                //move on to Element type
                                cnt++;
                            }

                        } else {

                            //No quotes a single word
                            if (s.getVerb().toLowerCase().contains("click") || s.getVerb().toLowerCase().contains("set") || s.getVerb().toLowerCase().contains("verify")) {

                                switch (word.toLowerCase()) {

                                    //Click on the
                                    //Click the
                                    //Verify the
                                    case "the":
                                    case "on":
                                        //report("  *** Removed '" + word + "' ***");
                                        //To do
                                        //clickSwitch = true;
                                        break;

                                    default:

                                        s.setElementName(word);
                                        //printParsedText();
                                        //Move on
                                        cnt++;
                                        break;

                                }

                            } else {
                                s.setElementName(word);
                                //printParsedText();
                                //Move on
                                cnt++;
                                break;
                            }


                        }


                    } else {

                        //Adding more words Inside single quotes

                        if (locOfLastQuote != word.length()) {

                            s.setElementName(s.getElementName() + " " + word);
                            //keep adding

                        } else {
                            s.setElementName(s.getElementName() + " " + word.substring(0, word.length() - 1));
                            inQuotes = false;
                            //printParsedText();
                            //Move on
                            cnt++;
                        }

                    }

                    break;

                case ELEMENT_TYPE:
                    //Element Type - not nessarily one word

                    switch (word.toLowerCase()) {

                        case "into":

                            //report("  *** Removed 'into' ***");

                            if (s.getVerb().toLowerCase().contains("enter")) {
                                //report ("Activated Select/From switch");
                                switchElementnameWithData = true;

                            }

                            break;

                        case "from":

                            //report("  *** Removed 'from' ***");

                            if (s.getVerb().toLowerCase().contains("select")) {
                                //report ("Activated Select/From switch");
                                switchSelect = true;
                            }

                            break;

                        // Select from the
                        // Click the
                        // Click on the named button
                        // !Click on named button
                        case "on":
                            debug("  *** Removed 'on' ***");
                            break;

                        case "the":
                            debug("  *** Removed 'the' ***");
                            break;

                        default:

                            //Set if "select" / "from" was not seen

                            if (!switchSelect && !switchElementnameWithData) {
                                //Get the class name
                                s.setElementType(word);
                                //printParsedText();

                                cnt++;
                                //Move on if not in quotes

                            } else {

                                //Getting the Element Name to be switched out later
                                if (!inQuotes) {

                                    //Starts with single quote '


                                    if (locOfFirstQuote == 0) {

                                        //One word surrounded in single quotes?

                                        if (locOfLastQuote != word.length()) {
                                            //remove initial quote
                                            s.setElementType(word.substring(1, word.length()));
                                            inQuotes = true;

                                        } else {
                                            //Remove both start and end quotes
                                            inQuotes = false;
                                            s.setElementType(word.substring(1, word.length() - 1));

                                            // printParsedText();
                                            //move on not in quotes
                                            cnt++;
                                        }

                                    } else {

                                        if (inQuotes) {
                                            if (locOfLastQuote != word.length()) {
                                                //Keep adding words to clssname to be switched out later
                                                s.setElementType(s.getElementType() + " " + word);
                                                //Do not move on

                                            } else {
                                                //Add the last word
                                                s.setElementType(s.getElementType() + " " + word.substring(0, word.length() - 1));
                                                inQuotes = false;
                                                //printParsedText();
                                                //Move on
                                                cnt++;
                                            }

                                        } else {
                                            //Just one word
                                            s.setElementType(word);

                                            //Move on
                                            //printParsedText();
                                            cnt++;
                                        }

                                    }

                                } else {

                                    //Adding words Inside single quotes
                                    int wordLength = word.length();

                                    if (locOfLastQuote != wordLength) {
                                        s.setElementType(s.getElementType() + " " + word);
                                        //keep adding

                                    } else {
                                        s.setElementType(s.getElementType() + " " + word.substring(0, word.length() - 1));
                                        //Move on
                                        inQuotes = false;
                                        //printParsedText();
                                        cnt++;
                                    }

                                }

                            }

                    }

                    break;

                case VALIDATION:
                    //Data or Verify type

                    //set    name field *to* 'Paul'
                    //verify name field does not contain Steve

                    if (s.getVerb().toLowerCase().contentEquals("verify")) {

                        switch (word.toLowerCase()) {

                            case "is":
                                if (word.contentEquals("is")) {
                                    word = word; //Debug
                                }
                                //Move on at end
                                break;

                            case "exists":
                            case "displayed":


                            case "greater":
                            case "or":
                            case "less":
                            case "than":

                            case "does":
                            case "not":
                            case "exist":
                            case "equal":
                            case "enabled":
                            case "disabled":
                                s.setVerify(s.getVerify() + " " + word);
                                s.setVerify(s.getVerify().trim());
                                break;

                            case "to":
                                //Discard "is equal to" > "is equal"
                                cnt++;
                                //Move on
                                break;

                            case "equals":
                            case "=":
                            case "<>":
                            case ">":
                            case "<":

                            case "contains":
                            case "contain":
                                s.setVerify(" " + word);
                                s.setVerify(s.getVerify().trim());
                                cnt++;
                                //move on
                                break;

                            default:
                                //Picked up data
                                s.setVerify(s.getVerify().trim());
                                cnt++;
                                //drop through to data
                        }

                    } else {
                        //drop through to data

                        cnt++;

                        if (word.toLowerCase().contentEquals("to")) {
                            break;
                        }

//                          "to" not caught
//                        switch (word.toLowerCase()) {
//                            //Set Email Field to qtpmgrossman
//                            case "to":
//                                break;
                    }


                    //Fix: Verify Disclaimer link "" exists
                    if (s.getVerb().toLowerCase().contentEquals("verify")) {
                        if (s.getVerify().contentEquals("")) {
                            swapStrings("Verify", "Data");
                        }
                        break;
                        //}else{
                        //s.setData(word);
                    }

                    //printParsedText();


                    // break;


                case EXPECTED_RESULT:

                    //Data - Quotes still matter
                    //       Enter 'data' into 'named' Field
                    //debug("Dropping through to data");


                    if (!inQuotes) {
                        //Starts with single quote '

                        if (locOfFirstQuote == 0) {
                            //Adding first word

                            //One word surrounded in single quotes?
                            inQuotes = true;

                            if (locOfLastQuote != word.length()) {
                                //remove first quote
                                s.setData(s.getData() + word.substring(1, word.length()));


                            } else {
                                //Remove both quotes around single word
                                s.setData(s.getData() + word.substring(1, word.length() - 1));
                                inQuotes = false;

                                //printParsedText();
                                //move on to Element type at end of string
                                cnt++;
                            }
                        } else {

                            //Not in quotes
                            s.setData(s.getData() + " " + word);


                        }


                    } else {
                        //Inside quoted string - Add or End?
                        if (locOfLastQuote != word.length()) {
                            //Keep adding words to clssname to be switched out later
                            s.setData(s.getData() + " " + word);
                            //Do not move on

                        } else {
                            //Add the last word and shave off the quote
                            s.setData(s.getData() + " " + word.substring(0, word.length() - 1));
                            inQuotes = false;
                            //printParsedText();

                            //Move on - We might still have a class
                            //Enter 'stuff' into 'Bogus' field
                            cnt++;

                        }


                    }

                    s.setData(s.getData().trim());

                    // printParsedText();


                    break;

                default:

                    //store element type in temp
                    if (s.getData().isEmpty()) {

                        s.setData(word);
                        swapStrings("ElementType", "Data");

                    } else {
                        finalWord = word;
                        debug("Final word: " + word);

                    }

                    //printParsedText();

            }



        }



        if (switchSelect) {

            //report("   *** Reformatting Select/from ***");

            //Switch to process Select item From named list
            //Verb     |    Element name        |     elementType      | verify      | data
            //Select,       item     ,    *from* *the* list name,   list,         ""
            //Select,       ""       ,    *from* *the* list name,   list,         item
            //Select,       ""       ,    *from* *the* list,        list name,    item
            //Select,       list name,                 list,        "",           item

            //Verify        Some                        field       contains        data

            //tmp = s.setElementName;
            //s.setElementName( s.setElementType;
            //s.setElementType = tmp;

            swapStrings("elementName", "elementType");

            //            tmp = s.setData;
            //            s.setData = s.setElementType;
            //            s.setElementType = tmp;

            swapStrings("data", "elementType");

            //report("\nVerb        : " + s.getVerb());
            //report("Element Name: " + s.setElementName);
            //report("elementType   : " + s.setElementType);
            //report("Verify      : " + s.setVerify);
            //report("Data        : " + s.setData);

            //report(s.getVerb() + " '" + s.setElementName + "' " + s.setElementType + " " + s.setVerify + " '" + s.setData + "'\n");

            //Verify, *the* ele name,                  class,       "exists",     ""
            //Verify,       list name,                 list,        "contains",   item name


        }

        if (switchElementnameWithData) {


            //report("   *** Reformatting Enter/Into ***");

            //Switch to process Select item From named list
            //Verb   | Element name |  *Removed*      | elementType      | verify    | data
            // Enter,   data         ,  *into* *the*     field name,      field,      ""
            // Enter,   field name   ,                   data,            field,      ""
            // Enter,   field name   ,                   field,           data,       ""
            // Enter,   field name   ,                   field,           "",         ""

            debug("is Element type empty 1?");
            swapStrings("elementName", "elementType");


            debug("is Element type empty 2?");

            //swapStrings("data", "elementType");
            //debug ("is Element type empty 3?");

            if (s.getElementType().isEmpty()) {
                s.setElementType("field");
                elementType = "field";
                //printParsedText();
            }

            if (s.getData().toLowerCase().contains("field")) {
                swapStrings("Data", "ElementType");
            }


            //report("\nVerb        : " + s.getVerb());
            //report("Element Name: " + s.setElementName);
            //report("elementType   : " + s.setElementType);
            //report("Verify      : " + s.setVerify);
            //report("Data        : " + s.setData);

            //report(s.getVerb() + " '" + s.setElementName + "' " + s.setElementType + " " + s.setVerify + " '" + s.setData + "'\n");

            //Verify, *the* ele name,                  class,       "exists",     ""
            //Verify,       list name,                 list,        "contains",   item name


        }

        //Verify swap
        if (verb.toLowerCase().contentEquals("verify")) {
            //report("   *** Reformatting Verify/Data ***");

            if (s.getVerify().contentEquals("")) {

                //report("   *** Reformatting Verify/Data ***");


                swapStrings("data", "verify");

                //report("\nVerb        : " + s.getVerb());
                //report("Element Name: " + s.setElementName);
                //report("elementType   : " + s.setElementType);
                //report("Verify      : " + s.setVerify);
                //report("Data        : " + s.setData);

                //report(s.getVerb() + " '" + s.setElementName + "' " + s.setElementType + " " + s.setVerify + " '" + s.setData + "'\n");
            }

            //if (s.setData.contentEquals("")){
            //    report("\n" + s.getVerb() + " '" + s.setElementName + "' " + s.setElementType + " " + s.setVerify);

            //}else{
            //    report("\n" + s.getVerb() + " '" + s.setElementName + "' " + s.setElementType + " " + s.setVerify + " '" + s.setData);
            //}

        }
        //printParsedText();
    }

    public ExecutionResult execute(WebAddonHelper helper) throws FailureException {

        s.setSentence(sentence);

        Framework fw = new Framework(
                alreadyFailed,
                currentElement,
                currentElementText,
                priorElementAvailable,
                priorElement,
                priorClickHoverElement,
                lastElementName,
                lastElementType,
                found,
                find,
                visibleElementIndex,
                elements,
                dpElements,
                caseSensitiveMatch,
                cntElements,
                priorClickHoverElementAvailable);



        //Init Browser
        WebDriver driver = helper.getDriver();
        js = driver;


        //Init Reporting
        reporter = helper.getReporter();

//        driver.navigate().to(url);
//        helper.getSearchCriteria();
//        driver.testproject().isVisible();

        //WebElement element = driver.findElement(By.xpath("//a[contains(text(), 'Get in Touch')]"));

        //Global Variables for each element
        boolean success = false;
        boolean isEnabled = false;
        boolean isDisplayed = false;
        boolean lastObjectFound = false;
        boolean verified = false;
        boolean clicked = false;
        boolean exists = false;

        WebElement item = null;

        String result = "";
        String negativeTest = ""; //Default Positive test
        String lastObjectName = "";
        String lastObjectType = "";
        String wasWasNot = " was ";

        //Reset flags
        caseSensitiveMatch = false;
        //Final overal step / test validation
        verified = false;

        //Flag to skip processing of object with comments
        boolean skip = false;

        WebElement element;

        Actions builder = new Actions(driver);

        //No Wait, all Sync
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);

        //driver.manage().window().maximize();
        //driver.manage().window().fullscreen();

        viewport = driver.findElement(By.tagName("body"));
        Dimension d = viewport.getSize();
        viewportHeight = driver.manage().window().getSize().getHeight();
        //viewportWidth = d.getWidth();  //Future use

        //Todo: Reset browser zoom to 100%
        //typeAtBrowser(driver, "CTRL+0");




        s.setSentence((s.getSentence() == null) ? "" : s.getSentence());

        s.setElementType((s.getElementType() == null) ? "" : s.getElementType());

        s.setVerify((s.getVerify() == null) ? "" : s.getVerify());
        s.setData((s.getData() == null) ? "" : s.getData());

        //framework.appendResults ("Next Step...\n");

        int lines = 1;
        int thisLine = 1;

        //List<String> arrLines = null;

        ArrayList<String> arrLines = new ArrayList<String>();

        //Check is sentence is a file
        if (s.getSentence().indexOf(".csv") > 0) {
            //open the file
            arrLines = readCsv(s.getSentence());
        } else {
            arrLines.add(s.getSentence());
        }

        //itterate all lines in file
        //until done

        for (String sentence : arrLines) {

            //Clear results

            parse(s.getSentence());

            //framework.appendResults ("Test continues...\n" +s.getSentence());

            if (fw.isAlreadyFailed()) {
                report("\nSKIPPED");
                //return ;
            }

            //Was URL supplied without without Open verb?
            if (s.getElementName().toLowerCase().isEmpty()) {
                //Todo: simulate  framework crash
                //if (s.getVerb() == "crash") {

                //}else{
                //Just a URL was passed.
                swapStrings("Verb", "ElementName");
                s.setVerb("Open");
                //printParsedText();
                //}
            }

            //Get the element
            switch (s.getVerb().toLowerCase()) {

                case "comment":
                case "comment:":
                case "feature:":
                case "background:":
                case "scenario:":
                    result = this.sentence;
                    exists = true;
                    verified = true;
                    skip = true;
                    break;

                case "type":
                    exists = true;
                    break;

                case "crash":
                    exists = false;
                    break;


                case "launch":
                case "open":
                case "nav to":
                case "navigate to":

                    if (!s.getElementName().contains("http")) {
                        s.setElementName("http://" + s.getElementName());
                    }

                    //framework.appendResults ("Test continues with URL...\n" +s.getElementName());

                    URL url = null;

                    try {
                        url = new URL(s.getElementName());
                        //Open Url


                        //Stops here if Chrome browser is ever-loading
                        //https://blog.francium.tech/handling-ever-loading-pages-via-selenium-webdriver-fb658e0d8094
                        //https://developers.perfectomobile.com/display/TT/Page+Load+Timeouts+in+Selenium

                        //Fall through to let pageSync determine when page is done loading

                        //debug(s.getVerb() + " browser!");
                        // Give browser build a breather

                        driver.manage().timeouts().pageLoadTimeout(3, TimeUnit.SECONDS);


                        // Reset the browser to 100% zoom
                        driver.get("chrome://settings");
                        driver.executeScript("chrome.settingsPrivate.setDefaultZoom(1.0);");
                        //driver.navigate().to(url);
                        driver.get(String.valueOf(url));



                    } catch (MalformedURLException e) {
                        //Catch other than MalformedURL

                        report("\nThe URL '" + s.getElementName() + "' is invalid\n\n" + e.getMessage());
                        result = "Failed: Unable to navigate to '" + url + "'";
                        //Stop all further execution
                        fw.setAlreadyFailed(true);
                    } catch (TimeoutException e) {
                        //Ast
                        debug("Done waiting for browser timed out");
                        result = "Navigated to " + url + " timed out";
                        pageSync(driver, "");
                        verified = true;
                    } catch (Exception e) {
                        debug(e.toString());
                        report("\nThe URL '" + s.getElementName() + "' failed to load:\n\n" + e.getMessage());
                        result = "Failed: Unable to navigate to '" + url + "'";
                        //Stop all further execution
                        fw.setAlreadyFailed(true);
                    }

                    break;

                default:

                    //*****************
                    //*  Get Element  *
                    //*****************

                    //Get the object to click, set, select verify
                    exists = momGetElement(driver);

                    if ((exists == false) && (s.getElementType().toLowerCase().contentEquals("button"))) {
                        //Class switch to a link
                        fw.setAlreadyFailed(false);
                        report("   *** Class switching BUTTON class to LINK ***");
                        s.setElementType("link");
                        exists = momGetElement(driver);

                    }

                    if ((exists == false) && (s.getElementType().toLowerCase().contentEquals("link"))) {
                        //Class switch to a button
                        fw.setAlreadyFailed(false);
                        report("   *** Class switching LINK class to BUTTON ***");
                        s.setElementType("button");
                        exists = momGetElement(driver);
                    }

                    if ((exists == false) && (s.getElementType().toLowerCase().contentEquals("image"))) {
                        //Class switch to a svg
                        fw.setAlreadyFailed(false);
                        report("   *** Class switching IMG class to SVG ***");
                        s.setElementType("svg");
                        exists = momGetElement(driver);
                    }


                    //Search by relative Label sibling (Evolent Health)
                    if (!exists) {
                        //Save the type
                        String tmpType = s.getElementType();

                        s.setElementType("label");  //Selenium does not pull labels
                        exists = momGetElement(driver);

                        if (exists) {
                            //"//div[@class='canvas- graph']//a[@href='/accounting.html'][i[@class='icon-usd']]/following-sibling::h4"
                            s.setElementType(tmpType + " sibling");
                            exists = momGetElement(driver);

                        }
                    }

                    if (!exists) {
                        report("No '" + s.getElementName() + "' " + s.getElementType() + "s were found\n");
                    }

            }

            if (exists && !skip) {

                switch (s.getVerb().toLowerCase()) {
                    case "feature:":
                    case "background:":
                    case "scenario:":
                        result = s.getVerb() + " " + s.getElementName();
                        verified = true;
                        break;


                    case "wait":
                        verified = true;
                        result = "Waiting " + s.getElementName() + " seconds automatic page update.";

                        sleep(Integer.parseInt(s.getElementName()));
                        break;

                    case "wait for":
                        verified = pageSync(driver, s.getElementName());
                        break;

                    case "highlight":
                        highlightAll(driver, elements);

                    case "unhighlight":
                        unhighlightAll(driver, elements);

                    case "expand":
                        currentElement.sendKeys(" ");


                    case "type":
                        //Send keys at browser
                        //send ESC to top element to clear
                        result = typeAtBrowser(driver, s.getElementName());
                        verified = true;
                        break;

                    case "rightclick":
                    case "click":

                        // DO NOT CHANGE THIS CODE
                        // IT WORKS, DON'T ASK WHY

                        //Scroll the object into view first
                        //slow
                        scrollIntoView(driver, currentElement);

                        if (!s.getElementType().contains("radio")) {
                            unhighlight(driver, priorElement);
                            highlight(driver, currentElement, "blue");
                        }

                        //Hover Set focus
                        js.executeScript("window.focus();");


                        try {
                            currentElement.sendKeys(Keys.SHIFT);
                        } catch (Exception e) {
                            //Eat the error
                            //e.printStackTrace();
                        }

                        //The browser page updates here
                        //StaleElementReferenceException


                        try {
                            js.executeScript("arguments[0].focus();", currentElement);
                        } catch (Exception e) {
                            refreshCurrentObject();
                            js.executeScript("arguments[0].focus();", currentElement);
                            //e.printStackTrace();
                        }


                        try {
                            currentElement.sendKeys("");
                        } catch (Exception e) {
                            //Eat the error
                            //e.printStackTrace();
                        }

                        //new Actions(driver).moveToElement(this.currentElement).perform();
                        //Alternate click #1  Works when rebooted / not when agent restarted
                        // Works when stopped and run

                        //REQUIRED
                        if (verb.toLowerCase() == "rightclick") {
                            js.executeScript("arguments[0].rightclick();", this.currentElement);
                        } else {
                            js.executeScript("arguments[0].click();", this.currentElement);
                        }

                        //try {
                        //REQUIRED
                        //this.currentElement.click();   //Works when stopped and run

                        clicked = true;
                        //} catch (Exception e) {
                        //REQUIRED
                        //Alternate click #2 Works when Rebooted, Stopped and run

                        //    try {
                        //        currentElement.sendKeys(Keys.RETURN);
                        //    } catch (StaleElementReferenceException staleE){
                        //Eat the StaleElementReferenceException error
                        //    }


                        //}

                        //REQUIRED to allow click to work
                        sleep(400);


                        //Get the browser status

                        //Alternate click #3 Does not work
                        //currentElement.sendKeys(Keys.ENTER);
                        //report ("Ready State = 'Complete': " + String.elementNameOf(js.executeScript("return document.readyState").toString().equals("complete")));


                        //May need benign interaction
                        //WebElement body = driver.findElement(By.tagName("body"));

                        //driver.navigate().refresh();
                        //driver.findElement(By.name("name")).sendKeys(Keys.F5);

                        //Click
                        //report ("IsEnabled: " + String.elementNameOf(currentElement.isEnabled()));

                        //double abs_x;
                        //double abs_y;
                        //double oldAbs_y =0;

                        //abs_x = (double) js.executeScript("return arguments[0].getBoundingClientRect().left;", currentElement);
                        //report ("abs_x: " + abs_x );

                        //abs_y = (double) js.executeScript("return arguments[0].getBoundingClientRect().top; ", currentElement);
                        //oldAbs_y = abs_y;
                        //report ("abs_y: " + abs_y );

                        //while (oldAbs_y != abs_y) {
                        //    oldAbs_y = abs_y;
                        //    report ("still scrolling...");
                        //    abs_y = (double) js.executeScript("return arguments[0].getBoundingClientRect().top; ", currentElement);
                        //}

                        //Get the screen location of the current object
                        //abs_x = (double) js.executeScript("return arguments[0].getBoundingClientRect().left;", currentElement);
                        //report ("abs_x: " + abs_x );

                        //abs_y = (double) js.executeScript("return arguments[0].getBoundingClientRect().top; ", currentElement);
                        //report ("abs_y: " + abs_y );

                        //report ("hover...");

                        //Alternate click #4 //Does Not work - not even stop and run

                        //builder.moveToElement(currentElement, 10, 25)
                        //        .click(currentElement);

                        //Alternate #5
                        //builder.moveToElement(driver.findElement(By.tagName("body")), 0, 0);
                        //builder.moveByOffset(328,900).click().build().perform();

                        //Alternate #6
                        //js.executeScript("el = document.elementFromPoint("+ (-abs_x) +", "+ (1-abs_y) +"); el.click();");

                        //Alternate#7
                        //Actions actions = new Actions(this.session);
                        //int xPosition = this.session.FindElementsByAccessibilityId("GraphicView")[0].Size.Width - 530;
                        //int yPosition = this.session.FindElementsByAccessibilityId("GraphicView")[0].Size.Height- 150;
                        //actions.MoveToElement(this.xecuteClientSession.FindElementsByAccessibilityId("GraphicView")[0], xPosition, yPosition).ContextClick().Build().Perform();

                        //Alternate #8
                        //Actions actions = new Actions(driver);
                        //WebElement body = driver.findElement(By.tagName("body"));

                        //Dimension d = body.getSize();

                        //int w = d.getWidth();
                        //int h = d.getHeight();

                        //report ("h: " + h);
                        //report ("w: " + w);

                        //int zero_x = d.getWidth()/2;
                        //int zero_y = d.getHeight()/2;

                        //report ("zero_x: " + zero_x);
                        //report ("zero_y: " + zero_y);

                        //actions.moveToElement(body), (int)abs_x, (int)currentElement.click();
                        //actions.moveToElement(driver.findElement(By.tagName("body")), -386, -429);

                        //100 -900 = Ginger
                        //100 -500 = "US"
                        //100 -200 to -129 = Message
                        //0    -25 = Below message
                        //0     0 -4 = "see"

                        //Works on everything EXCEPT buttons
                        //actions.moveByOffset(0, -115).click().build().perform();

                        //9 Click with JS
                        //abs_x = 350;
                        //abs_y = 2120;

                        //js.executeScript("var ev = document.createEvent('MouseEvent'); "+
                        //        "var el = document.elementFromPoint(" + abs_x + ", " + abs_x + ");" +
                        //        "ev.initMouseEvent(" +
                        //        "'click', true, true, window, null, " +
                        //        abs_x + ", " + abs_y + ", 0, 0, " +
                        //        "false, false, false, false, 0, null); " +
                        //"el.dispatchEvent(ev);");

                        //10 execute a double click on the body object at the button location
                        //Does not work
                        //js.executeScript("arguments[0].click(350, 10);", body);
                        pageSync(driver, "");
                        result = "The object was clicked and page synced";
                        verified = true;
                        break;

                    case "enter":
                    case "set":
                        if (s.getElementType().toLowerCase() == "checkbox") {

                            if (!this.currentElement.isSelected()) {


                                try {
                                    //Open the list
                                    this.currentElement.click();
                                } catch (Exception e) {
                                    //Execute through JS if an objetoverlays it
                                    js.executeScript("arguments[0].click()", this.currentElement);
                                }

                                highlight(driver, currentElement, "blue");

                                result = "The checkbox was set";
                                verified = true;
                            } else {
                                result = "The checkbox was already set.";
                                verified = true;
                            }

                        } else {

                            //swap data and verify
                            if (s.getVerify() != "" && s.getData() == "") {
                                //s.setData = s.setVerify.trim();
                                swapStrings("Data", "Verify");

                            }

                            //No highlight - data entry is visible
                            highlight(driver, currentElement, "blue");
                            scrollIntoView(driver, currentElement);

                            this.currentElement.clear();
                            this.currentElement.sendKeys(s.getData());
                            result = "The field was set to " + s.getData();
                            verified = true;

                        }
                        break;

                    case "select":
                        //List
                        if (exists) {

                            try {
                                //Open the list, radio checkbox
                                if (!s.getElementType().contains("radio")) {
                                    flash(driver, currentElement, "blue");
                                }

                                this.currentElement.click();

                            } catch (Exception e) {
                                //Execute through JS if an objetoverlays it
                                js.executeScript("arguments[0].click()", this.currentElement);
                            }

                            if (s.getElementType().toLowerCase().contains("list")) {

                                result = "The '" + s.getElementName() + "' list was opened";

                                //Find the list item
                                pageSync(driver, "");
                                WebElement webList = currentElement;

                                //Get the item
                                elementType = "item";
                                s.setElementType(elementType);
                                elementName = s.getData();
                                s.setElementName(elementName);

                                exists = momGetElement(driver);

                                if (exists) {
                                    //Voodoo code - to close list after click item
                                    //REQUIRED

                                    //Not needed - text changes
                                    //unhighlight (driver, priorElement);
                                    //highlight ( currentElement, "blue");

                                    js.executeScript("arguments[0].click();", this.currentElement);

                                    try {
                                        //REQUIRED
                                        this.currentElement.click();   //Works when stopped and run

                                        clicked = true;
                                    } catch (Exception e) {
                                        //REQUIRED
                                        //Alternate click #2 Works when Rebooted, Stopped and run

                                        try {
                                            currentElement.sendKeys(Keys.RETURN);
                                        } catch (StaleElementReferenceException staleE) {
                                            //Eat the StaleElementReferenceException error
                                        }

                                    }


                                    js.executeScript("arguments[0].blur(false);", currentElement);
                                    js.executeScript("arguments[0].blur(false);", webList);


                                    //REQUIRED to allow click to work
                                    sleep(400);

                                    //if item exists, click list
                                    //if (currentElementType.contains("item")){
                                    //    webList.click();
                                    //}

                                    fw.appendStepResult (" and the '" + s.getData() + "' item was selected.");

                                } else {
                                    fw.appendStepResult (" but the '" + s.getData() + "' item did not exist.");
                                    // take screen capture

                                }

                            } else {
                                result = "The '" + s.getElementName() + "' " + s.getElementType() + " was selected";
                                verified = true;
                            }
                        }
                        break;

                    case "clear":

                        //Checkbox or field

                        if (s.getElementType().toLowerCase().contains("checkbox")) {

                            if (this.currentElement.isSelected()) {

                                try {
                                    //Clear the checkbox
                                    highlight(driver, currentElement, "blue");

                                    this.currentElement.click();
                                } catch (Exception e) {  //replaced ElementClickInterceptedException
                                    //Execute through JS if an objetoverlays it
                                    js.executeScript("arguments[0].click()", this.currentElement);
                                }

                                result = "The checkbox was cleared";
                                verified = true;

                            } else {
                                result = "The checkbox was already clear.";
                                verified = true;

                            }

                        } else {

                            this.currentElement.clear();
                            result = "The '" + s.getElementName() + "' field was cleared";
                            debug("Cleared the '" + s.getElementName() + "' " + s.getElementType());
                            verified = true;

                        }

                        break;

                    case "over":
                        if (s.getVerb().toLowerCase() == "hover") {
                            s.setVerb("hover over");
                            //We already hovered on first pass
                            break;

                        } else {
                            //Over is part of a text
                            break;
                        }

                    case "hover over":
                    case "hover":
                        //Get the count of objects
                        flash(driver, currentElement, "blue");

                        //Set focus to ensure hover works
                        js.executeScript("window.focus();");


                        try {
                            currentElement.sendKeys(Keys.SHIFT);
                        } catch (Exception e) {
                            //Eat the error
                            //e.printStackTrace();
                        }


                        //The browser page updates here
                        //StaleElementReferenceException

                        //REQUIRED
                        try {
                            js.executeScript("arguments[0].focus();", currentElement);
                        } catch (Exception e) {
                            refreshCurrentObject();
                            js.executeScript("arguments[0].focus();", currentElement);
                            //e.printStackTrace();
                        }

                        try {
                            currentElement.sendKeys("");
                        } catch (Exception e) {
                            //Eat the error
                            //e.printStackTrace();
                        }


                        builder
                                .moveToElement(currentElement)
                                .perform();

                        //js.executeScript("arguments[0].hover();", currentElement);
                        //js.executeScript("arguments[0].onmouseover();", currentElement);

                        //Wait for the count to change
                        result = "Hovering over '" + s.getElementName() + "' " + elementType;
                        pageSync(driver, ""); //Hover takes more time
                        //pageSync(driver, ""); //Hover takes more time

                        verified = true;

                        break;

                    case "find":    //Find an element based on another element
                        //Paylocity Find named news article. Click 'Read More'
                        find = true;

                    case "verify":
                        wasWasNot = " was displayed.";

                        //Extraneous

                        //if (s.setElementType.toLowerCase().contains("text")){
                        //    waitFor(driver, s.setElementName, 9); //9 seconds is default regardless of lower numbers
                        //}

                        String verify = s.getVerify().toLowerCase();

                        switch (verify) {
                            case "is":
                                verified = (this.currentElement.toString() == s.getElementName());
                                if (!verified) wasWasNot = " was ";
                                break;

                            case "is not":
                                verified = !(this.currentElement.toString() == s.getElementName());
                                if (!verified) wasWasNot = " was not ";
                                negativeTest = "yes";
                                break;
                            case "did appear":
                            case "does appear":
                            case "appeared":
                            case "appears":
                            case "exists":
                            case "displayed":
                            case "visible":
                            case "is displayed":
                            case "is visible":
                                wasWasNot = " was displayed ";

                                verified = exists;
                                if (verified) {

                                    flashPass(driver, currentElement);

                                    //Highlight any close matches
                                    highlightCloseMatches(driver);


                                } else {
                                    wasWasNot = " was NOT displayed ";
                                }

                                break;


                            case "is enabled":
                                wasWasNot = " was enabled ";
                                verified = exists;

                                if (verified) {

                                    isEnabled = currentElement.isEnabled();

                                    if (isEnabled) {

                                        flashPass(driver, currentElement);

                                        //Highlight any close matches
                                        highlightCloseMatches(driver);
                                    } else {
                                        flashFail(driver, currentElement);
                                    }

                                } else {
                                    wasWasNot = " was NOT displayed ";
                                }

                                break;

                            case "does not exist":
                            case "is not displayed":
                            case "is not visible":
                                verified = exists;
                                wasWasNot = " was not displayed ";
                                if (verified) {
                                    wasWasNot = " WAS displayed ";

                                    flashFail(driver, currentElement);

                                }
                                negativeTest = "yes";
                                break;

                            case "contains":
                                verified = (this.currentElement.toString().contains(s.getElementName()));
                                wasWasNot = " did contain ";
                                if (!verified) wasWasNot = " did NOT contain ";
                                break;

                            case "does not contain":
                                verified = (this.currentElement.toString().contains(s.getElementName()));
                                wasWasNot = " did not contain ";
                                if (!verified) wasWasNot = " DID contain ";
                                negativeTest = "yes";

                                break;
                        }


                        if (lastElementType.toLowerCase().contains("text")) {
                            result = "The text '" + lastElementName + "' " + wasWasNot;
                            verified = exists;

                            //flashPass(driver, this.currentElement);

                        } else {
                            result = "The '" + lastElementName + "' " + lastElementType + wasWasNot;

                        }
                        break;

                    default:


                        report("   *** Unknown verb command '" + verb + "' ***");


                }
            }

            //Switch verified result for negative test
            if (negativeTest.toLowerCase().contains("true") || negativeTest.toLowerCase().contains("yes")) {
                verified = !verified;  //Not found is a Success, found is a Fail
                fw.appendStepResult (" (Negative Test)");
            }

            //Wait to see the result
            //driver.testproject().pause(1000);

            //reporter.result is only the last elementName of the step


            //Wait to see the result
            //driver.testproject().pause(1000);

        }


        if (caseSensitiveMatch) {

            if (s.getElementName() != currentElementText) {

                fw.appendStepResult ("   *** WARNING CASE INSENSITIVE MATCH ***" + "\n   '" + s.getElementName() + "'   \nmatched\n   '" + currentElementText + "'\n");
                //fw.appendStepResult(result);
            }

            //Reset flag
            caseSensitiveMatch = false;
        }


        //Send all the results of the test back to TestProject
        //reporter.result(framework.getTestResults() + "\n");

        //Clear results for next test
        //framework.clearTestResults();


        if (verified) {
            //report("\n" + result);
            return ExecutionResult.PASSED;

        } else {
            result = "   *** FAILURE " + result;
            //report("\n" + result);


            return ExecutionResult.FAILED;
        }


    }


    private String typeAtBrowser(WebDriver driver, String elementName) {
        String result = "";
        Actions action = new Actions(driver);

        switch (elementName.toLowerCase()) {

            case "enter":
            case "return":
                action.sendKeys(Keys.ENTER).build().perform();
                result = "'ENTER' key was typed at the browser.";
                pageSync(driver, "");
                //todo - Other keys
                break;


            case "esc":
            case "escape":
                //Voodoo Code: 500-999 ms DO NOT REMOVE
                sleep(999);

                //Hover Set focus
                //js.executeScript("window.focus();");

                action.sendKeys(Keys.ESCAPE).build().perform();
                result = "'ESC' was typed at the browser.";
                pageSync(driver, "");
                //todo - Other keys
                break;

            case "page down":
            case "pgdn":
                action.sendKeys(Keys.PAGE_DOWN).build().perform();
                result = "'PGDN' was typed at the browser.";
                break;

            case "down key":
            case "arrow down":
            case "down":
                action.sendKeys(Keys.ARROW_DOWN).build().perform();
                result = "'Down Arrow' was typed at the browser.";
                break;

            case "page up":
            case "pgup":
                action.sendKeys(Keys.PAGE_UP).build().perform();
                result = "'PGUP' was typed at the browser.";
                break;

            case "up key":
            case "arrow up":
            case "up":
                action.sendKeys(Keys.ARROW_UP).build().perform();
                result = "'Up Arrow' was typed at the browser.";
                break;

            case " ":
            case "space":
            case "spacebar":
                action.sendKeys(Keys.SPACE).build().perform();
                break;

            //Todo: Reset browser zoom to 100% - Neither approach works on Chome
            //case "ctrl+0":

            //Approach #1
            //   action.sendKeys(Keys.chord(Keys.CONTROL, "0")).build().perform();
            //   break;

            //Approach #2
            //WebElement html = driver.findElement(By.tagName("html"));
            //html.sendKeys(Keys.chord(Keys.CONTROL, "0"));

            //Approach #3
            //JavascriptExecutor js = driver;
            //js.executeScript("document.body.style.zoom='100%'");


            default:
                action.sendKeys(elementName).build().perform();
                result = "'" + s.getElementName() + "' was typed at the browser.";

        }
        return result;
    }

    // ****************************************************************
    // Supporting Methods
    // ****************************************************************

    /**
     * Gets the named webelement type.
     *
     * @param driver
     * @return success - True if object found, false if not
     * @Author Paul Grossman Date: 8/8/2019
     */

    private boolean momGetElement(WebDriver driver) {
        //Name may be normallized like 'q' search
        boolean isDisplayed;
        boolean success = false;
        boolean sibling = false;
        String elementName = s.getElementName();

        //framework.setLastElementType(s.getElementType());

        String elementType = s.getElementType();
        String verb = s.getVerb();

        String normalizedElementName = elementName;

        if (elementType.toLowerCase().contains(" sibling")) {
            elementType = elementType.replace(" sibling", "");
            sibling = true;
        }

        switch (elementType.toLowerCase()) {
            case "svg":
            case "label":
            case "list":
            case "item":
            case "option":
            case "image":
            case "icon":
            case "radio":
            case "radiobutton":
            case "radio button":
            case "checkbox":
            case "button":
            case "link":  //Same as SVG
            case "text":
            case "column":
            case "all":
            case "tab":
            case "field":
                break;
            default:
                //Move everything down
                //debug ("Move eveything down and
                // guess at element type");
                swapStrings("verify", "data");
                swapStrings("elementType", "verify");

        }

        if (s.getVerb().toLowerCase().contains("enter") && s.getData().isEmpty()) {
            swapStrings("verify", "data");
        }


        //Use educated guesses if there is no Element type
        if (s.getElementType() == "")

            switch (s.getVerb().toLowerCase()) {
                case "verify":
                    s.setElementType("text");
                    elementType = "text";

                    if (s.getVerify() == "") {
                        s.setVerify("exists");
                    }

                    break;

                case "wait":
                    s.setElementType("text");
                    elementType = "text";
                    break;

                case "wait for":

                    s.setElementType("text");
                    elementType = "text";
                    break;

                case "click":
                    s.setElementType("link");
                    elementType = "link";
                    break;

                case "enter":
                case "enter into":
                    s.setElementType("field");
                    elementType = "field";
                    break;

                case "expand":
                    s.setElementType("link");
                    elementType = "link";
                    break;

                case "set":
                    s.setElementType("checkbox");
                    elementType = "checkbox";
                    break;

                case "clear":
                    s.setElementType("checkbox");
                    elementType = "checkbox";
                    break;

                case "hover":
                    s.setElementType("button");
                    elementType = "button";
                    break;

                case "":
                    report("ERROR: Blank Verb and Element Type");
                    fw.setAlreadyFailed(true);
            }

        //s.setElementType(s.getElementType());  /Redundant


        //Normalize sample: Click close icon 'X'
        if (elementName.toLowerCase().contentEquals("close icon")) {
            s.setElementName("scrim");
            elementName = "scrim";
            s.setElementType("button");
            elementType = "button";
        }

        printParsedText();

        boolean bypassToAll = false;

        int thisElement = 0;

        // print("getElement type: " + elementType.toLowerCase());

        //elements = driver
        //    .findElements(By.cssSelector("*"));

        //report ("Searching " + elements.size() + " elements.");
        //report ("Searching " + elementType + "s.");

        //Case insensitive  //text()[matches(.,'test', 'i')]

        //debug ("s.setElementType: " + s.getElementType().toLowerCase());

        //debug ("elementType: " +elementType.toLowerCase());

        //s.setElementType breaks item

        if (sibling == true) {
            switch (elementType.toLowerCase()) {
                //Elements that support Sibling search
                case "field":
                case "radiobutton":
                    break;

                default:
                    //Skip the Sibling search
                    elementType = "none";
            }
        }

        //Get a count of iFrames

        String dpFrame = "//iframe";
        elements = driver
                .findElements(By.xpath(dpFrame));

        int maxFrames = elements.size();
        //maxFrames = driver.findElements(By.tagName("iframe")).size();
        // https://www.guru99.com/handling-iframes-selenium.html
        // Nothing to add the first time round

        dpFrame = "";

        //iFrames are 1-based
        for (int thisFrame = 0; thisFrame <= maxFrames; thisFrame++) {



            if (thisFrame > 0) {
                try {
                    driver.switchTo().frame(thisFrame + 1);
                    debug("Searching in frame " + (thisFrame + 1) +" ...");
                } catch (Exception e) {
                    //Eat the error
                }

                //}else{
                //    driver.switchTo().defaultContent();
            }


            switch (elementType.toLowerCase()) {

                case "svg":
                    //highlightAll(driver, elements);
                    //Was button
                    dpElements = "//svg[contains(@class, '" + normalizedElementName + "')]";
                    elements = driver
                            .findElements(By.xpath(dpElements));

                    //Was button
                    if (elements.size() < 1) {
                        dpElements = "//svg[contains(@class, '" + normalizedElementName + "')]";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    //remove all spaces
                    if (elements.size() < 1) {
                        dpElements = "//svg[contains(@class, '" + normalizedElementName.toLowerCase().replaceAll(" ", "") + "')]";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    break;

                case "list":
                    //name ot id (short)
                    dpElements = "//select[contains(@name, '" + normalizedElementName + "')]";
                    elements = driver
                            .findElements(By.xpath(dpElements));


                    //remove all spaces
                    if (elements.size() < 1) {
                        dpElements = "//div[contains(@class, '" + normalizedElementName.replaceAll(" ", "") + "')]";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    //remove all spaces
                    if (elements.size() < 1) {
                        dpElements = "//div[contains(@class, '" + normalizedElementName.replaceAll(" ", "") + "')]";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }


                    //May only work on certain browser
                    if (elements.size() < 1) {
                        dpElements = "select[id*='" + normalizedElementName.replaceAll(" ", "-") + "'i]"; //All elements good syntax case insensitive//
                        elements = driver
                                .findElements(By.cssSelector(dpElements));
                        caseSensitiveMatch = true;
                    }

                    if (elements.size() < 1) {
                        dpElements = "//select[contains(@class, '" + normalizedElementName.toLowerCase().replaceAll(" ", "-") + "')]";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }


                    if (elements.size() < 1) {
                        dpElements = "*[class*='" + normalizedElementName.replaceAll(" ", "") + "'i]"; //All elements good syntax case insensitive//
                        elements = driver
                                .findElements(By.cssSelector(dpElements));
                        caseSensitiveMatch = true;
                    }

                    if (elements.size() < 1) {
                        dpElements = "//div[@class='form-control']";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    //Find the item to select and click
                    break;

                case "item":
                case "option":
                    //id or name
                    dpElements = "//option[contains(@value, '" + normalizedElementName + "')]";
                    elements = driver
                            .findElements(By.xpath(dpElements));

                    //print("Found " + elements.size() + " item(s0");

                    break;

                case "image":
                    dpElements = "//img[contains(@name, '" + normalizedElementName + "')]";
                    elements = driver
                            .findElements(By.xpath(dpElements));

                    if (elements.size() < 1) {
                        dpElements = "//img[contains(@alt, '" + normalizedElementName + "')]";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    if (elements.size() < 1) {
                        dpElements = "//img[contains(@src, '" + normalizedElementName + "')]";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    if (elements.size() < 1) {
                        dpElements = "//div[contains(@class, '" + normalizedElementName.toLowerCase() + "')]";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    if (elements.size() > 0) {
                        break;
                    }
                    debug("Class switch from image to icon");

                    //Class switch to Icon
                case "icon":
                    dpElements = "//div[contains(@class, '" + normalizedElementName + "') and contains(@class, 'icon')  ]";
                    elements = driver
                            .findElements(By.xpath(dpElements));
                    // print("Found " + elements.size() + " elements");
                    if (elements.size() < 1) {
                        dpElements = "//div[contains(@class, '" + normalizedElementName + "')]";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    break;

                //https://stackoverflow.com/questions/2030711/selenium-locator-for-label-for-x
                case "label":

                    //List of non working approaches
                    //iFrame is the issue //iframe[@id='dynamic-table']
                    //dpElements = "(//label)";  //Works

                    //dpElements = "//label[text()='" + normalizedElementName + "']";

                    //dpElements = "//label[text()='Username']/@for'), 'xxx'";
                    //dpElements = "//input[@id=(//label[text()='Username']/@for)]";
                    //dpElements = "//label[text()='Username']/@for";
                    //dpElements ="//label[text()='Username']/input";

                    //form[@id='pardot-form']/p[@class='Company']
                    //form[@id='pardot-form']
                    //p[contains(@class, 'Company')]/label
                    //label[contains(text(), 'Company')]/input
                    //label[contains(., 'Company')]/input

                    //UFT  "//FORM[@id="pardot-form"]/P[1]/INPUT[1]"
                    //dpElements = "//form[@id='pardot-form']/p[@class='" + normalizedElementName + "']";

                    dpElements = "(//label[text()='" + normalizedElementName + "'])";
                    elements = driver
                            .findElements(By.xpath(dpElements));

                    debug(dpElements);

                    if (elements.size() < 1) {
                        dpElements = "(//label[contains(text(), '" + normalizedElementName + "')])";
                        debug(dpElements);
                        elements = driver
                                .findElements(By.xpath(dpElements));

                    }

                    if (elements.size() < 1) {
                        dpElements = "(//iframe[" + thisFrame + "]/label[contains(text(), '" + normalizedElementName + "')])";
                        debug(dpElements);
                        elements = driver
                                .findElements(By.xpath(dpElements));

                    }

                    if (elements.size() < 1) {
                        dpElements = "(//p[contains(@class, '" + normalizedElementName + "')])";
                        debug(dpElements);
                        elements = driver
                                .findElements(By.xpath(dpElements));

                    }

                    // uses dot(.)  Finds text in Text() and <TAG> </TAG>
                    if (elements.size() < 1) {
                        dpElements = "//p[contains(., '" + normalizedElementName + "')]";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                        debug(dpElements);
                    }
                    break;


                case "radio":
                case "radiobutton":
                case "radio button":


                    if (sibling) {
                        //Evolent
                        dpElements += "/following-sibling::input";
                    } else {

                        dpElements = "//label[text()='" + normalizedElementName + "']";
                    }


                    elements = driver
                            .findElements(By.xpath(dpElements));

                    if (elements.size() < 1) {
                        dpElements = "//input[@value='" + normalizedElementName + "']";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }


                    if (elements.size() < 1) {
                        dpElements = "//span[text()='" + normalizedElementName + "' and contains(@class,'radio')]";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    if (elements.size() < 1) {
                        dpElements = "//span[text()='" + normalizedElementName + "']";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    if (elements.size() < 1) {
                        dpElements = "//input[@value='" + normalizedElementName + "' and @type ='radio']";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    if (elements.size() < 1) {
                        dpElements = "//label[contains(text(), '" + normalizedElementName + "') and contains(@class,'radio')]";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    if (elements.size() < 1) {
                        dpElements = "//label[text()='" + normalizedElementName + "']";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    //                                     "//div[@class='question-group']/label[text()='Mrs']")
                    if (elements.size() < 1) {
                        dpElements = "//p[@class='group']/label[text()='" + normalizedElementName + "']";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    //  //div[@class='question-group']/input[@elementName='MRS']

                    if (elements.size() < 1) {
                        dpElements = "//label[text()='" + normalizedElementName + "']";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }


                    if (elements.size() < 1) {
                        dpElements = "(//iframe[" + thisFrame + "]/label[contains(text(), '" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }


                    break;


                case "button":
                    // print("Normalize Button");
                    // Check if it's in elementName //div[contains(@iList<WebElement> buttons =
                    // (List<WebElement>) driver

                    // Normalize "Add to Cart" to "shopping-cart"
                    //switch (elementName.toLowerCase()) {
                    //    case "add to cart":
                    //        normalizedElementName = "shopping-cart";
                    //}

                    //report ("Searching for a '" + normalizedElementName + "' button.");

                    //All buttons - Fast
                    //elements = (List<WebElement>) driver
                    //        .findElements(By.xpath("//input"));

                    //Slow Webdriver 76 & 77 Chrome and Firefox,


                    dpElements = "(//button[text()='" + normalizedElementName + "'])";
                    elements = driver
                            .findElements(By.xpath(dpElements));

                    if (elements.size() < 1) {
                        dpElements = "(//button[contains(text(), '" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    if (elements.size() < 1) {
                        dpElements = "button[text='" + normalizedElementName + "'i]"; //All elements good syntax case insensitive//
                        elements = driver
                                .findElements(By.cssSelector(dpElements));
                        caseSensitiveMatch = true;

                    }

                    if (elements.size() < 1) {
                        dpElements = "button[innertext='" + normalizedElementName + "'i]"; //All elements good syntax case insensitive//
                        elements = driver
                                .findElements(By.cssSelector(dpElements));
                        caseSensitiveMatch = true;

                    }


                    if (elements.size() < 1) {
                        dpElements = "(//input[@value='" + normalizedElementName + "'])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }


                    if (elements.size() < 1) {
                        dpElements = "(//input[contains(@value, '" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    //Slow
                    // print("Looking through " + elements.size() + " elementName/input close match
                    // input");
                    // Add to Cart xpath for span alternative:
                    // xpath//span[contains(@class,'cart') //Works case sensitive
                    // <span class="fa fa-shopping-cart"></span>

                    if (elements.size() < 1) {
                        dpElements = "(//input[contains(@elementName,'" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    //report ("elements " + elements.size());

                    // print("Looking through " + elements.size() + " span/class close match
                    // input span");
                    if (!found && elements.size() < 1) {
                        dpElements = "(//span[contains(@class,'" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                        //print("Span elements: " + elements.size());
                    }

                    if (elements.size() < 1)
                    //    print("Searching..");
                    {
                        dpElements = "(//button[contains(text(), '" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }
                    // print("Looking through " + elements.size() + " inout/text close match
                    // input buttons");


                    if (elements.size() > 0)
                        break;

                    break;

                case "link":  //Same as SVG
                    dpElements = "(//a[contains(text(),'" + normalizedElementName + "')])";
                    elements = driver
                            .findElements(By.xpath(dpElements));
                    //report ("Searching " + elements.size() + " links.");

                    if (elements.size() < 1) {

                        dpElements = "(//a[contains(@title,'" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    // Check if it's in Span element text
                    if (elements.size() < 1) {
                        dpElements = "(//span[contains(text(),'" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                        // print("Looking through " + elements.size() + " elementName/divs text");
                    }


                    // Check if it's in case sensitive element text

                    //May only work on certain browser
                    if (elements.size() < 1) {
                        dpElements = "a[href*='" + normalizedElementName + "'i]"; //All elements good syntax case insensitive//
                        elements = driver
                                .findElements(By.cssSelector(dpElements));
                        caseSensitiveMatch = true;

                    }

                    //Case insensitive match
                    if (elements.size() < 1) {
                        dpElements = "a[text='" + normalizedElementName + "'i]"; //All elements good syntax case insensitive//
                        elements = driver
                                .findElements(By.cssSelector(dpElements));
                        caseSensitiveMatch = true;

                        break;
                    }

                    //Case insensitive match
                    if (elements.size() < 1) {
                        dpElements = "a[text*='" + normalizedElementName + "'i]"; //All elements good syntax case insensitive//
                        elements = driver
                                .findElements(By.cssSelector(dpElements));
                        caseSensitiveMatch = true;

                        break;
                    }

                case "tab":
                    dpElements = "(//a[contains(text(),'" + normalizedElementName + "')])";
                    elements = driver
                            .findElements(By.xpath(dpElements));

                    if (elements.size() < 1) {
                        dpElements = "(//a[contains(@name,'" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    //Paylocity
                    if (elements.size() < 1) {
                        dpElements = "(//span[contains(@name,'" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }
                    break;


                case "field":
                    // print("Normalize Button");
                    // Check if it's in elementName //div[contains(@iList<WebElement> buttons =
                    // (List<WebElement>) driver

                    //todo - search all properties and elementNames for the text for commmon identifier

                    // Normalize "Add to Cart" to "shopping-cart"
                    //switch (elementName.toLowerCase()) {
                    //    case "add to cart":
                    //        normalizedElementName = "shopping-cart";
                    //}


                    //span[translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'Google Search']
                    //String uc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                    //String lc = "abcdefghijklmnopqrstuvwxyz";
                    //span[translate(.,'uc', 'lc') = 'Google Search']

                    //Case inseneitive Sample #1
                    //[translate(.,'uc', 'lc') = 'Google Search']

                    //Case inseneitive Sample #2
                    //html/body//text()[matches(.,'test', 'i')]

                    // //CD[matches(@title,'empire burlesque','i')]

                    //dpElements = "(//input[matches(@name,'" + normalizedElementName + "','i')])";


                    if (sibling) {
                        //Evolent
                        dpElements += "/following-sibling::input";
                    } else {

                        dpElements = "(//input[@name='" + normalizedElementName + "'])";
                    }

                    elements = driver.
                            findElements(By.xpath(dpElements));

                    if (sibling && elements.size() < 1) {
                        dpElements = dpElements.replace("::input", "::textarea");
                        elements = driver.
                                findElements(By.xpath(dpElements));
                    }


                    // Todo Normalized custom property
                    if (!sibling) {

                        //Common properties: type, id(contains), name(contains), elementName placeholder
                        if (elements.size() < 1) {
                            dpElements = "(//input[@type='" + normalizedElementName + "'])";
                            elements = driver.
                                    findElements(By.xpath(dpElements));
                        }

                        //if (elements.size() < 1) {
                        //    elements = (List<WebElement>) driver.
                        //            findElements(By.xpath("(//input[contains(@data-aid, '"+ normalizedElementName + "')])"));
                        //}

                        if (elements.size() < 1) {
                            dpElements = "(//input[contains(@data-aid, '" + normalizedElementName.toUpperCase() + "')])";
                            elements = driver.
                                    findElements(By.xpath(dpElements));
                        }

                        if (elements.size() < 1) {
                            dpElements = "(//input[@id='" + normalizedElementName + "'])";
                            elements = driver.
                                    findElements(By.xpath(dpElements));
                        }

                        //Total Spectrum Care
                        if (elements.size() < 1) {
                            dpElements = "(//input[contains(@placeholder, '" + normalizedElementName.toUpperCase() + "')])";
                            elements = driver.
                                    findElements(By.xpath(dpElements));
                        }

                        if (elements.size() < 1) {
                            dpElements = "(//input[@ng-model='" + normalizedElementName + "'])";
                            elements = driver.
                                    findElements(By.xpath(dpElements));
                        }

                        if (elements.size() < 1) {
                            dpElements = "input[name*='" + normalizedElementName + "'i]"; //All elements good syntax case insensitive//
                            elements = driver
                                    .findElements(By.cssSelector(dpElements));
                            caseSensitiveMatch = true;
                        }

                        if (elements.size() < 1) {
                            dpElements = "(//textarea[contains(@data-aid, '" + normalizedElementName.toUpperCase() + "')])";
                            elements = driver.
                                    findElements(By.xpath(dpElements));
                        }

                        if (elements.size() < 1) {
                            dpElements = "textarea[id*='" + normalizedElementName + "'i]"; //All elements good syntax case insensitive//
                            elements = driver
                                    .findElements(By.cssSelector(dpElements));
                            caseSensitiveMatch = true;
                        }

                        if (elements.size() < 1) {
                            dpElements = "textarea[name*='" + normalizedElementName + "'i]"; //All elements good syntax case insensitive//
                            elements = driver
                                    .findElements(By.cssSelector(dpElements));
                            caseSensitiveMatch = true;
                        }


                        //Company ID field - id without spaces different case
                        if (elements.size() < 1) {
                            dpElements = "input[name*='" + normalizedElementName.replaceAll(" ", "") + "'i]"; //All elements good syntax case insensitive//
                            elements = driver
                                    .findElements(By.cssSelector(dpElements));
                            caseSensitiveMatch = true;

                        }

                        if (elements.size() < 1) {
                            dpElements = "input[id*='" + normalizedElementName.replaceAll(" ", "") + "'i]"; //All elements good syntax case insensitive//
                            elements = driver
                                    .findElements(By.cssSelector(dpElements));
                            caseSensitiveMatch = true;

                        }

                        if (elements.size() < 1) {
                            dpElements = "input[type*='" + normalizedElementName.replaceAll(" ", "") + "'i]"; //All elements good syntax case insensitive//
                            elements = driver
                                    .findElements(By.cssSelector(dpElements));
                            caseSensitiveMatch = true;
                        }

                        if (elements.size() < 1) {
                            dpElements = "(//input[@type='" + normalizedElementName.toLowerCase() + "'])";
                            elements = driver.
                                    findElements(By.xpath(dpElements));
                        }

                    }


                    break;

                case "checkbox":
                    //todo common checkbox properties
                    //name(contains), id(contains), text()
                    dpElements = "(//input[contains(@name, '" + normalizedElementName + "')])";
                    elements = driver
                            .findElements(By.xpath(dpElements));

                    if (elements.size() < 1) {
                        dpElements = "(//input[contains(@id, '" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    //Replace with underscores
                    if (elements.size() < 1) {
                        //report ("Underscore normalization: " + normalizedElementName.replace(" ", "_"));
                        dpElements = "(//input[contains(@id, '" + normalizedElementName.replace(" ", "_").toLowerCase() + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }


                    if (elements.size() < 1) {
                        dpElements = "(//input[contains(text(), '" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    //Replace with underscores
                    if (elements.size() < 1) {
                        report(normalizedElementName.replace(" ", "_"));
                        dpElements = "(//input[contains(@name, '" + normalizedElementName.replace(" ", "_").toLowerCase() + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    //Last resort - text
                    if (elements.size() < 1) {
                        dpElements = "(//label[contains(text(), '" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    if (elements.size() < 1) {
                        dpElements = "//input[text()='" + normalizedElementName + "']";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }

                    //Css text() equivalent data-content

                    //May only work on certain browser - Exact match
                    if (elements.size() < 1) {
                        dpElements = "input[data-content='" + normalizedElementName + "'i]"; //All elements good syntax case insensitive//
                        elements = driver
                                .findElements(By.cssSelector(dpElements));
                    }

                    //May only work on certain browser - Close match
                    if (elements.size() < 1) {
                        dpElements = "input[data-content*='" + normalizedElementName + "'i]"; //All elements good syntax case insensitive//
                        elements = driver
                                .findElements(By.cssSelector(dpElements));
                        caseSensitiveMatch = true;
                    }

                    if (elements.size() < 1) {
                        report("   *** Class switching CHECKBOX to ALL ***");
                        bypassToAll = true;

                    } else {
                        break;
                    }

                case "column":
                    dpElements = "(//th[@id = '" + normalizedElementName.toLowerCase() + "'])";
                    elements = driver
                            .findElements(By.xpath(dpElements));

                    if (elements.size() < 1) {
                        dpElements = "(//th[contains(@id, '" + normalizedElementName.toLowerCase() + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                    }


                case "text":
                    if (!bypassToAll) {

                        //NoSuchSessionException - Browser refreshed?
                        dpElements = "(//span[contains(text(),'" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));

                        // Check if it's in Div element
                        if (elements.size() < 1) {
                            dpElements = "(//span[contains(@elementName, '" + normalizedElementName + "')])";
                            elements = driver
                                    .findElements(By.xpath(dpElements));
                            // print("Looking through " + elements.size() + " elementName/divs text");
                        }

                        // Check if it's in span element
                        if (elements.size() < 1) {
                            dpElements = "(//div[contains(@elementName, '" + normalizedElementName + "')])";
                            elements = driver
                                    .findElements(By.xpath(dpElements));
                            // print("Looking through " + elements.size() + " elementName/spans text");
                        }

                        // Check if it's in P(aragraph) text //TestGuild.com
                        if (elements.size() < 1) {
                            dpElements = "(//p[contains(text(), '" + normalizedElementName + "')])";
                            elements = driver
                                    .findElements(By.xpath(dpElements));
                            // print("Looking through " + elements.size() + " text/divs text");
                        }

                        // Check if it's in div text
                        if (elements.size() < 1) {
                            dpElements = "(//div[contains(text(), '" + normalizedElementName + "')])";
                            elements = driver
                                    .findElements(By.xpath(dpElements));
                            // print("Looking through " + elements.size() + " text/divs text");
                        }

                        // Check if it's in h1 text (Paylocity)
                        if (elements.size() < 1) {
                            dpElements = "(//h1[contains(text(), '" + normalizedElementName + "')])";
                            elements = driver
                                    .findElements(By.xpath(dpElements));
                            // print("Looking through " + elements.size() + " text/divs text");
                        }

                        // Check if it's in h3
                        if (elements.size() < 1) {
                            dpElements = "(//h3[contains(text(), '" + normalizedElementName + "')])";
                            elements = driver
                                    .findElements(By.xpath(dpElements));
                            // print("Looking through " + elements.size() + " text/divs text");
                        }

                        // Check if it's a label (Applitools)
                        if (elements.size() < 1) {
                            dpElements = "(//label[contains(text(), '" + normalizedElementName + "')])";
                            elements = driver
                                    .findElements(By.xpath(dpElements));
                            // print("Looking through " + elements.size() + " text/divs text");
                        }


                        //report ("Searching " + elements.size() + " text(s).");
                        if (elements.size() != 0) {
                            break;
                        } else {
                            report("   *** Class switching TEXT to ALL ***");
                        }

                    }

                case "all":
                    //dpElements = "(//*[contains(text(),'" + normalizedElementName + "')])";
                    //elements = driver
                    //        .findElements(By.xpath(dpElements));

                    //https://caniuse.com/#feat=css-case-insensitive
                    //Source to case insensitive CSS

                    //if (elements.size() < 1) {
                    //dpElements = "a[href='" + normalizedElementName + "' i]"; Good syntax - finds nothing
                    //dpElements = "a:contains('" + normalizedElementName + "')"; bad syntax
                    //dpElements = "a:contains(" + normalizedElementName + ")"; bad syntax
                    //dpElements = "a:contains('^" + normalizedElementName + "$')"; //bad syntax//
                    //dpElements = "innertext:contains(\"^" + normalizedElementName + "$\")"; //bad syntax//
                    //dpElements = "input[name='" + normalizedElementName + "']"; //bad syntax//

                    //dpElements = "a[id*='" + normalizedElementName + "']"; //good syntax//
                    //dpElements = "a[text*='" + normalizedElementName + "']"; //good syntax//
                    //dpElements = "a[innertext*='" + normalizedElementName + "']"; //good syntax//
                    //dpElements = "a[innertext*='" + normalizedElementName + "']"; //good syntax//
                    //dpElements = "a[href*='" + normalizedElementName.toLowerCase() + "']"; //good syntax//
                    //dpElements = "a[href*='" + normalizedElementName + "'i]"; //good syntax caseinsensitive// ******
                    //dpElements = "a[innertext*='" + normalizedElementName + "'i]"; //good syntax case insensitive//
                    //dpElements = "a[text*='" + normalizedElementName + "'i]"; //Link good syntax case insensitive//

                    //Did not find h3

                    dpElements = "*[href*='" + normalizedElementName + "'i]"; //All elements good syntax case insensitive//
                    elements = driver
                            .findElements(By.cssSelector(dpElements));
                    caseSensitiveMatch = true;

                    if (elements.size() < 1) {
                        dpElements = "*[name='" + normalizedElementName + "'i]"; //All elements good syntax case insensitive//
                        elements = driver
                                .findElements(By.cssSelector(dpElements));
                    }

                    if (elements.size() < 1) {
                        dpElements = "*[name*='" + normalizedElementName + "'i]"; //All elements good syntax case insensitive//
                        elements = driver
                                .findElements(By.cssSelector(dpElements));
                    }

                    if (elements.size() < 1) {
                        dpElements = "*[id='" + normalizedElementName + "'i]"; //All elements good syntax case insensitive//
                        elements = driver
                                .findElements(By.cssSelector(dpElements));
                    }

                    //Experiment - get all elements with xPath
                    if (elements.size() < 1) {
                        dpElements = "(//*[contains(text(), '" + normalizedElementName + "')])";
                        elements = driver
                                .findElements(By.xpath(dpElements));
                        // print("Looking through " + elements.size() + " text/divs text");
                    }


                    break;

                case "none":

                    //Sibling skip
                    report("No '" + elementType + "' sibling '" + s.getElementName() + "' sibling element found.");
                    break;

                default:
                    report("Unknown element type: '" + s.getElementName() + "' '" + elementType + "'");


            }

            //NullPointerException here due to Blank Verb and Element Type

            try {
                cntElements = elements.size();
            } catch (Exception e) {
                cntElements = 0;
            }

            success = false;

            if (cntElements > 0) {
                break;
            }
            //Loop around and try the next frame
            //dpFrame = "//iFrame[" + thisFrame + "]";
            //driver.switchTo().defaultContent();
        }


//TODO: Find Close Match by individual words with (.)

        //Split text
        //Loop through all match elements
        //  Add
        //Select element


        int cntVisible = 0;

        switch (cntElements) {

            case 0:
                debug("   *** FAILURE: No '" + s.getElementName() + "' " + elementType + "s were found.");

                //Prevent a Null pointer Exception
                caseSensitiveMatch = false;

                //ToDo: Not if Verify
                if (!s.getVerb().toLowerCase().contains("verify")) {
                    //Exclude Verify Negative Test -
                    //Expected result could be object does not appear

                    //to do: class switching fail
                    //framework.setAlreadyFailed(true);

                }

                break;

            case 1:
                debug ("   *** one unique '" + s.getElementName() + "' " + elementType + ".");
                break;

            default:
                WebElement highlightedElement = null;
                WebElement highlightedCloseMatchElement = null;
                //ignore for text
                if (elementType.toLowerCase() != "text") {
                    //Subtract the count of non visible objects
                    int nextElement;

                    //*******************************************************************
                    //  Search for the first visible element
                    //  That is not the prior element
                    //      - Click login link to expand window, then click Login button

                    //Start changed from nextElement+1
                    for (nextElement = 0; nextElement < cntElements; nextElement++) {

                        try {

                            //Fast check
                            isDisplayed = elements.get(nextElement).getSize().width > 4;  //fast

                            if (isDisplayed) {
                                cntVisible++;

                                if (cntVisible == 1) {
                                    highlightedElement = elements.get(nextElement);
                                    highlight(driver, highlightedElement, "blue");

                                } else {
                                    //Highlight visible close match
                                    highlightedCloseMatchElement = elements.get(nextElement);
                                    flash(driver, highlightedCloseMatchElement, "orange");
                                    flash(driver, highlightedCloseMatchElement, "orange");
                                }

                            } else {
                                //Slow double check
                                isDisplayed = elements.get(nextElement).isDisplayed();

                                if (!isDisplayed) {
                                    cntVisible++;

                                    if (cntVisible == 1) {
                                        highlightedElement = elements.get(nextElement);
                                        highlight(driver, highlightedElement, "blue");
                                    } else {
                                        //Highlight visible close match
                                        flash(driver, highlightedCloseMatchElement, "orange");
                                        flash(driver, highlightedCloseMatchElement, "orange");
                                    }

                                }

                            }

                        } catch (Exception e) {
                            //cntVisible--;
                            //Eat the error
                        }

                    }

                    if (cntVisible > 1) {

                        String msg;
                        msg = "   *** WARNING: " + Integer.toString(cntVisible) ;
                        msg += " '" + s.getElementName() + "' ";
                        msg += s.getElementType() + "s matched. ***";
                        report (msg);
                        //report("   *** WARNING: " + cntVisible + " '" + s.getElementName() + "' " + s.getElementType() + "s matched. ***");
                        unhighlight(driver, highlightedElement);
                    }

                }
        }


        if (cntElements > 0) {

            for (thisElement = 0; thisElement < cntElements; thisElement++) {

                try {

                    //Fast check
                    //int width = elements.get(thisElement).getSize().width;
                    int height = elements.get(thisElement).getSize().height;

                    isDisplayed = height > 4;  //fast

                    if (isDisplayed == false) {
                        //Slow double check if fast check is not visible
                        isDisplayed = elements.get(thisElement).isDisplayed();
                    }

                } catch (Exception e) {
                    isDisplayed = false;
                }

                //Skip if this the same element that was just clicked with the same name.
                //https://www.independenthealth.com/
                // Log In link opens credential drop down with log in button

                //Todo : priorClickHoverElementAvailable resets to false.

                if (isDisplayed && priorClickHoverElementAvailable) {
                    if (priorClickHoverElement == elements.get(thisElement)) {
                        report("   *** The same element was identified and skipped. ***");
                        isDisplayed = false;
                    }
                }

                if (isDisplayed) {

                    //Cature this first visible image
                    visibleElementIndex = thisElement;

                    //Send this to output
                    if (thisElement == 0) {
                        debug("   *** xpath/CSS: " + dpElements);
                    } else {
                        debug("   *** xpath/CSS: " + dpElements + " [" + thisElement + "]");
                    }

                    //Support prior object reference
                    this.priorElementAvailable = true;
                    this.priorElement = currentElement;
                    currentElement = elements.get(thisElement);

                    //Support navigation from one element that reaveals another element with the same name
                    //Click Login In (Link), Enter credentials, Click Login (Button)
                    if (s.getVerb().toLowerCase().contentEquals("click") || s.getVerb().toLowerCase().contentEquals("hover")) {
                        priorClickHoverElementAvailable = true;
                        priorClickHoverElement = currentElement;
                    }

                    lastElementName = elementName;
                    lastElementType = elementType;

                    currentElementText = this.currentElement.getText();

                    success = true;
                    found = true;


                    // No more to search
                    break;


                }
            }
        }

        if (success) {

            visibleElementIndex = thisElement;

            if (thisElement > 0) {
                report("Element " + (thisElement + 1) + ":  '" + s.getElementName() + "' " + elementType + " was the first visible element.\n");
            }
/*
        }else{

            if (cntElements == 0) {
                report("No '" + s.getElementName() + "' " + elementType + "s were found\n");
            }else {
                report("None of the " + cntElements + " '" + s.getElementName() + "' " + elementType + "s were visible\n");
            }
*/

        }

        return success;

    }


    private void printParsedText() {

        //Skip if this is Feature: Background: or Scenario:
        if (s.getSentence().indexOf(":") > 0) {
            debug((s.getVerb() + " '" + s.getElementName() + "' " + s.getElementType() + " " + s.getVerify() + " " + s.getData() + "\n"));

        } else {

            debug("NLS/MOM Breakdown\n" +
                    "          Verb        : " + s.getVerb()+ "\n" +
                    "          Element Name: " + s.getElementName()+"\n" +
                    "          Element Type: " + s.getElementType()+"\n" +
                    "          Verify      : " + s.getVerify()+"\n" +
                    "          Data        : " + s.getData()); //Space out results
        }

    }

    private void clearParsedText() {
        //Skip if this is Feature: Background: or Scenario:
        //framework.appendResults ( "Cleared Parsed text...\n");

        s.setVerb("");
        s.setElementName("");
        s.setElementType("");
        s.setVerify("");
        s.setData("");
    }


    //TBD - Support a Global Project object normalization
    private String normalizeWebElement(String elementName, String elementType, String normalizedName) {
        if (normalizedName != "") {
            return (normalizedName);
        } else {
            return (elementName);
        }

    }

    //A wrapper for debug statements

    private static void debug (String text){
        //Implement cool debugging here.
        //Leave out of reporting

        //Basic: Send to the console with a minimized
        if (text.length() > 0) {
            System.out.println("\n  DEBUG: " + text);
            if (text.length() < 5) {
                System.out.println("***DEBUG: {short string passed} :" + text.length());
            }
        }else{
            System.out.println("***DEBUG: {Empty string passed}");
        }
    }

    //Add to TestProject results output
    public void report(String text) {
        finalTestResults += text;
        //Sends out to Reporter
        SoftAssert softAssert = new SoftAssert();

        try {

            //Null Pointer exception
            //fw.appendStepResult(text);

            //Full results report

            //Branch on "PASS: " and "FAIL:" in text and send to SoftAsserts
            if (text.toLowerCase().indexOf("pass ") == 1) {
                softAssert.assertTrue(true, text);

            } else if (text.toLowerCase().indexOf("fail ") == 1) {
                softAssert.assertTrue(false, text);

                //Take screen capture
                //https://sqa.stackexchange.com/questions/36253/taking-screenshot-on-test-failure-selenium-webdriver-testng
                fw.appendStepResult("\n" + "  *** Failure detected ***");
                String fileName = String.format("Screenshot-%s.jpg", Calendar.getInstance().getTimeInMillis());

                TakesScreenshot ts = (TakesScreenshot) driver;

                File srcFile = ts.getScreenshotAs(OutputType.FILE);
                File destFile = new File("./screenshots/" + fileName);

                try {

                    FileUtils.copyFile(srcFile, destFile);
                    debug(" *** Screenshot taken : " + destFile);

                } catch (IOException e) {
                    debug(" *** Screenshot failure : " + destFile);
                    e.printStackTrace();
                }

            } else {

                //Non-validation statement
                debug(text);

            }

            softAssert.assertAll();

        }catch (Exception e) {
                //Eat the null exception pointer error
                debug("*** Empty/Null string sent to Report ***");
                e.printStackTrace();
        }



    }

    /**
     * Waits for page build, optionally will wait for text to appear first, for
     * spinner. //Wait time is 3 X longest wait time of 3 sec.
     * <p>
     * WWW 1r
     *
     * @param waitForString - Name of the Field to return
     * @return always true if no text; true if text found, false if not
     * @Author Paul Grossman Date: 6/12/2019
     */

    private boolean pageSync(WebDriver driver, String waitForString) {

        boolean success = false;

        List elements;
        int cnt = 0;
        int oldCnt = 0;
        int retry = 0;
        String syncResult = "Page Sync detail:\n";
        int maxWaitTimeInSeconds = 9; // seconds

        // Option to wait for some text to appear before waiting for the page build to
        // complete

        //All elements - slow
        //elements = driver
        //        .findElements(By.cssSelector("*"));

        //all elements
        //elements = driver
        //        .findElements(By.xpath("//*"));

        elements = driver
                .findElements(By.xpath("(//div)"));

        oldCnt = elements.size();

        //  report("   *** Page sync on object old count: " + oldCnt + " ***");


        sleep(100);
        //Wait for page build to start

        if (waitForString != "") {
            syncResult += "PageSync waiting for '" + waitForString + "' ...\n";
            success = waitFor(driver, waitForString, maxWaitTimeInSeconds); //9 seconds

            if (!success){
                syncResult += "  *** PageSync '" + waitForString + "' never appeared\n";
            }

        } else {
            success = false;
        }

        if (!success) {

            js = driver;
            int iQuarterSecond;
            //Wait up to 30 seconds for page build to complete.
            //Check 4 times for second

            StopWatch pageLoad = new StopWatch();
            pageLoad.start();

            int waitTime = 200;


            for (iQuarterSecond = 1; iQuarterSecond < 121; iQuarterSecond++) {
                //Check every 1/4 second

                //VS sync in 8-12 sec with scroll
                //elements = driver
                //        .findElements(By.xpath("//div"));

                //Lost session
                //NoSuchSessionException

                //Count the current number of page Links
                elements = driver
                        .findElements(By.xpath("//a"));
                cnt = elements.size();


                if (oldCnt == cnt) {
                    //Stable count
                    retry++;
                    waitTime = 150;
                    syncResult +="          Sync: " + retry + " : " + oldCnt + " == " + cnt + "  waitTime: " + waitTime+" (Stable)\n";

                } else {
                    //Link count changed
                    retry = 0;
                    waitTime += 50; //wait a little longer

                    syncResult +="          Sync: " + retry + " : " + oldCnt + " == " + cnt + "  waitTime: " + waitTime+" (Unstable)\n";
                }

                oldCnt = cnt;

                // Must have 3 times stable counts of page links
                if (retry > 3) {

                    //Continue even if page is still loading
                    //pageLoad.stop();

                    if (pageLoad.getTime() / 1000 < 2) {
                        syncResult += "           *** Page sync completed within  1 second. ***\n";

                    } else {
                        syncResult += "           *** Page sync completed in " + (pageLoad.getTime() / 1000) + " seconds. ***\n";
                    }

                    //Skip the final 1/4 sec sleep
                    break;
                }

                //Wait 1/4 of a second more or less each loop
                sleep(waitTime);

            }

            //Final optional backup page sync: Is the readyState complete?
            waitTime = QUARTER_SECOND;

            //Old school approach: 30 seconds
            for (iQuarterSecond = 1; iQuarterSecond < 121; iQuarterSecond++) {
                //Check every 1/4 second

                //To check page ready state.
                if (js.executeScript("return document.readyState").toString().equals("complete")) {
                    syncResult +=("          Old School Page Sync: JS says Browser readyState is complete");
                    //result = "Page Sync browser ready";
                    break;

                } else {
                    syncResult +=("          Old School Page Sync: JS says Browser readyState is NOT complete");
                }

                sleep(waitTime); //Quarter second

            }

            //    Assert.fail("Timeout waiting for Page Load Request to complete.");
        }

        debug (syncResult);


        return success;

    }


    /**
     * Waits for page build or text for spinner if passed.
     *
     * @param expectedText - waits for the text to appear for up to five seconds.
     *                     Performs 4 checks per second for quickest response
     *                     time.Reports if text was not found in the amount of time
     * @param timeout      - the location of the image, relative to the uii argument
     * @return true if text found, false if not
     * @Author Paul Grossman Date: 6/12/2019
     */

    private boolean waitFor(WebDriver driver, String expectedText, int timeout) {
        boolean success = false;
        boolean negativeTest = false;

        //report ("Waiting for '" + expectedText + "' ...");

        // negative number is waitFor negative test - text should not appear
        if (timeout < 0) {
            timeout = Math.abs(timeout);
            negativeTest = true;
            report("Expecting '" + expectedText + "' NOT to appear in " + timeout + " seconds.");

        } else {
            // Default timeout is 9 second page build (Triple 3-second average)
            if (timeout < 9) {
                timeout = 9;
            }

            report("Expecting '" + expectedText + "' to appear in " + timeout + " seconds.");
        }

        int initialTimeout = timeout;
        timeout = timeout * 1000; // Convert to milliseconds

        do {

            if (timeout < 0) {
                break;
            }

            // Do not breakpoint on this line - debug has long response
            success = driver.getPageSource().contains(expectedText);
            timeout = timeout - QUARTER_SECOND; // Retry 4 X a second
            // Wait 1/4 second to try again
            sleep(QUARTER_SECOND);

        } while (!success);

        if (success) {
            if (negativeTest) {
                report("Text unexpectedly appeared within " + (initialTimeout - (timeout / 1000)) + " seconds.");
            } else {
                report("Expected text appeared within " + (initialTimeout - (timeout / 1000)) + " seconds.");
            }

        } else {
            if (negativeTest) {
                report("Text did not appear within " + (initialTimeout - (timeout / 1000)) + " seconds as expected.");
            } else {
                report("Expected text did not appeared within " + (initialTimeout - (timeout / 1000)) + " seconds.");
            }
        }

        return success;

    }

    //Highlight close matches that appear on screen - skip the firstmatch
    private void highlightCloseMatches(WebDriver driver) {
        double abs_y;

        //Speed - reference the Y and visibility
        //double[] arrElementY = new double[elements.size()];
        boolean[] arrElementVisible = new boolean[elements.size()];

        int loop = 0;

        //Images only for now
        if (s.getElementType().toLowerCase().contains("image")) {
            boolean elementsToHighlight = false;
            int thisElementIndex; //index
            WebElement element;

            //highlight visible close matches 5 times
            for (loop = 0; loop < 5; loop++) {
                //Turn on Highlight all elements in viewport in Yellow except last image
                for (thisElementIndex = 0; thisElementIndex < elements.size(); thisElementIndex++) {

                    //if (loop == 0) {
                    element = elements.get(thisElementIndex);

                    if (isInViewport(element)) {
                        arrElementVisible[thisElementIndex] = true;

                        //skip the chosen element
                        if (thisElementIndex != visibleElementIndex) {
                            highlight(driver, element, "yellow");
                            elementsToHighlight = true;
                        } else {
                            //Skip the selected visible element
                            arrElementVisible[thisElementIndex] = false;
                        }
                    }
                    //}

                    //if (arrElementVisible[thisElementIndex] = true) {

                }

                if (elementsToHighlight) {
                    sleep(HALF_SECOND);
                }

                //Turn off Highlight all elements in viewport in Yellow except last image
                for (thisElementIndex = 0; thisElementIndex < elements.size(); thisElementIndex++) {

                    if (arrElementVisible[thisElementIndex]) {
                        element = elements.get(thisElementIndex);
                        highlight(driver, element, "lightgreen");
                    }

                }

                //Repeat
                if (elementsToHighlight) {
                    sleep(HALF_SECOND);
                }

            }
        }

    }

    private void highlightAll(WebDriver driver, List elements) {
        int el;
        Object thisEl;
        for (el = 0; el < elements.size(); el++) {

            thisEl = elements.get(el);
            highlight(driver, (WebElement) thisEl, "blue");
        }


    }

    //ToDo: Melt highlight in separate thread
    public void highlight(WebDriver driver, WebElement element, String color) {

        boolean isSeen = false;

        try {
            //isSeen = element.isDisplayed();  //slow
            isSeen = element.getSize().width > 0;  //fast
        } catch (Exception e) {
            //Eat StaleElementReferenceException
            //e.reportStackTrace();
        }

        if (isSeen) {

            //js.executeScript("arguments[0].scrollIntoView(false);", currentElement);
            //this.currentElement.sendKeys(Keys.PAGE_UP);
            //currentElement.sendKeys(Keys.ARROW_DOWN);

            try {

                for (int i = 1; i < 6; i++) {

                    js.executeScript("arguments[0].setAttribute('style', 'border: "+ i +"px solid " + color + ";');", element);
                    sleep (75); //Demo only
                }

            } catch (Exception e) {
                //StaleElementReferenceException
                //e.printStackTrace();
            }

            //}else{
            //report ("Next Element");
            //report (String.elementNameOf(element.getSize().height));
            //report (String.elementNameOf(element.getSize().getHeight()));
            //report (String.elementNameOf(element.getSize().width));
            //report (String.elementNameOf(element.getSize().getWidth()));
        }

    }

    public void unhighlight(WebDriver driver, WebElement element) {

        try {

            js.executeScript("arguments[0].setAttribute('style', 'border: 0px solid black;');", element);

        } catch (Exception e) {
            //StaleElementReferenceException
            //e.printStackTrace();
        }

    }

    private void flash(WebDriver driver, WebElement element, String color) {
        highlight(driver, element, color);
        unhighlight(driver, element);
    }


    private void unhighlightAll(WebDriver driver, List elements) {
        int el;
        Object thisEl;
        for (el = 0; el < elements.size(); el++) {

            thisEl = elements.get(el);
            unhighlight(driver, (WebElement) thisEl);
        }

    }


    public void flashPass(WebDriver driver, WebElement element) {

        //JavascriptExecutor js = driver;

        //js.executeScript("arguments[0].scrollIntoView(false);", currentElement);
        //this.currentElement.sendKeys(Keys.PAGE_UP);

        if (scrollIntoView(driver, element)) {

            //wait for image to scroll
            //sleep (QUARTER_SECOND);

            //typeAtBrowser(driver, "ARROW KEY");
            //    Actions action = new Actions(driver);
            //    action.sendKeys(Keys.ARROW_DOWN).build().perform();

            highlight(driver, element, "green");

            report("PASS: " + s.getSentence());
            /*
            js.executeScript("arguments[0].setAttribute('style', 'border: 5px solid green;');", element);
            sleep(10);
            js.executeScript("arguments[0].setAttribute('style', 'border: 0px solid black;');", element);
            sleep(10);
            js.executeScript("arguments[0].setAttribute('style', 'border: 5px solid green;');", element);
            sleep(10);
            js.executeScript("arguments[0].setAttribute('style', 'border: 0px solid black;');", element);
            sleep(10);
            js.executeScript("arguments[0].setAttribute('style', 'border: 5px solid green;');", element);
            //sleep(500);
            */

            //Demo only
            sleep(1000);
            unhighlight(driver, element);

//        js.executeScript("arguments[0].setAttribute('style', 'border: 0px solid black;');", element);

            //Take picture only local
            //takeSnapShot(driver,"..//momSnapshot.png");
        }

    }

    public void flashFail(WebDriver driver, WebElement element) {

        JavascriptExecutor js = driver;

        //js.executeScript("arguments[0].scrollIntoView(false);", currentElement);
        //this.currentElement.sendKeys(Keys.PAGE_UP);
        //currentElement.sendKeys(Keys.ARROW_DOWN);

        if (scrollIntoView(driver, element)) {
/*
            js.executeScript("arguments[0].setAttribute('style', 'border: 5px solid red;');", element);
            sleep(50);
            js.executeScript("arguments[0].setAttribute('style', 'border: 0px solid black;');", element);
            sleep(25);
            js.executeScript("arguments[0].setAttribute('style', 'border: 5px solid red;');", element);
*/
            //Flash there error ellement in red 3 times
            for (int iCycle = 1; iCycle < 4; iCycle++) {
                for (int i = 1; i < 6; i++) {
                    js.executeScript("arguments[0].setAttribute('style', 'border: " + i + "px solid red;');", element);
                    sleep(75); //Demo only
                }

                for (int i = 5; i > 0; i--) {
                    js.executeScript("arguments[0].setAttribute('style', 'border: " + i + "px solid red;');", element);
                    sleep(75); //Demo only
                }
            }

            //leave the red highight on
            for (int i = 5; i > 0; i--) {
                js.executeScript("arguments[0].setAttribute('style', 'border: " + i + "px solid red;');", element);
                sleep(75); //Demo only
            }


            //Take picture
            //takeSnapShot(driver, "..//momSnapshot.png");

            report("FAIL: " + s.getSentence());

            //pause for 3 seconds to show errors
            sleep(3000);
            //js.executeScript("arguments[0].setAttribute('style', 'border: 0px solid black;');", element);
        }

    }

    public void flashCloseMatch(WebDriver driver, WebElement element) {

        JavascriptExecutor js = driver;

        //js.executeScript("arguments[0].scrollIntoView(false);", currentElement);
        //this.currentElement.sendKeys(Keys.PAGE_UP);
        //currentElement.sendKeys(Keys.ARROW_DOWN);

        if (scrollIntoView(driver, element)) {

            js.executeScript("arguments[0].setAttribute('style', 'border: 5px solid seagreen;');", element);

            //Take picture
            //takeSnapShot(driver,"..//momSnapshot.png");

            //sleep(50);
            //js.executeScript("arguments[0].setAttribute('style', 'border: 0px solid black;');", element);
        }

    }


    public boolean scrollIntoView(WebDriver driver, WebElement element) {

        //report ("Started scrolling");
        //Dimension d = driver.manage().window().getSize();
        //int h = d.getHeight();
        double last_y;
        double elementHeight;
        double lastElementHeight;
        //Get the y co-ordinate
        boolean goodState = true;

        double abs_y = -1;

        try {

            //Get the current position of the element in the window
            //Returns 10000000 if StaleElementReferenceException
            abs_y = getElementY(element);

            //report (String.valueOf(getElementY(element)));

            goodState = (abs_y != -1000000);

            if (goodState) {
                //Scroll only if not on screen
                if ((abs_y > viewportHeight) ||  (abs_y < 0)) {

                    //Does not work on Fields
                    switch (s.getElementType()) {
                        case "field":
                            //scroll up
                            js.executeScript("window.scrollBy(0, 150)");
                            break;
                        default:
                            //Scroll the element into view
                            js.executeScript("arguments[0].scrollIntoView(false);", element);
                    }
                    //Wait for the scroll to complete
                    sleep(300);
                }



                //skip if weblist
                //report (currentElementType);
                //report (lastElementType);
                //report (this.currentElementType);
                //report (this.lastElementType);



                //if (lastElementType.toLowerCase().contains("list")){
                //Remove focus - don't sent scroll to an open weblist
                js.executeScript("window.focus();");
                js.executeScript("arguments[0].blur(false);", element);

                //lastElementHeight = this.currentElement.getSize().height;

                //js.executeScript("arguments[0].scrollIntoView(false);", element);

                //sleep(100);  //Wait a moment for the scroll to complete

                //elementHeight = this.currentElement.getSize().height;

                //Loop until Element Y + Element H > Screen height
                //Wait for scroll to complete

                //while (lastElementHeight != this.currentElement.getSize().height) {
                //    lastElementHeight = elementHeight;
                //    sleep(100);
                //}

                //Get the new screen location
                abs_y = getElementY(element);


                //If element is above the middle of the view port, scroll down
                while (abs_y < viewportHeight / 2) {
                    last_y = abs_y;

                    //typeAtBrowser(driver, "Down key");  //Interferes with Radio buttons
                    //typeAtBrowser(driver, "pgdn");
                    js.executeScript("window.scrollBy(0,-130)"); // -150 or speed -30 for demo
                    elementHeight = this.currentElement.getSize().height;

                    abs_y = getElementY(element);
                    //debug ("scrolling up");
                    if (last_y == abs_y) {
                        //Exit if the element is at the very top and can't scroll down any furthur
                        break;
                    }
                }

                //If element is below the bottom third of the view port, scroll up
                while (abs_y > (viewportHeight / 3)) {
                    last_y = abs_y;
                    //typeAtBrowser(driver, "Up key");   //Interferes with Radio buttons
                    //typeAtBrowser(driver, "pgup");
                    //elementHeight = this.currentElement.getSize().height;
                    js.executeScript("window.scrollBy(0,130)"); //500 for speed, 50 for demo
                    abs_y = getElementY(element);
                    //debug ("scrolling down");
                    if (last_y == abs_y) {
                        //Exit if the elment is at the very botton and can't scroll up any more
                        break;
                    }
                }

                //Fine elmement adjustment to upper third od screen if possible
                //typeAtBrowser(driver, "Down key");
                //typeAtBrowser(driver, "Down key");

                //Focus the element
                js.executeScript("arguments[0].focus;", element);

                //report(String.valueOf(getElementY(element)));
                //}


                //Demo only - wait for scroll to complete
                //sleep(500);
                //typeAtBrowser(driver, "Down key");

                //while (abs_y > h) {
                //report("Scrolling..." + abs_y + " > " + h);
                //    sleep(400);
                //    abs_y = (double) js.executeScript("return arguments[0].getBoundingClientRect().top;", currentElement);
                // }
                //report("Scrolling Done " + abs_y + " < " + h);
            }

        } catch (Exception e) {
            e.printStackTrace();  //IllegalMonitorExceptionState
            //goodState = false;
        }

        return goodState;

    }


    private int getElementWidth(WebElement element) {
        //Stale Element reference
        int width = 0;

        //Fast check

        try {
            //    width = (int) js.executeScript("return arguments[0].getBoundingClientRect().width;", element);
            width = element.getSize().width;  //fast
        } catch (Exception e) {
            //Stale Element reference
            //e.printStackTrace();
        }
        return width;
    }


    private double getElementY(WebElement element) {
        //Stale Element reference
        double top = -1000000;

        try {
            top = (double) js.executeScript("return arguments[0].getBoundingClientRect().top;", element);
        } catch (Exception e) {

            try {

                //Double cannot be cast to java.base/java.lang.Long
                top = (long) js.executeScript("return arguments[0].getBoundingClientRect().top;", element);

            } catch (Exception e2) {
                //Stale Element reference
                e2.printStackTrace();
            }

            //Stale Element reference

            //e.printStackTrace();
        }

        return top;

    }

    private double getElementX(WebElement element) {
        return (double) js.executeScript("return arguments[0].getBoundingClientRect().left;", element);
    }

    private boolean isInViewport(WebElement element) {
        double abs_y;
        //Returns One Million for stale element reference
        abs_y = getElementY(element);

        return (abs_y > 0 && abs_y < viewportHeight);
    }

    // Rule: You can sleep when you're dead!
    private void sleep(int millis) {

        if (millis > 999) {
            debug("Find a better way than sleep(" + millis + ")");
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }

    private String generateXpath(WebElement childElement, String current) {
        String childTag = childElement.getTagName();

        if (childTag.equals("html")) {
            return "/html[1]" + current;
        }

        WebElement parentElement = childElement.findElement(By.xpath(".."));
        List<WebElement> childrenElements = parentElement.findElements(By.xpath("*"));
        int count = 0;

        for (int i = 0; i < childrenElements.size(); i++) {
            WebElement childrenElement = childrenElements.get(i);
            String childrenElementTag = childrenElement.getTagName();

            if (childTag.equals(childrenElementTag)) {
                count++;
            }

            if (childElement.equals(childrenElement)) {
                return generateXpath(parentElement, "/" + childTag + "[" + count + "]" + current);
            }
        }
        return null;
    }


    public boolean refreshCurrentObject() {
        boolean isDisplayed = false;
        boolean success = false;
        int thisElement = 0;

        String elementType = lastElementType;

        //Get a refreshed collection of matching objects
        //Null pointer - driver is null


        if (helper == null) {
            report("   *** Helper support system failed ***");
            fw.setAlreadyFailed(true);
            return false;
        }

        //Re-Init Browser
        WebDriver driver = helper.getDriver();

        elements = driver
                .findElements(By.xpath(dpElements));

        if (elements.size() > 0) {

            for (thisElement = 0; thisElement < elements.size(); thisElement++) {
                // report("Looking through element");
                try {

                    //Fast check
                    isDisplayed = elements.get(thisElement).getSize().width > 0;  //fast

                    if (isDisplayed == false) {
                        //Slow double check
                        isDisplayed = elements.get(thisElement).isDisplayed();
                    }

                } catch (Exception e) {
                    isDisplayed = false;
                }

                if (isDisplayed) {

                    //Cature this first visible image
                    visibleElementIndex = thisElement;
                    //Support prior object reference
                    currentElement = elements.get(thisElement);

                    //flash (driver, currentElement);

                    success = true;
                    found = true;


                    // No more to search
                    break;
                }
            }
        }


        if (success) {
            visibleElementIndex = thisElement;
            report("Refreshed current object Element " + (thisElement + 1) + ":  '" + s.getElementName() + "' " + elementType + " was the 1st visible element.\n");
        } else {
            report("Refresh failed None of the " + elements.size() + " '" + s.getElementName() + "' " + elementType + " were visible\n");
        }

        return true;

    }


    public void swapStrings(String stringA, String stringB) {

        //s.getVerb() = verb;
        String tmpA = "";
        String tmpB = "";

        switch (stringA.toLowerCase()) {
            case "verb":
                tmpB = s.getVerb();
                break;

            case "elementname":
                tmpB = s.getElementName();
                break;

            case "elementtype":
                tmpB = s.getElementType();
                break;
            case "verify":
                tmpB = s.getVerify();
                break;
            case "data":
                tmpB = s.getData();
                break;
        }

        switch (stringB.toLowerCase()) {
            case "verb":
                tmpA = s.getVerb();
                break;

            case "elementname":
                tmpA = s.getElementName();
                break;
            case "elementtype":
                tmpA = s.getElementType();
                break;
            case "verify":
                tmpA = s.getVerify();
                break;
            case "data":
                tmpA = s.getData();
                break;
        }

        switch (stringA.toLowerCase()) {
            case "verb":
                s.setElementName(tmpA);
                break;
            case "elementname":
                s.setElementName(tmpA);
                break;
            case "elementtype":
                s.setElementType(tmpA);
                break;
            case "verify":
                s.setVerify(tmpA);
                break;
            case "data":
                s.setData(tmpA);
                break;
        }

        switch (stringB.toLowerCase()) {

            case "verb":
                s.setElementName(tmpB);
                break;
            case "elementname":
                s.setElementName(tmpB);
                break;
            case "elementtype":
                s.setElementType(tmpB);
                break;
            case "verify":
                s.setVerify(tmpB);
                break;
            case "data":
                s.setData(tmpB);
                break;
        }

        //printParsedText();

    }


    /**
     * This function will take screenshot
     *
     * @param webdriver
     * @param fileWithPath
     * @throws Exception
     */

    public static void takeSnapShot(WebDriver webdriver, String fileWithPath) {
        try {
            //Convert web driver object to TakeScreenshot
            TakesScreenshot scrShot = webdriver;

            //Call getScreenshotAs method to create image file
            File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);

            //Move image file to new destination
            File DestFile = new File(fileWithPath);

            //Copy file at destination
            FileUtils.copyFile(SrcFile, DestFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ArrayList<String> readCsv(String csvFile) {

        //Path wiki_path = Paths.get("", csvFile);

        Charset charset = Charset.forName("ISO-8859-1");
        ArrayList<String> lines = null;

        debug("TEST: " + csvFile);

        try {
            lines = (ArrayList<String>) Files.readAllLines(Paths.get(csvFile), charset);

            for (String line : lines) {
                debug(line);
            }

        } catch (IOException e) {
            //Something is wrong with the file
            report("FAIL: unable to read '" + csvFile +"'");
            report("Tests must be located in the \\Tests directory directly above " + System.getProperty("user.dir") + "\n\n" + e);

        }

        return lines;
    }


}
