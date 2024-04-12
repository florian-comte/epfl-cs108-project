package ch.epfl.javelo.projection;

import ch.epfl.javelo.Preconditions;

/**
 * Enregistrement représentant un point dans le système Web Mercator
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 * 
 * @param x coordonnée x du point
 * @param y coordonnée y du point
 */
public record PointWebMercator(double x, double y) {

    private static final int MIN_IMAGE_POWER_SIZE = 8;
    /**
     * Construit un point dans le système Web Mercator
     * 
     * @throws IllegalArgumentException si x ou y n'est pas compris dans [0;1]
     */
    public PointWebMercator {
        Preconditions.checkArgument(x <= 1 && x >= 0 && y <= 1 && y >= 0);
    }

    /**
     * Méthode de construction d'un point dans le système Web Mercator
     * 
     * @param zoomLevel niveau de zoom
     * @param x         coordonnée x du point
     * @param y         coordonnée y du point
     * 
     * @return un point de coordonnées x et y dans le système Web Mercator avec un certain niveau de
     *         zoom
     */
    public static PointWebMercator of(int zoomLevel, double x, double y) {
        return new PointWebMercator(Math.scalb(x, -MIN_IMAGE_POWER_SIZE - zoomLevel),
                Math.scalb(y, -MIN_IMAGE_POWER_SIZE - zoomLevel));
    }

    /**
     * Méthode de construction d'un point dans le système Web Mercator
     * 
     * @param pointCh point dans le système Suisse
     * 
     * @return un point de coordonnées x et y dans le système Web Mercator
     */
    public static PointWebMercator ofPointCh(PointCh pointCh) {
        return new PointWebMercator(WebMercator.x(pointCh.lon()), WebMercator.y(pointCh.lat()));
    }

    /**
     * Méthode permettant d'obtenir la coordonnée x à un certain niveau de zoom
     * 
     * @param zoomLevel niveau de zoom
     * 
     * @return coordonnée x dans le système Web Mercator
     */
    public double xAtZoomLevel(int zoomLevel) {
        return Math.scalb(x, MIN_IMAGE_POWER_SIZE + zoomLevel);
    }

    /**
     * Méthode permettant d'obtenir la coordonnée y à un certain niveau de zoom
     * 
     * @param zoomLevel niveau de zoom
     * 
     * @return coordonnée y dans le système Web Mercator
     */
    public double yAtZoomLevel(int zoomLevel) {
        return Math.scalb(y, MIN_IMAGE_POWER_SIZE + zoomLevel);
    }

    /**
     * Méthode permettant d'obtenir la longitude du point
     * 
     * @return la longitude du point en radians
     */
    public double lon() {
        return WebMercator.lon(x);
    }

    /**
     * Méthode permettant d'obtenir la latitude du point
     * 
     * @return la latitude du point en radians
     */
    public double lat() {
        return WebMercator.lat(y);
    }

    /**
     * Méthode permettant de convertir le point dans le système Suisse
     * 
     * @return un point dans le système Suisse
     */
    public PointCh toPointCh() {
        double lon = lon();
        double lat = lat();
        return SwissBounds.containsEN(Ch1903.e(lon, lat), Ch1903.n(lon, lat))
                ? new PointCh(Ch1903.e(lon, lat), Ch1903.n(lon, lat))
                : null;
    }
}