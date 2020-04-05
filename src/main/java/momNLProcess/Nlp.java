//Sentence class - cleared each step
//Version 4.0.0
package momNLProcess;

public class Nlp {
    private String mSentence;
    private String mVerb;
    private String mElementName;
    private String mElementType;
    private String mVerify;
    private String mData;

    //Constructor
    public Nlp() {
        mSentence = "";
        mVerb = "";
        mElementName = "";
        mElementType = "";
        mVerify = "";
        mData = "";
    }

    public String getSentence() {
        return mSentence;
    }

    public void setSentence(String sentence) {
        mSentence = sentence;
    }

    public String getVerb() {
        return mVerb;
    }

    public void setVerb(String verb) {
        mVerb = verb;
    }

    public String getElementName() {
        return mElementName;
    }

    public void setElementName(String elementName) {
        mElementName = elementName;
    }

    public String getElementType() {
        return mElementType;
    }

    public void setElementType(String elementType) {
        mElementType = elementType;
    }

    public String getVerify() {
        return mVerify;
    }

    public void setVerify(String verify) {
        mVerify = verify;
    }

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        mData = data;
    }
}

