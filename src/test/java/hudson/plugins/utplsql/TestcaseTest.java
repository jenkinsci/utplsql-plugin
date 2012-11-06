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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for {@link Testcase}
 */
public class TestcaseTest {

	@Test
	public void successConstructor()
	{
		Testcase testcase = new Testcase(new TestPackage("test"), "SUCCESS - UT_FAKE.UT_FAKE1: this is just a unittest");
		assertEquals("Result is wrong", Testcase.SUCCESS, testcase.getResult());
	}

	@Test
	public void failureConstructor()
	{
		Testcase testcase = new Testcase(new TestPackage("test"), "FAILURE - UT_FAKE.UT_FAKE1: this is just a unittest");
		assertEquals("Result is wrong", Testcase.FAILURE, testcase.getResult());
	}

	@Test(expected=IllegalArgumentException.class)
	public void invalidResultConstructor()
	{
		@SuppressWarnings("unused")
		Testcase testcase = new Testcase(new TestPackage("test"), "something other than SUCCESS or FAILURE as a start");
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void invalidLineContructor()
	{
		//line to parse too short
		@SuppressWarnings("unused")
		Testcase testcase = new Testcase(new TestPackage("test"), "a");		
	}

}
