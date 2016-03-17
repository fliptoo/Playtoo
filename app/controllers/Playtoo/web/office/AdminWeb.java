package controllers.Playtoo.web.office;

import models.Playtoo.Faulty;
import models.Playtoo.Util;
import models.Playtoo.db.UserID;
import models.Playtoo.paginate.ListPaginator;

import org.apache.commons.lang.StringUtils;

import controllers.Playtoo.BaseOffice;

public class AdminWeb extends BaseOffice {

	public static void index(String keyword, int page, int length) {

		length = Util.length(length);
		ListPaginator<UserID> users = new ListPaginator(UserID.search(keyword,
				UserID.ADMIN, null, page, length), UserID.count(keyword,
				UserID.ADMIN, null));
		users.setPageSize(length);
		render(users, keyword);
	}

	public static void get(Long id) {
		UserID user = UserID.findById(id);
		boolean usingEmail = UserID.usingEmail();
		render(user, usingEmail);
	}

	public static void create(String recognizer, String password, String name) {
		boolean usingEmail = UserID.usingEmail();
		if (isGET()) {
			render(recognizer, name, usingEmail);
		}

		try {
			UserID me = connected();
			UserID.create(recognizer, password, name, null, me, UserID.ADMIN);
		} catch (Faulty e) {
			validation.addError(e.ref, e.getMessage());
			validation.keep();
			render(recognizer, name, usingEmail);
		}
		index(null, 1, 10);
	}

	public static void update(Long id, String name, String password) {
		boolean usingEmail = UserID.usingEmail();
		UserID user = UserID.findById(id);
		if (isGET())
			render(user, usingEmail);

		try {
			UserID me = connected();
			if (StringUtils.isNotEmpty(name))
				user.name = name;
			if (StringUtils.isNotEmpty(password))
				user.encryptPassword(password);
			user.log.updatedBy(me);
			user.save();
		} catch (Faulty e) {
			validation.addError(e.ref, e.getMessage());
			validation.keep();
			render(user, usingEmail);
		}
		get(id);
	}

	public static void delete(Long id) {
		UserID me = connected();
		UserID user = UserID.findById(id);
		user.delete(me);
		index(null, 1, 10);
	}
}
