package controllers.Playtoo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import models.Playtoo.Checker;
import models.Playtoo.Faulty;
import models.Playtoo.Images;
import models.Playtoo.Secured;
import models.Playtoo.db.Binary;
import models.Playtoo.db.Media;
import models.Playtoo.db.Media.Size;
import models.Playtoo.db.Media.Variety;
import models.Playtoo.db.UserID;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.data.Upload;

public class MediaHub extends Controller {

	private static void renderImage(Media media, String size)
			throws IOException {

		if (media != null && media.bin != null && media.bin.getFile() != null) {
			File image = media.bin.getFile();
			if (!StringUtils.isEmpty(size)
					&& !size.equalsIgnoreCase(Size.O.toString())) {

				int v;
				if (StringUtils.isNumeric(size))
					v = Integer.parseInt(size);
				else
					v = Size.of(size).value;
				Variety variety = media.findVariety(size);

				if (variety == null && v > 0) {
					File rImage = File.createTempFile("resize", media.filename);
					Images.resize(image, rImage, v, v, true);
					Binary bin = new Binary(media.folder);
					bin.set(new FileInputStream(rImage), media.mimeType);
					variety = media.newVariety(size, bin);
				}
				image = variety.bin.getFile();
			}
			renderBinary(new FileInputStream(image), media.filename,
					media.mimeType, true);
		} else {
			File image = Play.getFile("/resources/images/avatar.png");
			renderBinary(new FileInputStream(image), "placeholder.png",
					"image/png", true);
		}
	}

	@Secured
	public static void upload(Upload data) {
		request.args.put(REQ_TYPE, API);
		request.format = "json";
		Media media = Media.create(data, connected());
		media.save();
		renderJSON(media.asJson());
	}

	public static void download(long id) throws FileNotFoundException, Faulty {
		Media media = Media.findById(id);
		Checker.NotFound(media);
		renderBinary(new FileInputStream(media.bin.getFile()), media.filename,
				media.mimeType, true);
	}

	public static void image(Long id, String size) throws IOException, Faulty {
		Checker.Required(id);
		Media media = Media.findById(id);
		Checker.NotFound(media);
		renderImage(media, size);
	}

	public static void user(Long id, String size) throws Faulty, IOException {
		Checker.Required(id);
		UserID user = UserID.findById(id);
		Checker.NotFound(user);
		renderImage(user.avatar, size);
	}
}
