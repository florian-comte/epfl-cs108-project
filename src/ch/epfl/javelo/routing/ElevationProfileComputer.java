package ch.epfl.javelo.routing;

import java.util.Arrays;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

/**
 * Classe représentant un calculateur de profil en long
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class ElevationProfileComputer {
    /**
     * Constructeur privé
     */
    private ElevationProfileComputer() {
    }

    /**
     * Méthode permettant d'obtenir le profil en long d'un itinéraire
     * 
     * @param route         itinéraire souhaité
     * @param maxStepLength espacement maximum entre les échantillons du profil
     * 
     * @throws IllegalArgumentException si maxStepLength est inférieur ou égal à 0
     * 
     * @return le profil en long de l'itinéraire
     */
    public static ElevationProfile elevationProfile(Route route, double maxStepLength) {
        Preconditions.checkArgument(maxStepLength > 0);

        int nbrSamples = (int) (Math.ceil(route.length() / maxStepLength) + 1);
        double lengthInterval = Math.ceil(route.length())
                / Math.ceil(route.length() / maxStepLength);
        float[] samples = new float[nbrSamples];

        float firstValidArg = Float.NaN;
        int firstValidIndex = -1;
        float lastValidArg = Float.NaN;
        int lastValidIndex = -1;

        // Remplissage avec valeurs connues et sinon NaN
        for (int i = 0; i < nbrSamples; i++) {
            float currentElevation = (float) route.elevationAt(lengthInterval * i);
            if (!Float.isNaN(currentElevation)) {
                if (Float.isNaN(firstValidArg)) {
                    firstValidArg = currentElevation;
                    firstValidIndex = i;
                }
                lastValidArg = currentElevation;
                lastValidIndex = i;
            }
            samples[i] = currentElevation;
        }

        // cas dégénéré
        if (Float.isNaN(firstValidArg)) {
            Arrays.fill(samples, 0);
        } else {
            // Remplissage des valeurs avant le premier bon argument et après le
            // dernier bon argument
            Arrays.fill(samples, 0, firstValidIndex, firstValidArg);
            Arrays.fill(samples, lastValidIndex, nbrSamples, lastValidArg);

            defineNaNValuesInSamples(samples, firstValidIndex, firstValidArg);
        }
        return new ElevationProfile(route.length(), samples);
    }

    /**
     * Méthode permettant de définir les valeurs non définies par interpolation linéaire
     * 
     * @param samples        tableau des échantillons
     * @param firstIndex     index du premier échantillon défini
     * @param firstArg       valeur du premier échantillon défini
     */
    private static void defineNaNValuesInSamples(float[] samples, int firstIndex, float firstArg) {
        boolean currentNanPeriod = false;
        int i = 0;
        // parcours du tableau d'échantillons
        for (float currentElevation : samples) {

            // si on recupère une élévation définie
            if (!Float.isNaN(currentElevation)) {
                if (currentNanPeriod) {
                    // on est à la fin d'une période de "NaN"
                    int nbrInterpolate = i - firstIndex;
                    /*
                     * on itère le nombre de fois entre l'index actuel et le dernier index défini
                     */
                    for (int counter = 1; counter < nbrInterpolate; counter++) {
                        /*
                         * récupération de l'abscisse en divisant le compteur d'interpolations par
                         * le nombre d'élévations à interpoller
                         */
                        double x = (double) counter / nbrInterpolate;
                        samples[counter + firstIndex] = (float) Math2.interpolate(firstArg,
                                currentElevation, x);
                    }
                }

                /*
                 * on définit à chaque fois le dernier argument valide et son index
                 */
                firstArg = currentElevation;
                firstIndex = i;
                currentNanPeriod = false;
            } else {
                // sinon on dit que l'on est dans une periode "NaN"
                currentNanPeriod = true;
            }
            i++;
        }
    }
}