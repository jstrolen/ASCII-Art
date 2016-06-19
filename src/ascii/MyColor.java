package ascii;

import java.awt.Color;

/**
 * Created by Josef Stroleny
 */
class MyColor {
	private final double gray;
	private final Color grayColor;
	private final Color rgb;
	
	MyColor(int pixel) {
		rgb = new Color(pixel);		
		gray = Settings.RED_CONST * ((pixel >> 16) & 0xff) + Settings.GREEN_CONST * ((pixel >> 8) & 0xff) + Settings.BLUE_CONST * (pixel & 0xff);
		grayColor = new Color((int) gray, (int) gray, (int) gray);
	}

	Color getColor() {
		return rgb;
	}

	Color getGray() {
		return grayColor;
	}

	double getShade() {
		return gray;
	}
}
