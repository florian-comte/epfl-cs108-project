package ch.epfl.javelo.gui;

import java.io.IOException;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.gui.TileManager.TileId;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;

/**
 * Classe représentant le gestionnaire d'affichage et d'interaction du fond de carte
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class BaseMapManager {
    private static final int TILE_SIZE = 256;
    private static final int ADDED_TIME_SCROLL = 200;
    private static final int MIN_ZOOM = 8;
    private static final int MAX_ZOOM = 19;

    private final TileManager tilesManager;
    private final WaypointsManager waypointsManager;
    private final Canvas canvas;
    private final Pane pane;
    private final ObjectProperty<MapViewParameters> viewParams;
    private boolean redrawNeeded;

    /**
     * Construit le gestionnaire d'affichage et d'interaction du fond de carte
     * 
     * @param tileManager      gestionnaire de tuiles
     * @param waypointsManager gestionnaire de points de passage
     * @param viewParams       propriété JavaFX contenant les paramètres de la carte
     */
    public BaseMapManager(TileManager tileManager, WaypointsManager waypointsManager,
            ObjectProperty<MapViewParameters> viewParams) {
        this.viewParams = viewParams;
        this.tilesManager = tileManager;
        this.waypointsManager = waypointsManager;
        this.canvas = new Canvas();
        this.pane = new Pane();

        setupStyle();
        setupBindings();
        setupListeners();
        setupHandlers();
    }

    /**
     * Méthode permettant d'obtenir le panneau JavaFX affichant le fond de carte
     * 
     * @return le panneau JavaFX affichant le fond de carte
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
     * Méthode permettant d'effectuer un redessin
     */
    private void redrawIfNeeded() {
        if (!redrawNeeded)
            return;
        redrawNeeded = false;

        GraphicsContext context = canvas.getGraphicsContext2D();
        double xTopLeft = viewParams().x();
        double yTopLeft = viewParams().y();
        double xBotRight = xTopLeft + canvas.getWidth();
        double yBotRight = yTopLeft + canvas.getHeight();

        int xLeftTile = (int) (xTopLeft / TILE_SIZE);
        int yLeftTile = (int) (yTopLeft / TILE_SIZE);
        int xRightTile = (int) (xBotRight / TILE_SIZE);
        int yRightTile = (int) (yBotRight / TILE_SIZE);

        for (int y = yLeftTile; y <= yRightTile; y++) {
            for (int x = xLeftTile; x <= xRightTile; x++) {
                if (TileId.isValid(viewParams().zoom(), x, y)) {
                    try {
                        Image img = tilesManager.imageForTileAt(new TileId(viewParams().zoom(), x, y));
                        context.drawImage(img, x * TILE_SIZE - xTopLeft, y * TILE_SIZE - yTopLeft);
                    } catch (IOException e) {
                        // pas d'impression de tuile
                    }
                }
            }
        }

    }

    /**
     * Méthode permettant de demander un redessin au prochain battement
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    // Méthodes de construction (utilisées seulement dans le constructeur)

    /**
     * Méthode permettant de mettre en place le style du gestionnaire
     */
    private void setupStyle() {
        pane.getChildren().add(canvas);
    }

    /**
     * Méthode permettant de mettre en place les gestionnaires d'événements
     */
    private void setupHandlers() {
        SimpleLongProperty minScrollTime = new SimpleLongProperty();
        ObjectProperty<Point2D> lastCursorPosition = new SimpleObjectProperty<>();

        pane.setOnScroll(e -> {
            if (e.getDeltaY() == 0d)
                return;
            // limitation de la frequence de changement de niveau de zoom
            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get())
                return;
            minScrollTime.set(currentTime + ADDED_TIME_SCROLL);
            // delta du zoom (+1 lors du zoom, -1 lors du dézoom)
            double zoomDelta = Math.signum(e.getDeltaY());

            int newZoom = Math2.clamp(MIN_ZOOM, (int) Math.round(viewParams().zoom() + zoomDelta), MAX_ZOOM);

            // translation du point du curseur de la souris au point haut-gauche
            PointWebMercator newTopLeft = viewParams().pointAt(e.getX(), e.getY());

            // zoom puis translation inverse
            viewParams.setValue(
                    new MapViewParameters(newZoom, newTopLeft.xAtZoomLevel(newZoom) - e.getX(),
                            newTopLeft.yAtZoomLevel(newZoom) - e.getY()));
        });

        pane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY)
                if (e.isStillSincePress())
                    waypointsManager.addWaypoint(e.getX(), e.getY());
        });

        pane.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY)
                lastCursorPosition.set(new Point2D(e.getX(), e.getY()));
        });

        pane.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                Point2D newPoint = viewParams().topLeft().add(lastCursorPosition.get().getX() - e.getX(),
                        lastCursorPosition.get().getY() - e.getY());
                viewParams.set(viewParams().withMinXY(newPoint.getX(), newPoint.getY()));
                lastCursorPosition.set(new Point2D(e.getX(), e.getY()));
            }
        });
    }

    /**
     * Méthode permettant de mettre en place les auditeurs
     */
    private void setupListeners() {
        canvas.sceneProperty().addListener((p, o, n) -> {
            assert o == null;
            n.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        canvas.widthProperty().addListener(i -> redrawOnNextPulse());
        canvas.heightProperty().addListener(i -> redrawOnNextPulse());
        viewParams.addListener(i -> redrawOnNextPulse());
    }

    /**
     * Méthode permettant de créer les liens
     */
    private void setupBindings() {
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
    }
}