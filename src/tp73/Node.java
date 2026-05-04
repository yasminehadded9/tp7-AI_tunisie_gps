package tp73;

/**
 * Représente un nœud dans le graphe routier tunisien.
 * h(n) n'est PAS stocké en dur — il est injecté depuis une source externe (table heuristique).
 */
public class Node implements Comparable<Node> {

    private final String name;
    private double g;   // coût réel depuis la source
    private double h;   // heuristique vers la destination (injectée)
    private double f;   // f = g + h
    private Node parent;

    // Coordonnées pixel sur la carte (pour l'affichage JavaFX)
    private final double mapX;
    private final double mapY;

    public Node(String name, double mapX, double mapY) {
        this.name = name;
        this.mapX = mapX;
        this.mapY = mapY;
        this.g = Double.MAX_VALUE;
        this.h = 0;
        this.f = Double.MAX_VALUE;
        this.parent = null;
    }

    /** Injecte la valeur heuristique depuis la table externe */
    public void setH(double h) {
        this.h = h;
    }

    public void setG(double g) {
        this.g = g;
        this.f = g + h;
    }

    public void recalcF() {
        this.f = this.g + this.h;
    }

    public String getName()   { return name; }
    public double getG()      { return g; }
    public double getH()      { return h; }
    public double getF()      { return f; }
    public Node   getParent() { return parent; }
    public void   setParent(Node parent) { this.parent = parent; }
    public double getMapX()   { return mapX; }
    public double getMapY()   { return mapY; }

    @Override
    public int compareTo(Node other) {
        return Double.compare(this.f, other.f);
    }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        return name.equals(((Node) o).name);
    }

    @Override
    public int hashCode() { return name.hashCode(); }
}