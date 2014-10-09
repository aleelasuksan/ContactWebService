package contact.service;

import static org.junit.Assert.*;

import main.JettyMain;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
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
	public void initializeSystem() {
		client = new HttpClient();
		try {
			client.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		DaoFactory.setFactory(new MemDaoFactory("ContactTest.xml"));
		url = JettyMain.startServer(8080,"contact.resource");
		// always initialize with contact's id 1
		// doesn't need to clear a dao because it use memory-based without load/save file.
		addContact(1);
	}
	
	/**
	 * method that done after test
	 * use to shutdown server and httpclient that tested.
	 * @throws Exception
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
	 * should response 200 OK with content.
	 */
	@Test
	public void testGetPass() {
		ContentResponse res;
		try {
			URI uri = new URI(url+"/1");
			res = client.GET(uri);
			assertEquals("Response should be 200 OK", Status.OK.getStatusCode(), res.getStatus());
			assertTrue("Have body content", !res.getContentAsString().isEmpty());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * test fail GET request
	 * try to GET data that not exist
	 * should response 404 Not Found.
	 */
	@Test
	public void testGetFail() {
		ContentResponse res;
		try {
			URI uri = new URI(url+"/0");
			res = client.GET(uri);
			assertEquals("Response should be 404 Not Found", Status.NOT_FOUND.getStatusCode(), res.getStatus());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * test success POST request
	 * post new contact to web server
	 * should response 201 Created and there is actual new contact in web server.
	 */
	@Test
	public void testPOSTPass() {
		StringContentProvider content = new StringContentProvider("<contact id=\"11\">" +
				"<title>contact nickname or title</title>" +
				"<name>contact's full name</name>" +
				"<email>contact's email address</email>" +
				"<phoneNumber>contact's telephone number</phoneNumber>"+
				"</contact>");
		ContentResponse res;
		try {
			res = client.newRequest(url)
					.content(content,"application/xml")
					.method(HttpMethod.POST)
					.send();
			assertEquals("POST complete should response 201 Created", Status.CREATED.getStatusCode(), res.getStatus());
			res = client.GET(res.getHeaders().get(HttpHeader.LOCATION));
			assertTrue("Check by use GET request id that POSTED", !res.getContentAsString().isEmpty() );
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * test fail POST request
	 * post new contact that id already exist
	 * should response 409 Conflict and can't create new contact.
	 */
	@Test
	public void testPOSTFail() {
		StringContentProvider content = new StringContentProvider("<contact id=\"1\">" +
				"<title>contact nickname or title</title>" +
				"<name>contact's full name</name>" +
				"<email>contact's email address</email>" +
				"<phoneNumber>contact's telephone number</phoneNumber>"+
				"</contact>");
		ContentResponse res;
		try {
			res = client.newRequest(url)
					.content(content,"application/xml")
					.method(HttpMethod.POST)
					.send();
			assertEquals("POST should response CONFLICT due to already exist id", Status.CONFLICT.getStatusCode(), res.getStatus());
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * test success PUT request
	 * put an update to exist contact
	 * should response 200 OK.
	 */
	@Test
	public void testPUTPass() {
		StringContentProvider content = new StringContentProvider("<contact id=\"1\">" +
				"<title>newContactTitle</title>" +
				"<name>newContactName</name>" +
				"<email></email>" +
				"<phoneNumber></phoneNumber>"+
				"</contact>");
		ContentResponse res;
		try {
			URI uri = new URI(url+"/1");
			res = client.newRequest(uri)
					.method(HttpMethod.PUT)
					.content(content, "application/xml")
					.send();
			assertEquals("PUT Success response 200 OK", Status.OK.getStatusCode(), res.getStatus());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * test fail PUT request
	 * put an update to id that not exist
	 * should response 400 BAD REQUEST.
	 */
	@Test
	public void testPUTFail() {
		StringContentProvider content = new StringContentProvider("<contact id=\"9876545\">" +
				"<title>newContactTitle</title>" +
				"<name>newContactName</name>" +
				"<email></email>" +
				"<phoneNumber> </phoneNumber>"+
				"</contact>");
		ContentResponse res;
		try {
			URI uri = new URI(url+"/51234");
			res = client.newRequest(uri)
					.method(HttpMethod.PUT)
					.content(content, "application/xml")
					.send();
			assertEquals("PUT Fail response 404 Not Found", Status.NOT_FOUND.getStatusCode(), res.getStatus());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * test success DELETE request
	 * delete exist contact
	 * should response 200 OK and check that contact deleted.
	 */
	@Test
	public void testDELETEPass() {
		ContentResponse res;
		try {
			URI uri = new URI(url+"/1");
			res = client.newRequest(uri)
					.method(HttpMethod.DELETE)
					.send();
			assertEquals("DELETE success response 200 OK", Status.OK.getStatusCode(), res.getStatus());
			res = client.GET(url+1);
			assertEquals("Is it really deleted", Status.NOT_FOUND.getStatusCode(), res.getStatus());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * test fail DELETE request
	 * try to delete contact that doesn't exist
	 * should response 404 Not Found.
	 */
	@Test
	public void testDELETEFail() {
		ContentResponse res;
		try {
			URI uri = new URI(url+"/0");
			res = client.newRequest(uri)
					.method(HttpMethod.DELETE)
					.send();
			assertEquals("Contact doesn't exist, response 404 Not Found", Status.NOT_FOUND.getStatusCode(), res.getStatus());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}
