package tp73;



/**
 * Représente une arête pondérée entre deux villes.
 */
public class Edge {
    private final Node source;
    private final Node destination;
    private final double weight; // distance en km

    public Edge(Node source, Node destination, double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public Node getSource()      { return source; }
    public Node getDestination() { return destination; }
    public double getWeight()    { return weight; }

    @Override
    public String toString() {
        return source.getName() + " --[" + weight + "km]--> " + destination.getName();
    }
}