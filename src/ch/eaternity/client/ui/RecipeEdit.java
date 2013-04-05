
package ch.eaternity.client.ui;

import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.PreloadedImage;
import gwtupload.client.PreloadedImage.OnLoadPreloadedImageHandler;

import java.util.Date;
import java.util.Iterator;

import ch.eaternity.client.DataController;
import ch.eaternity.client.activity.RechnerActivity;
import ch.eaternity.client.events.AlertEvent;
import ch.eaternity.client.events.AlertEventHandler;
import ch.eaternity.client.events.IngredientAddedEvent;
import ch.eaternity.client.events.IngredientAddedEventHandler;
import ch.eaternity.client.events.LoadedDataEvent;
import ch.eaternity.client.events.LoadedDataEventHandler;
import ch.eaternity.client.events.LoginChangedEvent;
import ch.eaternity.client.events.LoginChangedEventHandler;
import ch.eaternity.client.events.MonthChangedEvent;
import ch.eaternity.client.events.MonthChangedEventHandler;
import ch.eaternity.client.events.RecipeLoadedEvent;
import ch.eaternity.client.events.RecipeLoadedEventHandler;
import ch.eaternity.client.place.RechnerRecipeEditPlace;
import ch.eaternity.client.place.RechnerRecipeViewPlace;
import ch.eaternity.client.ui.widgets.ConfirmDialog;
import ch.eaternity.client.ui.widgets.FlexTableRowDragController;
import ch.eaternity.client.ui.widgets.FlexTableRowDropController;
import ch.eaternity.client.ui.widgets.IngredientWidget;
import ch.eaternity.client.ui.widgets.UploadPhoto;
import ch.eaternity.shared.FoodProduct;
import ch.eaternity.shared.Ingredient;
import ch.eaternity.shared.Recipe;
import ch.eaternity.shared.SavingPotential;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Close;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.Alert;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author aurelianjaggi
 *
 */
public class RecipeEdit extends Composite {
	interface Binder extends UiBinder<Widget, RecipeEdit> { }
	private static Binder uiBinder = GWT.create(Binder.class);
	
	// ---------------------- User Interface Elements --------------
	@UiField AbsolutePanel dragArea;
	
	@UiField Close closeRecipe;
	@UiField VerticalPanel alertPanel;
	@UiField TextBox RezeptName;
	@UiField TextBox rezeptDetails;
	@UiField Label co2valueLabel;
	@UiField Image co2Image;
	
	@UiField HTML codeImage;
	@UiField HorizontalPanel imageWidgetPanel;
	
	@UiField TextBox amountPersons;
	@UiField CheckBox makePublic;
	@UiField TextBox recipeDate;
	@UiField HTML recipeDateError;
	
	@UiField HorizontalPanel addInfoPanel;
	
	@UiField FlexTable SuggestTable;
	@UiField FlexTable commentTable;
	
	@UiField Anchor PreparationButton;
	@UiField CheckBox preparationFactor;
	@UiField TextArea cookingInstr;
	
	
	@UiField VerticalPanel MenuTableWrapper;
	@UiField FlexTable MenuTable;
	@UiField FlowPanel collectionPanel;
	@UiField HTML bottomIndikator;
	
	@UiField Button newRecipeButton;
	@UiField Button generatePDFButton;
	@UiField Button publishButton;
	@UiField Button duplicateButton;
	@UiField Button saveButton;
	@UiField Button deleteButton;
	
	@UiField static SelectionStyleRow selectionStyleRow;
	@UiField static EvenStyleRow evenStyleRow;
	@UiField static TextErrorStyle textErrorStyle;
	
	
	// ---------------------- Class Variables ----------------------
	
	private RechnerActivity presenter;
	private DataController dco;
	
	private FlowPanel panelImages = new FlowPanel();
	private UploadPhoto uploadWidget;
	private HandlerRegistration imagePopUpHandler = null;
	private static int overlap = 0;
	
