package models.Playtoo;

import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.exceptions.PlayException;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Http.Request;

public class Faulty extends PlayException {

	public static class Error extends play.mvc.results.Error {

		private Faulty faulty;

		public Error(Faulty faulty) {
			super(Http.StatusCode.BAD_REQUEST, faulty.getMessage());
			this.faulty = faulty;
		}

		public String toString() {
			return faulty.toString();
		}

	}

	public static enum Type {
		MIN, MAX, NOT_FOUND, DUPLICATED, REQUIRED, INVALID;
	}

	public final Type type;
	public final String code;
	public final String ref;
	public final String action;

	public Faulty(Type type, String ref) {
		this(type, ref, Messages.get(ref));
	}

	public Faulty(Type type, String ref, String message) {
		super(message);
		this.type = type;
		this.ref = ref;
		Request req = Request.current().get();
		this.action = req == null ? null : req.path.replaceAll("/", "_")
				.substring(1);
		StringBuilder o = new StringBuilder();
		o.append(type);
		if (action != null)
			o.append("." + action.toUpperCase());
		if (ref != null)
			o.append("." + ref.toUpperCase());
		this.code = o.toString();
		if (JPA.isEnabled())
			JPA.setRollbackOnly();
	}

	public void apply(boolean isAPI) {
		if (isAPI) {
			Request.current().format = "json";
			throw new Error(this);
		} else {
			Validation.clear();
			Validation.addError(null, getMessage());
		}
	}

	public String toString() {
		return code;
	}

	@Override
	public String getErrorTitle() {
		return toString();
	}

	@Override
	public String getErrorDescription() {
		return getMessage();
	}

}
