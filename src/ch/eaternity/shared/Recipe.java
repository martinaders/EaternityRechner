package ch.eaternity.shared;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import javax.persistence.Id;
import javax.persistence.Transient;

import com.googlecode.objectify.annotation.Serialized;


public class Recipe implements Serializable, Cloneable{
 

	private static final long serialVersionUID = -5888386800366492104L;

	@Id Long id;
    
	private String symbol;
	
	private String subTitle;
	
	private String cookInstruction;
	
	public UploadedImage image;
	
	private String emailAddressOwner;
	
	private Long persons;
	
	private Date createDate;
	private Long hits;
	private Long popularity;
	
	@Transient
	private Boolean selected = false;
	
    // @Persistent //(mappedBy = "recipe") //, defaultFetchGroup = "true")
//    @Element(dependent = "true")
	@Serialized
	public ArrayList<IngredientSpecification> Zutaten = new ArrayList<IngredientSpecification>();
    
//    @Persistent 
//    private List<String> ZutatSpecificationKeys = new ArrayList<String>(); 
    
	private Double CO2Value;
	public Boolean openRequested;
	public Boolean open;
	public Boolean eaternitySelected;
	public Boolean bio;
	public Boolean regsas;

	public Recipe() {

	}

	public Recipe(String symbol) {
		this.symbol = symbol;
	}

	public Recipe(Long id, String symbol) {
		this();

		this.symbol = symbol;
	}



	public String getSymbol() {
		return this.symbol;
	}





	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void addZutaten(List<IngredientSpecification> zutaten) {
		for(IngredientSpecification zutat : zutaten){
//			zutat.setRezept(this);
			this.Zutaten.add(zutat);
		}
		
	}

	public ArrayList<IngredientSpecification> getZutaten() {
		return this.Zutaten;
	}

	public void setZutaten(ArrayList<IngredientSpecification> zutaten) {
			this.Zutaten = zutaten;

	}
	
	public void removeZutat(int index) {
		this.Zutaten.remove(index);
	}


	public void setOpen(boolean open) {
		this.open = open;
	}

	public boolean isOpen() {
		return open;
	}

	public void setHits(Long hits) {
		this.hits = hits;
	}

	public Long getHits() {
		return hits;
	}

	public void setPopularity(Long popularity) {
		this.popularity = popularity;
	}

	public Long getPopularity() {
		return popularity;
	}
	public Recipe getRezept(){
		return this;
	}

	

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCO2Value() {
		double sum = 0;
		for ( IngredientSpecification zutatSpec : Zutaten){
			sum += zutatSpec.getCalculatedCO2Value();
		}
		if(persons != null){
			CO2Value = sum/persons;
		} else {
			CO2Value = sum;
		}
	}

	public double getCO2Value() {
		return CO2Value;
	}

	public void setCookInstruction(String cookInstruction) {
		this.cookInstruction = cookInstruction;
	}

	public String getCookInstruction() {
		return cookInstruction;
	}

	public void setPersons(Long persons) {
		this.persons = persons;
	}

	public Long getPersons() {
		return persons;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public Boolean getSelected() {
		return selected;
	}

	public void setEmailAddressOwner(String emailAddressOwner) {
		this.emailAddressOwner = emailAddressOwner;
	}

	public String getEmailAddressOwner() {
		return emailAddressOwner;
	}





}