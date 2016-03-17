package models.Playtoo.db;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;

import play.data.Upload;
import play.db.jpa.Model;
import play.mvc.Router;

@Entity
public class Media extends Model {

	public static enum Size {

		S(80), M(250), L(600), XL(1200), O(0);

		public final int value;

		Size(int value) {
			this.value = value;
		}

		public static Size of(String size) {
			if (size.equalsIgnoreCase(S.toString()))
				return S;
			else if (size.equalsIgnoreCase(M.toString()))
				return M;
			else if (size.equalsIgnoreCase(L.toString()))
				return L;
			else if (size.equalsIgnoreCase(XL.toString()))
				return XL;
			else
				return O;
		}
	}

	@Embeddable
	public class Variety {

		@Column(nullable = false)
		public Binary bin;

		public long filesize;

		Variety(Binary bin) {
			this.bin = bin;
			this.filesize = bin.length();
		}
	}

	@Column(nullable = false)
	public Binary bin;

	@Column(nullable = false)
	public String mimeType;

	@Column(nullable = false)
	public String filename;

	public long filesize;

	public boolean isAttached = true;

	public String folder;

	@ElementCollection
	@CollectionTable(name = "Media_Variety", joinColumns = @JoinColumn(name = "media_id"))
	@MapKeyColumn(name = "variety_key")
	public Map<String, Variety> varieties = new HashMap<String, Variety>();

	@Embedded
	public Log log;

	private Media(Upload file, UserID createdBy) {
		if (createdBy != null)
			this.folder = createdBy.id.toString();
		this.bin = new Binary(this.folder);
		this.bin.set(file.asStream(), file.getContentType());
		this.mimeType = this.bin.type().toLowerCase();
		this.filename = file.getFileName();
		this.filesize = file.getSize();
		this.log = new Log(createdBy);
	}

	private Media(Binary bin, String filename, String mimeType, UserID createdBy) {
		this.bin = bin;
		this.mimeType = mimeType.toLowerCase();
		this.filename = filename;
		this.filesize = bin.length();
		if (createdBy != null)
			this.folder = createdBy.id.toString();
		this.log = new Log(createdBy);
	}

	public Object asJson() {
		Map<String, Object> o = new HashMap<String, Object>();
		o.put("ID", id);
		return o;
	}

	public String imageURL(String size) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("size", size);
		map.put("id", this.id);
		return Router.reverse("MediaHub.image", map).url;
	}

	public Variety findVariety(String id) {
		return varieties.get(id.toUpperCase());
	}

	public Variety newVariety(String id, Binary bin) {
		Variety variety = new Variety(bin);
		varieties.put(id.toUpperCase(), variety);
		save();
		return variety;
	}

	public static Media create(Upload upload, UserID createdBy) {
		if (upload == null)
			return null;
		Media media = new Media(upload, createdBy);
		return media.save();
	}

	public static Media create(InputStream is, String filename,
			String mimeType, UserID createdBy) {
		String folder = null;
		if (createdBy != null)
			folder = createdBy.id.toString();
		Binary bin = new Binary(folder);
		bin.set(is, mimeType);
		Media media = new Media(bin, filename, mimeType, createdBy);
		return media.save();
	}

	public String url(String size) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("size", size);
		map.put("id", id);
		return Router.reverse("Playtoo.MediaHub.image", map).url;
	}

	public Media delete() {
		varieties.values();
		super.delete();
		if (this.bin.getFile() != null)
			this.bin.getFile().delete();
		for (Variety variety : varieties.values()) {
			if (variety.bin.getFile() != null)
				variety.bin.getFile().delete();
		}
		return this;
	}

}
