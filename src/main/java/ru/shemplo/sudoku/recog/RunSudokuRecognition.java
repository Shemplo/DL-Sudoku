package ru.shemplo.sudoku.recog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.events.LearningEvent.Type;
import org.neuroph.core.learning.error.MeanAbsoluteError;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;

public class RunSudokuRecognition {
    
    private static final Random r = new Random (1L);
    
    private static final int INPUT = 9, OUTPUT = 9;
    private static final int VALIDATION = 10;
    private static final int EPOCHS = 1000;
    
    public static void main (String ... args) throws IOException {
        Locale.setDefault (Locale.ENGLISH);
        
        final var nn = logAction ("Creating network", () -> {
            final var network = new MultiLayerPerceptron (
                TransferFunctionType.SIGMOID,
                INPUT * INPUT * INPUT,
                //96 * 96,
                //108 * 108,
                //96 * 96,
                //84 * 84,
                //72 * 72,
                //48 * 48,
                //24 * 24,
                28 * 28,
                //6 * 6,
                //5 * 5,
                //5 * 5,
                OUTPUT * OUTPUT * OUTPUT
            );
            return network;
        });
        
        logAction ("Randomizing weights", () -> nn.randomizeWeights (r));
        
        final var dataset = logAction ("Loading dataset", () -> { 
            final var tmp = loadDataset (); 
            System.out.println ("    Loaded parts: " + tmp.getRows ().size ());
            tmp.shuffle (r);
            return tmp;
        });
        
        final var tests = logAction ("Creating validation set", () -> {
            final var tmp = IntStream.range (0, VALIDATION).mapToObj (__ -> dataset.remove (0))
                . collect (Collectors.toList ());
            dataset.shuffle (r);
            
            return tmp;
        });
        
        final var epochs = new AtomicInteger ();
        final var rule = logAction ("Creating learning rules", () -> {
            //final var tmp = new ConvolutionalBackpropagation ();
            final var tmp = new BackPropagation ();
            tmp.setErrorFunction (new MeanAbsoluteError ());
            //tmp.setErrorFunction (new RecognitionError ());
            tmp.setMaxIterations (EPOCHS);
            //tmp.setLearningRate (0.3);
            tmp.setBatchMode (true);
            //tmp.setMaxError (0.01);
            tmp.setLearningRate (0.05);
            
            
            tmp.addListener (le -> {
                if (le.getEventType () == Type.EPOCH_ENDED) {
                    if (epochs.incrementAndGet () % 1 == 0) {
                        final var error = tmp.getErrorFunction ().getTotalError ();
                        System.out.printf ("    %3d epochs over - Error: %.3f;", epochs.get (), error);
                        
                        nn.pauseLearning ();
                        validateNetwork (nn, tests, epochs.get () % 10 == 0);
                        nn.resumeLearning ();
                    }
                }
            });
            
            return tmp;
        });
        
        logAction ("Training network", () -> {
            nn.learn (dataset, rule);
        });
        
        //nn.save ("sudoku.nnet");
        
        logAction ("Validating network", () -> validateNetwork (nn, tests, true));
    }
    
    private static DataSet loadDataset () throws IOException {
        final var dataset = new DataSet (INPUT * INPUT * INPUT, OUTPUT * OUTPUT * OUTPUT);
        final var rootDir = new File ("sudokus");
        
        for (final var dir : rootDir.listFiles (File::isDirectory)) {
            readSudokuFolder (dataset, dir);
        }
        
        return dataset;
    }
    
