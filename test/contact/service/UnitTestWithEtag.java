package contact.service;

import static org.junit.Assert.*;
import main.JettyMain;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.Response.Status;

/**
 * JUnit Test to test ContactResource with Etag implemented.
 * Provide a test Request with If-Match, If-None-Match headers
 * Also test that GET always return ETag header
 * @author Atit Leelasuksan 5510546221
 *
 */
public class UnitTestWithEtag {

	private String url;
	private HttpClient client;
	
	/**
	 * method that done before test
	 * use to start server and start httpclient for test.
	 * @throws Exception
	 */
	@Before
	public void initializeSystem() throws Exception {
		url = JettyMain.startServer(8080,"contact.resource");
		client = new HttpClient();
		client.start();
	}
	
	/**
	 * method that done after test
	 * use to shutdown server and httpclient that tested.
	 * @throws Exception
	 */
	@After
	public void shutdownSystem() throws Exception {
		JettyMain.stopServer();
		client.stop();
	}
	
}
