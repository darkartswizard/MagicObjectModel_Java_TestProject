// Magic Object Model with Natural Language Processing Engine
// Paul Grossman 2020
// TestNG
// jdk-9.0.4

//Version 4.0.0

import ch.qos.logback.core.net.SyslogOutputStream;
import io.testproject.java.enums.AutomatedBrowserType;
import io.testproject.java.sdk.v2.Runner;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.time.StopWatch;

//Use TestNG
import org.testng.ISuite;
import org.testng.annotations.*;

//Selenium support
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
//import java.io.IOException;


import java.io.IOException;

import static momUtilities.Utils.*;

//Local Unit Testing - does not run from TestProject.io

public class Tests {

    private static Runner runner;
    private static StopWatch executionTime = new StopWatch();
    private static WebDriver driver;

    //Developer Key from TestProject
	//1 - Create a Free Forever account at TestProject.io
	//2 - Click the Integrations Tab
	//3 - Click the green Developer Key button
	//4 - Store the token in a new "TESTPROJECT_DEV_TOKEN" System Variable

    private final static String devToken = System.getenv("TESTPROJECT_DEV_TOKEN");


    private static boolean runningFromTestProject = true;

	//Todo: hook into existing browser session
    private boolean restartBrowser = false;

    @BeforeClass
    public void setup() throws InstantiationException {
    
    }

    @BeforeSuite //Tests
    public static void init() throws InstantiationException {
        try {
            log("Launching browser...");
            runner = Runner.createWeb(devToken, AutomatedBrowserType.Chrome);
            log("TestProject Agent is running.");
        } catch (Exception e){
            msgBox ("TestProject Agent is either not running or is out of date. \n\n1) Download the latest testproject.sdk from TestProject.io\n2) Rename it to io.testproject.sdk.java.jar \n3) Replace the one that is in /testProject/SDK folder\n4) Start the Agent.\n\n Then run your test again.\n\nIf you don't have a TestProject Automation Platform account yet, get one: it's free, forever.");
        }

        executionTime.start();

        //try {
        //ToDo: Can't catch this error.
        // javax.ws.rs.ProcessingException: Already connected

        //normalize ("button", "add to Cart", "shopping-cart");
        runningFromTestProject = false;

        //Get the Driver
        driver = runner.getDriver();

        //Todo: Reconnect to last browser for unit testing
        //Resize browser for demo to show real-time logger during demo
        Dimension d1;
        Dimension d2;

        d1 = driver.manage().window().getSize();
        d2 = driver.manage().window().getSize();

        int width = d1.getWidth();
        int height =  d1.getHeight();

        if (width > 1510) {

            width = (int) (width * 0.80);
            height = (int) (height * 0.80);

            //1280 x 720 = 1296 x 736   1920 x 1080
            d2 = new Dimension(width, height);

            driver.manage().window().setSize(d2);

        }



    }


/*    @BeforeTest //Each test
      public void recoverFromError() throws InstantiationException {

        //Do we have an error?
        log ("Checking if prior run had an error.");

        restartBrowser = true; //unit test restart

        if (restartBrowser)
        {
            //driver.close();
            init();
            setup();
            restartBrowser = false;
        }

    }*/


