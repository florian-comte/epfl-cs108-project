package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.ElevationProfile;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;

/**
 * Classe représentant le gestionnaire d'affichage du profil en long de l'itinéraire, et
 * l'interaction avec lui
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class ElevationProfileManager {
    private static final int METERS_OF_ONE_KILOMETER = 1000;
    private static final int[] POSITION_STEPS = { 1000, 2000, 5000, 10_000, 25_000, 50_000,
            100_000 };
    private static final int[] ELEVATION_STEPS = { 5, 10, 20, 25, 50, 100, 200, 250, 500, 1_000 };
    private static final int PADDING_TOP = 10;
    private static final int PADDING_BOTTOM = 20;
    private static final int PADDING_RIGHT = 10;
    private static final int PADDING_LEFT = 40;
    private static final int HORIZONTAL_PADDING = PADDING_RIGHT + PADDING_LEFT;
    private static final int VERTICAL_PADDING = PADDING_BOTTOM + PADDING_TOP;
    private static final int MIN_X_PROFILE = 0;
    private static final int MIN_DISTANCE_ELEVATION = 25;
    private static final int MIN_DISTANCE_POSITION = 50;
    private static final int HORIZONTAL_LABEL_SHIFT = 2;

    private static final double NOT_ON_RECTANGLE = Double.NaN;

    private static final Font FONT = Font.font("Avenir", 10);
    private static final String BORDERPANE_STYLE = "elevation_profile.css";
    private static final String VBOX_ID = "profile_data";
    private static final String PATH_ID = "grid";
    private static final String POLYGON_ID = "profile";
    private static final String LABEL_STYLE = "grid_label";
    private static final String HORIZONTAL_STYLE = "horizontal";
    private static final String VERTICAL_STYLE = "vertical";
    private static final String MIN_Y_PROPERTY = "minY";
    private static final String MAX_Y_PROPERTY = "maxY";
    private static final String STATS_FORMAT = "Longueur : %.1f km" + "     Montée : %.0f m"
            + "     Descente : %.0f m" + "     Altitude : de %.0f m à %.0f m";

    private final ReadOnlyDoubleProperty highlightPosition;
    private final DoubleProperty cursorX;
    private final ReadOnlyObjectProperty<ElevationProfile> profile;
    private final ObjectProperty<Rectangle2D> rectangle;
    private final ObjectProperty<Transform> screenToWorld;
    private final ObjectProperty<Transform> worldToScreen;

    private final BorderPane borderPane;
    private final Pane pane;
    private final Path path;
    private final Group group;
    private final Polygon polygon;
    private final Line line;
    private final VBox vBox;
    private final Text stats;

    /**
     * Construit le gestionnaire d'affichage du profil en long de l'itinéraire, et l'interaction
     * avec lui
     * 
     * @param profile  propriété en lecture seule du profil le long de l'itinéraire
     * @param position position mise en évidence
     */
    public ElevationProfileManager(ReadOnlyObjectProperty<ElevationProfile> profile,
            ReadOnlyDoubleProperty position) {
        this.cursorX = new SimpleDoubleProperty(NOT_ON_RECTANGLE);
        this.rectangle = new SimpleObjectProperty<>(Rectangle2D.EMPTY);
        this.screenToWorld = new SimpleObjectProperty<>(new Affine());
        this.worldToScreen = new SimpleObjectProperty<>(new Affine());
        this.highlightPosition = position;
        this.profile = profile;
        this.pane = new Pane();
        this.borderPane = new BorderPane();
        this.group = new Group();
        this.vBox = new VBox();
        this.polygon = new Polygon();
        this.path = new Path();
        this.stats = new Text();
        this.line = new Line();

        setupStyle();
        setupHandlers();
        setupListeners();
        setupBindings();
    }

    /**
     * Méthode permettant d'obtenir le panneau JavaFX affichant le profil
     * 
     * @return le panneau JavaFX affichant le profil
     */
    public Pane pane() {
        return borderPane;
    }

    /**
     * Méthode permettant d'obtenir la position du pointeur de la souris le long du profil
     * 
     * @return une propriété en lecture seule contenant la position du pointeur de la souris le long
     *         du profil (en mètres, arrondie à l'entier le plus proche), ou NaN si le pointeur de
     *         la souris ne se trouve pas au-dessus du profil
     */
    public ReadOnlyDoubleProperty mousePositionOnProfileProperty() {
        return cursorX;
    }

    /**
     * Méthode permettant de mettre à jour l'affichage du profil
     */
    private void updateProfile() {
        polygon.getPoints().clear();
        path.getElements().clear();
        group.getChildren().clear();

        if (profile.get() != null) {
            // définition ici pour éviter le re-calcul dans chaque méthode
            double rectangleHeightInScreen = rectangleHeight() + PADDING_TOP;
            double rectangleWidthInScreen = rectangleWidth() + PADDING_LEFT;
            updateTransform();
            updatePolygon(rectangleHeightInScreen, rectangleWidthInScreen);
            updateGrid(rectangleHeightInScreen, rectangleWidthInScreen);
            updateStats();
        }
    }

    /**
     * Méthode permettant de mettre à jour le polygone
     * 
     * @param rectangleHeightInScreen hauteur du rectangle dans le point du vue du panneau
     * @param rectangleWidthInScreen  largeur du rectangle dans le point de vue du panneau
     */
    private void updatePolygon(double rectangleHeightInScreen, double rectangleWidthInScreen) {

        // ajout du point en bas à gauche du rectangle
        polygon.getPoints().add((double) PADDING_LEFT);
        polygon.getPoints().add(rectangleHeightInScreen);

        for (int i = PADDING_LEFT; i <= rectangleWidthInScreen; i++) {
            double worldElevation = profile.get().elevationAt(screenToWorldX(i));
            double screenElevation = worldToScreenY(worldElevation);

            polygon.getPoints().add((double) i);
            polygon.getPoints().add(screenElevation);
        }

        // ajout du point en bas à droite du rectangle
        polygon.getPoints().add(rectangleWidthInScreen);
        polygon.getPoints().add(rectangleHeightInScreen);
    }

    /**
     * Méthode permettant de mettre à jour la grille
     * 
     * @param rectangleHeightInScreen hauteur du rectangle dans le point du vue du panneau
     * @param rectangleWidthInScreen  largeur du rectangle dans le point de vue du panneau
     */
    private void updateGrid(double rectangleHeightInScreen, double rectangleWidthInScreen) {
        // espace entre les lignes horizontales
        int positionsSpace = positionsSpace();
        for (int i = 0; i <= profile.get().length(); i = i + positionsSpace) {
            double screenX = worldToScreenX(i);
            path.getElements().addAll(new MoveTo(screenX, PADDING_TOP),
                    new LineTo(screenX, rectangleHeightInScreen));

            Text label = new Text(Integer.toString((int) mToKm(i)));
            label.setFont(FONT);
            label.getStyleClass().addAll(LABEL_STYLE, HORIZONTAL_STYLE);
            label.setLayoutX(screenX - label.prefWidth(0) / 2);
            label.setLayoutY(rectangleHeightInScreen);
            label.setTextOrigin(VPos.TOP);
            group.getChildren().add(label);
        }

        // espace entre les lignes verticales
        int elevationsSpace = elevationsSpace();
        // calcul du premier multiple de l'espace entre les lignes au dessus de l'altitude minimal
        int firstElevation = (int) (minElevation()
                + (elevationsSpace - minElevation() % elevationsSpace));

        for (int i = firstElevation; i <= maxElevation(); i = i + elevationsSpace) {
            double yScreen = worldToScreenY(i);
            path.getElements().addAll(new MoveTo(PADDING_LEFT, yScreen),
                    new LineTo(rectangleWidthInScreen, yScreen));

            Text label = new Text(Integer.toString((int) i));
            label.setFont(FONT);
            label.getStyleClass().addAll(LABEL_STYLE, VERTICAL_STYLE);
            label.setTextOrigin(VPos.CENTER);
            label.setLayoutX(PADDING_LEFT - label.prefWidth(0) - HORIZONTAL_LABEL_SHIFT);
            label.setLayoutY(yScreen);
            group.getChildren().add(label);
        }
    }

    /**
     * Méthode permettant de mettre à jour les statistiques
     */
    private void updateStats() {
        stats.setText(
                String.format(STATS_FORMAT, mToKm(profileLength()), profile.get().totalAscent(),
                        profile.get().totalDescent(), minElevation(), maxElevation()));
    }

    /**
     * Méthode permettant de mettre à jour la fonction de transformation
     */
    private void updateTransform() {
        Affine affine = new Affine();

        double scaleX = profileLength() / rectangleWidth();
        double scaleY = (maxElevation() - minElevation()) / rectangleHeight();

        affine.prependTranslation(-PADDING_LEFT, -PADDING_TOP);
        affine.prependScale(scaleX, -scaleY);
        affine.prependTranslation(MIN_X_PROFILE, maxElevation());

        screenToWorld.set(affine);
        try {
            worldToScreen.set(screenToWorld.get().createInverse());
        } catch (NonInvertibleTransformException error) {
            // n'est censé jamais arriver
            throw new Error();
        }
    }

    /**
     * Méthode permettant de mettre à jour le rectangle
     */
    private void updateRectangle() {
        double paneWidthWithoutPadding = pane.getWidth() - HORIZONTAL_PADDING;
        double paneHeightWithoutPadding = pane.getHeight() - VERTICAL_PADDING;
        if (paneWidthWithoutPadding > 0 && paneHeightWithoutPadding > 0)
            rectangle.set(new Rectangle2D(PADDING_LEFT, PADDING_TOP, paneWidthWithoutPadding,
                    paneHeightWithoutPadding));
    }

    /**
     * Méthode permettant d'obtenir l'espace entre les lignes horizontales
     * 
     * @return espace entre les lignes horizontales
     */
    private int positionsSpace() {
        for (int pos : POSITION_STEPS) {
            if (worldToScreenDeltaX(pos) >= MIN_DISTANCE_POSITION) {
                return pos;
            }
        }
        return POSITION_STEPS[POSITION_STEPS.length - 1];
    }

    /**
     * Méthode permettant d'obtenir l'espace entre les lignes verticales
     * 
     * @return espace entre les lignes verticales
     */
    private int elevationsSpace() {
        for (int ele : ELEVATION_STEPS) {
            if (worldToScreenDeltaY(-ele) >= MIN_DISTANCE_ELEVATION) {
                return ele;
            }
        }
        return ELEVATION_STEPS[ELEVATION_STEPS.length - 1];
    }

    /**
     * Méthode permettant d'obtenir la largeur du rectangle
     * 
     * @return largeur du rectangle
     */
    private double rectangleWidth() {
        return rectangle.get().getWidth();
    }

    /**
     * Méthode permettant d'obtenir la hauteur du rectangle
     * 
     * @return hauteur du rectangle
     */
    private double rectangleHeight() {
        return rectangle.get().getHeight();
    }

    /**
     * Méthode permettant d'obtenir l'altitude minimal
     * 
     * @return altitude minimal
     */
    private double minElevation() {
        return profile.get().minElevation();
    }

    /**
     * Méthode permettant d'obtenir l'altitude maximal
     * 
     * @return altitude maximal
     */
    private double maxElevation() {
        return profile.get().maxElevation();
    }

    /**
     * Méthode permettant d'obtenir la longueur du profil
     * 
     * @return la longueur du profil
     */
    private double profileLength() {
        return profile.get().length();
    }

    /**
     * Méthode permettant de convertir la coordonnée x d'un point dans le système du monde réel dans
     * le système du panneau
     * 
     * @param x coordonnée x
     * 
     * @return coordonnée x convertie dans le système du panneau
     */
    private double worldToScreenX(double x) {
        return worldToScreen.get().transform(x, 0).getX();
    }

    /**
     * Méthode permettant de convertir la coordonnée x d'un point dans le système du panneau dans le
     * système du monde réel
     * 
     * @param x coordonnée x
     * 
     * @return coordonnée x convertie dans le système du monde réel
     */
    private double screenToWorldX(double x) {
        return screenToWorld.get().transform(x, 0).getX();
    }

    /**
     * Méthode permettant de convertir la coordonnée y d'un point dans le système du monde réel dans
     * le système du panneau
     * 
     * @param y coordonnée y
     * 
     * @return coordonnée y convertie dans le système du panneau
     */
    private double worldToScreenY(double y) {
        return worldToScreen.get().transform(0, y).getY();
    }

    /**
     * Méthode permettant de convertir un point dans le système du panneau dans le système du monde
     * réel
     * 
     * @param x coordonnée x
     * @param y coordonnée y
     * 
     * @return point converti dans le système du monde réel
     */
    private Point2D screenToWorld(double x, double y) {
        return screenToWorld.get().transform(x, y);
    }

    /**
     * Méthode permettant de convertir la coordonnée x d'un vecteur dans le système du monde réel
     * dans le système du panneau
     * 
     * @param x coordonnée x
     * 
     * @return coordonnée x convertie dans le système du panneau
     */
    private double worldToScreenDeltaX(double x) {
        return worldToScreen.get().deltaTransform(x, 0).getX();
    }

    /**
     * Méthode permettant de convertir la coordonnée y d'un vecteur dans le système du monde réel
     * dans le système du panneau
     * 
     * @param y coordonnée y
     * 
     * @return coordonnée y convertie dans le système du panneau
     */
    private double worldToScreenDeltaY(double y) {
        return worldToScreen.get().deltaTransform(0, y).getY();
    }

    /**
     * Méthode permettant de savoir si un point dans le système du monde réel est dans le rectangle
     * 
     * @param point point à vérifier
     * 
     * @return TRUE si le point est dans le rectangle, FALSE sinon
     */
    private boolean isInRectangle(Point2D point) {
        return point.getY() >= minElevation() && point.getY() <= maxElevation()
                && point.getX() >= MIN_X_PROFILE && point.getX() <= profileLength();
    }

    /**
     * Méthode permettant de passer de mètres à kilomètres
     * 
     * @param meters valeur en mètres
     * 
     * @return valeur en kilomètres
     */
    private double mToKm(double meters) {
        return meters / METERS_OF_ONE_KILOMETER;
    }

    // Méthodes de construction (utilisées seulement dans le constructeur)

    /**
     * Méthode permettant de mettre en place le style du gestionnaire
     */
    private void setupStyle() {
        borderPane.getStylesheets().add(BORDERPANE_STYLE);
        borderPane.setCenter(pane);
        borderPane.setBottom(vBox);

        pane.setPadding(new Insets(PADDING_TOP, PADDING_RIGHT, PADDING_BOTTOM, PADDING_LEFT));
        pane.getChildren().add(path);
        pane.getChildren().add(group);
        pane.getChildren().add(polygon);
        pane.getChildren().add(line);

        vBox.getChildren().add(stats);

        vBox.setId(VBOX_ID);
        path.setId(PATH_ID);
        polygon.setId(POLYGON_ID);
    }

    /**
     * Méthode permettant de créer les liens
     */
    private void setupBindings() {
        line.layoutXProperty().bind(Bindings.createDoubleBinding(
                () -> worldToScreenX(highlightPosition.get()), highlightPosition));
        line.startYProperty().bind(Bindings.select(rectangle, MIN_Y_PROPERTY));
        line.endYProperty().bind(Bindings.select(rectangle, MAX_Y_PROPERTY));
        line.visibleProperty().bind(highlightPosition.greaterThanOrEqualTo(MIN_X_PROFILE));
    }

    /**
     * Méthode permettant de mettre en place les gestionnaires d'événements
     */
    private void setupHandlers() {
        pane.setOnMouseMoved(e -> {
            Point2D worldPoint = screenToWorld(e.getX(), e.getY());
            if (isInRectangle(worldPoint))
                cursorX.set(worldPoint.getX());
            else
                cursorX.set(NOT_ON_RECTANGLE);
        });

        pane.setOnMouseExited(e -> {
            cursorX.set(NOT_ON_RECTANGLE);
        });
    }

    /**
     * Méthode permettant de mettre en place les auditeurs
     */
    private void setupListeners() {
        profile.addListener(i -> updateProfile());
        pane.widthProperty().addListener(i -> updateRectangle());
        pane.heightProperty().addListener(i -> updateRectangle());
        rectangle.addListener(i -> updateProfile());
    }
}
