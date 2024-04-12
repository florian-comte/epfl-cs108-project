package ch.epfl.javelo.projection;

/**
 * Classe permettant de faire des conversions entre les coordonnées du système WGS84 et du système
 * Suisse
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class Ch1903 {
    /**
     * Constructeur privé
     */
    private Ch1903() {
    }

    /**
     * Méthode permettant d'obtenir la coordonnée EST d'un point dans le système Suisse
     * 
     * @param lon longitude du point dans le système WGS84
     * @param lat latitude du point dans le système WGS84
     * 
     * @return la coordonnée EST du point dans le système Suisse
     */
    public static double e(double lon, double lat) {
        double lambda = Math.pow(10, -4) * (3600 * Math.toDegrees(lon) - 26782.5);
        double phi = Math.pow(10, -4) * (3600 * Math.toDegrees(lat) - 169028.66);

        return 2600072.37 + 211455.93 * lambda - 10938.51 * lambda * phi - 0.36 * lambda * phi * phi
                - 44.54 * lambda * lambda * lambda;
    }

    /**
     * Méthode permettant d'obtenir la coordonnée NORD dans le système Suisse
     * 
     * @param lon longitude du point dans le système WGS84
     * @param lat latitude du point dans le système WGS84
     * 
     * @return la coordonnée NORD du point dans le système Suisse
     */
    public static double n(double lon, double lat) {
        double lambda = Math.pow(10, -4) * (3600 * Math.toDegrees(lon) - 26782.5);
        double phi = Math.pow(10, -4) * (3600 * Math.toDegrees(lat) - 169028.66);

        return 1200147.07 + 308807.95 * phi + 3745.25 * lambda * lambda + 76.63 * phi * phi
                - 194.56 * lambda * lambda * phi + 119.79 * phi * phi * phi;
    }

    /**
     * Méthode permettant d'obtenir la longitude d'un point dans le système WGS84
     * 
     * @param e coordonnée EST du point dans le système Suisse
     * @param n coordonnée NORD du point dans le système Suisse
     * 
     * @return la longitude dans le système WGS84
     */
    public static double lon(double e, double n) {
        double x = Math.pow(10, -6) * (e - 2600000);
        double y = Math.pow(10, -6) * (n - 1200000);

        double lambda = 2.6779094 + 4.728982 * x + 0.791484 * y * x + 0.1306 * y * y * x
                - 0.0436 * x * x * x;

        return Math.toRadians(lambda) * 100.0 / 36.0;
    }

    /**
     * Méthode permettant d'obtenir la latitude d'un point dans le système WGS84
     * 
     * @param e coordonnée EST du point dans le système Suisse
     * @param n coordonnée NORD du point dans le système Suisse
     * 
     * @return la latitude dans le système WGS84
     */
    public static double lat(double e, double n) {
        double x = Math.pow(10, -6) * (e - 2600000);
        double y = Math.pow(10, -6) * (n - 1200000);

        double phi = 16.9023892 + 3.238272 * y - 0.270978 * x * x - 0.002528 * y * y
                - 0.0447 * y * x * x - 0.0140 * y * y * y;

        return Math.toRadians(phi) * 100.0 / 36.0;
    }
}