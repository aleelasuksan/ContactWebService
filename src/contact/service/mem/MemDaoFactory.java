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
import contact.service.ContactDao;
import contact.service.DaoFactory;

/**
 * Manage instances of Data Access Objects (DAO) used in the app.
 * This enables you to change the implementation of the actual ContactDao
 * without changing the rest of your application.
 * 
 * in case that you want to save a xml file of contacts,
 * you need to setFilepath before you shutdown the factory.
 * @author jim, Atit Leelasuksan 5510546221
 */
public class MemDaoFactory extends DaoFactory {

	private ContactDao dao;
	/**
	 * A path to load and save .xml file of contacts.
	 */
	private String contacts_file;
	
	/**
	 * Default method to initialize dao without input file.
	 */
	public MemDaoFactory() {
		dao = new MemContactDao();
	}
	
	/**
	 * Initialize dao with input file.
	 * @param filepath of input file
	 */
	public MemDaoFactory(String filepath) {
		this();
		this.contacts_file = filepath;
		loadFile(filepath);
	}
	
	/**
	 * load data from file by unmarshall file locate at filePath
	 * then add all contact to DAO
	 * @param filePath path of file to load.
	 * @throws FileNotFoundException 
	 */
	public void loadFile(String filePath) throws FileNotFoundException {
		File infile = new File(filePath);
		if(!infile.exists()) throw new FileNotFoundException();
		try {
			JAXBContext context = JAXBContext.newInstance( ContactList.class );
			Unmarshaller um = context.createUnmarshaller();
			ContactList list = (ContactList)um.unmarshal(infile);
			if(list!=null) {
				List<Contact> contacts = list.getContactList();
				if(contacts!=null) {
					for(Contact c: contacts) {
						dao.save(c);
					}
				}
			}
		} catch (JAXBException je) {
			je.printStackTrace();
		}
	}
	
	@Override
	public ContactDao getContactDao() {
		return dao;
	}

	@Override
	public void shutdown() {
		if(contacts_file!=null) {
			try {
				JAXBContext context = JAXBContext.newInstance(ContactList.class);
				Marshaller marshaller = context.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				List<Contact> contacts = dao.findAll();
				ContactList list = new ContactList(contacts);
				FileOutputStream output = new FileOutputStream(contacts_file);
				marshaller.marshal(list, output);
				output.close();
			} catch (JAXBException e) {
				e.printStackTrace();
			} catch (FileNotFoundException ex ) {
				ex.printStackTrace();
			} catch (IOException iex) {
				iex.printStackTrace();
			}
		}
	}
}
