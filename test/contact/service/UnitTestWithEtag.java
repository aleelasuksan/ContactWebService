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

import contact.entity.Contact;
import contact.service.mem.MemDaoFactory;

import java.net.URI;
import java.net.URISyntaxException;
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
	 */
	@Before
	public void initializeSystem() {
		client = new HttpClient();
		try {
			client.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		DaoFactory.setFactory(new MemDaoFactory());
		url = JettyMain.startServer(8080,"contact.resource");
		// always initialize with contact's id 1
		// doesn't need to clear a dao because it use memory-based without load/save file.
		addContact(1);
	}
	
	/**
	 * method that done after test
	 * use to shutdown server and httpclient that tested.
	 */
	@After
	public void shutdownSystem() {
		JettyMain.stopServer();
		try {
			client.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Add contact to dao.
	 * @param id of contact.
	 */
	private void addContact(long id) {
		Contact test = new Contact("Test contact", "Test Name", "none@testing.com");
		test.setId(id);
		DaoFactory.getInstance().getContactDao().save(test);
	}
	
	/**
	 * test success GET request
	 * should response 200 OK with content and ETag header.
	 */
	@Test
	public void testGet(){
		ContentResponse res;
		try {
			URI uri = new URI(url+"/1");
			res = client.GET(uri);
			assertEquals("Response should be 200 OK", Status.OK.getStatusCode(), res.getStatus());
			assertTrue("Have body content", !res.getContentAsString().isEmpty());
			assertTrue("Have ETag header", res.getHeaders().containsKey("ETag"));
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * test success POST request
	 * post new contact to web server
	 * should response 201 Created with ETag header and there is actual new contact in web server.
	 */
	@Test
	public void testPOST() {
		StringContentProvider content = new StringContentProvider("<contact id=\"11\">" +
				"<title>contact nickname or title</title>" +
				"<name>contact's full name</name>" +
				"<email>contact's email address</email>" +
				"<photoUrl></photoUrl>"+
				"</contact>");
		ContentResponse res;
		try {
			res = client.newRequest(url)
					.content(content,"application/xml")
					.method(HttpMethod.POST)
					.send();
			assertEquals("POST complete should response 201 Created", Status.CREATED.getStatusCode(), res.getStatus());
			assertTrue("POST response have ETag header", res.getHeaders().containsKey("ETag"));
			res = client.GET(res.getHeaders().get(HttpHeader.LOCATION));
			assertTrue("Check by use GET request id that POSTED", !res.getContentAsString().isEmpty() );
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * test GET with If-Match, If-None-Match header request
	 * for If-None-Match should response 304 Not Modified.
	 * for If-Match should response 200 OK.
	 */
	@Test
	public void testGetwithETag() {
		ContentResponse res;
		try {
			URI uri = new URI(url+"/1");
			res = client.GET(uri);
			String etag = res.getHeaders().get(HttpHeader.ETAG).replace("\"","");
			res = client.newRequest(uri)
					.method(HttpMethod.GET)
					.header(HttpHeader.IF_NONE_MATCH, etag)
					.accept("application/xml")
					.send();
			assertEquals("Response should be 304 Not Modified", Status.NOT_MODIFIED.getStatusCode(), res.getStatus());
			res = client.newRequest(uri)
					.method(HttpMethod.GET)
					.header(HttpHeader.IF_MATCH, etag)
					.accept("application/xml")
					.send();
			assertEquals("Response should be 200 OK", Status.OK.getStatusCode(), res.getStatus());
			assertTrue("GET response with content", !res.getContentAsString().isEmpty());
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * test PUT with If-Match, If-None-Match header request
	 * put an update to exist contact
	 * for If-Match should response 200 OK.
	 * for If-None-Match should response 412 Precondition Failed.
	 */
	@Test
	public void testPUT() {
		ContentResponse res;
		try {
			URI uri = new URI(url+"/1");
			res = client.GET(uri);
			String etag = res.getHeaders().get(HttpHeader.ETAG).replace("\"","");
			StringContentProvider content = new StringContentProvider("<contact id=\"1\">" +
					"<title>newContactTitle</title>" +
					"<name>newContactName</name>" +
					"<email>aaa@g.g</email>" +
					"<photoUrl>edvhyt</photoUrl>"+
					"</contact>");
			res = client.newRequest(uri)
					.method(HttpMethod.PUT)
					.header(HttpHeader.IF_NONE_MATCH, etag)
					.content(content, "application/xml")
					.send();
			assertEquals("PUT not success response 412 Precondition Failed", Status.PRECONDITION_FAILED.getStatusCode(), res.getStatus());
			res = client.newRequest(uri)
					.method(HttpMethod.PUT)
					.header(HttpHeader.IF_MATCH, etag)
					.content(content, "application/xml")
					.send();
			assertEquals("PUT Success response 200 OK", Status.OK.getStatusCode(), res.getStatus());
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * test DELETE If-Match, If-None-Match header request
	 * delete exist contact
	 * for If-Match should response 200 OK
	 * for If-None-Match should response 412 Precondition Failed.
	 */
	@Test
	public void testDELETE() {
		ContentResponse res;
		try {
			URI uri = new URI(url+"/1");
			res = client.GET(uri);
			String etag = res.getHeaders().get(HttpHeader.ETAG).replace("\"","");
			res = client.newRequest(uri)
					.method(HttpMethod.DELETE)
					.header(HttpHeader.IF_NONE_MATCH, etag)
					.send();
			assertEquals("DELETE not success 412 Precondition Failed", Status.PRECONDITION_FAILED.getStatusCode(), res.getStatus());
			res = client.newRequest(uri)
					.method(HttpMethod.DELETE)
					.header(HttpHeader.IF_MATCH, etag)
					.send();
			assertEquals("DELETE success response 200 OK", Status.OK.getStatusCode(), res.getStatus());
			res = client.GET(uri);
			assertEquals("Is it really deleted", Status.NOT_FOUND.getStatusCode(), res.getStatus());
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
}
