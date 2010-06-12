package hudson.plugins.utplsql;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestBuilder;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.test.AbstractTestResultAction;

public class UtplsqlTestResultParserTest extends HudsonTestCase
{

	public void testOneFileOnePackage() throws Exception
	{
		final InputStream input = this.getClass().getResourceAsStream("OneFileOnePackage.log");
		FreeStyleProject project = createFreeStyleProject();
		project.getBuildersList().add(new TestBuilder() {
		    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
		        BuildListener listener) throws InterruptedException, IOException {
		        OutputStream output = build.getWorkspace().child("result.txt").write();
		        int b;
		        do
		        {
		        	b = input.read();
		        	if (b == -1)
		        	{
		        		break;
		        	}
		        	output.write(b);
		        } while (true);
		        return true;
		    }
		});
		UtplsqlRecorder recorder = new UtplsqlRecorder("*.txt");
		project.getPublishersList().add(recorder);
		FreeStyleBuild build = project.scheduleBuild2(0).get();
		assertBuildStatus(Result.UNSTABLE, build);
		AbstractTestResultAction action = build.getAction(AbstractTestResultAction.class);
		assertEquals(1, action.getTotalCount());
		assertEquals(1, action.getFailCount());
	}
}
