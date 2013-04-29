package ch.eaternity.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import com.googlecode.objectify.annotation.*;

public class Transportation implements Serializable {

	private static final long serialVersionUID = -5971128872903171922L;
	
	@Id Long id;

	private String symbol;
	private Double factor;
    
    private Transportation() {}
    
    public Transportation(Transportation toClone) {
		symbol = new String(toClone.symbol);
		factor = new Double(toClone.factor);
	}

    public Transportation(String symbol) {
    	this.symbol = symbol;
	}
    
    public Transportation(String symbol, Double factor) {
    	this.symbol = symbol;
    	this.factor = factor;
	}
    
    public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Double getFactor() {
		return factor;
	}

	public void setFactor(Double factor) {
		this.factor = factor;
	}
	
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Transportation other = (Transportation) obj;
        if ((this.symbol == null) ? (other.symbol != null) : !this.symbol.equals(other.symbol)) {
            return false;
        }
        return true;
    }
}
