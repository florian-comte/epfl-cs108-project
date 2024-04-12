package ch.epfl.javelo.routing;

import java.util.function.DoubleUnaryOperator;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

/**
 * Enregistrement représentant une arête d'un itinéraire
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 * 
 * @param fromNodeId l'identité du noeud de départ de l'arête
 * @param toNodeId   l'identité du noeud d'arrivé de l'arête
 * @param fromPoint  le point de départ de l'arête
 * @param toPoint    le point d'arrivée de l'arête
 * @param length     la longueur de l'arête en mètres
 * @param profile    le profil le long de l'arête
 */
public record Edge(int fromNodeId, int toNodeId, PointCh fromPoint, PointCh toPoint, double length,
        DoubleUnaryOperator profile) {

    /**
     * Méthode permettant d'obtenir une instance de Edge avec différents attributs
     * 
     * @param graph      graphe JaVelo
     * @param edgeId     identité de l'arête
     * @param fromNodeId l'identité du noeud de départ de l'arête
     * @param toNodeId   l'identité du noeud d'arrivée de l'arête
     * 
     * @return l'instance de Edge
     */
    public static Edge of(Graph graph, int edgeId, int fromNodeId, int toNodeId) {
        return new Edge(fromNodeId, toNodeId, graph.nodePoint(fromNodeId),
                graph.nodePoint(toNodeId), graph.edgeLength(edgeId), graph.edgeProfile(edgeId));
    }

    /**
     * Méthode permettant d'obtenir la position du point sur l'arête le plus proche du point donnée
     * 
     * @param point point de référence
     * 
     * @return la position le long de l'arête en mètres qui se trouve la plus proche du point donnée
     */
    public double positionClosestTo(PointCh point) {
        return Math2.projectionLength(fromPoint.e(), fromPoint.n(), toPoint.e(), toPoint.n(),
                point.e(), point.n());
    }

    /**
     * Méthode permettant d'obtenir le point à une certaine position sur une arête
     * 
     * @param position position sur l'arête
     * 
     * @return le point se trouvant à la position donnée sur l'arête en mètres
     */
    public PointCh pointAt(double position) {
        if (length == 0)
            return fromPoint;

        return new PointCh(Math2.interpolate(fromPoint.e(), toPoint.e(), position / length),
                Math2.interpolate(fromPoint.n(), toPoint.n(), position / length));
    }

    /**
     * Méthode permettant d'obtenir l'altitude à une certaine position sur une arête
     * 
     * @param position position sur l'arête
     * 
     * @return l'altitude à la position donnée sur l'arête en mètres
     */
    public double elevationAt(double position) {
        return profile.applyAsDouble(position);
    }
}
