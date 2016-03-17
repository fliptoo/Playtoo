package controllers.Playtoo.web.office;

import models.Playtoo.Util;
import models.Playtoo.db.UserID;
import models.Playtoo.paginate.ListPaginator;
import controllers.Playtoo.BaseOffice;

public class UserWeb extends BaseOffice {

	public static void index(String keyword, int page, int length) {

		length = Util.length(length);
		ListPaginator<UserID> users = new ListPaginator(UserID.search(keyword,
				UserID.MEMBER, null, page, length), UserID.count(keyword,
				UserID.MEMBER, null));
		users.setPageSize(length);
		render(users, keyword);
	}

	public static void get(Long id) {
		UserID user = UserID.findById(id);
		render(user);
	}

	public static void delete(Long id) {
		UserID me = connected();
		UserID user = UserID.findById(id);
		user.delete(me);
		index(null, 1, 10);
	}
}
