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

	public String token;

	public String version;

	@ManyToOne
	public UserID user;

	public Date lastUsed;

	public Platform(UserID user, String type, String token, String version) {
		this.user = user;
		this.type = type;
		this.token = token;
		this.version = version;
		this.lastUsed = new Date();
	}

	public static void clear(UserID user, String type, String token) {

		Platform ePlatform = null;
		if (StringUtils.isNotEmpty(token))
			ePlatform = Platform.find("byTypeAndToken", type, token).first();
		else
			ePlatform = Platform.find("byTypeAndTokenIsNull", type).first();

		if (ePlatform != null && ePlatform.user.id != user.id) {
			ePlatform.user.platforms.remove(ePlatform);
			ePlatform.user.save();
			ePlatform.delete();
		}
	}

	public static List<String> findAllToken(String type) {
		if (StringUtils.isNotEmpty(type))
			return Platform
					.em()
					.createQuery(
							"SELECT p.token FROM Platform p WHERE p.type = '"
									+ type + "'").getResultList();
		return Platform.em().createQuery("SELECT p.token FROM Platform")
				.getResultList();
	}

	public String toString() {
		return type;
	}
}
