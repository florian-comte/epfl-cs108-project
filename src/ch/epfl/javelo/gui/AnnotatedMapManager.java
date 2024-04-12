package ch.epfl.javelo.gui;

import java.util.function.Consumer;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Classe représentant le gestionnaire de l'affichage de la carte annotée
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class AnnotatedMapManager {
    private static final String MAP_SHEET_CSS = "map.css";
    private static final int INITIAL_X = 543200;
    private static final int INITIAL_Y = 370650;
    private static final int INITIAL_ZOOM = 12;
    private static final int MAX_DISTANCE_HIGHLIGHT = 15;
    private static final Point2D MOUSE_NOT_ON_PANE = new Point2D(Double.NaN, Double.NaN);

    private final BaseMapManager baseMapManager;
    private final WaypointsManager waypointsManager;
    private final RouteManager routeManager;
    private final RouteBean bean;

    private final ObjectProperty<MapViewParameters> mapViewParameters;
    private final ObjectProperty<Point2D> mousePosition;
    private final DoubleProperty positionHighlight;

    private final StackPane pane;

    /**
     * Construit le gestionnaire de l'affichage de la carte annotée
     * 
     * @param graph       graphe de l'itinéraire
     * @param tileManager gestionnaire de tuiles
     * @param bean        bean de la route
     * @param error       consommateur d'erreurs
     */
    public AnnotatedMapManager(Graph graph, TileManager tileManager, RouteBean bean,
            Consumer<String> error) {
        this.mapViewParameters = new SimpleObjectProperty<MapViewParameters>(
                new MapViewParameters(INITIAL_ZOOM, INITIAL_X, INITIAL_Y));
        this.waypointsManager = new WaypointsManager(graph, mapViewParameters, bean.waypoints(),
                error);
        this.baseMapManager = new BaseMapManager(tileManager, waypointsManager, mapViewParameters);
        this.routeManager = new RouteManager(bean, mapViewParameters);
        this.pane = new StackPane(baseMapManager.pane(), waypointsManager.pane(),
                routeManager.pane());
        this.mousePosition = new SimpleObjectProperty<>(MOUSE_NOT_ON_PANE);
        this.bean = bean;
        this.positionHighlight = new SimpleDoubleProperty(Double.NaN);

        setupStyle();
        setupHandlers();
        setupBindings();
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
     * Méthode permettant d'obtenir la propriété contenant la position du pointeur de la souris le
     * long de l'itinéraire
     * 
     * @return la propriété contenant la position du pointeur de la souris le long de l'itinéraire
     */
    public DoubleProperty mousePositionOnRouteProperty() {
        return positionHighlight;
    }

    // Méthodes de construction (utilisées seulement dans le constructeur)

    /**
     * Méthode permettant de mettre en place le style du gestionnaire
     */
    private void setupStyle() {
        pane.getStylesheets().add(MAP_SHEET_CSS);
    }

    /**
     * Méthode permettant de mettre en place les liens
     */
    private void setupBindings() {
        positionHighlight.bind(Bindings.createDoubleBinding(() -> {
            if (bean.route() == null || mousePosition.get() == MOUSE_NOT_ON_PANE)
                return Double.NaN;

            PointCh pointed = mapViewParameters.get().getPointChFromPane(mousePosition.get());
            if (pointed == null)
                return Double.NaN;

            RoutePoint routePoint = bean.route().pointClosestTo(pointed);
            double distance = mousePosition.get().distance(
                    mapViewParameters.get().viewX(routePoint.point()),
                    mapViewParameters.get().viewY(routePoint.point()));
            if (distance <= MAX_DISTANCE_HIGHLIGHT)
                return routePoint.position();
            else
                return Double.NaN;

        }, mousePosition, mapViewParameters, bean.routeProperty()));
    }

    /**
     * Méthode permettant de mettre en place les gestionnaires d'événements
     */
    private void setupHandlers() {
        pane.setOnMouseMoved(e -> mousePosition.set(new Point2D(e.getX(), e.getY())));
        pane.setOnMouseExited(e -> mousePosition.set(MOUSE_NOT_ON_PANE));
    }
}