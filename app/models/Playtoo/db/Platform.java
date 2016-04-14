package models.Playtoo.db;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.Model;

@Entity
public class Platform extends Model {

	public final static String IOS = "IOS";
	public final static String ANDROID = "ANDROID";

	public String type;

	public String device;

	public String token;

	public String version;

	@ManyToOne
	public UserID user;

	public Date lastUsed;

	public static Platform create(UserID user, String type, String token,
			String device, String version) {
		Platform platform = new Platform();
		platform.user = user;
		platform.type = type;
		platform.token = token;
		platform.device = device;
		platform.version = version;
		platform.lastUsed = new Date();
		return platform.save();
	}

	public static List<String> findAllToken(String type) {
		if (StringUtils.isNotEmpty(type))
			return Platform.em()
					.createQuery(
							"SELECT p.token FROM Platform p WHERE p.type = '"
									+ type + "'")
					.getResultList();
		return Platform.em().createQuery("SELECT p.token FROM Platform")
				.getResultList();
	}

	public String toString() {
		return type;
	}
}