    @AfterSuite
    public static void tearDown() throws IOException {

        //How can I tell if I'm launched from TestProject?
        executionTime.stop();
        log("*** Test execution time: " + executionTime.getTime() / 1000 + " seconds. ***");

        //Demo only - wait to see results on browser
        sleep(5000);

        //Close only if test passed

        //Iterating over each suite included in the test
/*      Suite suites = TestRunner.getSuite();

        for (ISuite suite : suites) {
            //Following code gets the suite name
            String suiteName = suite.getName();
            //Getting the results for the said suite
            Map<String, ISuiteResult> suiteResults = suite.getResults();
            for (ISuiteResult sr : suiteResults.values()) {

                ITestContext tc = sr.getTestContext();
                System.out.println("Passed tests for suite '" + suiteName +
                        "' is:" + tc.getPassedTests().getAllResults().size());
                System.out.println("Failed tests for suite '" + suiteName +
                        "' is:" +
                        tc.getFailedTests().getAllResults().size());
                System.out.println("Skipped tests for suite '" + suiteName +
                        "' is:" +
                        tc.getSkippedTests().getAllResults().size());
                System.out.println("Total excution time for test '" + tc.getName() +
                        "' is:" + (tc.getEndDate().getTime() - tc.getStartDate().getTime()));

            }
        }*/
                runner.close();

        //

    }




//********************************************************
//               Magic Object model (MOM)
//				     	    AND
//			  Natural Language Process Engine
//********************************************************

// The NLS engine parses plain English into five elements
// Verb, ElementName, ElementType, Validation, ExpectedResult

// Example : Verify the 'First name' field contains 'Paul'
// Verb: "Verify", ElementName: "First name", ElementType: "field", Validation: "contains", ExpectedResult: "Paul"

// The NLS engine can also parse plain English in a natual written order
// Verb, Text, ElementName, ElementType
// Example : Enter 'First name' field 'Paul'    	//Difficult to read, but is correct
// Example : Enter 'Paul' into 'First name' field   //Easier to read, out of order

// Single quotes are optional for single word elements
// Example : Enter 02190 into the zipcode field

// Single quotes are optional for validation data
// Example : Verify the address field contains 100 N. Main Steet

// The five elements are then interpreted by the Magic Object model engine
// to return an element and perform the action or validation


// Verbs: 			Open, Verify, Click, Hover, Enter, Type, Select, Mark, Clear
// Element Types: 	Button, Field, Link, Tab, Radiobutton, Checkbox, Text
// Validations: 	is, contains, exists, enabled, disabled, marcked, cleared,
//					does not cotain, does not exist



// The momAction has two forms:

// 1) Five textual elements
//action = new momAction("verify", "Bogus", "text", "exists","");


// 2) Parsing full sentances
// action.setSentence("Set the email field to TheDarkArtsWizard@gmail.com");


// The NLP Engine uses deductive reasoning to iterpret incomplete sentances
//
//       action.setSentence("Click 'Add to cart');


// The NLP Engine supports alternate natural English structure
//
//		In this example: Verb, Text, Element name, Element Type
//       action.setSentence("Enter 'Paul' into the 'First Name' field");


// The NLP Engine is not case sensitive
//
//       action.setSentence("CLICK the 'ADD TO CART' BUTTON");


// The NLS Engine can perform negaive testing
//       action.setSentence("Verify the 'Add to cart' button is not enabled');




// Three sample National Language Processing unit tests
// This is where new functionality can be tested

// These tests do not execute from TestProject
// They mearly excersize the NLP Engine and MOM element factory




    @Test
    public void testTricentis() throws Exception {

        MomAction action = new MomAction();

        action.setSentence("Open sampleapp.tricentis.com/101");
        runner.run(action);

        //Todo  Support click on the named button

        action.setSentence("Click on the Camper link");
        runner.run(action);

        //Not working in Firefox?
        action.setSentence("Select Make list Volvo");
        runner.run(action);

        //Parse from plain English
        action.setSentence("Select Opel from the Make list");
        runner.run(action);

        action.setSentence("Click Yes radiobutton");
        runner.run(action);

        action.setSentence("Select 5 from the 'Number of Seats' list");
        runner.run(action);

    }

	//Sample with base Cucumber styling: Feature, Background, Sceantio

