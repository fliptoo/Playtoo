package controllers.Playtoo;

import models.Playtoo.db.UserID;
import play.mvc.Before;

public class BaseWeb extends Controller {

	@Before
	protected static void before() throws Throwable {
		request.args.put(REQ_TYPE, WEB);

		UserID me = connected();
		renderArgs.put("me", me);
	}

	protected static boolean isGET() {
		return request.method.equalsIgnoreCase("GET");
	}

	protected static boolean isPOST() {
		return request.method.equalsIgnoreCase("POST");
	}

}
