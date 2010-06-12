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

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.FilePath.FileCallable;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.junit.TestResult;
import hudson.tasks.test.TestResultParser;
import hudson.util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

/**
 * In the future, this class should create utPLSQL specific TestResults.
 * Currently it transforms the utPLSQL output to jUnit files and runs
 * creates a jUnit testresult.
 */
public class UtplsqlTestResultParser extends TestResultParser implements
		Serializable {

	/**
	 * FileCallable, actually doing the work. In the DefaultTestResultParserImpl, 
	 * which I used as an example first, this Class is created somewhat different,
	 * which causes problems during Serialization, so that builds on a slave will
	 * fail. 
	 */
	private static final class ParseTestResultCallable implements
			FileCallable<TestResult> {
		private String testResultLocations;

		public ParseTestResultCallable(String testResultLocations) {
			this.testResultLocations = testResultLocations;
		}

		public TestResult invoke(File dir, VirtualChannel channel)
				throws IOException, InterruptedException {

			FilePath[] paths = new FilePath(dir).list(testResultLocations);
			if (paths.length == 0)
				throw new AbortException("No test reports that matches "
						+ testResultLocations + " found. Configuration error?");

			// since dir is local, paths all point to the local files
			List<File> files = new ArrayList<File>(paths.length);
			for (FilePath path : paths) {
				File report = new File(path.getRemote());
				files.add(report);
			}

			return parse(files, dir);
		}

		private TestResult parse(List<File> files, File workSpace)
				throws InterruptedException, IOException {
			TestResult testResult;
			final String tmpDirectory = "utPlsql-temporary";
			ArrayList<TestPackage> testPackages = new ArrayList<TestPackage>();
			File junitOutputPath = new File(workSpace, tmpDirectory);
			junitOutputPath.mkdirs();

			for (File file : files) {
				TestPackage currentPackage = null;
				String currentLine;
				BufferedReader fr = new BufferedReader(new FileReader(file));
				do {
					currentLine = fr.readLine();
					if (currentLine == null) {
						// either a new package, or the end of the file will add
						// the package
						testPackages.add(currentPackage);
						break;
					}
					// For some reason there are a lot of trailing whitespaces.
					currentLine = currentLine.trim();
					if (Pattern.matches("^((SUCCESS)|(FAILURE)): \".*\"",
							currentLine)) {
						// new Package starting
						if (currentPackage != null) {
							if (currentPackage != null) {
								testPackages.add(currentPackage);
							}
						}
						currentPackage = new TestPackage(currentLine.substring(
								10, currentLine.length() - 1));
					} else if (Pattern.matches("^((SUCCESS)|(FAILURE)) - .*",
							currentLine)) {
						// TODO: What about multiline stuff?
						currentPackage.add(new Testcase(currentLine));
					}

				} while (true);
				IOUtils.closeQuietly(fr);
			}

			// build fake jUnit-Files from Testcases
			for (TestPackage testPackage : testPackages) {
				FileWriter fw = null;
				try {
					File outFile = new File(junitOutputPath, testPackage
							.getName()
							+ "fake-jUnit.xml");
					fw = new FileWriter(outFile);
					fw.append(testPackage.getXmlSnippet());
				} finally {
					IOUtils.closeQuietly(fw);
				}
			}

			FileSet fs = Util.createFileSet(workSpace, tmpDirectory + "/*.xml");
			DirectoryScanner ds = fs.getDirectoryScanner();

			String[] fileNames = ds.getIncludedFiles();
			if (fileNames.length == 0) {
				// no test result. Most likely a configuration error or fatal
				// problem
				throw new AbortException(
						"No test report files were found. Configuration error?");
			}
			
			//create the jUnit result from our fake files. 
			testResult = new TestResult(0, ds, false);

			return testResult;
		}

	}

	private static final long serialVersionUID = 1L;

	@Override
	public String getDisplayName() {
		return "ut/plsql parser";
	}

	@Override
	public String getTestResultLocationMessage() {
		return "Paths to dbms_output files of ut/plsql:";
	}

	/**
	 * Called by the Recorder.
	 */
	@Override
	public TestResult parse(String testResultLocations, AbstractBuild build,
			Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		TestResult testResult = build.getWorkspace().act(
				new ParseTestResultCallable(testResultLocations));
		return testResult;
	}
}
