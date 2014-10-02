package contact.entity;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * A person is a contact with a name, title, and email.
 * title is text to display for this contact in a list of contacts,
 * such as a nickname or company name.
 * @author jim, Atit Leelasuksan 5510546221
 */
@Entity
@Table(name="CONTACTS")
@XmlRootElement(name="contact")
@XmlAccessorType(XmlAccessType.FIELD)
public class Contact implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@XmlAttribute
	private long id;
	@NotNull
	@XmlElement(required=true,nillable=false)
	private String title;
	@XmlElement
	private String name;
	@XmlElement
	private String email;
	@XmlElement
	/** URL of photo */
	private String photoUrl;
	
	/** Create a new contact with no data.  Intended for use by persistence framework. */
	public Contact() {
		
	}
	
	/** Create a new contact with the given title, name, and email address. */
	public Contact(String title, String name, String email ) {
		this.title = title;
		this.name = name;
		this.email = email;
		this.photoUrl = "";
	}

	public Contact(long id) {
		this.id = id;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photo) {
		this.photoUrl = photo;
	}

  
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return String.format("%s: %s <%s> (%d)", title, name, email, id);
	}
	
	/** Two contacts are equal if they have the same id,
	 * even if other attributes differ.
	 * @param other another contact to compare to this one.
	 */
	public boolean equals(Object other) {
		if (other == null || other.getClass() != this.getClass()) return false;
		Contact contact = (Contact) other;
		return contact.getId() == this.getId();
	}
	
	/**
	 * Copy another contact's data into this contact.
	 * The id of this contact is not changed.  This allows
	 * complete updates of an existing contact without
	 * changing the object's identity.
	 * @param other another Contact whose fields are copied to this contact.
	 */
	public void copyOf(Contact other) {
		if (other == null) throw new IllegalArgumentException("source contact may not be null");
		// don't check the id value. Its the caller's responsibility to supply correct argument
		this.setTitle(other.getTitle()); 
		this.setName(other.getName()); 
		this.setEmail(other.getEmail());
		this.setPhotoUrl(other.getPhotoUrl());
	}
	
	/**
	 * Update this contact's data from another Contact.
	 * The id field of the update must either be 0 or the same value as this contact!
	 * @param update the source of update values
	 */
	public void applyUpdate(Contact update) {
		if (update == null) return;
		if (update.getId() != 0 && update.getId() != this.getId() )
			throw new IllegalArgumentException("Update contact must have same id as contact to update");
		// Since title is used to display contacts, don't allow empty title
		if (! isEmpty( update.getTitle()) ) this.setTitle(update.getTitle()); // empty nickname is ok
		// value can be null.
		this.setName(update.getName()); 
		this.setEmail(update.getEmail());
		this.setPhotoUrl(update.getPhotoUrl());
	}
	
	/**
	 * Test if a string is null or only whitespace.
	 * @param arg the string to test
	 * @return true if string variable is null or contains only whitespace
	 */
	private static boolean isEmpty(String arg) {
		return arg == null || arg.matches("\\s*") ;
	}
	
	@Override
	public int hashCode() {
		return (this.id+this.title+this.name+this.email+this.photoUrl).hashCode();
	}
}