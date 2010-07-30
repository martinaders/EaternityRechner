/*
 * Copyright 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ch.eaternity.client;



import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import ch.eaternity.shared.SingleDistance;

import com.google.gwt.core.client.GWT;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.i18n.client.DateTimeFormat;

import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ListBox;

import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Label;



/**
 * The top panel, which contains the 'welcome' message and various links.
 */
public class TopPanel extends Composite {

  interface Binder extends UiBinder<Widget, TopPanel> { }
  private static final Binder binder = GWT.create(Binder.class);
  static String currentHerkunft = "Schweiz";

  @UiField Button locationButton;
  @UiField Anchor signOutLink;
  @UiField Anchor signInLink;
  @UiField Anchor AdminLink;
  @UiField Anchor ingredientLink;
  @UiField Label loginLabel;
  @UiField
static ListBox Monate;
  @UiField static Label locationLabel;
  @UiField InlineLabel loadingLabel;
  @UiField
static TextBox clientLocation;
  public static Placemark currentLocation;
  public static DistancesDialog ddlg;

protected static ArrayList<SingleDistance> allDistances = new ArrayList<SingleDistance>();
  
  public TopPanel() {
    initWidget(binder.createAndBindUi(this));
    locationButton.setEnabled(false);
    AdminLink.setVisible(false);
    ingredientLink.setVisible(false);
    signOutLink.setVisible(false);
  
	Monate.addItem("Januar");
	Monate.addItem("Februar");
	Monate.addItem("März");
	Monate.addItem("April");
	Monate.addItem("Mai");
	Monate.addItem("Juni");
	Monate.addItem("Juli");
	Monate.addItem("August");
	Monate.addItem("September");
	Monate.addItem("Oktober");
	Monate.addItem("November");
	Monate.addItem("Dezember");
	
	Date date = new Date();
	Monate.setSelectedIndex(date.getMonth());
	
	//.parse( Integer.toString( TopPanel.Monate.getSelectedIndex() ));
	

  }
  

  @UiHandler("Monate")
  void onChange(ChangeEvent event) {
	  EaternityRechner.updateSaison();
  }

  @UiHandler("locationButton")
  public void onClick(ClickEvent event) {
	  
	  
	  ddlg = new DistancesDialog(clientLocation.getText()); 

	  

  }
  


}
