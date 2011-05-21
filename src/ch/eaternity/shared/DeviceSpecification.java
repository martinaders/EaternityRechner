package ch.eaternity.shared;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.persistence.Embedded;
import javax.persistence.Id;

import com.googlecode.objectify.annotation.Serialized;


public class DeviceSpecification implements Serializable {

	/** 
	 * 
	 */
	private static final long serialVersionUID = 3172640409035191698L;

	@Id String id;
     
	 
	public Double kWConsumption;
	
	 
	public String deviceName;
	
	 
	public String deviceSpec;
	
	 
	public Long duration;
	
	

	public DeviceSpecification() {

	}

	public DeviceSpecification(String deviceName,String deviceSpec, Double kWConsumption, Long duration) {
		this.deviceName = deviceName;
		this.deviceSpec = deviceSpec;
		this.kWConsumption = kWConsumption;
		this.duration =duration;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}


	

	  

}
