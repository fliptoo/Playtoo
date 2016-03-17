package controllers.Playtoo;

import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import models.Playtoo.db.UserID;
import play.Play;
import play.vfs.VirtualFile;

public class Swagger extends Controller {

	public static final String API_VERSION = Play.configuration
			.getProperty("api.version");
	public static final String API_MIN_VERSION = Play.configuration
			.getProperty("api.min.version");

	private static final Yaml YAML = new Yaml();

	public static void index() {
		String url = "http://" + request.host + "/swagger/api";
		UserID me = connected();
		render(url, me);
	}

	public static void api(String path) throws Exception {
		if (!StringUtils.isEmpty(path)) {
			path = "/app/swagger/" + path + ".yml";
			VirtualFile vfs = VirtualFile.fromRelativePath(path);
			if (vfs.exists())
				renderJSON(GSON.toJson(YAML.load(vfs.contentAsString())));
			else {
				vfs = VirtualFile.fromRelativePath("{module:Playtoo}" + path);
				if (vfs.exists())
					renderJSON(GSON.toJson(YAML.load(vfs.contentAsString())));
			}
		}

		JsonArray apis = new JsonArray();
		String[] _apis = Play.configuration.getProperty("playtoo.swagger.api")
				.split(",");
		for (String api : _apis)
			apis.add(newApi(api.trim()));
		JsonObject doc = new JsonObject();
		doc.addProperty("swaggerVersion", "1.0.0");
		doc.addProperty("apiVersion", API_VERSION);
		doc.addProperty("basePath", "http://" + request.host);
		doc.add("apis", apis);
		renderJSON(doc.toString());
	}

	private static JsonObject newApi(String path) {
		JsonObject api = new JsonObject();
		api.addProperty("path", "/swagger/api/" + path);
		return api;
	}

}
