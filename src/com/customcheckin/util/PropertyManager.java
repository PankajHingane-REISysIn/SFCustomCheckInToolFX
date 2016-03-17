package com.customcheckin.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.lib.util.PropertyLoader;

public class PropertyManager extends PropertyLoader {
	private static final String PROPERTY_FILE = "config.properties";
	private static PropertyManager instance;
	private static Logger log = Logger.getRootLogger();
	private Properties prop;

	private PropertyManager() {
		this.prop = super.loadProperties(PROPERTY_FILE);
	}

	public static PropertyManager getInstance() {
		if (instance == null) {
			instance = new PropertyManager();
		}
		return instance;
	}

	public String getString(String key) {
		return prop.getProperty(key);
	}

	public Integer getInteger(String key) {
		return Integer.valueOf(getString(key));
	}

	public Double getDouble(String key) {
		return Double.valueOf(getString(key));
	}
	
	public void setString(String key, String value) {
		prop.setProperty(key, value);
	}
	
	public void storePropertyFile() {
		OutputStream output = null;

		try {
			String path = findCurrentFolderName();
			path = path.replace("\\bin", "\\src\\");
			output = new FileOutputStream(path + PROPERTY_FILE);
			prop.store(output, null);

		} catch (IOException io) {
			log.error(io);
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					log.error(e);
				}
			}
		}
	}

	/* PRIVATE METHODS */
	
    public static synchronized String findCurrentFolderName() {
    	PropertyManager f = new PropertyManager();
        Class clazz = f.getClass();
        try {
            java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
            String location = url.toString();
            if (location.startsWith("file")) {
                java.io.File file = new java.io.File(url.getFile());
                String ret = file.getAbsolutePath();
                return removeSpecialChars(ret);
            }
			return removeSpecialChars(url.toString());
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
    
    private static String removeSpecialChars(String data) {
    	return data.replaceAll("%20", " ");
    }
	public static void main(String[] args) {
		log.info(PropertyManager.getInstance().getString("pmo.username"));
		PropertyManager.getInstance().setString("pmo.username", "efg");
		PropertyManager.getInstance().storePropertyFile();
	}

}
