package controllers.Playtoo;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonObject;

import models.Playtoo.Checker;
import models.Playtoo.Faulty;
import models.Playtoo.db.UserID;
import models.Playtoo.json.JLogin;
import models.Playtoo.json.JModel;
import play.Play;
import play.libs.OAuth2;
import play.libs.WS;
import play.mvc.Router;
import play.mvc.Scope.Params;
import play.mvc.results.Redirect;

public class Facebook extends Controller {

	public static final String appId = Play.configuration
			.getProperty("facebook.appId");
	public static final String appSecret = Play.configuration
			.getProperty("facebook.secret");
	static OAuth2 FB = new OAuth2("https://graph.facebook.com/oauth/authorize",
			"https://graph.facebook.com/oauth/access_token", appId, appSecret);
	public static final String scope = Play.configuration
			.getProperty("facebook.scope");

	static String authURL() {
		return Router.getFullUrl("Facebook.webAuth");
	}

	public static void apiAuth(Long fbid, String token, String version,
			String platform) throws Throwable {
		Checker.Required(fbid);
		Checker.Required(token);

		request.format = "json";
		verifyToken(fbid, token);
		UserID user = UserID.findByFbid(fbid);
		if (user == null)
			user = register(token);
		if (user == null)
			forbidden();

		user.fbToken = token;
		user.save();
		authenticated(true, user, version, platform);

		Map<String, Object> o = new HashMap<String, Object>();
		o.put("user", JModel.from(connected(), JLogin.class));
		renderJSON(o);
	}

	public static void webAuth(String redirect) throws Throwable {
		if (OAuth2.isCodeResponse()) {
			OAuth2.Response res = FB.retrieveAccessToken(authURL());
			if (res.accessToken != null) {
				UserID user = register(res.accessToken);
				if (user == null)
					forbidden();
				authenticated(true, user, null, null);
				redirectToOriginalURL();
			}
		}

		if (Params.current().get("error") != null) {
			validation.addError("FBConnect-error",
					"FBConnect failed. Please try again.",
					Params.current().get("error"),
					Params.current().get("error_code"));
			params.flash();
			validation.keep();
			redirectToOriginalURL();
		}

		flash.put("url", redirect == null ? "/" : redirect);
		rememberMe();

		String fbAuthUrl = FB.authorizationURL + "?client_id=" + FB.clientid
				+ "&redirect_uri=" + authURL();
		if (StringUtils.isNotEmpty(scope))
			fbAuthUrl = fbAuthUrl + "&scope=" + scope;
		throw new Redirect(fbAuthUrl);
	}

	private static UserID register(String token) throws ParseException, Faulty {
		JsonObject me = WS
				.url("https://graph.facebook.com/me?access_token=%s&fields=id,name,email",
						WS.encode(token))
				.get().getJson().getAsJsonObject();
		return UserID.findOrCreateFromFB(me, token);
	}

	private static void verifyToken(Long fbid, String token) throws Faulty {
		JsonObject result = WS
				.url("https://graph.facebook.com/debug_token?input_token=%s&access_token=%s",
						WS.encode(token), WS.encode(appId + "|" + appSecret))
				.get().getJson().getAsJsonObject();
		if (result.has("data"))
			result = result.get("data").getAsJsonObject();
		if (result.has("error"))
			Checker.Invalid(token);
		if (result.has("user_id") && result.get("user_id").getAsLong() != fbid)
			Checker.Invalid(fbid);
	}
}
