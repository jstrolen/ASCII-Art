package ascii;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by Josef Stroleny
 */
public class AsciiArt implements IAsciiArt, Runnable {
	private boolean toTextFile;
	private boolean createImage;
	private boolean colorImage;
	private boolean detail;
	private int maxWidth;
	private int multiplier;
	private int fontSize;
	private MyColor[][] pixels;
	private char[][] lines;

	private final int threadCount = Runtime.getRuntime().availableProcessors();
	private int location;
	private int maxLocation;
	private BufferedImage buffImage;

	public AsciiArt(boolean toTextFile, boolean createImage, boolean colorImage, boolean detail, int maxWidth, int multiplier, int fontSize) {
		changeSettings(toTextFile, createImage, colorImage, detail, maxWidth, multiplier, fontSize);
	}

	@Override
	public void changeSettings(boolean toTextFile, boolean createImage, boolean colorImage, boolean detail, int maxWidth, int multiplier, int fontSize) {
		this.toTextFile = toTextFile;
		this.createImage = createImage;
		this.colorImage = colorImage;
		this.detail = detail;
		this.maxWidth = maxWidth;
		this.multiplier = multiplier;
		this.fontSize = fontSize;
	}

	@Override
	public void process(String path) throws Exception {
		if (path == null) return;
		BufferedImage image = ImageIO.read(new File(path));
		int width = Math.min(maxWidth, image.getWidth());
		parse(path, width, (int) (width * ((double) image.getHeight() / image.getWidth())));
	}

	private void parse(String path, int resolutionX, int resolutionY) throws Exception {
		if (resolutionX < 1 || resolutionY < 1) return;
		pixels = new MyColor[resolutionY][resolutionX];
		lines = new char[resolutionY][resolutionX];

		BufferedImage image = resize(ImageIO.read(new File(path)), resolutionX, resolutionY);
		getPixels(image);
		getChars();
		write(path);
	}

	private BufferedImage resize(BufferedImage originalImage, int resolutionX, int resolutionY) {
		BufferedImage image = new BufferedImage(resolutionX, resolutionY, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(originalImage, 0, 0, resolutionX, resolutionY, null);
		g.dispose();

		return image;
	}

	private void getPixels(BufferedImage image) {
		int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		for (int pixel = 0; pixel < pixels.length; pixel++) {
			this.pixels[pixel / image.getWidth()][pixel % image.getWidth()] = new MyColor(pixels[pixel]);
		}
	}

	private void getChars() {
		String chars = Settings.CHARS_1;
		if (!detail) chars = Settings.CHARS_2;

		double step = Math.ceil(255.0 / chars.length());
		int maxSizeY = pixels.length;
		for (int y = 0; y < maxSizeY; y++) {
			for (int x = 0; x < pixels[y].length; x++) {
				int position = (int) ((255 - pixels[y][x].getShade()) / step);
				lines[y][x] = chars.charAt(position);
			}
		}
	}

	private void write(String path) throws Exception {
		if (toTextFile) {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path + ".txt")));
			for (int i = 0; i < lines.length; i++) {
				bw.write(String.valueOf(lines[i]));
				bw.write("\r\n");
			}
			bw.close();
		}

		if (createImage) draw(path);
	}

	private void draw(String path) throws Exception {
		BufferedImage newImage = new BufferedImage(pixels[0].length * multiplier,
				pixels.length * multiplier, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) newImage.getGraphics();
		g.setColor(Settings.BG_COLOR);
		g.fillRect(0, 0, newImage.getWidth(), newImage.getHeight());
		g.dispose();
		
		this.location = 0;
		this.maxLocation = pixels[0].length * pixels.length;
		this.buffImage = newImage;
		
		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(this);
			threads[i].start();
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
		}
		ImageIO.write(newImage, Settings.EXTENSION, new File(path + "." + Settings.EXTENSION));
	}

	@Override
	public void run() {
		int width = pixels[0].length;
		Graphics2D g = (Graphics2D) buffImage.getGraphics();
		g.setFont(new Font(Settings.FONT_NAME, Settings.FONT_STYLE, fontSize));
		g.setColor(Color.BLACK);
		
		while(true) {
			int location = getLocation();
			if (location < 0) {
				g.dispose();
				return;
			}
			
			int x = location % width;
			int y = location / width;
			
			if (colorImage) g.setColor(pixels[y][x].getColor());
			g.drawString("" + lines[y][x], x * multiplier, y * multiplier);
		}
	}

	private synchronized int getLocation() {
		if (location >= maxLocation) return -1;
		return location++;
	}
}
