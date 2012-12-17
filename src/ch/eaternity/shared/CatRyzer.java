package ch.eaternity.shared;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import ch.eaternity.server.DAO;
import ch.eaternity.shared.comparators.RezeptDateComparator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.logging.client.HasWidgetsLogHandler;
import com.google.gwt.logging.client.LoggingPopup;
import com.google.gwt.user.client.ui.PopupPanel;



public class CatRyzer {
	
	// -------------- Inner Classes --------------
	public class CatMapping {
		public String category;
		public List<String> hastags = new ArrayList<String>();
		public List<String> hasnotthistags  = new ArrayList<String>();
		
		public CatMapping() {}
	}
	
	public class CatFormula {
		public String category;
		public String formula;
		public boolean isHeading;
		
		public CatFormula() {}
		
		public CatFormula(String category, String formula, boolean isHeading) {
			this.category = category;
			this.formula = formula;
			this.isHeading = isHeading;
		}
		
		public CatFormula(String category, String formula) {
			this.category = category;
			this.formula = formula;
			this.isHeading = false;
		}
	}
	
	public class DateValue {
		public Date date;
		public Double co2value;
		
		public DateValue() {}
		
		public DateValue(Date date, Double co2value) {
			this.date = date;
			this.co2value = co2value;
		}
	}
	
	public class CategoryValue {
		public String categoryName;
		public Double co2value;
		
		public CategoryValue(){}
		
		public CategoryValue(String name, Double co2value) {
			this.categoryName = name;
			this.co2value = co2value;
		}
	}
	
	public class CategoryValuesByDates {
		public List<CategoryValue> category;
		//mulltiple dates are possible, usually just one
		public List<Date> date = new ArrayList<Date>();
		public Double co2value;
		
		public CategoryValuesByDates(){}
		
		public CategoryValuesByDates(List<CategoryValue> category, List<Date> date){
			this.category = category;
			this.date = date;
		}
	}
	
	// -------------- Class Variables --------------
	DAO dao = new DAO();
	Logger rootLogger;
	private List<Recipe> recipes 					= new ArrayList<Recipe>();
	private List<IngredientSpecification> ingSpecs  = new ArrayList<IngredientSpecification>();
	private List<Ingredient> ingredients 			= new ArrayList<Ingredient>();

	private boolean initializedMapping = false;
	private boolean recipesLoaded = false;
	

	private List<DateValue> dateValues 				= new ArrayList<DateValue>();
	private List<CategoryValue> categoryValues 		= new ArrayList<CategoryValue>();
	private List<CategoryValuesByDates> categoryValuesByDatesList = new ArrayList<CategoryValuesByDates>();
	
	public Multimap<String,IngredientSpecification> catMultiMap = HashMultimap.create();
	public List<CatMapping> mappings 				= new ArrayList<CatMapping>();
	
	// -------------- Functions --------------
	// Constructors
	public CatRyzer() {
		//Initialize the Logger
		rootLogger = Logger.getLogger("");
		ingredients = dao.getAllIngredients();
	}
	
	public CatRyzer(List<Recipe> recipes)
	{
		this();
		this.recipes = recipes;
		writeDatesToIngSpec();
		//get all ingredients from all recipes, write into single list
		for (Recipe recipe : recipes){
			ingSpecs.addAll((Collection<IngredientSpecification>)recipe.getZutaten());
		}
		recipesLoaded = true;
	}
	
	
	
	// -------------- Public --------------
	/***
	 * Sets the current mapping of categories to tags
	 * @param str_mappings (Category, Tag1, Tag2, -Tag3, ...)
	 * @throws IllegalArgumentException
	 */
	public void setCatFormulas(List<CatFormula> formulas) throws IllegalArgumentException
	{
		// clear old mapping
		mappings.clear();
					
		for (CatFormula formula : formulas)
		{
			String tag_ar[] = formula.formula.split(",");
			if (tag_ar.length == 0)
				throw new IllegalArgumentException("A Category with no tag is not valid.");
			
			CatMapping newmap = new CatMapping();
			newmap.category = formula.category;
			for (int i = 0; i < tag_ar.length;i++)
			{
				tag_ar[i].trim();
				if (tag_ar[i].charAt(0) == '-')
					newmap.hasnotthistags.add(tag_ar[i].substring(1));
				else
					newmap.hastags.add(tag_ar[i]);
			}
			mappings.add(newmap);
		}
		initializedMapping = true;
	}
	
