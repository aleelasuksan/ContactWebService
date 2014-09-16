package contact.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * ContactList is entity class for jax-rs.
 * use for be a root of many contact element.
 * @author Atit Leelasuksan 5510546221
 *
 */
@XmlRootElement(name="contacts")
@XmlAccessorType(XmlAccessType.FIELD)
public class ContactList {
	/**
	 * List of contact.
	 */
	@XmlElement(name="contact")
	private List<Contact> contacts;
	
	/**
	 * Default constructor, initialize contacts.
	 */
	public ContactList() {
		contacts = new ArrayList<Contact>();
	}
	
	/**
	 * A constructor with contacts param to import contacts.
	 * use to create object to be entity from List of contact
	 * so didn't need to add each contact.
	 * @param contacts of ContactList.
	 */
	public ContactList(List<Contact> contacts) {
		this.contacts = contacts;
	}
	
	/**
	 * Add contact to ContactList
	 * @param contact to be added.
	 */
	public void addContact(Contact contact) {
		contacts.add(contact);
	}
	
	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}
	
	public List<Contact> getContactList() {
		return contacts;
	}
}
