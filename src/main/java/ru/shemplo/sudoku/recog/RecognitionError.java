package ru.shemplo.sudoku.recog;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.neuroph.core.learning.error.ErrorFunction;

public class RecognitionError implements ErrorFunction, Serializable {
    
    private static final long serialVersionUID = 3803909934116969660L;
    
    private double totalError = 0;
    private int patterns = 0;
    
    @Override
    public void reset () {
        totalError = 0;
        patterns = 0;
    }
    
    private final Set <Long> repeated = new HashSet <> ();
    
    @Override
    public double [] addPatternError (double [] predictedOutput, double [] targetOutput) {
        final var patternError = new double [targetOutput.length];
        repeated.clear ();
        
        double repeatFactor = 1;
        for (int i = 0; i < targetOutput.length; i++) {
            final var actualDigit = Math.round (targetOutput [i] * 9);
            if (actualDigit > 0 && repeated.contains (actualDigit)) {
                repeatFactor += 0.25;
            }
            
            repeated.add (actualDigit);
        }
        
        for (int i = 0; i < targetOutput.length; i++) {
            final var expected = Math.ceil (predictedOutput [i]) / 10;
            final var actual = Math.ceil (targetOutput [i]) / 10;
            
            patternError [i] = Math.pow (Math.abs (expected - actual), 2);
            totalError += patternError [i];
        }
        
        patterns += 1;
        //System.out.println (patternError [1] + " / " + predictedOutput [1] + " / " + targetOutput [1]);
        //System.out.println (Arrays.toString (patternError));
        //System.out.println (Arrays.toString (predictedOutput));
        //System.out.println (Arrays.toString (targetOutput));
        //System.out.println ();
        return patternError;
    }

    @Override
    public double getTotalError () {
        return totalError / patterns;
    }
    
}
