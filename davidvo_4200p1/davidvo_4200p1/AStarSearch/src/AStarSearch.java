import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.io.File;

class AStarSearch {
	private static final int[][] goalState = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 } };
	private static final int SINGLE_TEST_PUZZLE = 1;
	private static final int MULTI_TEST_PUZZLE = 2;
	private static long startTime; // Variable to store the start time
	private static long endTime; // Variable to store the end time

	// Comparator to order nodes in the priority queue based on their cost
	private static final Comparator<Node> costComparator = Comparator.comparingInt(a -> a.cost);

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		int option = 0;

		while (option != 3) {
			System.out.println("[1] Single Test Puzzle");
			System.out.println("[2] Multi-Test Puzzle");
			System.out.println("[3] Exit");
			System.out.print("Choose an option: ");
			option = scanner.nextInt();

			if (option == SINGLE_TEST_PUZZLE) {
				solveSinglePuzzle(scanner);
			} else if (option == MULTI_TEST_PUZZLE) {
				solveMultiPuzzles(scanner);
			} else if (option == 3) {
				System.out.println("Exiting...");
			} else {
				System.out.println("Invalid option. Please choose again.");
			}
		}

		scanner.close();
	}

	private static void solveSinglePuzzle(Scanner scanner) {

		System.out.println("Select Input Method:\n[1] Random\n[2] File");
		int option = scanner.nextInt();
		int depth;

		int[][] initialPuzzle;
		if (option == 1) {
			initialPuzzle = generateRandomPuzzle();

			System.out.print("Enter Solution Depth (2-20): ");
			depth = scanner.nextInt();

			if (depth < 2 || depth > 20) {
				System.out.println("Invalid depth. The depth should be between 2 and 20.");
				return;
			}

			System.out.println("Initial Puzzle:");
			printPuzzle(initialPuzzle);
		} else if (option == 2) {
			System.out.println("Enter the 8-puzzle configuration (3 rows, space-separated integers in each row):");
			initialPuzzle = readSpecificPuzzle(scanner);
			System.out.print("Enter Solution Depth (2-20): ");
			depth = scanner.nextInt();

			if (depth < 2 || depth > 20) {
				System.out.println("Invalid depth. The depth should be between 2 and 20.");
				return;
			}
		} else {
			System.out.println("Invalid option. Exiting...");
			return;
		}

		System.out.println("Select Heuristic:\n[1] H1\n[2] H2");
		int heuristicOption = scanner.nextInt();

		HeuristicFunction heuristic;
		if (heuristicOption == 1) {
			heuristic = AStarSearch::calculateHeuristic;
		} else if (heuristicOption == 2) {
			heuristic = AStarSearch::calculateHeuristic2;
		} else {
			System.out.println("Invalid heuristic option. Exiting...");
			return;
		}

		// Check solvability only once at the beginning
		if (!isSolvable(initialPuzzle)) {
			System.out.println("Puzzle is not solvable.");
			return;
		}

		boolean solutionFound = false; // Variable to track if a solution is found

		for (int d = 2; d <= depth; d++) {
			startTime = System.currentTimeMillis(); // Record start time
			int searchCost = runAStarSearchWithDepth(initialPuzzle, heuristic, d);
			endTime = System.currentTimeMillis(); // Record end time

			if (searchCost >= 0) {
				System.out.println("Search Cost: " + searchCost);
				System.out.println("Time taken: " + (endTime - startTime) + " milliseconds\n");
				solutionFound = true;
				break; // Goal state reached, break out of the loop
			}
		}

		if (!solutionFound) {
			System.out.println("Puzzle is not solvable.");
		}
	}

	private static int runAStarSearchWithDepth(int[][] initialPuzzle, HeuristicFunction heuristic, int depth) {
		if (!isSolvable(initialPuzzle)) {
			System.out.println("Puzzle is not solvable.");
			return -1; // Return -1 for invalid puzzles
		}
		PriorityQueue<Node> openList = new PriorityQueue<>(costComparator);
		HashSet<String> closedList = new HashSet<>();

		Node initialNode = new Node(initialPuzzle);
		initialNode.cost = calculateCost(initialNode, heuristic);
		openList.add(initialNode);

		int searchCost = 0; // Variable to track the search cost

		while (!openList.isEmpty()) {
			Node currentNode = openList.poll();
			closedList.add(nodeToString(currentNode));
			searchCost++; // Increment the search cost for each expanded node

			if (isGoalState(currentNode.puzzle)) {
				// Check if the goal state is reached at the specified depth
				if (currentNode.level <= depth) {
					printSolution(currentNode); // Call with only the Node argument
					return searchCost; // Return the search cost immediately if the solution is found
				}
				break; // Solution found, break out of the loop
			}

			// Generate child nodes and add them to the open list if not already explored
			int[][] moves = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } }; // Up, Down, Left, Right
			for (int[] move : moves) {
				int newX = currentNode.blankX + move[0];
				int newY = currentNode.blankY + move[1];

				if (isValidMove(newX, newY)) {
					int[][] newPuzzle = new int[3][3];
					for (int i = 0; i < 3; i++) {
						System.arraycopy(currentNode.puzzle[i], 0, newPuzzle[i], 0, 3);
					}
					// Swap the blank space with the neighboring tile
					newPuzzle[currentNode.blankX][currentNode.blankY] = newPuzzle[newX][newY];
					newPuzzle[newX][newY] = 0;

					Node childNode = new Node(newPuzzle);
					childNode.cost = calculateCost(childNode, heuristic);
					childNode.level = currentNode.level + 1;
					childNode.parent = currentNode;
					childNode.blankX = newX;
					childNode.blankY = newY;

					if (!closedList.contains(nodeToString(childNode)) && childNode.level <= depth) {
						openList.add(childNode);
					}
				}
			}
		}
		return -1; // Return the search cost after the search is completed
	}

	private static void solveMultiPuzzles(Scanner scanner) {
		System.out.println("Select Heuristic:\n[1] Heuristic 1\n[2] Heuristic 2");
		int heuristicOption = scanner.nextInt();
		int totalSearchCost = 0; // Variable to store the sum of search costs for all puzzles
	    long totalTime = 0; // Variable to store the sum of times for all puzzles
	    int numSolvedPuzzles = 0; // Variable to track the number of solved puzzles

		HeuristicFunction heuristic;
		if (heuristicOption == 1) {
			heuristic = AStarSearch::calculateHeuristic;
		} else if (heuristicOption == 2) {
			heuristic = AStarSearch::calculateHeuristic2;
		} else {
			System.out.println("Invalid heuristic option. Exiting...");
			return;
		}

		System.out.println("Select Input Method:\n[1] Random\n[2] File");
		int option = scanner.nextInt();

		System.out.print("Enter Solution Depth (2-20): ");
		int depth = scanner.nextInt();

		if (depth < 2 || depth > 20) {
			System.out.println("Invalid depth. The depth should be between 2 and 20.");
			return;
		}

		if (option == 1) {
			System.out.print("Enter the number of puzzles to solve: ");
			int numPuzzles = scanner.nextInt();
			for (int i = 0; i < numPuzzles; i++) {
				System.out.println("/////////////////////////////\nSolving Puzzle " + (i + 1));
				int[][] initialPuzzle = generateRandomPuzzle();

				System.out.println("Initial Puzzle:");
				printPuzzle(initialPuzzle);

				// Check solvability only once at the beginning
				if (!isSolvable(initialPuzzle)) {
					System.out.println("Puzzle is not solvable.");
					continue; // Move to the next puzzle if the current puzzle is not solvable
				}

				boolean solutionFound = false; // Variable to track if a solution is found			
			    for (int d = 2; d <= depth; d++) {
					startTime = System.currentTimeMillis(); // Record start time
					int searchCost = runAStarSearchWithDepth(initialPuzzle, heuristic, d);
					endTime = System.currentTimeMillis(); // Record end time

					if (searchCost >= 0) {
						System.out.println("Search Cost: " + searchCost);
						System.out.println("Time taken: " + (endTime - startTime) + " milliseconds\n");
						solutionFound = true;
					}
				}

				if (!solutionFound) {
					System.out.println("Puzzle is not solvable.");
				}
			}
		} else if (option == 2) {
			System.out.print("Enter the file path containing the puzzles: ");
			String filePath = scanner.next();

			List<int[][]> puzzles = readPuzzlesFromFile(filePath);
			if (puzzles.isEmpty()) {
				System.out.println("No valid puzzles found in the file.");
				return;
			}

			for (int i = 0; i < puzzles.size(); i++) {
				int[][] initialPuzzle = puzzles.get(i);
				System.out.println("/////////////////////////////\nSolving Puzzle " + (i + 1));

				System.out.println("Initial Puzzle:");
				printPuzzle(initialPuzzle);

		        // Check solvability only once at the beginning
		        if (!isSolvable(initialPuzzle)) {
		            System.out.println("Puzzle is not solvable.");
		            continue; // Move to the next puzzle if the current puzzle is not solvable
		        }

		        boolean solutionFound = false; // Variable to track if a solution is found
		        int searchCost = -1; // Variable to store the search cost for the current puzzle

		        for (int d = 2; d <= depth; d++) {
		            startTime = System.currentTimeMillis(); // Record start time
		            searchCost = runAStarSearchWithDepth(initialPuzzle, heuristic, d);
		            endTime = System.currentTimeMillis(); // Record end time

		            if (searchCost >= 0) {
		                System.out.println("Search Cost: " + searchCost);
		                System.out.println("Time taken: " + (endTime - startTime) + " milliseconds\n");
		                solutionFound = true;
		                break;
		            }
		        }

		        if (solutionFound) {
		            totalSearchCost += searchCost;
		            totalTime += (endTime - startTime);
		            numSolvedPuzzles++;
		        } else {
		            System.out.println("Puzzle is not solvable.");
		        }
		    }

		    if (numSolvedPuzzles > 0) {
		        // Calculate and print the average search cost and average time
		        double averageTime = (double) totalTime / numSolvedPuzzles;
		        
		        System.out.println("\n------------------------");
		        System.out.println("Average Search Cost: " + (totalSearchCost / numSolvedPuzzles));
		        System.out.println("Average Time Taken: " + String.format("%.2f", averageTime) + " milliseconds");
		    } else {
		        System.out.println("\nNo puzzles were solved.");
		    }
		}
	}

	private static boolean isSolvable(int[][] puzzle) {
		int[] flattenedPuzzle = new int[9];
		int k = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				flattenedPuzzle[k++] = puzzle[i][j];
			}
		}

		int inversions = 0;
		for (int i = 0; i < 8; i++) {
			for (int j = i + 1; j < 9; j++) {
				if (flattenedPuzzle[i] != 0 && flattenedPuzzle[j] != 0 && flattenedPuzzle[i] > flattenedPuzzle[j]) {
					inversions++;
				}
			}
		}

		return inversions % 2 == 0;
	}

	private static int[][] generateRandomPuzzle() {
		int[] values = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
		Random random = new Random();

		// Shuffle the values array to get a random permutation
		for (int i = values.length - 1; i > 0; i--) {
			int index = random.nextInt(i + 1);
			int temp = values[index];
			values[index] = values[i];
			values[i] = temp;
		}

		int[][] puzzle = new int[3][3];
		int k = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				puzzle[i][j] = values[k++];
			}
		}

		return puzzle;
	}

	private static int[][] readSpecificPuzzle(Scanner scanner) {
		int[][] puzzle = new int[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				puzzle[i][j] = scanner.nextInt();
			}
		}
		return puzzle;
	}

	private static int calculateCost(Node node, HeuristicFunction heuristic) {
		int h = heuristic.calculateHeuristic(node);
		// Calculate the total cost using the heuristic value (h(n)) and the depth of
		// the node (g(n))
		return node.level + h;
	}

	private static int calculateHeuristic(Node node) {
		// Calculate the heuristic value using the Hamming distance
		int heuristic = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (node.puzzle[i][j] != 0) {
					int goalX = (node.puzzle[i][j] - 1) / 3;
					int goalY = (node.puzzle[i][j] - 1) % 3;
					heuristic += Math.abs(i - goalX) + Math.abs(j - goalY);
				}
			}
		}
		return heuristic;
	}

	private static int calculateHeuristic2(Node node) {
		// Calculate the heuristic value using the Manhattan distance
		int heuristic = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (node.puzzle[i][j] != 0 && node.puzzle[i][j] != goalState[i][j]) {
					heuristic++;
				}
			}
		}
		return heuristic;
	}

	private static boolean isGoalState(int[][] puzzle) {
		// Check if the puzzle state matches the goal state
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (puzzle[i][j] != goalState[i][j]) {
					return false;
				}
			}
		}
		return true;
	}

	private static void printSolution(Node node) {
		Node tempNode = node;
		List<Node> solutionSteps = new ArrayList<>();

		// Reconstruct and store the path from the initial state to the goal state
		while (tempNode != null) {
			solutionSteps.add(tempNode);
			tempNode = tempNode.parent;
		}

		System.out.println("\nSolution Found:");
		// Print the solution steps
		for (int i = solutionSteps.size() - 1; i >= 0; i--) {
			System.out.println("Step " + (solutionSteps.size() - i - 1) + ":");
			printPuzzle(solutionSteps.get(i).puzzle);
			System.out.println();
		}
	}

	private static void printPuzzle(int[][] puzzle) {
		for (int[] row : puzzle) {
			for (int num : row) {
				System.out.print(num + " ");
			}
			System.out.println();
		}
	}

	private static boolean isValidMove(int x, int y) {
		// Check if the move is within the bounds of the puzzle grid
		return x >= 0 && x < 3 && y >= 0 && y < 3;
	}

	private static String nodeToString(Node node) {
		// Convert the puzzle state to a string representation for hashing
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				sb.append(node.puzzle[i][j]);
			}
		}
		return sb.toString();
	}

	@FunctionalInterface
	private interface HeuristicFunction {
		int calculateHeuristic(Node node);
	}

	private static List<int[][]> readPuzzlesFromFile(String filePath) {
		List<int[][]> puzzles = new ArrayList<>();

		try (Scanner fileScanner = new Scanner(new File(filePath))) {
			int[][] puzzle = null;
			int lineCount = 0;

			while (fileScanner.hasNextLine()) {
				String line = fileScanner.nextLine().trim();

				if (line.startsWith("/////////////////////////////////////////////////////")) {
					// Separator line found, add the puzzle to the list
					if (puzzle != null && lineCount == 3) {
						puzzles.add(puzzle);
					}
					puzzle = new int[3][3];
					lineCount = 0;
				} else if (puzzle != null) {
					// Read the puzzle configuration from the line
					String[] numbers = line.split("\\s+");
					if (numbers.length != 3) {
						continue; // Skip invalid lines that do not contain exactly 3 numbers
					}

					for (int i = 0; i < numbers.length; i++) {
						puzzle[lineCount][i] = Integer.parseInt(numbers[i]);
					}

					lineCount++;
				}
			}

			// Add the last puzzle to the list (if any)
			if (puzzle != null && lineCount == 3) {
				puzzles.add(puzzle);
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + filePath);
		}

		return puzzles;
	}

	static class Node {
		int[][] puzzle;
		int cost; // Total cost: g(n) + h(n)
		int level; // Depth of the node in the search tree
		Node parent; // Parent node to reconstruct the path
		int blankX; // X-coordinate of the blank space
		int blankY; // Y-coordinate of the blank space

		// Constructor to initialize the puzzle state
		Node(int[][] puzzle) {
			this.puzzle = puzzle;
			this.cost = 0;
			this.level = 0;
			this.parent = null;
			this.blankX = 0;
			this.blankY = 0;
			findBlankPosition();
		}

		private void findBlankPosition() {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					if (puzzle[i][j] == 0) {
						blankX = i;
						blankY = j;
						return;
					}
				}
			}
		}
	}
}