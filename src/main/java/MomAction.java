//Version 4.0.0
// - is enabled
// - wrapper for reporting errors

import io.testproject.java.annotations.v2.Action;

import io.testproject.java.sdk.v2.addons.WebAction;
import io.testproject.java.sdk.v2.addons.helpers.WebAddonHelper;

import io.testproject.java.sdk.v2.enums.ExecutionResult;
import io.testproject.java.sdk.v2.exceptions.FailureException;

import java.io.PrintWriter;
import java.io.StringWriter;



import org.openqa.selenium.interactions.Actions;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


//This goes back to TestProject front end

//Data comes from Annotations
//@ElementAction()
//public class momAction implements WebElementAction {
//}

//Description is the Step Name

@Action(name = "Run NLP Command", description = "Execute {{sentence}}", summary = "Execute Natural Language Statement")
public class MomAction extends MomBase implements WebAction {

    public MomAction() {}



    @Override
    public ExecutionResult execute(WebAddonHelper helper) throws FailureException {
        try {
            //reporter.result(results);
            return super.execute(helper);

        } catch (Exception e) {
            //whatever was the error - throw it.
            //fw.nullPointerException
            if (finalTestResults == null) {

                reporter.result("No results recorded");
                //reporter.result("\n\n" + e.getMessage());

            }else{
                //Report the entire test results
                //reporter.result(fw.getTestResults());

                //Workaround - Framework (fw) getting recycled and null
                reporter.result(finalTestResults);
                finalTestResults = ""; //Reset for nxt test. May be redundant
                //reporter.result(framework.getTestResults() + "\n");
            }

            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));

            throw new FailureException("\n\nNLP/MOM Stack trace:\n" + errors.toString());

        }
    }
}

