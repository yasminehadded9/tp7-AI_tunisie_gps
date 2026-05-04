# 🗺 Mini GPS Tunisie — TP n°7 Intelligence Artificielle

## Description
Application JavaFX de navigation intelligente sur la carte de Tunisie,
implémentant trois algorithmes de recherche de chemin optimal.

## Algorithmes implémentés
- **A\*** (optimal) — f(n) = g(n) + h(n)
- **UCS** — Uniform Cost Search (Dijkstra)
- **Best-First Search** — recherche gloutonne par heuristique

## Fonctionnalités
- Carte interactive de la Tunisie
- Animation étape par étape de l'exploration
- Comparaison visuelle des algorithmes
- Trace d'exécution détaillée (open list / closed list)
- Heuristiques admissibles vers Tozeur

## Villes du graphe
Tunis · Sousse · Kairouan · Sfax · Gafsa · Gabès · El Kef · Tozeur

## Technologies
- Java 21
- JavaFX 21
- Eclipse IDE

## Structure du projet
src/tp73/
├── MainApp.java          # Interface JavaFX principale
├── GraphFactory.java     # Construction du graphe tunisien
├── Graph.java            # Structure de données graphe
├── Node.java             # Nœud avec coordonnées carte
├── Edge.java             # Arête avec poids
├── Astarsearch.java      # Algorithme A*
├── UcsSearch.java        # Algorithme UCS
├── BestFirstSearch.java  # Best-First Search
└── Searchresult.java     # Résultat de recherche
## Auteur
**Yasmine Hadded** — GLSI 2 
