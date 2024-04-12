package ch.epfl.javelo.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.epfl.javelo.routing.ElevationProfile;
import ch.epfl.javelo.routing.ElevationProfileComputer;
import ch.epfl.javelo.routing.MultiRoute;
import ch.epfl.javelo.routing.Route;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Pair;

/**
 * Classe représentant un bean JavaFX regroupant les propriétés relatives aux points de passage et à
 * l'itinéraire correspondant
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class RouteBean {
    private static final int MAX_CACHE_SIZE = 15;
    private static final int INIT_CAPACITY_LIST = 16;
    private static final int MIN_WAYPOINTS_SIZE = 2;
    private static final int MAX_STEP_LENGTH = 5;
    private static final float FACTOR_LIST = 0.75f;
    private static final boolean ACCESS_ORDER_LIST = true;

    private final RouteComputer routeComputer;
    private final ObservableList<Waypoint> waypoints;
    private final ObjectProperty<Route> route;
    private final DoubleProperty highlightedPosition;
    private final ObjectProperty<ElevationProfile> elevationProfile;

    private final Map<Pair<Integer, Integer>, Route> routeCache;

    /**
     * Construit le bean JavaFX
     * 
     * @param routeComputer calculateur d'itinéraire
     */
    public RouteBean(RouteComputer routeComputer) {
        this.routeComputer = routeComputer;
        this.route = new SimpleObjectProperty<>();
        this.highlightedPosition = new SimpleDoubleProperty();
        this.elevationProfile = new SimpleObjectProperty<>();
        this.routeCache = new LinkedHashMap<>(INIT_CAPACITY_LIST, FACTOR_LIST, ACCESS_ORDER_LIST);
        this.waypoints = FXCollections.observableArrayList();

        setupListeners();
    }

    /**
     * Méthode permettant d'obtenir la liste de points de passage
     * 
     * @return la liste de points de passage
     */
    public ObservableList<Waypoint> waypoints() {
        return waypoints;
    }

    /**
     * Méthode permettant d'obtenir la propriété JavaFX de l'itinéraire
     * 
     * @return la propriété JavaFX de l'itinéraire
     */
    public ReadOnlyObjectProperty<Route> routeProperty() {
        return route;
    }

    /**
     * Méthode permettant d'obtenir la propriété JavaFX du profil de l'itinéraire
     * 
     * @return la propriété JavaFX du profil de l'itinéraire
     */
    public ReadOnlyObjectProperty<ElevationProfile> elevationProfileProperty() {
        return elevationProfile;
    }

    /**
     * Méthode permettant d'obtenir le profil de l'itinéraire
     * 
     * @return le profil de l'itinéraire
     */
    public ElevationProfile elevationProfile() {
        return elevationProfile.get();
    }

    /**
     * Méthode permettant d'obtenir l'itinéraire
     * 
     * @return l'itinéraire
     */
    public Route route() {
        return route.get();
    }

    /**
     * Méthode permettant d'obtenir la propriété JavaFX de la position mise en évidence
     * 
     * @return la propriété JavaFX de la position mise en évidence
     */
    public DoubleProperty highlightedPositionProperty() {
        return highlightedPosition;
    }

    /**
     * Méthode permettant d'obtenir la position mise en évidence
     * 
     * @return la position mise en évidence
     */
    public double highlightedPosition() {
        return highlightedPosition.get();
    }

    /**
     * Méthode permettant d'obtenir l'index d'un segment à une position donnée
     * 
     * @param position position sur l'itinéraire
     * 
     * @return l'index du segment à la position donnée en mètres
     */
    public int indexOfNonEmptySegmentAt(double position) {
        int index = route().indexOfSegmentAt(position);
        for (int i = 0; i <= index; i += 1) {
            int n1 = waypoints.get(i).id();
            int n2 = waypoints.get(i + 1).id();
            if (n1 == n2)
                index += 1;
        }
        return index;
    }

    /**
     * Méthode permettant de calculer l'itinéraire
     */
    private void calculateRoute() {
        if (waypoints.size() < MIN_WAYPOINTS_SIZE) {
            route.set(null);
            elevationProfile.set(null);
            return;
        }

        List<Route> singlesRoutes = new ArrayList<>();
        Iterator<Waypoint> it = waypoints.iterator();
        /*
         * on peut récuperer le premier élement car l'on a vérifié qu'il y a au moins deux points de
         * passages
         */
        Waypoint previousWaypoint = it.next();
        while (it.hasNext()) {
            Waypoint nextWaypoint = it.next();
            int firstId = previousWaypoint.id();
            int secondId = nextWaypoint.id();

            if (firstId == secondId)
                continue;

            Route singleRoute = getRoute(firstId, secondId);

            if (singleRoute == null) {
                route.set(null);
                elevationProfile.set(null);
                return;
            }

            singlesRoutes.add(singleRoute);
            previousWaypoint = nextWaypoint;
        }

        if (singlesRoutes.isEmpty()) {
            route.set(null);
            elevationProfile.set(null);
            return;
        }

        Route finalRoute = new MultiRoute(singlesRoutes);
        ElevationProfile routeProfile = ElevationProfileComputer.elevationProfile(finalRoute,
                MAX_STEP_LENGTH);

        route.set(finalRoute);
        elevationProfile.set(routeProfile);
    }

    /**
     * Méthode permettant de récupérer l'itinéraire à partir des identifiants des noeuds aux
     * extrémités
     * 
     * @param firstId  identifiant du premier noeud
     * @param secondId identifiant du second noeud
     * 
     * @return l'itinéraire
     */
    private Route getRoute(int firstId, int secondId) {
        for (Entry<Pair<Integer, Integer>, Route> entry : routeCache.entrySet()) {
            if (entry.getKey().equals(new Pair<>(firstId, secondId)))
                return entry.getValue();
        }

        Route r = routeComputer.bestRouteBetween(firstId, secondId);
        if (routeCache.size() >= MAX_CACHE_SIZE)
            routeCache.remove(routeCache.keySet().iterator().next());
        /**
         * enregistrement de la route dans le cache même si elle est nulle pour éviter de la
         * recalculer
         */
        routeCache.put(new Pair<Integer, Integer>(firstId, secondId), r);

        return r;
    }

    /**
     * Méthode permettant de mettre en place les auditeurs
     */
    private void setupListeners() {
        waypoints.addListener((ListChangeListener<Waypoint>) (c -> calculateRoute()));
    }
}