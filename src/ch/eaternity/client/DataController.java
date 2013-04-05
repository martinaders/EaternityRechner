package ch.eaternity.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import ch.eaternity.client.events.*;
import ch.eaternity.shared.*;
import ch.eaternity.shared.Util.RecipeScope;

import com.allen_sauer.gwt.log.client.Log;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Manages clientside data object, keeps it in sync with cloud and fires events
 */
public class DataController {
	
	// ---------------------- Class Variables ----------------------
	
	private ClientFactory clientFactory;
	private DataServiceAsync dataRpcService;
	private EventBus eventBus;
	
	// here is the database of all data pushed to....
	private ClientData cdata = new ClientData();
	private boolean dataLoaded = false;
	
	private ListDataProvider<RecipeInfo> recipeDataProvider = new ListDataProvider<RecipeInfo>();

	// ---------------------- public Methods ----------------------
	
	public DataController () {}
	
	public void setClientFactory(ClientFactory factory) {
		this.clientFactory = factory;
		this.dataRpcService = factory.getDataServiceRPC();
		this.eventBus = factory.getEventBus();
	}
	
	public boolean dataLoaded() {
		return dataLoaded;
	}
	
	public void loadData() {
		eventBus.fireEvent(new SpinnerEvent(true, "Daten werden geladen"));
		dataRpcService.getData(GWT.getHostPageBaseURL(), new AsyncCallback<ClientData>() {
			public void onFailure(Throwable error) {
				eventBus.fireEvent(new SpinnerEvent(false));
				handleError(error);
			}
			public void onSuccess(ClientData data) {
				// the data objects holds all the data	
				cdata = data;
				
				//recipeDataProvider.setList(cdata.recipeInfos);
				
				// Load currentKitchen via ID
				boolean kitchenLoaded = true;
				if (cdata.userInfo != null && cdata.userInfo.getCurrentKitchen() != null)
				{
					cdata.currentKitchen = cdata.getKitchenByID(cdata.userInfo.getCurrentKitchen());
					if (cdata.currentKitchen == null)
						kitchenLoaded = false;
				}
				else
					kitchenLoaded = false;
				
				if (kitchenLoaded) {
					changeKitchen(cdata.currentKitchen.getId());
				}
				
				
				// Load current Month or Kitchen Month
				Date date = new Date();
				cdata.currentMonth = date.getMonth() + 1;
				eventBus.fireEvent(new MonthChangedEvent(cdata.currentMonth));
				
				if (cdata.userInfo != null)
					eventBus.fireEvent(new LoginChangedEvent(cdata.userInfo));
				
				eventBus.fireEvent(new SpinnerEvent(false));
				eventBus.fireEvent(new LoadedDataEvent());
			}
			
		});
		dataLoaded = true;
	}
	
	// --------------------- Methods accessed by SubViews --------------------- 
	
	public void createRecipe() {
		Recipe recipe = new Recipe();
		setRecipeParameters(recipe);
	}
	
	public void cloneRecipe(Recipe toclone) {
		Recipe recipe = new Recipe(toclone);
		setRecipeParameters(recipe);
	}
	
	private void setRecipeParameters(Recipe recipe) {
		recipe.setCreateDate(new Date());
		
		if (cdata.currentKitchen != null)
			recipe.setKitchenId(cdata.currentKitchen.getId());
		
		if (cdata.userInfo != null) {
			recipe.setUserId(cdata.userInfo.getId());
		}
		
		recipe.setPopularity(0L);
		recipe.setHits(0L);
		
		cdata.editRecipe = recipe;
		eventBus.fireEvent(new RecipeLoadedEvent(recipe)); 
	}
	
	
	
