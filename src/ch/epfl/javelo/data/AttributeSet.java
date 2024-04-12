package ch.epfl.javelo.data;

import java.util.StringJoiner;

import ch.epfl.javelo.Preconditions;

/**
 * Enregistrement représentant un ensemble d'attributs OpenStreetMap
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 * 
 * @param bits contenu de l'ensemble au moyen d'un bit par valeur possible
 */
public record AttributeSet(long bits) {
    /**
     * Construit un ensemble d'attributs OpenStreetMap
     * 
     * @throws IllegalArgumentException si la valeur passée contient un bit à 1 qui ne correspond à
     *                                  aucun attribut valide
     */
    public AttributeSet {
        Preconditions.checkArgument(((bits >> Attribute.COUNT) == 0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringJoiner j = new StringJoiner(",", "{", "}");
        for (Attribute attr : Attribute.ALL)
            if (contains(attr))
                j.add(attr.keyValue());
        return j.toString();

    }

    /**
     * Méthode de création d'un ensemble d'attributs OpenStreetMap
     * 
     * @param attributes attributs souhaités dans l'ensemble
     * 
     * @return l'ensemble d'attributs OpenStreetMap passé en paramètre
     */
    public static AttributeSet of(Attribute... attributes) {
        long bit = 0L;
        for (Attribute attr : attributes)
            bit = bit | getMaskOfPosition(attr);
        return new AttributeSet(bit);
    }

    /**
     * Méthode permettant de savoir si un attribut est contenu dans l'ensemble d'attributs
     * 
     * @param attribute attribut pour lequel on veut vérifier la contenance dans l'ensemble
     * 
     * @return TRUE si l'ensemble récepteur (this) contient l'attribut donné sinon FALSE
     */
    public boolean contains(Attribute attribute) {
        return (getMaskOfPosition(attribute) & bits) == getMaskOfPosition(attribute);
    }

    /**
     * Méthode permettant de savoir si deux sets d'attributs ont des attributs en commun
     * 
     * @param that autre set d'attributs à comparer
     * 
     * @return TRUE si l'intersection de l'ensemble récepteur (this) avec celui passé en argument
     *         (that) n'est pas vide sinon FALSE
     */
    public boolean intersects(AttributeSet that) {
        return (that.bits & bits) != 0;
    }

    /**
     * Méthode permettant d'obtenir un masque de bits correspondant à un certain attribut
     * 
     * @param attribut attribut souhaité
     * 
     * @return un masque de bits pour l'attribut souhaité
     */
    private static long getMaskOfPosition(Attribute attribut) {
        return 1L << attribut.ordinal();
    }
}