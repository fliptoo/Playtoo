package models.Playtoo.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;

import play.Play;
import play.db.Model.BinaryField;
import play.exceptions.UnexpectedException;
import play.libs.Codec;
import play.libs.IO;

public class Binary implements BinaryField, UserType {

	private String UUID;
	private String type;
	private String folder;
	private File file;

	public Binary(String folder) {
		this.folder = folder;
	}

	private Binary(String UUID, String type, String folder) {
		this.UUID = UUID;
		this.type = type;
		this.folder = folder;
	}

	public InputStream get() {
		if (exists()) {
			try {
				return new FileInputStream(getFile());
			} catch (Exception e) {
				throw new UnexpectedException(e);
			}
		}
		return null;
	}

	public void set(InputStream is, String type) {
		this.UUID = Codec.UUID();
		this.type = type;
		IO.write(is, getFile());
	}

	public long length() {
		return getFile().length();
	}

	public String type() {
		return type;
	}

	public boolean exists() {
		return UUID != null && getFile().exists();
	}

	public File getFile() {
		if (file == null) {
			file = new File(getStore(folder), UUID);
		}
		return file;
	}

	public String getUUID() {
		return UUID;
	}

	public int[] sqlTypes() {
		return new int[] { Types.VARCHAR };
	}

	public Class returnedClass() {
		return Binary.class;
	}

	private static boolean equal(Object a, Object b) {
		return a == b || (a != null && a.equals(b));
	}

	public boolean equals(Object o, Object o1) throws HibernateException {
		if (o instanceof Binary && o1 instanceof Binary) {
			return equal(((Binary) o).UUID, ((Binary) o1).UUID)
					&& equal(((Binary) o).type, ((Binary) o1).type);
		}
		return equal(o, o1);
	}

	public int hashCode(Object o) throws HibernateException {
		return o.hashCode();
	}

	@SuppressWarnings("deprecation")
	public Object nullSafeGet(ResultSet resultSet, String[] names, Object o)
			throws HibernateException, SQLException {
		String val = (String) StringType.INSTANCE.get(resultSet, names[0]);

		if (val == null || val.length() == 0 || !val.contains("|")) {
			return new Binary(null);
		}

		String vals[] = val.split("[|]");
		String UUID = vals[0];
		String type = vals[1];
		String folder = null;
		if (vals.length > 2)
			folder = vals[2];
		return new Binary(UUID, type, folder);
	}

	public void nullSafeSet(PreparedStatement ps, Object o, int i)
			throws HibernateException, SQLException {
		if (o != null) {
			Binary bin = (Binary) o;
			ps.setString(i, bin.UUID + "|" + bin.type + "|" + bin.folder);
		} else {
			ps.setNull(i, Types.VARCHAR);
		}
	}

	public Object deepCopy(Object o) throws HibernateException {
		if (o == null) {
			return null;
		}
		return new Binary(((Binary) o).UUID, ((Binary) o).type,
				((Binary) o).folder);
	}

	public boolean isMutable() {
		return true;
	}

	public Serializable disassemble(Object o) throws HibernateException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Object assemble(Serializable srlzbl, Object o)
			throws HibernateException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Object replace(Object o, Object o1, Object o2)
			throws HibernateException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public static String getUUID(String dbValue) {
		return dbValue.split("[|]")[0];
	}

	public static File getStore(String folder) {
		if (StringUtils.isEmpty(folder))
			folder = "attachments";
		else {
			String[] tokenizer = folder.split("");
			folder = "";
			for (String token : tokenizer) {
				if (token.trim().length() > 0) {
					folder = folder + File.separator + token;
				}
			}
		}
		String name = Play.configuration.getProperty("attachments.path",
				"attachments") + File.separator + folder;
		File store = null;
		if (new File(name).isAbsolute()) {
			store = new File(name);
		} else {
			store = Play.getFile(name);
		}
		if (!store.exists()) {
			store.mkdirs();
		}
		return store;
	}
}
