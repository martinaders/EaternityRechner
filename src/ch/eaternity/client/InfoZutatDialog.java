package ch.eaternity.client;

import java.util.Date;
import java.util.List;


import ch.eaternity.shared.IngredientCondition;
import ch.eaternity.shared.Extraction;
import ch.eaternity.shared.Ingredient;
import ch.eaternity.shared.MoTransportation;
import ch.eaternity.shared.Production;
import ch.eaternity.shared.Rezept;
import ch.eaternity.shared.SingleDistance;
import ch.eaternity.shared.ZutatSpecification;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class InfoZutatDialog extends Composite {
	interface Binder extends UiBinder<Widget, InfoZutatDialog> { }
	private static Binder uiBinder = GWT.create(Binder.class);
	@UiField HTML zutatName;
	@UiField PassedStyle passedStyle;
	@UiField Label hinweisPanel;
	
	static double distance = 0;

	@UiField SelectionStyle selectionStyle;
	ZutatSpecification zutatSpec;
	private int selectedRow;
	private FlexTable menuTable;
	@UiField
	FlexTable specificationTable;
	private Rezept rezept;
	private FlexTable suggestTable;
	
	private HTML kmText = new HTML();
	
	interface SelectionStyle extends CssResource {
		String selectedBlob();
	}
	
	interface PassedStyle extends CssResource {
		String hinweisPassed();
	}
	
	void styleHinweis( boolean selected) {
		
		String style = passedStyle.hinweisPassed();

		if (selected) {
			hinweisPanel.addStyleName(style);
		} else {
			hinweisPanel.removeStyleName(style);
		}
	
	}
	public void stylePanel(boolean onOff) {
		if (onOff) {
//			infoBox.setHeight("500px");
		} else {
			
		}
	
}

	public InfoZutatDialog(ZutatSpecification zutatSpec, Ingredient zutat, TextBox amount, FlexTable menuTable, int selectedRow, Rezept rezept, FlexTable suggestTable) {
		initWidget(uiBinder.createAndBindUi(this));
		zutatName.setHTML("<h1>"+ zutatSpec.getName() +"</h1>");
		// TODO Auto-generated constructor stub
		this.setZutatSpec(zutatSpec);
		this.setSelectedRow(selectedRow);
		this.setRezept(rezept);
		this.menuTable = menuTable;
		this.suggestTable = suggestTable;
		setValues( zutat);
	}

	
	
	public void setValues( final Ingredient zutat){
		
		if(zutat.hasSeason != null && zutat.hasSeason){
			specificationTable.setHTML(0, 0, "Saison");
			updateSaison(zutatSpec);
		}
		
		if(zutat.getExtractions() != null && zutat.getExtractions().size()>0){
			
			final ListBox herkuenfte = new ListBox();
			
			for(Extraction extraction : zutat.getExtractions()){
				herkuenfte.addItem(extraction.symbol);
			}
			herkuenfte.addChangeHandler(new ChangeHandler(){
				public void onChange(ChangeEvent event){
					zutatSpec.setHerkunft(zutat.getExtractions().get((herkuenfte.getSelectedIndex())) );
					for(SingleDistance singleDistance : Search.getClientData().getDistances()){
						if(singleDistance.getFrom().contentEquals(TopPanel.currentHerkunft) && 
								singleDistance.getTo().contentEquals(zutatSpec.getHerkunft().symbol)){
							
							zutatSpec.setDistance(singleDistance.getDistance());
						

					    	String formatted = NumberFormat.getFormat("##").format( zutatSpec.getDistance()/100000 );
					    	if(formatted.contentEquals("0")){
					    		kmText.setHTML("ca. " + formatted + "km");
					    	}else{
					    		kmText.setHTML("ca. " + formatted + "00km");
					    	}
						}

					}
			
					updateZutatCO2();
				}
			});
		    
			int row = specificationTable.getRowCount();
			specificationTable.setHTML(row, 0, "Herkunft");
			HorizontalPanel flow = new HorizontalPanel();
			specificationTable.setWidget(row,1,flow);
			flow.add(herkuenfte);
			
	    	String formatted = NumberFormat.getFormat("##").format( zutatSpec.getDistance()/100000 );
	    	if(formatted.contentEquals("0")){
	    		kmText.setHTML("ca. " + formatted + "km");
	    	}else{
	    		kmText.setHTML("ca. " + formatted + "00km");
	    	}
	    	flow.add(kmText);
			
			
			
			herkuenfte.setSelectedIndex(zutat.getExtractions().indexOf(zutatSpec.getHerkunft()));
			
		}
		


		if(zutat.moTransportations != null && zutat.moTransportations.size()>0){
			int row = specificationTable.getRowCount();
			specificationTable.setHTML(row, 0, "Transport");
			FlowPanel flow = new FlowPanel();
			specificationTable.setWidget(row,1,flow);
			
			for(final MoTransportation moTransportations : zutat.moTransportations){
				RadioButton transport = new RadioButton("Transportations",moTransportations.symbol);
				if(moTransportations.symbol.equalsIgnoreCase(zutatSpec.getTransportmittel().symbol)){
					transport.setValue(true);
				}
				transport.addClickHandler(new ClickHandler() {
				      public void onClick(ClickEvent event) {
				        boolean checked = ((RadioButton) event.getSource()).isChecked();
				        if(checked){
				        	zutatSpec.setTransportmittel(moTransportations);
				        	updateZutatCO2();
				        }
				      }
				    });
				
				flow.add(transport);
			}
			
		}
		
		if(zutat.productions != null && zutat.productions.size()>0){
			int row = specificationTable.getRowCount();
			specificationTable.setHTML(row, 0, "Herstellung");
			FlowPanel flow = new FlowPanel();
			specificationTable.setWidget(row,1,flow);
			
			for(final Production production : zutat.productions){
				RadioButton productionBox = new RadioButton("productions",production.symbol);
				if(production.symbol.equalsIgnoreCase(zutatSpec.getProduktion().symbol)){
					productionBox.setValue(true);
				}
				productionBox.addClickHandler(new ClickHandler() {
				      public void onClick(ClickEvent event) {
				        boolean checked = ((RadioButton) event.getSource()).isChecked();
				        if(checked){
				        	zutatSpec.setProduktion(production);
				        	updateZutatCO2();
				        }
				      }
				    });

				flow.add(productionBox);

			}
		}
		
		
		if(zutat.conditions != null && zutat.conditions.size()>0){
			int row = specificationTable.getRowCount();
			specificationTable.setHTML(row, 0, "Zustand");
			FlowPanel flow = new FlowPanel();
			specificationTable.setWidget(row,1,flow);
			
			for(final IngredientCondition condition : zutat.conditions){
				RadioButton conditionBox = new RadioButton("conditions",condition.symbol);
				if(condition.symbol.equalsIgnoreCase(zutatSpec.getZustand().symbol)){
					conditionBox.setValue(true);
				}
				conditionBox.addClickHandler(new ClickHandler() {
				      public void onClick(ClickEvent event) {
				        boolean checked = ((RadioButton) event.getSource()).isChecked();
				        if(checked){
				        	zutatSpec.setZustand(condition);
				        	updateZutatCO2();
				        }
				      }
				    });

				flow.add(conditionBox);
			}
		}
		

		

		
	}
	

	public void updateSaison(ZutatSpecification zutatSpec) {
		// if it is Greenhouse, or conserved then it should be koherent...
		
		Date date = DateTimeFormat.getFormat("MM").parse(Integer.toString(TopPanel.Monate.getSelectedIndex()+1));
		// In Tagen
//		String test = InfoZutat.zutat.getStartSeason();
		Date dateStart = DateTimeFormat.getFormat("dd.MM").parse( zutatSpec.getStartSeason() );		
		Date dateStop = DateTimeFormat.getFormat("dd.MM").parse( zutatSpec.getStopSeason() );
		
		if(		dateStart.before(dateStop)  && date.after(dateStart) && date.before(dateStop) ||
				dateStart.after(dateStop) && !( date.before(dateStart) && date.after(dateStop)  ) ){
			
			
			specificationTable.setHTML(0, 1, "Diese Zutat hat Saison");
			
			styleHinweis(false);
			hinweisPanel.setText("Angaben sind koherent.");
			
		} else {
			specificationTable.setHTML(0, 1, "Diese Zutat hat keine Saison");
			
			// unvollständig:
			
			styleHinweis(true);
			hinweisPanel.setText("Angaben sind unvollständig.");
		}
		
		
	}

	
	
	//TODO here comes all the CO2 Logic
	public void updateZutatCO2(){
		String formatted = NumberFormat.getFormat("##").format( zutatSpec.getCalculatedCO2Value() );
//		valueLabel.setText(formatted + "g CO2-Äquivalent");
		if(selectedRow != -1){
//			if(EaternityRechner.zutatImMenu.contains(zutat)){
//				EaternityRechner.zutatImMenu.set(EaternityRechner.zutatImMenu.indexOf(zutat), zutat);
//				
				menuTable.setHTML(selectedRow, 3, "ca "+formatted + "g *");
				rezept.Zutaten.set(selectedRow, zutatSpec);
				Double MenuLabelWert = getRezeptCO2(rezept.Zutaten);
				String formattedMenu = NumberFormat.getFormat("##").format(MenuLabelWert);
				suggestTable.setHTML(0,1,"ca <b>"+formattedMenu+"g</b> *");
//			}
			//TODO uncomment this:
			// EaternityRechner.MenuTable.setText(row, 4, ": ca. "+formatted + "g CO2-Äquivalent");
		}
		
		
	}
	
	private void styleLabel( HTMLPanel panel, boolean selected) {
		
		String style = selectionStyle.selectedBlob();

		if (selected) {
			panel.addStyleName(style);
		} else {
			panel.removeStyleName(style);
		}
	
}
	public void setZutatSpec(ZutatSpecification zutatSpec) {
		this.zutatSpec = zutatSpec;
	}
	public ZutatSpecification getZutatSpec() {
		return zutatSpec;
	}
	public void setSelectedRow(int selectedRow) {
		this.selectedRow = selectedRow;
	}
	public int getSelectedRow() {
		return selectedRow;
	}
	public void setRezept(Rezept rezept) {
		this.rezept = rezept;
	}
	public Rezept getRezept() {
		return rezept;
	}
	private Double getRezeptCO2(List<ZutatSpecification> Zutaten) {
		Double MenuLabelWert = 0.0;
		for (ZutatSpecification zutatSpec : Zutaten) { 
			MenuLabelWert +=zutatSpec.getCalculatedCO2Value();

		}
		return MenuLabelWert;
	}
}
