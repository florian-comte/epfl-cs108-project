package ch.epfl.javelo.routing;

import java.util.List;

import ch.epfl.javelo.projection.PointCh;

/**
 * Interface représentant un itinéraire
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public interface Route {
    /**
     * Méthode permettant d'obtenir l'index d'un segment à une position donnée
     * 
     * @param position position sur l'itinéraire
     * 
     * @return l'index du segment à la position donnée en mètres
     */
    int indexOfSegmentAt(double position);

    /**
     * Méthode permettant d'obtenir la longueur d'un itinéraire
     * 
     * @return longueur de l'itinéraire en mètres
     */
    double length();

    /**
     * Méthode permettant d'obtenir la totalité des arêtes de l'itinéraire
     * 
     * @return la totalité des arêtes de l'itinéraire
     */
    List<Edge> edges();

    /**
     * Méthode permettant d'obtenir la totalité des points situés aux extrémités des arêtes de
     * l'itinéraire
     * 
     * @return la totalité des points situés aux extrémités des arêtes de l'itinéraire
     */
    List<PointCh> points();

    /**
     * Méthode permettant d'obtenir le point se trouvant à une certaine position sur l'itinéraire
     * 
     * @param position position sur l'itinéraire
     * 
     * @return le point se trouvant à la position donnée le long de l'itineaire
     */
    PointCh pointAt(double position);

    /**
     * Méthode permettant d'obtenir l'altitude à une certaine position sur l'itinéraire
     * 
     * @param position position sur l'itinéraire
     * 
     * @return l'altitude à la position donnée le long de l'itineaire
     */
    double elevationAt(double position);

    /**
     * Méthode permettant d'obtenir l'identité du noeud d'un itinéraire le plus proche d'une
     * position donnée
     * 
     * @param position position sur l'itinéraire
     * 
     * @return l'identité du noeud appartenant à l'itinéraire et se trouvant le plus proche de la
     *         position donnée
     */
    int nodeClosestTo(double position);

    /**
     * Méthode permettant d'obtenir le point de l'itinéraire se trouvant le plus proche d'un point
     * de référence
     * 
     * @param point point de référence
     * 
     * @return le point de l'itinéraire se trouvant le plus proche du point de référence donné
     */
    RoutePoint pointClosestTo(PointCh point);
}