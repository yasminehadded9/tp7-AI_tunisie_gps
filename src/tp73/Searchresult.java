package tp73;



import java.util.List;

/**
 * Résultat d'une recherche de chemin.
 */
public class Searchresult {
    private final List<Node> path;
    private final double totalCost;
    private final int nodesExplored;
    private final List<String> traceLog; // log itération par itération
    private final List<List<Node>> explorationOrder; // pour l'animation

    public Searchresult(List<Node> path, double totalCost, int nodesExplored,
                        List<String> traceLog, List<List<Node>> explorationOrder) {
        this.path = path;
        this.totalCost = totalCost;
        this.nodesExplored = nodesExplored;
        this.traceLog = traceLog;
        this.explorationOrder = explorationOrder;
    }

    public List<Node> getPath()              { return path; }
    public double getTotalCost()             { return totalCost; }
    public int getNodesExplored()            { return nodesExplored; }
    public List<String> getTraceLog()        { return traceLog; }
    public List<List<Node>> getExplorationOrder() { return explorationOrder; }
    public boolean isFound()                 { return path != null && !path.isEmpty(); }
}