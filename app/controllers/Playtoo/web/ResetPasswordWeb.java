package controllers.Playtoo.web;

import org.apache.commons.lang.StringUtils;

import controllers.Playtoo.BaseWeb;
import models.Playtoo.Faulty;
import models.Playtoo.db.UserID;

public class ResetPasswordWeb extends BaseWeb {

	public static void index(String token, String recognizer, String password,
			String confirm) throws Faulty {
		UserID user = UserID.findByRecognizer(recognizer);
		if (isGET()) {
			if (StringUtils.isEmpty(token))
				validation.addError("token", "Token does not exists.");

			if (user == null)
				validation.addError("recognizer", "Recognizer does not exist.");
			else {
				if (!token.equalsIgnoreCase(user.pwdToken))
					validation.addError("token", "Token is not valid.");
			}
			render(token, recognizer);
		} else {
			if (StringUtils.isEmpty(password))
				validation.addError("password", "New Password is required.");
			if (StringUtils.isEmpty(token))
				validation.addError("confirm", "Confirm password is required.");
			if (!password.equals(confirm))
				validation.addError("New Password",
						"New Password does not match the confirm password");

			if (!token.equalsIgnoreCase(user.pwdToken))
				validation.addError("token", "Invalid token.");

			if (validation.hasErrors())
				render(token, recognizer);

			user.encryptPassword(password);
			user.pwdToken = null;
			user.save();
			ack();
		}
	}

	public static void ack() {
		render();
	}
}
