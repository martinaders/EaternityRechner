package ch.eaternity.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import ch.eaternity.client.NotLoggedInException;
import ch.eaternity.client.DataService;
import ch.eaternity.shared.Data;
import ch.eaternity.shared.Rezept;
import ch.eaternity.shared.Zutat;
import ch.eaternity.shared.ZutatSpecification;
import ch.eaternity.shared.Zutat.Herkuenfte;
import ch.eaternity.shared.Zutat.Produktionen;
import ch.eaternity.shared.Zutat.Transportmittel;
import ch.eaternity.shared.Zutat.Zustaende;



import com.google.gwt.user.client.Window;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DataServiceImpl extends RemoteServiceServlet implements DataService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6050252880920260705L;

	private static final Logger LOG = Logger.getLogger(DataServiceImpl.class.getName());
	private static final PersistenceManagerFactory PMF =
		JDOHelper.getPersistenceManagerFactory("transactions-optional");

	public String addZutat(Zutat zutat) throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();

		try {
			pm.makePersistent(zutat);
		} finally {
			pm.close();
		}
		return zutat.getSymbol();
	}


	public String addRezept(Rezept rezept) throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		
//		Rezept newRezept = new Rezept();
//		newRezept.setSymbol(rezept.getSymbol());
//		newRezept.addZutaten(rezept.getZutaten());
//		newRezept.setOpen(rezept.isOpen());
		

		try {
			UserRezept userRezept = new UserRezept(getUser());
			userRezept.setRezept(rezept);
			pm.makePersistent(userRezept);
			Long key = userRezept.getRezept().getId();
//			List<String> zutatSpecificationKeys = new ArrayList<String>();
			for(ZutatSpecification zutat: rezept.getZutaten()){
				zutat.setRezeptKey(key);
				pm.makePersistent(zutat);
			}

//			userRezept.setRezept(rezept);
//			pm.makePersistent(rezept);
        } finally {
			pm.close();
		}
		return rezept.getSymbol();
	}


	public void removeRezept(Long rezept_id) throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try {
			//			user verification is missing here! ist it necessary?
			UserRezept rezept =	pm.getObjectById(UserRezept.class,rezept_id);
			pm.deletePersistent(rezept);


			//			Query q = pm.newQuery(UserRezept.class, "user == u");
			//			q.declareParameters("com.google.appengine.api.users.User u");
			//			List<UserRezept> rezepte = (List<UserRezept>) q.execute(getUser());


		} finally {
			pm.close();
		}
	}



	@SuppressWarnings("unchecked")
	public List<Rezept> getYourRezepte() throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		List<Rezept> rezepte = new ArrayList<Rezept>();
		try {
			Query q = pm.newQuery(UserRezept.class, "user == u");
			q.declareParameters("com.google.appengine.api.users.User u");
			q.setOrdering("createDate");
			//			rezepte = (List<UserRezept>) q.execute(getUser());
			List<UserRezept> rezepteUser = (List<UserRezept>) q.execute(getUser());

			for (UserRezept rezept : rezepteUser) {
				rezepte.add(rezept.getRezept());
			}


		} finally {
			pm.close();
		}
		return rezepte;
	}

	public Data getData() throws NotLoggedInException {
		// reference:
		// http://code.google.com/p/googleappengine/source/browse/trunk/java/demos/gwtguestbook/src/com/google/gwt/sample/gwtguestbook/server/GuestServiceImpl.java
		PersistenceManager pm = getPersistenceManager();
		List<Rezept> rezeptePersonal = new ArrayList<Rezept>();
		List<Rezept> rezepte = new ArrayList<Rezept>();
		//		List<Zutat> zutaten = new ArrayList<Zutat>();
		Data data = new Data();

		try {
//			Query specs = pm.newQuery(ZutatSpecification.class);
//			List<ZutatSpecification> specsList = (List<ZutatSpecification>) specs.execute();
			
			if (getUser() != null) {
				Query q = pm.newQuery(UserRezept.class, "user == u");
				q.declareParameters("com.google.appengine.api.users.User u");
				q.setOrdering("createDate");
				List<UserRezept> rezepteUser = (List<UserRezept>) q.execute(getUser());

				for (UserRezept userRezept : rezepteUser) {
					List<ZutatSpecification> specsList =  new ArrayList<ZutatSpecification>();
					Rezept rezept = userRezept.getRezept();
					Long key = rezept.getId();
					Query zutaten = pm.newQuery(ZutatSpecification.class, "RezeptKey == key");
					zutaten.declareParameters("Long lastNameParam");
					List<ZutatSpecification> zutatenList = (List<ZutatSpecification>) zutaten.execute(key);
					for(ZutatSpecification zutat : zutatenList){
						ZutatSpecification newZutat = new ZutatSpecification(zutat.getZutat_id(), zutat.getName(),
								zutat.getCookingDate(),zutat.getZustand(),zutat.getProduktion(), 
								zutat.getTransportmittel());
						specsList.add(newZutat);
					}
					rezept.addZutaten(specsList);
					rezeptePersonal.add(rezept);
				}

				data.setYourRezepte(rezeptePersonal);
			}
			Query q2 = pm.newQuery(Rezept.class, "open == true");
			q2.setOrdering("createDate");
			List<Rezept> rezeptePublic =   (List<Rezept>) q2.execute();
			

//			return (Employee[]) employees.toArray(new Employee[0]);
			// pm.detachCopyAll(
			for (Rezept rezeptPublic : rezeptePublic) {
				List<ZutatSpecification> specsList =  new ArrayList<ZutatSpecification>();
				Rezept rezept = rezeptPublic.getRezept();
				Long key = rezept.getId();
				Query zutaten = pm.newQuery(ZutatSpecification.class, "RezeptKey == key");
				zutaten.declareParameters("Long lastNameParam");
				List<ZutatSpecification> zutatenList = (List<ZutatSpecification>) zutaten.execute(key);
				for(ZutatSpecification zutat : zutatenList){
					ZutatSpecification newZutat = new ZutatSpecification(zutat.getZutat_id(), zutat.getName(),
							zutat.getCookingDate(),zutat.getZustand(),zutat.getProduktion(), 
							zutat.getTransportmittel());
					specsList.add(newZutat);
				}
				rezept.addZutaten(specsList);
				rezepte.add(rezept);
			}
			data.setPublicRezepte(rezepte);

			Query q3 = pm.newQuery(Zutat.class);
			q3.setOrdering("createDate");
			List<Zutat> zutatenQuery = (List<Zutat>) q3.execute();

			List<Zutat> zutaten = new ArrayList<Zutat>(zutatenQuery.size());
			for (Zutat zutat : zutatenQuery) {

				List<Long> alternativen = new ArrayList<Long>(zutat.getAlternativen().size());
				for(Long alternative : zutat.getAlternativen()){
					alternativen.add(alternative);
				}


				//		    	  ZutatSpecification stdMengen = new ZutatSpecification(zutat.getId(),zutat.getSymbol());
				//		    	  if(zutat.getZutatStdWerte_id() != null){
				//		    		  ZutatSpecification stdMengeRequest =	pm.getObjectById(ZutatSpecification.class,zutat.getZutatStdWerte_id());
				//		    		  stdMengen.setHerkunft(stdMengeRequest.getHerkunft());
				//		    		  stdMengen.setMengeGramm(stdMengeRequest.getMengeGramm());
				//		    	  }
				//		    			  											,,
				//		    			  											null, zutat.getZutatStdWerte().getZustand(), zutat.getZutatStdWerte().getProduktion(),
				//		    			  											zutat.getZutatStdWerte().getTransportmittel(), zutat.getZutatStdWerte().getLabel());
				//		    		  zutat.getZutatStdWerte();
//				pm.detachCopy(zutat);
				ArrayList<Herkuenfte> herkuenfte = new ArrayList<Herkuenfte>();
				for( Herkuenfte herkunft : zutat.getHerkuenfte()){
					herkuenfte.add(Herkuenfte.valueOf(herkunft.name()));
				}
				
				zutaten.add(new Zutat( zutat.getId(), zutat.getSymbol(), zutat.getCreateDate(), zutat.getCO2eWert(),
						alternativen, zutat.getStdHerkunft(),zutat.getStdZustand(),zutat.getStdProduktion(),zutat.getStdTransportmittel(),
						zutat.getStdMengeGramm() , herkuenfte, zutat.getStdStartSeason(),zutat.getStdStopSeason()));
				

				//		    			  zutat_id, herkunft, cookingDate, zustand, produktion, transportmittel, labe)
			}
			data.setZutaten(zutaten);
			
		
		} finally {
			pm.close();
		}
		return data;
	}

	private void checkLoggedIn() throws NotLoggedInException {
		if (getUser() == null) {
			throw new NotLoggedInException("Not logged in.");
		}
	}

	private User getUser() {
		UserService userService = UserServiceFactory.getUserService();
		return userService.getCurrentUser();
	}

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}
}