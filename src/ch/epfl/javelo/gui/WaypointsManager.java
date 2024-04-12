package ch.epfl.javelo.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

/**
 * Classe représentant le gestionnaire de points de passage
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class WaypointsManager {
    private static final double SEARCH_DISTANCE = 500;
    private static final int NO_NODE_CLOSEST = -1;
    private static final String EXTERIOR_PIN_CONTENT = "M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20";
    private static final String INTERIOR_PIN_CONTENT = "M0-23A1 1 0 000-29 1 1 0 000-23";
    private static final String PIN_STYLE = "pin";
    private static final String PIN_OUTSIDE_STYLE = "pin_outside";
    private static final String PIN_INSIDE_STYLE = "pin_inside";
    private static final String FIRST_STYLE = "first";
    private static final String MIDDLE_STYLE = "middle";
    private static final String LAST_STYLE = "last";
    private static final String ERROR_NO_PATH = "Aucune route à proximité !";

    private final Pane pane;
    private final Graph graph;
    private final Consumer<String> error;
    private final List<Node> groups;
    private final ObservableList<Waypoint> waypoints;
    private final ObjectProperty<MapViewParameters> viewParams;

    /**
     * Construit un gestionnaire de points de passage
     * 
     * @param graph      le graphe JaVelo
     * @param viewParams propriété JavaFX contenant les paramètres de la carte
     * @param waypoints  liste de points de passage initiaux
     * @param error      consommateur erreur
     */
    public WaypointsManager(Graph graph, ObjectProperty<MapViewParameters> viewParams,
            ObservableList<Waypoint> waypoints, Consumer<String> error) {
        this.pane = new Pane();
        this.graph = graph;
        this.waypoints = waypoints;
        this.viewParams = viewParams;
        this.error = error;
        this.groups = new ArrayList<>();

        setupStyle();
        setupListeners();
        createWaypoints();
    }

    /**
     * Méthode permettant d'obtenir le panneau JavaFX affichant les waypoints
     * 
     * @return le panneau JavaFX affichant les waypoints
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Méthode permettant d'ajouter un point de passage
     * 
     * @param x coordonnée x du point de passage
     * @param y coordonnée y du point de passage
     */
    public void addWaypoint(double x, double y) {
        PointCh point = viewParams().getPointChFromPane(x, y);
        int closestNode = getClosestNode(point);
        if (closestNode != NO_NODE_CLOSEST)
            waypoints.add(new Waypoint(point, closestNode));
    }

    /**
     * Méthode permettant de récuperer l'identité du noeud le plus proche d'un point dans un rayon
     * de SEARCH_DISTANCE
     * 
     * @param x coordonnée x
     * @param y coordonnée y
     * 
     * @return l'identité du noeud le plus proche ou -1 s'il n'y en a pas
     */
    private int getClosestNode(PointCh point) {
        if (point == null) {
            error.accept(ERROR_NO_PATH);
            return NO_NODE_CLOSEST;
        }

        int closestNode = graph.nodeClosestTo(point, SEARCH_DISTANCE);
        if (closestNode == NO_NODE_CLOSEST) {
            error.accept(ERROR_NO_PATH);
            return NO_NODE_CLOSEST;
        }
        return closestNode;
    }

    /**
     * Méthode permettant de créer les points de passage
     */
    private void createWaypoints() {
        ObjectProperty<Point2D> lastCursorPosition = new SimpleObjectProperty<Point2D>();

        groups.clear();
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint waypoint = waypoints.get(i);
            SVGPath interior = new SVGPath();
            SVGPath exterior = new SVGPath();
            Group group = new Group(exterior, interior);
            int id = i;

            interior.setContent(INTERIOR_PIN_CONTENT);
            interior.getStyleClass().add(PIN_INSIDE_STYLE);
            exterior.setContent(EXTERIOR_PIN_CONTENT);
            exterior.getStyleClass().add(PIN_OUTSIDE_STYLE);

            group.getStyleClass().add(PIN_STYLE);

            if (i == 0)
                group.getStyleClass().add(FIRST_STYLE);
            else if (i == waypoints.size() - 1)
                group.getStyleClass().add(LAST_STYLE);
            else
                group.getStyleClass().add(MIDDLE_STYLE);

            group.setOnMouseReleased(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    if (!e.isStillSincePress()) {
                        PointCh point = viewParams().getPointChFromPane(group.getLayoutX(),
                                group.getLayoutY());
                        int closestNode = getClosestNode(point);
                        if (closestNode == NO_NODE_CLOSEST) {
                            group.setLayoutX(viewParams().viewX(waypoint.point()));
                            group.setLayoutY(viewParams().viewY(waypoint.point()));
                        } else
                            waypoints.set(id, new Waypoint(point, closestNode));
                        return;
                    }
                    waypoints.remove(id);

                }
            });

            group.setOnMousePressed(e -> {
                if (e.getButton() == MouseButton.PRIMARY)
                    lastCursorPosition.set(group.localToParent(e.getX(), e.getY()));
            });

            group.setOnMouseDragged(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    Point2D parentPoint = group.localToParent(e.getX(), e.getY());
                    double xDiff = parentPoint.getX() - lastCursorPosition.get().getX();
                    double yDiff = parentPoint.getY() - lastCursorPosition.get().getY();
                    Point2D newPoint = new Point2D(group.getLayoutX(), group.getLayoutY())
                            .add(xDiff, yDiff);
                    group.setLayoutX(newPoint.getX());
                    group.setLayoutY(newPoint.getY());
                    lastCursorPosition.set(parentPoint);
                }
            });
            groups.add(group);
        }
        updatePositionsWaypoints();
    }

    /**
     * Méthode permettant de mettre à jour les positions des points de passage
     */
    private void updatePositionsWaypoints() {
        pane.getChildren().clear();
        for (int i = 0; i < waypoints.size(); i++) {
            Node group = groups.get(i);
            Waypoint waypoint = waypoints.get(i);
            group.setLayoutX(viewParams().viewX(waypoint.point()));
            group.setLayoutY(viewParams().viewY(waypoint.point()));

            pane.getChildren().add(group);
        }
    }

    /**
     * Méthode permettant d'obtenir la propriété JavaFX contenant les paramètres de la carte
     * 
     * @return la propriété JavaFX contenant les paramètres de la carte
     */
    private MapViewParameters viewParams() {
        return viewParams.get();
    }

    // Méthodes de construction (utilisées seulement dans le constructeur)

    /**
     * Méthode permettant de mettre en place le style du gestionnaire
     */
    private void setupStyle() {
        pane.setPickOnBounds(false);
    }

    /**
     * Méthode permettant de mettre en place les auditeurs
     */
    private void setupListeners() {
        waypoints.addListener((ListChangeListener<Waypoint>) (c -> createWaypoints()));
        viewParams.addListener(i -> updatePositionsWaypoints());
    }
}