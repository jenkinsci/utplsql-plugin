package hudson.plugins.utplsql;


import hudson.model.FreeStyleProject;

import org.jvnet.hudson.test.HudsonTestCase;

public class UtplsqlRecorderTest extends HudsonTestCase {

	public void testConfigurationRoundTrip() throws Exception 
	{
			FreeStyleProject project;
			project = createFreeStyleProject();
			UtplsqlRecorder before = new UtplsqlRecorder("testing");
			project.getPublishersList().add(before);
			submit(createWebClient().getPage(project, "configure").getFormByName("config"));
			UtplsqlRecorder after = project.getPublishersList().get(UtplsqlRecorder.class);
			assertEqualBeans(before, after, "testResults");
	}
}
