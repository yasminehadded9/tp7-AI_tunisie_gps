package tp73;



import java.util.HashMap;
import java.util.Map;

/**
 * Fabrique le graphe routier tunisien selon les données du TP.
 * Coordonnées pixel calculées pour une image 700x1000 (carte de Tunisie).
 */
public class GraphFactory {

    /**
     * Construit et retourne le graphe de base du TP (+ extension Q4).
     * Les coordonnées (mapX, mapY) sont en proportion [0..1] de l'image.
     */
	public static Graph buildTunisiaGraph() {
	    Graph graph = new Graph();

	    // Coordonnées corrigées sur la vraie carte (x=0 gauche, y=0 haut)
	    graph.addNode(new Node("Tunis",    0.595, 0.085));
	    graph.addNode(new Node("Sousse",   0.700, 0.305));
	    graph.addNode(new Node("Kairouan", 0.545, 0.345));
	    graph.addNode(new Node("Sfax",     0.645, 0.505));
	    graph.addNode(new Node("Gafsa",    0.370, 0.575));
	    graph.addNode(new Node("Tozeur",   0.230, 0.615));
	    graph.addNode(new Node("Gabès",    0.660, 0.635));
	    graph.addNode(new Node("El Kef",   0.340, 0.215));

	    // Arêtes inchangées
	    graph.addEdge("Tunis",    "Sousse",   140);
	    graph.addEdge("Tunis",    "Kairouan", 160);
	    graph.addEdge("Sousse",   "Kairouan",  90);
	    graph.addEdge("Sousse",   "Sfax",     130);
	    graph.addEdge("Kairouan", "Gafsa",    200);
	    graph.addEdge("Sfax",     "Gafsa",    150);
	    graph.addEdge("Gafsa",    "Tozeur",    90);
	    graph.addEdge("Sfax",     "Gabès",     75);
	    graph.addEdge("Gabès",    "Gafsa",    130);
	    graph.addEdge("Tunis",    "El Kef",   170);
	    graph.addEdge("El Kef",   "Kairouan", 110);

	    return graph;
	}
    /**
     * Table des heuristiques h(n) vers Tozeur (TP de base).
     * Injectées externellement — le Node ne les stocke pas en dur.
     */
    public static Map<String, Double> getHeuristicsToTozeur() {
        Map<String, Double> h = new HashMap<>();
        h.put("Tunis",    400.0);
        h.put("Sousse",   300.0);
        h.put("Kairouan", 250.0);
        h.put("Sfax",     230.0);
        h.put("Gafsa",    100.0);
        h.put("Tozeur",     0.0);
        // Extension
        h.put("Gabès",    180.0);
        h.put("El Kef",   320.0);
        return h;
    }

    /**
     * Calcule dynamiquement h(n) = distance euclidienne vers la destination
     * (heuristique générique, admissible si les proportions sont correctes).
     */
    public static Map<String, Double> computeEuclideanHeuristics(Graph graph, String destName,
                                                                   double mapWidthKm, double mapHeightKm) {
        Node dest = graph.getNode(destName);
        Map<String, Double> h = new HashMap<>();
        if (dest == null) return h;
        for (Node n : graph.getAllNodes()) {
            double dx = (n.getMapX() - dest.getMapX()) * mapWidthKm;
            double dy = (n.getMapY() - dest.getMapY()) * mapHeightKm;
            h.put(n.getName(), Math.sqrt(dx * dx + dy * dy));
        }
        return h;
    }
}
