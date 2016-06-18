package GUI;

import ascii.AsciiArt;

import java.io.File;
import java.io.FilenameFilter;

public class Main {

	public static void main(String[] args) {
		AsciiArt ascii = new AsciiArt();
		
		ascii.detail = false;
		ascii.colorImage = true;
		ascii.defaultWidth = 600;
		
		try {
			final File dir = new File("C:\\");
			final String[] extensions = new String[]{"gif", "png", "jpg", "bmp", "jpeg"};	
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					for (String ext : extensions) {
						if (name.endsWith("." + ext)) {
							boolean ok = true;
							for (String ext2 : extensions) {
								if (name.endsWith("." + ext2 + ".png")) ok = false;
							}
							if (ok) return true;
						}
					}
					return false;
				}
			};
			
			if (dir.isDirectory()) {
				for (File f : dir.listFiles(filter)) {
					ascii.load(f.getAbsolutePath());
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
