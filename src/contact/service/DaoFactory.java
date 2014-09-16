package contact.service;

/**
 * Manage instances of Data Access Objects (DAO) used in the app.
 * This enables you to change the implementation of the actual ContactDao
 * without changing the rest of your application.
 * 
 * @author jim
 */
public class DaoFactory {
	// singleton instance of this factory
	private static DaoFactory factory;
	private ContactDao daoInstance;
	
	private DaoFactory() {
		daoInstance = new ContactDao();
	}
	
	public static DaoFactory getInstance() {
		if (factory == null) factory = new DaoFactory();
		return factory;
	}
	
	public ContactDao getContactDao() {
		return daoInstance;
	}
}
