package contact.resource;

import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import org.eclipse.jetty.http.HttpHeader;

import contact.entity.Contact;
import contact.entity.ContactList;
import contact.service.ContactDao;
import contact.service.DaoFactory;
import contact.service.mem.MemDaoFactory;

/**
 * Provide Contact web resource that response to HTTP request
 * With GET, GET with parameter, GET with query, POST, PUT with parameter, DELETE method.
 * now GET with id parameter, POST and PUT support ETag Header.
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
	 * @return reponse ok with entity of ContactList that provide all contact.
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
	 * @return response ok with entity of Contact.
	 */
	@GET
	@Path("{id}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getContactByID(@PathParam("id") long id, @Context Request req) {
		Contact contact = dao.find(id);
		if(contact!=null) {
			eTag = new EntityTag(contact.getLastUpdate().hashCode()+"");
			rb = req.evaluatePreconditions(eTag);
			if(rb!=null) {
				return rb.status(Status.NOT_MODIFIED).cacheControl(cc).tag(eTag).build();
			}
			return Response.ok(contact).cacheControl(cc).tag(eTag).build();
		}
		return Response.noContent().build();
	}
	
	/**
	 * POST method to create new contact from xml input.
	 * 
	 * @param element of contact.
	 * @param uriInfo info of requested uri.
	 * @return Created response if create success.
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
				eTag = new EntityTag(contact.getLastUpdate().hashCode()+"");
				return Response.created(uriInfo.getBaseUriBuilder().path("/contacts/{id}").build(contact.getId())).cacheControl(cc).tag(eTag).build();
			}
			System.out.println("CREATE ERROR");
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
	 * @return OK Response if update success.
	 * 		   
	 */
	@PUT
	@Path("{id}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateContact(@PathParam("id") long id, JAXBElement<Contact> element, @Context UriInfo uriInfo, @Context Request req) {
		Contact contact = element.getValue();
		Contact testCon = dao.find(id);
		eTag = new EntityTag(testCon.getLastUpdate().hashCode()+"");
		rb = req.evaluatePreconditions(eTag);
		if(rb==null) return Response.status(Status.PRECONDITION_FAILED).build();
		if(contact!=null) {
			contact.setId(id);
			boolean isSuccess = dao.update(contact);
			if( isSuccess ) {
				System.out.println("Update id:"+id);
				return Response.ok().cacheControl(cc).tag(eTag).build();
			}
		}
		System.out.println("UPDATE ERROR");
		return Response.status(Status.BAD_REQUEST).build();
	}
	
	/**
	 * DELETE method to delete specific contact.
	 * use if to indicate contact to delete.
	 * @param id of contact.
	 * @return OK Response if delete success.
	 * 		   
	 */
	@DELETE
	@Path("{id}")
	public Response deleteContact(@PathParam("id") long id, @Context Request req) {
		Contact testCon = dao.find(id);
		if(testCon!=null) {
			eTag = new EntityTag(testCon.getLastUpdate().hashCode()+"");
			rb = req.evaluatePreconditions(eTag);
			if(rb==null) return Response.status(Status.PRECONDITION_FAILED).build();
			boolean isSuccess = dao.delete(id);
			if( isSuccess ) {
				System.out.println("Delete id:" + id);
				return Response.ok().build();
			}
		}
		System.out.println("DELETE ERROR");
		return Response.status(Status.BAD_REQUEST).build();
	}

}
