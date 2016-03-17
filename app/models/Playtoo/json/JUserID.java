package models.Playtoo.json;

import models.Playtoo.db.UserID;

public class JUserID extends JModel {

	public Long fbid;
	public String name;
	public String recognizer;
	public Long avatar;
	public String status;

	public JUserID(UserID user) {
		super(user);
		this.recognizer = user.recognizer;
		if (user.avatar != null)
			this.avatar = user.avatar.id;
		this.fbid = user.fbid;
		this.name = user.name;
		this.status = user.status;
	}
}
