package ch.epfl.javelo.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;
import javafx.util.Pair;

/**
 * Classe représentant un itinéraire multiple
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class MultiRoute implements Route {
    private final List<Route> segments;
    private final List<Edge> edges;
    private final List<PointCh> points;
    private final double[] lengthSegments;
    private final double length;

    /**
     * Construit un itinéraire multiple
     * 
     * @param segments liste des segments de l'itinéraire
     * 
     * @throws IllegalArgumentException si la liste des segments est vide
     */
    public MultiRoute(List<Route> segments) {
        Preconditions.checkArgument(!segments.isEmpty());
        this.segments = List.copyOf(segments);
        this.lengthSegments = new double[segments.size() + 1];
        Pair<List<Edge>, List<PointCh>> lists = constructRoute();
        this.edges = List.copyOf(lists.getKey());
        this.points = List.copyOf(lists.getValue());
        this.length = lengthSegments[segments.size()];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOfSegmentAt(double position) {
        int segmentIndex = getSegmentIndexByPosition(position);

        int numberSegments = 0;
        for (int i = 0; i < segmentIndex; i++)
            numberSegments += segments.get(i).indexOfSegmentAt(getPositionOnSegment(position, i))
                    + 1;

        return numberSegments + segments.get(segmentIndex)
                .indexOfSegmentAt(getPositionOnSegment(position, segmentIndex));
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
        return points;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PointCh pointAt(double position) {
        int segmentIndex = getSegmentIndexByPosition(position);
        return segments.get(segmentIndex).pointAt(getPositionOnSegment(position, segmentIndex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double elevationAt(double position) {
        int segmentIndex = getSegmentIndexByPosition(position);
        return segments.get(segmentIndex).elevationAt(getPositionOnSegment(position, segmentIndex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int nodeClosestTo(double position) {
        int segmentIndex = getSegmentIndexByPosition(position);
        return segments.get(segmentIndex)
                .nodeClosestTo(getPositionOnSegment(position, segmentIndex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        RoutePoint routePoint = RoutePoint.NONE;

        for (int i = 0; i < segments.size(); i++)
            routePoint = routePoint.min(
                    segments.get(i).pointClosestTo(point).withPositionShiftedBy(lengthSegments[i]));

        return routePoint;
    }

    /**
     * Méthode permettant d'obtenir la position sur un segment
     * 
     * @param position     position sur l'itinéraire
     * @param segmentIndex index du segment
     * 
     * @return position sur le segment
     */
    private double getPositionOnSegment(double position, int segmentIndex) {
        return Math2.clamp(0, position, length) - lengthSegments[segmentIndex];
    }

    /**
     * Méthode permettant d'obtenir l'index d'un segment avec sa position
     * 
     * @param position position sur l'itinéraire
     * 
     * @return index du segment
     */
    private int getSegmentIndexByPosition(double position) {
        int binSearch = Arrays.binarySearch(lengthSegments, Math2.clamp(0, position, length));
        if (binSearch < 0)
            binSearch = Math.abs(binSearch + 2);
        if (binSearch == segments.size())
            binSearch = binSearch - 1;

        return binSearch;
    }

    // Méthodes de construction (utilisées seulement dans le constructeur)

    /**
     * Méthode permettant de construire le tableau de longueurs, les points et les arêtes
     */
    private Pair<List<Edge>, List<PointCh>> constructRoute() {
        double tempLength = 0;
        List<Edge> edgesTemp = new ArrayList<>();
        List<PointCh> pointsTemp = new ArrayList<>();

        for (int i = 0; i < segments.size(); i++) {
            Route segment = segments.get(i);
            edgesTemp.addAll(segment.edges());
            pointsTemp.addAll(segment.points());

            /*
             * Suppression du dernier point tant que l'on est pas à la fin de la liste de segments
             * pour éviter les doublons
             */
            if (i < segments.size() - 1)
                pointsTemp.remove(pointsTemp.size() - 1);

            lengthSegments[i] = tempLength;
            tempLength += segment.length();
        }
        lengthSegments[segments.size()] = tempLength;

        return new Pair<List<Edge>, List<PointCh>>(edgesTemp, pointsTemp);
    }
}