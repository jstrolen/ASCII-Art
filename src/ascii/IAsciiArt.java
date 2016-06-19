package ascii;

/**
 * Created by Josef Stroleny
 */
public interface IAsciiArt {
    void changeSettings(boolean toTextFile, boolean createImage, boolean colorImage, boolean detail, int maxWidth, int multiplier, int fontSize);

    void process(String path) throws Exception;
}
