package ch.eaternity.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface IngredientAddedEventHandler extends EventHandler {
	
	void onEvent(IngredientAddedEvent event);

}
