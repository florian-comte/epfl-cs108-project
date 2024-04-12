package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

/**
 * Enregistrement représentant un point dans le système de coordonnées Suisses
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 * 
 * @param e coordonnée EST du point
 * @param n coordonnée NORD du point
 */
public record PointCh(double e, double n) {

    /**
     * Construit un point dans le système de coordonnées Suisses
     * 
     * @throws IllegalArgumentException si les coordonnées fournies ne sont pas dans les limites de
     *                                  la Suisse définies par SwissBounds
     */
    public PointCh  {
        Preconditions.checkArgument(SwissBounds.containsEN(e, n));
    }

    /**
     * Méthode permettant d'obtenir le carré de la distance en mètres séparant le récepteur (this)
     * de l'argument that
     * 
     * @param that point dans le système Suisse pour lequel on veut calculer la distance avec le
     *             récepteur (this)
     * 
     * @return le carré de la distance en mètres séparant le récepteur (this) de l'argument that
     */
    public double squaredDistanceTo(PointCh that) {
        return Math2.squaredNorm(that.e - e, that.n - n);
    }

    /**
     * Méthode permettant d'obtenir la distance en mètres séparant le récepteur (this) de l'argument
     * that
     * 
     * @param that point dans le système Suisse pour lequel on veut calculer la distance avec le
     *             récepteur (this)
     * 
     * @return la distance en mètres séparant le récepteur (this) de l'argument that
     */
    public double distanceTo(PointCh that) {
        return Math.sqrt(squaredDistanceTo(that));
    }

    /**
     * Méthode permettant d'obtenir la longitude du point dans le système WGS84
     * 
     * @return la longitude du point dans le système WGS84 en radians
     */
    public double lon() {
        return Ch1903.lon(e, n);
    }

    /**
     * Méthode permettant d'obtenir la latitude du point dans le système WGS84
     * 
     * @return la latitude du point dans le système WGS84 en radians
     */
    public double lat() {
        return Ch1903.lat(e, n);
    }
}