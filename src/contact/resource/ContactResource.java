package contact.resource;

import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import contact.entity.Contact;
import contact.entity.ContactList;
import contact.service.ContactDao;
import contact.service.DaoFactory;

/**
 * Provide Contact web resource.
 * With GET, GET with param, GET with query, POST, PUT with param, DELETE method.
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
		dao = DaoFactory.getInstance().getContactDao();
	}
	
	/**
	 * Standard GET method to get all exist contact.
	 * Alternate version is for query parameter, check for contact's name that contain searchText. 
	 * @param searchText is query text to search
	 * @return reponse ok with entity of ContactList that provide all contact.
	 */
	@GET
	@Produces(MediaType.TEXT_XML) 
	public Response getContact(@QueryParam("q") String searchText) {
		if(searchText==null) {
			ContactList contacts = new ContactList(dao.findAll());
			return Response.ok(contacts).build();
		}
		else {
			ContactList contacts = new ContactList();
			List<Contact> list = dao.findAll();
			for(Contact contact : list) {
				if(contact.getName().toLowerCase().contains(searchText.toLowerCase())) {
					contacts.addContact(contact);
				}
			}
			return Response.ok(contacts).build();
		}
	}
	
	/**
	 * GET method with path parameter of id.
	 * @param id to GET specific contact that match the id.
	 * @return response ok with entity of Contact.
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.TEXT_XML)
	public Response getContactByID(@PathParam("id") int id) {
		Contact contact = dao.find(id);
		return Response.ok(contact).build();
	}
	
	/**
	 * POST method to create new contact from xml input.
	 * 
	 * @param element of contact.
	 * @param uriInfo info of requested uri.
	 * @return Created response if create success.
	 * 		   N
	 */
	@POST
	@Consumes(MediaType.TEXT_XML)
	public Response createContactXML(JAXBElement<Contact> element, @Context UriInfo uriInfo) {
		Contact contact = element.getValue();
		boolean isSuccess = dao.save(contact);
		if( isSuccess ) {
			System.out.println("Create name:" + contact.getName());
			return Response.created(uriInfo.getAbsolutePath()).build();
		}
		System.out.println("CREATE ERROR");
		return Response.notModified().build();
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
	@Consumes(MediaType.TEXT_XML)
	public Response updateContact(@PathParam("id") int id, JAXBElement<Contact> element, @Context UriInfo uriInfo) {
		Contact contact = element.getValue();
		boolean isSuccess = dao.update(contact);
		if( isSuccess ) {
			System.out.println("Update id:"+id);
			return Response.ok(uriInfo.getRequestUri()).build();
		}
		System.out.println("UPDATE ERROR");
		return Response.notModified().build();
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
	public Response deleteContact(@PathParam("id") int id) {
		boolean isSuccess = dao.delete(id);
		if( isSuccess ) {
			System.out.println("Delete id:" + id);
			return Response.ok().build();
		}
		System.out.println("DELETE ERROR");
		return Response.notModified().build();
	}
	
}
