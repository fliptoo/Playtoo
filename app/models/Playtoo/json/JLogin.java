package models.Playtoo.json;

import models.Playtoo.db.UserID;
import play.Play;
import controllers.Playtoo.Swagger;

public class JLogin extends JUser {

	public String apiVersion;
	public String apiMinVersion;

	public JLogin(UserID user) {
		super(user);
		this.apiVersion = Play.configuration.getProperty("api.version");
		this.apiMinVersion = Swagger.API_MIN_VERSION;
	}
}
