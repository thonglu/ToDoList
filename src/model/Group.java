package model;

import java.io.Serializable;

public class Group implements Serializable {

	private String title;
	
	private int DID =-1;
	private String GID;
	
	public Group(String title){
		this.setTitle(title);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getDID() {
		return DID;
	}

	public void setDID(int dID) {
		DID = dID;
	}

	public String getGID() {
		return GID;
	}

	public void setGID(String gID) {
		GID = gID;
	}
	
	@Override
	public String toString(){
		return DID + " "+ GID +" " +title;
	}
}
