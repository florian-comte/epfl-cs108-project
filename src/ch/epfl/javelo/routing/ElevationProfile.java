package ch.epfl.javelo.routing;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.function.DoubleUnaryOperator;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.Preconditions;
import javafx.util.Pair;

/**
 * Classe représentant le profil le long d'un itinéraire simple ou multiple
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class ElevationProfile {
    private final double length;
    private final float[] elevationSamples;
    private final double minElevation;
    private final double maxElevation;
    private final double ascent;
    private final double descent;
    private final DoubleUnaryOperator function;

    /**
     * Construit un profil le long d'un itinéraire
     * 
     * @param length           longueur de l'itinéraire
     * @param elevationSamples échantillons d'altitudes
     * 
     * @throws IllegalArgumentException si la longueur est négative ou nulle, ou si le tableau
     *                                  d'échantillons contient moins de 2 éléments
     */
    public ElevationProfile(double length, float[] elevationSamples) {
        Preconditions.checkArgument(length > 0 && elevationSamples.length >= 2);
        this.length = length;
        this.elevationSamples = Arrays.copyOf(elevationSamples, elevationSamples.length);
        Pair<Double, Double> descentAscent = constructDescentAscent();
        this.ascent = descentAscent.getValue();
        this.descent = descentAscent.getKey();
        this.function = Functions.sampled(elevationSamples, length);

        DoubleSummaryStatistics stats = constructStats();
        this.minElevation = stats.getMin();
        this.maxElevation = stats.getMax();
    }

    /**
     * Méthode permettant d'obtenir la longueur d'un profil
     * 
     * @return longueur du profil en mètres
     */
    public double length() {
        return length;
    }

    /**
     * Méthode permettant d'obtenir l'altitude minimum d'un profil
     * 
     * @return l'altitude minimum du profil en mètres
     */
    public double minElevation() {
        return minElevation;
    }

    /**
     * Méthode permettant d'obtenir l'altitude maximum d'un profil
     * 
     * @return l'altitude maximum du profil en mètres
     */
    public double maxElevation() {
        return maxElevation;
    }

    /**
     * Méthode permettant d'obtenir le dénivelé positif total d'un profil
     * 
     * @return le dénivelé positif total du profil en mètres
     */
    public double totalAscent() {
        return ascent;
    }

    /**
     * Méthode permettant d'obtenir le dénivelé négatif total d'un profil
     * 
     * @return le dénivelé négatif total du profil en mètres
     */
    public double totalDescent() {
        return descent;
    }

    /**
     * Méthode permettant d'obtenir l'altitude d'un profil à une position donnée,
     * 
     * @param position position souhaitée
     * 
     * @return l'altitude du profil à la position donnée (le premier échantillon est retourné
     *         lorsque la position est négative, le dernier lorsqu'elle est supérieure à la
     *         longueur)
     */
    public double elevationAt(double position) {
        return function.applyAsDouble(position);
    }

    // Méthodes de construction (utilisées seulement dans le constructeur)

    /**
     * Méthode permettant de "construire" le dénivelé négatif et positif total d'un profil
     * 
     * @return une paire contenant d'abord le dénivelé négatif total puis le dénivelé positif total
     */
    private Pair<Double, Double> constructDescentAscent() {
        double totalDescent = 0;
        double totalAscent = 0;
        for (int i = 0; i < elevationSamples.length - 1; i++) {
            if (elevationSamples[i + 1] - elevationSamples[i] < 0)
                totalDescent -= elevationSamples[i + 1] - elevationSamples[i];
            if (elevationSamples[i + 1] - elevationSamples[i] > 0)
                totalAscent += elevationSamples[i + 1] - elevationSamples[i];
        }
        return new Pair<Double, Double>(totalDescent, totalAscent);
    }

    /**
     * Méthode permettant de construire les statistiques du profil
     * 
     * @return les statistiques du profil
     */
    private DoubleSummaryStatistics constructStats() {
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (float elevation : elevationSamples)
            stats.accept(elevation);

        return stats;
    }
}