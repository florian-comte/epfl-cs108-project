package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

/**
 * Enregistrement représentant un point d'un itinéraire le plus proche d'un point de
 * référence donnée
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 * 
 * @param point               point sur l'itinéraire
 * @param position            position du point le long de l'itinéraire en mètres
 * @param distanceToReference la distance en mètres entre le point et la référence
 */
public record RoutePoint(PointCh point, double position, double distanceToReference) {

    /**
     * Représente un point inexistant
     */
    public static final RoutePoint NONE = new RoutePoint(null, Double.NaN,
            Double.POSITIVE_INFINITY);

    /**
     * Méthode permettant d'obtenir un point identique au récepteur (this) mais dont la position est
     * décalée
     * 
     * @param positionDifference différence de position
     * 
     * @return le point identique au récepteur (this) mais dont la position est décalée de la
     *         différence
     */
    public RoutePoint withPositionShiftedBy(double positionDifference) {
        return new RoutePoint(point, position + positionDifference, distanceToReference);
    }

    /**
     * Méthode permettant de comparer un point à celui de l'instance actuelle et d'obtenir celui
     * pour lequel sa distance à la référence est la plus faible
     * 
     * @param that point à comparer avec this
     * 
     * @return this si sa distance à la référence est inférieure ou égale à celle de that et that
     *         sinon
     */
    public RoutePoint min(RoutePoint that) {
        return (distanceToReference <= that.distanceToReference) ? this : that;
    }

    /**
     * Méthode permettant de comparer un point à celui de l'instance actuelle et d'obtenir celui
     * pour lequel sa distance à la référence est la plus faible
     * 
     * @param thatPoint               point dans le système Suisse à comparer
     * @param thatPosition            position sur l'itinéraire du point à comparer
     * @param thatDistanceToReference distance à la référence du point à comparer
     * 
     * @return this si sa distance à la référence est inférieure ou égale à thatDistanceToReference
     *         ou sinon une nouvelle instance de RoutePoint avec les arguments passes en paramètres
     */
    public RoutePoint min(PointCh thatPoint, double thatPosition, double thatDistanceToReference) {
        return (distanceToReference <= thatDistanceToReference) ? this
                : new RoutePoint(thatPoint, thatPosition, thatDistanceToReference);
    }
}