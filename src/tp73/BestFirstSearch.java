package tp73;


import java.util.*;

/**
 * Best-First Search — guidé uniquement par h(n), non optimal.
 */
public class BestFirstSearch {

    public static Searchresult search(Graph graph, String sourceName, String destName,
                                      Map<String, Double> heuristics) {
        graph.reset();
        Node source = graph.getNode(sourceName);
        Node dest   = graph.getNode(destName);
        if (source == null || dest == null) return null;

        for (Node n : graph.getAllNodes()) {
            Double h = heuristics.get(n.getName());
            n.setH(h != null ? h : 0.0);
        }
        source.setG(0);
        source.recalcF();

        // Tri par h uniquement
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(Node::getH));
        Set<String> closedSet = new LinkedHashSet<>();
        openList.add(source);

        List<String> traceLog = new ArrayList<>();
        List<List<Node>> explorationOrder = new ArrayList<>();
        int iteration = 0;
        // track g for path cost
        Map<String, Double> gMap = new HashMap<>();
        gMap.put(sourceName, 0.0);

        while (!openList.isEmpty()) {
            iteration++;
            Node current = openList.poll();
            if (closedSet.contains(current.getName())) continue;
            closedSet.add(current.getName());
            explorationOrder.add(List.of(current));

            StringBuilder sb = new StringBuilder();
            sb.append("=== Itération ").append(iteration).append(" ===\n");
            sb.append("Nœud courant : ").append(current.getName())
              .append(" | h=").append((int)current.getH()).append("\n");
            sb.append("Closed list  : ").append(closedSet).append("\n");
            traceLog.add(sb.toString());

            if (current.getName().equals(destName)) {
                List<Node> path = reconstructPath(current);
                return new Searchresult(path, current.getG(), closedSet.size(), traceLog, explorationOrder);
            }

            for (Edge edge : graph.getNeighbors(current)) {
                Node neighbor = edge.getDestination();
                if (closedSet.contains(neighbor.getName())) continue;
                double gNew = current.getG() + edge.getWeight();
                if (gNew < neighbor.getG()) {
                    neighbor.setG(gNew);
                    neighbor.setParent(current);
                    openList.remove(neighbor);
                    openList.add(neighbor);
                }
            }
        }
        return new Searchresult(null, -1, closedSet.size(), traceLog, explorationOrder);
    }

    private static List<Node> reconstructPath(Node dest) {
        LinkedList<Node> path = new LinkedList<>();
        Node current = dest;
        while (current != null) { path.addFirst(current); current = current.getParent(); }
        return path;
    }
}