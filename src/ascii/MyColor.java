package ascii;

import java.awt.Color;


public class MyColor {
	private static double redConst = 0.299;
	private static double greenConst = 0.587;
	private static double blueConst = 0.114;
	
	private double gray;
	private Color grayColor;
	private Color rgb;
	
	public MyColor(int pixel) {
		rgb = new Color(pixel);		
		gray = (redConst * ((pixel >> 16) & 0xff) + greenConst * ((pixel >> 16) & 0xff) + blueConst * (pixel & 0xff)) / 3.0;
		grayColor = new Color((int) gray, (int) gray, (int) gray);
	}
	
	public Color getColor() {
		return rgb;
	}
	
	public Color getGray() {
		return grayColor;
	}

	public double getShade() {
		return gray;
	}
}