	public void saveRecipe(final Recipe recipe) { 
		eventBus.fireEvent(new SpinnerEvent(true, "speichern"));
		dataRpcService.saveRecipe(recipe, new AsyncCallback<Long>() {
			public void onFailure(Throwable error) {
				handleError(error);
				eventBus.fireEvent(new SpinnerEvent(false));
			}

			public void onSuccess(Long id) {
				eventBus.fireEvent(new SpinnerEvent(false));
							
				recipe.setId(id);
				RecipeInfo recipeInfo = cdata.getRecipeById(recipe.getId());

				if (recipeInfo != null) {
					//cdata.recipeInfos.remove(recipeInfo);
					recipeInfo.update(recipe);
				}
				else
				{
					if (recipe.getKitchenId() != null && cdata.recipeScope == RecipeScope.KITCHEN && recipe.getKitchenId().equals(cdata.currentKitchen.getId())) {
						cdata.recipeInfos.add(new RecipeInfo(recipe));
					}
					else if (recipe.getKitchenId() == null && cdata.recipeScope == RecipeScope.USER) {
						cdata.recipeInfos.add(new RecipeInfo(recipe));
					}
					else if (recipe.getOpen() && cdata.recipeScope == RecipeScope.PUBLIC) {
						cdata.recipeInfos.add(new RecipeInfo(recipe));
					}
				}
				//TODO change refresh() (something isnt working with refresh if list.add is used)
				recipeDataProvider.setList(cdata.recipeInfos);
				eventBus.fireEvent(new AlertEvent("Rezept gespeichert.", AlertType.INFO, AlertEvent.Destination.BOTH, 2000));
			}
		});
	}
	
