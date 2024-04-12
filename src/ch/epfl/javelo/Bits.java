package ch.epfl.javelo;

/**
 * Classe permettant de manipuler des bits
 *
 * @author Florian COMTE (346006)
 * @author Marc FARHAT (325811)
 */
public final class Bits {
    private static final int INT_SIZE = Integer.SIZE;

    /**
     * Constructeur privé
     */
    private Bits() {
    }

    /**
     * Méthode permettant d'extraire d'un vecteur de 32 bits une plage de bits interprétée comme une
     * valeur signée
     * 
     * @param value  vecteur initial
     * @param start  index de début de la plage de bits à extraire
     * @param length taille de la plage de bits à extraire
     * 
     * @throws IllegalArgumentException si la plage de bits à extraire n'est pas inclus dans
     *                                  l'intervalle de 0 à 31 (inclus)
     * 
     * @return plage de bits extraite
     */
    public static int extractSigned(int value, int start, int length) {
        Preconditions.checkArgument(length >= 0 && start + length <= INT_SIZE && start >= 0);
        value = value << INT_SIZE - start - length;
        value = value >> INT_SIZE - length;

        return value;
    }

    /**
     * Méthode permettant d'extraire d'un vecteur de 32 bits une plage de bits interprétée comme une
     * valeur non signée
     * 
     * @param value  vecteur initial
     * @param start  index de début de la plage de bits à extraire
     * @param length taille de la plage de bits à extraire
     * 
     * @throws IllegalArgumentException si la plage de bits à extraire n'est pas inclus dans
     *                                  l'intervalle de 0 à 31 (inclus) ou si la taille de la plage
     *                                  de bits à extraire est égale à 32
     * 
     * @return plage de bits extraite
     */
    public static int extractUnsigned(int value, int start, int length) {
        Preconditions.checkArgument(
                length >= 0 && start + length <= INT_SIZE && length != INT_SIZE && start >= 0);

        value = value << INT_SIZE - start - length;
        value = value >>> INT_SIZE - length;
        return value;
    }
}