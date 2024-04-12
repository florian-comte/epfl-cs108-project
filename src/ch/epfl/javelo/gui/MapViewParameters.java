package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

/**
 * Enregistrement représentant les paramètres du fond de carte
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 * 
 * @param zoom niveau de zoom
 * @param x    coordonnée x du coin haut-gauche
 * @param y    coordonnée y du coin haut-gauche
 */
public record MapViewParameters(int zoom, double x, double y) {

    /**
     * Méthode permettant d'obtenir le point du coin haut-gauche
     * 
     * @return le point du coin haut-gauche
     */
    public Point2D topLeft() {
        return new Point2D(x, y);
    }

    /**
     * Méthode permettant d'obtenir une instance de MapViewParameters de coordonnées passées en
     * argument mais de même zoom
     * 
     * @param x coordonnée x
     * @param y coordonnée y
     * 
     * @return instance de MapViewParameters
     */
    public MapViewParameters withMinXY(double x, double y) {
        return new MapViewParameters(zoom, x, y);
    }

    /**
     * Méthode permettant d'obtenir un point sous la forme d'un PointWebMercator
     * 
     * @param x position x exprimée par rapport au coin haut-gauche
     * @param y position y exprimée par rapport au coin haut-gauche
     * 
     * @return instance de PointWebMercator du point souhaité
     */
    public PointWebMercator pointAt(double x, double y) {
        return PointWebMercator.of(zoom, this.x + x, this.y + y);
    }

    /**
     * Méthode permettant d'obtenir la position x d'un point
     * 
     * @param point point dans le système WebMercator
     * 
     * @return la position x exprimée par rapport au coin haut-gauche
     */
    public double viewX(PointWebMercator point) {
        return point.xAtZoomLevel(zoom) - x;
    }

    /**
     * Méthode permettant d'obtenir la position x d'un point
     * 
     * @param point point dans le système Suisse
     * 
     * @return la position x exprimée par rapport au coin haut-gauche
     */
    public double viewX(PointCh point) {
        return viewX(PointWebMercator.ofPointCh(point));
    }

    /**
     * Méthode permettant d'obtenir la position y d'un point
     * 
     * @param point point dans le système WebMercator
     * 
     * @return la position y exprimée par rapport au coin haut-gauche
     */
    public double viewY(PointWebMercator point) {
        return point.yAtZoomLevel(zoom) - y;
    }

    /**
     * Méthode permettant d'obtenir la position y d'un point
     * 
     * @param point point dans le système Suisse
     * 
     * @return la position y exprimée par rapport au coin haut-gauche
     */
    public double viewY(PointCh point) {
        return viewY(PointWebMercator.ofPointCh(point));
    }

    /**
     * Méthode permettant de récupérer un point dans le système Suisse
     * 
     * @param x coordonnée x du point dans le panneau
     * @param y coordonnée y du point dans le panneau
     * 
     * @return le point dans le système Suisse
     */
    public PointCh getPointChFromPane(double x, double y) {
        return pointAt(x, y).toPointCh();
    }

    /**
     * Méthode permettant de récupérer un point dans le système Suisse
     * 
     * @param point point souhaité
     * 
     * @return le point dans le système Suisse
     */
    public PointCh getPointChFromPane(Point2D point) {
        return getPointChFromPane(point.getX(), point.getY());
    }
}