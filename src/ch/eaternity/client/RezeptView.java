package ch.eaternity.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


import ch.eaternity.shared.Ingredient;
import ch.eaternity.shared.Rezept;
import ch.eaternity.shared.ZutatSpecification;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.Cell;

public class RezeptView extends Composite {
	interface Binder extends UiBinder<Widget, RezeptView> { }
	private static Binder uiBinder = GWT.create(Binder.class);
	
	@UiField SelectionStyleRow selectionStyleRow;

	@UiField FlexTable MenuTable;
	@UiField HTMLPanel SaveRezeptPanel;
	@UiField Button RezeptButton;
	@UiField TextBox RezeptName;
	@UiField CheckBox makePublic;
	@UiField FlexTable SuggestTable;
	@UiField HorizontalPanel addInfoPanel;
	@UiField Button removeRezeptButton;
	@UiField HTMLPanel htmlRezept;
	@UiField Label rezeptNameTop;
	HandlerRegistration klicky;
	
	boolean saved;
	
	private Listener listener;
	int  selectedRow = 0;
	int  selectedRezept = -1;
	private Rezept rezept;

	
//	static ArrayList<ZutatSpecification> zutatImMenu = new ArrayList<ZutatSpecification>();
	
	
	public RezeptView(Rezept rezept) {
	    // does this need to be here?
	    initWidget(uiBinder.createAndBindUi(this));
	    setRezept(rezept);
	    saved = true;
	    initTable();
	    
		if(EaternityRechner.loginInfo.isLoggedIn()) {
			SaveRezeptPanel.setVisible(true);
		} else   {
			SaveRezeptPanel.setVisible(false);
		}
	  }
	
	
	public interface Listener {
		void onItemSelected(ZutatSpecification item);
	}


	
	interface SelectionStyleRow extends CssResource {
		String selectedRow();
	}

	
	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	
	@UiHandler("MenuTable")
	void onTableClicked(ClickEvent event) {
		// Select the row that was clicked (-1 to account for header row).
		Cell cell = MenuTable.getCellForEvent(event);
		if (cell != null) {
			int row = cell.getRowIndex();
			selectRow(row);
		}
	}
	
