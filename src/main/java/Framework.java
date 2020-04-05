import org.openqa.selenium.WebElement;

import java.util.List;

import static java.lang.System.*;
import org.openqa.selenium.interactions.Actions;
//Framework
public class Framework {

    private WebElement mCurrentElement;
    private String mCurrentElementText = "";
    private boolean mAlreadyFailed = false;
    // The prior button, link, field that was identified by getLink(), getButton(),
    // getField()
    // This is used for relative object to object position validation.
    // And I should see a 'Search' button
    // "And it is below the 'Search' field
    private boolean mPriorElementAvailable = false;

    //For object relative validation
    private WebElement mPriorElement;

    //For same named element navigation
    private WebElement mPriorClickHoverElement;


    private String mLastElementName;
    private String mLastElementType;


    private boolean mFound = false;
    private boolean mFind = false;  //Relative element location


    private String mStepResult;
    private String mTestResults;



    private int mVisibleElementIndex;


    public Framework(WebElement currentElement, String currentElementText, boolean alreadyFailed, boolean priorElementAvailable,
                     WebElement priorElement, WebElement priorClickHoverElement, String lastElementName, String lastElementType, boolean found,
                     boolean find , String stepResult, String testResults,
                     int visibleElementIndex, List<WebElement> elements, String dpElements, boolean caseSensitiveMatch,
                     int cntElements, boolean priorClickHoverElementAvailable){

        mCurrentElement = currentElement;
        mCurrentElementText = currentElementText;

        mAlreadyFailed = alreadyFailed;

        mPriorElementAvailable = priorElementAvailable;
        mPriorElement = priorElement;
        mPriorClickHoverElement = priorClickHoverElement;
        mLastElementName = lastElementName;
        mLastElementType = lastElementType;
        mFound = found;
        mFind = find;

        mStepResult = stepResult;
        mTestResults = testResults;

        mVisibleElementIndex = visibleElementIndex;
        //s.setViewport = viewport;
        mDpElements = dpElements;
        mElements = elements;
        //s.setViewportHeight = viewportHeight;
        mPriorClickHoverElementAvailable = priorClickHoverElementAvailable;
        mCaseSensitiveMatch = caseSensitiveMatch;
        mCntElements = cntElements;
    }

    //Contructor
    public Framework(
            boolean alreadyFailed,
            WebElement currentElement,
            String currentElementText,
            boolean priorElementAvailable,
            WebElement priorElement,
            WebElement priorClickHoverElement,
            String lastElementName,
            String lastElementType,
            boolean found,
            boolean find,
            int visibleElementIndex,
            List<WebElement> elements,
            String dpElements,
            boolean caseSensitiveMatch,
            int cntElements,
            boolean priorClickHoverElementAvailable) {
    }


    public WebElement getViewport() {
        return viewport;
    }
    private WebElement viewport;


    //Save off the last good DP to refresh of page updates unexpectedly
    private String mDpElements;

    //Reuse when highlighting multiple matches
    private List<WebElement> mElements = null;
    public  int viewportHeight;

    //private int viewportWidth;  //Future use for resizing if element is not displayed because browser is too small
    private boolean mPriorClickHoverElementAvailable;
    private boolean mCaseSensitiveMatch = false;
    private int mCntElements;

    public WebElement getCurrentElement() {
        return mCurrentElement;
    }

    public void setCurrentElement(WebElement currentElement) {
        mCurrentElement = currentElement;
    }

    public String getCurrentElementText() {
        return mCurrentElementText;
    }

    public void setCurrentElementText(String currentElementText) {
        mCurrentElementText = currentElementText;
    }

    public boolean isAlreadyFailed() {

        if (mAlreadyFailed)
            frameworkDebug ("   *** TEST STEP SKIPPED - ALREADY FAILED ***");
        return mAlreadyFailed;
    }

    //Todo: This is a flag to skip additional test steps methods for fast exit
    public void setAlreadyFailed(boolean alreadyFailed) {
        if (alreadyFailed)
            frameworkDebug ("   *** TEST STEP FAILED ***");
        mAlreadyFailed = alreadyFailed;
    }

    public boolean isPriorElementAvailable() {
        return mPriorElementAvailable;
    }

    public void setPriorElementAvailable(boolean priorElementAvailable) {
        mPriorElementAvailable = priorElementAvailable;
    }

    public WebElement getPriorElement() {
        return mPriorElement;
    }

    public void setPriorElement(WebElement priorElement) {
        mPriorElement = priorElement;
    }

    public WebElement getPriorClickHoverElement() {
        return mPriorClickHoverElement;
    }

    public void setPriorClickHoverElement(WebElement priorClickHoverElement) {
        mPriorClickHoverElement = priorClickHoverElement;
    }

    public String getLastElementName() {
        return mLastElementName;
    }

    public void setLastElementName(String lastElementName) {
        mLastElementName = lastElementName;
    }

    public String getLastElementType() {
        return mLastElementType;
    }

    public void setLastElementType(String lastElementType) {
        mLastElementType = lastElementType;
    }

    public boolean isFound() {
        return mFound;
    }

    public void setFound(boolean found) {
        mFound = found;
    }

    public boolean isFind() {
        return mFind;
    }

    public void setFind(boolean find) {
        mFind = find;
    }

    public void appendStepResult(String stepResult) {
        mStepResult += stepResult + "\n";
        //mTestResults += stepResult + "\n";
        //finalTestResults = mTestResults;
    }


    public void clearStepResult() {
        mStepResult  = "";
    }

    public void appendTestResults(String testResults) {
        mTestResults += testResults + "\n";
        //finalTestResults = mTestResults;
        }

    public String getTestResults() {
        return mTestResults;
    }

    public void clearTestResults() {
        mTestResults  = "";
    }

    public int getVisibleElementIndex() {
        return mVisibleElementIndex;
    }

    public void setVisibleElementIndex(int visibleElementIndex) {
        mVisibleElementIndex = visibleElementIndex;
    }

    public String getDpElements() {
        return mDpElements;
    }

    public void setDpElements(String dpElements) {
        mDpElements = dpElements;
    }

    public List<WebElement> getElements() {
        return mElements;
    }

    public void setElements(List<WebElement> elements) {
        mElements = elements;
    }

    public boolean isPriorClickHoverElementAvailable() {
        return mPriorClickHoverElementAvailable;
    }

    public void setPriorClickHoverElementAvailable(boolean priorClickHoverElementAvailable) {
        mPriorClickHoverElementAvailable = priorClickHoverElementAvailable;
    }

    public boolean isCaseSensitiveMatch() {
        return mCaseSensitiveMatch;
    }

    public void setCaseSensitiveMatch(boolean caseSensitiveMatch) {
        mCaseSensitiveMatch = caseSensitiveMatch;
    }

    public int getCntElements() {
        return mCntElements;
    }

    public void setCntElements(int cntElements) {
        mCntElements = cntElements;
    }

    //Does not print to results "Elsa"
    private static void frameworkDebug(String text) {
        System.out.println(text);
    }

}