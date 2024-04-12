package ch.epfl.javelo;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

/**
 * Classe permettant de créer des objets représentants des fonctions mathematiques
 *
 * @author Florian COMTE (346006)
 * @author Marc FARHAT (325811)
 */
public final class Functions {
    /**
     * Constructeur privé
     */
    private Functions() {
    }

    /**
     * Méthode permettant d'obtenir une fonction constante
     * 
     * @param y valeur constante de la fonction
     * 
     * @return une fonction constante de valeur y
     */
    public static DoubleUnaryOperator constant(double y) {
        return operand -> y;
    }

    /**
     * Méthode permettant d'obtenir une fonction par interpolation linéaire
     * 
     * @param samples échantillons de coordonnée y de points
     * @param xMax    coordonnée x maximum
     * 
     * @throws IllegalArgumentException si le tableau d'échantillons contient moins de 2 éléments ou
     *                                  si xMax est inférieur ou égal à 0
     * 
     * @return une fonction par interpolation linéaire
     */
    public static DoubleUnaryOperator sampled(float[] samples, double xMax) {
        Preconditions.checkArgument(samples.length >= 2 && xMax > 0);
        float[] samplesArray = Arrays.copyOf(samples, samples.length);
        return operand -> {
            if (operand >= xMax)
                return samples[samplesArray.length - 1];

            if (operand <= 0)
                return samples[0];

            double interval = xMax / (samplesArray.length - 1);
            // index de la première valeur de y (y0)
            int yIndex = (int) (operand / interval);

            return Math2.interpolate(samplesArray[yIndex], samplesArray[yIndex + 1],
                    operand / interval - yIndex);
        };
    }
}