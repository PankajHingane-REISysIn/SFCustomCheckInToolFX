package com.customcheckin.service.salesforce;

import java.util.ArrayList;
import java.util.List;

import com.customcheckin.service.salesforce.vo.ConfigObjectVO;
import com.customcheckin.service.salesforce.vo.ProjectVO;

/**
 * Singleton class holds all config object names from PMO org
 * @author shahnavazk
 *
 */
public class ConfigObjects {
	private static ConfigObjects instance;
	private List<ConfigObjectVO> customObjects = new ArrayList<>();
	private List<ConfigObjectVO> customSettings = new ArrayList<>();

	private ConfigObjects() {
		init();
	}
	
	public static ConfigObjects getInstance() {
		if (instance == null) {
			instance = new ConfigObjects();
		}
		return instance;
	}
	
	/* PRIVATE METHODS */
	
	public List<ConfigObjectVO> getCustomObjects() {
		return customObjects;
	}

	public List<ConfigObjectVO> getCustomSettings() {
		return customSettings;
	}

	private void init() {		
		ProjectVO project = SalesforcePMOConnection.getInstance().getCurrentProject();
		List<ConfigObjectVO> records = SalesforcePMOConnection.getInstance().getForceDelegate()
				.queryMultiple("Select Name, ObjectType__c, CustomSettingType__c from ConfigObject__c where Project__c=?", new Object[]{project.getId()});
		for (ConfigObjectVO obj : records) {
			if ("Custom Object".equals(obj.getObjectType__c())) {
				customObjects.add(obj);
			}
			else if ("Custom Setting".equals(obj.getObjectType__c())) {
				customSettings.add(obj);
			}
		}
	}
}
