package contact.service.jpa;

import java.util.*;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import java.util.logging.Logger;

import jersey.repackaged.com.google.common.collect.Lists;
import contact.entity.Contact;
import contact.service.ContactDao;

/**
 * Data access object for saving and retrieving contacts,
 * using JPA.
 * To get an instance of this class use:
 * dao = DaoFactory.getInstance().getContactDao()
 * 
 * @author jim, Atit Leelasuksan 5510546221
 */
public class JpaContactDao implements ContactDao {
	/** the EntityManager for accessing JPA persistence services. */
	private final EntityManager em;
	
	/**
	 * constructor with injected EntityManager to use.
	 * @param em an EntityManager for accessing JPA services.
	 */
	public JpaContactDao(EntityManager em) {
		this.em = em;
		//createTestContact( );
	}
	
	/** add contacts for testing. */
	private void createTestContact( ) {
		long id = 101; // usually we should let JPA set the id
		if (find(id) == null) {
			Contact test = new Contact("Test contact", "Joe Experimental", "none@testing.com");
			test.setId(id);
			save(test);
		}
		id++;
		if (find(id) == null) {
			Contact test2 = new Contact("Another Test contact", "Testosterone", "testee@foo.com");
			test2.setId(id);
			save(test2);
		}
	}

	/**
	 * @see contact.service.ContactDao#find(long)
	 */
	@Override
	public Contact find(long id) {
		return em.find(Contact.class, id);  // isn't this sooooo much easier than JDBC?
	}

	/**
	 * @see contact.service.ContactDao#findAll()
	 */
	@Override
	public List<Contact> findAll() {
		Query query = em.createQuery("SELECT c FROM Contact c");
		List<Contact> contacts = query.getResultList();
		return contacts;
	}

	/**
	 * Find contacts whose title contains string
	 * @see contact.service.ContactDao#findByTitle(java.lang.String)
	 */
	@Override
	public List<Contact> findByTitle(String titlestr) {
		// LIKE does string match using patterns.
		Query query = em.createQuery("select c from Contact c where LOWER(c.title) LIKE :title");
		// % is wildcard that matches anything
		query.setParameter("title", "%"+titlestr.toLowerCase()+"%");
		// now why bother to copy one list to another list?
		java.util.List<Contact> result = Lists.newArrayList( query.getResultList() );
		return result;
	}

	/**
	 * @see contact.service.ContactDao#delete(long)
	 */
	@Override
	public boolean delete(long id) {
		Contact contact = find(id);
		if(contact != null) {
			EntityTransaction ett = em.getTransaction();
			try {
				ett.begin();
				em.remove(contact);
				ett.commit();
				return true;
			} catch (EntityExistsException ex) {
				Logger.getLogger(this.getClass().getName()).warning(ex.getMessage());
				if (ett.isActive()) {
					try {
						ett.rollback();
					} catch (Exception e) {}
				}
			}
		}
		return false;
		
	}
	
	/**
	 * @see contact.service.ContactDao#save(contact.entity.Contact)
	 */
	@Override
	public boolean save(Contact contact) {
		if (contact == null) throw new IllegalArgumentException("Can't save a null contact");
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			em.persist(contact);
			tx.commit();
			return true;
		} catch (EntityExistsException ex) {
			Logger.getLogger(this.getClass().getName()).warning(ex.getMessage());
			if (tx.isActive()) try { tx.rollback(); } catch(Exception e) {}
			return false;
		}
	}

	/**
	 * @see contact.service.ContactDao#update(contact.entity.Contact)
	 */
	@Override
	public boolean update(Contact update) {
		if (update == null) throw new IllegalArgumentException("Can't update a null contact");
		if (find(update.getId()) == null) throw new IllegalArgumentException("Can't save a null contact");
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			em.find(Contact.class, update.getId());
			em.merge(update);
			return true;
		} catch (EntityExistsException ex) {
			Logger.getLogger(this.getClass().getName()).warning(ex.getMessage());
			if (tx.isActive()) try { tx.rollback(); } catch(Exception e) {}
			return false;
		}
		
	}
}