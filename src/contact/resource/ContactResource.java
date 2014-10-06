package contact.resource;

import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
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
	
	/**
	 * Initialize Resource and Contact Data Access Object.
	 */
	public ContactResource() {
// The decision to use a particular factory shouldn't be done here.
// Put in Main class instead.
		DaoFactory.setFactory(new MemDaoFactory());
		dao = DaoFactory.getInstance().getContactDao();
	}
	
	/**
	 * Standard GET method to get all persisted contacts.
	 * Alternate version is for query parameter, check for contact's name that contain searchText. 
	 * @param searchText is query text to search
	 * @return reponse ok with entity of ContactList that provide all contact.
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON}) 
	public Response getContact(@QueryParam("title") String searchText) {
		
		//Jim: you should remove these println before submitting code
		System.out.println("---GET---");
		if(searchText==null) {
			ContactList contacts = new ContactList(dao.findAll());
			if(contacts.getContactList().size()>0)
				return Response.ok(contacts).build();
			return Response.noContent().build();
		}
		else {
			List<Contact> cont = dao.findByTitle(searchText);
			ContactList contacts = new ContactList(cont);
//ERROR: it is OK for contacts to be empty.  should return 200 and body <contacts></contacts>
			if(contacts.getContactList().size()>0)
				return Response.ok(contacts).build();
//Wrong response code
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
	public Response getContactByID(@PathParam("id") long id) {
		System.out.println("---GETID---");
		Contact contact = dao.find(id);
		if(contact!=null)
			return Response.ok(contact).build();
//ERROR: wrong response code. should be NOT FOUND
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
	public Response createContactXML(JAXBElement<Contact> element, @Context UriInfo uriInfo) {
		System.out.println("---POST---");
		Contact contact = element.getValue();
// Convoluted logic. Rearrange to eliminate nested if.
// In the most common case, contact.getId() is 0 and you don't need to bother
// querying DAO.  Since DAO may take time, more efficient to test for getId() == 0 first.
		if(dao.find(contact.getId())==null) {
			boolean isSuccess = dao.save(contact);
			if( isSuccess ) {
				System.out.println("Create name:" + contact.getName());
//JIM: Don't hardcode the "/contacts/" into path. Use uriInfo to get request uri. 
				return Response.created(uriInfo.getBaseUriBuilder().path("/contacts/{id}").build(contact.getId())).build();
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
	public Response updateContact(@PathParam("id") long id, JAXBElement<Contact> element, @Context UriInfo uriInfo) {
		System.out.println("---PUT---");
		Contact contact = element.getValue();
		if(contact!=null) {
			contact.setId(id);
			boolean isSuccess = dao.update(contact);
			if( isSuccess ) {
				System.out.println("Update id:"+id);
				return Response.ok().build();
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
	public Response deleteContact(@PathParam("id") long id) {
		System.out.println("---DELETE---");
		boolean isSuccess = dao.delete(id);
		if( isSuccess ) {
			System.out.println("Delete id:" + id);
			return Response.ok().build();
		}
//ERROR: if contact doesn't exist it should return NOT_FOUND
		System.out.println("DELETE ERROR");
		return Response.status(Status.BAD_REQUEST).build();
	}

}
