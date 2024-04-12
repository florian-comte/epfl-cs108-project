package ch.epfl.javelo.data;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Q28_4;

/**
 * Enregistrement représentant des arêtes
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 * 
 * @param edgesBuffer mémoire tampon des arêtes
 * @param profileIds  mémoire tampon des profils
 * @param elevations  mémoire tampon contenant la totalité des échantillons des profils
 */
public record GraphEdges(ByteBuffer edgesBuffer, IntBuffer profileIds, ShortBuffer elevations) {

    private static final int OFFSET_EDGES_INDEX = 0;
    private static final int OFFSET_EDGES_LENGTH = OFFSET_EDGES_INDEX + Integer.BYTES;
    private static final int OFFSET_EDGES_ELEVATION = OFFSET_EDGES_LENGTH + Short.BYTES;
    private static final int OFFSET_EDGES_ID = OFFSET_EDGES_ELEVATION + Short.BYTES;
    private static final int EDGES_INT = OFFSET_EDGES_ID + Short.BYTES;
    
    private static final int NO_PROFIL = 0;
    private static final int PROFIL_1 = 1;
    private static final int PROFIL_2 = 2;
    private static final int PROFIL_3 = 3;


    /**
     * Méthode permettant de savoir si l'arête va dans le sens inverse de la voie OSM
     * 
     * @param edgeId identité de l'arête
     * 
     * @return TRUE si l'arête d'identité donnée va dans le sens inverse de la voie OSM sinon FALSE
     */
    public boolean isInverted(int edgeId) {
        return getIntInEdges(edgeId, OFFSET_EDGES_INDEX) < 0;
    }

    /**
     * Méthode permettant d'obtenir l'identité du noeud de destination d'une arête
     * 
     * @param edgeId identité de l'arête
     * 
     * @return l'identité du noeud de destination de l'arête d'identité donnée
     */
    public int targetNodeId(int edgeId) {
        int targetId = getIntInEdges(edgeId, OFFSET_EDGES_INDEX);
        return (targetId < 0 ? ~targetId : targetId);
    }

    /**
     * Méthode permettant d'obtenir la longueur d'une arête
     * 
     * @param edgeId identité de l'arête
     * 
     * @return la longueur, en mètres, de l'arête d'identité donnée
     */
    public double length(int edgeId) {
        return Q28_4.asDouble(Short.toUnsignedInt(getShortInEdges(edgeId, OFFSET_EDGES_LENGTH)));
    }

    /**
     * Méthode permettant d'obtenir le dénivelé positif d'une arête
     * 
     * @param edgeId identité de l'arête
     * 
     * @return le dénivelé positif, en mètres, de l'arête d'identité donnée
     */
    public double elevationGain(int edgeId) {
        return Q28_4.asDouble(Short.toUnsignedInt(getShortInEdges(edgeId, OFFSET_EDGES_ELEVATION)));
    }

    /**
     * Méthode permettant de savoir si une arête a un profil
     * 
     * @param edgeId identité de l'arête
     * 
     * @return TRUE si l'arête possède un profil et FALSE sinon
     */
    public boolean hasProfile(int edgeId) {
        return getProfileType(edgeId) != NO_PROFIL;
    }

    /**
     * Méthode permettant d'obtenir le tableau des échantillons du profil d'une arête
     * 
     * @param edgeId identité de l'arête
     * 
     * @return le tableau des échantillons du profil de l'arête d'identité donnée
     */
    public float[] profileSamples(int edgeId) {
        int firstSampleId = getFirstSampleId(edgeId);
        int numberSamples = getNumberOfSamples(edgeId);
        int profileType = getProfileType(edgeId);
        float[] samples = new float[numberSamples];

        switch (profileType) {
        case NO_PROFIL:
            return new float[0];
        case PROFIL_1: {
            for (int i = 0; i < numberSamples; i++)
                samples[i] = Q28_4.asFloat(getElevationSample(firstSampleId + i));
            break;
        }
        case PROFIL_2: {
            setSamplesCompressed(firstSampleId, numberSamples, samples, 2, 8, 1, 2);
            break;
        }
        case PROFIL_3: {
            setSamplesCompressed(firstSampleId, numberSamples, samples, 4, 4, 0, 1);
            break;
        }
        }

        if (isInverted(edgeId))
            reverseFloatArray(samples);

        return samples;
    }

    /**
     * Méthode permettant d'obtenir l'identité de l'ensemble d'attributs attaché à une arête
     * 
     * @param edgeId identité de l'arête
     * 
     * @return l'identité de l'ensemble d'attributs attaché à l'arête d'identité donnée
     */
    public int attributesIndex(int edgeId) {
        return Short.toUnsignedInt(getShortInEdges(edgeId, OFFSET_EDGES_ID));
    }

