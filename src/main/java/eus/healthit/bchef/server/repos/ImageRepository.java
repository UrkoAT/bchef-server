package eus.healthit.bchef.server.repos;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Hashtable;
import java.util.UUID;

import javax.imageio.ImageIO;

public class ImageRepository {

	public static String saveImage(String imageString) throws SQLException {
		if (imageString.equals("nochange")) {
			return "nochange";
		} else if (imageString.equals("default")) {
			return "default";
		}
		 Image image = decodeImage(imageString).getScaledInstance(500, 400, Image.SCALE_SMOOTH);
		 BufferedImage bImage      = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);

		Graphics2D bImageGraphics = bImage.createGraphics();

		bImageGraphics.drawImage(image, null, null);

		RenderedImage rImage      = (RenderedImage)bImage;
		File file = null;
		try {
			
			file = new File("main/resources/" + UUID.randomUUID().toString() + ".jpg");
			file.createNewFile();
			ImageIO.write(rImage, "jpg", file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file.getPath();
	}
	
	@SuppressWarnings("unchecked")
	public static BufferedImage convertRenderedImage(RenderedImage img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}
		ColorModel cm = img.getColorModel();
		int width = img.getWidth();
		int height = img.getHeight();
		WritableRaster raster = cm
				.createCompatibleWritableRaster(width, height);
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		@SuppressWarnings("rawtypes")
		Hashtable properties = new Hashtable();
		String[] keys = img.getPropertyNames();
		if (keys != null) {
			for (int i = 0; i < keys.length; i++) {
				properties.put(keys[i], img.getProperty(keys[i]));
			}
		}
		BufferedImage result = new BufferedImage(cm, raster,
				isAlphaPremultiplied, properties);
		img.copyData(raster);
		return result;
	}

	public static Image decodeImage(String codedImage) {
		try {
			byte[] bytes = Base64.getDecoder().decode(codedImage);
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			BufferedImage bImage2 = ImageIO.read(bis);
			return bImage2;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String encodeImage(String pathString) {
		try {
			 if (pathString.equals("default")) {
				return "default";
			}
			File file = new File(pathString);
			String string = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
			return string;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