    @Test
    public void testTestGuild() throws Exception {

        MomAction action = new MomAction();
        // Gherkin Cucumber feature file: Feature, Scenario
        // Todo: Gherkin Cucumber feature file: Background
        // Todo: Given, When, Then, And, But
        // Todo: High level statements
        // Todo: Data driven
        action.setSentence("Feature: TestGuild links");
        runner.run(action);

        action.setSentence("Background: Login (High level statements not implemented in NLS/MOM framework yet)");
        runner.run(action);

        action.setSentence("Scenario: Check the header links");
        runner.run(action);

        action.setSentence("Open testGuild.com");
        runner.run(action);

        //Optional Single quotes
        action.setSentence("Verify Disclaimer link exists");
        runner.run(action);

        action.setSentence("Verify 'Disclaimer' link exists");
        runner.run(action);

        action.setSentence("Verify 'logo' image exists");
        runner.run(action);

        action.setSentence("Verify 'You can do better' exists");
        runner.run(action);

        action.setSentence("Verify 'The Third Wave' exists");
        runner.run(action);

        action.setSentence("Click Blog");
        runner.run(action);

        action.setSentence("Click Podcasts");
        runner.run(action);

        action.setSentence("Click About");
        runner.run(action);

        action.setSentence("Click Search");
        runner.run(action);

        action.setSentence("Type ESC");
        runner.run(action);
	}

    @Test
    public void testReadFromCSV() throws Exception {

        //Read in a file to an array
        //loop each sentance in the array

        //MomAction action = new MomAction ("..\\Tests\\MagicObjectModel.csv");

        //action.setSentence("Type ESC");
        //runner.run(action);


    }

    @Test
    public void testCandyMapper() throws Exception {

        log("Unit test: CandyMapper");

        //Local unit test
        MomAction action = new MomAction();

        // -- CANDYMAPPER DEMO --

        action = new MomAction();
        action.setSentence("open www.candymapper.com");
        runner.run(action);

        //Scroll Send button into viww by clicking 'Get in Touch'
        action.setSentence("click 'Get in Touch' button");
        runner.run(action);

        // Populate bad email
        action.setSentence("Set the email field to TheDarkArtsWizard");
        runner.run(action);

        //Populate Name
        action.setSentence("Enter Name field Paul");
        runner.run(action);
        //Populate message field

        action.setSentence("Enter 'Hi Tricentis' into Message field");
        runner.run(action);

        //Click Send button
        action.setSentence("Click the Send button");
        runner.run(action);

        action.setSentence("Verify 'Please enter a valid email address.' text exists");
        runner.run(action);

        //Negative test - Verify Error message does not appear
        action.setSentence("Verify 'Please enter a valid email address.' text does not exist");
        runner.run(action);

        // Populate good email
        action.setSentence("Set the email field to TheDarkArtsWizard@gmail.com");
        runner.run(action);

        //Click Send button
        action.setSentence("Verify the Send button is enabled");
        runner.run(action);

        //Click Send button
        action.setSentence("Click the Send button");
        runner.run(action);

        action.setSentence("Verify 'Thank you for your inquiry! We will get back to you within 48 hours.' text exists");
        runner.run(action);


        //Negative Test Verify non-existant text appears
        //action = new momAction("verify", "Bogus", "text", "exists","");
        //runner.run(action);

        //Verify non-existant text does not appear
        //action = new momAction("verify", "Bogus", "text", "does not exist","");
        //runner.run(action);
    }


    //A wrapper for logging

    private static void log (String text){
		//Implement cool logging here.

		//Basic: Send to the console with a minimized

        try {

            System.out.println("  *** UNIT TEST LOG: " + text);

        }catch (Exception e) {
            //Eat the null exception pointer error
            //Todo: see if we can return the line number where the null string was passed without everything
            // with UncaughtExceptionHandler and e.getCause().getStackTrace()
            //https://stackoverflow.com/questions/23903831/how-can-i-get-the-line-number-where-the-exception-was-thrown-using-thread-uncaug
            System.out.println("  *** Empty/Null string sent to UNIT TEST LOG ***");

        }

    }




    //A wrapper for sleep
	//Used to slow down demo for videos

    private static void sleep(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }



    private static void msgBox(String infoMessage){

        JOptionPane.showMessageDialog(null, infoMessage, "Did you know...", JOptionPane.INFORMATION_MESSAGE);

    }

}

