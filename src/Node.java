class Node {
    int[][] puzzle;
    int cost; // Total cost: g(n) + h(n)
    int level; // Depth of the node in the search tree
    Node parent; // Parent node to reconstruct the path

    // Constructor to initialize the puzzle state
    Node(int[][] puzzle) {
        this.puzzle = puzzle;
        this.cost = 0;
        this.level = 0;
        this.parent = null;
    }
}
