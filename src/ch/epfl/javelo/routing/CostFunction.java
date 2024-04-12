package ch.epfl.javelo.routing;

/**
 * Interface représentant une fonction de coût
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public interface CostFunction {

    /**
     * Méthode permettant d'obtenir le facteur multiplicateur d'une arête
     * 
     * @param nodeId identité du noeud
     * @param edgeId identité de l'arête
     * 
     * @return facteur multiplicateur de l'arête
     */
    double costFactor(int nodeId, int edgeId);
}