package contact.service.mem;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import contact.entity.Contact;
import contact.service.ContactDao;

/**
 * Data access object for saving and retrieving contacts.
 * This DAO uses an in-memory list of person.
 * Use DaoFactory to get an instance of this class, such as:
 * dao = DaoFactory.getInstance().getContactDao()
 * 
 * @author jim
 */
public class MemContactDao implements ContactDao {
	private List<Contact> contacts;
	private AtomicLong nextId;
	
	public MemContactDao() {
		contacts = new ArrayList<Contact>();
		nextId = new AtomicLong(1000L);
		//createTestContact(1);
	}
	
	/** add a single contact with given id for testing. */
	private void createTestContact(long id) {
		Contact test = new Contact("Test contact", "Joe Experimental", "none@testing.com");
		test.setId(id);
		contacts.add(test);
	}

	@Override
	public Contact find(long id) {
		for(Contact c : contacts) 
			if (c.getId() == id) return c;
		return null;
	}

	@Override
	public List<Contact> findAll() {
		return java.util.Collections.unmodifiableList(contacts);
	}

	@Override
	public boolean delete(long id) {
		for(int k=0; k<contacts.size(); k++) {
			if (contacts.get(k).getId() == id) {
				contacts.remove(k);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean save(Contact contact) {
		if (contact.getId() == 0) {
			contact.setId( getUniqueId() );
			return contacts.add(contact);
		}
		// check if this contact is already in persistent storage
		Contact other  = find(contact.getId());
		if (other == contact) return true;
		if ( other != null ) contacts.remove(other);
		return contacts.add(contact);
	}

	@Override
	public boolean update(Contact update) {
		Contact contact = find(update.getId());
		if (contact == null) return false;
		contact.applyUpdate(update);
		save(contact);
		return true;
	}
	
	/**
	 * Get a unique contact ID.
	 * @return unique id not in persistent storage
	 */
	private synchronized long getUniqueId() {
		long id = nextId.getAndAdd(1L);
		while( id < Long.MAX_VALUE ) {	
			if (find(id) == null) return id;
			id = nextId.getAndAdd(1L);
		}
		return id; // this should never happen
	}

	@Override
	public List<Contact> findByTitle(String prefix) {
		List<Contact> found = new ArrayList<Contact>();
		Pattern patt = Pattern.compile(".*"+prefix+".*", Pattern.CASE_INSENSITIVE);
		for(Contact c: contacts) {
			if(patt.matcher(c.getTitle()).matches())
				found.add(c);
		}
		return found;
	}
}
