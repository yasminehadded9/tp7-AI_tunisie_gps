package tp73;



import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.*;

/**
 * TP n°7 — Algorithme A* · Mini GPS Tunisie
 * Interface JavaFX complète avec carte interactive.
 */
public class MainApp extends Application {

    // ── Dimensions ──────────────────────────────────────────────────────
    private static final double MAP_W  = 560;
    private static final double MAP_H  = 800;
    private static final double NODE_R = 14;

    // ── Couleurs palette ────────────────────────────────────────────────
    private static final Color BG_DARK   = Color.web("#0d1117");
    private static final Color PANEL_BG  = Color.web("#161b22");
    private static final Color ACCENT    = Color.web("#2ea043");
    private static final Color ACCENT2   = Color.web("#388bfd");
    private static final Color WARN      = Color.web("#d29922");
    private static final Color DANGER    = Color.web("#f85149");
    private static final Color TEXT_PRI  = Color.web("#e6edf3");
    private static final Color TEXT_SEC  = Color.web("#8b949e");
    private static final Color BORDER    = Color.web("#30363d");

    // ── Données métier ───────────────────────────────────────────────────
    private Graph graph;
    private Map<String, Double> heuristics;
    private Searchresult lastResult;

    // ── UI ───────────────────────────────────────────────────────────────
    private Canvas mapCanvas;
    private GraphicsContext gc;
    private Image mapImage;
    private TextArea logArea;
    private Label costLabel, nodesLabel, pathLabel, statusLabel;
    private ComboBox<String> sourceCombo, destCombo, algoCombo;
    private Button runBtn, clearBtn;

    // Animation
    private List<tp73.Node> animPath = new ArrayList<>();
    private List<tp73.Node> exploredNodes = new ArrayList<>();
    private int animStep = 0;

    @Override
    public void start(Stage stage) {
        graph      = GraphFactory.buildTunisiaGraph();
        heuristics = GraphFactory.getHeuristicsToTozeur();

        // Load map image
        try {
            // Essaie plusieurs chemins possibles dans Eclipse
            InputStream is = getClass().getResourceAsStream("/map_tunisia.jpg");
            if (is == null)
                is = getClass().getResourceAsStream("/resources/map_tunisia.jpg");
            if (is == null)
                is = getClass().getResourceAsStream("/Map_of_Tunisia.jpg");
            if (is != null) mapImage = new Image(is);
        } catch (Exception e) {
            System.out.println("Image non trouvée: " + e.getMessage());
        }

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0d1117;");

        root.setTop(buildHeader());
        root.setLeft(buildControlPanel());
        root.setCenter(buildMapPane());
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root, 1280, 860);
        scene.getStylesheets().add(inlineCSS());
        stage.setScene(scene);
        stage.setTitle("Mini GPS Tunisie — TP n°7 Algorithme A*");
        stage.setResizable(true);
        stage.show();