	private HTML htmlCooking;
	private Boolean askForLess;
	private Boolean askForLess2;
	private Image showImageRezept = new Image();
	private Anchor bildEntfernen;
	private HandlerRegistration showImageHandler = null;
	private FlexTableRowDragController tableRowDragController = null;
	private FlexTableRowDropController flexTableRowDropController = null;
	
	private boolean saved = false;
	private boolean infoDialogIsOpen = false;
	
	private int numberofComments = 0;
	
	private Listener listener;
	private int selectedRow = 0;
	private Recipe recipe;
	private String recipeId;
	
	
	public interface Listener {
		void onItemSelected(Ingredient item);
	}
	
	interface SelectionStyleRow extends CssResource {
		String selectedRow();
	}
	interface EvenStyleRow extends CssResource {
		String evenRow();
	}
	interface TextErrorStyle extends CssResource {
		String redTextError();
	}
	
	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	
	// ---------------------- public Methods -----------------------
	
	public RecipeEdit() {
		initWidget(uiBinder.createAndBindUi(this));
		this.setVisible(false);
		
	    tableRowDragController = new FlexTableRowDragController(dragArea);
	    flexTableRowDropController = new FlexTableRowDropController(MenuTable,this);
	    tableRowDragController.registerDropController(flexTableRowDropController);

	    saveButton.setEnabled(false);
		generatePDFButton.setEnabled(false);
		publishButton.setEnabled(false);
		duplicateButton.setEnabled(false);
		saveButton.setEnabled(false);
		
		// Image
		uploadWidget = new UploadPhoto(this);
		uploadWidget.setStyleName("notInline");	
		imageWidgetPanel.insert(uploadWidget,0);
		imageWidgetPanel.setVisible(false);
	    
	    MenuTable.getColumnFormatter().setWidth(0, "300px");
	}
	
	

	private void bind() {
		// Listen to the EventBus 
		presenter.getEventBus().addHandler(IngredientAddedEvent.TYPE,
				new IngredientAddedEventHandler() {
					@Override
					public void onEvent(IngredientAddedEvent event) {
						if (recipe != null) {
							addIngredient(event.ing);
							changeSaveStatus(false);
							updateIcons();
						}
					}
				});
		presenter.getEventBus().addHandler(MonthChangedEvent.TYPE,
				new MonthChangedEventHandler() {
					@Override
					public void onEvent(MonthChangedEvent event) {
						if (recipe != null)
							updateIcons();
					}
				});
		presenter.getEventBus().addHandler(LoginChangedEvent.TYPE,
				new LoginChangedEventHandler() {
					@Override
					public void onEvent(LoginChangedEvent event) {
						if (recipe != null)
							updateLoginSpecificParameters();
					}
				});
		presenter.getEventBus().addHandler(RecipeLoadedEvent.TYPE,
				new RecipeLoadedEventHandler() {
					@Override
					public void onEvent(RecipeLoadedEvent event) {
						recipe = event.recipe;
						setVisible(true);
						updateParameters();
						changeSaveStatus(true);
					}
				});
		presenter.getEventBus().addHandler(AlertEvent.TYPE,
				new AlertEventHandler() {
					@Override
					public void onEvent(final AlertEvent event) {
						if (event.destination == AlertEvent.Destination.EDIT || event.destination == AlertEvent.Destination.BOTH) {
							alertPanel.insert(event.alert,0);
							Timer t = new Timer() {
								public void run() {
									event.alert.close();
								}
							};
							if (event.timeDisplayed != null)
								t.schedule(event.timeDisplayed);
						}
					}
				});
	}
	
	public void setPresenter(RechnerActivity presenter) {
		this.presenter = presenter;
		this.dco = presenter.getDCO();
		this.setHeight("1600px");
		
		bind();
	}
	
