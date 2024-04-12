package ch.epfl.javelo.gui;

import java.awt.Toolkit;

import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Classe représentant le gestionnaire d'erreurs
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class ErrorManager {
    private static final String VBOX_STYLESHEET = "error.css";
    private static final double MIN_FADE = 0;
    private static final double MAX_FADE = 0.8;
    private static final int PAUSE_DURATION = 2000;
    private static final int FADE_IN_DURATION = 200;
    private static final int FADE_OUT_DURATION = 500;

    private final VBox vbox;
    private final SequentialTransition animation;
    private final Text text;

    /**
     * Construit un gestionnaire d'erreurs
     */
    public ErrorManager() {
        this.vbox = new VBox();
        this.text = new Text();
        this.animation = new SequentialTransition();

        setupStyle();
        setupAnimation();
    }

    /**
     * Méthode permettant d'obtenir le panneau d'affichage des erreurs
     * 
     * @return panneau d'affichage des erreurs
     */
    public Pane pane() {
        return vbox;
    }

    /**
     * Méthode permettant d'afficher un message d'erreur
     * 
     * @param error chaîne de caractères représentant le message d'erreur
     */
    public void displayError(String error) {
        text.setText(error);
        if (animation.getStatus() == Status.RUNNING)
            animation.stop();
        Toolkit.getDefaultToolkit().beep();
        animation.play();
    }

    // Méthodes de construction (utilisées seulement dans le constructeur)

    /**
     * Méthode permettant de mettre en place le style du gestionnaire
     */
    private void setupStyle() {
        vbox.getStylesheets().add(VBOX_STYLESHEET);
        vbox.setMouseTransparent(true);
        vbox.getChildren().add(text);
    }

    /**
     * Méthode permettant de mettre en place l'animation
     */
    private void setupAnimation() {
        animation.setNode(vbox);

        FadeTransition fadeIn = new FadeTransition(new Duration(FADE_IN_DURATION));
        fadeIn.setFromValue(MIN_FADE);
        fadeIn.setToValue(MAX_FADE);
        FadeTransition fadeOut = new FadeTransition(new Duration(FADE_OUT_DURATION));
        fadeOut.setFromValue(MAX_FADE);
        fadeOut.setToValue(MIN_FADE);

        animation.getChildren().add(fadeIn);
        animation.getChildren().add(new PauseTransition(new Duration(PAUSE_DURATION)));
        animation.getChildren().add(fadeOut);
    }
}