package com.customcheckin.model;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PMOLogin")
public class PMOLogin {
	@XmlElement(name = "userName")
	public String userName;
	@XmlElement(name = "password")
	public String password;
	
	public PMOLogin() {
		
	}
	public PMOLogin(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	
	
public static void main(String[] args) throws JAXBException {
	/*JAXBContext context = JAXBContext
            .newInstance(PMOLogin.class);
    Marshaller m = context.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    PMOLogin p =new PMOLogin("Pankaj", "Pass");
    m.marshal(p, new File("D:\\temp.xml"));*/
	
	JAXBContext context = JAXBContext
            .newInstance(PMOLogin.class);
    Unmarshaller um = context.createUnmarshaller();

    // Reading XML from the file and unmarshalling.
    PMOLogin wrapper = (PMOLogin) um.unmarshal(new File("D:\\temp.xml"));
    System.out.println(wrapper.userName);
    
}
}
