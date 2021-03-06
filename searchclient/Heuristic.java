package searchclient;

import java.util.*;

import searchclient.NotImplementedException;

public abstract class Heuristic implements Comparator<Node> {
    char[][] goals;
    int maxRow;
    int maxCol;
    int BIG_NUMBER_CONST = 100000000;

    // Note: character is a goal represented by an lowercase letter, and the value is is a list of locations of that goal
    // This stores a given goal (character) and all of the points at which that goal is found.
    HashMap<Character, ArrayList<Point>> goalLocations;

    /** pointDistances is a representation of how we are going to store the distances between points. We will have a grid that is the
     * same size as the level. Then for each coordinate on the grid, there will be another grid that is the same size as the
     * level grid.*/
    /*        0 1 2
              0 X + +
              1 + + +
              2 + + +
              In the X grid the following grid is stored; the value at each coordinate gives the distance between the '0-point'
              and the given coordinate.
              0 1 2
              1 2 3
              2 3 4
              */

    // Declare pointDistances, which will store our 'map' of the level. Now we can reference the "Real" Distance between two points
    // without calculating each time.
    int[][][][] pointDistances;

    /**
     * Constructor for Heuristic
     */
    public Heuristic(Node initialState, char[][] goals, boolean[][] walls) {
        // Here's a chance to pre-process the static parts of the level.
        this.goals = goals;

        this.maxRow = initialState.maxRow;
        this.maxCol = initialState.maxCol;

        // Here we make hashmap of goals for efficient look up of nearest goal
        this.goalLocations = new HashMap<>();

        // Instantiate pointDistances since it was only declared before the invocation of the Heuristic constructor.
        this.pointDistances = new int[maxRow][maxCol][maxRow][maxCol];

        // Here we find the longest row length of the level, through the goals-representation.
        int longestRowLength = 0;
        for (int i = 0; i < goals.length; i++) {
            if (goals[i].length > longestRowLength) {
                longestRowLength = goals[i].length;
            }
        }

        // Number of rows in goals
        int rowCount = goals.length;
        // Technically redundant but easier to read later code
        int columnCount = longestRowLength;

        /**
         * When we store our point distances, we need to take walls into account. So, we will store a
         *  'dummy' grid to as a wall to indicate that that particular coordinate is not to be used in
         *  calculating the "Real" Distance.
         */
        // The 'dummy' wall, AKA a filler object if a point is a wall in the point distances array object
        int[][] wallFiller = new int[rowCount][columnCount];
        int WALL_INT_CONSTANT = BIG_NUMBER_CONST;

        // Populate wall filler
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                wallFiller[row][col] = WALL_INT_CONSTANT;
            }
        }

        // Loop through the nested goals array to find and store goal locations
        //System.err.println("Max row is " + goals.length);
        for (int row = 0; row < goals.length; row++) {
            for (int col = 0; col < goals[row].length; col++) {
                char currentChar = goals[row][col];

                // Determine the "Real" distance from one point to another for every point in the array
                /***********************************************************************************/

                // If current value is a wall, then add the wall filler to pointDistances.
                if (walls[row][col]) {
                    pointDistances[row][col] = wallFiller;
                }

                // If it is not a wall then do BFS to find distance from current point to
                // all other non-wall points and store the results.
                else {
                    // Represents the grid that is the distance between current point and other points

                    /** Start BFS from the current coordinate.
                     *  Note that the first part of this code-chunk is not performing BFS.
                     */
                    // 1. First, we do a simple calculation from the 'beginning' of the grid, (0,0)
                    Queue<PointNode> frontier = new LinkedList<>();
                    boolean[][] frontierSet = new boolean[maxRow][maxCol];

                    // keep track of points already visited
                    boolean[][] visited = new boolean[maxRow][maxCol];

                    // if point is not 0,0 we want to back trace already performed calculations
                    if (row != 0 && col != 0) {
                        for (int i = 0; i <= row; i++) {
                            for (int j = 0; j < col; j++) {
                                //if point is a wall add a filler max int
                                if (pointDistances[i][j] == wallFiller) {
                                    pointDistances[i][j][i][j] = WALL_INT_CONSTANT;
                                }
                                //otherwise see previously calculated values
                                else {
                                    pointDistances[i][j][i][j] = distanceBetweenTwoPoints(i, j, row, col);
                                    visited[i][j] = true;
                                }
                            }
                        }
                    }

                    // 2. Now, fill in the rest of the grid with BFS.
                    frontier.add(new PointNode(row, col, -1));
                    frontierSet[row][col] = true;
                    while(!frontier.isEmpty()){
                        PointNode currentPointNode = frontier.poll();
                        int currentX = currentPointNode.getX();
                        int currentY = currentPointNode.getY();
                        Point currentPoint = new Point(currentX, currentY);
                        frontierSet[row][col] = false;

                        // Update grid
                        // Check if current point is a wall - if it is add wall filler value
                        if (walls[currentX][currentY]){
                            pointDistances[row][col][currentX][currentY] = WALL_INT_CONSTANT;
                        }
                        /**
                         * Here, if the current point is not a wall, we continue with the BFS algorithm.
                         *   BFS will follow the 'children' of the current coordinate points.
                         *   These are the points directly to the left, right, top, and bottom.
                         */
                        else {
                            int currentDistance = currentPointNode.getPreviousDistance() + 1;
                            pointDistances[row][col][currentX][currentY] = currentDistance;

                            /* Add points around it to frontier if they aren't already visited or in frontier.
                             * This will be in a 4-unit 'cross' around the current point. The coordinates directly
                             * above, below, to the left, and to the right will be checked. */

                            // See if point above is anything
                            if (currentY > 0) {
                                int x = currentX;
                                int y = currentY-1;
                                if(!visited[x][y] && !frontierSet[x][y]){
                                    frontier.add(new PointNode(x, y, currentDistance));
                                    frontierSet[x][y] = true;
                                }
                            }
                            // See if point below is anything
                            if (currentY < walls.length-2) {
                                int x = currentX;
                                int y = currentY+1;
                                if(!visited[x][y] && !frontierSet[x][y]){
                                    frontier.add(new PointNode(x, y, currentDistance));
                                    frontierSet[x][y] = true;
                                }
                            }
                            // See if point to the left is anything
                            if (currentX > 0) {
                                int x = currentX-1;
                                int y = currentY;
                                if(!visited[x][y] && !frontierSet[x][y]){
                                    frontier.add(new PointNode(currentX-1, currentY, currentDistance));
                                    frontierSet[x][y] = true;
                                }
                            }
                            //see if point to the right is anything
                            if (currentX < walls[0].length-2) {
                                int x = currentX+1;
                                int y = currentY;
                                if(!visited[x][y] && !frontierSet[x][y]){
                                    frontier.add(new PointNode(x, y, currentDistance));
                                    frontierSet[x][y] = true;
                                }
                            }
                        }
                        // Mark the current point as visited
                        visited[currentX][currentY] = true;
                    }
                }

                /***********************************************************************************/
                //The following code-chunk is finding all the locations of goals and adding to
                // the hashmap tracking goal locations.
                /***********************************************************************************/
                if (currentChar != '\u0000') {
                    // Set array with location of point
                    Point currentLocation = new Point(row, col);
                    ArrayList<Point> currentLocationsOfGoal;
                    // If there are already locations of char listed, then find them
                    if (goalLocations.containsKey(currentChar)) {
                        currentLocationsOfGoal = goalLocations.get(currentChar);
                    }
                    // If there are not locations of char found initialize a new array containing them
                    else {
                        currentLocationsOfGoal = new ArrayList<>();
                    }
                    // Then add new location found to list of locations
                    currentLocationsOfGoal.add(currentLocation);
                    // And add updated locations to hashmap
                    goalLocations.put(currentChar, currentLocationsOfGoal);
                }
            }
        }
    }

    // Finds manhattan distance of two points aka x distance away + y distance away
    public int manhattanDistance(int x1, int y1, int x2, int y2){
        return (x1 - x2) + (y2 - y1);
    }

    // Find the shortest "Real" Distance between a point and and another point.
    public int distanceBetweenTwoPoints(int x1, int y1, int x2, int y2){
        return pointDistances[x1][y1][x2][y2];
    }

    public int h(Node n) {
        // Track goal node and closest row
        int returnSum = 0;
        char[][] boxes = n.boxes;
        int closestAgentBoxDistance = BIG_NUMBER_CONST;
        for (int row = 0; row < n.maxRow; row++) {
            for (int col = 0; col < n.maxCol; col++) {
                char currentChar = Character.toLowerCase(boxes[row][col]);
                //if current value is a box
                if (currentChar != '\u0000') {
                    if (goalLocations.containsKey(currentChar)) {
                        //see if this box is closest to the agent and if so update closestAgentBoxDistance
                        int distanceToAgent = distanceBetweenTwoPoints(row, col, n.agentRow, n.agentCol);
                        //make sure closest box is not already on a node
                        if (distanceToAgent < closestAgentBoxDistance) {
                            closestAgentBoxDistance = distanceToAgent;
                        }
                        //find goal locations
                        ArrayList<Point> currentGoalLocations = goalLocations.get(currentChar);
                        int closestDistance = BIG_NUMBER_CONST;
                        for (Point location : currentGoalLocations) {
                            int goalRow = location.getX();
                            int goalCol = location.getY();

                            // Here, we use the "Real" Shortest Distance, instead of the Manhattan.
                            int distance = distanceBetweenTwoPoints(row, col, goalRow, goalCol);
                            if (distance < closestDistance) {
                                closestDistance = distance;
                            }
                        }
                        returnSum += closestDistance;
                    }
                }
            }
        }
        if (closestAgentBoxDistance != BIG_NUMBER_CONST) {
            returnSum += closestAgentBoxDistance;
        }

        return returnSum;
    }

    public abstract int f(Node n);

    @Override
    public int compare(Node n1, Node n2) {
        return this.f(n1) - this.f(n2);
    }

    public static class AStar extends Heuristic {
        public AStar(Node initialState, char[][] goals, boolean[][] walls) {
            super(initialState, goals, walls);
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

        public WeightedAStar(Node initialState, char[][] goals, boolean[][] walls, int W) {
            super(initialState, goals, walls);
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
        public Greedy(Node initialState, char[][] goals, boolean[][] walls) {
            super(initialState, goals, walls);
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
