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
import java.util.ArrayList;

/**
 * Holds all information about one testpackage ({@link Testcase}s and number of errors etc.)
 * Currently just an intermediate solution for easy creation of the jUnit xml
 * files. Maybe this becomes obsolete later.
 * @see Testcase
 */
public class TestPackage implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * the testcases in this package
	 * @see Testcase
	 */
	private ArrayList<Testcase> testCases;
	
	/**
	 * the number of failures
	 */
	private int failures;
	
	/**
	 * The package name
	 */
	private String name;
	
	
	public String getName() {
		return name;
	}

	/**
	 * Creates a new instance of this class
	 * @param name the name of the test package
	 */
	public TestPackage(String name)
	{
		this.testCases = new ArrayList<Testcase>();
		this.name = name;
		this.failures = 0;
	}
	
	/**
	 * Add a Testcase to this package
	 * @param testcase the Testcase to add
	 */
	public void add(Testcase testcase)
	{
		this.testCases.add(testcase);
		if (testcase.getResult().equals(Testcase.FAILURE))
		{
			this.failures++;
		}
	}
	
    /**
     * The result of this function should be written to one xml-file later representing
     * The testresults for one Testpackage.
     * @return the xml-code to be inserted into
     */
		public String getXmlSnippet()
    {
        StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        sb.append(System.getProperty("line.separator"));
        sb.append("<testsuite errors=\"0\" failures=\"");
        sb.append(this.failures);
        sb.append("\" name=\"");
        sb.append(this.name);
        sb.append("\" tests=\"");
        sb.append(testCases.size());
        sb.append("\">");
        sb.append(System.getProperty("line.separator"));
        for (Testcase testcase : this.testCases)
        {
        	sb.append(testcase.getXmlSnippet());
        }
        sb.append("</testsuite>");
        return sb.toString();
    }
}
