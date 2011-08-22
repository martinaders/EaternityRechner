package ch.eaternity.server;

import java.util.Date;


import javax.persistence.Id;

import ch.eaternity.shared.Recipe;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Serialized;


public class UserRecipe{

	@Id Long id;
	
	public Date createDate;
	
	@Indexed
	public User user;
	
	
	@Indexed
	public Long kitchenId;
	
	public Boolean approvedOpen;
	public Boolean requestedOpen;
	
	@Serialized
	public Recipe recipe;
	
	public void setUser(User user) {
		this.user = user;
	}
	public User getUser() {
		return user;
	}
	
	public UserRecipe(){
		
	}

	public UserRecipe( User user){
		this.createDate = new Date();
		this.user = user;	
	}
	
	public UserRecipe( Recipe recipe ,User user){
		this.recipe = recipe;
		this.createDate = new Date();
		this.user = user;	
	}

	public Date getCreateDate() {
		return this.createDate;
	}
	public void setRezept(Recipe recipe) {
		this.recipe = recipe;
	}
	public Recipe getRezept() {
		return recipe;
	}

	public String getUserName() {
		return user.getNickname();
	}





}