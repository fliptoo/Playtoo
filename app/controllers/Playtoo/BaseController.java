package controllers.Playtoo;

import play.Play;
import play.mvc.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BaseController extends Controller {

    public static final String REQ_TYPE = "REQ_TYPE";
    public static final String API = "API";
    public static final String WEB = "WEB";

    protected static Gson GSON = new GsonBuilder().setDateFormat(
			Play.configuration.getProperty("date.format")).create();

	protected static void renderJSON(Object o) {
		renderJSON(GSON.toJson(o));
	}

}
