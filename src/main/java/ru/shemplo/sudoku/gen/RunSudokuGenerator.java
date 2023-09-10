package ru.shemplo.sudoku.gen;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import javafx.application.Application;
import lombok.Getter;
import ru.shemplo.snowball.stuctures.Trio;

@Getter
public class RunSudokuGenerator {
    
    public static final File SUDOKUS_DIR = Paths.get ("sudokus").toFile ();
    public static final double RESOLUTION = 3.0 * 64.0;
    public static final int SIZE = 9;
    
    @Getter
    private static long seed;
    
    @Getter
    private static Random random;
    
    public static void main (String ... args) {
        seed = System.currentTimeMillis ();
        if (args.length > 0) {
            System.out.println ("Seed is defined: " + args [0]);
            //seed = args [0].hashCode ();
        }
        
        System.out.println ("Seed value: " + seed);
        random = new Random (seed);
        
        Application.launch (JavaFXApp.class);
    }
    
    public static List <Integer> getSequence (int first, int length) {
        return IntStream.range (first, first + length).mapToObj (i -> i).collect (Collectors.toList ());
    }
    
    public static List <Trio <int [][], int [][], String>> getMatrices (File rootDir) {
        if (!rootDir.exists () || !rootDir.isDirectory ()) {
            throw new IllegalStateException ();
        }
        
        return Arrays.stream (rootDir.listFiles (f -> f.getName ().endsWith (".txt"))).parallel ()
             . map (file -> {
                 try {
                     final var lines = Files.readAllLines (file.toPath ());
                     if (lines.size () < SIZE * 2 + 1) { return null; }
                     
                     int [][] matrix = new int [SIZE][SIZE], 
                            solution = new int [SIZE][SIZE];
                     for (int r = 0; r < SIZE; r++) {
                         String [] matrixLine = lines.get (r + SIZE + 1).split ("\\s+");
                         String [] solutionLine = lines.get (r).split ("\\s+");
                         
                         for (int c = 0; c < SIZE; c++) {
                             matrix [r][c] = matrixLine [c].equals ("_") ? 0 : Integer.parseInt (matrixLine [c]);
                             solution [r][c] = Integer.parseInt (solutionLine [c]);
                         }
                     }
                     
                     System.out.println ("File " + file.getName () + " parsed and added to render queue");
                     return Trio.mt (matrix, solution, file.getName ().replaceFirst ("\\.txt$", ""));
                 } catch (IOException ioe) {
                     return null;
                 }
             })
             . filter (Objects::nonNull)
             . collect (Collectors.toList ());
    }
    
    public static Map <Integer, List <File>> getDigit2ImageFiles (File rootDir) {
        if (!rootDir.exists () || !rootDir.isDirectory ()) {
            throw new IllegalStateException ();
        }
        
        return Arrays.stream (rootDir.listFiles ())
             . collect (Collectors.toMap (
                 file -> {
                     try {
                         return Integer.parseInt (file.getName ());
                     } catch (NumberFormatException nfe) {
                         return -1;
                     }
                 }, 
                 file -> {
                     if (!file.isDirectory ()) { return List.of (); }
                     return Arrays.asList (file.listFiles (f -> f.getName ().endsWith (".png")));
                 }
             ));
    }
    
    public static void saveSudoku (
        String filename, int [][] matrix, int [][] solution, 
        RenderedImage full, List <RenderedImage> blocks
    ) {
        final var dir = new File (SUDOKUS_DIR, filename);
        dir.mkdirs ();
        
        final var solutionFile = new File (dir, "solution.txt").toPath ();
        final var matrixFile = new File (dir, "matrix.txt").toPath ();
        final var fullImgFile = new File (dir, "full.png");
        
        try (
            final var mBW = Files.newBufferedWriter (matrixFile);
            final var mPW = new PrintWriter (mBW);
                
            final var sBW = Files.newBufferedWriter (solutionFile);
            final var sPW = new PrintWriter (sBW);
        ) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix [i].length; j++) {
                    sPW.print (solution [i][j]); sPW.print (" ");
                    mPW.print (matrix [i][j]); mPW.print (" ");
                }
                
                sPW.println (); mPW.println ();
            }
            
            ImageIO.write (full, "png", fullImgFile);
            
            for (int i = 0; i < blocks.size (); i++) {
                final var file = new File (dir, "block-" + i + ".png");
                ImageIO.write (blocks.get (i), "png", file);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace ();
        }
    }
    
}
