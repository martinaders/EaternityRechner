package ch.eaternity.client.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eaticious.common.Quantity;
import org.eaticious.common.QuantityImpl;
import org.eaticious.common.Unit;

import ch.eaternity.client.DataController;
import ch.eaternity.client.activity.RechnerActivity;
import ch.eaternity.client.events.MonthChangedEvent;
import ch.eaternity.client.events.MonthChangedEventHandler;
import ch.eaternity.client.ui.RecipeEdit;
import ch.eaternity.shared.Condition;
import ch.eaternity.shared.Extraction;
import ch.eaternity.shared.FoodProduct;
import ch.eaternity.shared.HomeDistances;
import ch.eaternity.shared.Ingredient;
import ch.eaternity.shared.NotLoggedInException;
import ch.eaternity.shared.Production;
import ch.eaternity.shared.Route;
import ch.eaternity.shared.SeasonDate;
import ch.eaternity.shared.Transportation;
import ch.eaternity.shared.HomeDistances.RequestCallback;


import com.github.gwtbootstrap.client.ui.Close;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class IngredientSpecificationWidget extends Composite {
	interface Binder extends UiBinder<Widget, IngredientSpecificationWidget> { }
	private static Binder uiBinder = GWT.create(Binder.class);
	
	@UiField Label NameLabel;
	@UiField Close closeButton;
	@UiField Label SeasonLabel;
	@UiField HTML SeasonHTML;
	
	@UiField Label ExtractionLabel;
	@UiField ListBox ExtractionList;
	@UiField TextBox UnknownExtractionTextBox;
	
	@UiField Label TransportationLabel;
	@UiField ListBox TransportationList;
	@UiField HTML kmHTML;
	@UiField Label ProductionLabel;
	@UiField ListBox ProductionList;
	@UiField Label ConditionLabel;
	@UiField ListBox ConditionList;
	
	@UiField Label CostLabel;
	@UiField TextBox CostTextBox;
	@UiField Label CurrencyLabel;
	@UiField Label CostErrorLabel;
	
	@UiField HTML CoherenceHTML;
	@UiField static IngSpecWidgetStyles ingSpecWidgetStyles;
	
	
	private Ingredient ingredient;
	private FoodProduct product;
	private RecipeEdit recipeEdit;
	private DataController dco;
	private HomeDistances homeDistances;
	private static Geocoder geocoder;
	private String verifiedRecipeLocation = "";
	
	private static final String calulationAnchor = "<a style='margin-left:3px;cursor:pointer;cursor:hand;'>berechnen</a>";
	
	private boolean presenterSetted = false;
	
	interface IngSpecWidgetStyles extends CssResource {
		String redTextError();
	}
	
	// ---------------------- public Methods -----------------------
	
	public IngredientSpecificationWidget() {
		 initWidget(uiBinder.createAndBindUi(this));
		 try {
			 // this is data/rpc construction code and shouldn't be in the ui widget 
			 // (fallbacks need to be gracefully handled)
			 geocoder = new Geocoder();
			 geocoder.setBaseCountryCode("ch");
		 } catch (Exception e) {}
		 
		 this.setVisible(false);
	}
	
	public void setPresenter(RechnerActivity presenter, Ingredient ingredient, String verifiedRecipeLocation) {
		this.ingredient = ingredient;
		this.product = ingredient.getFoodProduct();
		this.recipeEdit = presenter.getRecipeEdit();
		this.dco = presenter.getDCO();
		this.homeDistances = dco.getHomeDistances(verifiedRecipeLocation);
		this.verifiedRecipeLocation = verifiedRecipeLocation;
		setFields();
		updateSeasonCoherency();
		
		presenterSetted = true;
	}
	
	public void setIngredient(Ingredient ingredient, String verifiedRecipeLocation) {
		this.ingredient = ingredient;
		this.product = ingredient.getFoodProduct();
		this.verifiedRecipeLocation =  verifiedRecipeLocation;
		setFields();
		updateSeasonCoherency();
	}
	
	public boolean isPresenterSetted(){
		return presenterSetted;
	}
	
	// ---------------------- private Methods -----------------------

	private void setFields() {
		UnknownExtractionTextBox.setVisible(false);
		
		NameLabel.setText(product.getName());
		
		if (product.getExtractions().size() > 0)
			processExtraction(product.getExtractions().get(0).symbol);
		
		TransportationList.clear();
		for (Transportation transport : product.getTransportations()) {
			TransportationList.addItem(transport.getSymbol());
		}
		ProductionList.clear();
		for (Production production : product.getProductions()) {
			ProductionList.addItem(production.getSymbol());
		}
		ConditionList.clear();
		for (Condition condition : product.getConditions()) {
			ConditionList.addItem(condition.getSymbol());
		}
	}

	private void switchToUnknownExtraction(){
		int width = ExtractionList.getOffsetWidth();
		ExtractionList.setVisible(false);
		
		UnknownExtractionTextBox.setWidth("200px");//Integer.toString(width)+"px");
		UnknownExtractionTextBox.setVisible(true);
		UnknownExtractionTextBox.setText("");
		
		kmHTML.setHTML(calulationAnchor);
		
		UnknownExtractionTextBox.setFocus(true);
	}
	
	/**
	 * processExtraction must be called before so that distance is setted in ingredient
	 */
	private void switchToKnownExtractions() {
		kmHTML.setHTML("ca. " + ingredient.getKmDistanceRounded() + "km");
		UnknownExtractionTextBox.setVisible(false);
		ExtractionList.setVisible(true);
		ExtractionList.setFocus(true);
	}
	
	private void updateExtractionList(Extraction selectedExtraction) {
		int i = 0;
		ExtractionList.clear();
		// find the corresponding item in the ListBox
		for (Extraction extraction : product.getExtractions()) {
			ExtractionList.addItem(extraction.getName());
			
	  		if (extraction.getName().equals(selectedExtraction.getName())) 
	  			ExtractionList.setSelectedIndex(i);
	  		i++;
		}
		ExtractionList.addItem("andere ... ");
	}
	
	/**
	 * switches to known extraction automatically if successfull processed
	 * @param extractionString
	 */
	private void processExtraction(String extractionString) {
		
		homeDistances.getRoute(extractionString, verifiedRecipeLocation, new RequestCallback() {
			public void onFailure() {
				adressNotFound();
			}
			public void onCallback(Route route) {
				ingredient.setRoute(route);
	  			recipeEdit.updateIngredientValue(ingredient);
	  			
	  			Extraction extraction = product.addExtraction(route.getFrom());				  					  			
		    	updateExtractionList(extraction);					    
		    	ingredient.setExtraction(extraction);
		    	
				switchToKnownExtractions();
			}
		});
	}
		
	private void adressNotFound() {
		kmHTML.setHTML("Adresse nicht gefunden!");
    	  Timer t = new Timer() {
    		  public void run() {
    			  kmHTML.setHTML(calulationAnchor);
    		  }
    	  };
    	  t.schedule(2000);
	}
	
	
	private void updateSeasonCoherency() {
		if (product.isSeasonDependant()) {
			String seasonText = "von <strong>" + product.getSeason().getBeginning().toMonthString() + "</strong> bis <strong>" + product.getSeason().getEnd().toMonthString() + "</strong>";
			
			SeasonDate begin = product.getSeason().getBeginning();
			SeasonDate end = product.getSeason().getEnd();
			SeasonDate date = new SeasonDate(recipeEdit.getRecipe().getCookingDate());
			
			if( date.after(begin) && date.before(end)) {
				seasonText.concat("<br />Diese Zutat ist saisonal");
				CoherenceHTML.setHTML("Angaben sind kohärent. <br /> Es ist möglich die Zutat frisch und lokal zu beziehen.");
			}
			else {
				seasonText.concat("<br />Diese Zutat ist nicht saisonal");
				if (ingredient.getRoute().getDistanceKM().getAmount() < 100 
						&& !ingredient.getProduction().getSymbol().equalsIgnoreCase("GH")
						&& ingredient.getCondition().getSymbol().equalsIgnoreCase("frisch")) {
					CoherenceHTML.setHTML("Angaben sind unvollständig. <br >" +
							"Bitte geben Sie an ob die Zutat importiert, konserviert oder im Gewächaus produziert wurde.");
				}
			}
			
			SeasonHTML.setHTML(seasonText);
			
			SeasonLabel.setVisible(true);
			SeasonHTML.setVisible(true);
			CoherenceHTML.setVisible(true);
		}
		else {
			SeasonLabel.setVisible(false);
			SeasonHTML.setVisible(false);
			CoherenceHTML.setVisible(false);
		}		
	}
	
	// ---------------------- UI Handlers ----------------------
	
	@UiHandler("closeButton") 
	public void onCloseClick(ClickEvent event) {
		this.setVisible(false);
	}
	
	
	@UiHandler("ExtractionList")
	public void onExtractionChange(ChangeEvent event) {
		int count = ExtractionList.getItemCount();
		int selected = ExtractionList.getSelectedIndex();
		if(count-1 == selected){
			// we have selected the "andere" item
			switchToUnknownExtraction();
		} 
		else {
			processExtraction(ExtractionList.getItemText(selected));
		}
	}
	
	@UiHandler("TransportationList")
	public void onTransportationChange(ChangeEvent event) {
		ingredient.setTransportation(product.getTransportation(TransportationList.getItemText(TransportationList.getSelectedIndex())));
		recipeEdit.updateIngredientValue(ingredient);
	}
	
	@UiHandler("ProductionList")
	public void onProductionChange(ChangeEvent event) {
		ingredient.setProduction(product.getProduction(ProductionList.getItemText(ProductionList.getSelectedIndex())));
		recipeEdit.updateIngredientValue(ingredient);
		updateSeasonCoherency();
	}
	
	@UiHandler("ConditionList")
	public void onConditionChange(ChangeEvent event) {
		ingredient.setCondition(product.getCondition(ConditionList.getItemText(ConditionList.getSelectedIndex())));
		recipeEdit.updateIngredientValue(ingredient);
		updateSeasonCoherency();
	}

	
	@UiHandler("CostTextBox")
	public void onKeyUp(BlurEvent event) {
		String errorStyle = ingSpecWidgetStyles.redTextError();
		String text = CostTextBox.getText();
		Double cost = 0.0;
		boolean success = false;
		
		try { 
			if ("".equals(text))
				CostTextBox.removeStyleName(errorStyle);
			else {
				cost = Double.parseDouble(text.trim());
				if (cost > 0.0) {
					success = true;
					CostTextBox.removeStyleName(errorStyle);
				}
			}
		}
		catch (IllegalArgumentException IAE) {}
		
		if (success) {
			ingredient.setCost(cost);
			recipeEdit.updateIngredientValue(ingredient);
		}
		else {
			CostTextBox.addStyleName(errorStyle);
		}
	}
	
	@UiHandler("UnknownExtractionTextBox")
	public void onKeyDown(KeyDownEvent event) {
		if(KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
			processExtraction(UnknownExtractionTextBox.getText());
		}
	}
	
	@UiHandler("kmHTML")
	public void onClick(ClickEvent event) {
		processExtraction(UnknownExtractionTextBox.getText());
	}
	
	
	
	
}
