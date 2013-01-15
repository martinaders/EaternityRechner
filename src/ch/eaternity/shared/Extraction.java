package ch.eaternity.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.Key;

public class Extraction implements Serializable,Cloneable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -3996022378367844877L;

	@Id Long id;
    
	public String symbol;

	public Date startSeason;
	public Date stopSeason;

	
	public Condition stdCondition;
	public Production stdProduction;
	public MoTransportation stdMoTransportation;
	
	@Embedded
	public List<ProductLabel> stdProductLabels;
	
    private Extraction() {
		
	}
    
    public Extraction(Extraction toClone) {
    	symbol = new String(toClone.symbol);
    	startSeason = (Date) toClone.startSeason.clone();
    	stopSeason = (Date) toClone.stopSeason.clone();
    	
    }
	
    public Extraction(String symbol) {
		this.symbol = symbol;
	}
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Extraction other = (Extraction) obj;
        if ((this.symbol == null) ? (other.symbol != null) : !this.symbol.equals(other.symbol)) {
            return false;
        }
        //if (this.age != other.age) {
        //    return false;
        //}
        return true;
    }
    
    public void setSeason(String strStart,String strStop) {
		this.startSeason = DateTimeFormat.getFormat("dd.MM").parse( strStart );		
		this.stopSeason = DateTimeFormat.getFormat("dd.MM").parse( strStop );
	}

	
}