    private static void readSudokuFolder (DataSet set, File directory) throws IOException {
        final var linesS = Files.readAllLines (new File (directory, "solution.txt").toPath ());
        final var lines = Files.readAllLines (new File (directory, "matrix.txt").toPath ());
        /*
        final var matrix = lines.stream ().map (line -> {
            return Arrays.stream (line.split ("\s+")).map (Integer::parseInt).mapToDouble (v -> 1.0 * v).toArray ();
        }).collect (Collectors.toList ());
        final var matrixS = linesS.stream ().map (line -> {
            return Arrays.stream (line.split ("\s+")).map (Integer::parseInt).mapToDouble (v -> 1.0 * v).toArray ();
        }).collect (Collectors.toList ());
        */
        
        final var matrix = lines.stream ().flatMap (line -> {
            return Arrays.stream (line.split ("\s+"));
        }).map (Integer::parseInt).flatMap (d -> Arrays.stream (digitToarray (d)).boxed ())
          .mapToDouble (v -> v).toArray ();
        
        final var matrixS = linesS.stream ().flatMap (line -> {
            return Arrays.stream (line.split ("\s+"));
        }).map (Integer::parseInt).flatMap (d -> Arrays.stream (digitToarray (d)).boxed ())
          .mapToDouble (v -> v).toArray ();
        
        set.add (new DataSetRow (matrix, matrixS));
        
        /*
        for (int i = 0; i < 9; i++) {
            try (
                final var is = new FileInputStream (new File (directory, "block-" + i + ".png"));
            ) {
                final var image = new Image (is);
                final var pr = image.getPixelReader ();
                
                final int w = (int) image.getWidth (), h = (int) image.getHeight ();
                final var buffer = new double [w * h];
                
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        final var color = pr.getColor (x, y);
                        buffer [y * w + x] = (color.getRed () + color.getGreen () + color.getBlue ()) / 3;
                    }
                }
                
                set.add (new DataSetRow (buffer, getBlockFromMatrix (matrix, i)));
            }
        }
        */
    }
    
    private static double [] digitToarray (int digit) {
        final var array = new double [9];
        if (digit > 0) {            
            array [digit - 1] = 1.0;
        }
        return array;
    }
    
    private static double arrayToDigit (double [] array, int offset) {
        for (int i = 0; i < 9; i++) {
            if (Math.round (array [offset * 9 + i]) == 1) {
                return i + 1;
            }
        }
        
        return 0.0;
    }
    
    private static double [] getBlockFromMatrix (List <List <Integer>> matrix, int index) {
        final var block = new double [OUTPUT * OUTPUT];
        
        final var xoffset = index % 3;
        final var yoffset = index / 3;
        
        for (int y = 0; y < OUTPUT; y++) {
            for (int x = 0; x < OUTPUT; x++) {
                block [y * 3 + x] = matrix.get (y + yoffset * 3).get (x + xoffset * 3).intValue () / 9.0;
            }
        }
        
        return block;
    }
    
    private static void validateNetwork (NeuralNetwork <?> nn, List <DataSetRow> tests, boolean verbose) {
        int total = 0, correct = 0;
        
        for (int i = 0; i < tests.size (); i++) {
            final var test = tests.get (i);
            
            nn.setInput (test.getInput ());
            nn.calculate ();
            
            if (verbose) {
                System.out.println ("    Test #" + i);
                System.out.println ("    Expected: " + Arrays.stream (test.getDesiredOutput ())
                    .mapToObj (v -> Math.round (v * 1)).collect (Collectors.toList ())
                );                
                System.out.println ("    Actual:   " + Arrays.stream (nn.getOutput ())
                    .mapToObj (v -> Math.round (v * 1)).collect (Collectors.toList ())
                );
            }
            
            for (int j = 0; j < OUTPUT * OUTPUT; j++) {
                //final var expected = Math.round (test.getDesiredOutput () [j] * 9);
                //final var actual = Math.round (nn.getOutput () [j] * 9);
                final var expected = arrayToDigit (test.getDesiredOutput (), j);
                final var actual = arrayToDigit (nn.getOutput (), j);
                
                if (Math.abs (expected - actual) < 1e-2) { correct++; }
                
                total++;
            }
            
        }
        
        System.out.println (String.format ("    Accuracy: %.2f", correct * 1.0 / total));
    }
    
    public static <E extends Exception> void logAction (String actionName, CheckedRunnable <E> action) throws E {
        System.out.println (actionName.concat ("..."));
        final var start = System.currentTimeMillis ();
        
        action.run ();
        
        final var end = System.currentTimeMillis ();
        System.out.println (String.format ("%s [%dms]", actionName, end - start));
    }
    
    public static <T, E extends Exception> T logAction (String actionName, CheckedSupplier <T, E> action) throws E {
        System.out.println (actionName.concat ("..."));
        final var start = System.currentTimeMillis ();
        
        final var result = action.supply ();
        
        final var end = System.currentTimeMillis ();
        System.out.println (String.format ("%s [%dms]", actionName, end - start));
        
        return result;
    }
    
}
