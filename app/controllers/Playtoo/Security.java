package controllers.Playtoo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import models.Playtoo.Check;
import models.Playtoo.Checker;
import models.Playtoo.Faulty;
import models.Playtoo.Faulty.Type;
import models.Playtoo.Secured;
import models.Playtoo.db.Platform;
import models.Playtoo.db.UserID;
import models.Playtoo.json.JLogin;
import models.Playtoo.json.JModel;
import play.Play;
import play.libs.Crypto;
import play.libs.Time;
import play.mvc.Before;
import play.mvc.Catch;
import play.mvc.Http;

public class Security extends BaseController {

	private final static String USER = "C";

	@Catch(Faulty.class)
	protected static void onFaulty(Faulty faulty) {
		faulty.apply(Controller.isApi());
	}

	@Before(unless = { "login", "authenticate", "logout" })
	protected static void checkAccess() throws Throwable {

		Secured secured = getActionAnnotation(Secured.class);
		if (secured == null)
			secured = getControllerInheritedAnnotation(Secured.class);
		if (secured == null || !secured.value())
			return;

		// Authenticate
		if (!session.contains(USER)) {
			flash.put("url", "GET".equals(request.method) ? request.url
					: Play.ctxPath + "/");
			if (Controller.isApi())
				forbidden();
			login();
		}

		// Checks
		Check check = getActionAnnotation(Check.class);
		if (check != null)
			check(check);

		check = getControllerInheritedAnnotation(Check.class);
		if (check != null)
			check(check);
	}

	public static void login() throws Throwable {
		boolean usingEmail = UserID.usingEmail();
		rememberMe();
		flash.keep("url");
		render(usingEmail);
	}

	public static void status() {
		Map<String, Boolean> o = new HashMap<String, Boolean>();
		o.put("isConnected", isConnected() ? true : false);
		renderJSON(o);
	}

	public static void authenticate(String recognizer, String password,
			boolean remember, String version, String platform)
			throws Throwable {

		Checker.Required(recognizer);
		Checker.Required(password);

		// Check tokens
		UserID me = authenticate(recognizer, password);
		Boolean allowed = me != null;
		if (validation.hasErrors() || !allowed) {
			if (Controller.isApi()) {
				if (UserID.usingEmail())
					throw new Faulty(Type.INVALID, "Bad-Credential",
							"Invalid email or password");
				else
					throw new Faulty(Type.INVALID, "Bad-Credential",
							"Invalid username or password");
			}

			flash.keep("url");
			if (UserID.usingEmail())
				flash.error("Invalid email or password");
			else
				flash.error("Invalid username or password");
			params.flash();
			login();
		}

		authenticated(remember, me, version, platform);
		redirectToOriginalURL();
	}

	public static void logout(String redirect, String device) throws Throwable {

		UserID me = connected();
		if (StringUtils.isNotEmpty(device)) {
			Platform platform = Platform.find("byDevice", device).first();
			if (platform != null) {
				if (me.platforms.remove(platform)) {
					me.save();
					platform.delete();
				}
			}
		}

		session.clear();
		response.removeCookie("rememberme");

		if (Controller.isApi())
			BaseApi.OK();

		flash.success("secure.logout");
		if (StringUtils.isEmpty(redirect))
			redirect = Play.ctxPath + "/";
		redirect(redirect);
	}

	protected static void redirectToOriginalURL() throws Throwable {
		if (Controller.isApi()) {
			Map<String, Object> o = new HashMap<String, Object>();
			o.put("user", JModel.from(connected(), JLogin.class));
			renderJSON(o);
		}

		String url = flash.get("url");
		if (url == null) {
			url = Play.ctxPath + "/";
		}
		redirect(url);
	}

	protected static void authenticated(boolean remember, UserID user,
			String version, String platform) throws Faulty {

		// Mark user as connected
		session.put(USER, user.id);
		user.lastLogin = new Date();
		user.save();

		// Remember if needed
		if (remember) {
			Date expiration = new Date();
			String duration = "30d";
			expiration.setTime(
					expiration.getTime() + Time.parseDuration(duration));
			response.setCookie("rememberme",
					Crypto.sign(user.id + "-" + expiration.getTime()) + "-"
							+ user.id + "-" + expiration.getTime(),
					duration);
		}
	}

	protected static void rememberMe() throws Throwable {
		Http.Cookie remember = request.cookies.get("rememberme");
		if (remember != null) {
			int firstIndex = remember.value.indexOf("-");
			int lastIndex = remember.value.lastIndexOf("-");
			if (lastIndex > firstIndex) {
				String sign = remember.value.substring(0, firstIndex);
				String restOfCookie = remember.value.substring(firstIndex + 1);
				String user = remember.value.substring(firstIndex + 1,
						lastIndex);
				String time = remember.value.substring(lastIndex + 1);
				Date expirationDate = new Date(Long.parseLong(time));
				Date now = new Date();
				if (expirationDate == null || expirationDate.before(now)) {
					logout(null, null);
				}
				if (Crypto.sign(restOfCookie).equals(sign)) {
					session.put(USER, user);
					redirectToOriginalURL();
				}
			}
		}
	}

	protected static UserID authenticate(String recognizer, String password)
			throws Faulty {
		return UserID.authenticate(recognizer, password);
	}

	protected static boolean check(String profile) {
		return connected().roles.contains(profile);
	}

	protected static UserID connected() {
		if (isConnected()) {
			UserID user = UserID.findById(Long.parseLong(session.get(USER)));
			return user;
		}
		return null;
	}

	protected static boolean isConnected() {
		return session.contains(USER);
	}

	private static void check(Check check) throws Throwable {
		for (String profile : check.value()) {
			boolean hasProfile = check(profile);
			if (!hasProfile) {
				forbidden();
			}
		}
	}

}
