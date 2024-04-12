package ch.epfl.javelo.gui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.CityBikeCF;
import ch.epfl.javelo.routing.GpxGenerator;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Classe principale de l'application JaVelo
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class JaVelo extends Application {
    private static final String SUBMENU_NAME = "Exporter GPX";
    private static final String MENU_NAME = "Fichier";
    private static final String OSM_HOST = "tile.openstreetmap.org";
    private static final String CACHE_DIRECTORY = "osm-cache";
    private static final String GRAPH_DIRECTORY = "javelo-data";
    private static final String GPX_EXPORT_NAME = "javelo.gpx";
    private static final String APP_NAME = "JaVelo";
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;
    private static final int MIN_MOUSE_POSITION_ON_ROUTE = 0;

    /**
     * Méthode principale de l'application
     * 
     * @param args les arguments de la ligne de commande.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Graph graph = Graph.loadFrom(Path.of(GRAPH_DIRECTORY));
        RouteBean bean = new RouteBean(new RouteComputer(graph, new CityBikeCF(graph)));

        TileManager tileManager = new TileManager(Path.of(CACHE_DIRECTORY), OSM_HOST);
        ElevationProfileManager profileManager = new ElevationProfileManager(
                bean.elevationProfileProperty(), bean.highlightedPositionProperty());
        ErrorManager errorManager = new ErrorManager();
        AnnotatedMapManager mapManager = new AnnotatedMapManager(graph, tileManager, bean,
                errorManager::displayError);

        SplitPane splitPane = new SplitPane(mapManager.pane());
        StackPane stackPane = new StackPane(splitPane, errorManager.pane());
        BorderPane borderPane = new BorderPane();

        Menu menu = new Menu(MENU_NAME);
        MenuBar menuBar = new MenuBar(menu);
        MenuItem item = new MenuItem(SUBMENU_NAME);

        menu.getItems().add(item);
        item.disableProperty().bind(bean.routeProperty().isNull());

        SplitPane.setResizableWithParent(profileManager.pane(), false);
        splitPane.setOrientation(Orientation.VERTICAL);

        borderPane.setCenter(stackPane);
        borderPane.setTop(menuBar);

        item.setOnAction(e -> {
            try {
                GpxGenerator.writeGpx(GPX_EXPORT_NAME, bean.route(), bean.elevationProfile());
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        });

        bean.elevationProfileProperty().addListener((p, o, n) -> {
            if (n != null) {
                if (!splitPane.getItems().contains(profileManager.pane()))
                    splitPane.getItems().add(profileManager.pane());
            } else if (splitPane.getItems().contains(profileManager.pane()))
                splitPane.getItems().remove(profileManager.pane());
        });

        bean.highlightedPositionProperty()
                .bind(Bindings
                        .when(mapManager.mousePositionOnRouteProperty()
                                .greaterThanOrEqualTo(MIN_MOUSE_POSITION_ON_ROUTE))
                        .then(mapManager.mousePositionOnRouteProperty())
                        .otherwise(profileManager.mousePositionOnProfileProperty()));

        primaryStage.setTitle(APP_NAME);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setScene(new Scene(borderPane));
        primaryStage.show();
    }
}