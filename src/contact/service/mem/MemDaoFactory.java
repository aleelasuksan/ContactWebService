package contact.service.mem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
 * @author jim, Atit Leelasuksan 5510546221
 */
public class MemDaoFactory extends DaoFactory {
//JIM Your filename (c:/Contact.xml" will fail if not using Windows
	private static final String CONTACTS_FILE = "Contact.xml";

	private MemContactDao daoInstance;
	
	public MemDaoFactory() {
		daoInstance = new MemContactDao();
//JIM Same filename is used in several places. Use named constant instead.
		loadFile(CONTACTS_FILE);
	}
	
	/**
	 * load data from file by unmarshall file locate at filePath
	 * then add all contact to DAO
	 * @param filePath path of file to load.
	 */
	public void loadFile(String filePath) {
		File infile = new File(filePath);
		if (! infile.exists() ) return;
		
		try {
			JAXBContext context = JAXBContext.newInstance( ContactList.class );
			Unmarshaller um = context.createUnmarshaller();
//JIM Throws exception if file doesn't exist!
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
			FileOutputStream output = new FileOutputStream(CONTACTS_FILE);
			marshaller.marshal(list, output);
			output.close();
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException iex) {
			iex.printStackTrace();
		}
	}
	
}
