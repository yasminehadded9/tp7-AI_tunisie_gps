package tp73;


import java.util.*;

/**
 * Graphe pondéré non orienté (liste d'adjacence).
 */
public class Graph {

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<String, List<Edge>> adjacency = new HashMap<>();

    public void addNode(Node node) {
        nodes.put(node.getName(), node);
        adjacency.put(node.getName(), new ArrayList<>());
    }

    /** Ajoute une arête bidirectionnelle */
    public void addEdge(String from, String to, double weight) {
        Node nodeFrom = nodes.get(from);
        Node nodeTo   = nodes.get(to);
        if (nodeFrom == null || nodeTo == null) {
            throw new IllegalArgumentException("Nœud introuvable : " + from + " ou " + to);
        }
        adjacency.get(from).add(new Edge(nodeFrom, nodeTo, weight));
        adjacency.get(to).add(new Edge(nodeTo, nodeFrom, weight));
    }

    public List<Edge> getNeighbors(Node node) {
        return adjacency.getOrDefault(node.getName(), Collections.emptyList());
    }

    public Node getNode(String name) { return nodes.get(name); }

    public Collection<Node> getAllNodes() { return nodes.values(); }

    /** Réinitialise g, f, parent de tous les nœuds avant une nouvelle recherche */
    public void reset() {
        for (Node n : nodes.values()) {
            n.setG(Double.MAX_VALUE);
            n.setParent(null);
        }
    }
}