        drawMap(Collections.emptyList(), Collections.emptyList());
    }

    // ═══════════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════════
    private Node buildHeader() {
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-width: 0 0 1 0;");

        Label ico = new Label("🗺");
        ico.setFont(Font.font(28));

        VBox titles = new VBox(2);
        Label title = new Label("Mini GPS Tunisie");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 22));
        title.setTextFill(TEXT_PRI);

        Label sub = new Label("TP n°7 — Navigation intelligente · Algorithme A* · Fondements de l'IA");
        sub.setFont(Font.font("Consolas", 12));
        sub.setTextFill(TEXT_SEC);
        titles.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label("GLSI 2 · Semestre 4 · 2025/2026");
        badge.setFont(Font.font("Consolas", 11));
        badge.setTextFill(ACCENT);
        badge.setStyle("-fx-background-color: #0d2a15; -fx-padding: 4 10; -fx-background-radius: 20;");

        header.getChildren().addAll(ico, titles, spacer, badge);
        return header;
    }

    // ═══════════════════════════════════════════════════════════════
    // CONTROL PANEL (LEFT)
    // ═══════════════════════════════════════════════════════════════
    private Node buildControlPanel() {
        VBox panel = new VBox(0);
        panel.setPrefWidth(310);
        panel.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-width: 0 1 0 0;");
        panel.setPadding(new Insets(20, 16, 16, 16));
        panel.setSpacing(0);

        List<String> cities = new ArrayList<>();
        graph.getAllNodes().forEach(n -> cities.add(n.getName()));
        Collections.sort(cities);

        // ── Section : Configuration ──────────────────────────────
        panel.getChildren().add(sectionLabel("⚙  Configuration"));
        panel.getChildren().add(spacer(8));

        sourceCombo = styledCombo(cities, "Tunis");
        destCombo   = styledCombo(cities, "Tozeur");
        algoCombo   = styledCombo(List.of("A* (optimal)", "UCS (Dijkstra)", "Best-First Search"), "A* (optimal)");

        panel.getChildren().addAll(
            formRow("Départ", sourceCombo),
            spacer(6),
            formRow("Arrivée", destCombo),
            spacer(6),
            formRow("Algorithme", algoCombo),
            spacer(16)
        );

        // ── Heuristique info ─────────────────────────────────────
        panel.getChildren().add(sectionLabel("📐  Heuristiques h(n) → Tozeur"));
        panel.getChildren().add(spacer(8));
        panel.getChildren().add(buildHeuristicTable());
        panel.getChildren().add(spacer(16));

        // ── Boutons ───────────────────────────────────────────────
        runBtn   = actionButton("▶  Lancer la recherche", ACCENT);
        clearBtn = actionButton("↺  Réinitialiser",       Color.web("#21262d"));
        clearBtn.setStyle(clearBtn.getStyle() + "-fx-text-fill: #8b949e;");

        runBtn.setOnAction(e -> runSearch());
        clearBtn.setOnAction(e -> clearAll());

        panel.getChildren().addAll(runBtn, spacer(6), clearBtn, spacer(20));

        // ── Résultats ─────────────────────────────────────────────
        panel.getChildren().add(sectionLabel("📊  Résultats"));
        panel.getChildren().add(spacer(8));

        costLabel  = resultLabel("—");
        nodesLabel = resultLabel("—");
        pathLabel  = resultLabel("—");
        pathLabel.setWrapText(true);

        panel.getChildren().addAll(
            resultRow("Coût total",       costLabel),
            spacer(4),
            resultRow("Nœuds explorés",   nodesLabel),
            spacer(4),
            resultRow("Chemin",           pathLabel),
            spacer(20)
        );

        // ── Log console ───────────────────────────────────────────
        panel.getChildren().add(sectionLabel("📋  Trace d'exécution"));
        panel.getChildren().add(spacer(8));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(220);
        logArea.setStyle(
            "-fx-control-inner-background: #0d1117;" +
            "-fx-text-fill: #7ee787;" +
            "-fx-font-family: 'Consolas';" +
            "-fx-font-size: 11;" +
            "-fx-border-color: #30363d;" +
            "-fx-border-width: 1;"
        );
        logArea.setText("Sélectionnez départ, arrivée et algorithme, puis cliquez ▶\n");
        VBox.setVgrow(logArea, Priority.ALWAYS);
        panel.getChildren().add(logArea);

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #161b22;");
        return scroll;
    }

    // ═══════════════════════════════════════════════════════════════
    // MAP PANE (CENTER)
    // ═══════════════════════════════════════════════════════════════
    private Node buildMapPane() {
        mapCanvas = new Canvas(MAP_W, MAP_H);
        gc = mapCanvas.getGraphicsContext2D();

        StackPane stack = new StackPane(mapCanvas);
        stack.setStyle("-fx-background-color: #0d1117;");
        stack.setPadding(new Insets(20));

        // Legend
        VBox legend = buildLegend();
        StackPane.setAlignment(legend, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(legend, new Insets(0, 30, 30, 0));
        stack.getChildren().add(legend);

        return stack;
    }

    // ═══════════════════════════════════════════════════════════════
    // STATUS BAR
    // ═══════════════════════════════════════════════════════════════
    private Node buildStatusBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 20, 8, 20));
        bar.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-width: 1 0 0 0;");

        statusLabel = new Label("Prêt");
        statusLabel.setFont(Font.font("Consolas", 12));
        statusLabel.setTextFill(TEXT_SEC);

        Label info = new Label("  •  f(n) = g(n) + h(n)  •  A* est optimal si h(n) est admissible");
        info.setFont(Font.font("Consolas", 11));
        info.setTextFill(Color.web("#444c56"));

        bar.getChildren().addAll(statusLabel, info);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // DRAW MAP
    // ═══════════════════════════════════════════════════════════════
    private void drawMap(List<tp73.Node> path, List<tp73.Node> explored) {
        gc.clearRect(0, 0, MAP_W, MAP_H);

        // Background
        gc.setFill(Color.web("#0d1117"));
        gc.fillRect(0, 0, MAP_W, MAP_H);

        // CHANGEMENT 1 & 2 : Image à 100% d'opacité + overlay très léger (10%)
        if (mapImage != null) {
            gc.setGlobalAlpha(1.0);  // CHANGEMENT 1 : était 0.75, maintenant 1.0
            gc.drawImage(mapImage, 0, 0, MAP_W, MAP_H);
            gc.setGlobalAlpha(1.0);
        } else {
            // Fallback: simple silhouette Tunisia shape
            gc.setFill(Color.web("#1a2a1a"));
            gc.fillRoundRect(60, 10, 440, 780, 60, 60);
        }

        // CHANGEMENT 2 : Overlay noir à 10% (était 35%)
        gc.setFill(Color.rgb(13, 17, 23, 0.10));
        gc.fillRect(0, 0, MAP_W, MAP_H);

        // ── Draw edges ───────────────────────────────────────────
        Set<String> pathEdgeSet = buildPathEdgeSet(path);

        for (tp73.Node n : graph.getAllNodes()) {
            for (Edge edge : graph.getNeighbors(n)) {
                if (edge.getSource().getName().compareTo(edge.getDestination().getName()) > 0) continue;
                double x1 = n.getMapX() * MAP_W;
                double y1 = n.getMapY() * MAP_H;
                double x2 = edge.getDestination().getMapX() * MAP_W;
                double y2 = edge.getDestination().getMapY() * MAP_H;

                String key = n.getName() + "-" + edge.getDestination().getName();
                if (pathEdgeSet.contains(key) || pathEdgeSet.contains(edge.getDestination().getName() + "-" + n.getName())) {
                    // Path edge — glowing green
                    gc.setStroke(Color.web("#2ea04388"));
                    gc.setLineWidth(8);
                    gc.strokeLine(x1, y1, x2, y2);
                    gc.setStroke(ACCENT);
                    gc.setLineWidth(3.5);
                    gc.strokeLine(x1, y1, x2, y2);
                } else {
                    gc.setStroke(Color.web("#30363d"));
                    gc.setLineWidth(1.5);
                    gc.setLineDashes(6, 4);
                    gc.strokeLine(x1, y1, x2, y2);
                    gc.setLineDashes(0);
                }

                // Distance label
                double mx = (x1 + x2) / 2;
                double my = (y1 + y2) / 2;
                gc.setFill(Color.web("#30363d"));
                gc.fillRoundRect(mx - 16, my - 9, 32, 16, 8, 8);
                gc.setFill(TEXT_SEC);
                gc.setFont(Font.font("Consolas", 9));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(String.valueOf((int)edge.getWeight()), mx, my + 4);
            }
        }

        // ── Draw nodes ───────────────────────────────────────────
        Set<String> pathSet = new HashSet<>();
        path.forEach(n -> pathSet.add(n.getName()));
        Set<String> exploredSet = new HashSet<>();
        explored.forEach(n -> exploredSet.add(n.getName()));

        String src = sourceCombo != null ? sourceCombo.getValue() : "";
        String dst = destCombo   != null ? destCombo.getValue()   : "";

        for (tp73.Node n : graph.getAllNodes()) {
            double cx = n.getMapX() * MAP_W;
            double cy = n.getMapY() * MAP_H;

            Color ring, fill;
            String icon = "●";

            if (n.getName().equals(src)) {
                ring = ACCENT; fill = Color.web("#0d2a15"); icon = "S";
            } else if (n.getName().equals(dst)) {
                ring = DANGER; fill = Color.web("#2d0f0f"); icon = "D";
            } else if (pathSet.contains(n.getName())) {
                ring = ACCENT; fill = Color.web("#122a16");
            } else if (exploredSet.contains(n.getName())) {
                ring = WARN;   fill = Color.web("#2a1f0a");
            } else {
                ring = ACCENT2; fill = Color.web("#0c1929");
            }

            // Shadow glow
            gc.setFill(Color.color(ring.getRed(), ring.getGreen(), ring.getBlue(), 0.25));
            gc.fillOval(cx - NODE_R - 6, cy - NODE_R - 6, (NODE_R + 6) * 2, (NODE_R + 6) * 2);

            // Circle
            gc.setFill(fill);
            gc.fillOval(cx - NODE_R, cy - NODE_R, NODE_R * 2, NODE_R * 2);
            gc.setStroke(ring);
            gc.setLineWidth(2.5);
            gc.strokeOval(cx - NODE_R, cy - NODE_R, NODE_R * 2, NODE_R * 2);

            // Icon/letter
            gc.setFill(ring);
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(icon, cx, cy + 4);

            // CHANGEMENT 3 & 4 : Nom de la ville avec fond semi-transparent pour lisibilité
            String cityName = n.getName();
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
            // Mesure approximative de la largeur du texte pour le fond
            double textWidth = cityName.length() * 7.5;
            double textX = cx - textWidth / 2;
            double textY = cy - NODE_R - 6;

            // CHANGEMENT 3 : Fond noir semi-transparent derrière le texte
            gc.setFill(Color.rgb(0, 0, 0, 0.60));
            gc.fillRoundRect(textX - 3, textY - 11, textWidth + 6, 14, 4, 4);

            // CHANGEMENT 4 : Texte blanc bien visible
            gc.setFill(TEXT_PRI);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(cityName, cx, textY);
        }

        // ── Path arrows ──────────────────────────────────────────
        if (path.size() > 1) {
            for (int i = 0; i < path.size() - 1; i++) {
                tp73.Node a = path.get(i);
                tp73.Node b = path.get(i + 1);
                drawArrow(a.getMapX() * MAP_W, a.getMapY() * MAP_H,
                          b.getMapX() * MAP_W, b.getMapY() * MAP_H);
            }
        }
    }

    private void drawArrow(double x1, double y1, double x2, double y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double mx = (x1 + x2) / 2;
        double my = (y1 + y2) / 2;
        double size = 10;
        gc.setFill(ACCENT);
        double[] xs = {
            mx + size * Math.cos(angle),
            mx + size * Math.cos(angle + 2.4),
            mx + size * Math.cos(angle - 2.4)
        };
        double[] ys = {
            my + size * Math.sin(angle),
            my + size * Math.sin(angle + 2.4),
            my + size * Math.sin(angle - 2.4)
        };
        gc.fillPolygon(xs, ys, 3);
    }

    private Set<String> buildPathEdgeSet(List<tp73.Node> path) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < path.size() - 1; i++) {
            set.add(path.get(i).getName() + "-" + path.get(i + 1).getName());
        }
        return set;
    }

    // ═══════════════════════════════════════════════════════════════
    // SEARCH LOGIC
    // ═══════════════════════════════════════════════════════════════
    private void runSearch() {
        String src  = sourceCombo.getValue();
        String dst  = destCombo.getValue();
        String algo = algoCombo.getValue();

        if (src == null || dst == null || src.equals(dst)) {
            statusLabel.setText("⚠ Sélectionnez deux villes différentes !");
            return;
        }

        // Recalculate heuristics for destination
        Map<String, Double> h = computeHeuristics(dst);

        statusLabel.setText("⏳ Calcul en cours…");
        runBtn.setDisable(true);

        Searchresult result;
        switch (algo) {
            case "UCS (Dijkstra)"   -> result = UcsSearch.search(graph, src, dst);
            case "Best-First Search"-> result = BestFirstSearch.search(graph, src, dst, h);
            default                 -> result = Astarsearch.search(graph, src, dst, h);
        }

        lastResult = result;

        if (result == null || !result.isFound()) {
            statusLabel.setText("✗ Aucun chemin trouvé entre " + src + " et " + dst);
            logArea.setText("Aucun chemin trouvé.\n");
            drawMap(Collections.emptyList(), Collections.emptyList());
            runBtn.setDisable(false);
            return;
        }

        // Update results panel
        costLabel.setText(String.format("%.0f km", result.getTotalCost()));
        nodesLabel.setText(result.getNodesExplored() + " nœuds");

        StringBuilder pathStr = new StringBuilder();
        List<tp73.Node> path = result.getPath();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) pathStr.append(" → ");
            pathStr.append(path.get(i).getName());
        }
        pathLabel.setText(pathStr.toString());

        // Log
        StringBuilder log = new StringBuilder();
        log.append("Algorithme : ").append(algo).append("\n");
        log.append("Route      : ").append(src).append(" → ").append(dst).append("\n\n");
        result.getTraceLog().forEach(entry -> log.append(entry).append("\n"));
        log.append("\n══════════════════════════════\n");
        log.append("CHEMIN : ").append(pathStr).append("\n");
        log.append("COÛT   : ").append((int)result.getTotalCost()).append(" km\n");
        log.append("NŒUDS  : ").append(result.getNodesExplored()).append("\n");
        logArea.setText(log.toString());
        logArea.setScrollTop(Double.MAX_VALUE);

        // Build explored list for coloring
        List<tp73.Node> explored = new ArrayList<>();
        result.getTraceLog().forEach(entry -> {
            // Extract closed list nodes from log
        });

        // Animate path drawing
        animPath = result.getPath();
        animStep = 0;
        statusLabel.setText("✅  " + algo + " — " + src + " → " + dst +
                            " | Coût: " + (int)result.getTotalCost() + " km | Nœuds: " + result.getNodesExplored());

        animateResult(result);
        runBtn.setDisable(false);
    }

    private void animateResult(Searchresult result) {
        // Show explored nodes progressively then final path
        List<tp73.Node> explored = collectExplored(result);

        Timeline timeline = new Timeline();
        int stepMs = Math.max(150, 800 / Math.max(1, explored.size()));

        for (int i = 0; i <= explored.size(); i++) {
            final int step = i;
            KeyFrame kf = new KeyFrame(Duration.millis(step * stepMs), e -> {
                List<tp73.Node> shown = explored.subList(0, step);
                drawMap(step == explored.size() ? result.getPath() : Collections.emptyList(), shown);
            });
            timeline.getKeyFrames().add(kf);
        }
        timeline.play();
    }

    private List<tp73.Node> collectExplored(Searchresult result) {
        // Parse trace log to find explored nodes in order
        List<tp73.Node> list = new ArrayList<>();
        for (String entry : result.getTraceLog()) {
            for (String line : entry.split("\n")) {
                if (line.startsWith("Nœud courant :")) {
                    String name = line.split(":")[1].trim().split("\\|")[0].trim();
                    tp73.Node n = graph.getNode(name);
                    if (n != null) list.add(n);
                }
            }
        }
        return list;
    }

    private Map<String, Double> computeHeuristics(String destName) {
        // Use TP table if destination is Tozeur, else compute euclidean
        if ("Tozeur".equals(destName)) {
            return GraphFactory.getHeuristicsToTozeur();
        }
        // Generic euclidean (Tunisia ≈ 750km tall, 350km wide)
        return GraphFactory.computeEuclideanHeuristics(graph, destName, 350, 750);
    }

    private void clearAll() {
        costLabel.setText("—");
        nodesLabel.setText("—");
        pathLabel.setText("—");
        logArea.setText("Sélectionnez départ, arrivée et algorithme, puis cliquez ▶\n");
        statusLabel.setText("Réinitialisé");
        drawMap(Collections.emptyList(), Collections.emptyList());
        lastResult = null;
    }

    // ═══════════════════════════════════════════════════════════════
    // UI HELPERS
    // ═══════════════════════════════════════════════════════════════
    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        l.setTextFill(TEXT_SEC);
        l.setPadding(new Insets(0, 0, 4, 0));
        l.setStyle("-fx-border-color: #30363d; -fx-border-width: 0 0 1 0;");
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private <T> ComboBox<T> styledCombo(List<T> items, T selected) {
        ComboBox<T> cb = new ComboBox<>(FXCollections.observableArrayList(items));
        cb.setValue(selected);
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setStyle(
            "-fx-background-color: #0d1117;" +
            "-fx-text-fill: #e6edf3;" +
            "-fx-border-color: #30363d;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-font-family: Consolas;"
        );
        return cb;
    }

    private HBox formRow(String labelText, javafx.scene.control.Control control) {
        Label l = new Label(labelText);
        l.setFont(Font.font("Consolas", 12));
        l.setTextFill(TEXT_SEC);
        l.setPrefWidth(80);
        HBox row = new HBox(8, l, control);
        HBox.setHgrow(control, Priority.ALWAYS);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Button actionButton(String text, Color bg) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
        btn.setStyle(String.format(
            "-fx-background-color: #%02x%02x%02x;" +
            "-fx-text-fill: #e6edf3;" +
            "-fx-padding: 10 16;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;",
            (int)(bg.getRed()*255), (int)(bg.getGreen()*255), (int)(bg.getBlue()*255)
        ));
        return btn;
    }

    private HBox resultRow(String label, Label value) {
        Label l = new Label(label + " :");
        l.setFont(Font.font("Consolas", 11));
        l.setTextFill(TEXT_SEC);
        l.setPrefWidth(110);
        HBox row = new HBox(6, l, value);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label resultLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        l.setTextFill(ACCENT);
        return l;
    }

    private javafx.scene.Node spacer(double h) {
        Region r = new Region();
        r.setPrefHeight(h);
        return r;
    }

    private VBox buildHeuristicTable() {
        VBox box = new VBox(2);
        String[] cities = {"Tunis","Sousse","Kairouan","Sfax","Gafsa","Gabès","El Kef","Tozeur"};
        for (String city : cities) {
            Double hVal = GraphFactory.getHeuristicsToTozeur().get(city);
            HBox row = new HBox();
            row.setStyle("-fx-background-color: #0d1117; -fx-padding: 2 6; -fx-background-radius: 3;");
            Label cityL = new Label(city);
            cityL.setFont(Font.font("Consolas", 11));
            cityL.setTextFill(TEXT_PRI);
            cityL.setPrefWidth(90);
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Label hL = new Label(hVal != null ? (int)(double)hVal + " km" : "—");
            hL.setFont(Font.font("Consolas", 11));
            hL.setTextFill(WARN);
            row.getChildren().addAll(cityL, sp, hL);
            box.getChildren().add(row);
        }
        return box;
    }

    private VBox buildLegend() {
        VBox box = new VBox(5);
        box.setStyle(" -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #30363d; -fx-border-radius: 8;");

        Label title = new Label("Légende");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        title.setTextFill(TEXT_SEC);
        box.getChildren().add(title);

        box.getChildren().addAll(
            legendRow("⬤", ACCENT,  "Départ / Chemin optimal"),
            legendRow("⬤", DANGER,  "Destination"),
            legendRow("⬤", WARN,    "Nœud exploré"),
            legendRow("⬤", ACCENT2, "Nœud non visité")
        );
        return box;
    }

    private HBox legendRow(String icon, Color color, String text) {
        Label ico = new Label(icon);
        ico.setTextFill(color);
        ico.setFont(Font.font(12));
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Consolas", 10));
        lbl.setTextFill(TEXT_SEC);
        HBox row = new HBox(6, ico, lbl);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private String inlineCSS() {
        return "";
    }

    public static void main(String[] args) {
        launch(args);
    }
}