	public void searchRecipes(RecipeSearchRepresentation search) {
		dataRpcService.searchRecipes(search, new AsyncCallback<ArrayList<RecipeInfo>>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}

			public void onSuccess(ArrayList<RecipeInfo> recipeInfos) {
				if (recipeInfos != null){
					cdata.recipeInfos.clear();
					cdata.recipeInfos.addAll(recipeInfos);
					recipeDataProvider.setList(cdata.recipeInfos);
				}
			}
		});
	}
	
	public void deleteRecipe(final Long recipeId) {
		eventBus.fireEvent(new SpinnerEvent(true, "löschen"));
		dataRpcService.deleteRecipe(recipeId, new AsyncCallback<Boolean>() {
			public void onFailure(Throwable error) {
				handleError(error);
				eventBus.fireEvent(new SpinnerEvent(false));
			}
			public void onSuccess(Boolean ignore) {
				eventBus.fireEvent(new SpinnerEvent(false));
				cdata.recipeInfos.remove(cdata.getRecipeById(recipeId));
				recipeDataProvider.refresh();
				if (cdata.editRecipe.getId().equals(recipeId))
					cdata.editRecipe = null;
			}
		});
	}
	
	/**
	 * After call to this function, editRecipe is != null for sure
	 * @param id
	 */
	public void setEditRecipe(String idStr) {
		
		if (idStr == null || (idStr != null && (idStr.equals("") || idStr.equals("new")))) {
			createRecipe();
		}
		else {
			try {
				final Long id = Long.parseLong(idStr);
				eventBus.fireEvent(new SpinnerEvent(true, "Rezept laden"));
				dataRpcService.getRecipe(id, new AsyncCallback<Recipe>() {
					public void onFailure(Throwable error) {
						createRecipe();
						eventBus.fireEvent(new SpinnerEvent(false));
						if (error instanceof NotLoggedInException)
							eventBus.fireEvent(new AlertEvent("Das Rezept mit ID " + id + " ist dem aktuellen User nicht zugeordnet. Neues Rezept erstellt.", AlertType.ERROR, AlertEvent.Destination.EDIT)); 
						else if (error instanceof NoSuchElementException)
							eventBus.fireEvent(new AlertEvent("Nicht alle Zutaten vom Rezept konnten geladen werden. Neues Rezept erstellt. ", AlertType.ERROR, AlertEvent.Destination.EDIT)); 
						
						else
							eventBus.fireEvent(new AlertEvent("Rezept mit ID " + id + " konnte nicht geladen werden. Neues Rezept erstellt.", AlertType.ERROR, AlertEvent.Destination.EDIT)); 
					}
					public void onSuccess(Recipe recipe) {
						eventBus.fireEvent(new SpinnerEvent(false));
						if (recipe != null) {
							cdata.editRecipe = recipe;
							
							eventBus.fireEvent(new RecipeLoadedEvent(recipe)); 
							
							if (recipe.getKitchenId() != null)
								changeKitchen(recipe.getKitchenId());
						}
					}
				});
			}
			catch (NumberFormatException nfe) {
				eventBus.fireEvent(new AlertEvent("Rezept mit ID " + idStr + " nicht gefunden. Neues Rezept erstellt.", AlertType.INFO, AlertEvent.Destination.EDIT, 6000));
			}
		}
	}
	
	public void addIngredientToMenu(FoodProductInfo product, final Quantity weight) {

		dataRpcService.getFoodProduct(product.getId(), new AsyncCallback<FoodProduct>() {
			public void onFailure(Throwable error) {
				eventBus.fireEvent(new AlertEvent("Zutat konnte nicht geladen werden.", AlertType.ERROR, AlertEvent.Destination.EDIT)); 
			}
			public void onSuccess(FoodProduct product) {
				Ingredient ingredient = new Ingredient(product);
				if (weight == null) {
					ingredient.setWeight(product.getStdWeight());
				}
				else{
					ingredient.setWeight(weight);
					//TODO implement right Distance fetch
					//ingSpec.setDistance(cdata.distances.getDistance(ingSpec.getExtraction().symbol, cdata.currentLocation));
					ingredient.setDistance(new Quantity(100.0, Unit.KILOMETER));
				}
				
				if (cdata.editRecipe != null) {
					cdata.editRecipe.addIngredient(ingredient);
					eventBus.fireEvent(new IngredientAddedEvent(ingredient));
				}
				else
					eventBus.fireEvent(new AlertEvent("Rezept noch nicht geladen werden. Bitte Seite erneut laden.", AlertType.ERROR, AlertEvent.Destination.EDIT, 10000));
			}
		});
	}
	
	// probably other place?
	public void changeMonth(int month) {
		cdata.currentMonth = month;
		eventBus.fireEvent(new MonthChangedEvent(month));
	}
	
	public void changeKitchen(Long kitchenId) {
		Kitchen kitchen = cdata.getKitchenByID(kitchenId);
		// maybee kitchen = null not necessary...
		if (kitchen == null) {
			cdata.currentKitchen = null;
			cdata.currentKitchenRecipes = null;
			cdata.userInfo.setCurrentKitchen(null);
			
			eventBus.fireEvent(new KitchenChangedEvent(-1L));
		}
		else {
			cdata.currentKitchen = kitchen;
			cdata.userInfo.setCurrentKitchen(kitchen.getId());
			for (Recipe recipe : cdata.kitchenRecipes) {
				if (recipe.getKitchenId() == kitchen.getId());
					cdata.currentKitchenRecipes.add(recipe);
			}
			eventBus.fireEvent(new KitchenChangedEvent(kitchen.getId()));
		}
		
		
		dataRpcService.setCurrentKitchen(kitchen.getId(), new AsyncCallback<Boolean>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}
			public void onSuccess(Boolean ignore) {}
		});
	
	}
	
	public void saveKitchen() {}
	
	public void deleteKitchen() {}

	
	// reload will do it...
	public void approveRecipe(final Recipe recipe, final Boolean approve) {
		dataRpcService.approveRecipe(recipe.getId(), approve,new AsyncCallback<Boolean>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}
			public void onSuccess(Boolean ignore) {
				eventBus.fireEvent(new AlertEvent("Rezept veröffentlicht. Bitte Seite erneut laden um das Rezept in den öffentlichen Rezepten zu sehen.", AlertType.INFO, AlertEvent.Destination.EDIT)); 
			}
		});
	}
	// Select the KitchenRecipes
	// Always call updateResults after for propper loading!
	public void changeKitchenRecipes(Long id) {
		cdata.currentKitchenRecipes.clear();
		for(Recipe recipe : cdata.kitchenRecipes){
			if(id.equals(recipe.getKitchenId()))
			{
				cdata.currentKitchenRecipes.add(recipe);
			}
		}
		cdata.currentKitchen = cdata.getKitchenByID(id);
	}
	
	/**
	 * make sure you fetched all distances before!! otherwise not all ingredients will be updated
	 * @param processedLocation
	 */

	public void changeCurrentLocation(String processedLocation) {
		cdata.currentLocation = processedLocation;
		
		// Iterate over all IngredientSpecificatino of Kitchen Recipes or editRecipe
		List<Ingredient> ingSpecs = new ArrayList<Ingredient>();
		if (cdata.currentKitchen != null ) {
			for (Recipe recipe : cdata.currentKitchenRecipes)
				ingSpecs.addAll(recipe.getIngredients());
		}
		else
			ingSpecs.addAll(cdata.editRecipe.getIngredients());
		
		for (Ingredient ingSpec : ingSpecs) {
				ingSpec.setDistance(cdata.distances.getDistance(processedLocation, ingSpec.getExtraction().symbol));
		}		
			
		
		eventBus.fireEvent(new LocationChangedEvent(cdata.currentLocation));
	}
	
	
	/**
	 * 
	 * @param products the result of the search, list of ingredients
	 * @param alternatives the alternatives of the ingredient if just one was found
	 */
	public void searchIngredients(String searchString, List<FoodProductInfo> products, List<FoodProductInfo> alternatives) {
		products.clear();
		alternatives.clear();
		
		if(searchString.trim().length() != 0){

 			String[] searches = searchString.split(" ");

			// consider strings with whitespaces, seek for each string individually
			for(String search : searches)
			{
				// TODO this search algorithm is extremely slow, make faster
				for(FoodProductInfo product : cdata.productInfos){
					if( search.trim().length() <= product.getName().length() &&  product.getName().substring(0, search.trim().length()).compareToIgnoreCase(search) == 0){
						if(!products.contains(product)){
							product.setNotASubstitute(true);
							products.add(product);
						}
					}
				}
				// only look for alternatives, if there is only 1 result
				if(products.size() == 1){
					FoodProductInfo product = products.get(0);
					if(product.getSubstituteIds() != null){
						for(Long alternativen_id : product.getSubstituteIds()){
							for(FoodProductInfo zutat2 : cdata.productInfos){
								if(zutat2.getId().equals(alternativen_id)){
									if(!alternatives.contains(zutat2)){
										zutat2.setNotASubstitute(false);
										alternatives.add(zutat2);
									}
								}
							}
						}
					}

				}
			}
		}
		else {
			// the search routine is also fast enough, the lag comes later
			for(FoodProductInfo product : cdata.productInfos){
				products.add(product);
				product.setNotASubstitute(true);
			}
		}
	}
	
	
	private List<Recipe> searchRecipe(String searchString, List<Recipe> recipes) {
		List<Recipe> result = new ArrayList<Recipe>();
		String[] searches = searchString.split(" ");
		
		if(searchString.trim().length() != 0){
			for(Recipe recipe : recipes){
				// search by Name
				if( getLevenshteinDistance(recipe.getTitle(),searchString) < 5){
					result.add(recipe);
				}
				
				// idea: it also splits up the recipes names by whitespaces
				for(String snippet : recipe.getTitle().split(" ")){
				if( getLevenshteinDistance(snippet,searchString) < 2){
					result.add(recipe);
					}
				}
	
				// search by matching Ingredient names included in the recipes
				List<Ingredient> recipeIngredients = recipe.getIngredients();
				if(recipeIngredients != null){
					int i = 0;
					for(Ingredient ZutatImRezept : recipeIngredients ){
						if(ZutatImRezept != null){
							for(String search2 : searches){
								if( search2.trim().length() <= ZutatImRezept.getFoodProduct().getName().length() &&  ZutatImRezept.getFoodProduct().getName().substring(0, search2.trim().length()).compareToIgnoreCase(search2) == 0){
									i++;
								}
							}
							if(i == searches.length)
								result.add(recipe);
						}
					}
				}
			}
		}
		else {
			return recipes;
		}
		return result;
	}
	
	public List<Recipe> searchUserRecipes(String searchString) {
		return searchRecipe(searchString, cdata.userRecipes);
	}
	
	public List<Recipe> searchKitchenRecipes(String searchString) {
		return searchRecipe(searchString, cdata.currentKitchenRecipes);
	}
	
	public List<Recipe> searchPublicRecipes(String searchString) {
		return searchRecipe(searchString, cdata.publicRecipes);
	}
	



	
	
	/**
	 * This function may proof to be useful for a more fuzzy matching!
	 * This is used for a matching in the recipes names
	 */
	private static int getLevenshteinDistance(String s, String t) {
		if (s == null || t == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}

		/*
       The difference between this impl. and the previous is that, rather 
       than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
       we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
       is the 'current working' distance array that maintains the newest distance cost
       counts as we iterate through the characters of String s.  Each time we increment
       the index of String t we are comparing, d is copied to p, the second int[].  Doing so
       allows us to retain the previous cost counts as required by the algorithm (taking 
       the minimum of the cost count to the left, up one, and diagonally up and to the left
       of the current cost count being calculated).  (Note that the arrays aren't really 
       copied anymore, just switched...this is clearly much better than cloning an array 
       or doing a System.arraycopy() each time  through the outer loop.)

       Effectively, the difference between the two implementations is this one does not 
       cause an out of memory condition when calculating the LD over two very large strings.
		 */

		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		if (n > m) {
			// swap the input strings to consume less memory
			String tmp = s;
			s = t;
			t = tmp;
			n = m;
			m = t.length();
		}

		int p[] = new int[n+1]; //'previous' cost array, horizontally
		int d[] = new int[n+1]; // cost array, horizontally
		int _d[]; //placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for (i = 0; i<=n; i++) {
			p[i] = i;
		}

		for (j = 1; j<=m; j++) {
			t_j = t.charAt(j-1);
			d[0] = j;

			for (i=1; i<=n; i++) {
				cost = s.charAt(i-1)==t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost
				d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}

		// our last action in the above loop was to switch d and p, so p now 
		// actually has the most recent cost counts
		return p[n];
	}
	
	public void changeRecipeScope(RecipeScope recipeScope) {
		
		if (cdata.recipeScope != recipeScope) {
			cdata.recipeScope = recipeScope;
			cdata.recipeSeachRepresentation.setScope(recipeScope);
			searchRecipes(cdata.recipeSeachRepresentation);
		}
	}
	
	
	
	public RecipeScope getRecipeScope() {
		if (cdata.recipeScope == null) 
			cdata.recipeScope = RecipeScope.PUBLIC;
		return cdata.recipeScope;
	}
	
	
	
	private void handleError(Throwable error) {
		if (error instanceof NotLoggedInException) {
			Window.Location.replace(cdata.userInfo.getLoginUrl());
		}
		Log.error(error.getCause() + " EXCEPTION TYPE: " + error.getClass().toString());
		eventBus.fireEvent(new AlertEvent("Schwerwiegender Fehler (siehe Log für Details). Bitte versuche es noch einmal.", AlertType.ERROR, AlertEvent.Destination.BOTH, 10000));
	}
	
	
	// ----------------- Getters -----------------------------
	public List<Recipe> getPublicRecipes() {
		return cdata.publicRecipes;
	}

	public List<Recipe> getUserRecipes() {
		return cdata.userRecipes;
	}

	public List<Recipe> getKitchenRecipes() {
		return cdata.kitchenRecipes;
	}

	public List<Recipe> getCurrentKitchenRecipes() {
		return cdata.currentKitchenRecipes;
	}
	
	public List<RecipeInfo> getRecipeInfos() {
		return cdata.recipeInfos;
	}

	public Recipe getEditRecipe() {
		return cdata.editRecipe;
	}

	public List<FoodProduct> getIngredients() {
		return cdata.ingredients;
	}
	
	public FoodProduct getIngredientByID(long id) {
		return cdata.getIngredientByID(id);
	}

	public CountryDistance getDist() {
		return cdata.distances;
	}

	public List<Kitchen> getKitchens() {
		return cdata.kitchens;
	}

	public UserInfo getLoginInfo() {
		return cdata.userInfo;
	}

	public Kitchen getCurrentKitchen() {
		return cdata.currentKitchen;
	}

	public int getCurrentMonth() {
		return cdata.currentMonth;
	}

	public String getCurrentLocation() {
		return cdata.currentLocation;
	}

	public void clearEditRecipe() {
		cdata.editRecipe = null;
	}

	


	public List<FoodProductInfo> getProductInfos() {
		return cdata.productInfos;
	}

	public ListDataProvider<RecipeInfo> getRecipeDataProvider() {
		return recipeDataProvider;
	}

	public void setRecipeDataProvider(ListDataProvider<RecipeInfo> recipeDataProvider) {
		this.recipeDataProvider = recipeDataProvider;
	}




}

