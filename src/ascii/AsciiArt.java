package ascii;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class AsciiArt implements Runnable {
	private static String chars1 = " .'`^\",:;Il!i><~+_-?][}{1)(|\\/tfjrxnuvczXYUJCLQ0OZmwqpdbkhao*#MW&8%B@$";
	private static String chars2 = " .:-=+*#%@";

	public boolean toFile = false;
	public boolean createImage = true;
	public boolean colorImage = true;
	public boolean detail = true;
	public int defaultWidth = 250;

	private List<List<MyColor>> pixels;

	private int threadCount = Runtime.getRuntime().availableProcessors();
	private int location;
	private int maxLocation;
	private BufferedImage img;
	private int multiplier = 7;
	private int fontSize = 10;
	private StringBuilder[] lines;

	public void load(String path) throws Exception {
		if (path == null) return;
		BufferedImage image = ImageIO.read(new File(path));
		int width = Math.min(defaultWidth, image.getWidth());
		parse(path, width, (int) (width * ((double)image.getHeight() / image.getWidth())));
	}

	public void load(String path, int width) throws Exception {
		if (path == null) return;
		BufferedImage image = ImageIO.read(new File(path));
		parse(path, width, (int) (width * ((double)image.getHeight() / image.getWidth())));
	}

	public void parse(String path, int resolutionX, int resolutionY) throws Exception {
		if (path == null) return;
		if (resolutionX < 1) resolutionX = 1;
		if (resolutionY < 1) resolutionY = 1;

		pixels = new ArrayList<>();
		for (int i = 0; i < resolutionY; i++) {
			pixels.add(new ArrayList<MyColor>());
		}

		BufferedImage image0 = ImageIO.read(new File(path));
		int width = image0.getWidth();
		int height = image0.getHeight();

		BufferedImage image = new BufferedImage(resolutionX, resolutionY, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(image0, 0, 0, resolutionX, resolutionY, 0, 0, width, height, null);
		g.dispose();

		divide(image, resolutionX, resolutionY);

		write(path);
	}

	private void divide(BufferedImage image, int resolutionX, int resolutionY) {
		int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		double max = pixels.length;
		for (int pixel = 0; pixel < max; pixel++) {
			this.pixels.get(pixel / resolutionX).add(new MyColor(pixels[pixel]));
		}
	}

	private void write(String path) throws Exception {
		String chars = chars1;
		if (!detail) chars = chars2;

		double step = Math.ceil(255.0 / chars.length());
		StringBuilder[] lines = new StringBuilder[pixels.size()];

		int maxSizeY = pixels.size();
		for (int y = 0; y < maxSizeY; y++) {
			lines[y] = new StringBuilder();
			int maxSizeX = pixels.get(y).size();
			for (int x = 0; x < maxSizeX; x++) {
				int position = (int) ((255 - pixels.get(y).get(x).getShade()) / step);
				lines[y].append(chars.charAt(position));
			}
		}

		if (toFile) {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path + ".txt")));
			for (int i = 0; i < lines.length; i++) {
				bw.write(lines[i].toString());
				bw.write("\r\n");
			}
			bw.close();
		}

		if (createImage) draw(path, lines);
	}

	private void draw(String path, StringBuilder[] lines) throws Exception {
		BufferedImage newImage = new BufferedImage(pixels.get(0).size() * multiplier,
				pixels.size() * multiplier, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) newImage.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, newImage.getWidth(), newImage.getHeight());
		g.dispose();
		
		this.location = 0;
		this.maxLocation = pixels.get(0).size() * pixels.size();
		this.img = newImage;
		this.lines = lines;
		
		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(this);
			threads[i].start();
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
		}
		ImageIO.write(newImage, "png", new File(path + ".png"));
	}

	@Override
	public void run() {
		int width = pixels.get(0).size();
		int multiplier = this.multiplier;
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.setFont(new Font("Courier New", Font.PLAIN, fontSize));
		g.setColor(Color.BLACK);
		
		while(true) {
			int location = getLocation();
			if (location < 0) {
				g.dispose();
				return;
			}
			
			int x = location % width;
			int y = location / width;
			
			if (colorImage) g.setColor(pixels.get(y).get(x).getColor());
			g.drawString("" + lines[y].charAt(x), x * multiplier, y * multiplier);
		}
	}

	private synchronized int getLocation() {
		if (location >= maxLocation) return -1;
		return location++;
	}
}
