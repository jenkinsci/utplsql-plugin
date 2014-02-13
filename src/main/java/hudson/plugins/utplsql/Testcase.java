/*
 * The MIT License
 *
 * Copyright (c) 2010 Nils op den Winkel
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal 
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
 * THE SOFTWARE.
 */
package hudson.plugins.utplsql;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Holds all information given by a single testcase (assert) of utplsql
 * Currently just an intermediate solution for easy creation of the jUnit xml
 * files. Maybe this becomes obsolete later.
 */
public class Testcase implements Serializable
{
	private static final long serialVersionUID = 1L;
    
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILURE = "FAILURE";
	
    private String result;
    
    private void setResult(String result) {
		if (result.equals(Testcase.SUCCESS) || result.equals(Testcase.FAILURE))
		{
			this.result = result;
		}
		else
		{
			throw new IllegalArgumentException("result has to be SUCCESS or FAILURE");
		}
	}

	public String getResult() {
		return result;
	}

    /**
     * the Classname is the package name in utplsql
     */
    private String className;

    public String getClassName() {
        return className;
    }

    /**
     * name is the procedure name
     */
    private String name;

    public String getName() {
        return name;
    }

    /**
     * message is the custom message of the assert
     */
    private String message;

    /**
     * @return Message value (surrounded with quotation marks)
     */
    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        if(this.message != null) {
            sb.append("\"").append(this.message).append("\"");
        }
        return sb.toString();
    }

    /**
     * elapsed time stored in String to skip conversions errors
     */
    private String elapsedTime;

    private void setElapsedTime(String elapsedTime){
        if(elapsedTime == null || elapsedTime.isEmpty()) {
            this.elapsedTime = "0";
        } else {
            this.elapsedTime = elapsedTime;
        }
    }

    public double getElapsedTimeInSeconds() {
        double valueInMilliSeconds = 0.0;

        if(this.elapsedTime != null) {
            valueInMilliSeconds = Double.valueOf(this.elapsedTime.replace(',', '.'))/1000;
        }
        return valueInMilliSeconds;
    }
    
    /**
     * Constructor to instanciate a testcase by parsing a given line of utPLSQL output
     * @param line one line of dbms_output indicating success or failure of a single assert
     */
    public Testcase(TestPackage testPackage, String line)
    {
        // Attended lines look like :
        // SUCCESS - UT_FAKE.UT_FAKE: EQ "Description of testcase very long and multiline
        // this is second line ..."
        //
        // or (utAssert.this does not have a message with "" at all so just take the rest of the line ):
        // SUCCESS - UT_FAKE.UT_FAKE: Description of testcase very long and multiline
        // this is second line ...
        //
        // or :
        // FAILURE - UT_FAKE.UT_FAKE: EQQUERYVALUE "Description of testcase very long and multiline
        // this is second line ..."
        //
        // or, with elapsed time :
        // SUCCESS - UT_FAKE.UT_FAKE [0,903 ms] : EQ "Description of testcase very long and multiline
        // this is second line ..."

        // Parsing line with RexExp - Tested on http://www.debuggex.com/
        Pattern p = Pattern.compile("(.{1,8}) - ([^\\.]{0,30})\\.([^\" :]{0,30})(?:(?: ?\\[)([\\d\\.,]+)(?: ms\\] ?))? ?:(?: (?:([^\" ]{1,30}) \")?(.*)(?:$))?");
        // group(O) : All line
        // group(1) : this.result (SUCCESS/FAILURE)
        // group(2) : this.className (Package name : 30 char MAX)
        // group(3) : this.name (UT Proc : 30 char MAX) (quotation marks and spaces forbidden)
        // group(4) : elapsed time in ms - /!\ : May be null !!!
        // group(5) : testFunction (EQ, THIS, ... : 30 char MAX) - /!\ : May be null !!!
        // group(6) : assertion message (should be multiline) and rest of line - /!\ : May be null if message begin on next line !!!
        Matcher m = p.matcher(line);

        if(!m.matches()){
            // Line doesn't match any expected form. Rather than raising exception, we
            // log unexpected line as failure test
            setResult(Testcase.FAILURE);
            this.className = "UTPLSQL_PLUGIN";
            this.name = "PARSING_ERROR";
            this.message = line;
        } else {
            setResult(m.group(1));
            this.className = m.group(2);
            this.name = testPackage.getCounter() + m.group(3);
            setElapsedTime(m.group(4));
            this.message =  m.group(6);
        }
    }
        
    public void appendToMessage(String newLine)
    {
        // Extract possible quotation marks as last char
        if(newLine.endsWith("\"")){
            newLine = newLine.substring(0,newLine.length()-1);
        }

        // If message is not yet significant, it's crush
        if(this.message == null || this.message.trim().length()==0) {
            this.message = newLine;
        } else {
            this.message = this.message + System.getProperty("line.separator") + newLine;
        }
     }
    
    /**
     * Returns an xml-snippet to be inserted into the jUnit-file
     * @return some text, ready to be inserted into a jUnit-file
     */
    public String getXmlSnippet()
    {
        StringBuffer sb = new StringBuffer("<testcase classname=\"");
        sb.append(this.className);
        sb.append("\" name=\"");
        sb.append(StringEscapeUtils.escapeXml(this.name));
        sb.append("\" time=\"");
        sb.append(getElapsedTimeInSeconds());
        sb.append("\">");
        if (this.result.equals(Testcase.FAILURE))
        {
            sb.append(System.getProperty("line.separator"));
            sb.append("<failure message=\"");
            sb.append(StringEscapeUtils.escapeXml(getMessage()));
            sb.append("\" />");
            sb.append(System.getProperty("line.separator"));
        }
        sb.append("</testcase>");
        sb.append(System.getProperty("line.separator"));
        return sb.toString();
    }
}