	public void updateParameters() {
		changeSaveStatus(false);
		RezeptName.setText(recipe.getTitle());
		rezeptDetails.setText(recipe.getSubTitle());
		amountPersons.setText(recipe.getPersons().toString());
		
		// Image
		if(recipe.getImage() !=null){
			codeImage.setHTML("<img src='" + recipe.getImage().getServingUrl() + "=s120-c' />");
		}
		
		//Date
		DateTimeFormat dtf = DateTimeFormat.getFormat("dd.MM.yy");
		Date date = recipe.getCookingDate();
		if(date != null)
		{
			recipeDate.setText(dtf.format(date));
		}
		
	    if(dco.getCurrentKitchen() == null){
	    	PreparationButton.setVisible(false);
	    }
		
	    // Cooking instruction
	    String cookingInstructions = recipe.getCookInstruction();
	    if(dco.getLoginInfo() != null && !dco.getLoginInfo().isLoggedIn() || dco.getLoginInfo() == null){
	    	presenter.getEventBus().fireEvent(new AlertEvent("Sie sind nicht angemeldet. Alle Änderungen am Rezept können nicht gespeichert werden.", AlertType.INFO, AlertEvent.Destination.EDIT));
    	}
		cookingInstr.setText(cookingInstructions);
		
		if (recipe.getOpen() == true)
			publishButton.setText("Veröffentlichung rückgängig");
		else
			publishButton.setText("veröffentlichen");
		
		updateLoginSpecificParameters();
		updateCo2Value();
		updateIngredients();
		
	}
	
	public void updateLoginSpecificParameters() {
		if (dco.getLoginInfo() != null && dco.getLoginInfo().isLoggedIn()) {
			codeImage.setHTML("<img src='http://placehold.it/120x120' />");
			initializeCommentingField();
			
			imageWidgetPanel.setVisible(true);
			
			generatePDFButton.setEnabled(true);
			publishButton.setEnabled(true);
			duplicateButton.setEnabled(true);
			saveButton.setEnabled(true);
		}
	}
	
	public void updateCo2Value() {
		co2valueLabel.setText("" + (recipe.getCO2Value().intValue()));
		co2Image.setUrl("/images/rating_bars.png");
	}

	// ---------------------- UI Handlers ----------------------
	
	@UiHandler("RezeptName")
	void onEdit(KeyUpEvent event) {
		if(RezeptName.getText() != ""){
			recipe.setTitle(RezeptName.getText());
			changeSaveStatus(false);
		}
	}
	
	@UiHandler("cookingInstr")
	void onEditCook(KeyUpEvent event) {
		if(cookingInstr.getText() != ""){
			recipe.setCookInstruction(cookingInstr.getText());
			changeSaveStatus(false);
		}
	}

	@UiHandler("rezeptDetails")
	void onEditSub(KeyUpEvent event) {
		if(rezeptDetails.getText() != ""){
			recipe.setSubTitle(rezeptDetails.getText());
			changeSaveStatus(false);
		}
	}
	
	
	@UiHandler("MenuTable")
	void onClick(ClickEvent event) {
		// Select the row that was clicked (-1 to account for header row).
		Cell cell = MenuTable.getCellForEvent(event);
		if (cell != null) {
			int row = cell.getRowIndex();
			//selectRow(row);
		}
	}
	
	
	@UiHandler("amountPersons")
	void onKeyUp(KeyUpEvent event) {
		String errorStyle = textErrorStyle.redTextError();
		String text = amountPersons.getText();
		Long persons = 4L;
		boolean success = false;
		
		try { 
			if ("".equals(text)) {
				amountPersons.removeStyleName(errorStyle);
			}
			else {
				persons = Long.parseLong(amountPersons.getText().trim());
				if (persons > 0) {
					success = true;
					amountPersons.removeStyleName(errorStyle);
				}
			}
		}
		catch (IllegalArgumentException IAE) {}
		
		if (success) {
			recipe.setPersons(persons);
			updateCo2Value();
			changeSaveStatus(false);
		}
		else {
			amountPersons.addStyleName(errorStyle);
		}
	}

	
	@UiHandler("saveButton")
	public void onSaveClicked(ClickEvent event) {
		dco.saveRecipe(recipe);
		changeSaveStatus(true);
	}
	
