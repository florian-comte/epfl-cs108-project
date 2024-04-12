package ch.epfl.javelo.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

/**
 * Enregistrement représentant des secteurs
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 * 
 * @param buffer mémoire tampon contenant la valeur des attributs de la totalité des secteurs
 */
public record GraphSectors(ByteBuffer buffer) {
    private static final int OFFSET_FIRST_NODE_ID = 0;
    private static final int OFFSET_NODES_NUMBER = OFFSET_FIRST_NODE_ID + Integer.BYTES;
    private static final int SECTOR_INTS = OFFSET_NODES_NUMBER + Short.BYTES;
    private static final int MAX_SECTORS = 128;
    private static final double X_SECTOR_SIZE = SwissBounds.WIDTH / MAX_SECTORS;
    private static final double Y_SECTOR_SIZE = SwissBounds.HEIGHT / MAX_SECTORS;

    /**
     * Enregistrement représentant un secteur
     * 
     * @param startNodeId identité du premier noeud du secteur
     * @param endNodeId   identité du dernier noeud du secteur
     */
    public record Sector(int startNodeId, int endNodeId) {
    }

    /**
     * Méthode permettant d'obtenir la liste de tous les secteurs ayant une intersection avec le
     * carré centré au point donné et de côte égal au double de la distance donnée
     * 
     * @param center   point du centre dans le système Suisse
     * @param distance moitié du côté du carré
     * 
     * @return liste des secteurs ayant une intersection avec le carré
     */
    public List<Sector> sectorsInArea(PointCh center, double distance) {
        List<Sector> sectors = new ArrayList<GraphSectors.Sector>();

        // valeurs brutes
        double xLeftCorner = center.e() - distance;
        double yLeftCorner = center.n() - distance;

        double xRightCorner = center.e() + distance;
        double yRightCorner = center.n() + distance;

        // valeurs comprises dans les limites suisses
        int xMin = Math2.clamp(0, (int) ((xLeftCorner - SwissBounds.MIN_E) / X_SECTOR_SIZE),
                MAX_SECTORS - 1);
        int yMin = Math2.clamp(0, (int) ((yLeftCorner - SwissBounds.MIN_N) / Y_SECTOR_SIZE),
                MAX_SECTORS - 1);

        int xMax = Math2.clamp(0, (int) ((xRightCorner - SwissBounds.MIN_E) / X_SECTOR_SIZE),
                MAX_SECTORS - 1);
        int yMax = Math2.clamp(0, (int) ((yRightCorner - SwissBounds.MIN_N) / Y_SECTOR_SIZE),
                MAX_SECTORS - 1);

        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                int sectorId = x + MAX_SECTORS * y;
                int nodeId = getNodeIdInBuffer(sectorId);
                sectors.add(new Sector(nodeId, nodeId + getNumberNodes(sectorId)));
            }
        }
        return sectors;
    }

    /**
     * Méthode permettant d'obtenir l'identité du noeud d'un secteur
     * 
     * @param sectorId identité du secteur
     * 
     * @return identité du noeud
     */
    private int getNodeIdInBuffer(int sectorId) {
        return buffer.getInt(SECTOR_INTS * sectorId + OFFSET_FIRST_NODE_ID);
    }

    /**
     * Méthode permettant d'obtenir le nombre de noeuds
     * 
     * @param sectorId identité du secteur
     * 
     * @return nombre de noeuds
     */
    private int getNumberNodes(int sectorId) {
        return Short.toUnsignedInt(buffer.getShort(SECTOR_INTS * sectorId + OFFSET_NODES_NUMBER));
    }
}