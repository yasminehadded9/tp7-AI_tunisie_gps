package tp73;



import java.util.*;

/**
 * Implémentation de l'algorithme A*.
 */
public class Astarsearch {

    /**
     * Lance A* depuis source jusqu'à destination.
     * La table heuristique est injectée ici (externe au Node).
     */
    public static Searchresult search(Graph graph, String sourceName, String destName,
                                      Map<String, Double> heuristics) {
        graph.reset();

        Node source = graph.getNode(sourceName);
        Node dest   = graph.getNode(destName);
        if (source == null || dest == null) return null;

        // Injecter les heuristiques vers la destination courante
        for (Node n : graph.getAllNodes()) {
            Double h = heuristics.get(n.getName());
            n.setH(h != null ? h : 0.0);
        }

        // Init source
        source.setG(0);
        source.recalcF();

        PriorityQueue<Node> openList = new PriorityQueue<>();
        Set<String> closedSet = new LinkedHashSet<>();
        openList.add(source);

        List<String> traceLog = new ArrayList<>();
        List<List<Node>> explorationOrder = new ArrayList<>();
        int iteration = 0;

        while (!openList.isEmpty()) {
            iteration++;
            Node current = openList.poll();

            if (closedSet.contains(current.getName())) continue;
            closedSet.add(current.getName());
            explorationOrder.add(new ArrayList<>(List.of(current)));

            // Log itération
            StringBuilder sb = new StringBuilder();
            sb.append("=== Itération ").append(iteration).append(" ===\n");
            sb.append("Nœud courant : ").append(current.getName())
              .append(" | g=").append((int)current.getG())
              .append(" h=").append((int)current.getH())
              .append(" f=").append((int)current.getF()).append("\n");

            String openStr = formatPQ(openList, closedSet);
            sb.append("Open list    : [").append(openStr).append("]\n");
            sb.append("Closed list  : ").append(closedSet).append("\n");
            traceLog.add(sb.toString());

            if (current.getName().equals(destName)) {
                List<Node> path = reconstructPath(current);
                double cost = current.getG();
                return new Searchresult(path, cost, closedSet.size(), traceLog, explorationOrder);
            }

            for (Edge edge : graph.getNeighbors(current)) {
                Node neighbor = edge.getDestination();
                if (closedSet.contains(neighbor.getName())) continue;

                double gNew = current.getG() + edge.getWeight();
                if (gNew < neighbor.getG()) {
                    neighbor.setG(gNew);
                    neighbor.setParent(current);
                    // Remove and re-add to refresh priority
                    openList.remove(neighbor);
                    openList.add(neighbor);
                }
            }
        }

        traceLog.add("Aucun chemin trouvé de " + sourceName + " vers " + destName);
        return new Searchresult(null, -1, closedSet.size(), traceLog, explorationOrder);
    }

    private static List<Node> reconstructPath(Node dest) {
        LinkedList<Node> path = new LinkedList<>();
        Node current = dest;
        while (current != null) {
            path.addFirst(current);
            current = current.getParent();
        }
        return path;
    }

    private static String formatPQ(PriorityQueue<Node> pq, Set<String> closed) {
        List<Node> sorted = new ArrayList<>(pq);
        Collections.sort(sorted);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Node n : sorted) {
            if (closed.contains(n.getName())) continue;
            if (!first) sb.append(", ");
            sb.append(n.getName()).append("(f=").append((int)n.getF()).append(")");
            first = false;
        }
        return sb.toString();
    }
}