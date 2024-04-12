package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;

/**
 * Enregistrement représentant un point de passage
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 * 
 * @param point point dans le système Suisse
 * @param id    identité du noeud JaVelo le plus proche de ce point de passage
 */
public record Waypoint(PointCh point, int id) {
}