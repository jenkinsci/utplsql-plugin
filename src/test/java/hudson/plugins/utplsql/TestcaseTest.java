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

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link Testcase}
 */
public class TestcaseTest {

    private final String packageName = "UT_FAKE";
    private final double expectedElapsedTime = 0.0;

    @Test
    public void successConstructor() {
        // GIVEN SUCCESS - UT_FAKE.UT_FAKE1: this is just a unittest
        String line = "SUCCESS - UT_FAKE.UT_FAKE1: this is just a unittest";

        // WHEN
        Testcase testcase = new Testcase(new TestPackage("test"), line);

        // THEN
        final String procedureName = "1UT_FAKE1";
        final String expectedMessage = "this is just a unittest";
        assertEquals("Result is success", Testcase.SUCCESS, testcase.getResult());
        assertEquals("Package name is filled", packageName, testcase.getClassName());
        assertEquals("Procedure name is filled", procedureName, testcase.getName());
        assertEquals("Message is filled", expectedMessage, testcase.getMessage());
        assertEquals("Elapsed time is forced to 0", 0.0, testcase.getElapsedTimeInSeconds(), 0.0);
    }

    @Test
    public void successConstructorSpecialCharacters() {
        // GIVEN
        // SUCCESS - UT_FAKE.UT_FAKE_3: EQ "hint: check regex, for : in first 60 Characters..." Expected "1" and got "1"
        StringBuffer line = new StringBuffer("SUCCESS - UT_FAKE.UT_FAKE_3: EQ \"hint: check regex, for : in first 60 Characters...\" Expected \"1\" and got \"1\"");

        // WHEN
        Testcase testcase = new Testcase(new TestPackage("test"), line.toString());

        // THEN
        String procedureName = "1UT_FAKE_3";
        String expectedMessage = "EQ \"hint: check regex, for : in first 60 Characters...\" Expected \"1\" and got \"1\"";
        assertEquals("Result is success", Testcase.SUCCESS, testcase.getResult());

        assertEquals("Package name is filled", packageName, testcase.getClassName());
        assertEquals("Procedure name is filled", procedureName, testcase.getName());
        assertEquals("Message is filled", expectedMessage, testcase.getMessage());
        assertEquals("Elapsed time is forced to 0", 0.0, testcase.getElapsedTimeInSeconds(), 0.0);
    }

    @Test
    public void successConstructorIsNull() {
        // GIVEN
        //Test for Jenkins-21657
        // SUCCESS - UT_FAKE.UT_FAKE_3: ISNULL "test" Expected "" and got ""
        String line = "SUCCESS - UT_FAKE.UT_FAKE_3: ISNULL \"test\" Expected \"\" and got \"\"";

        // WHEN
        Testcase testcase = new Testcase(new TestPackage("test"), line);

        // THEN
        String procedureName = "1UT_FAKE_3";
        String expectedMessage = "ISNULL \"test\" Expected \"\" and got \"\"";
        assertEquals("Result is success", Testcase.SUCCESS, testcase.getResult());
        assertEquals("Package name is filled", packageName, testcase.getClassName());
        assertEquals("Procedure name is filled", procedureName, testcase.getName());
        assertEquals("Message is filled", expectedMessage, testcase.getMessage());
        assertEquals("Elapsed time is forced to 0", 0.0, testcase.getElapsedTimeInSeconds(), 0.0);
    }

    @Test
    public void successConstructorWithElapsedTime() {
        // FIRST PATTERN :
        // SUCCESS - UT_FAKE.UT_FAKE1 [1,101 ms] : this is just a unittest
        String result = Testcase.SUCCESS;
        String packageName = "UT_FAKE";
        String procedureName = "UT_FAKE1";
        String elapsedTime = "1,101";
        String message = "this is just a unittest";
        StringBuffer expectedMessage = new StringBuffer(message);
        StringBuffer line = new StringBuffer(result).append(" - ")
                .append(packageName)
                .append(".")
                .append(procedureName)
                .append(" : ")
                .append(message);
        Testcase testcase = new Testcase(new TestPackage("test"), line.toString());
        assertEquals("First pattern - Result is success", result, testcase.getResult());
        assertEquals("First pattern - Package name is filled", packageName, testcase.getClassName());
        assertEquals("First pattern - Procedure name is filled", "1" + procedureName, testcase.getName());
        assertEquals("First pattern - Elapsed time is filled", expectedElapsedTime, testcase.getElapsedTimeInSeconds(), 0.0);
        assertEquals("First pattern - Message is filled", expectedMessage.toString(), testcase.getMessage());
    }

