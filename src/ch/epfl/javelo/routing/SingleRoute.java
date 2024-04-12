package ch.epfl.javelo.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

/**
 * Classe représentant un itinéraire simple
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class SingleRoute implements Route {
    private final List<Edge> edges;
    private final double length;
    private final List<PointCh> points;
    private final double[] lengthEdges;

    /**
     * Construit un itinéraire simple
     * 
     * @param edges liste des arêtes de l'itinéraire
     * 
     * @throws IllegalArgumentException si la liste des arêtes est vide
     */
    public SingleRoute(List<Edge> edges) {
        Preconditions.checkArgument(!edges.isEmpty());
        this.edges = List.copyOf(edges);
        this.points = new ArrayList<>();
        this.lengthEdges = new double[edges.size() + 1];

        constructRoute();

        this.length = lengthEdges[edges.size()];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double length() {
        return length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Edge> edges() {
        return edges;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PointCh> points() {
        return List.copyOf(points);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PointCh pointAt(double position) {
        int edgeIndex = getEdgeIndexByPosition(position);
        return edges.get(edgeIndex).pointAt(getPositionOnEdge(position, edgeIndex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double elevationAt(double position) {
        int edgeIndex = getEdgeIndexByPosition(position);
        return edges.get(edgeIndex).elevationAt(getPositionOnEdge(position, edgeIndex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int nodeClosestTo(double position) {
        int edgeIndex = getEdgeIndexByPosition(position);
        double positionOnEdge = getPositionOnEdge(position, edgeIndex);
        return positionOnEdge > edges.get(edgeIndex).length() / 2 ? edges.get(edgeIndex).toNodeId()
                : edges.get(edgeIndex).fromNodeId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        RoutePoint routePoint = RoutePoint.NONE;

        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            double positionClosest = Math2.clamp(0, edge.positionClosestTo(point), edge.length());
            PointCh thatPoint = edge.pointAt(positionClosest);

            routePoint = routePoint.min(thatPoint, lengthEdges[i] + positionClosest,
                    thatPoint.distanceTo(point));
        }
        return routePoint;
    }

    /**
     * Méthode permettant d'obtenir la position sur une arête
     * 
     * @param position  position sur l'itinéraire
     * @param edgeIndex index de l'arête
     * 
     * @return position sur l'arête
     */
    private double getPositionOnEdge(double position, int edgeIndex) {
        return Math2.clamp(0, position, length) - lengthEdges[edgeIndex];
    }

    /**
     * Méthode permettant d'obtenir l'index d'une arête avec sa position
     * 
     * @param position position sur l'itinéraire
     * 
     * @return index de l'arête
     */
    private int getEdgeIndexByPosition(double position) {
        int binSearch = Arrays.binarySearch(lengthEdges, Math2.clamp(0, position, length));
        if (binSearch < 0)
            binSearch = -(binSearch + 2);
        if (binSearch == edges.size())
            binSearch = binSearch - 1;

        return binSearch;
    }

    // Méthodes de construction (utilisées seulement dans le constructeur)

    /**
     * Méthode permettant de construire le tableau de longueurs et les points
     */
    private void constructRoute() {
        double tempLength = 0;

        for (int i = 0; i < edges.size(); i++) {
            lengthEdges[i] = tempLength;
            points.add(edges.get(i).fromPoint());

            tempLength += edges.get(i).length();
        }
        points.add(edges.get(edges.size() - 1).toPoint());
        lengthEdges[edges.size()] = tempLength;
    }
}