package contact.service.mem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import contact.entity.Contact;
import contact.entity.ContactList;
import contact.service.DaoFactory;

/**
 * Manage instances of Data Access Objects (DAO) used in the app.
 * This enables you to change the implementation of the actual ContactDao
 * without changing the rest of your application.
 * 
 * @author jim
 */
public class MemDaoFactory extends DaoFactory {
	// singleton instance of this factory
	private static MemDaoFactory factory;
	private MemContactDao daoInstance;
	
	private MemDaoFactory() {
		daoInstance = new MemContactDao();
		loadFile("c://Contact.xml");
	}
	
	public static MemDaoFactory getInstance() {
		if (factory == null) factory = new MemDaoFactory();
		return factory;
	}
	
	public void loadFile(String filePath) {
		File infile = new File(filePath);
		try {
			JAXBContext context = JAXBContext.newInstance( ContactList.class );
			Unmarshaller um = context.createUnmarshaller();
			ContactList list = (ContactList)um.unmarshal(infile);
			if(list!=null) {
				List<Contact> contacts = list.getContactList();
				if(contacts!=null) {
					for(Contact c: contacts) {
						daoInstance.save(c);
					}
				}
			}
		} catch (JAXBException je) {
			je.printStackTrace();
		}
	}
	
	@Override
	public MemContactDao getContactDao() {
		return daoInstance;
	}

	@Override
	public void shutdown() {
		try {
			JAXBContext context = JAXBContext.newInstance(ContactList.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			List<Contact> contacts = daoInstance.findAll();
			ContactList list = new ContactList(contacts);
			FileOutputStream output = new FileOutputStream("c://Contact.xml");
			marshaller.marshal(list, output);
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}

	}
}