	@UiHandler("closeRecipe")
	void onCloseClicked(ClickEvent event) {
		presenter.goTo(new RechnerRecipeViewPlace(dco.getRecipeScope().toString()));
	}
	
	@UiHandler("generatePDFButton")
	public void onGeneratePDFButtonClicked(ClickEvent event) {
		presenter.getEventBus().fireEvent(new AlertEvent("This is a veeeery Long message which should be displayd in RecipeEdit and not in RecipeView", AlertType.ERROR, AlertEvent.Destination.EDIT));
		
	}
	
	@UiHandler("deleteButton") 
	public void onDeleteClicked(ClickEvent event) {
		saved = true;
		if (dco.getLoginInfo().isLoggedIn())
			dco.deleteRecipe(recipe.getId());
		presenter.goTo(new RechnerRecipeViewPlace(dco.getRecipeScope().toString()));
	}
	
	@UiHandler("publishButton")
	public void onPublishClicked(ClickEvent event) {
		dco.approveRecipe(recipe, true);
	}

	@UiHandler("newRecipeButton")
	public void onAddRecipeButtonPress(ClickEvent event) {
		presenter.goTo(new RechnerRecipeEditPlace("new"));
	}

	@UiHandler("recipeDate")
	void onBlur(BlurEvent event)  {
		Date date = getDate();
		if (date != null) {
			dco.getEditRecipe().setCookingDate(date);
			changeSaveStatus(false);
		}
	}
	
	private Date getDate() {
		String text = recipeDate.getText();
		Date date = null;
		try { 
			if ("".equals(text)) {}
			else {
				DateTimeFormat fmt = DateTimeFormat.getFormat("dd.MM.yy");
				date = fmt.parseStrict(text);	
				recipeDateError.setHTML("");
			}
		}
		catch (IllegalArgumentException IAE) {
			if(!"TT/MM/JJ".equals(text))
				recipeDateError.setHTML("'" + text + "' is not a propper formated Date.");
			else
				recipeDateError.setHTML("");
			//recipeDate.setText("");
			//recipeDate.setCursorPos(0);
		}
		return date;
	}
	
	/**
	 * @return true if recipe has changed since last save, false otherwise
	 */
	public boolean isSaved() {
		return saved;
	}
	
	private void changeSaveStatus(boolean saved) {
		this.saved = saved;
		saveButton.setEnabled(!saved);
	}
	
	public void removeIngredient(IngredientWidget ingWidget) {
		recipe.removeIngredient(ingWidget.getIngredient());
		MenuTable.remove(ingWidget);
		
		// does this work to prevent the error? which error?
		// if ingredientsDialog is open, yet item gets removed... remove also IngredientsDialog
		/*
		styleRow(removedIndex, false);
		
		if(selectedRow == removedIndex){
			if(addInfoPanel.getWidgetCount() ==2){
				addInfoPanel.remove(1);
			}
		} else {
			if(selectedRow > removedIndex){
				selectedRow = selectedRow-1;
				selectRow(selectedRow);
			}
		}*/
		
		// set the colors in the right order...
		String style = evenStyleRow.evenRow();
		for(Integer rowIndex = 0; rowIndex<MenuTable.getRowCount(); rowIndex++){
			MenuTable.getRowFormatter().removeStyleName(rowIndex, style);
			if ((rowIndex % 2) == 1) {
				MenuTable.getRowFormatter().addStyleName(rowIndex, style);
			} 
		}
		updateCo2Value();
		changeSaveStatus(false);
	}
	
	public void updateIngredientValue(Ingredient ingSpec) {
		((IngredientWidget)MenuTable.getWidget(selectedRow,0)).updateCO2Value();
		updateCo2Value();
	}
	
	
	public void updateIngredients() {
		MenuTable.clear();
		for (Ingredient ingSpec : recipe.getIngredients()) {
			addIngredient(ingSpec);
		}
		updateCo2Value();
	}
	
