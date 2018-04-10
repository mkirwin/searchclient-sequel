package searchclient;

import java.util.Comparator;

import searchclient.NotImplementedException;

public abstract class Heuristic implements Comparator<Node> {
	char[][] goals;

	public Heuristic(Node initialState, char[][] goals) {
		this.goals = goals;
		// Here's a chance to pre-process the static parts of the level.
	}

	public int h(Node n) {
		int returnSum = 0;
		for (int row = 0; row < n.maxRow; row++) {
			for (int col = 0; col < n.maxCol; col++) {
				if (this.goals[row][col] == Character.toLowerCase(n.boxes[row][col])) {
					returnSum++;
				}
			}
		}
		return returnSum;
	}

	public abstract int f(Node n);

	@Override
	public int compare(Node n1, Node n2) {
		return this.f(n1) - this.f(n2);
	}

	public static class AStar extends Heuristic {
		public AStar(Node initialState, char[][] goals) {
			super(initialState, goals);
		}

		@Override
		public int f(Node n) {
			return n.g() + this.h(n);
		}

		@Override
		public String toString() {
			return "A* evaluation";
		}
	}

	public static class WeightedAStar extends Heuristic {
		private int W;

		public WeightedAStar(Node initialState, char[][] goals, int W) {
			super(initialState, goals);
			this.W = W;
		}

		@Override
		public int f(Node n) {
			return n.g() + this.W * this.h(n);
		}

		@Override
		public String toString() {
			return String.format("WA*(%d) evaluation", this.W);
		}
	}

	public static class Greedy extends Heuristic {
		public Greedy(Node initialState, char[][] goals) {
			super(initialState, goals);
		}

		@Override
		public int f(Node n) {
			return this.h(n);
		}

		@Override
		public String toString() {
			return "Greedy evaluation";
		}
	}
}
