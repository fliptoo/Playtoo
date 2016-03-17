package models.Playtoo.db;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonObject;

import models.Playtoo.Checker;
import models.Playtoo.Faulty;
import models.Playtoo.Util;
import play.Play;
import play.data.Upload;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.libs.Crypto;

@Entity
@Table(name = "User")
public class UserID extends Model {

	public final static String ADMIN = "Admin";
	public final static String MEMBER = "Member";

	public String recognizer;

	protected String password;

	@Column(unique = true)
	public Long fbid;

	@Lob
	public String fbToken;

	public String pwdToken;

	public String name;

	@ManyToOne
	public Media avatar;

	@Temporal(TemporalType.TIMESTAMP)
	public Date lastLogin;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "User_Role", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "role")
	public Set<String> roles = new HashSet<String>();

	@ManyToMany
	@JoinTable(name = "User_Platform", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "platform_id"))
	public List<Platform> platforms = new ArrayList<Platform>();

	public String status = Playtoo.ACTIVE;

	@Embedded
	public Log log;

	private static void validateEmail(String email, boolean checkDuplicated)
			throws Faulty {
		if (!UserID.usingEmail())
			return;
		if (StringUtils.isNotEmpty(email)) {
			if (!Validation.email("email", email).ok)
				Checker.Invalid(email);
			if (checkDuplicated && UserID.findByRecognizer(email) != null)
				Checker.Duplicated(email);
		}
	}

	private static UserID serialize(UserID user, JsonObject me, String token)
			throws Faulty, ParseException {
		if (user == null)
			user = new UserID();

		user.fbid = me.get("id").getAsLong();
		user.name = me.get("name").getAsString();

		if (UserID.usingEmail()) {
			if (StringUtils.isEmpty(user.recognizer)) {
				if (me.has("email") && !me.get("email").isJsonNull())
					user.recognizer = me.get("email").getAsString();
				else if (me.has("username") && !me.get("username").isJsonNull())
					user.recognizer = me.get("username").getAsString()
							+ "@facebook.com";
				else
					user.recognizer = user.fbid + "@facebook.com";
			}
		}
		return user;
	}

	public static UserID authenticate(String recognizer, String password)
			throws Faulty {
		validateEmail(recognizer, false);
		password = Crypto.passwordHash(password);
		return find("byRecognizerAndPassword", recognizer, password).first();
	}

	public static UserID create(String recognizer, String password,
			String name, Upload avatar, UserID createdBy, String... roles)
			throws Faulty {
		validateEmail(recognizer, true);
		UserID user = new UserID();
		user.recognizer = recognizer;
		user.encryptPassword(password);
		user.name = name;
		user.attachAvatar(avatar);
		user.log = new Log(createdBy);
		if (roles != null)
			for (String role : roles)
				user.roles.add(role);
		return user.save();
	}

	public static UserID findOrCreateFromFB(JsonObject me, String token)
			throws ParseException, Faulty {
		UserID user = findByFbid(me.get("id").getAsLong());
		if (user == null) {
			user = serialize(null, me, token);
			validateEmail(user.recognizer, true);
			user.save();
		}
		user.fbToken = token;
		return user.save();
	}

	public static UserID findByFbid(Long fbid) throws Faulty {
		return find("fbid", fbid).first();
	}

	public static UserID findByRecognizer(String recognizer) throws Faulty {
		return find("recognizer", recognizer).first();
	}

	public static List<UserID> search(String keyword, String role, UserID me,
			int page, int length) {
		length = Util.length(length);
		return query("SELECT o", keyword, role, me).fetch(page, length);
	}

	public static int count(String keyword, String role, UserID me) {
		return Integer.valueOf(query("SELECT count(*)", keyword, role, me)
				.first().toString());
	}

	private static JPAQuery query(String select, String keyword, String role,
			UserID me) {
		StringBuilder sql = new StringBuilder();
		sql.append(" FROM UserID o WHERE o.status = :status ");
		if (StringUtils.isNotEmpty(role))
			sql.append("AND :role IN ELEMENTS(o.roles) ");
		if (StringUtils.isNotEmpty(keyword)) {
			sql.append("AND ( ");
			sql.append("(UPPER(o.recognizer) LIKE :recognizer) OR ");
			sql.append("(UPPER(o.name) LIKE :name) ");
			sql.append(") ");
		}
		if (me != null)
			sql.append("AND o != :me ");

		JPAQuery query = UserID.find(select + sql.toString());
		query.bind("status", Playtoo.ACTIVE);
		if (StringUtils.isNotEmpty(role))
			query.bind("role", role);
		if (StringUtils.isNotEmpty(keyword)) {
			keyword = "%" + keyword.toUpperCase() + "%";
			query.bind("recognizer", keyword);
			query.bind("name", keyword);
		}
		if (me != null)
			query.bind("me", me);
		return query;
	}

	public void encryptPassword(String password) {
		this.password = Crypto.passwordHash(password);
	}

	public Platform findPlatform(String platform, String token) {
		for (Platform _platform : platforms) {
			if (_platform.type.equalsIgnoreCase(platform)
					&& _platform.token.equalsIgnoreCase(token)) {
				return _platform;
			}
		}
		return null;
	}

	public boolean passwordMatch(String _password) {
		return password.equals(Crypto.passwordHash(_password));
	}

	public UserID attachAvatar(Upload upload) {
		if (upload != null) {
			if (avatar != null) {
				Media o = avatar;
				avatar = null;
				save();
				o.delete();
			}
			avatar = Media.create(upload, this);
		}
		return save();
	}

	public UserID attachAvatar(Media upload) {
		if (upload != null) {
			if (avatar != null) {
				Media o = avatar;
				avatar = null;
				save();
				o.delete();
			}
			avatar = upload;
		}
		return save();
	}

	public UserID delete(UserID updatedBy) {
		this.status = Playtoo.INACTIVE;
		this.log.updatedBy(updatedBy);
		return save();
	}

	public static boolean usingEmail() {
		return Boolean.valueOf(Play.configuration
				.getProperty("playtoo.security.usingEmail"));
	}

}
