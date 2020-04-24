package ru.shemplo.digits.gen;

import static ru.shemplo.digits.gen.RunDigitsGenerator.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class JavaFXApp extends Application {
    
    private ProgressBar progressBar;
    private VBox textBox;
    private Text text;
    
    @Override
    public void start (Stage stage) throws Exception {
        stage.setTitle ("Digits generator");
        stage.setScene (initView ());
        stage.setResizable (false);
        stage.sizeToScene ();
        stage.show ();
        
        generate ();
    }
    
    private Scene initView () {
        final var pane  = new BorderPane ();
        pane.setPadding (new Insets (8));
        
        textBox = new VBox ();
        textBox.setPrefHeight (RESOLUTION);
        textBox.setPrefWidth (RESOLUTION);
        textBox.setAlignment (Pos.CENTER);
        pane.setCenter (textBox);
        
        text = new Text ();
        textBox.getChildren ().add (text);
        
        progressBar = new ProgressBar (0.0);
        progressBar.prefWidthProperty ().bind (textBox.widthProperty ());
        BorderPane.setMargin (progressBar, new Insets (8, 0, 0, 0));
        pane.setBottom (progressBar);
        
        return new Scene (pane);
    }
    
    private void generate () throws IOException {
        final var variations = FONT_FAMILIES.size () 
                             * FONT_SIZES.size () 
                             * FONT_WEIGHTS.size () 
                             * 9.0; // 9 digits
        final var generated = new AtomicInteger ();
        
        new Thread (() -> {
            for (int i = 1; i < 10; i++) {
                for (var family : FONT_FAMILIES) {
                    for (var size : FONT_SIZES) {
                        for (var weight : FONT_WEIGHTS) {
                            final var digit = i;
                            Platform.runLater (() -> {                            
                                generateDidit (text, digit, family, size, weight);
                                
                                try {
                                    saveImage (textBox, digit, generated.get ());
                                } catch (IOException e) {}
                                
                                progressBar.setProgress (generated.incrementAndGet () / variations);
                            });
                            
                            try   { Thread.sleep (30); } 
                            catch (InterruptedException e) {}
                        }
                    }
                }
            }
        }).start ();
    }
    
    private void generateDidit (Text text2, int digit, String ffamily, double fsize, FontWeight weight) {
        fsize *= 10.0;
        
        final var font = Font.font (ffamily, weight, fsize);
        text.setText (String.valueOf (digit));
        text.setFont (font);
    }
    
}
