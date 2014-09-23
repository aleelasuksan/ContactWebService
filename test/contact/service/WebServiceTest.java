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

public class WebServiceTest {

	private String url;
	private HttpClient client;
	
	@Before
	public void initializeSystem() throws Exception {
		url = JettyMain.startServer(8080,"contact.resource");
		client = new HttpClient();
		client.start();
	}
	
	@After
	public void shutdownSystem() throws Exception {
		JettyMain.stopServer();
		client.stop();
	}
	
	@Test
	public void testGetPass() throws Exception {
		ContentResponse res = client.GET(url+1);
		assertEquals("Response should be 200 OK", Status.OK.getStatusCode(), res.getStatus());
		assertTrue("Have body content", !res.getContentAsString().isEmpty());
	}
	
	@Test
	public void testGetFail() throws InterruptedException, ExecutionException, TimeoutException {
		ContentResponse res = client.GET(url+0);
		assertEquals("Response should be 204 No Content", Status.NO_CONTENT.getStatusCode(), res.getStatus());
		assertTrue("Empty Content", res.getContentAsString().isEmpty());
	}
	
	@Test
	public void testPOSTPass() throws InterruptedException, TimeoutException, ExecutionException {
		StringContentProvider content = new StringContentProvider("<contact id=\"1234\">" +
				"<title>contact nickname or title</title>" +
				"<name>contact's full name</name>" +
				"<email>contact's email address</email>" +
				"<phoneNumber>contact's telephone number</phoneNumber>"+
				"</contact>");
		ContentResponse res = client.newRequest(url).content(content,"application/xml").method(HttpMethod.POST).send();
		assertEquals("POST complete should response 201 Created", Status.CREATED.getStatusCode(), res.getStatus());
		res = client.GET(url+1234);
		assertTrue("Check by use GET request id that POSTED", !res.getContentAsString().isEmpty() );
	}
	
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
		assertEquals("PUT Success response 200 OK", Status.OK.getStatusCode(), res.getStatus());
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
		assertEquals("DELETE success response 200 OK", Status.OK.getStatusCode(), res.getStatus());
		res = client.GET(url+5555);
		assertTrue("Is it really deleted", res.getContentAsString().isEmpty());
	}
	
	@Test
	public void testDELETEFail() throws InterruptedException, TimeoutException, ExecutionException {
		Request req = client.newRequest(url+0);
		req = req.method(HttpMethod.DELETE);
		ContentResponse res= req.send();
		assertEquals("Contact doesn't exist, response 400 Bad Request", Status.BAD_REQUEST.getStatusCode(), res.getStatus());
	}
}
