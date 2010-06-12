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

import java.io.IOException;
import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import hudson.tasks.Publisher;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.junit.TestResult;
import hudson.tasks.test.TestResultProjectAction;

/**
 * A recorder for Testresults from utPLSQL. Currently, the Testresult-classes from JUnit
 * are use, but it is planned to write some more suited to utPLSQL
 */
public class UtplsqlRecorder extends Recorder implements Serializable{

	private static final long serialVersionUID = 1L;

	/**
     * {@link FileSet} "includes" string, like "foo/bar/*.log"
     */
    private final String testResults;
    
    /**
     * necessary for displaying the current configuration in the Job configuration
     * @return the testResults configuration
     */
    public String getTestResults() {
		return testResults;
	}

	@DataBoundConstructor
    public UtplsqlRecorder(String testResults)
    {
    	this.testResults = testResults;
    }
    
    // Copied from from HelloWorldBuilder. don't really know what it means, but it seems not to harm. 
    // overrided for better type safety.
    // if your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }    
    
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	/**
	 * This is necessary, to display the test results on the Project page already, not only for a single build.
	 */
    @Override
    public Action getProjectAction(AbstractProject<?,?> project) {
        TestResultProjectAction action = project.getAction(TestResultProjectAction.class);
        if (action == null) {
            return new TestResultProjectAction(project);
        } else {
            return null;
        }
    }	
	
    @Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
    	//a rough copy from the jUnit Recorder and NUnit recorder, since we just want to use that one first,
    	//before developing our own.
    	
    	UtplsqlTestResultParser parser = new UtplsqlTestResultParser();
    	TestResult testResult = parser.parse(testResults, build, launcher, listener);
    	
        TestResultAction action;
        
    	try {
			action = new TestResultAction(build, testResult, listener);
		} catch (NullPointerException npe) {
			throw new AbortException("Bad XML");
		}
		testResult.freeze(action);
		if (testResult.getPassCount() == 0 && testResult.getFailCount() == 0)
			throw new AbortException("Result is empty");

		build.getActions().add(action);

		if (action.getResult().getFailCount() > 0)
			build.setResult(Result.UNSTABLE);

		//Not realy nice, to hardcode the path, but will be removed anyway, as soon
		//as the jUnit-part is skipped
		build.getWorkspace().child("utPlsql-temporary").deleteRecursive();

    	return true;
    }


	@Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return Messages.UtplsqlRecorder_DisplayName();
		}
    	
    }
}
