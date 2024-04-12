package ch.epfl.javelo.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.data.GraphSectors.Sector;
import ch.epfl.javelo.projection.PointCh;

/**
 * Classe représentant le graphe JaVelo
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class Graph {
    private final GraphNodes nodes;
    private final GraphSectors sectors;
    private final GraphEdges edges;
    private final List<AttributeSet> attributeSets;

    /**
     * Construit un graphe JaVelo
     * 
     * @param nodes         noeuds du graphe
     * @param sectors       secteurs du graphe
     * @param edges         arêtes du graphe
     * @param attributeSets ensemble d'attributs du graphe
     */
    public Graph(GraphNodes nodes, GraphSectors sectors, GraphEdges edges,
            List<AttributeSet> attributeSets) {
        this.nodes = nodes;
        this.sectors = sectors;
        this.edges = edges;
        this.attributeSets = List.copyOf(attributeSets);
    }

    /**
     * Méthode permettant de charger le graphe depuis un répertoire
     * 
     * @param basePath chemin d'accès du répertoire
     * 
     * @throws IOException en cas d'erreur d'entrée/sortie
     * 
     * @return le graphe JaVelo obtenu à partir des fichiers
     */
    public static Graph loadFrom(Path basePath) throws IOException {
        IntBuffer nodesBuffer = getBufferFromPath(basePath, "nodes.bin").asIntBuffer();
        ByteBuffer sectorsBuffer = getBufferFromPath(basePath, "sectors.bin");
        ByteBuffer edgesBuffer = getBufferFromPath(basePath, "edges.bin");
        IntBuffer profilesIds = getBufferFromPath(basePath, "profile_ids.bin").asIntBuffer();
        ShortBuffer elevations = getBufferFromPath(basePath, "elevations.bin").asShortBuffer();
        LongBuffer attributes = getBufferFromPath(basePath, "attributes.bin").asLongBuffer();

        List<AttributeSet> attributeSet = new ArrayList<AttributeSet>();

        while (attributes.hasRemaining())
            attributeSet.add(new AttributeSet(attributes.get()));

        return new Graph(new GraphNodes(nodesBuffer), new GraphSectors(sectorsBuffer),
                new GraphEdges(edgesBuffer, profilesIds, elevations), attributeSet);
    }

    /**
     * Méthode permettant d'obtenir le nombre total de noeuds du graphe
     * 
     * @return le nombre total de noeuds
     */
    public int nodeCount() {
        return nodes.count();
    }

    /**
     * Méthode permettant d'obtenir la position d'un noeud
     * 
     * @param nodeId identité du noeud
     * 
     * @return position dans les coordonnées Suisses du noeud
     */
    public PointCh nodePoint(int nodeId) {
        return new PointCh(nodes.nodeE(nodeId), nodes.nodeN(nodeId));
    }

    /**
     * Méthode permettant d'obtenir le nombre d'arêtes sortantes d'un noeud
     * 
     * @param nodeId identité du noeud
     * 
     * @return le nombre d'arêtes sortantes du noeud
     */
    public int nodeOutDegree(int nodeId) {
        return nodes.outDegree(nodeId);
    }

    /**
     * Méthode permettant d'obtenir l'identité de l'edgeIndex-ième arête d'un noeud
     * 
     * @param nodeId    identité du noeud
     * @param edgeIndex index de l'arête
     * 
     * @return l'identité de l'edgeIndex-ième arête
     */
    public int nodeOutEdgeId(int nodeId, int edgeIndex) {
        return nodes.edgeId(nodeId, edgeIndex);
    }

    /**
     * Méthode permettant d'obtenir l'identité du noeud le plus proche d'un point
     * 
     * @param point          point de référence
     * @param searchDistance distance maximal de recherche
     * 
     * @return l'identité du noeud se trouvant le plus proche du point donné, à la distance maximale
     *         donnée (en mètres), ou -1 si aucun noeud ne correspond à ces critères
     */
    public int nodeClosestTo(PointCh point, double searchDistance) {
        double findDistance = searchDistance * searchDistance;
        int nodeId = -1;
        // récuperation de tous les secteurs dans la searchDistance
        for (Sector s : sectors.sectorsInArea(point, searchDistance)) {
            // parcours de tous les noeuds dans les secteurs
            for (int i = s.startNodeId(); i < s.endNodeId(); i++) {
                if (point.squaredDistanceTo(nodePoint(i)) <= findDistance) {
                    nodeId = i;
                    findDistance = point.squaredDistanceTo(nodePoint(i));
                }
            }
        }
        return nodeId;
    }

    /**
     * Méthode permettant d'obtenir l'identité du noeud de destination d'une arête
     * 
     * @param edgeId identité de l'arête
     * 
     * @return l'identité du noeud
     */
    public int edgeTargetNodeId(int edgeId) {
        return edges.targetNodeId(edgeId);
    }

    /**
     * Méthode permettant de savoir si une arête va dans le sens contraire de la voie OSM
     * 
     * @param edgeId identité de l'arête
     * 
     * @return TRUE si l'arête va dans le sens contraire de la voie OSM sinon FALSE
     */
    public boolean edgeIsInverted(int edgeId) {
        return edges.isInverted(edgeId);
    }

    /**
     * Méthode permettant d'obtenir l'ensemble des attributs OSM attaché à une arête
     * 
     * @param edgeId identité de l'arête
     * 
     * @return l'ensemble des attributs OSM attaché à l'arête
     */
    public AttributeSet edgeAttributes(int edgeId) {
        return attributeSets.get(edges.attributesIndex(edgeId));
    }

    /**
     * Méthode permettant d'obtenir la longueur d'une arête
     * 
     * @param edgeId identité de l'arête
     * 
     * @return longueur de l'arête en mètres
     */
    public double edgeLength(int edgeId) {
        return edges.length(edgeId);
    }

    /**
     * Méthode permettant d'obtenir le dénivelé positif total d'une arête
     * 
     * @param edgeId identité de l'arête
     * 
     * @return le dénivelé positif total de l'arête
     */
    public double edgeElevationGain(int edgeId) {
        return edges.elevationGain(edgeId);
    }

    /**
     * Méthode permettant d'obtenir le profil en long d'une arête
     * 
     * @param edgeId identité de l'arête
     * 
     * @return le profil en long de l'arête si elle en possède un sinon retourne une fonction
     *         constance qui renvoit Double.NaN
     */
    public DoubleUnaryOperator edgeProfile(int edgeId) {
        return edges.hasProfile(edgeId)
                ? Functions.sampled(edges.profileSamples(edgeId), edgeLength(edgeId))
                : Functions.constant(Double.NaN);
    }

    /**
     * Méthode permettant d'obtenir le buffer d'un fichier d'un répertoire
     * 
     * @param basePath le chemin d'accès au fichier
     * @param name     nom du fichier
     * 
     * @throws IOException en cas d'erreur d'entrée/sortie
     * 
     * @return le buffer du fichier
     */
    private static MappedByteBuffer getBufferFromPath(Path basePath, String name)
            throws IOException {
        try (FileChannel channel = FileChannel.open(basePath.resolve(name))) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
    }
}