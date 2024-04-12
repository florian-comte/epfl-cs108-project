package ch.epfl.javelo.routing;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

/**
 * Classe représentant un planificateur d'itinéraire
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class RouteComputer {
    private static final float ALREADY_DEFINE_FLOAT = Float.NEGATIVE_INFINITY;
    private static final float NOT_DEFINE_FLOAT = Float.POSITIVE_INFINITY;
    private final Graph graph;
    private final CostFunction costFunction;

    /***
     * Construit un planificateur d'itinéraire
     * 
     * @param graph        graphe souhaité
     * @param costFunction fonction de coût
     */
    public RouteComputer(Graph graph, CostFunction costFunction) {
        this.graph = graph;
        this.costFunction = costFunction;
    }

    /**
     * Méthode permettant d'obtenir l'itinéraire de coût minimal
     * 
     * @param startNodeId identité du de départ
     * @param endNodeId   identité du noeud d'arrivé
     * 
     * @throws IllegalArgumentException si le noeud de départ et d'arrivé sont les mêmes
     * 
     * @return l'itinéraire de coût minimal
     */
    public Route bestRouteBetween(int startNodeId, int endNodeId) {
        Preconditions.checkArgument(startNodeId != endNodeId);

        float[] distance = new float[graph.nodeCount()];
        /*
         * identité du prédécesseur code sur les 28 bits de poids faible et index de l'arête sur les
         * 4 bits de poids fort
         */
        int[] predecessor = new int[graph.nodeCount()];

        Arrays.fill(distance, NOT_DEFINE_FLOAT);
        distance[startNodeId] = 0;

        Queue<WeightedNode> queue = new PriorityQueue<>();
        queue.add(new WeightedNode(startNodeId, 0));
        PointCh endPoint = graph.nodePoint(endNodeId);

        while (!queue.isEmpty()) {
            int currentId = queue.remove().nodeId;
            if (distance[currentId] != ALREADY_DEFINE_FLOAT) {

                // si on arrive au point d'arrivée
                if (currentId == endNodeId)
                    return new SingleRoute(
                            getEdgesFromPredecessor(endNodeId, startNodeId, predecessor));

                // si pas déjà calculé
                analyseCurrentNode(distance, predecessor, queue, currentId, endPoint);
                distance[currentId] = ALREADY_DEFINE_FLOAT;
            }
        }
        return null;
    }

    /**
     * Enregistrement représentant un noeud pondéré
     * 
     * @author Marc FARHAT (325811)
     * @author Florian COMTE (346006)
     */
    private record WeightedNode(int nodeId, float distance) implements Comparable<WeightedNode> {
        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(WeightedNode that) {
            return Float.compare(this.distance, that.distance);
        }
    }

    /**
     * Méthode permettant d'analyser le noeud actuel et d'ajouter les noeuds à analyser dans la
     * queue
     * 
     * @param distance    tableau des distances
     * @param predecessor tableau des prédécesseurs
     * @param queue       queue des noeuds en attente de parcours
     * @param currentId   identité du noeud en cours d'analyse
     * @param endPoint    point d'arrivée dans le système Suisse
     */
    private void analyseCurrentNode(float[] distance, int[] predecessor, Queue<WeightedNode> queue,
            int currentId, PointCh endPoint) {
        // parcours des arêtes sortantes du noeud
        for (int i = 0; i < graph.nodeOutDegree(currentId); i++) {
            int edgeId = graph.nodeOutEdgeId(currentId, i);
            int nPrime = graph.edgeTargetNodeId(edgeId);
            float edgeLength = (float) (graph.edgeLength(edgeId)
                    * costFunction.costFactor(currentId, edgeId));

            float d = distance[currentId] + edgeLength;
            if (d < distance[nPrime]) {
                distance[nPrime] = d;
                predecessor[nPrime] = (i << 28) | currentId;

                float dWeighted = (float) (d + graph.nodePoint(nPrime).distanceTo(endPoint));

                queue.add(new WeightedNode(nPrime, dWeighted));
            }
        }
    }

    /**
     * Méthode permettant d'obtenir la liste de toutes les arêtes d'un itinéraire en remontant dans
     * les prédécesseurs
     * 
     * @param endNodeId   identité du noeud d'arrivée de l'itinéraire
     * @param startNodeId identité du noeud de départ de l'itinéraire
     * @param predecessor tableau des prédécesseurs
     * 
     * @return liste des arêtes
     */
    private List<Edge> getEdgesFromPredecessor(int endNodeId, int startNodeId, int[] predecessor) {
        LinkedList<Edge> edges = new LinkedList<>();

        int currentEndId = endNodeId;
        while (currentEndId != startNodeId) {
            int currentStartNode = predecessor[currentEndId];
            int edgeIndex = Bits.extractUnsigned(currentStartNode, 28, 4);
            int currentStartId = Bits.extractUnsigned(currentStartNode, 0, 28);
            int edgeId = graph.nodeOutEdgeId(currentStartId, edgeIndex);

            edges.addFirst(Edge.of(graph, edgeId, currentStartId, currentEndId));
            currentEndId = currentStartId;
        }
        return edges;
    }
}