	@UiHandler("removeRezeptButton")
	void onRemoveClicked(ClickEvent event) {
		final RezeptView test = this;
		if(saved){
			int row = getWidgetRow(test , EaternityRechner.rezeptList);
			EaternityRechner.rezeptList.remove(test);
			EaternityRechner.rezeptList.removeRow(row);
			EaternityRechner.selectedRezept = -1;
		} else {
		final ConfirmDialog dlg = new ConfirmDialog("Diese Zusammenstellungen wurde noch nicht gespeichert!");
		dlg.statusLabel.setText("Zusammenstellung trotzdem ausblenden?");
		// TODO recheck user if he really want to do this...
		
		dlg.executeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int row = getWidgetRow(test , EaternityRechner.rezeptList);
				EaternityRechner.rezeptList.remove(test);
				EaternityRechner.rezeptList.removeRow(row);
				EaternityRechner.selectedRezept = -1;
				dlg.hide();
			}
		});
		dlg.show();
		dlg.center();
		}
		

		

	}
	


	
	private void initTable() {
		MenuTable.getColumnFormatter().setWidth(0, "40px");
		MenuTable.getColumnFormatter().setWidth(1, "76px");
		
	}
	
	public void setRezept(Rezept rezept){
		this.rezept = rezept;
		showRezept(rezept);
	}

	public Rezept getRezept(){
		return this.rezept;
	}	
	
	public void showRezept(final Rezept rezept) {
			final RezeptView rezeptView = this;
			displayZutatImMenu(rezept.Zutaten);
			updateSuggestion();
//			zutatImMenu.clear();
			
//			int row = AddZutatZumMenu(rezept.getZutaten());
			// add Speicher Rezept Button
			if(klicky != null){
				klicky.removeHandler();
			}
			
			klicky = RezeptButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					if(RezeptName.getText() != ""){
//						Speichere Rezept ab. 
//						Rezept rezeptSave = new Rezept(RezeptName.getText());
//						rezeptSave.setOpen(makePublic.getValue());
//						rezeptSave.addZutaten(rezept.getZutaten());
//						EaternityRechner.addRezept(rezeptSave);
						rezept.setSymbol(RezeptName.getText());
						rezept.setOpen(makePublic.getValue());
						
						EaternityRechner.addRezept(rezept,rezeptView);
					}
				}
			});
		
	}

	
	

	 void selectRow(int row) {
		
		//TODO uncomment this:
		//Search.leftSplitPanel.setWidgetMinSize(Search.infoZutat, 448);
//		Window.alert(Integer.toString(row));
		
		 saved = false;
		 
		 if(selectedRow != -1 && addInfoPanel.getWidgetCount() ==2){
			 InfoZutatDialog infoDialog = (InfoZutatDialog)(addInfoPanel.getWidget(1));
			 ZutatSpecification zutatSpec2 = infoDialog.getZutatSpec();
//			 int index = zutatImMenu.indexOf(zutatSpec);
//			 zutatImMenu = (ArrayList<ZutatSpecification>) rezept.getZutaten();
			 rezept.Zutaten.set(selectedRow , zutatSpec2);

		 }
		 
		ZutatSpecification zutatSpec = rezept.Zutaten.get(row);

		if (zutatSpec == null) {
			return;
		}
		
		Long ParentZutatId = zutatSpec.getZutat_id();
		Ingredient zutat = Search.getClientData().getIngredientByID(ParentZutatId);
		
		openSpecificationDialog(zutatSpec,zutat, (TextBox) MenuTable.getWidget(row, 0), MenuTable,row);
		//InfoZutat.setZutat(item, clientDataHere.getZutatByID(ParentZutatId),row);
//
//		infoZutat.stylePanel(true);

		styleRow(selectedRow, false);
		
//		Search.styleRow(Search.selectedRow,false);
		Search.selectedRow = -1;
		
		styleRow(row, true);

		selectedRow = row;

		if (listener != null) {
			listener.onItemSelected(zutatSpec);
		}
		
		updateSuggestion();
	}

	private void openSpecificationDialog(ZutatSpecification zutatSpec, Ingredient zutat,  TextBox amount,FlexTable MenuTable,int selectedRow) {
		// TODO Auto-generated method stub
		
		if(addInfoPanel.getWidgetCount() ==2){
			addInfoPanel.remove(1);
		}
		InfoZutatDialog infoZutat = new InfoZutatDialog(zutatSpec,zutat,amount,MenuTable,selectedRow,rezept,SuggestTable);
		addInfoPanel.add(infoZutat);
		
//		addInfoPanel.add(new HTML("test"));
		
	}
	
	

	//TODO do the same for Search BUtton Press
	void styleRow(int row, boolean selected) {
		if (row != -1) {
			String style = selectionStyleRow.selectedRow();

			if (selected) {
				MenuTable.getRowFormatter().addStyleName(row, style);
			} else {
				MenuTable.getRowFormatter().removeStyleName(row, style);
			}
		}
	}
	


	
	private void displayZutatImMenu( List<ZutatSpecification> zutaten) {
	
	MenuTable.removeAllRows();;
	int row = MenuTable.getRowCount();
	
	for(final ZutatSpecification zutat : zutaten){

	Button removeZutat = new Button("x");
//	removeZutat.addStyleName("style.gwt-Button");
//	removeZutat.addStyleDependentName("gwt-Button");
	removeZutat.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {
			int removedIndex = rezept.Zutaten.indexOf(zutat);
			rezept.Zutaten.remove(removedIndex);
			MenuTable.removeRow(removedIndex);
//			rezept.removeZutat(removedIndex);
			updateSuggestion();
		}
	});
	
	final TextBox MengeZutat = new TextBox();
	MengeZutat.setText(Integer.toString(zutat.getMengeGramm()));
	MengeZutat.setWidth("35px");
	
	MengeZutat.addKeyUpHandler( new KeyUpHandler() {
		public void onKeyUp(KeyUpEvent event) {
			int keyCode = event.getNativeKeyCode();
			if ((!Character.isDigit((char) keyCode)) && (keyCode != KeyCodes.KEY_TAB)
					&& (keyCode != KeyCodes.KEY_BACKSPACE)
					&& (keyCode != KeyCodes.KEY_DELETE) && (keyCode != KeyCodes.KEY_ENTER) 
					&& (keyCode != KeyCodes.KEY_HOME) && (keyCode != KeyCodes.KEY_END)
					&& (keyCode != KeyCodes.KEY_LEFT) && (keyCode != KeyCodes.KEY_UP)
					&& (keyCode != KeyCodes.KEY_RIGHT) && (keyCode != KeyCodes.KEY_DOWN)) {
				// TextBox.cancelKey() suppresses the current keyboard event.
				MengeZutat.cancelKey();
			} else {
				String MengeZutatWert;
				int rowhere = getWidgetRow(MengeZutat,MenuTable);
				if(MengeZutat.getText() != ""){
					MengeZutatWert = MengeZutat.getText();
					zutat.setMengeGramm(Integer.valueOf(MengeZutatWert));
				} else {
					MengeZutatWert = "0";
				}
				
				updateTable(rowhere,zutat);
//				int length = (int)  Math.round(Double.valueOf(MengeZutatWert).doubleValue() *0.001);
//				MenuTable.setText(rowhere,3,"ca. "+ Double.toString(zutatSpec.getCalculatedCO2Value()).concat("g CO₂-Äquivalent"));
//				MenuTable.setHTML(rowhere, 4, "<div style='background:#ff0;width:".concat(Double.toString(zutatSpec.getCalculatedCO2Value()/1000).concat("px'>.</div>")));
//				updateSuggestion();
			}


		}


	});

	//Name
	MenuTable.setWidget(row, 0, MengeZutat);
	MenuTable.setText(row, 1, "g " + zutat.getName());
	MenuTable.setWidget(row, 6, removeZutat);
	// Remove Button

	


	
	
