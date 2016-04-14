package controllers.Playtoo.api;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import controllers.Playtoo.BaseApi;
import controllers.Playtoo.Mails;
import models.Playtoo.Checker;
import models.Playtoo.Faulty;
import models.Playtoo.Secured;
import models.Playtoo.db.Media;
import models.Playtoo.db.Platform;
import models.Playtoo.db.UserID;
import models.Playtoo.json.JModel;
import models.Playtoo.json.JUser;
import play.data.Upload;
import play.mvc.Router;

@Secured
public class UserApi extends BaseApi {

	@Secured(false)
	public static void register(String recognizer, String password, String name,
			Upload avatar) throws Faulty {
		Checker.Required(recognizer);
		Checker.Required(password);

		UserID user = UserID.create(recognizer, password, name, avatar, null,
				UserID.MEMBER);

		Map<String, Object> o = new HashMap<String, Object>();
		o.put("user", JModel.from(user, JUser.class));
		renderJSON(o);
	}

	public static void touch(String platform, String token, String version,
			String device) throws Faulty {
		Checker.Required(token);

		if (StringUtils.isEmpty(platform))
			platform = Platform.IOS;

		if (!(platform.equalsIgnoreCase(Platform.IOS)
				|| platform.equalsIgnoreCase(Platform.ANDROID)))
			Checker.Invalid(platform);

		UserID me = connected();
		Platform ePlatform = Platform.find("byDevice", device).first();
		if (ePlatform != null && ePlatform.user != me) {
			ePlatform.user.platforms.remove(ePlatform);
			ePlatform.user.save();
			ePlatform.delete();
			ePlatform = null;
		}

		if (ePlatform == null)
			me.platforms
					.add(Platform.create(me, platform, token, device, version));
		else {
			if (StringUtils.isNotEmpty(version))
				ePlatform.version = version;
			ePlatform.token = token;
			ePlatform.lastUsed = new Date();
			ePlatform.save();
		}
		me.save();
		OK();
	}

	@Secured(false)
	public static void find(Long id) throws Faulty {
		Checker.Required(id);
		UserID user = UserID.findById(id);
		Checker.NotFound(user);

		UserID me = connected();
		Map<String, Object> o = new HashMap<String, Object>();
		o.put("user", JModel.from(me, JUser.class));
		renderJSON(o);
	}

	public static void update(String name, Long avatar, String oldPassword,
			String newPassword) {
		UserID me = connected();

		if (StringUtils.isNotEmpty(oldPassword)
				&& StringUtils.isNotEmpty(newPassword)) {
			if (me.passwordMatch(oldPassword)) {
				me.encryptPassword(newPassword);
			} else {
				Checker.Invalid(oldPassword);
			}
		}

		if (StringUtils.isEmpty(oldPassword)
				&& StringUtils.isNotEmpty(newPassword)) {
			me.encryptPassword(newPassword);
		}

		if (StringUtils.isNotEmpty(name))
			me.name = name;
		if (avatar != null)
			me.attachAvatar((Media) Media.findById(avatar));
		me.save();

		Map<String, Object> o = new HashMap<String, Object>();
		o.put("user", JModel.from(me, JUser.class));
		renderJSON(o);
	}

	@Secured(false)
	public static void forgotPassword(String recognizer) throws Faulty {
		Checker.Required(recognizer);
		UserID user = UserID.findByRecognizer(recognizer);
		Checker.NotFound(user);

		user.pwdToken = UUID.randomUUID().toString();
		user.save();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("token", user.pwdToken);
		map.put("recognizer", user.recognizer);
		String resetURL = "http://" + request.host
				+ Router.reverse("Playtoo.web.ResetPasswordWeb.index", map).url;
		Mails.forgotPassword(user, resetURL);
		OK();
	}

	public static void search(String keyword, int page, int length)
			throws Faulty {
		Checker.Required(keyword);
		List<UserID> users = UserID.search(keyword, UserID.MEMBER, connected(),
				page, length);
		Map<String, Object> o = new HashMap<String, Object>();
		o.put("users", JModel.fromList(users, JUser.class));
		renderJSON(o);
	}

	public static void changePassword(String oldPassword, String newPassword)
			throws Faulty {
		UserID me = connected();
		if (me.passwordMatch(oldPassword)) {
			me.encryptPassword(newPassword);
			me.save();
			OK();
		} else {
			Checker.Invalid(oldPassword);
		}
	}

}
