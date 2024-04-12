package ch.epfl.javelo;

/**
 * Classe permettant de convertir des nombres entre la représentation Q28.4 et d'autres
 * représentations
 *
 * @author Florian COMTE (346006)
 * @author Marc FARHAT (325811)
 */
public final class Q28_4 {
    private static final int COMMA_SHIFT = 4;

    /**
     * Constructeur privé
     */
    private Q28_4() {
    }

    /**
     * Méthode permettant d'obtenir la représentation d'un entier en Q28.4
     * 
     * @param i valeur de l'entier
     * 
     * @return la valeur Q28.4 correspondant à l'entier donné
     */
    public static int ofInt(int i) {
        return i << COMMA_SHIFT;
    }

    /**
     * Méthode permettant d'obtenir un double à partir d'un entier en représentation Q28.4
     * 
     * @param q28_4 la valeur de l'entier en représentation Q28.4
     * 
     * @return la valeur de type double égale à la valeur Q28.4 donnée
     */
    public static double asDouble(int q28_4) {
        return Math.scalb((double) q28_4, -COMMA_SHIFT);
    }

    /**
     * Méthode permettant d'obtenir un float à partir d'un entier en représentation Q28.4
     * 
     * @param q28_4 la valeur de l'entier en représentation Q28.4
     * 
     * @return la valeur de type float égale à la valeur Q28.4 donnée
     */
    public static float asFloat(int q28_4) {
        return Math.scalb(q28_4, -COMMA_SHIFT);
    }
}