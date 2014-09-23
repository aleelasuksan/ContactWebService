package contact.service;

import java.util.List;

import contact.entity.Contact;
/**
 * Interface defines the operations required by 
 * a DAO for Contacts.
 * 
 * @author jim
 */
public interface ContactDao {

	/** Find a contact by ID in contacts.
	 * @param the id of contact to find
	 * @return the matching contact or null if the id is not found
	 */
	public abstract Contact find(long id);

	/**
	 * Return all the persisted contacts as a List.
	 * There is no guarantee what implementation of
	 * List is returned, so caller should use only
	 * List methods (not, say ArrayList).
	 * @return list of all contacts in persistent storage.
	 *   If no contacts, returns an empty list.
	 */
	public abstract List<Contact> findAll();
	
	/**
	 * Find a contact whose title starts with the  
	 * string parameter (the way Gmail does).
	 * @param prefix a string containing the start 
	 * of a contact title.  Must not be null.
	 * @return List of matching contacts. Return an empty list
	 * if no matches.
	 */
	public abstract List<Contact> findByTitle(String prefix);

	/**
	 * Delete a saved contact by id.
	 * @param id the id of contact to delete. Should be positive.
	 * @return true if contact is deleted, false otherwise.
	 */
	public abstract boolean delete(long id);

	/**
	 * Save or replace a contact.
	 * If the contact.id is 0 then it is assumed to be a
	 * new (not saved) contact.  In this case a unique id
	 * is assigned to the contact.  
	 * If the contact id is not zero and there is a saved
	 * contact with same id, then the old contact is replaced.
	 * @param contact the contact to save or replace.
	 * @return true if saved successfully
	 */
	public abstract boolean save(Contact contact);

	/**
	 * Update a Contact.  If the contact with same id
	 * as the update is already in persistent storage,
	 * then all fields of the contact are replaced with
	 * values in the update (including null values!).
	 * The id of the update must match the id of a contact
	 * already persisted.  If not, false is returned.
	 * @param update update info for the contact.
	 * @return true if the update is applied successfully.
	 */
	public abstract boolean update(Contact update);


}