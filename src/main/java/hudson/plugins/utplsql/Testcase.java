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
    
    /**
     * name is the procedure name
     */
    private String name;
    
    /**
     * message is the custom message of the assert
     */
    private String message;
    
    /**
     * Constructor to instanciate a testcase by parsing a given line of utPLSQL output
     * @param line one line of dbms_output indicating success or failure of a single assert
     */
    public Testcase(String line)
    {
    	setResult(line.substring(0, 7));
    	int packageSeparator = line.indexOf(".", 9);
    	this.className = line.substring(10, packageSeparator);
    	int functionSeparator = line.indexOf(":", packageSeparator);
    	this.name = line.substring(packageSeparator + 1, functionSeparator);
    	this.message = line.substring(functionSeparator+2);
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
        sb.append(this.name);
        sb.append("\" time=\"0\">");
        if (this.result.equals(Testcase.FAILURE))
        {
            sb.append(System.getProperty("line.separator"));
            sb.append("<failure message=\"");
            sb.append(StringEscapeUtils.escapeXml(message));
            sb.append("\" />");
            sb.append(System.getProperty("line.separator"));
        }
        sb.append("</testcase>");
        sb.append(System.getProperty("line.separator"));
        return sb.toString();
    }
}
