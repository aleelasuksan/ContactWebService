package contact.service;

import static org.junit.Assert.*;
import main.JettyMain;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import contact.service.mem.MemDaoFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
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
		DaoFactory.setFactory(new MemDaoFactory());
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
	 * should response 200 OK with content and ETag header.
	 * @throws Exception
	 */
	@Test
	public void testGet() throws InterruptedException, TimeoutException, ExecutionException {
		ContentResponse res = client.GET(url+1);
		assertEquals("Response should be 200 OK", Status.OK.getStatusCode(), res.getStatus());
		assertTrue("Have body content", !res.getContentAsString().isEmpty());
		assertTrue("Have ETag header", res.getHeaders().containsKey("ETag"));
	}
	
	/**
	 * test success POST request
	 * post new contact to web server
	 * should response 201 Created with ETag header and there is actual new contact in web server.
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	@Test
	public void testPOST() throws InterruptedException, TimeoutException, ExecutionException {
		StringContentProvider content = new StringContentProvider("<contact id=\"12\">" +
				"<title>contact nickname or title</title>" +
				"<name>contact's full name</name>" +
				"<email>contact's email address</email>" +
				"<photoUrl></photoUrl>"+
				"</contact>");
		ContentResponse res = client.newRequest(url)
				.content(content,"application/xml")
				.method(HttpMethod.POST)
				.send();
		assertEquals("POST complete should response 201 Created", Status.CREATED.getStatusCode(), res.getStatus());
		assertTrue("POST response have ETag header", res.getHeaders().containsKey("ETag"));
	}
	
	/**
	 * test GET with If-Match, If-None-Match header request
	 * for If-None-Match should response 304 Not Modified.
	 * for If-Match should response 200 OK.
	 * @throws Exception
	 */
	@Test
	public void testGetwithETag() throws InterruptedException, TimeoutException, ExecutionException {
		ContentResponse res = client.GET(url+1);
		String etag = res.getHeaders().get(HttpHeader.ETAG).replace("\"","");
		System.out.println("GET ETag: "+etag);
		res = client.newRequest(url+1)
				.method(HttpMethod.GET)
				.header(HttpHeader.IF_NONE_MATCH, etag)
				.accept("application/xml")
				.send();
		assertEquals("Response should be 304 Not Modified", Status.NOT_MODIFIED.getStatusCode(), res.getStatus());
		res = client.newRequest(url+1)
				.method(HttpMethod.GET)
				.header(HttpHeader.IF_MATCH, etag)
				.accept("application/xml")
				.send();
		assertEquals("Response should be 200 OK", Status.OK.getStatusCode(), res.getStatus());
		assertTrue("GET response with content", !res.getContentAsString().isEmpty());
	}
	
	/**
	 * test PUT with If-Match, If-None-Match header request
	 * put an update to exist contact
	 * for If-Match should response 204 No Content.
	 * for If-None-Match should response 412 Precondition Failed.
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	@Test
	public void testPUT() throws InterruptedException, TimeoutException, ExecutionException {
		ContentResponse res = client.GET(url+1);
		String etag = res.getHeaders().get(HttpHeader.ETAG).replace("\"","");
		System.out.println("PUT ETag: "+etag);
		StringContentProvider content = new StringContentProvider("<contact id=\"1\">" +
				"<title>newContactTitle</title>" +
				"<name>newContactName</name>" +
				"<email>aaa@g.g</email>" +
				"<photoUrl>edvhyt</photoUrl>"+
				"</contact>");
		res = client.newRequest(url+1)
				.method(HttpMethod.PUT)
				.header(HttpHeader.IF_NONE_MATCH, etag)
				.content(content, "application/xml")
				.send();
		assertEquals("PUT not success response 412 Precondition Failed", Status.PRECONDITION_FAILED.getStatusCode(), res.getStatus());
		res = client.newRequest(url+1)
				.method(HttpMethod.PUT)
				.header(HttpHeader.IF_MATCH, etag)
				.content(content, "application/xml")
				.send();
		assertEquals("PUT Success response 204 No Content", Status.NO_CONTENT.getStatusCode(), res.getStatus());
	}
	
	/**
	 * test DELETE If-Match, If-None-Match header request
	 * delete exist contact
	 * for If-Match should response 204 No Content
	 * for If-None-Match should response 412 Precondition Failed.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	@Test
	public void testDELETE() throws InterruptedException, ExecutionException, TimeoutException {
		StringContentProvider content = new StringContentProvider("<contact id=\"9876\">" +
				"<title>contact nickname or title</title>" +
				"<name>contact's full name</name>" +
				"<email>contact's email address</email>" +
				"<photoUrl>contact's telephone number</photoUrl>"+
				"</contact>");
		ContentResponse res = client.newRequest(url)
				.content(content,"application/xml")
				.method(HttpMethod.POST)
				.send();
		String etag = res.getHeaders().get(HttpHeader.ETAG).replace("\"","");
		System.out.println("DELETE ETag: "+etag);
		res = client.newRequest(url+9876)
				.method(HttpMethod.DELETE)
				.header(HttpHeader.IF_NONE_MATCH, etag)
				.send();
		assertEquals("DELETE not success 412 Precondition Failed", Status.PRECONDITION_FAILED.getStatusCode(), res.getStatus());
		res = client.newRequest(url+9876)
				.method(HttpMethod.DELETE)
				.header(HttpHeader.IF_MATCH, etag)
				.send();
		assertEquals("DELETE success response 204 No Content", Status.NO_CONTENT.getStatusCode(), res.getStatus());
		res = client.GET(url+9876);
		assertTrue("Is it really deleted", res.getContentAsString().isEmpty());
	}
	
}
