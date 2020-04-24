package ru.shemplo.digits.gen;

import static javafx.scene.text.FontWeight.*;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.text.FontWeight;

public class RunDigitsGenerator {
    
    public static final File DIGITS_DIR = Paths.get ("digits").toFile ();
    public static final double RESOLUTION = 256.0;
    
    public static final List <String> FONT_FAMILIES = List.of (
        "Arial", "Calibri", "Bell MT", "Bodoni MT", "CASTELLAR",
        "Century", "Century Gothic"
    );
    
    public static final List <Double> FONT_SIZES = List.of (
        15.0, 17.5, 20.0
    );
    
    public static final List <FontWeight> FONT_WEIGHTS = List.of (
        NORMAL, BOLD, LIGHT, MEDIUM
    );
    
    public static void main (String ... args) {
        Application.launch (JavaFXApp.class, args);
    }
    
    public static void saveImage (Node node, int digit, int order) throws IOException {
        final var image = new WritableImage ((int) RESOLUTION, (int) RESOLUTION);
        node.snapshot (new SnapshotParameters (), image);
        
        final RenderedImage renderedImage = SwingFXUtils.fromFXImage (image, null);
        final var dir  = new File (DIGITS_DIR, String.valueOf (digit));
        final var file = new File (dir, order + ".png");
        if (!file.exists ()) { file.createNewFile (); }
        
        ImageIO.write (renderedImage, "png", file);
    }
    
}