	public void addIngredient(Ingredient ingSpec) {
		int row = MenuTable.getRowCount();
		IngredientWidget ingWidget = new IngredientWidget(dco, ingSpec,this, dco.getCurrentMonth());
		MenuTable.setWidget(row, 0, ingWidget);
		
		// drag Handler
		tableRowDragController.makeDraggable(ingWidget,ingWidget.getDragHandle());
		
		//Alternate Coloring
		if ((row % 2) == 1) {
			String style = evenStyleRow.evenRow();
			MenuTable.getRowFormatter().addStyleName(row, style);
		}
		updateCo2Value();
	}

	
	// ---------------------- private Methods ---------------------

	private void updateIcons() {
		Iterator<Widget> it = MenuTable.iterator();
		while (it.hasNext()) {
			((IngredientWidget)it.next()).updateIcons();
		}
	}

		private void initializeCommentingField() {
			if (recipe.getSavingPotentials() != null) {
				numberofComments = recipe.getSavingPotentials().size();
						
				final Anchor addCommentButton = new Anchor("Einen Kommentar hinzufügen.");
				addCommentButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
						commentTable.remove(addCommentButton);
						fillCommentBoxes(null,numberofComments);
						numberofComments = numberofComments +1;
						commentTable.setWidget(numberofComments ,1,addCommentButton);
	
					}
	
				});
				
				for (int i = 0; i < numberofComments; i++) {
					fillCommentBoxes(recipe.getSavingPotentials().get(i),i);
				}
				
				commentTable.setWidget(numberofComments ,1,addCommentButton);
			}
		}
		
		private void fillCommentBoxes(SavingPotential recipeComment, int thisRow) {
			final Anchor removeRowButton = new Anchor("x");
			removeRowButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					int thisRow = getWidgetRow(removeRowButton,commentTable);
					commentTable.removeRow(thisRow);
				}
			});
			
			TextBox commentBox = new TextBox();
			if(recipeComment != null){
				commentBox.setText(recipeComment.symbol);
			}
			
			commentBox.addKeyUpHandler(new KeyUpHandler() {
				public void onKeyUp(KeyUpEvent event)  {
					updateComments();
				}
			});
			
		
			commentBox.setWidth("273px");
			
			if(recipeComment != null && recipeComment.amount != 0){
					setAmountBox(thisRow, recipeComment.amount);
			} else {
				final Anchor addCommentAmountButton = new Anchor("+");
				
				addCommentAmountButton.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						int thisRow = getWidgetRow(addCommentAmountButton,commentTable);
						commentTable.remove(addCommentAmountButton);
						setAmountBox(thisRow, 0);
					}
				});
				
				commentTable.setWidget(thisRow ,2,addCommentAmountButton);
			}

			commentTable.setWidget(thisRow,0,removeRowButton);
			commentTable.setWidget(thisRow,1,commentBox);
		}

		
		private void updateComments() {
			recipe.getSavingPotentials().clear();
			for (int i = 0; i < commentTable.getRowCount()-1; i++) {
				TextBox readBox = (TextBox) commentTable.getWidget(i, 1);
				if(readBox.getText() != ""){
					SavingPotential recipeComment = new SavingPotential(readBox.getText());
					try{
						TextBox readAmountBox = (TextBox) commentTable.getWidget(i, 2);
						recipeComment.amount = Integer.parseInt(readAmountBox.getText());
					} catch (ClassCastException error) {
						recipeComment.amount = 0;
					} catch (NumberFormatException error2) {}
					
					recipe.getSavingPotentials().add(recipeComment);
				}	
			}
			changeSaveStatus(false);
		}
		
		public void setAmountBox(int thisRow, int amount) {
			TextBox commentAmountBox = new TextBox();
			commentAmountBox.setText(Integer.toString(amount));
			commentAmountBox.addKeyUpHandler(new KeyUpHandler() {
				public void onKeyUp(KeyUpEvent event)  {
					int keyCode = event.getNativeKeyCode();
					if ((Character.isDigit((char) keyCode)) 
							|| (keyCode == KeyCodes.KEY_BACKSPACE)
							|| (keyCode == KeyCodes.KEY_DELETE) ) {
						updateComments();
					}
				}
			});
			commentAmountBox.setWidth("20px");
			commentTable.setWidget(thisRow ,2,commentAmountBox);
		}

		
	
	public void selectRow(int row) {

		// maybee into dialog?


		if (selectedRow != -1 && infoDialogIsOpen) {

			VerticalPanel verticalInfoPanel = (VerticalPanel) (addInfoPanel.getWidget(1));
			//InfoZutatDialog infoDialog = (InfoZutatDialog) (verticalInfoPanel.getWidget(0));

			//IngredientSpecification zutatSpec2 = infoDialog.getZutatSpec();

			//recipe.getIngredients().set(selectedRow, zutatSpec2);
		}

		Ingredient zutatSpec = recipe.getIngredients().get(row);

		if (zutatSpec == null) {
			return;
		}

		FoodProduct zutat = dco.getIngredientByID(zutatSpec.getId());

		//openSpecificationDialog(zutatSpec, zutat, (TextBox) MenuTable.getWidget(row, 1), MenuTable, row);

		styleRow(selectedRow, false);
		styleRow(row, true);
		selectedRow = row;

		if (listener != null) {
			listener.onItemSelected(zutatSpec);
		}
	}