    @Test
    public void successConstructorWithElapsedTimeWithoutSpaceAfterRightSquareBracket() {

        // SECOND PATTERN : without space after ']'
        // SUCCESS - UT_FAKE.UT_FAKE1 [1,101 ms]: this is just a unittest
        String result = Testcase.SUCCESS;
        String packageName = "UT_FAKE";
        String procedureName = "UT_FAKE1";
        String elapsedTime = "1,101";
        double expectedElapsedTime = 0.0;
        String message = "this is just a unittest";
        StringBuffer expectedMessage = new StringBuffer(message);
        StringBuffer line = new StringBuffer(result).append(" - ")
                .append(packageName).append(".")
                .append(procedureName).append(" [")
                .append(elapsedTime).append(" ms]: ")
                .append(message);
        Testcase testcase = new Testcase(new TestPackage("test"), line.toString());
        assertEquals("Second pattern - Result is success", result, testcase.getResult());
        assertEquals("Second pattern - Package name is filled", packageName, testcase.getClassName());
        assertEquals("Second pattern - Procedure name is filled", "1" + procedureName, testcase.getName());
        assertEquals("Second pattern - Elapsed time is filled", expectedElapsedTime, testcase.getElapsedTimeInSeconds(), 0.0);
        assertEquals("Second pattern - Message is filled", expectedMessage.toString(), testcase.getMessage());
    }

    @Test
    public void successConstructorWithElapsedTimeWithDotInPlaceOfComma() {
        // THIRD PATTERN : dot in place of comma
        // SUCCESS - UT_FAKE.UT_FAKE1 [1.101 ms]: this is just a unittest
        // GIVEN
        String line = "SUCCESS - UT_FAKE.UT_FAKE1 [1.101 ms]: this is just a unittest";

        // WHEN
        Testcase testcase = new Testcase(new TestPackage("test"), line);

        // THEN
        String procedureName = "1UT_FAKE1";
        String expectedMessage = "this is just a unittest";
        double expectedElapsedTime = 0.0;
        assertEquals("Third pattern - Result is success", Testcase.SUCCESS, testcase.getResult());
        assertEquals("Third pattern - Package name is filled", packageName, testcase.getClassName());
        assertEquals("Third pattern - Procedure name is filled",  procedureName, testcase.getName());
        assertEquals("Third pattern - Elapsed time is filled", expectedElapsedTime, testcase.getElapsedTimeInSeconds(), 0.0);
        assertEquals("Third pattern - Message is filled", expectedMessage, testcase.getMessage());
    }

    @Test
    public void successConstructorWithElapsedTimeWithTestFunction() {
        // FOURTH PATTERN : with test function
        // SUCCESS - UT_FAKE.UT_FAKE1 [1.101 ms]: EQ "this is just a unittest"
        //GIVEN
        String line = "SUCCESS - UT_FAKE.UT_FAKE1 [1.101 ms]: EQ \"this is just a unittest\"";

        // WHEN
        Testcase testcase = new Testcase(new TestPackage("test"), line);

        // THEN
        String procedureName = "1UT_FAKE1";
        String expectedMessage = "EQ \"this is just a unittest\"";

        assertEquals("Fourth pattern - Result is success", Testcase.SUCCESS, testcase.getResult());
        assertEquals("Fourth pattern - Package name is filled", packageName, testcase.getClassName());
        assertEquals("Fourth pattern - Procedure name is filled", procedureName, testcase.getName());
        assertEquals("Fourth pattern - Elapsed time is filled", expectedElapsedTime, testcase.getElapsedTimeInSeconds(), 0.0);
        assertEquals("Fourth pattern - Message is filled", expectedMessage, testcase.getMessage());
    }

    @Test
    public void failureConstructor() {
        Testcase testcase = new Testcase(new TestPackage("test"), "FAILURE - UT_FAKE.UT_FAKE1: this is just a unittest");
        assertEquals("Result is wrong", Testcase.FAILURE, testcase.getResult());
    }

    @Test
    public void invalidResultConstructor() {
        String unexpectedLine = "something other than SUCCESS or FAILURE as a start";
        StringBuffer expectedMessage = new StringBuffer(unexpectedLine);
        Testcase testcase = new Testcase(new TestPackage("test"), unexpectedLine);
        assertEquals("Result is wrong", Testcase.FAILURE, testcase.getResult());
        assertEquals("Unexpected line as message", expectedMessage.toString(), testcase.getMessage());
    }