//	int length = (int) Math.round(zutatSpec.getCalculatedCO2Value());
//	//	Menge CO2 Äquivalent
//	MenuTable.setText(row,3,Integer.toString(length).concat("g CO₂-Äquivalent"));
//
//	MenuTable.setHTML(row, 4, "<div style='background:#ff0;width:".concat(Integer.toString(length/1000)).concat("px'>.</div>"));
	
	updateTable(row,zutat);
//	MenuTable.setText(row,3,"ca. "+ Double.toString(zutatSpec.getCalculatedCO2Value()).concat("g CO₂-Äquivalent"));
//	MenuTable.setHTML(row, 4, "<div style='background:#ff0;width:".concat(Double.toString(zutatSpec.getCalculatedCO2Value()/1000).concat("px'>.</div>")));
//	updateSuggestion();
	row = row+1;
	}
}
	void updateSuggestion() {

		Double MenuLabelWert = 0.0;
		Double MaxMenuWert = 0.0;

		if(rezept.Zutaten.isEmpty()){
			if(addInfoPanel.getWidgetCount() ==2){
				addInfoPanel.remove(1);
			}
		}
		
		for (ZutatSpecification zutatSpec : rezept.Zutaten) { 
			MenuLabelWert +=zutatSpec.getCalculatedCO2Value();
			if(zutatSpec.getCalculatedCO2Value()>MaxMenuWert){
				MaxMenuWert = zutatSpec.getCalculatedCO2Value();
			}
			
		}
		for (ZutatSpecification zutatSpec : rezept.Zutaten) { 
			String formatted = NumberFormat.getFormat("##").format( zutatSpec.getCalculatedCO2Value() );
			MenuTable.setText(rezept.Zutaten.indexOf(zutatSpec),4,": ca. "+formatted+"g CO₂-Äquivalent ");
			MenuTable.setHTML(rezept.Zutaten.indexOf(zutatSpec), 8, " <div style='background:#ff0;width:".concat(Double.toString(zutatSpec.getCalculatedCO2Value()/MaxMenuWert*100).concat("px'>.</div>")));
		}
		
		String formatted = NumberFormat.getFormat("##").format(MenuLabelWert);
		
		SuggestTable.setWidth("300px");
		SuggestTable.setText(0,0," alles zusammen: ca "+formatted+"g CO₂-Äquivalent");
		
		// TODO Algorithm for the Top Suggestions
		// Von jedem Gericht gibt es einen CO2 Wert für 4Personen (mit oder ohne Herkunft? oder aus der nächsten Distanz?), 
		// so wie sie gepeichert wurde.
		// Es werden bei der Anzeige Rezepte berücksichtigt, die: 
		// min 20% identische Zutaten ( Zutat*(Menge im Rezept)/StdMenge ) und das pro Zutat, und davon min 20%identisch
		// min +50% Zutaten die in den alternativen Vorkommen
		// hierbei wird die 2 passendsten Rezepte jeweils aus den nicht durch das markierte Rezept belegten Bereich angezeigt
		// Bereich sind 0-20%	20%-50%		50%-100%
		
		// Rezepte sollten sich bewerten lassen, und deren Popularität gemessen werden. ( Über die Zeit?)
		// 

		// diese Filter sollten in der Reihenfolge ausgeführt werden, in der sie am wenigsten Berechnungen benötigen:
		
		// TODO alle Rezepte für 4 Personen, sonst macht der Vergleich keinen Sinn
		
		// get Comparator
		ArrayList<ComparatorObject> comparator = comparator(rezept);
		Double maxScore = 0.0;
		for(ComparatorObject comparatorObject : comparator){
			maxScore = maxScore+comparatorObject.value;
		}
		
		// all Recipes
		List<Rezept> allRecipes = Search.getClientData().getPublicRezepte();
		allRecipes.addAll(Search.getClientData().getYourRezepte());
		
		// zuerst der Filter über die tatsächlichen Zutaten
		ArrayList<ComparatorRecipe> scoreMap = new ArrayList<ComparatorRecipe>();
		scoreMap.clear();
		for( Rezept compareRecipe : allRecipes){
			ComparatorRecipe comparatorRecipe = new ComparatorRecipe();
			comparatorRecipe.key = compareRecipe.getId();
			comparatorRecipe.recipe = compareRecipe;
			comparatorRecipe.comparator = getExactScore(comparator,comparator(compareRecipe));
			Double error = 0.0;
			for(ComparatorObject comparatorObject : comparatorRecipe.comparator){
				error = error+Math.abs(comparatorObject.value);
			}
			comparatorRecipe.value = error;
			scoreMap.add(comparatorRecipe);
		}
		
		// dann der gröbere über die definierten Alternativen der Zutaten
		ArrayList<ComparatorRecipe> scoreMap2 = new ArrayList<ComparatorRecipe>();
		for(ComparatorRecipe compObj: scoreMap){
			if((compObj.value/maxScore)<0.2){ // this is min. 20% identical
				Rezept compareRecipe = compObj.recipe;
				ComparatorRecipe comparatorObject = new ComparatorRecipe();
				comparatorObject.recipe = compareRecipe;
				// TODO write this method
//				comparatorObject.comparator = getAltScore(comparator,comparator(compareRecipe));
				scoreMap2.add(comparatorObject);
			}
		}
		
		// alles was jetzt noch da ist, wird verglichen, das heisst die Statistik ausgerechnet
		
		
		// und die 2 Rezepte mit den höchsten Scores aus den entspr. Bereichen selektiert und angezeigt.
		
		
		
	}
	
	
	
	private ArrayList<ComparatorObject> getExactScore(ArrayList<ComparatorObject> recipeOrigin, ArrayList<ComparatorObject> recipeComparator) {
		
		ArrayList<ComparatorObject> resultComparator = new ArrayList<ComparatorObject>();
		
		// takes this Recipe from this RezeptView
		for(ComparatorObject comparatorObjectOrigin : recipeOrigin){
			// and compares every ingredient

			
			ComparatorObject comparatorResultObject = new ComparatorObject();
			double newValue = comparatorObjectOrigin.value;
			comparatorResultObject.key = comparatorObjectOrigin.key;
			
			// with the one from the database
			for(ComparatorObject comparatorObject :recipeComparator){
				
				// on match
				if(comparatorObject.key.equals(comparatorObjectOrigin.key)){	
					// calculate the error value
					newValue = comparatorObject.value-comparatorObjectOrigin.value;
					break;
				}
			}
			
			// store the error value
			comparatorResultObject.value = newValue;
			resultComparator.add(comparatorResultObject);
			

		}
		
		return resultComparator;
	}


	private ArrayList<ComparatorObject> comparator(Rezept rezept){
		// wtf is up with the Map() ???
//		Map<Long,Double> recipeComparator = Collections.emptyMap();
		// everything would have been so easy!!
		
		 ArrayList<ComparatorObject> recipeComparator = new  ArrayList<ComparatorObject>();
	
		
		for(ZutatSpecification zutatSpec : rezept.Zutaten){
			Ingredient zutat = Search.getClientData().getIngredientByID(zutatSpec.getZutat_id());
			Double amount = 1.0*zutatSpec.getMengeGramm()/zutat.stdAmountGramm;
			Double alreadyAmount = 0.0;
			int index = -1;
			for(ComparatorObject comparatorObject :recipeComparator){
				if(comparatorObject.key.equals(zutat.getId())){
					alreadyAmount =  comparatorObject.value;
					index = recipeComparator.indexOf(comparatorObject);
					break;
				}
			}
			
			ComparatorObject comparatorObject = new ComparatorObject();
			comparatorObject.ingredient = zutat;
			comparatorObject.key = zutat.getId();
			comparatorObject.value = amount+alreadyAmount;
			
			if(index!=-1){
				recipeComparator.set(index, comparatorObject);
			} else {
				recipeComparator.add(comparatorObject);
			}

		}

		return recipeComparator;
	}
	
	private Double getIdenticalScore(Rezept compareRecipe,ArrayList<ComparatorObject> recipeComparator) {
		// der Score ist:  ( Zutat*(Menge im Rezept)/StdMenge )
		Double score = 0.0;
		Double negativeScore = 0.0;
		for(ZutatSpecification zutatSpec : compareRecipe.Zutaten){
			
			int index = -1;
			double newValue = 0.0;
			for(ComparatorObject comparatorObject :recipeComparator){
				if(comparatorObject.key.equals(zutatSpec.getZutat_id())){
					Double amount = 1.0*zutatSpec.getMengeGramm()/Search.getClientData().getIngredientByID(zutatSpec.getZutat_id()).stdAmountGramm;
					if(amount>comparatorObject.value){
						score = score+comparatorObject.value;
//						comparator.recipeComparatorValues.set(comparator.recipeComparatorIndex.indexOf(zutatSpec.getZutat_id()), 0.0);
//						comparator.put(zutatSpec.getZutat_id(),0.0);
						negativeScore = negativeScore - (amount - comparatorObject.value);
					} else {
						score = score+amount;
						newValue = comparatorObject.value-amount ;
					}
					index = recipeComparator.indexOf(comparatorObject);
					break;
				}
			}
			
			ComparatorObject comparatorObject = new ComparatorObject();
			comparatorObject.key = zutatSpec.getZutat_id();
			comparatorObject.value = newValue;
			if(index!=-1){
				recipeComparator.set(index, comparatorObject);
			} 
//			
//			if(comparator.recipeComparatorIndex.contains(zutatSpec.getZutat_id())){
//				Double amount = 1.0*zutatSpec.getMengeGramm()/Search.getClientData().getIngredientByID(zutatSpec.getZutat_id()).stdAmountGramm;
//				Double compareAmount = comparator.recipeComparatorValues.get(comparator.recipeComparatorIndex.indexOf(zutatSpec.getZutat_id()));
//				if(amount>compareAmount){
//					score = score+compareAmount;
//					comparator.recipeComparatorValues.set(comparator.recipeComparatorIndex.indexOf(zutatSpec.getZutat_id()), 0.0);
////					comparator.put(zutatSpec.getZutat_id(),0.0);
//					negativeScore = negativeScore - (amount - compareAmount);
//				} else {
//					score = score+amount;
//					comparator.recipeComparatorValues.set(comparator.recipeComparatorIndex.indexOf(zutatSpec.getZutat_id()),compareAmount-amount );
//				}
//			}
//			
		}
		
		return score+negativeScore;
	}


	private void updateTable(int row,ZutatSpecification zutatSpec){
		saved = false;
		String formatted = NumberFormat.getFormat("##").format( zutatSpec.getCalculatedCO2Value() );
		
		MenuTable.getColumnFormatter().setWidth(4, "180px");
		MenuTable.setText(row,4,": ca. "+formatted+"g CO₂-Äquivalent ");
		
//		MenuTable.setHTML(row, 8, " <div style='background:#ff0;width:".concat(Double.toString(zutatSpec.getCalculatedCO2Value()/100).concat("px'>.</div>")));
		updateSuggestion();
	}
	
	private static int getWidgetRow(Widget widget, FlexTable table) {
		for (int row = 0; row < table.getRowCount(); row++) {
			for (int col = 0; col < table.getCellCount(row); col++) {
				Widget w = table.getWidget(row, col);
				if (w == widget) {
					return row;
				}
			}
		}
		throw new RuntimeException("Unable to determine widget row");
	}


	public void updateSaison() {
		if(addInfoPanel.getWidgetCount() ==2){
			InfoZutatDialog infoZutat = (InfoZutatDialog) addInfoPanel.getWidget(1);
			 infoZutat.updateSaison(infoZutat.zutatSpec);
		 }
	}
}


class ComparatorObject{
	public Long key;
	public Double value;
	public Ingredient ingredient;
	public ComparatorObject(){
		
	}
}

class ComparatorRecipe{
	public Long key;
	public Double value;
	public Rezept recipe;
	public ArrayList<ComparatorObject> comparator;
	public ComparatorRecipe(){
		
	}
}