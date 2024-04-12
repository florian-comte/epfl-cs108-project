package ch.epfl.javelo.gui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

import ch.epfl.javelo.Preconditions;
import javafx.scene.image.Image;

/**
 * Classe représentant le gestionnaire de tuiles
 * 
 * @author Marc FARHAT (325811)
 * @author Florian COMTE (346006)
 */
public final class TileManager {
    private static final String URL_PREFIX = "https://";
    private static final String IMAGE_EXTENSION = ".png";
    private static final String URL_DELIMITER = "/";
    private static final String REQUEST_PROPERTY_KEY = "User-Agent";
    private static final String REQUEST_PROPERTY_VALUE = "JaVelo";
    private static final int MAX_CACHE_SIZE = 100;
    private static final int INIT_CAPACITY_LIST = 16;
    private static final float FACTOR_LIST = 0.75f;
    private static final boolean ACCESS_ORDER_LIST = true;

    private final Map<TileId, Image> cacheMemory;
    private final Path diskPath;
    private final String serverName;

    /**
     * Enregistrement représentant l'identité d'une tuile
     * 
     * @author Marc FARHAT (325811)
     * @author Florian COMTE (346006)
     * 
     * @param zoom niveau de zoom
     * @param x    index x de la tuile
     * @param y    index y de la tuile
     */
    public record TileId(int zoom, int x, int y) {
        /**
         * Construit l'identité d'une tuile
         * 
         * @throws IllegalArgumentException si la tuile n'est pas valide
         */
        public TileId {
            Preconditions.checkArgument(isValid(zoom, x, y));
        }

        /**
         * Méthode permettant de savoir si une tuile est valide
         * 
         * @param zoom niveau de zoom
         * @param x    index x de la tuile
         * @param y    index y de la tuile
         * 
         * @return TRUE si la tuile est valide et FALSE sinon
         */
        public static boolean isValid(int zoom, int x, int y) {
            double max = Math.pow(2, zoom);
            return x >= 0 && y >= 0 && x < max && y < max && zoom >= 0;
        }
    }

    /**
     * Construit un gestionnaire de tuiles
     * 
     * @param diskPath   chemin d'accès au cache disque
     * @param serverName nom du serveur de tuiles
     */
    public TileManager(Path diskPath, String serverName) {
        this.diskPath = diskPath;
        this.serverName = serverName;
        this.cacheMemory = new LinkedHashMap<>(INIT_CAPACITY_LIST, FACTOR_LIST, ACCESS_ORDER_LIST);
    }

    /**
     * Méthode permettant d'obtenir l'image d'une tuile
     * 
     * @param id identité de la tuile
     * 
     * @return image de la tuile
     * 
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    public Image imageForTileAt(TileId id) throws IOException {
        // vérification existence dans la mémoire cache
        if (cacheMemory.containsKey(id))
            return cacheMemory.get(id);

        Path directoryPath = diskPath.resolve(Integer.toString(id.zoom))
                .resolve(Integer.toString(id.x));
        Path filePath = directoryPath.resolve(id.y + IMAGE_EXTENSION);

        // vérification existence dans la mémoire disque
        if (!Files.exists(filePath)) {
            Files.createDirectories(directoryPath);
            saveImageOnDisk(id, filePath);
        }

        // récuperation sur disque
        try (InputStream i = new FileInputStream(filePath.toFile())) {
            return getImageAndSaveItOnCache(id, i);
        }
    }

    /**
     * Méthode permettant d'enregistrer une image sur le disque
     * 
     * @param id       identité de la tuile
     * @param filePath chemin d'accès au fichier
     * 
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    private void saveImageOnDisk(TileId id, Path filePath) throws IOException {
        StringJoiner joiner = new StringJoiner(URL_DELIMITER, URL_PREFIX, IMAGE_EXTENSION);
        joiner.add(serverName).add(Integer.toString(id.zoom)).add(Integer.toString(id.x))
                .add(Integer.toString(id.y));
        URL u = new URL(joiner.toString());
        URLConnection c = u.openConnection();
        c.setRequestProperty(REQUEST_PROPERTY_KEY, REQUEST_PROPERTY_VALUE);

        try (InputStream i = c.getInputStream()) {
            try (OutputStream o = new FileOutputStream(filePath.toFile())) {
                i.transferTo(o);
            }
        }
    }

    /**
     * Méthode permettant d'obtenir l'image de la tuile d'identité donnée et de la mettre dans le
     * cache mémoire
     * 
     * @param id identité de la tuile
     * @param i  flot d'entrée
     * 
     * @return l'image de la tuile
     */
    private Image getImageAndSaveItOnCache(TileId id, InputStream i) {
        Image img = new Image(i);
        if (cacheMemory.size() >= MAX_CACHE_SIZE)
            cacheMemory.remove(cacheMemory.keySet().iterator().next());

        cacheMemory.put(id, img);
        return img;
    }
}