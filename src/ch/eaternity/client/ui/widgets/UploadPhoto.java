package ch.eaternity.client.ui.widgets;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import ch.eaternity.client.events.GalleryUpdatedEvent;
import ch.eaternity.client.events.GalleryUpdatedEventHandler;
import ch.eaternity.client.ui.RecipeEdit;
import ch.eaternity.client.DataService;
import ch.eaternity.client.DataServiceAsync;
import ch.eaternity.shared.Recipe;
import ch.eaternity.shared.UploadedImage;

public class UploadPhoto extends Composite implements HasHandlers {

	private static UploadPhotoUiBinder uiBinder = GWT.create(UploadPhotoUiBinder.class);
	interface UploadPhotoUiBinder extends UiBinder<Widget, UploadPhoto> {}

	@UiField Button uploadButton;
	@UiField FormPanel uploadForm;
	@UiField FileUpload uploadField;
	
	private Recipe recipe;
	private DataServiceAsync userImageService = GWT.create(DataService.class);
	private HandlerManager handlerManager;

	public UploadPhoto(final RecipeEdit editRecipeView) {
		handlerManager = new HandlerManager(this);
		
		this.recipe = editRecipeView.getRecipe();

		initWidget(uiBinder.createAndBindUi(this));

		uploadButton.setText("hochladen");
		uploadButton.setText("Loading...");
		uploadButton.setEnabled(false);

		uploadField.setName("image");

		startNewBlobstoreSession();

		uploadForm
				.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {

					@Override
					public void onSubmitComplete(SubmitCompleteEvent event) {
						uploadForm.reset();
						startNewBlobstoreSession();

						String key = event.getResults();
						
						if(key != null){

						userImageService.get(key,
								new AsyncCallback<UploadedImage>() {

									@Override
									public void onFailure(Throwable caught) {
										// TODO Auto-generated method stub

									}

									@Override
									public void onSuccess(final UploadedImage result) {
										
										editRecipeView.getRecipe().setImage(result);
									    editRecipeView.setImageUrl(result.getServingUrl()+"=s150-c");
									    	//editRecipeView.bildEntfernen.setVisible(true);
									    	//editRecipeView.uploadWidget.setVisible(false);
									    
										
										ImageOverlay overlay = new ImageOverlay(result);
										fireEvent(new GalleryUpdatedEvent());

										// TODO: Add something here that says,
										// hey, upload succeeded

										final PopupPanel imagePopup = new PopupPanel(true);
										imagePopup.setAnimationEnabled(true);
										imagePopup.setWidget(overlay);
//										imagePopup.setGlassEnabled(true);
										imagePopup.setAutoHideEnabled(true);

										imagePopup.center();
										imagePopup.setPopupPosition(10, 10);

									}
								});
						} else {
							GWT.log("no image object key was found");
							
						}

					}
				
	
				});
	}

	private void startNewBlobstoreSession() {
		userImageService.getBlobstoreUploadUrl(new AsyncCallback<String>() {

			@Override
			public void onSuccess(String result) {
				uploadForm.setAction(result);
				uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
				uploadForm.setMethod(FormPanel.METHOD_POST);

				uploadButton.setText("hochladen");
				uploadButton.setEnabled(true);

			}

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				GWT.log("error: ", caught);

			}
		});
	}

	@UiHandler("uploadButton")
	void onSubmit(ClickEvent e) {
		uploadForm.submit();
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		handlerManager.fireEvent(event);
	}

	public HandlerRegistration addGalleryUpdatedEventHandler(
			GalleryUpdatedEventHandler handler) {
		return handlerManager.addHandler(GalleryUpdatedEvent.TYPE, handler);
	}
}
