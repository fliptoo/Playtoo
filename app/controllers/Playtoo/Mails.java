package controllers.Playtoo;

import models.Playtoo.db.UserID;
import play.Play;

public class Mails extends play.mvc.Mailer {

	private final static String from = Play.configuration
			.getProperty("mail.smtp.user");

	public static void forgotPassword(UserID user, String resetURL) {
		setFrom("Playtoo <" + from + ">");
		setSubject("Password reset on Playtoo");
		addRecipient(user.recognizer);
		send(user, resetURL);
	}

}
