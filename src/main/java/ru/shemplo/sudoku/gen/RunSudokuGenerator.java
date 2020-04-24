package ru.shemplo.sudoku.gen;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import javafx.application.Application;
import lombok.Getter;
import ru.shemplo.snowball.stuctures.Pair;

@Getter
public class RunSudokuGenerator {
    
    public static final File SUDOKUS_DIR = Paths.get ("sudokus").toFile ();
    public static final double RESOLUTION = 9.0 * 64.0;
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
    
    public static Pair <int [][], int [][]> generateMatrix (Random random) {
        final var cpermutation = getPermutation (3, List.of (0, 3, 6), random); // columns permutation
        final var rpermutation = getPermutation (3, List.of (0, 3, 6), random); // rows permutation
        
        final var cbpermutation = getPermutation (3, 1, random); // column blocks permutation
        final var rbpermutation = getPermutation (3, 1, random); // row blocks permutation
        
        final var matrix = permuteMatrix (rpermutation, cpermutation, rbpermutation, cbpermutation);
        final var relaxedMatrix = relaxMatrix (matrix, 30, random);
        
        return Pair.mp (matrix, relaxedMatrix);
    }
    
    public static List <Integer> getSequence (int first, int length) {
        return IntStream.range (first, first + length).mapToObj (i -> i).collect (Collectors.toList ());
    }
    
    public static List <Integer> getListOf (int length, int value) {
        return IntStream.range (0, length).mapToObj (__ -> value).collect (Collectors.toList ());
    }
    
    public static List <Integer> getPermutation (int length, int blocks, Random random) {
        return getPermutation (length, getListOf (blocks, 0), random);
    }
    
    public static List <Integer> getPermutation (int length, List <Integer> blockOffsets, Random random) {
        return IntStream.range (0, blockOffsets.size ()).map (blockOffsets::get)
             . mapToObj (offset -> getSequence (offset, length))
             . peek (list -> Collections.shuffle (list, random))
             . flatMap (List::stream).collect (Collectors.toList ());
    }
    
    public static int [][] permuteMatrix (
        List <Integer> rperm,  List <Integer> cperm,
        List <Integer> rbperm, List <Integer> cbperm
    ) {
        final var matrix = new int [rperm.size ()][cperm.size ()];
        final var mod = matrix.length;
        
        for (int r = 0; r < matrix.length; r++) {
            final int rb = r / 3; // row block
            final int rr = rbperm.get (rb) * 3 + r % 3; // real row
            
            for (int c = 0; c < matrix [r].length; c++) {
                final int cb = c / 3; // column block
                final int rc = cbperm.get (cb) * 3 + c % 3; // real column
                
                //final var base = cperm.get (c) * 0 + c + rperm.get (r) * 3 + r / 3;
                //final var base = cperm.get (c) * 0 + c - rperm.get (r) + r / 3 + rperm.get (r) * 4;
                //final var base = cperm.get (c) + rperm.get (r) * 3 + r / 3;
                final var base = cperm.get (rc) + rperm.get (rr) * 3 + rbperm.get (rb);
                matrix [r][c] = ((base + mod) % mod) + 1;
            }
        }
        
        return matrix;
    }
    
    public static int [][] relaxMatrix (int [][] matrix, int difficulty, Random random) {
        final var relaxed = new int [matrix.length][];
        for (int i = 0; i < relaxed.length; i++) {
            relaxed [i] = Arrays.copyOf (matrix [i], matrix [i].length);
        }
        
        final var sequence = getSequence (0, matrix.length * matrix.length);
        Collections.shuffle (sequence, random);
        
        sequence.stream ().limit (difficulty).forEach (index -> {
            final var r = index / matrix.length;
            final var c = index % matrix.length;
            relaxed [r][c] = 0;
        });
        
        return relaxed;
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
    
    public static void saveSudoku (long seed, int [][] matrix, int [][] solution, RenderedImage full, List <RenderedImage> blocks) {
        final var dir = new File (SUDOKUS_DIR, String.valueOf (seed));
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
