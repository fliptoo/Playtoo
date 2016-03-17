package controllers.Playtoo;

import play.mvc.Before;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class BaseApi extends Controller {

	@Before
	protected static void before() throws Throwable {
		request.args.put(REQ_TYPE, API);
		request.format = "json";
	}

	protected static String getStackTrace(Throwable throwable) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		throwable.printStackTrace(printWriter);
		return writer.toString();
	}

	protected static void OK() {
		Map<String, String> o = new HashMap<String, String>();
		o.put("status", "OK");
		renderJSON(o);
	}

	protected static void NOK() {
		Map<String, String> o = new HashMap<String, String>();
		o.put("status", "NOK");
		renderJSON(o);
	}
}
