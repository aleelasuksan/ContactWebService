package contact.resource;

import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import contact.entity.Contact;
import contact.entity.ContactList;
import contact.service.ContactDao;
import contact.service.DaoFactory;
import contact.service.mem.MemDaoFactory;

/**
 * Provide Contact web resource that response to HTTP request
 * With GET, GET with parameter, GET with query, POST, PUT with parameter, DELETE method.
 * now GET with id parameter, POST and PUT support ETag, If-Match, If-None-Match Header.
 * @author Atit Leelasuksan 5510546221
 *
 */
@Path("/contacts")
@Singleton
public class ContactResource {

	/**
	 * Data access object to access Contact data.
	 * use to get data, save data and update data.
	 */
	private ContactDao dao;
	private CacheControl cc;
	private EntityTag eTag;
	ResponseBuilder rb;
	
	/**
	 * Initialize Resource and Contact Data Access Object.
	 */
	public ContactResource() {
		DaoFactory.setFactory(new MemDaoFactory());
		dao = DaoFactory.getInstance().getContactDao();
		cc = new CacheControl();
		cc.setMaxAge(3600);
	}
	
	/**
	 * Standard GET method to get all exist contact.
	 * Alternate version is for query parameter, check for contact's name that contain searchText. 
	 * @param searchText is query text to search
	 * @return OK response with entity of ContactList that provide all contact.
	 * 			No Content response if can't find any contact.
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON}) 
	public Response getContact(@QueryParam("title") String searchText) {
		if(searchText==null) {
			ContactList contacts = new ContactList(dao.findAll());
			if(contacts.getContactList().size()>0)
				return Response.ok(contacts).build();
			return Response.noContent().build();
		}
		else {
			List<Contact> cont = dao.findByTitle(searchText);
			ContactList contacts = new ContactList(cont);
			if(contacts.getContactList().size()>0)
				return Response.ok(contacts).build();
			return Response.noContent().build();
		}
	}
	
	/**
	 * GET method with path parameter of id.
	 * @param id to GET specific contact that match the id.
	 * @return OK response with entity of Contact include ETag.
	 * 			Not Modified if If-Match header exist and matches.
	 * 						or If-None-Match header exist and matches
	 * 			No Content if can't find contact.
	 */
	@GET
	@Path("{id}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getContactByID(@PathParam("id") long id, @Context Request req
			,@HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch) {
		Contact contact = dao.find(id);
		if(contact!=null) {
			String tag = contact.hashCode()+"";
			System.out.println("GET: "+tag);
			eTag = new EntityTag(tag);
			if(ifMatch!=null) {
				ifMatch = ifMatch.replace("\"", "");
				if(!(tag.equals(ifMatch))) 
					return Response.status(Status.NOT_MODIFIED).build();
			}
			else if(ifNoneMatch!=null) {
				ifNoneMatch = ifNoneMatch.replace("\"", "");
				if(tag.equals(ifNoneMatch)) 
					return Response.status(Status.NOT_MODIFIED).build();
			}
			return Response.ok(contact).cacheControl(cc).tag(eTag).build();
		}
		return Response.noContent().build();
	}
	
	/**
	 * POST method to create new contact from xml input.
	 * if create success, response created with ETag
	 * if id conflict with exist contact's id, response Conflict
	 * otherwise Bad Request
	 * @param element of contact.
	 * @param uriInfo info of requested uri.
	 * @return Created response with ETag if create success.
	 * 			Conflict if id that try to create is already exist.
	 * 			Bad Request for otherwise.
	 * 	
	 */
	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response createContactXML(JAXBElement<Contact> element, @Context UriInfo uriInfo, @Context Request req) {
		Contact contact = element.getValue();
		if(dao.find(contact.getId())==null) {
			boolean isSuccess = dao.save(contact);
			if( isSuccess ) {
				System.out.println("Create name:" + contact.getName());
				eTag = new EntityTag(contact.hashCode()+"");
				return Response.created(uriInfo.getBaseUriBuilder().path("/contacts/{id}").build(contact.getId())).cacheControl(cc).tag(eTag).build();
			}
			return Response.status(Status.BAD_REQUEST).build();
		}
		else {
			return Response.status(Status.CONFLICT).location(uriInfo.getRequestUri()).entity(contact).build();
		}
	}
	
	/**
	 * PUT method to update contact element.
	 * 
	 * @param id of contact to update.
	 * @param element of contact.
	 * @param uriInfo info of requested uri.
	 * @return OK response if update success.
	 * 			Precondition Failed if If-Match/If-None-Match exist and condition fail
	 * 			Bad Request if update id is not exist.
	 */
	@PUT
	@Path("{id}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateContact(@PathParam("id") long id, JAXBElement<Contact> element, @Context UriInfo uriInfo, @Context Request req
			,@HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch) {
		Contact contact = element.getValue();
		Contact testCon = dao.find(id);
		if(contact!=null&&testCon!=null) {
			String tag = testCon.hashCode()+"";
			eTag = new EntityTag(tag);
			if(ifMatch!=null) {
				ifMatch = ifMatch.replace("\"", "");
				if(!(tag.equals(ifMatch))) 
					return Response.status(Status.PRECONDITION_FAILED).build();
			}
			else if(ifNoneMatch!=null) {
				ifNoneMatch = ifNoneMatch.replace("\"", "");
				if(tag.equals(ifNoneMatch)) 
					return Response.status(Status.PRECONDITION_FAILED).build();
			}
			boolean isSuccess = dao.update(contact);
			if( isSuccess ) {
				eTag = new EntityTag(dao.find(id).hashCode()+"");
				return Response.noContent().cacheControl(cc).tag(eTag).build();
			}
		}
		return Response.status(Status.BAD_REQUEST).build();
	}
	
	/**
	 * DELETE method to delete specific contact.
	 * use if to indicate contact to delete.
	 * @param id of contact.
	 * @return OK response if delete success.
	 * 			Precondition Failed if If-Match/If-None-Match exist and condition fail
	 * 			Bad Request if update id is not exist.
	 */
	@DELETE
	@Path("{id}")
	public Response deleteContact(@PathParam("id") long id, @Context Request req
			,@HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch) {
		Contact testCon = dao.find(id);
		if(testCon!=null) {
			String tag = testCon.hashCode()+"";
			eTag = new EntityTag(tag);
			if(ifMatch!=null) {
				ifMatch = ifMatch.replace("\"", "");
				if(!(tag.equals(ifMatch))) 
					return Response.status(Status.PRECONDITION_FAILED).build();
			}
			else if(ifNoneMatch!=null) {
				ifNoneMatch = ifNoneMatch.replace("\"", "");
				if(tag.equals(ifNoneMatch)) 
					return Response.status(Status.PRECONDITION_FAILED).build();
			}
			boolean isSuccess = dao.delete(id);
			if( isSuccess ) {
				return Response.noContent().build();
			}
		}
		return Response.status(Status.NOT_FOUND).build();
	}

}
