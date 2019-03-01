package edu.itu.the_d.map.utils;

import edu.itu.the_d.map.view.RetinaIcon;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;


/**
 * Class for loading images easily. This class supports resizing images as well as loading retina images.
 */
public final class ImageLoader implements Serializable {
	public static final long serialVersionUID = 1231;
	//Resource path as string
	private static final String resources = "resources/";
	private static HashMap<String, BufferedImage> imageMap = new HashMap<>();

	/**
	 * Load image from filename from resource folder
	 *
	 * @param filename as string, including filetype as well
	 * @return a buffered image.
	 */
	public static BufferedImage loadImage(String filename) {
		try {
			return ImageIO.read(ResourceLoader.load(resources + filename));
		} catch (IOException | IllegalArgumentException e) {
			Debugger.print(User.GLOBAL, "ImageLoader: File not found: " + filename);
		}
		return createErrorImage();
	}

	/**
	 * Load image from filename from resource folder and resize it.
	 *
	 * @param filename as string, including filetype as well
	 * @param size     as dimension
	 * @param retina   as boolean. Set to true to double the image size
	 * @return a buffered image.
	 */
	public static BufferedImage loadImage(String filename, Dimension size, boolean retina) {
		if (RetinaIcon.isRetina() && retina) return loadImage(filename, new Dimension(size.width * 2, size.height * 2));
		return loadImage(filename, size);
	}


	/**
	 * Load image from filename from resource folder and resize it.
	 *
	 * @param filename as string, including filetype as well
	 * @param size     as dimension
	 * @param retina   as boolean. Set to true to double the image size
	 * @return a buffered image.
	 */
	public static BufferedImage loadImage(String filename, int size, boolean retina) {
		if (RetinaIcon.isRetina() && retina) return loadImage(filename, size * 2);
		return loadImage(filename, size);
	}


	/**
	 * Load image from filename from resource folder and resize it.
	 *
	 * @param filename as string, including filetype as well
	 * @param size     as dimension
	 * @return a buffered image
	 */
	public static BufferedImage loadImage(String filename, Dimension size) {
		if (imageMap.containsKey(filename)) return imageMap.get(filename);
		BufferedImage tmp = Scalr.resize(loadImage(filename), size.width, size.height);
		imageMap.put(filename, tmp);
		return tmp;
	}


	/**
	 * Load image from filename from resource folder and resize it.
	 *
	 * @param filename as string, including filetype as well
	 * @param size     as dimension
	 * @return a buffered image
	 */
	public static BufferedImage loadImage(String filename, int size) {
		if (imageMap.containsKey(filename)) return imageMap.get(filename);
		BufferedImage tmp = Scalr.resize(loadImage(filename), size, size);
		imageMap.put(filename, tmp);
		return tmp;
	}


	/**
	 * Create a error image that contains a questionmark to display in case the loadImage() method fails to load the file.
	 * Code fragments taken from: https://stackoverflow.com/questions/18800717/convert-text-content-to-image
	 *
	 * @return a buffered image
	 */
	private static BufferedImage createErrorImage() {
		String text = "?";

		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		Font font = new Font("Arial", Font.PLAIN, 48);
		g2d.setFont(font);
		FontMetrics fm = g2d.getFontMetrics();
		int width = fm.stringWidth(text);
		int height = fm.getHeight();
		g2d.dispose();

		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2d.setFont(font);
		fm = g2d.getFontMetrics();
		g2d.setColor(Color.BLACK);
		g2d.drawString(text, 0, fm.getAscent());
		g2d.dispose();
		return img;
	}


}