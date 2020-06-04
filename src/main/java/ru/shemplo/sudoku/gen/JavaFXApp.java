package ru.shemplo.sudoku.gen;

import static ru.shemplo.sudoku.gen.RunSudokuGenerator.*;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ru.shemplo.digits.gen.RunDigitsGenerator;

public class JavaFXApp extends Application {
    
    private ProgressBar progressBar;
    private VBox canvasBox;
    private Canvas canvas;
    
    @Override
    public void start (Stage stage) throws Exception {
        stage.setTitle ("Sudoku generator");
        stage.setScene (initView ());
        stage.setResizable (false);
        stage.sizeToScene ();
        stage.show ();
        
        final var dir = RunDigitsGenerator.DIGITS_DIR;
        final var digit2files = getDigit2ImageFiles (dir);
        
        new Thread (() -> {
            final var matrices = getMatrices (SUDOKUS_DIR);
            final var digits = loadImages (digit2files);
            final var counter = new AtomicInteger ();
            
            for (var matrixNsolutionNfile : matrices) {
                Platform.runLater (() -> {                
                    generate (matrixNsolutionNfile.F, matrixNsolutionNfile.S, matrixNsolutionNfile.T, digits);
                    progressBar.setProgress (counter.incrementAndGet () * 1.0 / matrices.size ());
                });
                
                try   { Thread.sleep (300); } 
                catch (InterruptedException e) {}
            }
        }).start ();
    }
    
    private Scene initView () {
        final var pane  = new BorderPane ();
        pane.setPadding (new Insets (8));
        
        canvasBox = new VBox ();
        canvasBox.setPrefHeight (RESOLUTION);
        canvasBox.setPrefWidth (RESOLUTION);
        canvasBox.setAlignment (Pos.CENTER);
        pane.setCenter (canvasBox);
        
        canvas = new Canvas (RESOLUTION, RESOLUTION);
        canvasBox.getChildren ().add (canvas);
        
        progressBar = new ProgressBar (0.0);
        progressBar.prefWidthProperty ().bind (canvasBox.widthProperty ());
        BorderPane.setMargin (progressBar, new Insets (8, 0, 0, 0));
        pane.setBottom (progressBar);
        
        return new Scene (pane);
    }
    
    private Map <Integer, List <Image>> loadImages (Map <Integer, List <File>> digit2files) {
        final var map = new HashMap <Integer, List <Image>> ();
        digit2files.forEach ((digit, files) -> {
            final var images = files.stream ().map (file -> {
                    try (
                        final var is = new FileInputStream (file);
                    ) {
                        return new Image (is);
                    } catch (IOException ioe) {
                        return null; // just skip file
                    }
                })
                . filter (Objects::nonNull)
                . collect (Collectors.toList ());
            map.put (digit, images);
        });
        return map;
    }
    
    private void generate (int [][] matrix, int [][] solution, String filename, Map <Integer, List <Image>> digit2image) {
        final var random = getRandom ();
        
        final var ctx = canvas.getGraphicsContext2D ();
        final var square = RESOLUTION / 9;
        
        ctx.setFill (Color.WHITE);
        ctx.fillRect (0, 0, RESOLUTION, RESOLUTION);
        
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix [r].length; c++) {
                if (matrix [r][c] > 0) {                     
                    final var variations = digit2image.getOrDefault (matrix [r][c], List.of ());
                    if (variations.isEmpty ()) { throw new IllegalStateException (); }
                    
                    final var image = variations.get (random.nextInt (variations.size ()));
                    ctx.drawImage (image, c * square, r * square, square, square);
                }
                
                if (((r + c) & 1) == 0) {
                    //ctx.setFill (Color.rgb (240, 240, 250, 0.5));
                    //ctx.fillRect (c * square, r * square, square, square);
                }
            }
        }
        
        ctx.setStroke (Color.BLACK);
        
        ctx.strokeLine (0, 0, RESOLUTION, 0);
        ctx.strokeLine (0, 0, 0, RESOLUTION);
        ctx.strokeLine (RESOLUTION, 0, RESOLUTION, RESOLUTION);
        ctx.strokeLine (RESOLUTION, RESOLUTION, 0, RESOLUTION);
        
        ctx.strokeLine (RESOLUTION / 3, 0, RESOLUTION / 3, RESOLUTION);
        ctx.strokeLine (RESOLUTION * 2 / 3, 0, RESOLUTION * 2 / 3, RESOLUTION);
        
        ctx.strokeLine (RESOLUTION, RESOLUTION / 3, 0, RESOLUTION / 3);
        ctx.strokeLine (RESOLUTION, RESOLUTION * 2 / 3, 0, RESOLUTION * 2 / 3);
        
        final var image = new WritableImage ((int) RESOLUTION, (int) RESOLUTION);
        canvas.snapshot (new SnapshotParameters (), image);
        
        final var fullSudoku = SwingFXUtils.fromFXImage (image, null);
        final var blocks = getSequence (0, 9).stream ()
            . <RenderedImage> map (i -> {
                final var size = (int) square * 3;
                final int r = i / 3, c = i % 3;
                
                return fullSudoku.getSubimage (c * size, r * size, size, size);
            })
            . collect (Collectors.toList ());

        saveSudoku (filename, matrix, solution, fullSudoku, blocks);
    }
    
}
