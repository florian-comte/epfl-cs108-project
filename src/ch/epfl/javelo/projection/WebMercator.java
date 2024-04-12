package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;

/**
 * Classe permettant de faire des conversions entre des coordonnées WGS84 et des coordonnées Web
 * Mercator
 *
 * @author Florian COMTE (346006)
 * @author Marc FARHAT (325811)
 */
public final class WebMercator {
    /**
     * Constructeur privé
     */
    private WebMercator() {
    }

    /**
     * Méthode permettant d'obtenir la coordonnée x de la projection d'un point se trouvant à une
     * certaine longitude
     * 
     * @param lon longitude du point dans le système WGS84 en radians
     * 
     * @return la coordonnée x de la projection du point
     */
    public static double x(double lon) {
        return Math.fma(1.0 / (2 * Math.PI), lon, 1.0 / 2.0);
    }

    /**
     * Méthode permettant d'obtenir la coordonnée y de la projection d'un point se trouvant à une
     * certaine latitude
     * 
     * @param lat latitude du point dans le système WGS84 en radians
     * 
     * @return la coordonnée y de la projection du point
     */
    public static double y(double lat) {
        return Math.fma(1.0 / (2 * Math.PI), -Math2.asinh(Math.tan(lat)), 1.0 / 2.0);

    }

    /**
     * Méthode permettant d'obtenir la longitude d'un point à partir de la coordonnée x
     * 
     * @param x coordonnée x dans le système Web Mercator
     * 
     * @return la longitude en radians du point
     */
    public static double lon(double x) {
        return Math.fma(2 * Math.PI, x, -Math.PI);

    }

    /**
     * Méthode permettant d'obtenir la latitude d'un point à partir de la coordonnée y
     * 
     * @param y coordonnée y dans le système Web Mercator
     * 
     * @return la latitude en radians du point
     */
    public static double lat(double y) {
        return Math.atan(Math.sinh(Math.fma(-2 * Math.PI, y, Math.PI)));
    }
}