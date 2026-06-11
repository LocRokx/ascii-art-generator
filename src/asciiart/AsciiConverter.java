package asciiart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Converts a BufferedImage into ASCII art.
 *
 * Each pixel is reduced to a luminance value (ITU-R BT.601 weights:
 * 0.299 R + 0.587 G + 0.114 B) and mapped onto a ramp of characters from
 * visually dense ('@') to sparse (' '). Before conversion the image is
 * scaled down to a maximum character width, with the height additionally
 * halved to compensate for monospace characters being roughly twice as
 * tall as they are wide.
 */
public final class AsciiConverter {

    /** Characters from darkest pixel to brightest. */
    private static final char[] RAMP = {'@', '#', '8', '&', 'o', ':', '*', '.', ' '};

    /** Upper luminance bound (exclusive) for each ramp character except the last. */
    private static final double[] THRESHOLDS = {50, 70, 100, 130, 160, 180, 200, 230};

    /** Monospace glyphs are ~2x taller than wide; halve rows to keep proportions. */
    private static final double CHARACTER_ASPECT = 0.5;

    public static final int DEFAULT_MAX_WIDTH = 300;

    private final boolean negative;
    private final int maxWidth;

    public AsciiConverter() {
        this(false, DEFAULT_MAX_WIDTH);
    }

    /**
     * @param negative invert brightness, for dark-background viewing
     * @param maxWidth maximum number of characters per output line
     */
    public AsciiConverter(boolean negative, int maxWidth) {
        if (maxWidth < 1) {
            throw new IllegalArgumentException("maxWidth must be at least 1");
        }
        this.negative = negative;
        this.maxWidth = maxWidth;
    }

    public String convert(BufferedImage image) {
        BufferedImage scaled = scaleToFit(image);

        StringBuilder builder = new StringBuilder((scaled.getWidth() + 1) * scaled.getHeight());
        for (int y = 0; y < scaled.getHeight(); y++) {
            if (builder.length() != 0) {
                builder.append('\n');
            }
            for (int x = 0; x < scaled.getWidth(); x++) {
                Color pixel = new Color(scaled.getRGB(x, y));
                double luminance = 0.299 * pixel.getRed()
                                 + 0.587 * pixel.getGreen()
                                 + 0.114 * pixel.getBlue();
                builder.append(toCharacter(luminance));
            }
        }
        return builder.toString();
    }

    private char toCharacter(double luminance) {
        double value = negative ? 255.0 - luminance : luminance;
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (value < THRESHOLDS[i]) {
                return RAMP[i];
            }
        }
        return RAMP[RAMP.length - 1];
    }

    private BufferedImage scaleToFit(BufferedImage image) {
        int targetWidth = Math.min(maxWidth, image.getWidth());
        int targetHeight = Math.max(1, (int) Math.round(
                (double) image.getHeight() * targetWidth / image.getWidth() * CHARACTER_ASPECT));

        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setColor(Color.WHITE); // flatten any transparency onto white
        graphics.fillRect(0, 0, targetWidth, targetHeight);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                  RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return scaled;
    }
}
