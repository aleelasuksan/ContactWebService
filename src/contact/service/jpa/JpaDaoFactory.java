package contact.service.jpa;

import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import contact.service.*;
import contact.service.mem.MemContactDao;

/**
 * JpaDaoFactory is a factory for DAO that use the Java Persistence API (JPA)
 * to persist objects.
 * The factory depends on the configuration information in META-INF/persistence.xml.
 * 
 * @see contact.service.DaoFactory
 * @version 2014.09.19
 * @author jim
 */
public class JpaDaoFactory extends DaoFactory {
	private static final String PERSISTENCE_UNIT = "contacts";
	/** instance of the entity DAO */
	private ContactDao contactDao;
	private static JpaDaoFactory factory;
	private final EntityManagerFactory emf;
	private EntityManager em;
	private static Logger logger;
	
	static {
		logger = Logger.getLogger(JpaDaoFactory.class.getName());
	}
	
	protected JpaDaoFactory() {
		emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
		em = emf.createEntityManager();
		contactDao = new JpaContactDao( em );
	}
	
	/**
	 * Get a singleton instance of the DaoFactory.
	 * @return instance of a concrete DaoFactory
	 */
	public static JpaDaoFactory getInstance() {
		if(factory == null) factory = new JpaDaoFactory();
		return factory;
	}
	
	@Override
	public ContactDao getContactDao() {
		return contactDao;
	}
	
	@Override
	public void shutdown() {
		try {
			if (em != null && em.isOpen()) em.close();
			if (emf != null && emf.isOpen()) emf.close();
		} catch (IllegalStateException ex) {
			logger.warning(ex.getMessage());
		}
	}
}