	// prepares the objects CategoryValue and CategoryValuesByDates
	public void categoryze() throws IllegalStateException{
		if (initializedMapping && recipesLoaded)
		{	
			
			// ---- first populate the dateValue List ------
			Multimap<Date,IngredientSpecification> dateMultiMap = HashMultimap.create();
			
			// iterate over all ingredientSpec, add them to the Map
			for (IngredientSpecification ingSpec : ingSpecs){
				Date date = ingSpec.getCookingDate();
				dateMultiMap.put(date, ingSpec);
			}
						
			// ---- first populate the categoryValue List ------
			// The Multimap could probably substitute categoryValues in the future ...
			// ... when it wouldn't be so darn f*ing complicated to debug ; )
			// String : Category, Long: id of Ingredient
			
			// iterate over all ingredientSpec, add them to the Map
			for (IngredientSpecification ingSpec : ingSpecs){
				if (getIngredient(ingSpec) == null) {
					rootLogger.log(Level.SEVERE, "Ingredient can not be found. Id of IngredientSpecification: " + ingSpec.getId() + " Id of Ingredient: " + ingSpec.getZutat_id());
				}
				else {
					fillCatMultiMap(catMultiMap, ingSpec);
				}
			}
			
			// ---- second populate the CategoryValuesByDates List ------
			Map<Date,Multimap<String,IngredientSpecification>> MapOfcatMultiMap = new HashMap<Date,Multimap<String,IngredientSpecification>>();
			
			for (IngredientSpecification ingSpec : ingSpecs){
				Date date = ingSpec.getCookingDate();
				
				// check if inner Multimap already exist, if not, create one
				Multimap<String,IngredientSpecification> catMM;
				if (!MapOfcatMultiMap.containsKey(date))
					catMM = HashMultimap.create();
				else
					catMM = MapOfcatMultiMap.get(date);
				
				fillCatMultiMap(catMM, ingSpec);
				MapOfcatMultiMap.put(date, catMM);
			}
			
			// sort the set
			List<Date> dateOfKeys = asSortedList(dateMultiMap.keySet());
			
			
			// filling own objects
			for (Date date : dateOfKeys) {
				Collection<IngredientSpecification> ingredientsSpecification = dateMultiMap.get(date);
				dateValues.add(new DateValue(date, getCo2Value(ingredientsSpecification)));
			}
			
			for (CatMapping mapping : mappings) {
				categoryValues.add(new CategoryValue(mapping.category, getCo2Value(catMultiMap.get(mapping.category))));
			}
			
			// sort the set
			List<Date> dateOfKeys2 = asSortedList(MapOfcatMultiMap.keySet());
			
			for(Date date : dateOfKeys2)
			{
				List<CategoryValue> categoryValues = new ArrayList<CategoryValue>();
				
				Multimap<String,IngredientSpecification> catMM = MapOfcatMultiMap.get(date);
				
				for (CatMapping mapping : mappings) {
					categoryValues.add(new CategoryValue(mapping.category, getCo2Value(catMM.get(mapping.category))));
				}
				
				CategoryValuesByDates categoryValuesByDates = new CategoryValuesByDates();
				categoryValuesByDates.date.add(date);
				categoryValuesByDates.category = categoryValues;
				categoryValuesByDates.co2value = 0.0;
				for (CategoryValue catval : categoryValues){
					categoryValuesByDates.co2value = categoryValuesByDates.co2value + catval.co2value;
				}
				categoryValuesByDatesList.add(categoryValuesByDates);
			}
			
			// int i = 1;
			
		
			
		}
		else
			throw new IllegalStateException("Object not initialized");
	}
	
	public List<CategoryValuesByDates> getCatValsByDates() {
		return this.categoryValuesByDatesList;
	}
	
