<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<%@ page import="ch.eaternity.shared.Recipe" %>
<%@ page import="ch.eaternity.shared.IngredientSpecification" %>
<%@ page import="ch.eaternity.shared.RecipeComment" %>
<%@ page import="ch.eaternity.shared.CatRyzer" %>
<%@ page import="ch.eaternity.shared.Pair" %>
<%@ page import="ch.eaternity.shared.Util" %>

<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.text.DecimalFormat" %>

<%@ page import="ch.eaternity.server.jsp.StaticDataLoader" %>
<%@ page import="ch.eaternity.server.jsp.StaticProperties" %>
<%@ page import="ch.eaternity.server.jsp.StaticTempBean" %>


<%@ page import="java.util.Date" %>


<jsp:useBean id="vars" scope="session"
     class="ch.eaternity.server.jsp.StaticDataLoader" />
    
<jsp:useBean id="temp" scope="session"
	class="ch.eaternity.server.jsp.StaticTempBean" />
	
<jsp:useBean id="properties" scope="session"
class="ch.eaternity.server.jsp.StaticProperties" />
     
     
<%

List<Recipe> recipes = temp.getRecipes();

if (recipes.size() >0) {
	List<Double> values = new ArrayList<Double>();
	
	for (Recipe recipe : recipes) {
		values.add(recipe.getCO2Value());
	}
	
	Double average = Util.getAverage(values);
	
	if(average > 0){
		
		String total = "200";
		String klimafriendly = properties.formatter.format(200*2/5);
		String length = properties.formatter.format(average/(properties.threshold)*200);
		String formatted = properties.formatter.format( average );
		
		String moreOrLess = "";
		String percent ="";
		Integer position = 1;
		
		if(average <= properties.threshold){
			percent = properties.formatter.format( -((average-properties.threshold)/(properties.threshold))*100 );
		
			if((properties.climateFriendlyValue-average)<0){
				moreOrLess = "weniger";
				position = 2;
			} 
			else {
				percent = properties.formatter.format( ((properties.climateFriendlyValue-average)/(properties.climateFriendlyValue))*100 );
				moreOrLess = "weniger";
			}
		
		}
		
		if(average > (properties.threshold)){
			position = 3;
			length = "200";
			total = properties.formatter.format((properties.threshold/average)*200);
			klimafriendly = properties.formatter.format((properties.climateFriendlyValue/average)*200);
			percent = properties.formatter.format( ((average-properties.threshold)/(properties.threshold))*100 );
			moreOrLess = "mehr";
		}
	
		%>			
		<table style="font-weight: 300;font-size: 14pt; margin-top:4em" >
		<!-- label of the label -->
		<tr><td>

		<table cellspacing="0" cellpadding="0" border="1" class="table rounded label-table">

		<!-- head of the label -->
		<tr style="border-bottom:0px;">
			<td style="border-bottom:0px;">
				<table cellspacing="0" cellpadding="0"  border="0" style="font-weight: 300;font-size: 11pt;margin-top:-20px;">
					<tr>
					 <td style="width:2em">

					 </td>

					 <td style="width:6em;text-align: right;padding:0em 1em 0em 1em;background:white;" ><span class="label-vergleich">Im Vergleich</span></td>
					 <td class="left-border" style="text-align: left;padding-left:1em;background:white;">
					 <% if (position<2){ %>
					 Die Menus verursachen <span style="font-size: 11pt;font-weight: 400;"><%= percent %>% weniger</span> CO<sub>2</sub>* als die vergleichbaren klimafreundlichen Menus.
					 <% }else{ %>
					 Die Menus verursachen <span style="font-size: 11pt;font-weight: 400;"><%= percent %>% <%= moreOrLess %></span> CO<sub>2</sub>* als die vergleichbaren Menus im Durchschnitt.
					 <% } %>
					 </td>

					 <td style="width:11em">

					 </td>
					</tr>
				</table>
			</td>
		</tr>

		<!-- body of the label -->
		<tr>
			<td style="border-top:0px;border-bottom:0px;">
				<table cellspacing="0" cellpadding="0"  border="0" style="width:100%">
					<tr>
					 <td>

						<table cellspacing="0" cellpadding="0" border="1" style="font-weight: 300;font-size: 11pt;margin:2.5em 2em 2em 2em;border:0px;">
						<tr><td style="text-align:right;border-top: 0px;border-bottom: 0px;border-left: 0px;"></td><td style="border-top: 0px;border-bottom: 0px;border-right: 0px"></td></tr>
						<% if (position==1){ %>
							<tr  height="28">
							 <td style="text-align:right;vertical-align:middle;border-top: 0px;border-bottom: 0px;border-left: 0px;padding-right:0.3em;white-space:nowrap;font-size: 11pt;font-weight: 600;text-transform: uppercase;">
							 	Die Menus
							 </td>
							 <td style="border-top: 0px;border-bottom: 0px;border-right: 0px">
							 	<img class="bar" src="green.png" alt="green" height="15"  width="<%= length %>" />
<!-- 								 	<%= formatted %> g CO<sub>2</sub>* -->
							 </td>
							</tr>
						<% } %>
							<tr height="28">
							 <td style="text-align:right;vertical-align:top;border-top: 0px;border-bottom: 0px;border-left: 0px;padding-right:0.3em;width:8em;font-size: 9pt; line-height: 11px; ">
							 	klimafreundliche Menus
							 </td>
							 <td style="border-top: 0px;border-bottom: 0px;border-right: 0px">
							 	<img class="bar" src="orange.png" alt="orange" height="15"  width="<%= klimafriendly %>" />
<!-- 								 	<%= formatted %> g CO<sub>2</sub>* -->
							 </td>
							</tr>
						<% if (position==2){ %>
							<tr  height="28">
							 <td style="text-align:right;vertical-align:middle;border-top: 0px;border-bottom: 0px;border-left: 0px;padding-right:0.3em;white-space:nowrap;font-size: 11pt;font-weight: 600;text-transform: uppercase;">
							 	Die Menus
							 </td>
							 <td style="border-top: 0px;border-bottom: 0px;border-right: 0px">
							 	<img class="bar" src="green.png" alt="green" height="15"  width="<%= length %>" />
<!-- 								 	<%= formatted %> g CO<sub>2</sub>* -->
							 </td>
							</tr>
						<% } %>
							<tr height="28">
							 <td style="text-align:right;vertical-align:middle;border-top: 0px;border-bottom: 0px;border-left: 0px;padding-right:0.3em;font-size: 9pt;">
							 	Alle Menus
							 </td>
							 <td style="border-top: 0px;border-bottom: 0px;border-right: 0px">
							 	<img class="bar" src="gray.png" alt="gray" height="15"  width="<%= total %>" />
<!-- 								 	<%= formatted %> g CO<sub>2</sub>* -->
							 </td>
							</tr>
						<% if (position==3){ %>
							<tr  height="28">
							 <td style="text-align:right;vertical-align:middle;border-top: 0px;border-bottom: 0px;border-left: 0px;padding-right:0.3em;white-space:nowrap;font-size: 11pt;font-weight: 600;text-transform: uppercase;">
							 	Die Menus
							 </td>
							 <td style="border-top: 0px;border-bottom: 0px;border-right: 0px">
							 	<img class="bar" src="green.png" alt="green" height="15"  width="<%= length %>" />
<!-- 								 	<%= formatted %> g CO<sub>2</sub>* -->
							 </td>
							</tr>
						<% } %>	

							<tr><td style="text-align:right;border-top: 0px;border-bottom: 0px;border-left: 0px"></td><td style="border-top: 0px;border-bottom: 0px;border-right: 0px"></td></tr>
						</table>
					 </td>

					<td>
								<span class="howAreYou" style="float:right;margin-top:1em;margin-bottom:-1.8em;font-size:10pt;">Wie ist die Klimabilanz:</span>
							 	<table cellspacing="0" cellpadding="0" border="1" class="rounded" style="float:right;font-weight: 300;font-size: 11pt;margin:2em;padding:1em;">
									<tr><td style="text-align:right;border:0px"></td><td style="border:0px"></td></tr>
									<tr height="28">
									<% if (position==1){ %>
									 <td style="border:0px; font-size: 11pt;font-weight: 600;white-space: nowrap;">
									 	&#x25b6; GROSSARTIG <img class="smile" src="smiley8.png" style="float:none;padding-left:0.5em;margin-bottom:-7px;" alt="smiley" /><img class="smile" src="smiley8.png" style="float:none;margin-bottom:-7px;" alt="smiley" />										 
									 	</td>
									 <% } else { %>
									 <td style="border:0px; color:gray;font-size: 9pt;">
									 	Grossartig
									 </td>
									 <% } %>
									</tr>
									<tr height="28">
									<% if (position==2){ %>
									 <td style="border:0px; font-size: 11pt;font-weight: 600;">
									 	&#x25b6; GUT <img class="smile" src="smiley8.png" style="float:none;padding-left:0.5em;margin-bottom:-7px;" alt="smiley" />
									 </td>
									 <% } else { %>
									 <td style="border:0px; color:gray;font-size: 9pt;">
									 	Gut
									 </td>
									 <% } %>
									</tr>
									<tr height="28">
									<% if (position==3){ %>
									 <td style="border:0px; font-size: 9pt;font-weight: 600;text-transform: uppercase;white-space: nowrap;padding-right:0.2em;">
									 	&#x25b6; Unter Durchschnitt
									 </td>
									 <% } else { %>
									 <td style="border:0px; color:gray;white-space: nowrap;font-size: 9pt;padding-right:1em;">
									 	Unter Durchschnitt
									 </td>
									 <% } %>
									</tr>
									<tr><td style="text-align:right;border-top: 0px;border-bottom: 0px;border-left: 0px"></td><td style="border:0px"></td></tr>
								</table>


					 </td>
					</tr>

				</table>
			</td>
		</tr>

		<!-- legend of the label -->
		<tr>
			<td style="border-top:0px;">
				 <table cellspacing="0" cellpadding="0" border="1" style="font-weight: 300;font-size: 11pt;padding:1em;margin:1em 1em 0em 1em;border-bottom:0px;border-left:0px;border-right:0px;">
					<tr >
					 <td style="padding: 0em 1em 0em 0em;width:9em;border:0px;text-transform: uppercase;vertical-align:top; text-align:center;font-size: 11pt;font-weight: 400;">

					 Welche Menus werden verglichen?

					 </td>

					<td style="vertical-align:top;padding: 0.5em 1em 0em 0em;border-top: 0px;border-bottom: 0px;border-right: 0px;border-left: 0px;font-size: 9pt;" >
					 	<img class="bar" src="gray.png" alt="gray" width="11" height="11" style="padding:3px 3px 0px 0px;" />
					 	ALLE MENUS<br /> Alle gemessenen vergleichbaren Menus.

					 </td>
					 <td style="vertical-align:top;padding: 0.5em 1em 0em 0em;border:0px;font-size: 9pt;">
					 	<img class="bar" src="orange.png" alt="gray" width="11" height="11" style="padding:3px 3px 0px 0px;"/>
					 	KLIMAFREUNDLICHE MENUS<br />Die besten 20% aus der Gruppe "Alle Menus".

					 </td>

					 <td style="padding:0em 0em 0em 1em;text-align:right;border:0px;width:4em;" class="left-border"><% if(properties.DoItWithPermanentIds) { %>
	<a href="<%= properties.BASEURL %>?pid="><img src="QR--CODE" width="42" height="42" /></a>
						<% } else { %> <span style="color:red;font-size:9pt;"></span> <% } %>
					 </td>
					</tr>


				</table>


				</td>
			</tr>
		</table>
			<br />
		</td>
	</tr>
	
	
	</table>


	<%
	}
}	
%>
	