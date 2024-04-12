package ch.epfl.javelo.routing;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ch.epfl.javelo.projection.PointCh;

/**
 * Classe permettant de générer des fichiers GPX
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class GpxGenerator {
    /**
     * Constructeur privé
     */
    private GpxGenerator() {
    }

    /**
     * Méthode permettant de créer un document GPX
     * 
     * @param route   itinéraire
     * @param profile profil de l'itinéraire
     * 
     * @return le document gpx de l'itinéraire
     */
    public static Document createGpx(Route route, ElevationProfile profile) {
        Document doc = newDocument();

        Element root = doc.createElementNS("http://www.topografix.com/GPX/1/1", "gpx");
        doc.appendChild(root);

        root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation",
                "http://www.topografix.com/GPX/1/1 " + "http://www.topografix.com/GPX/1/1/gpx.xsd");
        root.setAttribute("version", "1.1");
        root.setAttribute("creator", "JaVelo");

        Element metadata = doc.createElement("metadata");
        root.appendChild(metadata);

        Element name = doc.createElement("name");
        metadata.appendChild(name);
        name.setTextContent("Route JaVelo");

        Element rte = doc.createElement("rte");
        root.appendChild(rte);

        for (PointCh p : route.points()) {
            Element rtept = doc.createElement("rtept");
            rte.appendChild(rtept);

            rtept.setAttribute("lat", String.format(Locale.ROOT, "%.5f", Math.toDegrees(p.lat())));
            rtept.setAttribute("lon", String.format(Locale.ROOT, "%.5f", Math.toDegrees(p.lon())));

            Element ele = doc.createElement("ele");

            rtept.appendChild(ele);
            ele.setTextContent(String.format(Locale.ROOT, "%.2f",
                    profile.elevationAt(route.pointClosestTo(p).position())));
        }
        return doc;
    }

    /**
     * Méthode permettant d'écrire le document GPX dans le dossier racine du projet
     * 
     * @param name    nom du fichier
     * @param route   itinéraire
     * @param profile profil de l'itinéraire
     * 
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    public static void writeGpx(String name, Route route, ElevationProfile profile)
            throws IOException {
        Document doc = createGpx(route, profile);
        try (Writer w = new FileWriter(name)) {
            Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(w));
        } catch (TransformerException e) {
            throw new Error(e);
        }
    }

    /**
     * Méthode permettant de créer un nouveau document
     * 
     * @return un nouveau document
     */
    private static Document newDocument() {
        try {
            return DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new Error(e);
        }
    }
}