    @Test
    public void invalidLineContructor() {
        //line to parse too short
        String unexpectedLine = "a";
        StringBuffer expectedMessage = new StringBuffer(unexpectedLine);
        Testcase testcase = new Testcase(new TestPackage("test"), unexpectedLine);
        assertEquals("Result is wrong", Testcase.FAILURE, testcase.getResult());
        assertEquals("Unexpected line as message", expectedMessage.toString(), testcase.getMessage());

    }

    @Test
    public void multiLineMessage() {
        // SUCCESS - UT_FAKE.UT_FAKE1: EQ "this is just the first line of message
        // and this is my second line
        // and then, this is the last line of this message"
        String result = Testcase.SUCCESS;
        String packageName = "UT_FAKE";
        String procedureName = "UT_FAKE1";
        String firstLine = "\"this is just the first line of message";
        String secondLine = "and this is my second line";
        String lastLine = "and then, this is the last line of this message\"";
        StringBuffer message = new StringBuffer("EQ ").append(firstLine).append(System.getProperty("line.separator"))
                .append(secondLine).append(System.getProperty("line.separator"))
                .append(lastLine);
        StringBuffer line = new StringBuffer(result).append(" - ")
                .append(packageName).append(".")
                .append(procedureName).append(": EQ ")
                .append(firstLine);
        Testcase testcase = new Testcase(new TestPackage("test"), line.toString());
        testcase.appendToMessage(secondLine);
        testcase.appendToMessage(lastLine);
        assertEquals("Result is success", result, testcase.getResult());
        assertEquals("Package name is filled", packageName, testcase.getClassName());
        assertEquals("Procedure name is filled", "1" + procedureName, testcase.getName());
        assertEquals("Message is filled", message.toString(), testcase.getMessage());
        assertEquals("Elapsed time is forced to 0", 0.0, testcase.getElapsedTimeInSeconds(), 0.0);
    }

    @Test
    public void multiLineMessageWithExpected() {
        // SUCCESS - UT_FAKE.UT_FAKE1: EQ "this is just the first line of message
        // and then, this is the last line of this message" Expected "10" and got "10"
        String result = Testcase.SUCCESS;
        String packageName = "UT_FAKE";
        String procedureName = "UT_FAKE1";
        String firstLine = "\"this is just the first line of message";
        String secondLine = "and this is my second line";
        String lastLine = "and then, this is the last line of this message\" Expected \"10\" and got \"10\"";
        StringBuffer message = new StringBuffer("EQ ").append(firstLine).append(System.getProperty("line.separator"))
                .append(secondLine).append(System.getProperty("line.separator"))
                .append(lastLine);
        StringBuffer line = new StringBuffer(result).append(" - ")
                .append(packageName).append(".")
                .append(procedureName).append(": EQ ")
                .append(firstLine);
        Testcase testcase = new Testcase(new TestPackage("test"), line.toString());
        testcase.appendToMessage(secondLine);
        testcase.appendToMessage(lastLine);
        assertEquals("Result is success", result, testcase.getResult());
        assertEquals("Package name is filled", packageName, testcase.getClassName());
        assertEquals("Procedure name is filled", "1" + procedureName, testcase.getName());
        assertEquals("Message is filled", message.toString(), testcase.getMessage());
        assertEquals("Elapsed time is forced to 0", 0.0, testcase.getElapsedTimeInSeconds(), 0.0);
    }

    @Test
    public void messageOnlyOnSecondLine() {
        // SUCCESS - UT_FAKE.UT_FAKE1:
        // my message is on second line
        String result = Testcase.SUCCESS;
        String packageName = "UT_FAKE";
        String procedureName = "UT_FAKE1";
        String secondLine = "my message is on second line";
        StringBuffer expectedMessage = new StringBuffer(secondLine);
        StringBuffer line = new StringBuffer(result).append(" - ")
                .append(packageName).append(".")
                .append(procedureName).append(":");
        Testcase testcase = new Testcase(new TestPackage("test"), line.toString());
        testcase.appendToMessage(secondLine);
        assertEquals("Result is success", result, testcase.getResult());
        assertEquals("Package name is filled", packageName, testcase.getClassName());
        assertEquals("Procedure name is filled", "1" + procedureName, testcase.getName());
        assertEquals("Message is filled (and surrounded with quotation marks)", expectedMessage.toString(), testcase.getMessage());
        assertEquals("Elapsed time is forced to 0", 0.0, testcase.getElapsedTimeInSeconds(), 0.0);
    }
}
