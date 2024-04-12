package ch.epfl.javelo;

/**
 * Classe permettant d'effectuer des calculs mathématiques nécessaires
 *
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class Math2 {
    /**
     * Constructeur privé
     */
    private Math2() {
    }

    /**
     * Méthode permettant d'obtenir la partie entière par excès de la division de x par y
     * 
     * @param x numérateur
     * @param y dénominateur
     * 
     * @throws IllegalArgumentException dans le cas ou x est négatif ou y est négatif ou nul
     * 
     * @return la partie entière par excès de la division de x par y
     **/
    public static int ceilDiv(int x, int y) {
        Preconditions.checkArgument(x >= 0 && y > 0);
        return (x + y - 1) / y;
    }

    /**
     * Méthode permettant d'obtenir la coordonnée y du point se trouvant sur la droite passant par
     * (0,y0) et (1,y1) et de coordonnée x donnée
     * 
     * @param y0 ordonnée d'un point de la droite
     * @param y1 ordonnée d'un point de la droite
     * @param x  abscisse du point
     * 
     * @return ordonnée du point d'abcisse x sur la droite passant par (0,y0) et (1,y1)
     */
    public static double interpolate(double y0, double y1, double x) {
        return Math.fma(y1 - y0, x, y0);
    }

    /**
     * Méthode permettant de limiter la valeur v dans l'intervalle allant de min à max
     * 
     * @param min valeur minimale de l'intervalle
     * @param v   paramètre à encadrer
     * @param max valeur maximale de l'intervalle
     * 
     * @throws IllegalArgumentException dans le cas où le min > max
     * 
     * @return min si v est inférieur à min, v si v est compris entre le min et max si v est
     *         supérieur à max
     */
    public static int clamp(int min, int v, int max) {
        Preconditions.checkArgument(min <= max);
        return Math.min(max, Math.max(min, v));
    }

    /**
     * Méthode permettant de limiter la valeur v dans l'intervalle allant de min à max
     * 
     * @param min valeur minimale de l'intervalle
     * @param v   paramètre à encadrer
     * @param max valeur maximale de l'intervalle
     * 
     * @throws IllegalArgumentException dans le cas où le min > max
     * 
     * @return min si v est inférieur à min, v si v est compris entre le min et max si v est
     *         supérieur à max
     */
    public static double clamp(double min, double v, double max) {
        Preconditions.checkArgument(min <= max);
        return Math.min(max, Math.max(min, v));

    }

    /**
     * Méthode permettant d'obtenir le sinus hyperbolique inverse de x
     * 
     * @param x argument du sinus
     * 
     * @return le sinus hyperbolique inverse de l'argument x
     */
    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(1 + x * x));
    }

    /**
     * Méthode permettant d'obtenir le produit scalaire entre les vecteurs u et v
     * 
     * @param uX composante x du vecteur u
     * @param uY composante y du vecteur u
     * @param vX composante x du vecteur v
     * @param vY composante y du vecteur v
     * 
     * @return le produit scalaire entre u et v
     */
    public static double dotProduct(double uX, double uY, double vX, double vY) {
        return Math.fma(uX, vX, uY * vY);
    }

    /**
     * Méthode permettant d'obtenir la norme du vecteur u
     * 
     * @param uX composante x du vecteur u
     * @param uY composante y du vecteur u
     * 
     * @return la norme du vecteur u
     */
    public static double norm(double uX, double uY) {
        return Math.sqrt(squaredNorm(uX, uY));
    }

    /**
     * Méthode permettant d'obtenir la norme au carré du vecteur u
     * 
     * @param uX composante x du vecteur u
     * @param uY composante y du vecteur u
     * 
     * @return la norme au carré du vecteur u
     */
    public static double squaredNorm(double uX, double uY) {
        return dotProduct(uX, uY, uX, uY);
    }

    /**
     * Méthode permettant d'obtenir la longueur de la projection du vecteur AP sur le vecteur AB
     * 
     * @param aX composante x du vecteur a
     * @param aY composante y du vecteur a
     * @param bX composante x du vecteur b
     * @param bY composante y du vecteur b
     * @param pX composante x du vecteur p
     * @param pY composante y du vecteur p
     * 
     * @return la longueur de la projection du vecteur AP sur le vecteur AB
     */
    public static double projectionLength(double aX, double aY, double bX, double bY, double pX,
            double pY) {
        return dotProduct(pX - aX, pY - aY, bX - aX, bY - aY) / norm(bX - aX, bY - aY);
    }
}