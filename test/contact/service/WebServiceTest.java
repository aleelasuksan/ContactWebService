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
 * JUnit Test class to test ContactResource
 * Provide each 2 test for GET, POST, PUT, DELETE Method, one is success one is fail.
 * Test both response and actual data.
 * @author Atit Leelasuksan 5510546221
 *
 */
public class WebServiceTest {

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
	
	/**
	 * test success GET request
	 * should response 200 OK with content.
	 * @throws Exception
	 */
	@Test
	public void testGetPass() throws Exception {
		ContentResponse res = client.GET(url+1);
		assertEquals("Response should be 200 OK", Status.OK.getStatusCode(), res.getStatus());
		assertTrue("Have body content", !res.getContentAsString().isEmpty());
	}
	
	/**
	 * test fail GET request
	 * try to GET data that not exist
	 * should response 204 No Content without content.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	@Test
	public void testGetFail() throws InterruptedException, ExecutionException, TimeoutException {
		ContentResponse res = client.GET(url+0);
		assertEquals("Response should be 204 No Content", Status.NO_CONTENT.getStatusCode(), res.getStatus());
		assertTrue("Empty Content", res.getContentAsString().isEmpty());
	}
	
	/**
	 * test success POST request
	 * post new contact to web server
	 * should response 201 Created and there is actual new contact in web server.
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	@Test
	public void testPOSTPass() throws InterruptedException, TimeoutException, ExecutionException {
		StringContentProvider content = new StringContentProvider("<contact id=\"2222\">" +
				"<title>contact nickname or title</title>" +
				"<name>contact's full name</name>" +
				"<email>contact's email address</email>" +
				"<phoneNumber>contact's telephone number</phoneNumber>"+
				"</contact>");
		ContentResponse res = client.newRequest(url).content(content,"application/xml").method(HttpMethod.POST).send();
		assertEquals("POST complete should response 201 Created", Status.CREATED.getStatusCode(), res.getStatus());
		res = client.GET(url+2222);
		assertTrue("Check by use GET request id that POSTED", !res.getContentAsString().isEmpty() );
	}
	
	/**
	 * test fail POST request
	 * post new contact that id already exist
	 * should response 409 Conflict and can't create new contact.
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	@Test
	public void testPOSTFail() throws InterruptedException, TimeoutException, ExecutionException {
		StringContentProvider content = new StringContentProvider("<contact id=\"1\">" +
				"<title>contact nickname or title</title>" +
				"<name>contact's full name</name>" +
				"<email>contact's email address</email>" +
				"<phoneNumber>contact's telephone number</phoneNumber>"+
				"</contact>");
		ContentResponse res = client.newRequest(url).content(content,"application/xml").method(HttpMethod.POST).send();
		assertEquals("POST should response CONFLICT due to already exist id", Status.CONFLICT.getStatusCode(), res.getStatus());
	}
	
	/**
	 * test success PUT request
	 * put an update to exist contact
	 * should response 204 No Content.
	 * also check that contact updated.
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	@Test
	public void testPUTPass() throws InterruptedException, TimeoutException, ExecutionException {
		Request req = client.newRequest(url+1);
		req = req.method(HttpMethod.PUT);
		StringContentProvider content = new StringContentProvider("<contact id=\"1\">" +
				"<title>newContactTitle</title>" +
				"<name>newContactName</name>" +
				"<email></email>" +
				"<phoneNumber></phoneNumber>"+
				"</contact>");
		req = req.content(content, "application/xml");
		ContentResponse res = req.send();
		assertEquals("PUT Success response 204 OK", Status.NO_CONTENT.getStatusCode(), res.getStatus());
		res = client.GET(url);
		String con = res.getContentAsString();
		
		Pattern patt = Pattern.compile(".*<title>newContactTitle</title>.*");
		Matcher match = patt.matcher(con);
		assertTrue("Check is title updated.", match.matches());
		
		patt = Pattern.compile(".*<name>newContactName</name>.*");
		match = patt.matcher(con);
		assertTrue("Check is name updated", match.matches());
		
		patt = Pattern.compile(".*<email></email>.*");
		match = patt.matcher(con);
		assertTrue("Check is email updated", match.matches());
		
	}
	
	/**
	 * test fail PUT request
	 * put an update to id that not exist
	 * should response 400 BAD REQUEST.
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	@Test
	public void testPUTFail() throws InterruptedException, TimeoutException, ExecutionException {
		Request req = client.newRequest(url+51234);
		req = req.method(HttpMethod.PUT);
		StringContentProvider content = new StringContentProvider("<contact id=\"9876545\">" +
				"<title>newContactTitle</title>" +
				"<name>newContactName</name>" +
				"<email></email>" +
				"<phoneNumber> </phoneNumber>"+
				"</contact>");
		req = req.content(content, "application/xml");
		ContentResponse res = req.send();
		assertEquals("PUT Fail response 400 BAD REQUEST", Status.BAD_REQUEST.getStatusCode(), res.getStatus());
	}
	
	/**
	 * test success DELETE request
	 * delete exist contact
	 * should response 204 No Content and check that contact deleted.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	@Test
	public void testDELETEPass() throws InterruptedException, ExecutionException, TimeoutException {
		StringContentProvider content = new StringContentProvider("<contact id=\"5555\">" +
				"<title>contact nickname or title</title>" +
				"<name>contact's full name</name>" +
				"<email>contact's email address</email>" +
				"<phoneNumber>contact's telephone number</phoneNumber>"+
				"</contact>");
		ContentResponse res = client.newRequest(url).content(content,"application/xml").method(HttpMethod.POST).send();
		Request req = client.newRequest(url+5555);
		req = req.method(HttpMethod.DELETE);
		res= req.send();
		assertEquals("DELETE success response 204 OK", Status.NO_CONTENT.getStatusCode(), res.getStatus());
		res = client.GET(url+5555);
		assertTrue("Is it really deleted", res.getContentAsString().isEmpty());
	}
	
	/**
	 * test fail DELETE request
	 * try to delete contact that doesn't exist
	 * should response 404 Not Found.
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	@Test
	public void testDELETEFail() throws InterruptedException, TimeoutException, ExecutionException {
		Request req = client.newRequest(url+0);
		req = req.method(HttpMethod.DELETE);
		ContentResponse res= req.send();
		assertEquals("Contact doesn't exist, response 404 Not Found", Status.NOT_FOUND.getStatusCode(), res.getStatus());
	}
}