	public List<CategoryValue> getCatVals() {
		return this.categoryValues;	
	}
	
	public List<DateValue> getDateValues() {
		return this.dateValues;
	}
	
	// -------------- Private --------------
	
	
	// ids refer to an IngredientSPecification Object
	// this was getting the co2values of our standart ingredient, not the one from the recipe...
	/*
	private Long getCo2Value(Collection<Long> ids) {
		Long co2value = 0L;
		for (Long id : ids) {
			co2value = co2value + getIngredient(id).getCo2eValue();
		}
		return co2value;
	}
	*/
	/// error end
	
	public static
	<T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
	  List<T> list = new ArrayList<T>(c);
	  Collections.sort(list);
	  return list;
	}
	
	private void fillCatMultiMap(Multimap<String,IngredientSpecification> catMM, IngredientSpecification ingSpec)
	{
		List<String> tags = getIngredient(ingSpec).tags;
		if (tags != null && tags.size() != 0){
			for (String tag : tags) {
				for (CatMapping mapping : mappings) {
					if (mapping.hastags.contains(tag) && doesntContainThisTags(tags, mapping.hasnotthistags)) {
						catMM.put(mapping.category, ingSpec);
					}
				}
			}
		}
	}
	
	private Double getCo2Value(Collection<IngredientSpecification> ingredientsSpecifications) {
		Double co2value = 0.0;
		for (IngredientSpecification ingredientsSpecification : ingredientsSpecifications) {
			co2value = co2value + ingredientsSpecification.getCalculatedCO2Value();
		}
		return co2value;
	}
	
	public Set<String> getIngredientsNames(Collection<IngredientSpecification> ingSpecs){
		Set<String> names = new HashSet<String>();
		for (IngredientSpecification ingSpec : ingSpecs){
			names.add(ingSpec.getName());
		}
		
		return names;
	}
	
	public Set<String> getIngredientsNames_en(Collection<IngredientSpecification> ingSpecs){
		Set<String> names = new HashSet<String>();
		for (IngredientSpecification ingSpec : ingSpecs){
				names.add(getIngredientName_en(ingSpec));
		}	
		return names;
	}
	
	public String getIngredientName_en(IngredientSpecification ingSpec){
		Ingredient ing = getIngredient(ingSpec);
		if (ing == null)
			return ingSpec.getName();
		else if (ing.getSymbol_en() == null)
			return ingSpec.getName() + "(no eng)";
		else
			return ing.getSymbol_en();
	}
	
	//returns null if not found
	private Ingredient getIngredient(IngredientSpecification ingspec) {
		return getIngredient(ingspec.getZutat_id());
	}
	
	//returns null if not found
	private Ingredient getIngredient(Long id){
		for(Ingredient zutat : ingredients){
			if (zutat.getId().equals(id)){
				return zutat;
			}
		}
		return null;
	}
	
	private void writeDatesToIngSpec(){
		Calendar cal = Calendar.getInstance();
		cal.set(2001, 0, 1); //year is as expected, month is zero based, date is as expected
		for (Recipe recipe : recipes){
			for (IngredientSpecification ingSpec : recipe.getZutaten())
			{
				if (recipe.cookingDate != null)
					ingSpec.setCookingDate(recipe.cookingDate);
				else
					ingSpec.setCookingDate(cal.getTime());
				if (getIngredient(ingSpec) == null) {
					rootLogger.log(Level.SEVERE, "Ingredient " + ingSpec.getName() + " can not be found and has thus been removed. Id of Specification: " + ingSpec.getId() + " Id of Ingredient: " + ingSpec.getZutat_id());
					recipe.getZutaten().remove(ingSpec);
				}
			}
		}
	}
	
	// if just on tag of hasnotthistags is contained in tags, don't add the ingredient
	// tags - the object which should not contaion hasnotthistags
	private boolean doesntContainThisTags(List<String> tags, List<String> hasnotthistags){
		boolean doesntContainTags = true; 
		for (String hasnotthistag : hasnotthistags) {
			if (tags.contains(hasnotthistag))
				doesntContainTags = false;
		}
		return doesntContainTags;
	}
	

}