    /**
     * Méthode permettant d'obtenir tous les échantillons et les mettre dans le tableau
     * d'échantillons (profil compressé)
     * 
     * @param firstSampleId             identité du premier échantillon
     * @param numberSamples             nombre d'échantillons
     * @param samples                   tableau des échantillons
     * @param numberOfSamplesInOneShort nombre de différences d'altitude dans un short (en bits)
     * @param numberOfBitsForOneSample  nombre de bits pour représenter une différence d'altitude
     *                                  dans un short
     * @param forStart                  index initial de la boucle for qui extrait les différences
     *                                  d'altitudes compressées
     * @param forIncrement              valeur d'incrémentage de la boucle for qui extrait les
     *                                  différences d'altitudes compressées
     */
    private void setSamplesCompressed(int firstSampleId, int numberSamples, float[] samples,
            int numberOfSamplesInOneShort, int numberOfBitsForOneSample, int forStart,
            int forIncrement) {
        int counter = 1;
        // récupération du premier echantillon
        int tempElevation = getElevationSample(firstSampleId);
        samples[0] = Q28_4.asFloat(tempElevation);

        // nombre de short à parcourir
        int bufferNbr = Math2.ceilDiv(numberSamples, numberOfSamplesInOneShort);
        for (int buffIndex = firstSampleId + 1; buffIndex <= bufferNbr
                + firstSampleId; buffIndex++) {
            /*
             * On utilise la boucle for de sorte que si c'est le profil 3, on aura i=0,1,2,3 et si
             * c'est le profil 2, on aura i=1,3 afin d'utiliser goodPart
             */
            for (int i = forStart; i < 4; i = i + forIncrement) {
                if (counter >= numberSamples)
                    break;
                int toAdd = Bits.extractSigned(getElevationSample(buffIndex), getGoodStart(i),
                        numberOfBitsForOneSample);
                tempElevation = tempElevation + toAdd;
                samples[counter] = Q28_4.asFloat(tempElevation);
                counter++;
            }
        }
    }

    /**
     * Méthode permettant d'obtenir un entier dans le buffer des arêtes
     * 
     * @param edgeId identité de l'arête
     * @param offset offset voulu
     * 
     * @return l'entier dans le buffer des arêtes
     */
    private int getIntInEdges(int edgeId, int offset) {
        return edgesBuffer.getInt(EDGES_INT * edgeId + offset);
    }

    /**
     * Méthode permettant d'obtenir un short dans le buffer des arêtes
     * 
     * @param edgeId identité de l'arête
     * @param offset offset voulu
     * 
     * @return le short dans le buffer des arêtes
     */
    private short getShortInEdges(int edgeId, int offset) {
        return edgesBuffer.getShort(EDGES_INT * edgeId + offset);
    }

    /**
     * Méthode permettant d'obtenir un entier dans le buffer des profils
     * 
     * @param edgeId identité de l'arête
     * 
     * @return l'entier dans le buffer des profils
     */
    private int getInProfile(int edgeId) {
        return profileIds.get(edgeId);
    }

    /**
     * Méthode permettant d'obtenir le nombres d'échantillons d'une arête
     * 
     * @param edgeId identité de l'arête
     * 
     * @return le nombre d'échantillons de l'arête donnée
     */
    private int getNumberOfSamples(int edgeId) {
        return 1 + Math2.ceilDiv(getShortInEdges(edgeId, OFFSET_EDGES_LENGTH), Q28_4.ofInt(2));
    }

    /**
     * Méthode permettant d'obtenir le type de profil d'une arête
     * 
     * @param edgeId identité de l'arête
     * 
     * @return le type du profil de l'arête
     */
    private int getProfileType(int edgeId) {
        return Bits.extractUnsigned(getInProfile(edgeId), 30, 2);
    }

    /**
     * Méthode permettant d'obtenir l'identité du premier échantillon d'une arête
     * 
     * @param edgeId identité de l'arête
     * 
     * @return l'identité du premier échantillon de l'arête
     */
    private int getFirstSampleId(int edgeId) {
        return Bits.extractUnsigned(getInProfile(edgeId), 0, 30);
    }

    /**
     * Méthode permettant d'obtenir l'échantillon de dénivelé d'un certain index
     * 
     * @param sampleIndex index de l'échantillon
     * 
     * @return échantillon de dénivelé de l'index
     */
    private int getElevationSample(int sampleIndex) {
        return Short.toUnsignedInt(elevations.get(sampleIndex));
    }

    /**
     * Méthode permettant d'obtenir l'index du début de l'échantillon à extraire
     * 
     * @param i index du short
     * 
     * @return l'index du début de l'échantillon à extraire
     */
    private int getGoodStart(int i) {
        switch (i) {
        case 0:
            return 12;
        case 1:
            return 8;
        case 2:
            return 4;
        default:
            return 0;
        }
    }

    /**
     * Méthode permettant d'inverser un tableau de floats
     * 
     * @param array tableau à inverser
     */
    private void reverseFloatArray(float array[]) {
        for (int s = 0, e = array.length - 1; s <= e; s++, e--) {
            float temp = array[s];
            array[s] = array[e];
            array[e] = temp;
        }
    }
}