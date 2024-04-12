package ch.epfl.javelo;

/**
 * Classe permettant de vérifier des préconditions
 *
 * @author Florian COMTE (346006)
 * @author Marc FARHAT (325811)
 */
public final class Preconditions {
    /**
     * Constructeur privé
     */
    private Preconditions() {
    }

    /**
     * Méthode permettant de lever IllegalArgumentException si son argument est faux
     * 
     * @param shouldBeTrue condition qui est censée être respectée
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue)
            throw new IllegalArgumentException();
    }
}