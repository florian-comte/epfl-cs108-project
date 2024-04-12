package ch.epfl.javelo.data;

import java.nio.IntBuffer;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Q28_4;

/**
 * Enregistrement représentant des noeuds
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 * 
 * @param buffer mémoire tampon contenant la valeur des attributs de la totalité des noeuds du
 *               graphe
 */
public record GraphNodes(IntBuffer buffer) {
    private static final int OFFSET_E = 0;
    private static final int OFFSET_N = OFFSET_E + 1;
    private static final int OFFSET_OUT_EDGES = OFFSET_N + 1;
    private static final int NODE_INTS = OFFSET_OUT_EDGES + 1;
    private static final int DEGREE_FIRST_EXTRACT = 28;
    private static final int EDGE_FIRST_EXTRACT = 0;
    private static final int EXTRACT_lENGTH = 4;

    /**
     * Méthode permettant d'obtenir le nombre total de noeuds
     * 
     * @return le nombre total de noeuds
     */
    public int count() {
        return buffer.capacity() / NODE_INTS;
    }

    /**
     * Méthode permettant d'obtenir la coordonnée E du noeud d'identité donnée
     * 
     * @param nodeId identité du noeud
     * 
     * @return coordonnée E du noeud
     */
    public double nodeE(int nodeId) {
        return Q28_4.asDouble(getInBuffer(nodeId, OFFSET_E));
    }

    /**
     * Méthode permettant d'obtenir la coordonnée N du noeud d'identité donnée
     * 
     * @param nodeId identité du noeud
     * 
     * @return coordonnée N du noeud
     */
    public double nodeN(int nodeId) {
        return Q28_4.asDouble(getInBuffer(nodeId, OFFSET_N));
    }

    /**
     * Méthode permettant d'obtenir le nombre d'arêtes sortantes du noeud d'identité donnée
     * 
     * @param nodeId identité du noeud
     * 
     * @return nombre d'arêtes sortantes du noeud
     */
    public int outDegree(int nodeId) {
        return Bits.extractUnsigned(getInBuffer(nodeId, OFFSET_OUT_EDGES), DEGREE_FIRST_EXTRACT,
                EXTRACT_lENGTH);
    }

    /**
     * Méthode permettant d'obtenir l'identité de la edgeIndex-ième arête sortante du noeud
     * d'identité donnée
     * 
     * @param nodeId    identité du noeud
     * @param edgeIndex index de l'arête recherchée
     * 
     * @return l'identité de l'arête recherchée
     */
    public int edgeId(int nodeId, int edgeIndex) {
        return Bits.extractUnsigned(getInBuffer(nodeId, OFFSET_OUT_EDGES), EDGE_FIRST_EXTRACT,
                DEGREE_FIRST_EXTRACT) + edgeIndex;
    }

    /**
     * Méthode permettant d'obtenir l'entier d'un noeud d'identité donnée à un certain offset
     * 
     * @param nodeId identité du noeud
     * @param offset offset de l'entier cherché
     * 
     * @return l'entier du noeud recherché à un certain offset
     */
    private int getInBuffer(int nodeId, int offset) {
        return buffer.get(NODE_INTS * nodeId + offset);
    }
}