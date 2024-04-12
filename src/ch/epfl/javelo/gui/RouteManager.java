package ch.epfl.javelo.gui;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.Route;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;

/**
 * Classe représentant le gestionnaire de l'affichage de l'itinéraire
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class RouteManager {
    private static final String LINE_ID = "route";
    private static final String DISK_ID = "highlight";
    private static final int DISK_RADIUS = 5;

    private final Pane pane;
    private final Polyline line;
    private final Circle disk;
    private final RouteBean bean;
    private final ReadOnlyObjectProperty<MapViewParameters> viewParams;

    /**
     * Construit le gestionnaire de l'affichage de l'itinéraire
     * 
     * @param bean       bean de l'itinéraire
     * @param viewParams propriété JavaFX de la vue
     */
    public RouteManager(RouteBean bean, ReadOnlyObjectProperty<MapViewParameters> viewParams) {
        this.pane = new Pane();
        this.line = new Polyline();
        this.disk = new Circle();
        this.bean = bean;
        this.viewParams = viewParams;

        setupStyle();
        setupListeners();
        setupHandlers();
    }

    /**
     * Méthode permettant d'obtenir le panneau JavaFX affichant l'itinéraire
     * 
     * @return le panneau JavaFX affichant l'itinéraire
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Méthode permettant d'obtenir la propriété JavaFX contenant les paramètres de la carte
     * 
     * @return la propriété JavaFX contenant les paramètres de la carte
     */
    private MapViewParameters viewParams() {
        return viewParams.get();
    }

    /**
     * Méthode permettant de créer la ligne représentant l'itinéraire
     */
    private void createLine() {
        Route r = bean.route();

        if (r == null) {
            line.setVisible(false);
            return;
        }

        List<Double> coords = new ArrayList<>();
        for (PointCh point : r.points()) {
            coords.add(PointWebMercator.ofPointCh(point).xAtZoomLevel(viewParams().zoom()));
            coords.add(PointWebMercator.ofPointCh(point).yAtZoomLevel(viewParams().zoom()));
        }

        line.getPoints().setAll(coords);
        line.setVisible(true);

        positionLine();
    }

    /**
     * Méthode permettant de positionner la ligne représentant l'itinéraire
     * 
     * @param newValue le nouveau point haut-gauche
     */
    private void positionLine() {
        if (bean.route() == null)
            return;
        line.setLayoutX(-viewParams().x());
        line.setLayoutY(-viewParams().y());
    }

    /**
     * Méthode permettant de verifier si le disque doit être visible
     */
    private void checkDisk() {
        Route r = bean.route();
        if (r == null || Double.isNaN(bean.highlightedPosition())) {
            disk.setVisible(false);
            return;
        }

        PointCh point = r.pointAt(bean.highlightedPosition());

        disk.setVisible(true);
        disk.setLayoutX(viewParams().viewX(point));
        disk.setLayoutY(viewParams().viewY(point));
    }

    // Méthodes de construction (utilisées seulement dans le constructeur)

    /**
     * Méthode permettant de mettre en place le style du gestionnaire
     */
    private void setupStyle() {
        pane.setPickOnBounds(false);
        pane.getChildren().add(line);
        pane.getChildren().add(disk);

        line.setId(LINE_ID);
        disk.setId(DISK_ID);
        disk.setRadius(DISK_RADIUS);
        disk.setVisible(false);
    }

    /**
     * Méthode permettant de mettre en place les gestionnaires d'événements
     */
    private void setupHandlers() {
        disk.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                PointCh point = viewParams()
                        .getPointChFromPane(disk.localToParent(e.getX(), e.getY()));

                Route route = bean.route();
                double position = bean.highlightedPosition();
                int closestNode = route.nodeClosestTo(position);

                bean.waypoints().add(bean.indexOfNonEmptySegmentAt(position) + 1,
                        new Waypoint(point, closestNode));
            }
        });
    }

    /**
     * Méthode permettant de mettre en place les auditeurs
     */
    private void setupListeners() {
        viewParams.addListener((p, o, n) -> {
            checkDisk();
            if (o.zoom() != n.zoom())
                createLine();
            else
                positionLine();
        });

        bean.routeProperty().addListener(i -> {
            createLine();
            checkDisk();
        });

        bean.highlightedPositionProperty().addListener(i -> checkDisk());
    }
}