package GUI;

import ascii.AsciiArt;
import ascii.IAsciiArt;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by Josef Stroleny
 */
class Main {

	public static void main(String[] args) {
		IAsciiArt ascii = new AsciiArt(Settings.INIT_TO_TEXT_FILE, Settings.INIT_CREATE_IMAGE, Settings.INIT_COLOR_IMAGE, Settings.INIT_DETAIL, Settings.INIT_MAX_WIDTH, Settings.INIT_MULTIPLIER, Settings.INIT_FONT_SIZE);
		
		try {
			final File dir = new File(Settings.DEFAULT_PATH);

			//processed images filter
			final String[] extensions = Settings.DEFAULT_EXTENSIONS;
			FilenameFilter filter = (dir1, name) -> {
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
            };
			
			if (dir.isDirectory()) {
				for (File f : dir.listFiles(filter)) {
					ascii.process(f.getAbsolutePath());
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
