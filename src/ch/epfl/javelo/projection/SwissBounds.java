package ch.epfl.javelo.projection;

/**
 * Classe permettant d'obtenir les coordonnées limites de la Suisse et de vérifier si un point est
 * inclus dans ces dernières
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class SwissBounds {
    /**
     * Constructeur privé
     */
    private SwissBounds() {
    }

    /**
     * Plus petite coordonnée EST de la Suisse
     */
    public static final double MIN_E = 2485000;

    /**
     * Plus grande coordonnée EST de la Suisse
     */
    public static final double MAX_E = 2834000;

    /**
     * Plus petite coordonnée NORD de la Suisse
     */
    public static final double MIN_N = 1075000;

    /**
     * Plus grande coordonnée NORD de la Suisse
     */
    public static final double MAX_N = 1296000;

    /**
     * Largeur de la Suisse en mètres
     */
    public static final double WIDTH = MAX_E - MIN_E;

    /**
     * Hauteur de la Suisse en mètres
     */
    public static final double HEIGHT = MAX_N - MIN_N;

    /**
     * Méthode permettant de déterminer si un point se trouve sur le territoire Suisse
     * 
     * @param e coordonnée EST du point dans le système Suisse
     * @param n coordonnée NORD du point dans le système Suisse
     * 
     * @return TRUE si le point appartient au territoire Suisse et FALSE autrement
     */
    public static boolean containsEN(double e, double n) {
        return (e <= MAX_E) && (e >= MIN_E) && (n <= MAX_N) && (n >= MIN_N);
    }
}