/*
	private void openSpecificationDialog(IngredientSpecification zutatSpec, Ingredient zutat, TextBox amount, FlexTable MenuTable, int selectedRow) {
		// if another one was already open
		if (infoDialogIsOpen) {
			addInfoPanel.remove(1);
		} else {
			infoDialogIsOpen = true;
		}

		InfoZutatDialog infoZutat = new InfoZutatDialog(zutatSpec, zutat, amount, MenuTable, selectedRow, recipe, SuggestTable, this);
		infoZutat.setPresenter(presenter);
		addInfoPanel.insert(infoZutat, 1);
	}
		*/
		

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
/*
	// REFACTOR: listen to EventBus
	public void updateIngSpecWidgetSaison() {
		if (addInfoPanel.getWidgetCount() == 2) {
			InfoZutatDialog infoZutat = (InfoZutatDialog) addInfoPanel.getWidget(1);
			infoZutat.updateSaison();
		}
	}
	*/
	
	public void setImageUrl(String url) {
		codeImage.setHTML("<img src='" + url + "=s120-c' />");
	}

	// here comes the Image Uploader:
	private IUploader.OnFinishUploaderHandler onFinishUploaderHandler = new IUploader.OnFinishUploaderHandler() {
		public void onFinish(IUploader uploader) {
			if (uploader.getStatus() == Status.SUCCESS) {

				GWT.log("Successfully uploaded image: " + uploader.fileUrl(), null);
				new PreloadedImage(uploader.fileUrl(), showImage);

			}
		}
	};

	// Attach an image to the pictures viewer
	OnLoadPreloadedImageHandler showImage = new OnLoadPreloadedImageHandler() {
		public void onLoad(PreloadedImage image) {
			image.setWidth("125px");
			panelImages.add(image);
		}
	};

	public Recipe getRecipe() {
		return this.recipe;
	}


	public void closeRecipeEdit() {
		
		/*
		if (!saved && dco.getLoginInfo().isLoggedIn() && recipe != null) {
			String saveText = recipe.getTitle() + " ist noch nicht gespeichert!";
			final ConfirmDialog dlg = new ConfirmDialog(saveText);
			dlg.statusLabel.setText("Speichern?");

			dlg.yesButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					dco.saveRecipe(recipe);
					saved = true;
					dlg.hide();
				}
			});
			dlg.noButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					dlg.hide();
				}
			});

			dlg.show();
			dlg.center();
		}
		*/
		
		if (!saved && dco.getLoginInfo().isLoggedIn() && recipe != null) {
			dco.saveRecipe(recipe);
			saved = true;
		}
		
		dco.clearEditRecipe();
		
	}




	

}






