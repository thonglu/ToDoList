package model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("serial")
public class Task implements Serializable {

	/**
	 * 
	 */
	private int DID = -1;
	private String GID = "";
	private String title;
	private Date dueDate;
	private String priority;
	private String note;
	private String[] colEmail;
	private Group group;
	private boolean isComplete;
	private boolean isDelete;

	private boolean isSync = false;

	public int getDID() {
		return DID;
	}

	public void setDID(int dID) {
		DID = dID;
	}

	public String getGID() {
		return GID;
	}

	public void setGID(String GID) {
		this.GID = GID;
	}

	public boolean isSync() {
		return isSync;
	}

	public void setSync(boolean isSync) {
		this.isSync = isSync;
	}

	public Task(String title, String priority, Group group, Date due,
			String[] colEmail, boolean isComplete, String note) {
		this.title = title;
		this.priority = priority;
		this.note = note;
		this.group = group;
		this.colEmail = colEmail;
		this.dueDate = due;
		this.isComplete = isComplete;
		isDelete = false;
	}

	@Override
	public String toString() {
		String mes = "Title: " + title + "\nPriority: " + priority + "\nDue Date: "
				+ (new SimpleDateFormat("MMM dd yyyy").format(dueDate))
				+ "\nStatus: "
				+ ((isComplete) ? "Complete" : "Not Complete").toString();
		return mes;
	}

	public String toSMSString() {
		String mes = "Title: " + title + "\nPriority: " + priority
				+ "\nNotes: " + note + "\nDue Date: "
				+ (new SimpleDateFormat("MMM dd yyyy").format(dueDate));
		return mes;
	}

	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String[] getColEmail() {
		return colEmail;
	}

	public void setColEmail(String[] colEmail) {
		this.colEmail = colEmail;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

}
