package models.Playtoo.db;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Playtoo extends Model {

	public final static String ACTIVE = "ACTIVE";
	public final static String INACTIVE = "INACTIVE";
	public final static int INFINITY = 999999;

	public static final String APNS_DEV = "DEV";
	public static final String APNS_PROD = "PROD";

	public String apnsEnv = APNS_DEV;

	public String devApnsPassword;

	@ManyToOne
	public Media devP12;

	public String prodApnsPassword;

	@ManyToOne
	public Media prodP12;

	@Embedded
	public Log log;

	public static Playtoo findOnlyOne() {
		Playtoo playtoo = Playtoo.find("").first();
		if (playtoo == null) {
			playtoo = new Playtoo();
			playtoo.log = new Log();
			playtoo.save();
		}
		return playtoo;
	}
}
