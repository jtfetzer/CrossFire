package Connect4;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class Connect4{

	private static Scanner in = new Scanner(System.in);
	private static final int MAX_WIDTH = 5; // Max width of search
	static final int MAX_DEPTH = 10; // Max depth of search
	static final int MAX_WINS = 50000; // Computer wins with connect 4
	static final int MIN_WINS = -50000; // Human wins with connect 4
	
	static boolean ATTACK_MOVES = false;
	static boolean BLOCK_MOVES = true;
	
	static final int OPEN_3_VALUE = 30000;
	static final int OPEN_L_VALUE = 12000;
	static final int THREE_OF_FOUR_VALUE = 6000;
	static final int TWO_OF_FOUR_VALUE = 4000;
	static int BLOCK_MOST_SPACE_VALUE = 10000;
	static final int ONE_OF_FOUR_VALUE = 500;
	static final double PRE_MULT = .6;
	static final double BLOCK_MULT = .1; 		// reduce value of block moves by this multiplier,
											 	// e.g. a value of .1 will reduce block move values by 
	public static void main(String[] args) { 	// 10% of their attack move value counterparts.
		
		playGame();
		in.close();
	} // end method main
	
	private static void playGame() {
		BoardNode root = null;
		int firstPlayer;
		
//		System.out.print("Would you like to go first? (y/n): ");
//		String playFirst = in.nextLine().toLowerCase();
		
		String playFirst = "y";
		if(playFirst.equals("y")){
			firstPlayer = 1; // human
		} else {
			firstPlayer = 2; // computer
		} // end else
		
		if(firstPlayer == 2){ // computer makes first move
			root = new BoardNode(getRandomStartMove());
			root.printBoard();					//lastMove.column +1 because board starts at column 1						
			System.out.println("\n  My current move is: " + getRow(root.lastMove.row) + (root.lastMove.column + 1));
			root.update(validate(root));
		} else { // human makes first move
			root = new BoardNode(firstPlayer); 
			root.update(validate(firstPlayer, root)); 
		} 

		while(true){
			root.update(chooseMove(root)); // computer
			testGameOver(root);
			root.removeAllChildNodes();
			root.update(validate(root)); // human
			testGameOver(root);
			root.removeAllChildNodes();
			
		} // end while
	} // end method playGame

	private static void testGameOver(BoardNode root) {

		if(!gameOver(root)){
			
			if(root.lastMove.player == 2){
				System.out.println("\n  My current move is: " + getRow(root.lastMove.row) + (root.lastMove.column + 1));
			}
		} else {
			if(possibleConnect4(root, root.lastMove)){
				root.printBoard();
				printWinner(root.lastMove.player);
				System.exit(0);
			} // end if
			if(root.isStalemate()){
				
				if(root.lastMove.player != 1){
					root.printBoard();
				}
				System.out.println("-Draw!");
				System.exit(0);
			} // end if
		} // end else
	} // end method testGameOver

	private static Move validate(int player, BoardNode node) { // used for human first move
		int row = 0, column = 0;
		String moveString;
		while(true) {

			node.printBoard();
			System.out.print("\n  Choose your next move: ");
			moveString = in.nextLine();
			
			if(moveString.toLowerCase().equals("l")){ // load
				node.board = load();
				node.restoreObject();
				node.printBoard();
				System.out.print("\n  Choose your next move: ");
				moveString = in.nextLine();
			} else if(moveString.length() < 2 || moveString.length() > 2 ){
				continue;
			}
			
			row = getRow(String.valueOf(moveString.charAt(0)).toLowerCase());
			column = Integer.valueOf(String.valueOf(moveString.charAt(1))) - 1;
			if(row > 7 || column > 7){
				System.out.println("  Invalid entry, try again.");
			} else {
				if(node.hasMoveBeenPlayed(moveString)) {
					System.out.println("  ~ " + getRow(row) + (column + 1)+ " is already taken.");
				} else {
					return new Move(row, column, player, null); 
				}
			} // end else
		} // end while
	} // end method validate
	
	private static Move validate(BoardNode node) {
		int row = 0, column = 0;
		int player = 2 / node.lastMove.player;
		String moveString;
		
		if(node.isStalemate()){
			System.out.println("Stalemate!");
			System.exit(0);
		} // end if
		while(true) {
			node.printBoard();
			System.out.print("\n  Choose your next move: ");
			moveString = in.nextLine();
			if(moveString.length() < 2 || moveString.length() > 2 ){
				continue;
			}
			if(moveString.toLowerCase().equals("save")){ // Remove, unused.
				save(node); 	// Used in testing. Saves the game at the position 
				System.exit(0); // the board was in before the last piece was played.
			} 			
			row = getRow(String.valueOf(moveString.charAt(0)).toLowerCase());
			column = Integer.valueOf(String.valueOf(moveString.charAt(1))) - 1;
			if(row > 7 || column > 7){ 
				System.out.println("  Invalid entry, try again.");
			} else {
				if(node.hasMoveBeenPlayed(moveString)) {
					System.out.println("  ~ " + getRow(row) + (column + 1)+ " is already taken.");
				} 
				else { // modify to cache results and predict opponent moves
//					if(node.containsChildNode(moveString)){
//						System.out.println("Move String: " + moveString);
//						node.removeAllChildNodesExcept(moveString);
//						System.out.println(node.maxChildNode().lastMove.moveString);
//					} else{
//						node.removeAllChildren();
//					}
					return new Move(row,column,player,null); // human move type does not need to be determined
				}
			} // end else
		} // end while
	} // end method validate
	
	private static int[][] load() {
		BufferedReader br = null;
		FileReader fr = null;
		int[][] board = new int[8][8];
		int xCount = 0, oCount = 0;

		try {

			fr = new FileReader("board.txt");
			br = new BufferedReader(fr);

			br.readLine(); // get rid of A B C .. etc
			String[] line = new String[9];
			for (int i = 0; i < board.length; i++) {
				try {
					line = br.readLine().split(" ");
				} catch (IOException e) {
					e.printStackTrace();
				}
				for (int j = 1; j < board.length + 1; j++) {
					board[i][j-1] = getPlayer(line[j]);
					if(board[i][j-1] == 1){
						++xCount;
					} else if(board[i][j-1] == 2){
						++oCount;
					} // end else if
				} // end for j
			} // end for i
			if(xCount > oCount){ // computer if first player
				for (int i = 0; i < board.length; i++) {
					for (int j = 0; j < board.length; j++) {
						if(board[i][j] == 1){
							board[i][j] = 2;
						} else if(board[i][j] == 2){
							board[i][j] = 1;
						} // end else if
					} // end for j
				} // end for i
			} // end if

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}

		return board;
	} // end method load

	private static int getPlayer(String string) {
		if(string.equals("_")){
			return 0;
		} else if(string.equals("X")){
			return 1;
		} else if(string.equals("O")){
			return 2;
		}
		System.err.println("getPlayer: " + string);
		return 0;
	}

	private static void save(BoardNode node) {
		node.priorState();
		try (ObjectOutputStream oos =
				new ObjectOutputStream(new FileOutputStream("last_game.ser"))) {

			oos.writeObject(node);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // end method save

	static MoveSet getMostPromisingMoves(BoardNode node, boolean block) { // return best ten possible moves
		int player = 2/node.lastMove.player;

		MoveSet bestMoves = possibilities(node, block);
		if(player == 2){
			bestMoves.sort(new MaxMoveComparator());
		} else {
			bestMoves.sort(new MinMoveComparator());
		}
		
		    // MODIFY TO USE A STOCHASTIC BEAM SEARCH?
			// These moves are thought to be the most promising. The number of moves 
			// examined is reduced to reduce the size of the search space.
			// These moves do not yet have evaluated values, as their values 
			// are determined via the minimax algorithm.
			// each move added is a child node and part of the beam search
		if(player == 2){ // computer
			bestMoves.reduceMax(MAX_WIDTH); // perform a stochastic beam search implementing minimax with alpha-beta pruning
		} else {
			bestMoves.reduceMin(MAX_WIDTH);
		}
		return bestMoves;
	} // end getBestMove
	
	public static MoveSet possibilities(BoardNode node, boolean block) {
		// Use a threadpool here
		MoveSet best = new MoveSet();
		MoveSet connect4Positions = getConnect4PositionsList(node, block); // saves all possible 4-in-a-row moves to global list connect4Positions
		if(connect4Positions.size() > 0) {
			return connect4Positions;
		} 
		MoveSet open3Positions = getOpen3PositionsList(node, block);
		MoveSet smallOpenLPositions = getSmallOpenLPositionsList(node, block);
		MoveSet bigOpenLPositions = getBigOpenLPositionsList(node, block);
		MoveSet preSmallOpenLPositions = getPreSmallOpenLPositionsList(node, block);
		MoveSet preBigOpenLPositions = getPreBigOpenLPositionsList(node, block);
		MoveSet threeOfFourPositions = getThreeOfFourPositionsList(node, block);
		MoveSet twoOfFourPositions = getTwoOfFourPositionsList(node, block);
		
		if(open3Positions.size() > 0){
//			if(open3Positions.getNumOpen3() > 0){ // open3Positions contains both open3Positions and preOpen3Positions
//				return open3Positions.getOpen3();			  // check whether this MoveSet contains any open3Positions
//			}
			best.addAll(open3Positions);
		} 
		if(smallOpenLPositions.size() > 0){
			best.addAll(smallOpenLPositions);
		} // end if
		if(bigOpenLPositions.size() > 0){
			best.addAll(bigOpenLPositions);
		}
		if(preSmallOpenLPositions.size() > 0){
			best.addAll(preSmallOpenLPositions);
		}
		if(preBigOpenLPositions.size() > 0){
			best.addAll(preBigOpenLPositions);
		}
		if(threeOfFourPositions.size() > 0){
			best.addAll(threeOfFourPositions);
		}
		if(twoOfFourPositions.size() > 0){
			best.addAll(twoOfFourPositions);
		}
		if(block && node.moves.size() < 2){
			if(node.getNumEmptySpaces() != 0){	// Usually the best response to the first move
				best.addAll(blockMostSpace(node));
			} 
		}
		if(best.size() < MAX_WIDTH){
			best.addAll(getOneOfFourPositionsList(node, node.getZeros()));
		}
		if(best.size() < MAX_WIDTH){
			best.addAll(node.getZeros());
		}
//		best.print("best");
		return best;
	} // end method possibilities
	
	public static Move chooseMove(BoardNode root) { // root.children are sorted into best order

		root.printBoard();
		MoveSet attacks = getMostPromisingMoves(root, false);
		MoveSet blocks = getMostPromisingMoves(root, true);
		
		if(root.moves.size() < 2){ // save time of first move
			return blocks.max();
		}

		if(attacks.max().getValue() > blocks.max().getValue()){
			root.addAll(attacks);
		} else {
			root.addAll(blocks);
		}
//		if(root.getNumEmptySpaces() == 0){ // Change to check to see if game will end in stalemate.
//			if(possibleConnect4(root.board, root.lastMove)){
//				printWinner(root.lastMove.player);
//				System.exit(0);
//			} else {
//				System.out.println("Draw!");
//				System.exit(0);
//			} // end else
//		} // end if
		Move best_block = blocks.max();
		int minWinDepth = MAX_DEPTH + 1;
		int maxWinDepth = MAX_DEPTH + 1;
		Move maxWinMove = null;
		for (int i = 0; i < 2; i++) { // check attacks and blocks
			
			for (int j = 0; j < root.size(); j++) { // each node is evaluated up to Max_Depth
				if(minWinDepth < maxWinDepth){
					min(root.getChild(j), minWinDepth - 1);
				} else {
					min(root.getChild(j), maxWinDepth - 1);
				}
				if(root.getChild(j).value == MAX_WINS){ // attack successful
					if(root.getChild(j).minWinDepth < maxWinDepth){
						maxWinDepth = root.getChild(j).minWinDepth;
						maxWinMove = root.getChild(j).lastMove;
					}
					if(minWinDepth < MAX_DEPTH + 1){
						if(maxWinDepth < minWinDepth){
							return maxWinMove;
						}
					}
				} else if(root.getChild(j).value == MIN_WINS){ // attack successful
					if(root.getChild(j).minWinDepth < minWinDepth){
						minWinDepth = root.getChild(j).minWinDepth;
					}
				}
			} // end for j
			
			if(maxWinDepth < minWinDepth){ // attack successful
				return maxWinMove;
			} else if(root.minChildNodeValue() == MIN_WINS){
				if(i > 0) {
					return best_block;
				}
				root.removeAllChildNodes();
				
				if(attacks.max().getValue() < blocks.max().getValue()){
					root.addAll(attacks);
				} else {
					root.addAll(blocks);
				}
			} 
			else {
				if(i > 0){
					return root.minDepthNodeMove(root.maxChildNodes());
				}
			}
		} // end for i
		return best_block;
	} // end method evaluate
	
	public static void min(BoardNode node, int depth){
		
		if(node.lastMove.getValue() == MAX_WINS){
			node.value = node.lastMove.getValue();
			node.minWinDepth = node.depth;
			return;
		}
		
		if(depth == 0){
			return;
		}
		boolean block;
		MoveSet attacks = getMostPromisingMoves(node, ATTACK_MOVES);
		MoveSet blocks = getMostPromisingMoves(node, BLOCK_MOVES);
		
		if(attacks.min().getValue() < blocks.min().getValue()){
			node.addAll(attacks);
			block = false;
		} else {
			node.addAll(blocks);
			block = true;
		}

		for(int i = 0; i < node.size(); i++){ // most promising nodes
			max(node.getChild(i), depth - 1);
			if(node.getChild(i).value == MAX_WINS){
				if(node.minWinDepth > node.getChild(i).minWinDepth){
					node.minWinDepth = node.getChild(i).minWinDepth;
				}
				node.value = MAX_WINS;
			} else if(node.getChild(i).value == MIN_WINS){
				node.value = MIN_WINS;
				if(node.minWinDepth > node.getChild(i).minWinDepth){
					node.minWinDepth = node.getChild(i).minWinDepth;
				}
				node.removeAllChildNodesExcept(node.getChild(i));
				return;
			} else { // neither player wins 
				if(block){
					node.removeAllChildNodes();
					return;
				}
			}
		} // end for i
		
		if(attacks.min().getValue() > blocks.min().getValue()){
			node.addAll(attacks);
			block = false;
		} else {
			node.addAll(blocks);
			block = true;
		}

		for(int i = 0; i < node.size(); i++){ // most promising nodes
			max(node.getChild(i), depth - 1);
			if(node.getChild(i).value == MAX_WINS){
				if(node.minWinDepth > node.getChild(i).minWinDepth){
					node.minWinDepth = node.getChild(i).minWinDepth;
				}
				node.value = MAX_WINS;
				node.removeAllChildNodes();
				return;
			} else if(node.getChild(i).value == MIN_WINS){
				node.value = MIN_WINS;
				if(node.minWinDepth > node.getChild(i).minWinDepth){
					node.minWinDepth = node.getChild(i).minWinDepth;
				}
				node.removeAllChildNodesExcept(node.getChild(i));
				return;
			} else { // neither player wins 
				if(block){
					node.removeAllChildNodes();
					return;
				}
			} // end else
		} // end for i
			
		node.removeAllChildNodes();
	} // end method min
	
	public static void max(BoardNode node, int depth){

		if(node.lastMove.getValue() == MIN_WINS){
			node.value = node.lastMove.getValue();
			node.minWinDepth = node.depth;
			return;
		}
		if(depth == 0){
			return;
		}
		boolean block;
		MoveSet attacks = getMostPromisingMoves(node, ATTACK_MOVES);
		MoveSet blocks = getMostPromisingMoves(node, BLOCK_MOVES);
		
		if(attacks.max().getValue() > blocks.max().getValue()){
			node.addAll(attacks);
			block = false;
		} else {
			node.addAll(blocks);
			block = true;
		}
		
		for(int i = 0; i < node.size(); i++){ // most promising nodes
			min(node.getChild(i), depth - 1);
			if(node.getChild(i).value == MIN_WINS){
				if(node.minWinDepth > node.getChild(i).minWinDepth){
					node.minWinDepth = node.getChild(i).minWinDepth;
				}
				node.value = MIN_WINS;
			} else if(node.getChild(i).value == MAX_WINS){
				node.value = MAX_WINS;
				if(node.minWinDepth > node.getChild(i).minWinDepth){
					node.minWinDepth = node.getChild(i).minWinDepth;
				}
				node.removeAllChildNodesExcept(node.getChild(i));
				return;
			} else { // neither player wins 
				return;
			} // end else
		} // end for i
		if(attacks.max().getValue() < blocks.max().getValue()){
			node.addAll(attacks);
			block = false;
		} else {
			node.addAll(blocks);
			block = true;
		}
		
		for(int i = 0; i < node.size(); i++){ // most promising nodes
			min(node.getChild(i), depth - 1);
			if(node.getChild(i).value == MIN_WINS){
				if(node.minWinDepth > node.getChild(i).minWinDepth){
					node.minWinDepth = node.getChild(i).minWinDepth;
				}
				
				node.value = MIN_WINS;
				node.removeAllChildNodes();
				return;
			} else if(node.getChild(i).value == MAX_WINS){
				node.value = MAX_WINS;
				
				if(node.minWinDepth > node.getChild(i).minWinDepth){
					node.minWinDepth = node.getChild(i).minWinDepth;
				}
				
				node.removeAllChildNodesExcept(node.getChild(i));
				return;
			} else { // neither player wins 
				if(block){
					node.removeAllChildNodes();
					return;
				}
			} // end else
		} // end for i
			
		node.removeAllChildNodes();
	} // end method max

	private static Move getRandomStartMove() {
		int row = (int) Math.floor(Math.random() * 10);
		int col = (int) Math.floor(Math.random() * 10);
		if(row >= 5) {
			row = 4;
		} else {
			row = 3;
		}
		if(col >= 5){
			col = 4;
		} else {
			col = 3;
		}
		return new Move(3,4,2, new MoveType(MoveType.Type.ONE_4, 30));
	}
	
	public static Move getAnyAvailableMove(BoardNode node) {
		MoveSet zeros = node.getZeros(); // return random available move
		return zeros.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(0, zeros.size()));
	}
	
	public static boolean possibleConnect4(BoardNode node, Move move) {
		int[][] board = node.board;
		int row = move.row;
		int col = move.column;
		int player = move.player;
		boolean isConnect4 = false;
		
			int i = 0, j = 0;
			while((row + i) >= 0 && (board[row + i][col] == player)){ // check up
				--i;
			} // end while
			while((row + j) <= board.length - 1 && (board[row + j][col] == player)){ // check down
				++j;
			} // end while

			if(j - i > 4){ // > 4 since move is counted twice
				isConnect4 = true;
			}				
			
			i = 0;
			j = 0;
			while((col + i) >= 0 && (board[row][col + i] == player)){ // check left
				--i;
			} // end while
			
			while((col + j) < board.length && (board[row][col + j] == player)){ // check right
				++j;
			} // end while
			if(j - i > 4){
				isConnect4 = true;
			} // end if
			
			return isConnect4;
		} // end method connect4
	
	public static MoveSet blockMostSpace(BoardNode node) {
		MoveSet list = new MoveSet();
		int top, left, right, bottom;
		int player = 2/node.lastMove.player;
		int value = BLOCK_MOST_SPACE_VALUE;

		if(player == 1){ // Min
			value = -1*value;
		}
		
		top = getTop(node);
		bottom = getBottom(node);
		left = getLeft(node);
		right = getRight(node);
		Move next = getAnyAvailableMove(node);
		String block = "";
		if(top == 0 && bottom == 0 && right == 0 && left == 0){
			list.addUnique(next);
			return list;
		}
		if(top >= bottom){
			if(left >= right){
				if(top >= left){
					block += "top";
				} 
				if(left >= top){
					block += "left";
				} 
			} else { // right > left
				if(top >= right){
					block += "top";
				} 
				if(right >= top) {
					block += "right";
				} // end else if
			} // end else
		} else if( bottom >= top){
			if(left >= right){
				if(bottom >= left){
					block += "bottom";
				}
				if(left >= bottom){
					block += "left";
				}
			} else if(right >= left){
				if(bottom >= right){
					block += "bottom";
				} 
				if(right >= bottom){
					block += "right";
				}
			} // end else
		} 
		if(block.contains("left")){
			list.addUnique(new Move(node.lastMove.row, node.lastMove.column - 1, player, new MoveType(MoveType.Type.BLOCK_MOST_SPACE_HORIZ, value*left)));
		} 
		if(block.contains("right")){
			list.addUnique(new Move(node.lastMove.row, node.lastMove.column + 1, player, new MoveType(MoveType.Type.BLOCK_MOST_SPACE_HORIZ, value*right)));
		} 
		if(block.contains("top")){
			list.addUnique(new Move(node.lastMove.row - 1, node.lastMove.column, player, new MoveType(MoveType.Type.BLOCK_MOST_SPACE_VERT, value*top)));
		} 
		if(block.contains("bottom")){
			list.addUnique(new Move(node.lastMove.row + 1, node.lastMove.column, player, new MoveType(MoveType.Type.BLOCK_MOST_SPACE_VERT, value*bottom)));
		} // end else
//		list.print("list");
		return list;
	} // end method blockMostSpace

	public static int getRight(BoardNode node) {
		int i = 1;
		if((node.lastMove.column + 1) >= node.board.length) return 0;
		for (i = 1 ; i <= (node.board.length - 1 - node.lastMove.column); i++) {
			if(node.board[node.lastMove.row][node.lastMove.column + i] != 0){
				break;
			} // end if
		} // end for i
		return --i;
	} // end method getRight

	public static int getLeft(BoardNode node) {
		int i = 1;
		if((node.lastMove.column - 1) < 0) return 0;
		for (i = 1 ; i <= node.lastMove.column; i++) {
			if(node.board[node.lastMove.row][node.lastMove.column - i] != 0){
				break;
			} // end if
		} // end for i
		return --i;
	} // end method getLeft

	public static int getTop(BoardNode node) {
		int i = 1;
		if((node.lastMove.row - 1) < 0) return 0;
		for (i = 1 ; i <= node.lastMove.row; i++) {
			if(node.board[node.lastMove.row - i][node.lastMove.column] != 0){
				break;
			} // end if
		} // end for i
		return --i;
	} // end method getTop

	public static int getBottom(BoardNode node) {
		int i = 1;
		if((node.lastMove.row + 1) >= node.board.length) return 0;
		for (i = 1 ; i <= node.board.length - 1 - node.lastMove.row; i++) {
			if(node.board[node.lastMove.row + i][node.lastMove.column] != 0){
				break;
			} // end if
		} // end for i
		return --i;
	} // end method getBottom
	
	public static MoveSet getConnect4PositionsList(BoardNode node, boolean block) { // check if one more move can give a 4 in a row.
		return getConnect4PositionsList(node, (2/node.lastMove.player), block);
	}
	
	public static MoveSet getConnect4PositionsList(BoardNode node, int player, boolean block) {
		MoveSet connect4Positions = new MoveSet();
		int value = MAX_WINS;

		if(node.getNumMovesPlayed() < 5) return connect4Positions;
		MoveType type;
		
		if(player == 1){ 
			value = MIN_WINS;
		}
		
		for(Move move: node.moves){ // tests the positions above, below, right, and left of move.
			int row = move.row;
			int col = move.column;

			if(block){
				if(move.player == player){ // attack
					continue;
				} else { // block
					type = new MoveType(MoveType.Type.BLOCK_CONNECT_4, getMultiplierValue(value, BLOCK_MULT));
				} // end else 
			} else {
				if(move.player != player){ // block
					continue;
				} else { // attack
					type = new MoveType(MoveType.Type.CONNECT_4, value);
				} // end else
			} // end else
				
			if(row - 1 >= 0){ // prevents null pointer exceptions
				if(node.board[row - 1][col] == 0){
					node.board[row - 1][col] = move.player; // test move
					
					if(possibleConnect4(node, move)){
						connect4Positions.addUnique(new Move(row - 1, col, player, type));
					} // end if
					node.board[row - 1][col] = 0; // undo test move
				} // end if
			} // end if
			if(row + 1 < node.board.length){
				if(node.board[row + 1][col] == 0){
					node.board[row + 1][col] = move.player; // test move
					
					if(possibleConnect4(node, move)){
						
						connect4Positions.addUnique(new Move(row + 1, col, player, type));
					} // end if
					node.board[row + 1][col] = 0; // undo test move
				} // end if
			} // end if
			if(col - 1 >= 0) {
				if(node.board[row][col - 1] == 0){
					node.board[row][col - 1] = move.player; // test move
					
					if(possibleConnect4(node, move)){
						connect4Positions.addUnique(new Move(row, col - 1, player, type));
					} // end if
					node.board[row][col - 1] = 0; // undo test move
				} // end if
			} 
			if(col + 1 < node.board.length){
				if(node.board[row][col + 1] == 0){
					node.board[row][col + 1] = move.player; // test move
					
					if(possibleConnect4(node, move)){
						connect4Positions.addUnique(new Move(row, col + 1, player, type));
					} // end else if
					node.board[row][col + 1] = 0; // undo test move
				}// end if
			} // end if
		} // end for 
		
		return connect4Positions;
	} // end method connect4

	private static MoveSet getOpen3PositionsList(BoardNode node, boolean block) { // add preOpen3
		MoveSet open3Positions = new MoveSet();				  
		MoveSet preOpen3Positions = new MoveSet();
		
		int[][] board = node.board;
		int value = OPEN_3_VALUE;
		int player = 2/node.lastMove.player;
		
		MoveType typeOpen3Horiz;
		MoveType typeOpen3Vert;
		MoveType typePreOpen3Horiz;
		MoveType typePreOpen3Vert;
		
		if(node.getNumMovesPlayed() < 3) return open3Positions;
		
		if(player == 1){ 
			value = -1*value;
		}

		for(Move move : node.moves){
			int row = move.row;
			int col = move.column;
			int i = move.player;
			
			if(block){ // block == true
				if(move.player == player){ // attack
					continue;
				} else { // block
					typeOpen3Horiz = new MoveType(MoveType.Type.BLOCK_OPEN_3_HORIZ, getMultiplierValue(value, BLOCK_MULT));
					typeOpen3Vert = new MoveType(MoveType.Type.BLOCK_OPEN_3_VERT, getMultiplierValue(value, BLOCK_MULT));
					typePreOpen3Horiz = new MoveType(MoveType.Type.BLOCK_PRE_OPEN_3_HORIZ, getMultiplierValue(value, PRE_MULT, BLOCK_MULT));
					typePreOpen3Vert = new MoveType(MoveType.Type.BLOCK_PRE_OPEN_3_VERT, getMultiplierValue(value, PRE_MULT, BLOCK_MULT));
				} // end else
			} else {
				if(move.player != player){ // block
					continue;
				} else { // attack
					typeOpen3Horiz = new MoveType(MoveType.Type.OPEN_3_HORIZ, value);
					typeOpen3Vert = new MoveType(MoveType.Type.OPEN_3_VERT, value);
					typePreOpen3Horiz = new MoveType(MoveType.Type.PRE_OPEN_3_HORIZ, getMultiplierValue(value, PRE_MULT));
					typePreOpen3Vert = new MoveType(MoveType.Type.PRE_OPEN_3_VERT, getMultiplierValue(value, PRE_MULT));
				} // end else 
			} // end else
			
			if(col == 0){ // column 0
				if(row == 1){ // row 1 column 0
					
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0){ // down 1
							   if(board[row + 2][col] == i){ //down 2
								
								   open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
							   } else if(board[row + 2][col] == 0){ //down 2
									
								   preOpen3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
								   preOpen3Positions.addUnique( new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
							   }
							} else if(board[row + 1][col] == i){ // down 1
								if(board[row + 2][col] == 0){ // down 2
								
									open3Positions.addUnique( new Move(row + 2, col, player, typeOpen3Vert)); // down 2
								} 
							} // end else if
						} // end if
					} // end if
				} else if(row == 2){ // row 2 column 0
					
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0){ // down 1
							   if(board[row + 2][col] == i){ // down 2
								
								   open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
							   } else if(board[row + 2][col] == 0){ // down 2
									
								   open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
								   open3Positions.addUnique( new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
							   }
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								
								open3Positions.addUnique( new Move(row + 2, col, player, typeOpen3Vert)); // down 2
							} // else if
						} // end if
					} else if(board[row - 1][col] == i){ // up 1
						if((board[row - 2][col] == 0)){ // up 2
							if(board[row + 1][col] == 0){ // down 1
								if((board[row + 2][col] == 0)){ // down 2
									
									open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
								} // end if
							} // end if
						} // end if
					} // end else if
				} else if(row == 3 || row == 4){ // row 3 or 4 column 0
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0){// down 1
							   if(board[row + 2][col] == i){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
							   } else if(board[row + 2][col] == 0){ // down 2
									
								   open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
								   open3Positions.addUnique( new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
							   } // end else if
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								
								open3Positions.addUnique( new Move(row + 2, col, player, typeOpen3Vert)); // down 2
							} // end else if
						} // end if
					} else if(board[row - 1][col] == i){ // up 1
						if((board[row - 2][col] == 0)){ // up 2
							if(board[row + 1][col] == 0){ // down 1
								if((board[row + 2][col] == 0)){ // down 2
									
									open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
								} // end if
							} // end if
						} // end if
					} if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0){ // up 1
							   if(board[row - 2][col] == i){ // up 2
								   
								   open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
							   } else if(board[row + 2][col] == 0){ // up 2
								   
								   open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
								   open3Positions.addUnique( new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
							   } // end else if
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								open3Positions.addUnique( new Move(row - 2, col, player, typeOpen3Vert)); // up 2
							} // else if
						} // end if
					} else if(board[row + 1][col] == i){ // down 1
						if((board[row + 2][col] == 0)){ // down 2
							if(board[row - 1][col] == 0){ // up 1
								if((board[row - 2][col] == 0)){ // up 2
									open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
								} // end if
							} // end if
						} // end if
					} // end else if
				} else if(row == 5){ // row 5 column 0
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0){ // up 1
							   if(board[row - 2][col] == i){ // up 2
								   open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
							   } else if(board[row - 2][col] == 0){ // up 2
									
								   open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
								   open3Positions.addUnique( new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
							   } // end else if
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								open3Positions.addUnique( new Move(row - 2, col, player, typeOpen3Vert)); // up 2
							} // end else if
						} // end if
					} else if(board[row + 1][col] == i){ // down 1
						if((board[row + 2][col] == 0)){ // down 2
							if(board[row - 1][col] == 0){ // up 1
								if((board[row - 2][col] == 0)){ // up 2
									
									open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
								} // end if
							} // end if
						} // end if
					} // end else if
				} else if(row == 6){ // row 6 column 0
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0){ // up 1
							   if(board[row - 2][col] == i){ // up 2
								   
								   open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
							   } else if(board[row - 2][col] == 0){ // up 2
								   
								   open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
								   open3Positions.addUnique( new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
							   } // end else if
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								
								open3Positions.addUnique( new Move(row - 2, col, player, typeOpen3Vert)); // up 2
							} // end else if
						} // end if
					} // end if
				} // end else if
			} else if(col == 1){ // column 1
				if(row == 0 || row == 7){ // row 0 or 7 column 1
					if(board[row][col - 1] == 0){ // left 1
						if(board[row][col + 1] == 0){ // right 1
							if(board[row][col + 3] == 0){ // right 3
								if(board[row][col + 2] == i){ // right 2
									
									open3Positions.addUnique( new Move(row, col + 1, player, typeOpen3Horiz)); // right 1
								} else if(board[row][col + 2] == 0){ // right 2
									
									open3Positions.addUnique( new Move(row, col + 1, player, typePreOpen3Horiz)); // right 1
									open3Positions.addUnique( new Move(row, col + 2, player, typePreOpen3Horiz)); // right 2
								} // end if
							} // end if
						} else if(board[row][col + 1] == i){ // right 1
							if(board[row][col + 2] == 0){ // right 2
								if(board[row][col + 3] == 0){ // right 3
									
									open3Positions.addUnique( new Move(row, col + 2, player, typeOpen3Horiz)); // right 2
								} // end if
							} // end if
						} // end else if
					} // end if
				} else if(row == 1){ // row 1 column 1
					if(board[row][col - 1] == 0){ // left 1
						if(board[row][col + 1] == 0){ // right 1
							if(board[row][col + 3] == 0){ // right 3
								if(board[row][col + 2] == i){ // right 2
									
									open3Positions.addUnique( new Move(row, col + 1, player, typeOpen3Horiz)); // right 1
								} else if(board[row][col + 2] == 0){ // right 2
									
									open3Positions.addUnique( new Move(row, col + 1, player, typePreOpen3Horiz)); // right 1
									open3Positions.addUnique( new Move(row, col + 2, player, typePreOpen3Horiz)); // right 2
								} // end else if
							} // end if
						} else if(board[row][col + 1] == i){ // right 1
							if(board[row][col + 2] == 0){ // right 2
								if(board[row][col + 3] == 0){ // right 3
									
									open3Positions.addUnique( new Move(row, col + 2, player, typeOpen3Horiz)); // right 2
								} // end if
							} // end if
						} // end else if
					} // end if
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 1][col] == i){ // down 1
							if(board[row + 2][col] == 0){ // down 2
								if(board[row + 3][col] == 0){ // down 3
									
									open3Positions.addUnique(new Move(row + 2, col, player, typeOpen3Vert)); // down 2
								} // end if
							} // end if
						} else if(board[row + 1][col] == 0){ // down 1
							if(board[row + 3][col] == 0){ // down 3
								if(board[row + 2][col] == i){ // down 2
									
									open3Positions.addUnique(new Move(row + 1, col, player, typeOpen3Vert)); // down 1
								} else if(board[row + 2][col] == 0){ // down 2
									
									open3Positions.addUnique(new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
									open3Positions.addUnique(new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
								} // end if
							} // end if
						} // end if
					} // end if
				} else if(row == 2){ // row 2 column 1
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0){ // down 1
							   if(board[row + 2][col] == i){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
							   } else if(board[row + 2][col] == 0){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
								   open3Positions.addUnique( new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
							   } // end else if
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								
								open3Positions.addUnique( new Move(row + 2, col, player, typeOpen3Vert)); // down 2
							} // end else if
						} // end if
					} else if(board[row - 1][col] == i && // up 1
							  board[row - 2][col] == 0 ){ // up 2
						if(board[row + 1][col] == 0 && // down 1
						   board[row + 2][col] == 0){ // down 2
							
							open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
						} // end if
					} if(board[row][col - 1] == 0){ // left 1
						if(board[row][col + 3] == 0){ // right 3
							if(board[row][col + 1] == 0){ // right 1
							   if(board[row][col + 2] == i){ // right 2
								   
								   open3Positions.addUnique( new Move(row, col + 1, player, typeOpen3Horiz)); // right 1
							   } else if(board[row][col + 2] == 0){ // right 2
								   
								   open3Positions.addUnique( new Move(row, col + 1, player, typePreOpen3Horiz)); // right 1
								   open3Positions.addUnique( new Move(row, col + 2, player, typePreOpen3Horiz)); // right 2
							   } // end else if
							} else if(board[row][col + 1] == i && // right 1
									  board[row][col + 2] == 0){ // right 2
								
								open3Positions.addUnique( new Move(row, col + 2, player, typeOpen3Horiz)); // right 2
							} // end else if
						} // end if
					} // end if
				} else if(row == 3 || row == 4){ // rows 3 or 4 column 1
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0){ // down 1
							   if(board[row + 2][col] == i){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
							   } else if(board[row + 2][col] == 0){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
								   open3Positions.addUnique( new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
							   } // end else 
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								
								open3Positions.addUnique( new Move(row + 2, col, player, typeOpen3Vert)); // down 2
							} // end else if
						} // end if
					} else if(board[row - 1][col] == i && // up 1
								  board[row - 2][col] == 0 ){ // up 2
						if(board[row + 1][col] == 0 && // down 1
						   board[row + 2][col] == 0){ // down 2
							
							open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
						} // end if
					} if(board[row][col - 1] == 0){ // left 1
						if(board[row][col + 3] == 0){ // right 3
							if(board[row][col + 1] == 0){ // right 1
							   if(board[row][col + 2] == i){ // right 2
								   
								   open3Positions.addUnique( new Move(row, col + 1, player, typeOpen3Horiz)); // right 1
							   } else if(board[row][col + 2] == 0){ // right 2
								   
								   open3Positions.addUnique( new Move(row, col + 1, player, typePreOpen3Horiz)); // right 1
								   open3Positions.addUnique( new Move(row, col + 2, player, typePreOpen3Horiz)); // right 2
							   } // end else if
							} else if(board[row][col + 1] == i && // right 1
									  board[row][col + 2] == 0){ // right 2
								
								open3Positions.addUnique( new Move(row, col + 2, player, typeOpen3Horiz)); // right 2
							} // end else if
						} // end if
					} // end if
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0){ // up 1
							   if(board[row - 2][col] == i){ // up 2
								   
								open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
							   } else if(board[row - 2][col] == 0){ // up 2
								   
								open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
								open3Positions.addUnique( new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
							   } // end else if
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								
								open3Positions.addUnique( new Move(row - 2, col, player, typeOpen3Vert)); // up 2
							} // end else if
						} // end if
					} else if(board[row + 1][col] == i && // down 1
							  board[row + 2][col] == 0 ){ // down 2
						if(board[row - 1][col] == 0 && // up 1
						   board[row - 2][col] == 0){ // up 2
							
							open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
						} // end if
					} // end else if
				} else if(row == 5){ // row 5 column 1
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0){ // up 1
							   if(board[row - 2][col] == i){ // up 2
								   open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
							   } else if(board[row - 2][col] == 0){ // up 2
								   
								   open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
								   open3Positions.addUnique( new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
							   } // end else if
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								
								open3Positions.addUnique( new Move(row - 2, col, player, typeOpen3Vert)); // up 2
							} // end else if
						} // end if
					} else if(board[row + 1][col] == i && // down 1
							  board[row + 2][col] == 0 ){ // down 2
						if(board[row - 1][col] == 0 && // up 1
						   board[row - 2][col] == 0){ // up 2
							
							open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
						} // end if
					} if(board[row][col - 1] == 0){ // left 1
						if(board[row][col + 3] == 0){ // right 3
							if(board[row][col + 1] == 0){ // right 1
							   if(board[row][col + 2] == i){ // right 2
								   
								   open3Positions.addUnique( new Move(row, col + 1, player, typeOpen3Horiz)); // right 1
							   } else if(board[row][col + 2] == 0){ // right 2
								   
								   open3Positions.addUnique( new Move(row, col + 1, player, typePreOpen3Horiz)); // right 1
								   open3Positions.addUnique( new Move(row, col + 2, player, typePreOpen3Horiz)); // right 2
							   } // end else if
							} else if(board[row][col + 1] == i && // right 1
									  board[row][col + 2] == 0){ // right 2
								
								open3Positions.addUnique( new Move(row, col + 2, player, typeOpen3Horiz)); // right 2
							} // end else if
						} // end if
					} // end if
				} if(row == 6){ // row 6 column 1
					if(board[row][col - 1] == 0){ // left 1
						if(board[row][col + 1] == 0){ // right 1
							if(board[row][col + 3] == 0){ // right 3
								if(board[row][col + 2] == i){ // right 2
									
									open3Positions.addUnique( new Move(row, col + 1, player, typeOpen3Horiz)); // right 1
								} else if(board[row][col + 2] == 0){ // right 2
									
									open3Positions.addUnique( new Move(row, col + 1, player, typePreOpen3Horiz)); // right 1
									open3Positions.addUnique( new Move(row, col + 2, player, typePreOpen3Horiz)); // right 2
								} // end else if
							} // end if
						} else if(board[row][col + 1] == i){ // right 1
							if(board[row][col + 2] == 0){ // right 2
								if(board[row][col + 3] == 0){ // right 3
									
									open3Positions.addUnique( new Move(row, col + 2, player, typeOpen3Horiz)); // right 2
								} // end if
							} // end if
						} // end else if
					} // end if
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 1][col] == i){ // up 1
							if(board[row - 2][col] == 0){ // up 2
								if(board[row - 3][col] == 0){ // up 3
									
									open3Positions.addUnique(new Move(row - 2, col, player, typeOpen3Vert)); // up 2
								} // end if
							} // end if
						} else if(board[row - 1][col] == 0){ // up 1
							if(board[row - 3][col] == 0){ // up 3
								if(board[row - 2][col] == i){ // up 2
									
									open3Positions.addUnique(new Move(row - 1, col, player, typeOpen3Vert)); // up 1
								} else if(board[row - 2][col] == i){ // up 2
									
									open3Positions.addUnique(new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
									open3Positions.addUnique(new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
								} // end if
							} // end if
						} // end if
					} // end if
			    } // end if(row == 6)
			} else if(col == 2){ // column 2
				if(board[row][col - 1] == 0){ // left 1
					if(board[row][col + 1] == 0){ // right 1						
						if(board[row][col + 3] == 0){ // right 3
							if(board[row][col + 2] == i){ // right 2
								
								open3Positions.addUnique( new Move(row, col + 1, player, typeOpen3Horiz)); // right 1
							} else if(board[row][col + 2] == 0){ // right 2
								
								open3Positions.addUnique(new Move(row, col + 1, player, typePreOpen3Horiz)); // right 1
								open3Positions.addUnique(new Move(row, col + 2, player, typePreOpen3Horiz)); // right 2
							} // end else if
						} // end if
					} else if(board[row][col + 1] == i){ // right 1
						if(board[row][col + 2] == 0){ // right 2								
							if(board[row][col - 2] == 0){ // left 2
									
									open3Positions.addUnique( new Move(row, col - 1, player, typeOpen3Horiz)); // left 1
								} // end if
							} // end if
						}  // end else if
				} else if(board[row][col - 1] == i){ // left 1
					if(board[row][col - 2] == 0){ // left 2
						if(board[row][col + 1] == 0){ // right 1
							if(board[row][col + 2] == 0){ // right 2
									
								open3Positions.addUnique( new Move(row, col + 1, player, typeOpen3Horiz)); // right 1
							} // end if
						} // end if
					} // end if
				} // end else if
				if(row == 1 || row == 2 || row == 3 | row == 4){ // row 1-4 column 2
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == i){ // down 1
								if(board[row + 2][col] == 0){ // down 2
										
									open3Positions.addUnique( new Move(row + 2, col, player, typeOpen3Vert)); // down 2
								} // end if
							} else if(board[row + 1][col] == 0){ // down 1
								if(board[row + 2][col] == i){ // down 2
										
									open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
								} else if(board[row + 2][col] == 0){ // down 2
									
									open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
									open3Positions.addUnique( new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
								} // end else if
							} // end if
						} // end if
					} // end if
				} // end if
				if(row == 2 | row == 3 | row == 4 | row == 5){ // row 2-5 column 2
					if(board[row - 2][col] == 0){ // up 2
						if(board[row + 2][col] == 0){ // down 2
							if(board[row - 1][col] == i){ // up 1
								if(board[row + 1][col] == 0){ // down 1
										
									open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
								} 
							} else if(board[row - 1][col] == 0){ // up 1
								if(board[row + 1][col] == i){ // down 1
										
									open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
								} else if(board[row + 1][col] == 0){ // down 1
									
									open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
									open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
								} // end if
							} // end if
						} // end if
					} // end if
				} else if(row == 3 | row == 4 | row == 5 | row == 6){ // row 3-6 column 2
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == i){ // up 1
								if(board[row - 2][col] == 0){ // up 2
										
									open3Positions.addUnique( new Move(row - 2, col, player, typeOpen3Vert)); // up 2
								} // end if
							} else if(board[row - 1][col] == 0){ // up 1
								if(board[row - 2][col] == i){ // up 2
											
									open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
								} else if(board[row - 2][col] == 0){ // up 2
									
									open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
									open3Positions.addUnique( new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
								} // end if
							} // end else if
						} // end if
					} // end if
				} // end else if
			} else if(col == 3 || col == 4){ // columns 3 and 4
				if(board[row][col - 1] == 0){ // left 1
					if(board[row][col + 2] == i){ // right 2
						if(board[row][col + 1] == 0){ // right 1
							if(board[row][col + 3] == 0){ // right 3
								
								open3Positions.addUnique( new Move(row, col + 1, player, typeOpen3Horiz)); // right 1
							} else if(board[row][col + 2] == 0){ // right 2
								
								open3Positions.addUnique( new Move(row, col + 1, player, typePreOpen3Horiz)); // right 1
								open3Positions.addUnique( new Move(row, col + 2, player, typePreOpen3Horiz)); // right 2
							} // end if
						} // end if
					} else if(board[row][col + 2] == 0){ // right 2	 
						if(board[row][col + 3] == 0){ // right 3
							if(board[row][col + 1] == 0){ // right 1													
								open3Positions.addUnique( new Move(row, col + 1, player, typePreOpen3Horiz)); // right 1
								open3Positions.addUnique( new Move(row, col + 2, player, typePreOpen3Horiz)); // right 2
							} else if(board[row][col + 1] == i){ // right 1													
								
								open3Positions.addUnique( new Move(row, col + 2, player, typeOpen3Horiz)); // right 2
							} // end if
						}
						if(board[row][col - 2] == 0){ // left 2
							if(board[row][col + 1] == i){ // right 1													
								open3Positions.addUnique( new Move(row, col - 1, player, typeOpen3Horiz)); // left 1
							} else if(board[row][col + 1] == 0){ // right 1													
								
								open3Positions.addUnique( new Move(row, col - 1, player, typePreOpen3Horiz)); // left 1
								open3Positions.addUnique( new Move(row, col + 1, player, typePreOpen3Horiz)); // right 1
							} // end if
						} // end if
					}  // end else if
				} else if(board[row][col - 1] == i){ // left 1
					if(board[row][col - 2] == 0){ // left 2
						if(board[row][col + 1] == 0){ // right 1
							if(board[row][col + 2] == 0){ // right 2
									
								open3Positions.addUnique( new Move(row, col + 1, player, typeOpen3Horiz)); // right 1
							} // end if
						} // end if
					} // end if
				} // end else if
				
				if(board[row][col + 1] == 0){ // right 1
					if(board[row][col - 1] == 0){ // left 1
						if(board[row][col - 3] == 0){ // left 3
							if(board[row][col - 2] == i){ // left 2
								
								open3Positions.addUnique( new Move(row, col - 1, player, typeOpen3Horiz)); // left 1
							} else if(board[row][col - 2] == 0){ // left 2
								
								open3Positions.addUnique( new Move(row, col - 1, player, typePreOpen3Horiz)); // left 1
								open3Positions.addUnique( new Move(row, col - 2, player, typePreOpen3Horiz)); // left 2
							} // end else if
						} // end if
					} else if(board[row][col - 1] == i){ // left 1
						if(board[row][col - 2] == 0){ // left 2								
							if(board[row][col + 2] == 0){ // right 2
									
								open3Positions.addUnique( new Move(row, col + 1, player, typeOpen3Horiz)); // right 1
							} // end if
						} // end if
					}  // end else if
				} else if(board[row][col + 1] == i){ // right 1
					if(board[row][col + 2] == 0){ // right 2
						if(board[row][col - 1] == 0){ // left 1
							if(board[row][col - 2] == 0){ // left 2
									
								open3Positions.addUnique( new Move(row, col - 1, player, typeOpen3Horiz)); // left 1
							} // end if
						} // end if
					} // end if
				} // end else if
				if(row == 1 || row == 2 || row == 3 | row == 4){ // row 1-4 column 3-4
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == i){ // down 1
								if(board[row + 2][col] == 0){ // down 2
										
									open3Positions.addUnique( new Move(row + 2, col, player, typeOpen3Vert)); // down 2
								} // end if
							} else if(board[row + 1][col] == 0){ // down 1
								if(board[row + 2][col] == i){ // down 2
											
									open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
								} else if(board[row + 2][col] == 0){ // down 2
									
									open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
									open3Positions.addUnique( new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
								} // end else if
							} // end if
						} // end if
					} // end if
				} // end if
				if(row == 2 | row == 3 | row == 4 | row == 5){ // row 2-5 column 0
					if(board[row - 1][col] == i){ // up 1
						if(board[row - 2][col] == 0){ // up 2
							if(board[row + 1][col] == 0){ // down 1
								if(board[row + 2][col] == 0){ // down 2
										
									open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
								} // end if
							} // end if
						} // end if
					} else if(board[row - 1][col] == 0){ // up 1
						if(board[row - 2][col] == 0){ // up 2
							if(board[row + 2][col] == 0){ // down 2
								if(board[row + 1][col] == i){ // down 1
										
									open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
								} else if(board[row + 1][col] == i){ // down 1
									
									open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
									open3Positions.addUnique( new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
								} // end else if
							} // end if
						} // end if
					} // end else if
				} // end if
				
				if(row == 3 | row == 4 | row == 5 | row == 6){ // row 3-6 column 0
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == i){ // up 1
								if(board[row - 2][col] == 0){ // up 2
										
									open3Positions.addUnique( new Move(row - 2, col, player, typeOpen3Vert)); // up 2
								} // end if
							} else if(board[row - 1][col] == 0){ // up 1
								if(board[row - 2][col] == i){ // up 2
										
									open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
								} else if(board[row - 2][col] == 0){ // up 2
									
									open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
									open3Positions.addUnique( new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
								} // end else if
							} // end if
						} // end if
					} // end if
				} // end else if
			} else if(col == 5){ // column 5
				
				if(board[row][col + 1] == 0){ // right 1
					if(board[row][col - 1] == 0){ // left 1
						if(board[row][col - 2] == i){ // left 2
							if(board[row][col - 3] == 0){ // left 3
								
								open3Positions.addUnique( new Move(row, col - 1, player, typeOpen3Horiz)); // left 1
							} else if(board[row][col - 2] == 0){ // left 2
								
								open3Positions.addUnique( new Move(row, col - 1, player, typePreOpen3Horiz)); // left 1
								open3Positions.addUnique( new Move(row, col - 2, player, typePreOpen3Horiz)); // left 2
							} // end if
						} else if(board[row][col - 2] == 0){ // left 2
							if(board[row][col + 2] == 0){ // right 2
								
								open3Positions.addUnique( new Move(row, col - 1, player, typePreOpen3Horiz)); // left 1
								open3Positions.addUnique( new Move(row, col + 1, player, typePreOpen3Horiz)); // right 1
							}
						}
					} else if(board[row][col - 1] == i){ // left 1
						if(board[row][col - 2] == 0){ // left 2								
							if(board[row][col + 2] == 0){ // right 2

								open3Positions.addUnique( new Move(row, col + 1, player, typeOpen3Horiz)); // right 1
							} // end if
						} // end if
					}  // end else if
				} else if(board[row][col + 1] == i){ // right 1
					if(board[row][col + 2] == 0){ // right 2
						if(board[row][col - 1] == 0){ // left 1
							if(board[row][col - 2] == 0){ // left 2
									
								open3Positions.addUnique( new Move(row, col - 1, player, typeOpen3Horiz)); // left 1
							} // end if
						} // end if
					} // end if
				} // end else if
				if(row == 1 || row == 2 || row == 3 | row == 4){ // row 1-4 column 0
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 2][col] == 0){ // down 2
							if(board[row + 3][col] == 0){ // down 3
								if(board[row + 1][col] == i){ // down 1
										
									open3Positions.addUnique( new Move(row + 2, col, player, typeOpen3Vert)); // down 2
								} else if(board[row + 1][col] == 0){ // down 1
									
									open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
									open3Positions.addUnique( new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
								} // end else if
							} // end if
						} // end if
					} else if(board[row - 1][col] == 0){ // up 1
						if(board[row + 1][col] == 0){ // down 1
							if(board[row + 3][col] == 0){ // down 3
								if(board[row + 2][col] == i){ // down 2
										
									open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
								} else if(board[row + 2][col] == 0){ // down 2
									
									open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
									open3Positions.addUnique( new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
								} // end if
							} // end if
						} // end if
					} // end if
				} // end if
				if(row == 2 | row == 3 | row == 4 | row == 5){ // row 2-5 column 0
					if(board[row - 2][col] == 0){ // up 2
						if(board[row + 2][col] == 0){ // down 2
							if(board[row - 1][col] == i){ // up 1
								if(board[row + 1][col] == 0){ // down 1
										
									open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
								} // end if
							} else if(board[row - 1][col] == 0){ // up 1
								if(board[row + 1][col] == i){ // down 1
											
									open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
								} else if(board[row + 1][col] == 0){ // down 1
									
									open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
									open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
								} // end if
							} // end else if
						} // end if
					} // end if
				} // end if
				
				if(row == 3 | row == 4 | row == 5 | row == 6){ // row 3-6 column 0
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == i){ // up 1
								if(board[row - 2][col] == 0){ // up 2
											
										open3Positions.addUnique( new Move(row - 2, col, player, typeOpen3Vert)); // up 2
									} // end if
							} else if(board[row - 1][col] == 0){ // up 1
								if(board[row - 2][col] == i){ // up 2
											
									open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
								} else if(board[row - 2][col] == 0){ // up 2
									
									open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
									open3Positions.addUnique( new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
								} // end else if
							} // end if
						} // end if
					} // end if
				} // end else if
			} // end if
			
			if(col == 6){ // column 6
				if(row == 0){ // row 0 column 6
					if(board[row][col + 1] == 0){ // right 1
						if(board[row][col - 3] == 0){ // left 3
							if(board[row][col - 1] == 0){ // left 1
								if(board[row][col - 2] == i){ // left 2
									
									open3Positions.addUnique( new Move(row, col - 1, player, typeOpen3Horiz)); // left 1
								} else if(board[row][col - 2] == 0){ // left 2
									
									open3Positions.addUnique( new Move(row, col - 1, player, typePreOpen3Horiz)); // left 1
									open3Positions.addUnique( new Move(row, col - 2, player, typePreOpen3Horiz)); // left 2
								} // end if
							} else if(board[row][col - 1] == i){ // left 1
								if(board[row][col - 2] == 0){ // left 2
									
									open3Positions.addUnique( new Move(row, col - 2, player, typeOpen3Horiz)); // left 2
								} // end if
							} // end else if
						} // end if
					} // end if
				} else if(row == 1){ // row 1 column 6
					if(board[row][col + 1] == 0){ // right 1
						if(board[row][col - 3] == 0){ // left 3
							if(board[row][col - 1] == 0){ // left 1
								if(board[row][col - 2] == i){ // left 2
									
									open3Positions.addUnique( new Move(row, col - 1, player, typeOpen3Horiz)); // left 1
								} else if(board[row][col - 2] == 0){ // left 2
									
									open3Positions.addUnique( new Move(row, col - 1, player, typePreOpen3Horiz)); // left 1
									open3Positions.addUnique( new Move(row, col - 2, player, typePreOpen3Horiz)); // left 2
								} // end if
							} else if(board[row][col - 1] == i){ // left 1
								if(board[row][col - 2] == 0){ // left 2
									
									open3Positions.addUnique( new Move(row, col - 2, player, typeOpen3Horiz)); // left 2
								} // end if
							} // end else if
						} // end if
					} // end if
					
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == i){ // down 1
								if(board[row + 2][col] == 0){ // down 2
									
									open3Positions.addUnique(new Move(row + 2, col, player, typeOpen3Vert)); // down 2
								} // end if
							} else if(board[row + 1][col] == 0){ // down 1
								if(board[row + 2][col] == i){ // down 2
									
										open3Positions.addUnique(new Move(row + 1, col, player, typeOpen3Vert)); // down 1
								} else if(board[row + 2][col] == 0){ // down 2
									
									open3Positions.addUnique(new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
									open3Positions.addUnique(new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
								} // end else if
							} // end else if
						} // end if
					} // end if
				} else if(row == 2){ // row 2 column 6
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0){ // down 1
							   if(board[row + 2][col] == i){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
							   } else if(board[row + 2][col] == 0){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
								   open3Positions.addUnique( new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
							   } // end else if
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								
								open3Positions.addUnique( new Move(row + 2, col, player, typeOpen3Vert)); // down 2
							} // end else if
						} // end if
					} else if(board[row - 1][col] == i && // up 1
							  board[row - 2][col] == 0 ){ // up 2
						if(board[row + 1][col] == 0 && // down 1
						   board[row + 2][col] == 0){ // down 2
							
							open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
						} // end if
					} // end else if
					
					if(board[row][col + 1] == 0){ // right 1
						if(board[row][col - 3] == 0){ // left 3
							if(board[row][col - 1] == 0){ // left 1
							   if(board[row][col - 2] == i){ // left 2
								
								open3Positions.addUnique( new Move(row, col - 1, player, typeOpen3Horiz)); // left 1
							   } else if(board[row][col - 2] == 0){ // left 2
									
								   open3Positions.addUnique( new Move(row, col - 1, player, typePreOpen3Horiz)); // left 1
								   open3Positions.addUnique( new Move(row, col - 2, player, typePreOpen3Horiz)); // left 2
							   } // end else if
							} else if(board[row][col - 1] == i && // left 1
									  board[row][col - 2] == 0){ // left 2
								
								open3Positions.addUnique( new Move(row, col - 2, player, typeOpen3Horiz)); // left 2
							} // end else if
						} // end if
					} // end if
				} else if(row == 3 || row == 4){ // rows 3 or 4 column 6
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0){ // down 1
							   if(board[row + 2][col] == i){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
							   } else if(board[row + 2][col] == 0){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
								   open3Positions.addUnique( new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
							   } // end else if
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								open3Positions.addUnique( new Move(row + 2, col, player, typeOpen3Vert)); // down 2
							} // end else if
						} // end if
					} else if(board[row - 1][col] == i && // up 1
							  board[row - 2][col] == 0 ){ // up 2
						if(board[row + 1][col] == 0 && // down 1
						   board[row + 2][col] == 0){ // down 2
							
							open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
						} // end if
					} if(board[row][col + 1] == 0){ // right 1
						if(board[row][col - 3] == 0){ // left 3
							if(board[row][col - 1] == 0){ // left 1
							   if(board[row][col - 2] == i){ // left 2
								
								open3Positions.addUnique( new Move(row, col - 1, player, typeOpen3Horiz)); // left 1
							   } else if(board[row][col - 2] == 0){ // left 2
									
								   open3Positions.addUnique( new Move(row, col - 1, player, typePreOpen3Horiz)); // left 1
								   open3Positions.addUnique( new Move(row, col - 2, player, typePreOpen3Horiz)); // left 2
							   } // end else if
							} else if(board[row][col - 1] == i && // left 1
									  board[row][col - 2] == 0){ // left 2
								
								open3Positions.addUnique( new Move(row, col - 2, player, typeOpen3Horiz)); // left 2
							} // end else if
						} // end if
					} // end if
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0){ // up 1
							   if(board[row - 2][col] == i){ // up 2
								   
								   open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
							   } else if(board[row - 2][col] == 0){ // up 2
								   
								   open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
								   open3Positions.addUnique( new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
							   } // end else if
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								open3Positions.addUnique( new Move(row - 2, col, player, typeOpen3Vert)); // up 2
							} // end else if
						} // end if
					} else if(board[row + 1][col] == i && // down 1
							  board[row + 2][col] == 0 ){ // down 2
						if(board[row - 1][col] == 0 && // up 1
						   board[row - 2][col] == 0){ // up 2
							
							open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
						} // end if
					} // end else if
				} else if(row == 5){ // row 5 column 6
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0){ // up 1
							   if(board[row - 2][col] == i){ // up 2
								
								   open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
							   } else if(board[row - 2][col] == 0){ // up 2
								
								   open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
								   open3Positions.addUnique( new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
							   } // end if
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								
								open3Positions.addUnique( new Move(row - 2, col, player, typeOpen3Vert)); // up 2
							} // end else if
						} // end if
					} else if(board[row + 1][col] == i && // down 1
							  board[row + 2][col] == 0 ){ // down 2
						if(board[row - 1][col] == 0 && // up 1
						   board[row - 2][col] == 0){ // up 2
							
							open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
						} // end if
					} // end else if
					
					if(board[row][col + 1] == 0){ // right 1
						if(board[row][col - 3] == 0){ // left 3
							if(board[row][col - 1] == 0){ // left 1
							   if(board[row][col - 2] == i){ // left 2
								   
								   open3Positions.addUnique( new Move(row, col - 1, player, typeOpen3Horiz)); // left 1
							   } else if(board[row][col - 2] == 0){ // left 2
								   
								   open3Positions.addUnique( new Move(row, col - 1, player, typePreOpen3Horiz)); // left 1
								   open3Positions.addUnique( new Move(row, col - 2, player, typePreOpen3Horiz)); // left 2
							   } // end else if
							} else if(board[row][col - 1] == i && // left 1
									  board[row][col - 2] == 0){ // left 2
								
								open3Positions.addUnique( new Move(row, col - 2, player, typeOpen3Horiz)); // left 2
							} // end else if
						} // end if
					} // end if
				} if(row == 6){ // row 6 column 6
					if(board[row][col + 1] == 0){ // right 1
						if(board[row][col - 3] == 0){ // left 3
							if(board[row][col - 1] == 0){ // left 1
								if(board[row][col - 2] == i){ // left 2
									
									open3Positions.addUnique( new Move(row, col - 1, player, typeOpen3Horiz)); // left 1
								} else if(board[row][col - 2] == 0){ // left 2
									
									open3Positions.addUnique( new Move(row, col - 1, player, typePreOpen3Horiz)); // left 1
									open3Positions.addUnique( new Move(row, col - 2, player, typePreOpen3Horiz)); // left 2
								} // end else if
							} else if(board[row][col - 1] == i){ // left 1
								if(board[row][col - 2] == 0){ // left 2
									
									open3Positions.addUnique( new Move(row, col - 2, player, typeOpen3Horiz)); // left 2
								} // end if
							} // end else if
						} // end if
					} // end if
	
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == i){ // up 1
								if(board[row - 2][col] == 0){ // up 2
									
									open3Positions.addUnique(new Move(row - 2, col, player, typeOpen3Vert)); // up 2
								} // end if
							} else if(board[row - 1][col] == 0){ // up 1
								if(board[row - 2][col] == i){ // up 2
									
									open3Positions.addUnique(new Move(row - 1, col, player, typeOpen3Vert)); // up 1
								} else if(board[row - 2][col] == 0){ // up 2
									
									open3Positions.addUnique(new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
									open3Positions.addUnique(new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
								} // end else if
							} // end else if
						} // end if
					} // end if
			    } else if(row == 7){ // row 7 column 6
					if(board[row][col + 1] == 0){ // right 1
						if(board[row][col - 3] == 0){ // left 3
							if(board[row][col - 1] == 0){ // left 1
								if(board[row][col - 2] == i){ // left 2
									
									open3Positions.addUnique( new Move(row, col - 1, player, typeOpen3Horiz)); // left 1
								} else if(board[row][col - 2] == 0){ // left 2
									
									open3Positions.addUnique( new Move(row, col - 1, player, typePreOpen3Horiz)); // left 1
									open3Positions.addUnique( new Move(row, col - 2, player, typePreOpen3Horiz)); // left 2
								} // end if
							} else if(board[row][col - 1] == i){ // left 1
								if(board[row][col - 2] == 0){ // left 2
									
									open3Positions.addUnique( new Move(row, col - 2, player, typeOpen3Horiz)); // left 2
								} // end if
							} // end else if
						} // end if
					} // end if
			   } // end else if(row == 7).
			} else if(col == 7){ // column 7
				if(row == 1){ // row 1 column 7
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0){ // down 1
							   if(board[row + 2][col] == i){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
							   } else if(board[row + 2][col] == 0){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
								   open3Positions.addUnique( new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
							   } // end else if
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								
								open3Positions.addUnique( new Move(row + 2, col, player, typeOpen3Vert)); // down 2
							} // end else if
						} // end if
					} // end if
				} else if(row == 2){ // row 2 column 7
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0){ // down 1
							   if(board[row + 2][col] == i){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
							   } else if(board[row + 2][col] == 0){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
								   open3Positions.addUnique( new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
							   } // end else if
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								
								open3Positions.addUnique( new Move(row + 2, col, player, typeOpen3Vert)); // down 2
							} // end else if
						} // end if
					} else if(board[row - 1][col] == i){ // up 1
						if((board[row - 2][col] == 0)){ // up 2
							if(board[row + 1][col] == 0){ // down 1
								if((board[row + 2][col] == 0)){ // down 2
									
									open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
								} // end if
							} // end if
						} // end if
					} // end else if
				} else if(row == 3 || row == 4){ // row 3 or 4 column 7
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0){ // down 1
							   if(board[row + 2][col] == i){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
							   } else if(board[row + 2][col] == 0){ // down 2
								   
								   open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
								   open3Positions.addUnique( new Move(row + 2, col, player, typePreOpen3Vert)); // down 2
							   }
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								
								open3Positions.addUnique( new Move(row + 2, col, player, typeOpen3Vert)); // down 2
							} // end else if
						} // end if
					} else if(board[row - 1][col] == i){ // up 1
						if((board[row - 2][col] == 0)){ // up 2
							if(board[row + 1][col] == 0){ // down 1
								if((board[row + 2][col] == 0)){ // down 2
									
									open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
								} // end if
							} // end if
						} // end if
					} // end if
					
					if(board[row - 3][col] == 0){ // up 3
						if(board[row + 1][col] == 0){ // down 1
							if(board[row - 1][col] == 0){ // up 1
							   if(board[row - 2][col] == i){ // up 2
								   
								   open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
							   } else if(board[row - 2][col] == 0){ // up 2
								   
								   open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
								   open3Positions.addUnique( new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
							   } // end else if
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								
								open3Positions.addUnique( new Move(row - 2, col, player, typeOpen3Vert)); // up 2
							}
						}  else if(board[row + 1][col] == i){ // down 1
							if((board[row + 2][col] == 0)){ // down 2
								if(board[row - 1][col] == 0){ // up 1
									if((board[row - 2][col] == 0)){ // up 2
										
										open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
									} // end if
								} // end if
							} // end if
						} // end if
					} // end else if
				} else if(row == 5){ // row 5 column 7
					if(board[row - 3][col] == 0){ // up 3
						if(board[row + 1][col] == 0){ // down 1
							if(board[row - 1][col] == 0){ // up 1
							   if(board[row - 2][col] == i){ // up 2
								   
								   open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
							   } else if(board[row - 2][col] == 0){ // up 2
								   
								   open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
								   open3Positions.addUnique( new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
							   } // end else if
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								
								open3Positions.addUnique( new Move(row - 2, col, player, typeOpen3Vert)); // up 2
							} // end else if
						} else if(board[row + 1][col] == i){ // down 1
							if((board[row + 2][col] == 0)){ // down 2
								if(board[row - 1][col] == 0){ // up 1
									if((board[row - 2][col] == 0)){ // up 2
										
										open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
									} // end if
								} // end if
							} // end if
						} // end else if
					} // end if
				} else if(row == 6){ // row 6 column 7
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0){ // up 1
							   if(board[row - 2][col] == i){ // up 2
								   
								   open3Positions.addUnique( new Move(row - 1, col, player, typeOpen3Vert)); // up 1
							   } else if(board[row - 2][col] == 0){ // up 2
								   
								   open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
								   open3Positions.addUnique( new Move(row - 2, col, player, typePreOpen3Vert)); // up 2
							   } // end else if
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								
								open3Positions.addUnique( new Move(row - 2, col, player, typeOpen3Vert)); // up 2
							} // end else if
						} // end if
					} // end if
				} // end else if
			} // end if(col == 7) // column 7
		} // end if
//		open3Positions.print("open3Positions");
		return open3Positions;
	} // end method 
	
	private static int getMultiplierValue(int value, double mult1, double mult2) {
		
		return (int) (value - (value*mult1 + value*mult2));
	}

	private static int getMultiplierValue(int value, double mult1) {

		return (int) (value - (value*mult1));
	}

	private static MoveSet getSmallOpenLPositionsList(BoardNode node, boolean block){ 
		MoveSet smallOpenLPositions = new MoveSet();
		int value = OPEN_L_VALUE;
		int player = 2/node.lastMove.player;
		MoveType type;
		
		if(player == 1){ 
			value = -1*value;
		}
		
		for(Move move : node.moves){
			int row = move.row;
			int col = move.column;
			
			if(block){
				if(move.player == player){ // attack
					continue;
				} else { // block
					type = new MoveType(MoveType.Type.BLOCK_SMALL_OPEN_L, getMultiplierValue(value, BLOCK_MULT));
				} // end else 
			} else {
				if(move.player != player){ // block
					continue;
				} else { // attack
					type = new MoveType(MoveType.Type.SMALL_OPEN_L, value);
				} // end else
			} // end else
			
			if(row > 0 && row < 7 && col > 0 && row < 7){ // move not on perimeter
				if(col == 1){
					smallOpenLPositions.addAll(smallOpenLColumn1(node, move, type));
				} else if(col == 2 || col == 3 || col == 4){
					smallOpenLPositions.addAll(smallOpenLColumns_2_3_4(node, move, type));
				} if(col == 3 || col == 4 || col == 5){
					smallOpenLPositions.addAll(smallOpenLColumns_3_4_5(node, move, type));
				} else if(col == 6){
					smallOpenLPositions.addAll(smallOpenLColumn6(node, move, type));
				} 
			} // end if
		} // end for
//		smallOpenLPositions.print("smallOpenLPositions");
		return smallOpenLPositions;
	} // end method smallOpenLPositions
	
	public static MoveSet smallOpenLColumn1(BoardNode node, Move move, MoveType type){
		MoveSet smallOpenLPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(row == 1){ // row 1 column 1
			if(board[row + 1][col + 1] == i){ // diagonal down right
				if(board[row][col + 1] == 0){ // right 1
					
					if((board[row][col + 2]     == 0 || // right 2
					    board[row][col + 2]     == i)&& // right 2
					   (board[row][col + 3]     == 0 || // right 3
					    board[row][col + 3]     == i)&& // right 3
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row - 1][col + 1] == 0 || // up 1 right 1
					    board[row - 1][col + 1] == i)&& // up 1 right 1
					   (board[row + 2][col + 1] == 0 || // down 2 right 1
					    board[row + 2][col + 1] == i)&& // down 2 right 1
					   (board[row + 3][col + 1] == 0 || // down 3 right 1
						board[row + 3][col + 1] == i)){ // down 3 right 1
						
						 smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if
				} // end if right 1
				if(board[row + 1][col]  == 0){  // down 1
					if(board[row + 1][col + 2] == 0 && // down 1 right 2
					   board[row + 1][col + 2] == i && // down 1 right 2
					  (board[row + 1][col + 3] == 0 || // down 1 right 3
					   board[row + 1][col + 3] == i)&& // down 1 right 3
					  (board[row + 1][col - 1] == 0 || // down 1 left 1
					   board[row + 1][col - 1] == i)&& // down 1 left 1
					  (board[row - 1][col] 	   == 0 || // up 1
					   board[row - 1][col]     == i)&& // up 1
					  (board[row + 2][col]     == 0 || // down 2
					   board[row + 2][col]     == i)&& // down 2
					  (board[row + 3][col]     == 0 || // down 3
						 board[row + 3][col]   == i)){ // down 3
						
						smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					} // end if
				} // end else if
			} if(board[row][col + 1] == i && // right 1
					  board[row + 1][col] == 0){  // down 1
				if((board[row][col + 2]   == 0 || // right 2
				    board[row][col + 2]   == i)&& // right 2
			 	   (board[row][col + 3]   == 0 || // right 3
				    board[row][col + 3]   == i)&& // right 3
				   (board[row][col - 1]   == 0 || // left 1
				    board[row - 1][col]   == i)&& // up 1
				   (board[row + 1][col]   == 0 || // down 1
				    board[row + 2][col]   == i)&& // down 2
				   (board[row + 3][col]   == 0 || // down 3
				    board[row + 3][col]   == i)){ // down 3
						
					smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} // end if						
			} else if(board[row][col + 1]  == 0 && // right 1
					  board[row + 1][col]  == i){  // down 1
				
				if((board[row][col + 2] == 0 ||    // right 2
					board[row][col + 2] == i)&&    // right 2
				   (board[row][col + 3] == 0 ||    // right 3
					board[row][col + 3] == i)&&    // right 3
				   (board[row][col - 1] == 0 ||    // left 1
					board[row][col - 1] == i)&&   // left 1
				   (board[row - 1][col] == 0 ||    // up 1
					board[row - 1][col] == i)&&   // up 1
				   (board[row + 2][col] == 0 ||    // down 2
					board[row + 2][col] == i)&&   // down 2
				   (board[row + 3][col] == 0 ||    // down 3
					board[row + 3][col] == i)){    // down 3
					
					smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end if						
			} // end else if
		} if(row == 6){ // row 6 column 1
			if(board[row - 1][col + 1] == i){          // diagonal up right
				if(board[row][col + 1] == 0){ 		   // right 1
					
					if((board[row][col + 2]     == 0 || // right 2
					    board[row][col + 2]     == i)&& // right 2
					   (board[row][col + 3]     == 0 || // right 3
					    board[row][col + 3]     == i)&& // right 3
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row + 1][col + 1] == 0 || // down 1 right 1
					    board[row + 1][col + 1] == i)&& // down 1 right 1
					   (board[row - 2][col + 1] == 0 || // up 2 right 1
					    board[row - 2][col + 1] == i)&& // up 2 right 1
					   (board[row - 3][col + 1] == 0 || // up 3 right 1
						board[row - 3][col + 1] == i)){ // up 3 right 1
						
						 smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if
				} // end if right 1
				if(board[row - 1][col]  == 0){  // up 1
					
					if(board[row - 1][col + 2] == 0 && // up 1 right 2
					   board[row - 1][col + 2] == i && // up 1 right 2
					  (board[row - 1][col + 3] == 0 || // up 1 right 3
					   board[row - 1][col + 3] == i)&& // up 1 right 3
					  (board[row - 1][col - 1] == 0 || // up 1 left 1
					   board[row - 1][col - 1] == i)&& // up 1 left 1
					  (board[row + 1][col] 	   == 0 || // down 1
					   board[row + 1][col]     == i)&& // down 1
					  (board[row - 2][col]     == 0 || // up 2
					   board[row - 2][col]     == i)&& // up 2
					  (board[row - 3][col]     == 0 || // up 3
					   board[row - 3][col]   == i)){ // up 3
						
						smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					} // end if
				} // end else if
			} if(board[row][col + 1] == i && // right 1
					  board[row - 1][col] == 0){  // up 1
				
				if((board[row][col + 2]   == 0 || // right 2
				    board[row][col + 2]   == i)&& // right 2
			 	   (board[row][col + 3]   == 0 || // right 3
				    board[row][col + 3]   == i)&& // right 3
				   (board[row][col - 1]   == 0 || // left 1
				    board[row + 1][col]   == i)&& // down 1
				   (board[row - 1][col]   == 0 || // up 1
				    board[row - 2][col]   == i)&& // up 2
				   (board[row - 3][col]   == 0 || // up 3
				    board[row - 3][col]   == i)){ // up 3
						
					smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} // end if						
			} else if(board[row][col + 1]  == 0 && // right 1
					  board[row - 1][col]  == i){  // up 1
				
				if((board[row][col + 2] == 0 ||    // right 2
					board[row][col + 2] == i)&&    // right 2
				   (board[row][col + 3] == 0 ||    // right 3
					board[row][col + 3] == i)&&    // right 3
				   (board[row][col - 1] == 0 ||    // left 1
					board[row][col - 1] == i)&&    // left 1
				   (board[row + 1][col] == 0 ||    // down 1
					board[row + 1][col] == i)&&    // down 1
				   (board[row - 2][col] == 0 ||    // up 2
					board[row - 2][col] == i)&&    // up 2
				   (board[row - 3][col] == 0 ||    // up 3
					board[row - 3][col] == i)){    // up 3
					
					smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end if						
			} // end else if
		} else if(row == 2){ // row 2 column 1
				if(board[row + 1][col + 1]     == 0 && // down 1 right 1
				   board[row][col + 1] 	   	   == i){  // right 1

					if((board[row][col + 2]     == 0 || // right 2
					    board[row][col + 2]     == i)&& // right 2
					   (board[row][col + 3]     == 0 || // right 3
					    board[row][col + 3]     == i)&& // right 3
					   (board[row][col - 1]     == 0 || // left 1
					    board[row][col - 1]     == i)&& // left 1
					   (board[row - 1][col + 1] == 0 || // up 1 right 1  
					    board[row - 1][col + 1] == i)&& // up 1 right 1  
					   (board[row + 2][col + 1] == 0 || // down 2 right 1
					    board[row + 2][col + 1] == i)&& // down 2 right 1
					   (board[row + 3][col + 1] == 0 || // down 3 right 1
					    board[row + 3][col + 1] == i || // down 3 right 1
					    board[row - 2][col + 1] == 0 || // up 2 right 1	
					    board[row - 2][col + 1] == i)){ // up 2 right 1	

						smallOpenLPositions.addUnique(new Move(row + 1, col + 1, player, type)); // down 1 right 1
					} // end if	
				} else if(board[row][col + 1]      == 0 && // right 1
						  board[row + 1][col + 1]  == i){  // down 1 right 1
					
						if((board[row][col + 2]     == 0 || // right 2
						    board[row][col + 2]     == i)&& // right 2
						   (board[row][col + 3]     == 0 || // right 3
						    board[row][col + 3]     == i)&& // right 3
						   (board[row][col - 1]     == 0 || // left 1
						    board[row][col - 1]     == i)&& // left 1
						   (board[row - 1][col + 1] == 0 || // up 1 right 1  
						    board[row - 1][col + 1] == i)&& // up 1 right 1  
						   (board[row + 2][col + 1] == 0 || // down 2 right 1
						    board[row + 2][col + 1] == i)&& // down 2 right 1
						   (board[row + 3][col + 1] == 0 || // down 3 right 1
						    board[row + 3][col + 1] == i || // down 3 right 1
						    board[row - 2][col + 1] == 0 || // up 2 right 1
						    board[row - 2][col + 1] == i)){ // up 2 right 1					   
						  
							smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
						} // end if	
				} if(board[row + 1][col] 	   	   == i && // down 1
					  board[row + 1][col + 1]      == 0){  // down 1 right 1
						
						if((board[row + 1][col + 2] == 0 || // down 1 right 2
						    board[row + 1][col + 2] == i)&& // down 1 right 2
						   (board[row + 1][col + 3] == 0 || // down 1 right 3
						    board[row + 1][col + 3] == i)&& // down 1 right 3
						   (board[row + 1][col - 1] == 0 || // down 1 left 1
						    board[row + 1][col - 1] == i)&& // down 1 left 1
						   (board[row - 1][col]     == 0 || // up 1 
						    board[row - 1][col]     == i)&& // up 1 
						   (board[row + 2][col]     == 0 || // down 2
						    board[row + 2][col]     == i)&& // down 2
						   (board[row + 3][col]     == 0 || // down 3
						    board[row + 3][col]     == i || // down 3
						    board[row - 2][col]     == 0 || // up 2
						    board[row - 2][col]     == i)){ // up 2

							smallOpenLPositions.addUnique(new Move(row + 1, col + 1, player, type)); // down 1 right 1
						} // end if	
				} else if(board[row + 1][col]      	   == 0 &&    // down 1
						  board[row + 1][col + 1]  == i){ // down 1 right 1
						
						if((board[row + 1][col + 2]     == 0 || // down 1 right 2
						    board[row + 1][col + 2]     == i)&& // down 1 right 2
						   (board[row + 1][col + 3]     == 0 || // down 1 right 3
						    board[row + 1][col + 3]     == i)&& // down 1 right 3
						   (board[row + 1][col - 1]     == 0 || // down 1 left 1
						    board[row + 1][col - 1]     == i)&& // down 1 left 1
						   (board[row - 1][col] 		== 0 || // up 1 
						    board[row - 1][col] 		== i)&& // up 1 
						   (board[row + 2][col] 		== 0 || // down 2
						    board[row + 2][col] 		== i)&& // down 2
						   (board[row + 3][col] 		== 0 || // down 3
						    board[row + 3][col] 		== i || // down 3
						    board[row - 2][col] 		== 0 || // up 2
						    board[row - 2][col] 		== i)){ // up 2
							
							smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
						} // end if		
				} if(board[row][col + 1] 	 == i && // right 1
				     board[row - 1][col + 1] == 0){  // up 1 right 1
					
					if((board[row][col + 2]     == 0 || // right 2
					    board[row][col + 2]     == i)&& // right 2
					   (board[row][col + 3]     == 0 || // right 3
					    board[row][col + 3]     == i)&& // right 3
					   (board[row][col - 1]     == 0 || // left 1
					    board[row][col - 1]     == i)&& // left 1
					   (board[row - 2][col + 1] == 0 || // up 2 right 1
					    board[row - 2][col + 1] == i)&& // up 2 right 1
					   (board[row + 1][col + 1] == 0 || // down 1 right 1
					    board[row + 1][col + 1] == i)&& // down 1 right 1
					   (board[row + 2][col + 1] == 0 || // down 2 right 1
					    board[row + 2][col + 1] == i)){ // down 2 right 1

						smallOpenLPositions.addUnique(new Move(row - 1, col + 1, player, type)); // down 1 right 1
					} // end if	
				} else if(board[row][col + 1] == 0 && // right 1
					board[row - 1][col + 1]    == i){ // up 1 right 1
					
						if((board[row][col + 2]     == 0 || // right 2
						    board[row][col + 2]     == i)&& // right 2
						   (board[row][col + 3]     == 0 || // right 3
						    board[row][col + 3]     == i)&& // right 3
						   (board[row][col - 1]     == 0 || // left 1
						    board[row][col - 1]     == i)&& // left 1
						   (board[row - 2][col + 1] == 0 || // up 2 right 1
						    board[row - 2][col + 1] == i)&& // up 2 right 1
						   (board[row + 1][col + 1] == 0 || // down 1 right 1
						    board[row + 1][col + 1] == i)&& // down 1 right 1
						   (board[row + 2][col + 1] == 0 || // down 2 right 1
						    board[row + 2][col + 1] == i)){ // down 2 right 1

						smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if	
				} // end else if
		} else if(row == 5) { // row 5 column 1
			if(board[row][col + 1] == i &&    // right 1
			   board[row - 1][col + 1] == 0){ // up 1 right 1
				
				if((board[row][col + 2]     == 0 ||   // right 2
				    board[row][col + 2]     == i)&&   // right 2
				   (board[row][col + 3]     == 0 ||   // right 3
				    board[row][col + 3]     == i)&&   // right 3
				   (board[row][col - 1]     == 0 ||   // left 1
				    board[row][col - 1]     == i)&&   // left 1
				   (board[row - 2][col + 1] == 0 ||   // up 2 right 1
				    board[row - 2][col + 1] == i)&&   // up 2 right 1
				   (board[row + 1][col + 1] == 0 ||   // down 1 right 1
				    board[row + 1][col + 1] == i)&&   // down 1 right 1
				   (board[row + 2][col + 1] == 0 ||   // down 2 right 1
				    board[row + 2][col + 1] == i ||   // down 2 right 1
				    board[row - 3][col + 1] == 0 ||    // up 3 right 1
				    board[row - 3][col + 1] == i)){    // up 3 right 1

					smallOpenLPositions.addUnique(new Move(row - 1, col + 1, player, type)); // up 1 right 1
				} // end if	
			} else if(board[row][col + 1] == 0 &&    // right 1
					  board[row - 1][col + 1] == i){ // up 1 right 1
				
					if((board[row][col + 2]     == 0 ||   // right 2
					    board[row][col + 2]     == i)&&   // right 2
					   (board[row][col + 3]     == 0 ||   // right 3
					    board[row][col + 3]     == i)&&   // right 3
					   (board[row][col - 1]     == 0 ||   // left 1
					    board[row][col - 1]     == i)&&   // left 1
					   (board[row - 2][col + 1] == 0 ||   // up 2 right 1
					    board[row - 2][col + 1] == i)&&   // up 2 right 1
					   (board[row + 1][col + 1] == 0 ||   // down 1 right 1
					    board[row + 1][col + 1] == i)&&   // down 1 right 1
					   (board[row + 2][col + 1] == 0 ||   // down 2 right 1
					    board[row + 2][col + 1] == i ||   // down 2 right 1
					    board[row - 3][col + 1] == 0 ||    // up 3 right 1
					    board[row - 3][col + 1] == i)){    // up 3 right 1

						smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end if	
			} // end else if
		} else if(row < 5 && row > 2){ // rows 3 - 4 in column 1
			if(board[row][col + 1] == i &&    // right 1
			   board[row + 1][col + 1] == 0){ // down 1 right 1
				
				if((board[row][col + 2] == 0 ||       // right 2
				    board[row][col + 2] == i)&&       // right 2
				   (board[row][col + 3] == 0 ||       // right 3
				    board[row][col + 3] == i)&&       // right 3
				   (board[row][col - 1] == 0 ||       // left 1
				    board[row][col - 1] == i)&&       // left 1
				   (board[row - 1][col + 1] == 0 ||   // up 1 right 1
				    board[row - 1][col + 1] == i)&&   // up 1 right 1
				   (board[row + 2][col + 1] == 0 ||   // down 2 right 1
				    board[row + 2][col + 1] == i)&&   // down 2 right 1
				   (board[row + 3][col + 1] == 0 ||   // down 3 right 1
				    board[row + 3][col + 1] == i ||   // down 3 right 1
				    board[row - 2][col + 1] == 0 ||   // up 2 right 1
				    board[row - 2][col + 1] == i)){   // up 2 right 1
	   
					smallOpenLPositions.addUnique(new Move(row + 1, col + 1, player, type)); // down 1 right 1
				} // end if	
			} else if(board[row][col + 1] == 0 &&     // right 1
					  board[row + 1][col + 1] == i){  // down 1 right 1
				
				if((board[row][col + 2] == 0 ||       // right 2
				    board[row][col + 2] == i)&&       // right 2
				   (board[row][col + 3] == 0 ||       // right 3
				    board[row][col + 3] == i)&&       // right 3
				   (board[row][col - 1] == 0 ||       // left 1
				    board[row][col - 1] == i)&&       // left 1
				   (board[row - 1][col + 1] == 0 ||   // up 1 right 1
				    board[row - 1][col + 1] == i)&&   // up 1 right 1
				   (board[row + 2][col + 1] == 0 ||   // down 2 right 1
				    board[row + 2][col + 1] == i)&&   // down 2 right 1
				   (board[row + 3][col + 1] == 0 ||   // down 3 right 1
				    board[row + 3][col + 1] == i ||   // down 3 right 1
				    board[row - 2][col + 1] == 0 ||   // up 2 right 1
				    board[row - 2][col + 1] == i)){   // up 2 right 1
	   
					smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end if	
			} else if(board[row][col + 1] == i &&    // right 1
					  board[row - 1][col + 1] == 0){ // up 1 right 1
				
				if((board[row][col + 2]     == 0 || // right 2
				    board[row][col + 2]     == i)&& // right 2
				   (board[row][col + 3]     == 0 || // right 3
				    board[row][col + 3]     == i)&& // right 3
				   (board[row][col - 1]     == 0 || // left 1
				    board[row][col - 1]     == i)&& // left 1
				   (board[row - 2][col + 1] == 0 || // up 2 right 1
				    board[row - 2][col + 1] == i)&& // up 2 right 1
				   (board[row + 1][col + 1] == 0 || // down 1 right 1
				    board[row + 1][col + 1] == i)&& // down 1 right 1
				   (board[row + 2][col + 1] == 0 || // down 2 right 1
				    board[row + 2][col + 1] == i || // down 2 right 1
				    board[row - 3][col + 1] == 0 ||  // up 3 right 1
				    board[row - 3][col + 1] == i)){  // up 3 right 1

					smallOpenLPositions.addUnique(new Move(row - 1, col + 1, player, type)); // up 1 right 1
				} // end if	
			} else if(board[row][col + 1] == 0 &&    // right 1
					  board[row - 1][col + 1] == i){ // up 1 right 1
				
				if((board[row][col + 2]     == 0 || // right 2
				    board[row][col + 2]     == i)&& // right 2
				   (board[row][col + 3]     == 0 || // right 3
				    board[row][col + 3]     == i)&& // right 3
				   (board[row][col - 1]     == 0 || // left 1
				    board[row][col - 1]     == i)&& // left 1
				   (board[row - 2][col + 1] == 0 || // up 2 right 1
				    board[row - 2][col + 1] == i)&& // up 2 right 1
				   (board[row + 1][col + 1] == 0 || // down 1 right 1
				    board[row + 1][col + 1] == i)&& // down 1 right 1
				   (board[row + 2][col + 1] == 0 || // down 2 right 1
				    board[row + 2][col + 1] == i || // down 2 right 1
				    board[row - 3][col + 1] == 0 ||  // up 3 right 1
				    board[row - 3][col + 1] == i)){  // up 3 right 1

					smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end if	
			} // end else if
		} // end else if
		
		return smallOpenLPositions;
	} // checked smallOpenLPositions
	
	public static MoveSet smallOpenLColumn6(BoardNode node, Move move, MoveType type){
		MoveSet smallOpenLPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		 // column 6
		if(row == 1){ // row 1 column 6
			if(board[row + 1][col - 1] == i){ // diagonal down left
				if(board[row][col - 1] == 0){ // left 1
					
					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row - 1][col - 1] == 0 || // up 1 left 1
						board[row - 1][col - 1] == i)&& // up 1 left 1
					   (board[row + 2][col - 1] == 0 || // down 2 left 1
						board[row + 2][col - 1] == i)&& // down 2 left 1
					   (board[row + 3][col - 1] == 0 || // down 3 left 1
						board[row + 3][col - 1] == i)){ // down 3 left 1
						
						 smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if
				} // end if left 1
				if(board[row + 1][col]  == 0){  // down 1
					if(board[row + 1][col - 2] == 0 && // down 1 left 2
					   board[row + 1][col - 2] == i && // down 1 left 2
					  (board[row + 1][col - 3] == 0 || // down 1 left 3
					   board[row + 1][col - 3] == i)&& // down 1 left 3
					  (board[row + 1][col + 1] == 0 || // down 1 right 1
					   board[row + 1][col + 1] == i)&& // down 1 right 1
					  (board[row - 1][col] 	   == 0 || // up 1
					   board[row - 1][col]     == i)&& // up 1
					  (board[row + 2][col]     == 0 || // down 2
					   board[row + 2][col]     == i)&& // down 2
					  (board[row + 3][col]     == 0 || // down 3
					   board[row + 3][col]     == i)){ // down 3
						
						smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					} // end if
				} // end else if
			} if(board[row][col - 1] == i && // left 1
				 board[row + 1][col] == 0){  // down 1
				
				if((board[row][col - 2]   == 0 || // left 2
					board[row][col - 2]   == i)&& // left 2
				   (board[row][col - 3]   == 0 || // left 3
					board[row][col - 3]   == i)&& // left 3
				   (board[row][col + 1]   == 0 || // right 1
					board[row - 1][col]   == i)&& // up 1
				   (board[row + 1][col]   == 0 || // down 1
					board[row + 2][col]   == i)&& // down 2
				   (board[row + 3][col]   == 0 || // down 3
					board[row + 3][col]   == i)){ // down 3
						
					smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} // end if						
			} else if(board[row][col - 1]  == 0 && // left 1
					  board[row + 1][col]  == i){  // down 1
				
				if((board[row][col - 2] == 0 ||    // left 2
					board[row][col - 2] == i)&&    // left 2
				   (board[row][col - 3] == 0 ||    // left 3
					board[row][col - 3] == i)&&    // left 3
				   (board[row][col + 1] == 0 ||    // right 1
					board[row][col + 1] == i)&&   // right 1
				   (board[row - 1][col] == 0 ||    // up 1
					board[row - 1][col] == i)&&   // up 1
				   (board[row + 2][col] == 0 ||    // down 2
					board[row + 2][col] == i)&&   // down 2
				   (board[row + 3][col] == 0 ||    // down 3
					board[row + 3][col] == i)){    // down 3
					
					smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end if						
			} // end else if
		} if(row == 6){ // row 6 column 6
			if(board[row - 1][col - 1] == i){          // diagonal up left
				if(board[row][col - 1] == 0){ 		   // left 1
					
					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row + 1][col - 1] == 0 || // down 1 left 1
						board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row - 2][col - 1] == 0 || // up 2 left 1
						board[row - 2][col - 1] == i)&& // up 2 left 1
					   (board[row - 3][col - 1] == 0 || // up 3 left 1
						board[row - 3][col - 1] == i)){ // up 3 left 1
						
						 smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if
				} // end if left 1
				if(board[row - 1][col]  == 0){  // up 1
					
					if(board[row - 1][col - 2] == 0 && // up 1 left 2
					   board[row - 1][col - 2] == i && // up 1 left 2
					  (board[row - 1][col - 3] == 0 || // up 1 left 3
					   board[row - 1][col - 3] == i)&& // up 1 left 3
					  (board[row - 1][col + 1] == 0 || // up 1 right 1
					   board[row - 1][col + 1] == i)&& // up 1 right 1
					  (board[row + 1][col] 	   == 0 || // down 1
					   board[row + 1][col]     == i)&& // down 1
					  (board[row - 2][col]     == 0 || // up 2
					   board[row - 2][col]     == i)&& // up 2
					  (board[row - 3][col]     == 0 || // up 3
					   board[row - 3][col]   == i)){ // up 3
						
						smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					} // end if
				} // end else if
			} if(board[row][col - 1] == i && // left 1
					  board[row - 1][col] == 0){  // up 1
				
				if((board[row][col - 2]   == 0 || // left 2
					board[row][col - 2]   == i)&& // left 2
				   (board[row][col - 3]   == 0 || // left 3
					board[row][col - 3]   == i)&& // left 3
				   (board[row][col + 1]   == 0 || // right 1
					board[row + 1][col]   == i)&& // down 1
				   (board[row - 1][col]   == 0 || // up 1
					board[row - 2][col]   == i)&& // up 2
				   (board[row - 3][col]   == 0 || // up 3
					board[row - 3][col]   == i)){ // up 3
						
					smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} // end if						
			} else if(board[row][col - 1]  == 0 && // left 1
					  board[row - 1][col]  == i){  // up 1
				
				if((board[row][col - 2] == 0 ||    // left 2
					board[row][col - 2] == i)&&    // left 2
				   (board[row][col - 3] == 0 ||    // left 3
					board[row][col - 3] == i)&&    // left 3
				   (board[row][col + 1] == 0 ||    // right 1
					board[row][col + 1] == i)&&    // right 1
				   (board[row + 1][col] == 0 ||    // down 1
					board[row + 1][col] == i)&&    // down 1
				   (board[row - 2][col] == 0 ||    // up 2
					board[row - 2][col] == i)&&    // up 2
				   (board[row - 3][col] == 0 ||    // up 3
					board[row - 3][col] == i)){    // up 3
					
					smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end if						
			} // end else if
		} else if(row == 2){ // row 2 column 6
				if(board[row + 1][col - 1]     == 0 && // down 1 left 1
				   board[row][col - 1] 	   	   == i){  // left 1

					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row - 1][col - 1] == 0 || // up 1 left 1  
						board[row - 1][col - 1] == i)&& // up 1 left 1  
					   (board[row + 2][col - 1] == 0 || // down 2 left 1
						board[row + 2][col - 1] == i)&& // down 2 left 1
					   (board[row + 3][col - 1] == 0 || // down 3 left 1
						board[row + 3][col - 1] == i || // down 3 left 1
						board[row - 2][col - 1] == 0 || // up 2 left 1	
						board[row - 2][col - 1] == i)){ // up 2 left 1	

						smallOpenLPositions.addUnique(new Move(row + 1, col - 1, player, type)); // down 1 left 1
					} // end if	
				} else if(board[row][col - 1]      == 0 && // left 1
						  board[row + 1][col - 1]  == i){  // down 1 left 1
					
						if((board[row][col - 2]     == 0 || // left 2
							board[row][col - 2]     == i)&& // left 2
						   (board[row][col - 3]     == 0 || // left 3
							board[row][col - 3]     == i)&& // left 3
						   (board[row][col + 1]     == 0 || // right 1
							board[row][col + 1]     == i)&& // right 1
						   (board[row - 1][col - 1] == 0 || // up 1 left 1  
							board[row - 1][col - 1] == i)&& // up 1 left 1  
						   (board[row + 2][col - 1] == 0 || // down 2 left 1
							board[row + 2][col - 1] == i)&& // down 2 left 1
						   (board[row + 3][col - 1] == 0 || // down 3 left 1
							board[row + 3][col - 1] == i || // down 3 left 1
							board[row - 2][col - 1] == 0 || // up 2 left 1
							board[row - 2][col - 1] == i)){ // up 2 left 1					   
						  
							smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
						} // end if	
				} if(board[row + 1][col] 	   	   == i && // down 1
					  board[row + 1][col - 1]      == 0){  // down 1 left 1
						
						if((board[row + 1][col - 2] == 0 || // down 1 left 2
							board[row + 1][col - 2] == i)&& // down 1 left 2
						   (board[row + 1][col - 3] == 0 || // down 1 left 3
							board[row + 1][col - 3] == i)&& // down 1 left 3
						   (board[row + 1][col + 1] == 0 || // down 1 right 1
							board[row + 1][col + 1] == i)&& // down 1 right 1
						   (board[row - 1][col]     == 0 || // up 1 
							board[row - 1][col]     == i)&& // up 1 
						   (board[row + 2][col]     == 0 || // down 2
							board[row + 2][col]     == i)&& // down 2
						   (board[row + 3][col]     == 0 || // down 3
							board[row + 3][col]     == i || // down 3
							board[row - 2][col]     == 0 || // up 2
							board[row - 2][col]     == i)){ // up 2
							
							smallOpenLPositions.addUnique(new Move(row + 1, col - 1, player, type)); // down 1 left 1
						} // end if	
				} else if(board[row + 1][col]      	   == 0 &&  // down 1
						  board[row + 1][col - 1]  == i){ 		// down 1 left 1
						
						if((board[row + 1][col - 2]     == 0 || // down 1 left 2
							board[row + 1][col - 2]     == i)&& // down 1 left 2
						   (board[row + 1][col - 3]     == 0 || // down 1 left 3
							board[row + 1][col - 3]     == i)&& // down 1 left 3
						   (board[row + 1][col + 1]     == 0 || // down 1 right 1
							board[row + 1][col + 1]     == i)&& // down 1 right 1
						   (board[row - 1][col] 		== 0 || // up 1 
							board[row - 1][col] 		== i)&& // up 1 
						   (board[row + 2][col] 		== 0 || // down 2
							board[row + 2][col] 		== i)&& // down 2
						   (board[row + 3][col] 		== 0 || // down 3
							board[row + 3][col] 		== i || // down 3
							board[row - 2][col] 		== 0 || // up 2
							board[row - 2][col] 		== i)){ // up 2
							
							smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
						} // end if		
				} if(board[row][col - 1] 	 == i && // left 1
					 board[row - 1][col - 1] == 0){  // up 1 left 1
					
					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row - 2][col - 1] == 0 || // up 2 left 1
						board[row - 2][col - 1] == i)&& // up 2 left 1
					   (board[row + 1][col - 1] == 0 || // down 1 left 1
						board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row + 2][col - 1] == 0 || // down 2 left 1
						board[row + 2][col - 1] == i)){ // down 2 left 1

						smallOpenLPositions.addUnique(new Move(row - 1, col - 1, player, type)); // up 1 left 1
					} // end if	
				} else if(board[row][col - 1] == 0 && // left 1
					board[row - 1][col - 1]    == i){ // up 1 left 1
					
						if((board[row][col - 2]     == 0 || // left 2
							board[row][col - 2]     == i)&& // left 2
						   (board[row][col - 3]     == 0 || // left 3
							board[row][col - 3]     == i)&& // left 3
						   (board[row][col + 1]     == 0 || // right 1
							board[row][col + 1]     == i)&& // right 1
						   (board[row - 2][col - 1] == 0 || // up 2 left 1
							board[row - 2][col - 1] == i)&& // up 2 left 1
						   (board[row + 1][col - 1] == 0 || // down 1 left 1
							board[row + 1][col - 1] == i)&& // down 1 left 1
						   (board[row + 2][col - 1] == 0 || // down 2 left 1
							board[row + 2][col - 1] == i)){ // down 2 left 1

						smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if	
				} // end else if
		} else if(row == 5) { // row 5 column 6
			if(board[row][col - 1] == i &&    // left 1
			   board[row - 1][col - 1] == 0){ // up 1 left 1
				
				if((board[row][col - 2]     == 0 ||   // left 2
					board[row][col - 2]     == i)&&   // left 2
				   (board[row][col - 3]     == 0 ||   // left 3
					board[row][col - 3]     == i)&&   // left 3
				   (board[row][col + 1]     == 0 ||   // right 1
					board[row][col + 1]     == i)&&   // right 1
				   (board[row - 2][col - 1] == 0 ||   // up 2 left 1
					board[row - 2][col - 1] == i)&&   // up 2 left 1
				   (board[row + 1][col - 1] == 0 ||   // down 1 left 1
					board[row + 1][col - 1] == i)&&   // down 1 left 1
				   (board[row + 2][col - 1] == 0 ||   // down 2 left 1
					board[row + 2][col - 1] == i ||   // down 2 left 1
					board[row - 3][col - 1] == 0 ||    // up 3 left 1
					board[row - 3][col - 1] == i)){    // up 3 left 1

					smallOpenLPositions.addUnique(new Move(row - 1, col - 1, player, type)); // up 1 left 1
				} // end if	
			} else if(board[row][col - 1] == 0 &&    // left 1
					  board[row - 1][col - 1] == i){ // up 1 left 1
				
					if((board[row][col - 2]     == 0 ||   // left 2
						board[row][col - 2]     == i)&&   // left 2
					   (board[row][col - 3]     == 0 ||   // left 3
						board[row][col - 3]     == i)&&   // left 3
					   (board[row][col + 1]     == 0 ||   // right 1
						board[row][col + 1]     == i)&&   // right 1
					   (board[row - 2][col - 1] == 0 ||   // up 2 left 1
						board[row - 2][col - 1] == i)&&   // up 2 left 1
					   (board[row + 1][col - 1] == 0 ||   // down 1 left 1
						board[row + 1][col - 1] == i)&&   // down 1 left 1
					   (board[row + 2][col - 1] == 0 ||   // down 2 left 1
						board[row + 2][col - 1] == i ||   // down 2 left 1
						board[row - 3][col - 1] == 0 ||    // up 3 left 1
						board[row - 3][col - 1] == i)){    // up 3 left 1

						smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end if	
			} if(board[row][col - 1]     == i &&    // left 1
				 board[row + 1][col - 1] == 0){ 	// down 1 left 1
				    if((board[row][col - 2]     == 0 ||   // left 2
				    	board[row][col - 2]     == i)&&   // left 2
					   (board[row][col - 3]     == 0 ||   // left 3
						board[row][col - 3]     == i)&&   // left 3
					   (board[row][col + 1]     == 0 ||   // right 1
						board[row][col + 1]     == i)&&   // right 1
					   (board[row - 2][col - 1] == 0 ||   // up 2 left 1
						board[row - 2][col - 1] == i)&&   // up 2 left 1
					   (board[row + 1][col - 1] == 0 ||   // down 1 left 1
						board[row + 1][col - 1] == i)&&   // down 1 left 1
					   (board[row + 2][col - 1] == 0 ||   // down 2 left 1
						board[row + 2][col - 1] == i ||   // down 2 left 1
						board[row - 3][col - 1] == 0 ||    // up 3 left 1
						board[row - 3][col - 1] == i)){    // up 3 left 1

						smallOpenLPositions.addUnique(new Move(row + 1, col - 1, player, type)); // down 1 left 1
					} // end if	
				} else if(board[row][col - 1] == 0 &&    // left 1
						  board[row + 1][col - 1] == i){ // up 1 left 1
					
						if((board[row][col - 2]     == 0 ||   // left 2
							board[row][col - 2]     == i)&&   // left 2
						   (board[row][col - 3]     == 0 ||   // left 3
							board[row][col - 3]     == i)&&   // left 3
						   (board[row][col + 1]     == 0 ||   // right 1
							board[row][col + 1]     == i)&&   // right 1
						   (board[row - 2][col - 1] == 0 ||   // up 2 left 1
							board[row - 2][col - 1] == i)&&   // up 2 left 1
						   (board[row + 1][col - 1] == 0 ||   // down 1 left 1
							board[row + 1][col - 1] == i)&&   // down 1 left 1
						   (board[row + 2][col - 1] == 0 ||   // down 2 left 1
							board[row + 2][col - 1] == i ||   // down 2 left 1
							board[row - 3][col - 1] == 0 ||    // up 3 left 1
							board[row - 3][col - 1] == i)){    // up 3 left 1

							smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if	
				} // end else if
		} else if(row < 5 && row > 2){ // rows 3 - 4 in column 1
			if(board[row][col - 1] == i &&    // left 1
			   board[row + 1][col - 1] == 0){ // down 1 left 1
				
				if((board[row][col - 2] == 0 ||       // left 2
					board[row][col - 2] == i)&&       // left 2
				   (board[row][col - 3] == 0 ||       // left 3
					board[row][col - 3] == i)&&       // left 3
				   (board[row][col + 1] == 0 ||       // right 1
					board[row][col + 1] == i)&&       // right 1
				   (board[row - 1][col - 1] == 0 ||   // up 1 left 1
					board[row - 1][col - 1] == i)&&   // up 1 left 1
				   (board[row + 2][col - 1] == 0 ||   // down 2 left 1
					board[row + 2][col - 1] == i)&&   // down 2 left 1
				   (board[row + 3][col - 1] == 0 ||   // down 3 left 1
					board[row + 3][col - 1] == i ||   // down 3 left 1
					board[row - 2][col - 1] == 0 ||   // up 2 left 1
					board[row - 2][col - 1] == i)){   // up 2 left 1
	   
					smallOpenLPositions.addUnique(new Move(row + 1, col - 1, player, type)); // down 1 left 1
				} // end if	
			} else if(board[row][col - 1] == 0 &&     // left 1
					  board[row + 1][col - 1] == i){  // down 1 left 1
				
				if((board[row][col - 2] == 0 ||       // left 2
					board[row][col - 2] == i)&&       // left 2
				   (board[row][col - 3] == 0 ||       // left 3
					board[row][col - 3] == i)&&       // left 3
				   (board[row][col + 1] == 0 ||       // right 1
					board[row][col + 1] == i)&&       // right 1
				   (board[row - 1][col - 1] == 0 ||   // up 1 left 1
					board[row - 1][col - 1] == i)&&   // up 1 left 1
				   (board[row + 2][col - 1] == 0 ||   // down 2 left 1
					board[row + 2][col - 1] == i)&&   // down 2 left 1
				   (board[row + 3][col - 1] == 0 ||   // down 3 left 1
					board[row + 3][col - 1] == i ||   // down 3 left 1
					board[row - 2][col - 1] == 0 ||   // up 2 left 1
					board[row - 2][col - 1] == i)){   // up 2 left 1
	   
					smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end if	
			} else if(board[row][col - 1] == i &&    // left 1
					  board[row - 1][col - 1] == 0){ // up 1 left 1
				
				if((board[row][col - 2]     == 0 || // left 2
					board[row][col - 2]     == i)&& // left 2
				   (board[row][col - 3]     == 0 || // left 3
					board[row][col - 3]     == i)&& // left 3
				   (board[row][col + 1]     == 0 || // right 1
					board[row][col + 1]     == i)&& // right 1
				   (board[row - 2][col - 1] == 0 || // up 2 left 1
					board[row - 2][col - 1] == i)&& // up 2 left 1
				   (board[row + 1][col - 1] == 0 || // down 1 left 1
					board[row + 1][col - 1] == i)&& // down 1 left 1
				   (board[row + 2][col - 1] == 0 || // down 2 left 1
					board[row + 2][col - 1] == i || // down 2 left 1
					board[row - 3][col - 1] == 0 ||  // up 3 left 1
					board[row - 3][col - 1] == i)){  // up 3 left 1

					smallOpenLPositions.addUnique(new Move(row - 1, col - 1, player, type)); // up 1 left 1
				} // end if	
			} else if(board[row][col - 1] == 0 &&    // left 1
					  board[row - 1][col - 1] == i){ // up 1 left 1
				
				if((board[row][col - 2]     == 0 || // left 2
					board[row][col - 2]     == i)&& // left 2
				   (board[row][col - 3]     == 0 || // left 3
					board[row][col - 3]     == i)&& // left 3
				   (board[row][col + 1]     == 0 || // right 1
					board[row][col + 1]     == i)&& // right 1
				   (board[row - 2][col - 1] == 0 || // up 2 left 1
					board[row - 2][col - 1] == i)&& // up 2 left 1
				   (board[row + 1][col - 1] == 0 || // down 1 left 1
					board[row + 1][col - 1] == i)&& // down 1 left 1
				   (board[row + 2][col - 1] == 0 || // down 2 left 1
					board[row + 2][col - 1] == i || // down 2 left 1
					board[row - 3][col - 1] == 0 ||  // up 3 left 1
					board[row - 3][col - 1] == i)){  // up 3 left 1

					smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end if	
			} // end else if
		} // end else if
		
		return smallOpenLPositions;
	} // end method column2
	
	public static MoveSet smallOpenLColumns_2_3_4(BoardNode node, Move move, MoveType type){
		MoveSet smallOpenLPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(row == 1){ // row 1, column 2
			if(board[row + 1][col - 1] == i){ // diagonal down left								
				if(board[row][col - 1] == 0){ // left 1
					if((board[row][col - 2]     == 0 || // left 2
					    board[row][col - 2]     == i)&& // left 2
					   (board[row][col + 1]     == 0 || // right 1
					    board[row][col + 1]     == i)&& // right 1
					   (board[row][col + 2]     == 0 || // right 2
					    board[row][col + 2]     == i)&&  // right 2
					   (board[row - 1][col - 1] == 0 || // up 1 left 1
					    board[row - 1][col - 1] == i)&& // up 1 left 1
					   (board[row + 2][col - 1] == 0 || // down 2 left 1
					    board[row + 2][col - 1] == i)&& // down 2 left 1
					   (board[row + 3][col - 1] == 0 || // down 3 left 1 
					    board[row + 3][col - 1] == i)){ // down 3 left 1 
					  
						smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if
				} // end if
				if(board[row + 1][col]  == 0){ // down 1

					if((board[row + 1][col + 1] == 0 || // down 1 right 1
					    board[row + 1][col + 1] == i)&& // down 1 right 1
					   (board[row + 1][col + 2] == 0 || // down 1 right 2
					    board[row + 1][col + 2] == i)&& // down 1 right 2
					   (board[row + 1][col - 2] == 0 || // down 1 left 2
					    board[row + 1][col - 2] == i)&& // down 1 left 2
					   (board[row - 1][col]     == 0 || // up 1
					    board[row - 1][col]     == i)&& // up 1
					   (board[row + 2][col]     == 0 || // down 2
					    board[row + 2][col]     == i)&& // down 2
					   (board[row + 3][col]     == 0 ||  // down 3
					    board[row + 3][col]     == i)){  // down 3


						smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					} // end if
				} // end else if
			} // end if
			if((board[row][col - 2]	== 0 ||	// left 2
			    board[row][col - 2]	== i)&&	// left 2
			   (board[row][col + 1]	== 0 || // right 1
			    board[row][col + 1]	== i)&& // right 1
			   (board[row][col + 2]	== 0 || // right 2
			    board[row][col + 2]	== i)&& // right 2
			   (board[row - 1][col] == 0 || // up 1 
			    board[row - 1][col] == i)&& // up 1
			   (board[row + 2][col] == 0 || // down 2 
			    board[row + 2][col] == i)&& // down 2
			   (board[row + 3][col] == 0 || // down 3
			    board[row + 3][col] == i)){ // down 3 

				if(board[row][col - 1]  == i && // left 1
				   board[row + 1][col]  == 0){  // down 1
				  
					smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row + 1][col]  == i){  // down 1
					 
					smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end else if
			} // end if						
			if(board[row + 1][col + 1] == i){ // diagonal down right
				if(board[row][col + 1] == 0){  // right 1
					
					if((board[row][col + 2]     == 0 || // right 2
					    board[row][col + 2]     == i)&& // right 2
					   (board[row][col - 1]     == 0 || // left 1
					    board[row][col - 1]     == i)&& // left 1
					   (board[row - 1][col + 1] == 0 || // up 1  right 1
					    board[row - 1][col + 1] == i)&& // up 1  right 1
					   (board[row + 2][col + 1] == 0 || // down 2 right 1
					    board[row + 2][col + 1] == i)&& // down 2 right 1
					   (board[row + 3][col + 1] == 0 || // down 3 right 1 
					    board[row + 3][col + 1] == i)&& // down 3 right 1 
					   (board[row][col + 3]     == 0 || // right 3
					    board[row][col + 3]     == i || // right 3
					    board[row][col - 2]     == 0 || // left 2
					    board[row][col - 2]     == i)){ // left 2

					  
						smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if
				} // end if
				if(board[row + 1][col]  == 0){ // down 1
					
					if((board[row + 1][col - 1] == 0 || // down 1 left 1
					    board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row + 1][col + 2] == 0 || // down 1 right 2
					    board[row + 1][col + 2] == i)&& // down 1 right 2
					   (board[row - 1][col]     == 0 || // up 1 
					    board[row - 1][col]     == i)&& // up 1 
					   (board[row + 2][col]     == 0 || // down 2
					    board[row + 2][col]     == i)&& // down 2
					   (board[row + 3][col]     == 0 || // down 3
					    board[row + 3][col]     == i)&& // down 3
					   (board[row + 1][col - 2] == 0 || // down 1 left 2
					    board[row + 1][col - 2] == i || // down 1 left 2
					    board[row + 1][col + 3] == 0 || // down 1 right 3
					    board[row + 1][col + 3] == i)){ // down 1 right 3

						
						smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					} // end if
				} // end else if
			} // end if
				
			if((board[row][col - 1] == 0 || // left 1
			    board[row][col - 1] == i)&& // left 1
			   (board[row][col + 2] == 0 || // right 2
			    board[row][col + 2] == i)&& // right 2
			   (board[row - 1][col] == 0 || // up 1
			    board[row - 1][col] == i)&& // up 1
			   (board[row + 2][col] == 0 || // down 2
			    board[row + 2][col] == i)&& // down 2
			   (board[row + 3][col] == 0 || // down 3
			    board[row + 3][col] == i)&& // down 3
			   (board[row][col - 2] == 0 || // left 2
			    board[row][col - 2] == i || // left 2
			    board[row][col + 3] == 0 || // right 3
				board[row][col + 3] == i)){ // right 3

				if(board[row][col + 1]  == i && // right 1
				   board[row + 1][col]  == 0){ // down 1
				  
				   smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col + 1]  == 0 && // right 1
					      board[row + 1][col]  == i){ // down 1
					smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end else if
			} // end if				  
		} if(row == 6){ // row 6 column 2
			if(board[row - 1][col - 1] == i){ // diagonal up left								
				
				if(board[row][col - 1] == 0){ // left 1
					
					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row + 1][col - 1] == 0 || // down 1 left 1
						board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row - 2][col - 1] == 0 || // up 2 left 1
						board[row - 2][col - 1] == i)&& // up 2 left 1
					   (board[row - 3][col - 1] == 0 || // up 3 left 1 
						board[row - 3][col - 1] == i)){ // up 3 left 1 
					  
						smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if
				} // end if
				if(board[row - 1][col]  == 0){ // up 1

					if((board[row - 1][col + 1] == 0 || // up 1 right 1
						board[row - 1][col + 1] == i)&& // up 1 right 1
					   (board[row - 1][col + 2] == 0 || // up 1 right 2
						board[row - 1][col + 2] == i)&& // up 1 right 2
					   (board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i)&& // up 1 left 2
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i)){ // up 3


						smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					} // end if
				} // end else if
			} // end if
			if((board[row][col - 2] == 0 ||	// left 2
				board[row][col - 2] == i)&&	// left 2
			   (board[row][col + 1] == 0 || // right 1
				board[row][col + 1] == i)&& // right 1
			   (board[row][col + 2] == 0 || // right 2
				board[row][col + 2] == i)&& // right 2
			   (board[row + 1][col] == 0 || // down 1
				board[row + 1][col] == i)&& // down 1
			   (board[row - 2][col] == 0 || // up 2 
				board[row - 2][col] == i)&& // up 2 
			   (board[row - 3][col] == 0 || // up 3 
				board[row - 3][col] == i)){ // up 3 

				if(board[row][col - 1]  == i && // left 1
				   board[row - 1][col]  == 0){  // up 1
				  
					smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row - 1][col]  == i){  // up 1
					 
					smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end else if
			} // end if						
			if(board[row - 1][col + 1] == i){ // diagonal up right
				if(board[row][col + 1] == 0){ // right 1
					
					if((board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row + 1][col + 1] == 0 || // down 1 right 1
						board[row + 1][col + 1] == i)&& // down 1 right 1
					   (board[row - 2][col + 1] == 0 || // up 2 right 1
						board[row - 2][col + 1] == i)&& // up 2 right 1
					   (board[row - 3][col + 1] == 0 || // up 3 right 1 
						board[row - 3][col + 1] == i)&& // up 3 right 1 
					   (board[row][col + 3]     == 0 || // right 3
						board[row][col + 3]     == i || // right 3
						board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)){ // left 2

					  
						smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if
				} // end if
				if(board[row - 1][col]  == 0){ // up 1
					
					if((board[row - 1][col - 1] == 0 || // up 1 left 1
						board[row - 1][col - 1] == i)&& // up 1 left 1
					   (board[row - 1][col + 2] == 0 || // up 1 right 2
						board[row - 1][col + 2] == i)&& // up 1 right 2
					   (board[row + 1][col]     == 0 || // down 1 
						board[row + 1][col]     == i)&& // down 1 
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i)&& // up 3
					   (board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i || // up 1 left 2
						board[row - 1][col + 3] == 0 || // up 1 right 3
						board[row - 1][col + 3] == i)){ // up 1 right 3
						
						smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					} // end if
				} // end else if
			} // end if
				
				if((board[row][col - 1] == 0 || // left 1
					board[row][col - 1] == i)&& // left 1
				   (board[row][col + 2] == 0 || // right 2
					board[row][col + 2] == i)&& // right 2
				   (board[row + 1][col] == 0 || // down 1
					board[row + 1][col] == i)&& // down 1
				   (board[row - 2][col] == 0 || // up 2
					board[row - 2][col] == i)&& // up 2
				   (board[row - 3][col] == 0 || // up 3
					board[row - 3][col] == i)&& // up 3
				   (board[row][col - 2] == 0 || // left 2
					board[row][col - 2] == i || // left 2
					board[row][col + 3] == 0 || // right 3
					board[row][col + 3] == i)){ // right 3

				if(board[row][col + 1]  == i && // right 1
						board[row - 1][col]  == 0){ // up 1
				  
				   smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col + 1]  == 0 && // right 1
						  board[row - 1][col]  == i){ // up 1
					smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end else if
			} // end if				  
		} else if(row == 2 || row == 3 || row == 4){ // row 2-4, column 2
			if(board[row - 1][col - 1] == i){ // diagonal up left
				
				if(board[row - 1][col] == 0){ // up 1
					if((board[row - 1][col - 2] == 0 || // up 1 left 2
					    board[row - 1][col - 2] == i)&& // up 1 left 2
					   (board[row - 1][col + 1] == 0 || // up 1 right 1
					    board[row - 1][col + 1] == i)&& // up 1 right 1
					   (board[row - 2][col]     == 0 || // up 2
					    board[row - 2][col]     == i)&& // up 2
					   (board[row + 1][col]     == 0 || // down 1
					    board[row + 1][col]     == i)&& // down 1
					   (board[row + 2][col]     == 0 || // down 2
					    board[row + 2][col]     == i)&& // down 2
					   (board[row - 1][col + 2] == 0 || // up 1 right 2
					    board[row - 1][col + 2] == i)){ // up 1 right 2

						
						  smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					} // end if
				} // end if
				if(board[row][col - 1]   == 0){  // left 1
				  
					if((board[row][col - 2]     == 0 || // left 2
					    board[row][col - 2]     == i)&& // left 2
					   (board[row][col + 1]     == 0 || // right 1
					    board[row][col + 1]     == i)&& // right 1
					   (board[row][col + 2]	    == 0 || // right 2
					   board[row][col + 2]	    == i)&& // right 2
					   (board[row - 2][col - 1] == 0 || // up 2 left 1 
					    board[row - 2][col - 1] == i)&& // up 2 left 1 
					   (board[row + 1][col - 1] == 0 || // down 1 left 1 
					    board[row + 1][col - 1] == i)&& // down 1 left 1 
					   (board[row + 2][col - 1] == 0 || // down 2 left 1
					    board[row + 2][col - 1] == i)){ // down 2 left 1

						
						  smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if
				} // end else if 
			} // end if
				
			if((board[row][col - 2] == 0 || // left 2
			    board[row][col - 2] == i)&& // left 2
			   (board[row][col + 1] == 0 || // right 1
			    board[row][col + 1] == i)&& // right 1
			   (board[row][col + 2] == 0 || // right 2
			    board[row][col + 2] == i)&& // right 2
			   (board[row - 2][col] == 0 || // up 2
			    board[row - 2][col] == i)&& // up 2
			   (board[row + 1][col] == 0 || // down 1
			    board[row + 1][col] == i)&& // down 1
			   (board[row + 2][col] == 0 || // down 2
			    board[row + 2][col] == i)){ // down 2

				if(board[row][col - 1] == i && // left 1
						board[row - 1][col] == 0){ // up 1
						  
					  smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col - 1]  == 0 && // left 1
					board[row - 1][col] == i){ // up 1
				
				smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end else if
			} // end if						
			if(board[row - 1][col + 1] == i){ // diagonal up right
				if(board[row - 1][col] == 0){ // up 1
					
					if((board[row - 1][col + 2] == 0 || // up 1 right 2
					    board[row - 1][col + 2] == i)&& // up 1 right 2
					   (board[row - 1][col - 1] == 0 || // up 1 left 1
					    board[row - 1][col - 1] == i)&& // up 1 left 1
					   (board[row - 2][col]     == 0 || // up 2
					    board[row - 2][col]     == i)&& // up 2
					   (board[row + 1][col]     == 0 || // down 1
					    board[row + 1][col]     == i)&& // down 1
					   (board[row + 2][col]     == 0 || // down 2
					    board[row + 2][col]     == i)&& // down 2
					   (board[row - 1][col + 3] == 0 || // up 1 right 3
					    board[row - 1][col + 3] == i || // up 1 right 3
					    board[row - 1][col - 2] == 0 || // up 1 left 2
					    board[row - 1][col - 2] == i)){ // up 1 left 2
						
						  smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					}
				} else if(board[row][col + 1]  == 0){ // right 1
				  
					if((board[row][col + 2]     == 0 || // right 2
					    board[row][col + 2]     == i)&& // right 2
					   (board[row][col - 1]     == 0 || // left 1
					    board[row][col - 1]     == i)&& // left 1
					   (board[row - 2][col + 1] == 0 || // up 2 right 1 
					    board[row - 2][col + 1] == i)&& // up 2 right 1 
					   (board[row + 1][col + 1] == 0 || // down 1 right 1 
					    board[row + 1][col + 1] == i)&& // down 1 right 1 
					   (board[row + 2][col + 1] == 0 || // down 2 right 1 
					    board[row + 2][col + 1] == i)&& // down 2 right 1 
					   (board[row][col + 3]		== 0 || // right 3
					    board[row][col + 3]		== i || // right 3
					    board[row][col - 2]		== 0 || // left 2
					    board[row][col - 2]		== i)){ // left 2
						
						  smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if
				} // end else if 
			} // end if
				
			if((board[row][col - 2]    == 0 || // right 2
			    board[row][col - 2]    == i)&& // right 2
			   (board[row][col + 1]    == 0 || // left 1
			    board[row][col + 1]    == i)&& // left 1
			   (board[row - 2][col]    == 0 || // up 2
			    board[row - 2][col]    == i)&& // up 2
			   (board[row + 1][col]    == 0 || // down 1
			    board[row + 1][col]    == i)&& // down 1
			   (board[row + 2][col]    == 0 || // down 2
			    board[row + 2][col]    == i)&& // down 2
			   (board[row][col + 3]    == 0 || // right 3
			    board[row][col + 3]    == i || // right 3
			    board[row][col - 2]    == 0 || // left 2
			    board[row][col - 2]    == i)){ // left 2

				if(board[row][col + 1]  == i && // right 1
				   board[row - 1][col]  == 0){ // up 1
						  
					  smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col + 1]  == 0 && // right 1
					      board[row - 1][col]  == i){ // up 1
				
					  smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end if						
			} // end if
			
			if(board[row + 1][col - 1]  == i){ // diagonal down left
				if(board[row + 1][col]  == 0){ // down 1
					
					if((board[row + 1][col - 2] == 0 || // down 1 left 2
					    board[row + 1][col - 2] == i)&& // down 1 left 2
					   (board[row + 1][col + 1] == 0 || // down 1 right 1
					    board[row + 1][col + 1] == i)&& // down 1 right 1
					   (board[row + 1][col + 2] == 0 || // down 1 right 2
					    board[row + 1][col + 2] == i)&& // down 1 right 2
					   (board[row - 1][col]     == 0 || // up 1
					    board[row - 1][col]     == i)&& // up 1
					   (board[row + 2][col]     == 0 || // down 2
					    board[row + 2][col]     == i)&& // down 2
					   (board[row][col + 2]     == 0 || // up 2 
					    board[row][col + 2]     == i || // up 2 
					    board[row + 3][col]     == 0 || // down 3
					    board[row + 3][col]     == i)){ // down 3

						  smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					} // end if
				} else if(board[row][col - 1]  == 0){ // left 1
				  
					if((board[row][col - 2]     == 0 || // left 2
					    board[row][col - 2]     == i)&& // left 2
					   (board[row][col + 1]     == 0 || // right 1
					    board[row][col + 1]     == i)&& // right 1
					   (board[row][col + 2]     == 0 || // right 2
					    board[row][col + 2]     == i)&& // right 2
					   (board[row - 1][col - 1] == 0 || // up 1 left 1
					    board[row - 1][col - 1] == i)&& // up 1 left 1
					   (board[row + 2][col - 1] == 0 || // down 2 left 1
					    board[row + 2][col - 1] == i)&& // down 2 left 1
					   (board[row + 3][col - 1] == 0 || // down 3 left 1
					    board[row + 3][col - 1] == i || // down 3 left 1
					    board[row - 2][col - 1] == 0 || // up 2 left 1
					    board[row - 2][col - 1] == i)){ // up 2 left 1

						  smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if
				} // end else if
			} // end if
				
			if((board[row][col - 2] == 0 || // left 2
			    board[row][col - 2] == i)&& // left 2
			   (board[row][col + 1] == 0 || // right 1
			    board[row][col + 1] == i)&& // right 1
			   (board[row][col + 2] == 0 || // right 2
			    board[row][col + 2] == i)&& // right 2
			   (board[row - 1][col] == 0 || // up 1
			    board[row - 1][col] == i)&& // up 1
			   (board[row + 2][col] == 0 || // down 2
			    board[row + 2][col] == i)&& // down 2
			   (board[row - 2][col] == 0 || // up 2
			    board[row - 2][col] == i || // up 2
			    board[row + 3][col] == 0 || // down 3
			    board[row + 3][col] == i)){ // down 3

				if(board[row][col - 1]  == i && // left 1
						board[row + 1][col]  == 0){ // down 1
					  
					  smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col - 1]  == 0 && // left 1
						board[row + 1][col]  == i){ // down 1
										
					smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end if						
			} // end if
			
			if(board[row + 1][col + 1]  == i){ // diagonal down right
				
				if(board[row + 1][col]  == 0){ // down 1
					
					if((board[row + 1][col + 2] == 0 || // down 1 right 2
					    board[row + 1][col + 2] == i)&& // down 1 right 2
					   (board[row + 1][col - 1] == 0 || // down 1 left 1
					    board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row + 2][col]     == 0 || // down 2
					    board[row + 2][col]     == i)&& // down 2
					   (board[row - 1][col]     == 0 || // up 1
					    board[row - 1][col]     == i)&& // up 1
					   (board[row + 1][col - 2] == 0 || // down 1 left 2
					    board[row + 1][col - 2] == i || // down 1 left 2
					    board[row + 1][col + 3] == 0 || // down 1 right 3
					    board[row + 1][col + 3] == i)&& // down 1 right 3
					   (board[row + 3][col]     == 0 || // down 3
					    board[row + 3][col]     == i || // down 3
					    board[row - 2][col]     == 0 || // up 2
					    board[row - 2][col]     == i)){ // up 2
					
					  smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					} // end if
				} // end if
				if(board[row][col + 1]  == 0){ // right 1
				  
					if((board[row][col - 1]     == 0 || // left 1
					    board[row][col - 1]     == i)&& // left 1
					   (board[row][col + 2]     == 0 || // right 2
					    board[row][col + 2]     == i)&& // right 2
					   (board[row - 1][col + 1] == 0 || // up 1 right 1
					    board[row - 1][col + 1] == i)&& // up 1 right 1
					   (board[row + 2][col + 1] == 0 || // down 2 right 1
					    board[row + 2][col + 1] == i)&& // down 2 right 1
					   (board[row][col - 2]     == 0 || // left 2
					    board[row][col - 2]     == i || // left 2
					    board[row][col + 3]     == 0 || // right 3
					    board[row][col + 3]     == i)&& // right 3
					   (board[row + 3][col + 1] == 0 || // down 3 right 1
					    board[row + 3][col + 1] == i || // down 3 right 1
					    board[row - 2][col + 1] == 0 || // up 2 right 1
					    board[row - 2][col + 1] == i)){ // up 2 right 1
						
						  smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if
				} // end diagonal down right
			} // end if
					
			if((board[row][col + 2] == 0 || // right 2
			    board[row][col + 2] == i)&& // right 2
			   (board[row][col - 1] == 0 || // left 1
			    board[row][col - 1] == i)&& // left 1
			   (board[row - 1][col] == 0 || // up 1
			    board[row - 1][col] == i)&& // up 1
			   (board[row + 2][col] == 0 || // down 2
			    board[row + 2][col] == i)&& // down 2
			   (board[row - 2][col] == 0 || // up 2
			    board[row - 2][col] == i || // up 2
			    board[row + 3][col] == 0 || // down 3
			    board[row + 3][col] == i)&& // down 3
			   (board[row][col + 3] == 0 || // right 3
			    board[row][col + 3] == i || // right 3
			    board[row][col - 2] == 0 || // left 2
			    board[row][col - 2] == i)){ // left 2

				if(board[row][col + 1]  == i && // right 1
				   board[row + 1][col]  == 0){  // down 1
					
					smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col + 1]  == 0 && // right 1
						  board[row + 1][col]  == i){  // down 1
					  
					  smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end if						
			} // end if
		} else if(row == 3 || row == 4 || row == 5){ // row 3-5 column 2
			if(board[row + 1][col - 1] == i){ // diagonal down left
				//System.out.println("down left");
				if(board[row + 1][col] == 0){ // down 1
					if((board[row + 1][col - 2] == 0 || // down 1 left 2
						board[row + 1][col - 2] == i)&& // down 1 left 2
					   (board[row + 1][col + 1] == 0 || // down 1 right 1
						board[row + 1][col + 1] == i)&& // down 1 right 1
					   (board[row + 2][col]     == 0 || // down 2
						board[row + 2][col]     == i)&& // down 2
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row + 1][col + 2] == 0 || // down 1 right 2
						board[row + 1][col + 2] == i)){ // down 1 right 2
						
						  smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					} // end if
				} // end if
				if(board[row][col - 1]   == 0){  // left 1
				  
					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row][col + 2]	    == 0 || // right 2
					   board[row][col + 2]	    == i)&& // right 2
					   (board[row + 2][col - 1] == 0 || // down 2 left 1 
						board[row + 2][col - 1] == i)&& // down 2 left 1 
					   (board[row - 1][col - 1] == 0 || // up 1 left 1 
						board[row - 1][col - 1] == i)&& // up 1 left 1 
					   (board[row - 2][col - 1] == 0 || // up 2 left 1
						board[row - 2][col - 1] == i)){ // up 2 left 1

						  smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if
				} // end else if 
			} // end if
				
			if((board[row][col - 2] == 0 || // left 2
				board[row][col - 2] == i)&& // left 2
			   (board[row][col + 1] == 0 || // right 1
				board[row][col + 1] == i)&& // right 1
			   (board[row][col + 2] == 0 || // right 2
				board[row][col + 2] == i)&& // right 2
			   (board[row + 2][col] == 0 || // down 2
				board[row + 2][col] == i)&& // down 2
			   (board[row - 1][col] == 0 || // up 1
				board[row - 1][col] == i)&& // up 1
			   (board[row - 2][col] == 0 || // up 2
				board[row - 2][col] == i)){ // up 2

				if(board[row][col - 1] == i && // left 1
						board[row + 1][col] == 0){ // down 1
						  
					  smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col - 1]  == 0 && // left 1
					board[row + 1][col] == i){ // down 1
				
				smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end else if
			} // end if						
			if(board[row + 1][col + 1] == i){ // diagonal down right
				if(board[row + 1][col] == 0){ // down 1
					
					if((board[row + 1][col + 2] == 0 || // down 1 right 2
						board[row + 1][col + 2] == i)&& // down 1 right 2
					   (board[row + 1][col - 1] == 0 || // down 1 left 1
						board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row + 2][col]     == 0 || // down 2
						board[row + 2][col]     == i)&& // down 2
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row + 1][col + 3] == 0 || // down 1 right 3
						board[row + 1][col + 3] == i || // down 1 right 3
						board[row + 1][col - 2] == 0 || // down 1 left 2
						board[row + 1][col - 2] == i)){ // down 1 left 2
						
						  smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					}
				} else if(board[row][col + 1]  == 0){ // right 1
				  
					if((board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row + 2][col + 1] == 0 || // down 2 right 1 
						board[row + 2][col + 1] == i)&& // down 2 right 1 
					   (board[row - 1][col + 1] == 0 || // up 1 right 1 
						board[row - 1][col + 1] == i)&& // up 1 right 1 
					   (board[row - 2][col + 1] == 0 || // up 2 right 1 
						board[row - 2][col + 1] == i)&& // up 2 right 1 
					   (board[row][col + 3]		== 0 || // right 3
						board[row][col + 3]		== i || // right 3
						board[row][col - 2]		== 0 || // left 2
						board[row][col - 2]		== i)){ // left 2

						  smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if
				} // end else if 
			} // end if
				
			if((board[row][col - 2]    == 0 || // right 2
				board[row][col - 2]    == i)&& // right 2
			   (board[row][col + 1]    == 0 || // left 1
				board[row][col + 1]    == i)&& // left 1
			   (board[row + 2][col]    == 0 || // down 2
				board[row + 2][col]    == i)&& // down 2
			   (board[row - 1][col]    == 0 || // up 1
				board[row - 1][col]    == i)&& // up 1
			   (board[row - 2][col]    == 0 || // up 2
				board[row - 2][col]    == i)&& // up 2
			   (board[row][col + 3]    == 0 || // right 3
				board[row][col + 3]    == i || // right 3
				board[row][col - 2]    == 0 || // left 2
				board[row][col - 2]    == i)){ // left 2

				if(board[row][col + 1]  == i && // right 1
				   board[row + 1][col]  == 0){ // down 1
						  
					  smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col + 1]  == 0 && // right 1
						  board[row + 1][col]  == i){ // down 1
				
					  smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end if						
			} // end if
			
			if(board[row - 1][col - 1]  == i){ // diagonal up left
				if(board[row - 1][col]  == 0){ // up 1
					
					if((board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i)&& // up 1 left 2
					   (board[row - 1][col + 1] == 0 || // up 1 right 1
						board[row - 1][col + 1] == i)&& // up 1 right 1
					   (board[row - 1][col + 2] == 0 || // up 1 right 2
						board[row - 1][col + 2] == i)&& // up 1 right 2
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row][col + 2]     == 0 || // down 2 
						board[row][col + 2]     == i || // down 2 
						board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i)){ // up 3

						  smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					} // end if
				} else if(board[row][col - 1]  == 0){ // left 1
				  
					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row + 1][col - 1] == 0 || // down 1 left 1
						board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row - 2][col - 1] == 0 || // up 2 left 1
						board[row - 2][col - 1] == i)&& // up 2 left 1
					   (board[row - 3][col - 1] == 0 || // up 3 left 1
						board[row - 3][col - 1] == i || // up 3 left 1
						board[row + 2][col - 1] == 0 || // down 2 left 1
						board[row + 2][col - 1] == i)){ // down 2 left 1
						
						  smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if
				} // end else if
			} // end if
				
			if((board[row][col - 2] == 0 || // left 2
				board[row][col - 2] == i)&& // left 2
			   (board[row][col + 1] == 0 || // right 1
				board[row][col + 1] == i)&& // right 1
			   (board[row][col + 2] == 0 || // right 2
				board[row][col + 2] == i)&& // right 2
			   (board[row + 1][col] == 0 || // down 1
				board[row + 1][col] == i)&& // down 1
			   (board[row - 2][col] == 0 || // up 2
				board[row - 2][col] == i)&& // up 2
			   (board[row + 2][col] == 0 || // down 2
				board[row + 2][col] == i || // down 2
				board[row - 3][col] == 0 || // up 3
				board[row - 3][col] == i)){ // up 3

				if(board[row][col - 1]  == i && // left 1
						board[row - 1][col]  == 0){ // up 1
					  
					  smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col - 1]  == 0 && // left 1
						board[row - 1][col]  == i){ // up 1
										
					smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end if						
			} // end if
			
			if(board[row - 1][col + 1]  == i){ // diagonal up right
				
				if(board[row - 1][col]  == 0){ // up 1
					
					if((board[row - 1][col + 2] == 0 || // up 1 right 2
						board[row - 1][col + 2] == i)&& // up 1 right 2
					   (board[row - 1][col - 1] == 0 || // up 1 left 1
						board[row - 1][col - 1] == i)&& // up 1 left 1
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i || // up 1 left 2
						board[row - 1][col + 3] == 0 || // up 1 right 3
						board[row - 1][col + 3] == i)&& // up 1 right 3
					   (board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i || // up 3
						board[row + 2][col]     == 0 || // down 2
						board[row + 2][col]     == i)){ // down 2

					  smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					} // end if
				} // end if
				if(board[row][col + 1]  == 0){ // right 1
				  
					if((board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row + 1][col + 1] == 0 || // down 1 right 1
						board[row + 1][col + 1] == i)&& // down 1 right 1
					   (board[row - 2][col + 1] == 0 || // up 2 right 1
						board[row - 2][col + 1] == i)&& // up 2 right 1
					   (board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i || // left 2
						board[row][col + 3]     == 0 || // right 3
						board[row][col + 3]     == i)&& // right 3
					   (board[row - 3][col + 1] == 0 || // up 3 right 1
						board[row - 3][col + 1] == i || // up 3 right 1
						board[row + 2][col + 1] == 0 || // down 2 right 1
						board[row + 2][col + 1] == i)){ // down 2 right 1

						  smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if
				} // end diagonal up right
			} // end if
					
			if((board[row][col + 2] == 0 || // right 2
				board[row][col + 2] == i)&& // right 2
			   (board[row][col - 1] == 0 || // left 1
				board[row][col - 1] == i)&& // left 1
			   (board[row + 1][col] == 0 || // down 1
				board[row + 1][col] == i)&& // down 1
			   (board[row - 2][col] == 0 || // up 2
				board[row - 2][col] == i)&& // up 2
			   (board[row + 2][col] == 0 || // down 2
				board[row + 2][col] == i || // down 2
				board[row - 3][col] == 0 || // up 3
				board[row - 3][col] == i)&& // up 3
			   (board[row][col + 3] == 0 || // right 3
				board[row][col + 3] == i || // right 3
				board[row][col - 2] == 0 || // left 2
				board[row][col - 2] == i)){ // left 2

				if(board[row][col + 1]  == i && // right 1
				   board[row - 1][col]  == 0){  // up 1
					
					smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col + 1]  == 0 && // right 1
						  board[row - 1][col]  == i){  // up 1
					  
					  smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end if						
			} // end if
		}// end if
	
		return smallOpenLPositions;
	} // checked smallOpenLPositions
	
	public static MoveSet smallOpenLColumns_3_4_5(BoardNode node, Move move, MoveType type){
		MoveSet smallOpenLPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(row == 1){ // row 1, column 5
			if(board[row + 1][col + 1] == i){ // diagonal down right								
			
				if(board[row][col + 1] == 0){ // right 1
					if((board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row - 1][col + 1] == 0 || // up 1 right 1
						board[row - 1][col + 1] == i)&& // up 1 right 1
					   (board[row + 2][col + 1] == 0 || // down 2 right 1
						board[row + 2][col + 1] == i)&& // down 2 right 1
					   (board[row + 3][col + 1] == 0 || // down 3 right 1 
						board[row + 3][col + 1] == i)){ // down 3 right 1 
					  
						smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if
				} // end if
				if(board[row + 1][col]  == 0){ // down 1

					if((board[row + 1][col - 1] == 0 || // down 1 left 1
						board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row + 1][col - 2] == 0 || // down 1 left 2
						board[row + 1][col - 2] == i)&& // down 1 left 2
					   (board[row + 1][col + 2] == 0 || // down 1 right 2
						board[row + 1][col + 2] == i)&& // down 1 right 2
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row + 2][col]     == 0 || // down 2
						board[row + 2][col]     == i)&& // down 2
					   (board[row + 3][col]     == 0 || // down 3
						board[row + 3][col]     == i)){ // down 3

						smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					} // end if
				} // end else if
			} // end if
			if((board[row][col + 2] == 0 ||	// right 2
				board[row][col + 2] == i)&&	// right 2
			   (board[row][col - 1] == 0 || // left 1
				board[row][col - 1] == i)&& // left 1
			   (board[row][col - 2] == 0 || // left 2
				board[row][col - 2] == i)&& // left 2
			   (board[row - 1][col] == 0 || // up 1
				board[row - 1][col] == i)&& // up 1
			   (board[row + 2][col] == 0 || // down 2 
				board[row + 2][col] == i)&& // down 2 
			   (board[row + 3][col] == 0 || // down 3 
				board[row + 3][col] == i)){ // down 3 

				if(board[row][col + 1]  == i && // right 1
				   board[row + 1][col]  == 0){  // down 1
				  
					smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col + 1]  == 0 && // right 1
						  board[row + 1][col]  == i){  // down 1
					 
					smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end else if
			} // end if						
			if(board[row + 1][col - 1] == i){ // diagonal down left
				if(board[row][col - 1] == 0){  // left 1
				
					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row - 1][col - 1] == 0 || // up 1  left 1
						board[row - 1][col - 1] == i)&& // up 1  left 1
					   (board[row + 2][col - 1] == 0 || // down 2 left 1
						board[row + 2][col - 1] == i)&& // down 2 left 1
					   (board[row + 3][col - 1] == 0 || // down 3 left 1 
						board[row + 3][col - 1] == i)&& // down 3 left 1 
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i || // left 3
						board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)){ // right 2
					  
						smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if
				} // end if
				if(board[row + 1][col]  == 0){ // down 1
					
					if((board[row + 1][col + 1] == 0 || // down 1 right 1
						board[row + 1][col + 1] == i)&& // down 1 right 1
					   (board[row + 1][col - 2] == 0 || // down 1 left 2
						board[row + 1][col - 2] == i)&& // down 1 left 2
					   (board[row - 1][col]     == 0 || // up 1 
						board[row - 1][col]     == i)&& // up 1 
					   (board[row + 2][col]     == 0 || // down 2
						board[row + 2][col]     == i)&& // down 2
					   (board[row + 3][col]     == 0 || // down 3
						board[row + 3][col]     == i)&& // down 3
					   (board[row + 1][col + 2] == 0 || // down 1 right 2
						board[row + 1][col + 2] == i || // down 1 right 2
						board[row + 1][col - 3] == 0 || // down 1 left 3
						board[row + 1][col - 3] == i)){ // down 1 left 3

						
						smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					} // end if
				} // end else if
			} // end if
				
			if((board[row][col + 1] == 0 || // right 1
				board[row][col + 1] == i)&& // right 1
			   (board[row][col - 2] == 0 || // left 2
				board[row][col - 2] == i)&& // left 2
			   (board[row - 1][col] == 0 || // up 1
				board[row - 1][col] == i)&& // up 1
			   (board[row + 2][col] == 0 || // down 2
				board[row + 2][col] == i)&& // down 2
			   (board[row + 3][col] == 0 || // down 3
				board[row + 3][col] == i)&& // down 3
			   (board[row][col + 2] == 0 || // right 2
				board[row][col + 2] == i || // right 2
				board[row][col - 3] == 0 || // left 3
				board[row][col - 3] == i)){ // left 3

				if(board[row][col - 1]  == i && // left 1
						board[row + 1][col]  == 0){ // down 1
				  
				   smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row + 1][col]  == i){ // down 1
					smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end else if
			} // end if				  
		} if(row == 6){ // row 6 column 5
			if(board[row - 1][col + 1] == i){ // diagonal up right								
				
				if(board[row][col + 1] == 0){ // right 1
					
					if((board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row + 1][col + 1] == 0 || // down 1 right 1
						board[row + 1][col + 1] == i)&& // down 1 right 1
					   (board[row - 2][col + 1] == 0 || // up 2 right 1
						board[row - 2][col + 1] == i)&& // up 2 right 1
					   (board[row - 3][col + 1] == 0 || // up 3 right 1 
						board[row - 3][col + 1] == i)){ // up 3 right 1 
					  
						smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if
				} // end if
				if(board[row - 1][col]  == 0){ // up 1

					if((board[row - 1][col - 1] == 0 || // up 1 left 1
						board[row - 1][col - 1] == i)&& // up 1 left 1
					   (board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i)&& // up 1 left 2
					   (board[row - 1][col + 2] == 0 || // up 1 right 2
						board[row - 1][col + 2] == i)&& // up 1 right 2
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i)){ // up 3


						smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					} // end if
				} // end else if
			} // end if
			if((board[row][col + 2] == 0 ||	// right 2
				board[row][col + 2] == i)&&	// right 2
			   (board[row][col - 1] == 0 || // left 1
				board[row][col - 1] == i)&& // left 1
			   (board[row][col - 2] == 0 || // left 2
				board[row][col - 2] == i)&& // left 2
			   (board[row + 1][col] == 0 || // down 1
				board[row + 1][col] == i)&& // down 1
			   (board[row - 2][col] == 0 || // up 2 
				board[row - 2][col] == i)&& // up 2 
			   (board[row - 3][col] == 0 || // up 3 
				board[row - 3][col] == i)){ // up 3 

				if(board[row][col + 1]  == i && // right 1
				   board[row - 1][col]  == 0){  // up 1
				  
					smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col + 1]  == 0 && // right 1
						  board[row - 1][col]  == i){  // up 1
					 
					smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end else if
			} // end if						
			if(board[row - 1][col - 1] == i){ // diagonal up left
				if(board[row][col - 1] == 0){ // left 1
				
					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row + 1][col - 1] == 0 || // down 1 left 1
						board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row - 2][col - 1] == 0 || // up 2 left 1
						board[row - 2][col - 1] == i)&& // up 2 left 1
					   (board[row - 3][col - 1] == 0 || // up 3 left 1 
						board[row - 3][col - 1] == i)&& // up 3 left 1 
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i || // left 3
						board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)){ // right 2

					  
						smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if
				} // end if
				if(board[row - 1][col]  == 0){ // up 1
					
					if((board[row - 1][col + 1] == 0 || // up 1 right 1
						board[row - 1][col + 1] == i)&& // up 1 right 1
					   (board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i)&& // up 1 left 2
					   (board[row + 1][col]     == 0 || // down 1 
						board[row + 1][col]     == i)&& // down 1 
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i)&& // up 3
					   (board[row - 1][col + 2] == 0 || // up 1 right 2
						board[row - 1][col + 2] == i || // up 1 right 2
						board[row - 1][col - 3] == 0 || // up 1 left 3
						board[row - 1][col - 3] == i)){ // up 1 left 3
						
						smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					} // end if
				} // end else if
			} // end if
				
				if((board[row][col + 1] == 0 || // right 1
					board[row][col + 1] == i)&& // right 1
				   (board[row][col - 2] == 0 || // left 2
					board[row][col - 2] == i)&& // left 2
				   (board[row + 1][col] == 0 || // down 1
					board[row + 1][col] == i)&& // down 1
				   (board[row - 2][col] == 0 || // up 2
					board[row - 2][col] == i)&& // up 2
				   (board[row - 3][col] == 0 || // up 3
					board[row - 3][col] == i)&& // up 3
				   (board[row][col + 2] == 0 || // right 2
					board[row][col + 2] == i || // right 2
					board[row][col - 3] == 0 || // left 3
					board[row][col - 3] == i)){ // left 3

				if(board[row][col - 1]  == i && // left 1
						board[row - 1][col]  == 0){ // up 1
				  
				   smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row - 1][col]  == i){ // up 1
					smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end else if
			} // end if				  
		} else if(row == 2 || row == 3 || row == 4){ // row 2-4, column 5
			if(board[row - 1][col + 1] == i){ // diagonal up right
			
				if(board[row - 1][col] == 0){ // up 1
					if((board[row - 1][col + 2] == 0 || // up 1 right 2
						board[row - 1][col + 2] == i)&& // up 1 right 2
					   (board[row - 1][col - 1] == 0 || // up 1 left 1
						board[row - 1][col - 1] == i)&& // up 1 left 1
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row + 2][col]     == 0 || // down 2
						board[row + 2][col]     == i)&& // down 2
					   (board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i)){ // up 1 left 2
						
						  smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					} // end if
				} // end if
				if(board[row][col + 1]   == 0){  // right 1
				  
					if((board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row][col - 2]	    == 0 || // left 2
					   board[row][col - 2]	    == i)&& // left 2
					   (board[row - 2][col + 1] == 0 || // up 2 right 1 
						board[row - 2][col + 1] == i)&& // up 2 right 1 
					   (board[row + 1][col + 1] == 0 || // down 1 right 1 
						board[row + 1][col + 1] == i)&& // down 1 right 1 
					   (board[row + 2][col + 1] == 0 || // down 2 right 1
						board[row + 2][col + 1] == i)){ // down 2 right 1
						
						  smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if
				} // end else if 
			} // end if
				
			if((board[row][col + 2] == 0 || // right 2
				board[row][col + 2] == i)&& // right 2
			   (board[row][col - 1] == 0 || // left 1
				board[row][col - 1] == i)&& // left 1
			   (board[row][col - 2] == 0 || // left 2
				board[row][col - 2] == i)&& // left 2
			   (board[row - 2][col] == 0 || // up 2
				board[row - 2][col] == i)&& // up 2
			   (board[row + 1][col] == 0 || // down 1
				board[row + 1][col] == i)&& // down 1
			   (board[row + 2][col] == 0 || // down 2
				board[row + 2][col] == i)){ // down 2

				if(board[row][col + 1] == i && // right 1
						board[row - 1][col] == 0){ // up 1
						  
					  smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col + 1]  == 0 && // right 1
					board[row - 1][col] == i){ // up 1
				
				smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end else if
			} // end if						
			if(board[row - 1][col - 1] == i){ // diagonal up left
				if(board[row - 1][col] == 0){ // up 1
					
					if((board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i)&& // up 1 left 2
					   (board[row - 1][col + 1] == 0 || // up 1 right 1
						board[row - 1][col + 1] == i)&& // up 1 right 1
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row + 2][col]     == 0 || // down 2
						board[row + 2][col]     == i)&& // down 2
					   (board[row - 1][col - 3] == 0 || // up 1 left 3
						board[row - 1][col - 3] == i || // up 1 left 3
						board[row - 1][col + 2] == 0 || // up 1 right 2
						board[row - 1][col + 2] == i)){ // up 1 right 2
						
						  smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					}
				} else if(board[row][col - 1]  == 0){ // left 1
				  
					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row - 2][col - 1] == 0 || // up 2 left 1 
						board[row - 2][col - 1] == i)&& // up 2 left 1 
					   (board[row + 1][col - 1] == 0 || // down 1 left 1 
						board[row + 1][col - 1] == i)&& // down 1 left 1 
					   (board[row + 2][col - 1] == 0 || // down 2 left 1 
						board[row + 2][col - 1] == i)&& // down 2 left 1 
					   (board[row][col - 3]		== 0 || // left 3
						board[row][col - 3]		== i || // left 3
						board[row][col + 2]		== 0 || // right 2
						board[row][col + 2]		== i)){ // right 2
						
						  smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if
				} // end else if 
			} // end if
				
			if((board[row][col - 2]    == 0 || // left 2
				board[row][col - 2]    == i)&& // left 2
			   (board[row][col + 1]    == 0 || // right 1
				board[row][col + 1]    == i)&& // right 1
			   (board[row - 2][col]    == 0 || // up 2
				board[row - 2][col]    == i)&& // up 2
			   (board[row + 1][col]    == 0 || // down 1
				board[row + 1][col]    == i)&& // down 1
			   (board[row + 2][col]    == 0 || // down 2
				board[row + 2][col]    == i)&& // down 2
			   (board[row][col - 3]    == 0 || // left 3
				board[row][col - 3]    == i || // left 3
				board[row][col + 2]    == 0 || // right 2
				board[row][col + 2]    == i)){ // right 2

				if(board[row][col - 1]  == i && // left 1
				   board[row - 1][col]  == 0){ // up 1
						  
					  smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row - 1][col]  == i){ // up 1
				
					  smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end if						
			} // end if
			
			if(board[row + 1][col + 1]  == i){ // diagonal down right
				if(board[row + 1][col]  == 0){ // down 1
					
					if((board[row + 1][col + 2] == 0 || // down 1 right 2
						board[row + 1][col + 2] == i)&& // down 1 right 2
					   (board[row + 1][col - 1] == 0 || // down 1 left 1
						board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row + 1][col - 2] == 0 || // down 1 left 2
						board[row + 1][col - 2] == i)&& // down 1 left 2
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row + 2][col]     == 0 || // down 2
						board[row + 2][col]     == i)&& // down 2
					   (board[row][col + 2]     == 0 || // up 2 
						board[row][col + 2]     == i || // up 2 
						board[row + 3][col]     == 0 || // down 3
						board[row + 3][col]     == i)){ // down 3
						
						  smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					} // end if
				} else if(board[row][col + 1]  == 0){ // right 1
				  
					if((board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row - 1][col + 1] == 0 || // up 1 right 1
						board[row - 1][col + 1] == i)&& // up 1 right 1
					   (board[row + 2][col + 1] == 0 || // down 2 right 1
						board[row + 2][col + 1] == i)&& // down 2 right 1
					   (board[row + 3][col + 1] == 0 || // down 3 right 1
						board[row + 3][col + 1] == i || // down 3 right 1
						board[row - 2][col + 1] == 0 || // up 2 right 1
						board[row - 2][col + 1] == i)){ // up 2 right 1
						
						  smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if
				} // end else if
			} // end if
				
			if((board[row][col + 2] == 0 || // right 2
				board[row][col + 2] == i)&& // right 2
			   (board[row][col - 1] == 0 || // left 1
				board[row][col - 1] == i)&& // left 1
			   (board[row][col - 2] == 0 || // left 2
				board[row][col - 2] == i)&& // left 2
			   (board[row - 1][col] == 0 || // up 1
				board[row - 1][col] == i)&& // up 1
			   (board[row + 2][col] == 0 || // down 2
				board[row + 2][col] == i)&& // down 2
			   (board[row - 2][col] == 0 || // up 2
				board[row - 2][col] == i || // up 2
				board[row + 3][col] == 0 || // down 3
				board[row + 3][col] == i)){ // down 3

				if(board[row][col + 1]  == i && // right 1
						board[row + 1][col]  == 0){ // down 1
					  
					  smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col + 1]  == 0 && // right 1
						board[row + 1][col]  == i){ // down 1
										
					smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end if						
			} // end if
			
			if(board[row + 1][col - 1]  == i){ // diagonal down left
				
				if(board[row + 1][col]  == 0){ // down 1
					
					if((board[row + 1][col - 2] == 0 || // down 1 left 2
						board[row + 1][col - 2] == i)&& // down 1 left 2
					   (board[row + 1][col + 1] == 0 || // down 1 right 1
						board[row + 1][col + 1] == i)&& // down 1 right 1
					   (board[row + 2][col]     == 0 || // down 2
						board[row + 2][col]     == i)&& // down 2
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row + 1][col + 2] == 0 || // down 1 right 2
						board[row + 1][col + 2] == i || // down 1 right 2
						board[row + 1][col - 3] == 0 || // down 1 left 3
						board[row + 1][col - 3] == i)&& // down 1 left 3
					   (board[row + 3][col]     == 0 || // down 3
						board[row + 3][col]     == i || // down 3
						board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)){ // up 2

					  smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					} // end if
				} // end if
				if(board[row][col - 1]  == 0){ // left 1
				  
					if((board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row - 1][col - 1] == 0 || // up 1 left 1
						board[row - 1][col - 1] == i)&& // up 1 left 1
					   (board[row + 2][col - 1] == 0 || // down 2 left 1
						board[row + 2][col - 1] == i)&& // down 2 left 1
					   (board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i || // right 2
						board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row + 3][col - 1] == 0 || // down 3 left 1
						board[row + 3][col - 1] == i || // down 3 left 1
						board[row - 2][col - 1] == 0 || // up 2 left 1
						board[row - 2][col - 1] == i)){ // up 2 left 1

						  smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if
				} // end diagonal down left
			} // end if
					
			if((board[row][col - 2] == 0 || // left 2
				board[row][col - 2] == i)&& // left 2
			   (board[row][col + 1] == 0 || // right 1
				board[row][col + 1] == i)&& // right 1
			   (board[row - 1][col] == 0 || // up 1
				board[row - 1][col] == i)&& // up 1
			   (board[row + 2][col] == 0 || // down 2
				board[row + 2][col] == i)&& // down 2
			   (board[row - 2][col] == 0 || // up 2
				board[row - 2][col] == i || // up 2
				board[row + 3][col] == 0 || // down 3
				board[row + 3][col] == i)&& // down 3
			   (board[row][col - 3] == 0 || // left 3
				board[row][col - 3] == i || // left 3
				board[row][col + 2] == 0 || // right 2
				board[row][col + 2] == i)){ // right 2

				if(board[row][col - 1]  == i && // left 1
				   board[row + 1][col]  == 0){  // down 1
					
					smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row + 1][col]  == i){  // down 1
					  
					  smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end if						
			} // end if
		} else if(row == 3 || row == 4 || row == 5){ // row 3-5 column 5
			if(board[row + 1][col + 1] == i){ // diagonal down right
				//System.out.println("down right");
				if(board[row + 1][col] == 0){ // down 1
					if((board[row + 1][col + 2] == 0 || // down 1 right 2
						board[row + 1][col + 2] == i)&& // down 1 right 2
					   (board[row + 1][col - 1] == 0 || // down 1 left 1
						board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row + 2][col]     == 0 || // down 2
						board[row + 2][col]     == i)&& // down 2
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row + 1][col - 2] == 0 || // down 1 left 2
						board[row + 1][col - 2] == i)){ // down 1 left 2
						
						  smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					} // end if
				} // end if
				if(board[row][col + 1]   == 0){  // right 1
				  
					if((board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row][col - 2]	    == 0 || // left 2
					   board[row][col - 2]	    == i)&& // left 2
					   (board[row + 2][col + 1] == 0 || // down 2 right 1 
						board[row + 2][col + 1] == i)&& // down 2 right 1 
					   (board[row - 1][col + 1] == 0 || // up 1 right 1 
						board[row - 1][col + 1] == i)&& // up 1 right 1 
					   (board[row - 2][col + 1] == 0 || // up 2 right 1
						board[row - 2][col + 1] == i)){ // up 2 right 1

						  smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if
				} // end else if 
			} // end if
				
			if((board[row][col + 2] == 0 || // right 2
				board[row][col + 2] == i)&& // right 2
			   (board[row][col - 1] == 0 || // left 1
				board[row][col - 1] == i)&& // left 1
			   (board[row][col - 2] == 0 || // left 2
				board[row][col - 2] == i)&& // left 2
			   (board[row + 2][col] == 0 || // down 2
				board[row + 2][col] == i)&& // down 2
			   (board[row - 1][col] == 0 || // up 1
				board[row - 1][col] == i)&& // up 1
			   (board[row - 2][col] == 0 || // up 2
				board[row - 2][col] == i)){ // up 2

				if(board[row][col + 1] == i && // right 1
						board[row + 1][col] == 0){ // down 1
						  
					  smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col + 1]  == 0 && // right 1
					board[row + 1][col] == i){ // down 1
				
				smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end else if
			} // end if						
			if(board[row + 1][col - 1] == i){ // diagonal down left
				if(board[row + 1][col] == 0){ // down 1
					
					if((board[row + 1][col - 2] == 0 || // down 1 left 2
						board[row + 1][col - 2] == i)&& // down 1 left 2
					   (board[row + 1][col + 1] == 0 || // down 1 right 1
						board[row + 1][col + 1] == i)&& // down 1 right 1
					   (board[row + 2][col]     == 0 || // down 2
						board[row + 2][col]     == i)&& // down 2
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row + 1][col - 3] == 0 || // down 1 left 3
						board[row + 1][col - 3] == i || // down 1 left 3
						board[row + 1][col + 2] == 0 || // down 1 right 2
						board[row + 1][col + 2] == i)){ // down 1 right 2
						
						  smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					}
				} else if(board[row][col - 1]  == 0){ // left 1
				  
					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row + 2][col - 1] == 0 || // down 2 left 1 
						board[row + 2][col - 1] == i)&& // down 2 left 1 
					   (board[row - 1][col - 1] == 0 || // up 1 left 1 
						board[row - 1][col - 1] == i)&& // up 1 left 1 
					   (board[row - 2][col - 1] == 0 || // up 2 left 1 
						board[row - 2][col - 1] == i)&& // up 2 left 1 
					   (board[row][col - 3]		== 0 || // left 3
						board[row][col - 3]		== i || // left 3
						board[row][col + 2]		== 0 || // right 2
						board[row][col + 2]		== i)){ // right 2

						  smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if
				} // end else if 
			} // end if
				
			if((board[row][col - 2]    == 0 || // left 2
				board[row][col - 2]    == i)&& // left 2
			   (board[row][col + 1]    == 0 || // right 1
				board[row][col + 1]    == i)&& // right 1
			   (board[row + 2][col]    == 0 || // down 2
				board[row + 2][col]    == i)&& // down 2
			   (board[row - 1][col]    == 0 || // up 1
				board[row - 1][col]    == i)&& // up 1
			   (board[row - 2][col]    == 0 || // up 2
				board[row - 2][col]    == i)&& // up 2
			   (board[row][col - 3]    == 0 || // left 3
				board[row][col - 3]    == i || // left 3
				board[row][col + 2]    == 0 || // right 2
				board[row][col + 2]    == i)){ // right 2

				if(board[row][col - 1]  == i && // left 1
				   board[row + 1][col]  == 0){ // down 1
						  
					  smallOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row + 1][col]  == i){ // down 1
				
					  smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end if						
			} // end if
			
			if(board[row - 1][col + 1]  == i){ // diagonal up right
				if(board[row - 1][col]  == 0){ // up 1
					
					if((board[row - 1][col + 2] == 0 || // up 1 right 2
						board[row - 1][col + 2] == i)&& // up 1 right 2
					   (board[row - 1][col - 1] == 0 || // up 1 left 1
						board[row - 1][col - 1] == i)&& // up 1 left 1
					   (board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i)&& // up 1 left 2
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row][col + 2]     == 0 || // down 2 
						board[row][col + 2]     == i || // down 2 
						board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i)){ // up 3

						  smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					} // end if
				} else if(board[row][col + 1]  == 0){ // right 1
				  
					if((board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row + 1][col + 1] == 0 || // down 1 right 1
						board[row + 1][col + 1] == i)&& // down 1 right 1
					   (board[row - 2][col + 1] == 0 || // up 2 right 1
						board[row - 2][col + 1] == i)&& // up 2 right 1
					   (board[row - 3][col + 1] == 0 || // up 3 right 1
						board[row - 3][col + 1] == i || // up 3 right 1
						board[row + 2][col + 1] == 0 || // down 2 right 1
						board[row + 2][col + 1] == i)){ // down 2 right 1

						  smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					} // end if
				} // end else if
			} // end if
				
			if((board[row][col + 2] == 0 || // right 2
				board[row][col + 2] == i)&& // right 2
			   (board[row][col - 1] == 0 || // left 1
				board[row][col - 1] == i)&& // left 1
			   (board[row][col - 2] == 0 || // left 2
				board[row][col - 2] == i)&& // left 2
			   (board[row + 1][col] == 0 || // down 1
				board[row + 1][col] == i)&& // down 1
			   (board[row - 2][col] == 0 || // up 2
				board[row - 2][col] == i)&& // up 2
			   (board[row + 2][col] == 0 || // down 2
				board[row + 2][col] == i || // down 2
				board[row - 3][col] == 0 || // up 3
				board[row - 3][col] == i)){ // up 3

				if(board[row][col + 1]  == i && // right 1
						board[row - 1][col]  == 0){ // up 1
					  
					  smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col + 1]  == 0 && // right 1
						board[row - 1][col]  == i){ // up 1
										
					smallOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				} // end if						
			} // end if
			
			if(board[row - 1][col - 1]  == i){ // diagonal up left
				
				if(board[row - 1][col]  == 0){ // up 1
					
					if((board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i)&& // up 1 left 2
					   (board[row - 1][col + 1] == 0 || // up 1 right 1
						board[row - 1][col + 1] == i)&& // up 1 right 1
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row - 1][col + 2] == 0 || // up 1 right 2
						board[row - 1][col + 2] == i || // up 1 right 2
						board[row - 1][col - 3] == 0 || // up 1 left 3
						board[row - 1][col - 3] == i)&& // up 1 left 3
					   (board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i || // up 3
						board[row + 2][col]     == 0 || // down 2
						board[row + 2][col]     == i)){ // down 2

					  smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					} // end if
				} // end if
				if(board[row][col - 1]  == 0){ // left 1
				  
					if((board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row + 1][col - 1] == 0 || // down 1 left 1
						board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row - 2][col - 1] == 0 || // up 2 left 1
						board[row - 2][col - 1] == i)&& // up 2 left 1
					   (board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i || // right 2
						board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row - 3][col - 1] == 0 || // up 3 left 1
						board[row - 3][col - 1] == i || // up 3 left 1
						board[row + 2][col - 1] == 0 || // down 2 left 1
						board[row + 2][col - 1] == i)){ // down 2 left 1
						
						  smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					} // end if
				} // end diagonal up left
			} // end if
					
			if((board[row][col - 2] == 0 || // left 2
				board[row][col - 2] == i)&& // left 2
			   (board[row][col + 1] == 0 || // right 1
				board[row][col + 1] == i)&& // right 1
			   (board[row + 1][col] == 0 || // down 1
				board[row + 1][col] == i)&& // down 1
			   (board[row - 2][col] == 0 || // up 2
				board[row - 2][col] == i)&& // up 2
			   (board[row + 2][col] == 0 || // down 2
				board[row + 2][col] == i || // down 2
				board[row - 3][col] == 0 || // up 3
				board[row - 3][col] == i)&& // up 3
			   (board[row][col - 3] == 0 || // left 3
				board[row][col - 3] == i || // left 3
				board[row][col + 2] == 0 || // right 2
				board[row][col + 2] == i)){ // right 2

				if(board[row][col - 1]  == i && // left 1
				   board[row - 1][col]  == 0){  // up 1
					
					smallOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row - 1][col]  == i){  // up 1
					  
					  smallOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				} // end if						
			} // end if
		} // end if

		return smallOpenLPositions;
	} // end method
	


	public static MoveSet getPreSmallOpenLPositionsList(BoardNode node, boolean block){
		MoveSet preSmallOpenLPositions = new MoveSet();
		int player = 2/node.lastMove.player;
		int value = OPEN_L_VALUE;
		MoveType type;
		
		if(player == 1){ 
			value = -1*value;
		} // end if
		
		for(Move move : node.moves){
			int row = move.row;
			int col = move.column;

			if(block){
				if(move.player == player){ // attack
					continue;
				} else { // block
					type = new MoveType(MoveType.Type.BLOCK_PRE_SMALL_OPEN_L, getMultiplierValue(value, PRE_MULT, BLOCK_MULT));
				} // end else 
			} else {
				if(move.player != player){ // block
					continue;
				} else { // attack
					type = new MoveType(MoveType.Type.PRE_SMALL_OPEN_L, getMultiplierValue(value, PRE_MULT));
				} // end else
			} // end else
			
			if(row > 0 && row < 7 && col > 0 && col < 7){ // move not on perimeter
				if(col == 1 || col == 2 || col == 3 || col == 4){
					preSmallOpenLPositions.addAll(preSmallOpenL_Columns_1_2_3_4(node, move, type));
				} // end if
				if(col == 2 || col == 3 || col == 4 || col == 5){
					preSmallOpenLPositions.addAll(preSmallOpenL_Columns_2_3_4_5(node, move, type));
				} // end if
				if(col == 3 || col == 4 || col == 5 || col == 6){
					preSmallOpenLPositions.addAll(preSmallOpenL_Columns_3_4_5_6(node, move, type));
				} // end if
			 } // end if
		} // end for
//		preSmallOpenLPositions.print("preSmallOpenLPositions");
		return preSmallOpenLPositions;
	} // end method preOpenL
	
	public static MoveSet preSmallOpenL_Columns_1_2_3_4(BoardNode node, Move move, MoveType type){
		
		MoveSet preSmallOpenLPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(row == 1 || row == 2 || row == 3 || row == 4){ // rows 1-4 column 1
			if(board[row + 1][col + 1] == 0){ // diagonal down right
				if(board[row][col + 1] == 0 && // right 1
					board[row + 1][col] == 0) { // down 1
						
					if((board[row][col + 2]     == 0 || // right 2
					    board[row][col + 2]     == i)&& // right 2
					   (board[row][col + 3]     == 0 || // right 3
					    board[row][col + 3]     == i)&& // right 3
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row - 1][col + 1] == 0 || // up 1 right 1
					    board[row - 1][col + 1] == i)&& // up 1 right 1
					   (board[row + 2][col + 1] == 0 || // down 2 right 1
					    board[row + 2][col + 1] == i)&& // down 2 right 1
					   (board[row + 3][col + 1] == 0 || // down 3 right 1
						board[row + 3][col + 1] == i)){ // down 3 right 1
						
						 preSmallOpenLPositions.addUnique(new Move(row + 1, col + 1, player, type)); // down 1 right 1
					} 
					if((board[row + 2][col]		== 0 ||	// down 2
						board[row + 2][col]     == i)&& // down 2
					   (board[row + 3][col]     == 0 || // down 3
						board[row + 3][col]     == i)&& // down 3
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row + 1][col - 1] == 0 || // down 1 left 1 
						board[row + 1][col - 1] == i)&& // down 1 left 1 
					   (board[row + 1][col + 2] == 0 || // down 1 right 2 
						board[row + 1][col + 2] == i)&& // down 1 right 2 
					   (board[row + 1][col + 3] == 0 || // down 1 right 3 
						board[row + 1][col + 3] == i)){ // down 1 right 3 
						
						 preSmallOpenLPositions.addUnique(new Move(row + 1, col + 1, player, type)); // down 1 right 1
					} // end else if
				} // end if

			} // end if
		} // end if
		if(row == 2 || row == 3 || row == 4 || row == 5){
			if(board[row - 1][col + 1] == 0){ // diagonal up right
				if(board[row][col + 1] == 0 && // right 1
					board[row - 1][col] == 0) { // up 1
						
					if((board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row][col + 3]     == 0 || // right 3
						board[row][col + 3]     == i)&& // right 3
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row - 2][col + 1] == 0 || // up 2 right 1
						board[row - 2][col + 1] == i)&& // up 2 right 1
					   (board[row + 1][col + 1] == 0 || // down 1 right 1
						board[row + 1][col + 1] == i)&& // down 1 right 1
					   (board[row + 2][col + 1] == 0 || // down 2 right 1
						board[row + 2][col + 1] == i)){ // down 2 right 1
						
						 preSmallOpenLPositions.addUnique(new Move(row - 1, col + 1, player, type)); // up 1 right 1
					} // end if
					if((board[row - 2][col] == 0 || // up 2
						board[row - 2][col] == i)&& // up 2
					   (board[row + 1][col] == 0 || // down 1
						board[row + 1][col] == i)&& // down 1
					   (board[row + 2][col] == 0 || // down 2
						board[row + 2][col] == i)&& // down 2
					   (board[row][col - 1] == 0 || // left 1 
						board[row][col - 1] == i)&& // left 1 
					   (board[row][col + 2] == 0 || // right 2 
						board[row][col + 2] == i)&& // right 2 
					   (board[row][col + 3] == 0 || // right 3
						board[row][col + 3] == i)){ // right 3
						
						 preSmallOpenLPositions.addUnique(new Move(row - 1, col + 1, player, type)); // up 1 right 1
					} // end else if
				} // end if
	
			} // end if
			if(board[row + 1][col + 1] == 0){ // diagonal down right
				if(board[row][col + 1] == 0 && // right 1
					board[row + 1][col] == 0) { // down 1
						
					if((board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row][col + 3]     == 0 || // right 3
						board[row][col + 3]     == i)&& // right 3
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row + 2][col + 1] == 0 || // down 2 right 1
						board[row + 2][col + 1] == i)&& // down 2 right 1
					   (board[row - 1][col + 1] == 0 || // up 1 right 1
						board[row - 1][col + 1] == i)&& // up 1 right 1
					   (board[row - 2][col + 1] == 0 || // up 2 right 1
						board[row - 2][col + 1] == i)){ // up 2 right 1
						
						 preSmallOpenLPositions.addUnique(new Move(row + 1, col + 1, player, type)); // down 1 right 1
					} // end if
					if((board[row + 2][col] == 0 || // down 2
						board[row + 2][col] == i)&& // down 2
					   (board[row - 1][col] == 0 || // up 1
						board[row - 1][col] == i)&& // up 1
					   (board[row - 2][col] == 0 || // up 2
						board[row - 2][col] == i)&& // up 2
					   (board[row][col - 1] == 0 || // left 1 
						board[row][col - 1] == i)&& // left 1 
					   (board[row][col + 2] == 0 || // right 2 
						board[row][col + 2] == i)&& // right 2 
					   (board[row][col + 3] == 0 || // right 3
						board[row][col + 3] == i)){ // right 3
						
						 preSmallOpenLPositions.addUnique(new Move(row + 1, col + 1, player, type)); // down 1 right 1
					} // end else if
				} // end if

			} // end if
		} // end if
		if(row == 3 || row == 4 || row == 5 || row == 6){ // row 6 column 1
			if(board[row - 1][col + 1] == 0){ // diagonal up right
				if(board[row][col + 1] == 0 && // right 1
					board[row - 1][col] == 0) { // up 1
						
					if((board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row][col + 3]     == 0 || // right 3
						board[row][col + 3]     == i)&& // right 3
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row + 1][col + 1] == 0 || // down 1 right 1
						board[row + 1][col + 1] == i)&& // down 1 right 1
					   (board[row - 2][col + 1] == 0 || // up 2 right 1
						board[row - 2][col + 1] == i)&& // up 2 right 1
					   (board[row - 3][col + 1] == 0 || // up 3 right 1
						board[row - 3][col + 1] == i)){ // up 3 right 1
						
						 preSmallOpenLPositions.addUnique(new Move(row - 1, col + 1, player, type)); // up 1 right 1
					} // end if
					if((board[row - 2][col]	== 0 || // up 2
							board[row - 2][col]     == i)&& // up 2
						   (board[row - 3][col]     == 0 || // up 3
							board[row - 3][col]     == i)&& // up 3
						   (board[row + 1][col]     == 0 || // down 1
							board[row + 1][col]     == i)&& // down 1
						   (board[row - 1][col - 1] == 0 || // up 1 left 1 
							board[row - 1][col - 1] == i)&& // up 1 left 1 
						   (board[row - 1][col + 2] == 0 || // up 1 right 2 
							board[row - 1][col + 2] == i)&& // up 1 right 2 
						   (board[row - 1][col + 3] == 0 || // up 1 right 3 
							board[row - 1][col + 3] == i)){ // up 1 right 3 
							
							 preSmallOpenLPositions.addUnique(new Move(row - 1, col + 1, player, type)); // up 1 right 1
					} // end else if
				} // end if
			} // end if
		} // end if 

		return preSmallOpenLPositions;
	} // end method column1PreOpenL
	
		public static MoveSet preSmallOpenL_Columns_2_3_4_5(BoardNode node, Move move, MoveType type){
			MoveSet preSmallOpenLPositions = new MoveSet();
			int[][] board = node.board;
			int player = 2/node.lastMove.player;
			int i = move.player;
			int row = move.row;
			int col = move.column;
			
			if(row == 1 || row == 2 || row == 3 || row == 4){// rows 1-4 column 2
				if(board[row + 1][col + 1] == 0){ // diagonal down right
					if(board[row][col + 1] == 0 && // right 1
					   board[row + 1][col] == 0) { // down 1
							
							if((board[row][col + 2]     == 0 || // right 2
								board[row][col + 2]     == i)&& // right 2
							   (board[row][col - 1]     == 0 || // left 1
								board[row][col - 1]     == i)&& // left 1
						       (board[row][col - 2]     == 0 || // left 2
						        board[row][col - 2]     == i)&& // left 2
							   (board[row - 1][col + 1] == 0 || // up 1 right 1
								board[row - 1][col + 1] == i)&& // up 1 right 1
							   (board[row + 2][col + 1] == 0 || // down 2 right 1
								board[row + 2][col + 1] == i)&& // down 2 right 1
							   (board[row + 3][col + 1] == 0 || // down 3 right 1
								board[row + 3][col + 1] == i)){ // down 3 right 1
								
								 preSmallOpenLPositions.addUnique(new Move(row + 1, col + 1, player, type)); // down 1 right 1
							} 
							if((board[row + 2][col]== 0 || 		// down 2
								board[row + 2][col]     == i)&& // down 2
							   (board[row + 3][col]     == 0 || // down 3
								board[row + 3][col]     == i)&& // down 3
							   (board[row - 1][col]     == 0 || // up 1
								board[row - 1][col]     == i)&& // up 1
							   (board[row + 1][col - 1] == 0 || // down 1 left 1 
								board[row + 1][col - 1] == i)&& // down 1 left 1 
							   (board[row + 1][col - 2] == 0 || // down 1 left 2 
								board[row + 1][col - 2] == i)&& // down 1 left 2
							   (board[row + 1][col + 2] == 0 || // down 1 right 2 
								board[row + 1][col + 2] == i)){ // down 1 right 2 
								
								 preSmallOpenLPositions.addUnique(new Move(row + 1, col + 1, player, type)); // down 1 right 1
							} // end if
					
					} // end if
				} // end if
				if(board[row + 1][col - 1] == 0){   // diagonal down left
					if(board[row][col - 1] == 0 &&  // left 1
						board[row + 1][col] == 0) { // down 1
						
						if((board[row][col + 1]     == 0 || // right 1
							board[row][col + 1]     == i)&& // right 1
						   (board[row][col + 2]     == 0 || // right 2
							board[row][col + 2]     == i)&& // right 2
						   (board[row][col - 2]     == 0 || // left 2
							board[row][col - 2]     == i)&& // left 2
						   (board[row - 1][col - 1] == 0 || // up 1 left 1
							board[row - 1][col - 1] == i)&& // up 1 left 1
						   (board[row + 2][col - 1] == 0 || // down 2 left 1
							board[row + 2][col - 1] == i)&& // down 2 left 1
						   (board[row + 3][col - 1] == 0 || // down 3 left 1
							board[row + 3][col - 1] == i)){ // down 3 left 1
								
							 preSmallOpenLPositions.addUnique(new Move(row + 1, col - 1, player, type)); // down 1 left 1
						} 
						if((board[row + 2][col]		== 0 || // down 2
							board[row + 2][col]     == i)&& // down 2
						   (board[row + 3][col]     == 0 || // down 3
							board[row + 3][col]     == i)&& // down 3
						   (board[row - 1][col]     == 0 || // up 1
							board[row - 1][col]     == i)&& // up 1
						   (board[row + 1][col - 2] == 0 || // down 1 left 2 
							board[row + 1][col - 2] == i)&& // down 1 left 2 
						   (board[row + 1][col + 1] == 0 || // down 1 right 1 
							board[row + 1][col + 1] == i)&& // down 1 right 1 
						   (board[row + 1][col + 2] == 0 || // down 1 right 2 
							board[row + 1][col + 2] == i)){ // down 1 right 2 
							
							 preSmallOpenLPositions.addUnique(new Move(row + 1, col - 1, player, type)); // down 1 left 1
						} // end else if
					} // end if
		
				} // end if
			} // end if
		if(row == 2 || row == 3 || row == 4 || row == 5){
			if(board[row - 1][col + 1] == 0){ // diagonal up right
				if(board[row][col + 1] == 0 && // right 1
					board[row - 1][col] == 0) { // up 1
						
						if((board[row][col + 2]     == 0 || // right 2
							board[row][col + 2]     == i)&& // right 2
						   (board[row][col - 1]     == 0 || // left 1
							board[row][col - 1]     == i)&& // left 1
						   (board[row][col - 2]     == 0 || // left 2
							board[row][col - 2]     == i)&& // left 2
						   (board[row + 1][col + 1] == 0 || // down 1 right 1
							board[row + 1][col + 1] == i)&& // down 1 right 1
						   (board[row - 2][col + 1] == 0 || // up 2 right 1
							board[row - 2][col + 1] == i)&& // up 2 right 1
						   (board[row - 2][col + 1] == 0 || // down 2 right 1
							board[row - 2][col + 1] == i)){ // down 2 right 1
							
							 preSmallOpenLPositions.addUnique(new Move(row - 1, col + 1, player, type)); // up 1 right 1
						} 
						if((board[row - 2][col]		== 0 || // up 2
							board[row - 2][col]     == i)&& // up 2
						   (board[row + 1][col]     == 0 || // down 1
							board[row + 1][col]     == i)&& // down 1
						   (board[row - 2][col]     == 0 || // down 2
							board[row - 2][col]     == i)&& // down 2
						   (board[row - 1][col - 1] == 0 || // up 1 left 1 
							board[row - 1][col - 1] == i)&& // up 1 left 1 
						   (board[row - 1][col - 2] == 0 || // up 1 left 2 
							board[row - 1][col - 2] == i)&& // up 1 left 2
						   (board[row - 1][col + 2] == 0 || // up 1 right 2 
							board[row - 1][col + 2] == i)){ // up 1 right 2 
							
							 preSmallOpenLPositions.addUnique(new Move(row - 1, col + 1, player, type)); // up 1 right 1
						} // end else if
				
				} // end if
			} // end if
			if(board[row - 1][col - 1] == 0){ // diagonal up left
				if(board[row][col - 1] == 0 && // left 1
					board[row - 1][col] == 0) { // up 1
					
					if((board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row - 2][col - 1] == 0 || // up 2 left 1
						board[row - 2][col - 1] == i)&& // up 2 left 1
					   (board[row + 1][col - 1] == 0 || // down 1 left 1
					    board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row + 2][col - 1] == 0 || // down 2 left 1
						board[row + 2][col - 1] == i)){ // down 2 left 1
							
							 preSmallOpenLPositions.addUnique(new Move(row - 1, col - 1, player, type)); // up 1 left 1
					} 
					if((board[row - 2][col]		== 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row + 2][col]     == 0 || // down 2
						board[row + 2][col]     == i)&& // down 2
					   (board[row - 1][col - 2] == 0 || // up 1 left 2 
						board[row - 1][col - 2] == i)&& // up 1 left 2 
					   (board[row - 1][col + 1] == 0 || // up 1 right 1 
						board[row - 1][col + 1] == i)&& // up 1 right 1 
					   (board[row - 1][col + 2] == 0 || // up 1 right 2 
						board[row - 1][col + 2] == i)){ // up 1 right 2 
							
							 preSmallOpenLPositions.addUnique(new Move(row - 1, col - 1, player, type)); // up 1 left 1
						} // end else if
					} // end if
			} // end if
			if(board[row + 1][col + 1] == 0){ // diagonal down right
				if(board[row][col + 1] == 0 && // right 1
					board[row + 1][col] == 0) { // down 1
						
						if((board[row][col + 2]     == 0 || // right 2
							board[row][col + 2]     == i)&& // right 2
						   (board[row][col - 1]     == 0 || // left 1
							board[row][col - 1]     == i)&& // left 1
						   (board[row][col - 2]     == 0 || // left 2
							board[row][col - 2]     == i)&& // left 2
						   (board[row - 1][col + 1] == 0 || // up 1 right 1
							board[row - 1][col + 1] == i)&& // up 1 right 1
						   (board[row + 2][col + 1] == 0 || // down 2 right 1
							board[row + 2][col + 1] == i)&& // down 2 right 1
						   (board[row - 2][col + 1] == 0 || // up 2 right 1
							board[row - 2][col + 1] == i)){ // up 2 right 1
							
							 preSmallOpenLPositions.addUnique(new Move(row + 1, col + 1, player, type)); // down 1 right 1
						} 
						if((board[row + 2][col]		== 0 || // down 2
							board[row + 2][col]     == i)&& // down 2
						   (board[row - 1][col]     == 0 || // up 1
							board[row - 1][col]     == i)&& // up 1
						   (board[row - 2][col]     == 0 || // up 2
							board[row - 2][col]     == i)&& // up 2
						   (board[row + 1][col - 1] == 0 || // down 1 left 1 
							board[row + 1][col - 1] == i)&& // down 1 left 1 
						   (board[row + 1][col - 2] == 0 || // down 1 left 2 
							board[row + 1][col - 2] == i)&& // down 1 left 2
						   (board[row + 1][col + 2] == 0 || // down 1 right 2 
							board[row + 1][col + 2] == i)){ // down 1 right 2 
							
							 preSmallOpenLPositions.addUnique(new Move(row + 1, col + 1, player, type)); // down 1 right 1
						} // end else if
				
				} // end if
			} // end if
			if(board[row + 1][col - 1] == 0){ // diagonal down left
				if(board[row][col - 1] == 0 && // left 1
					board[row + 1][col] == 0) { // down 1
					
					if((board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row + 2][col - 1] == 0 || // down 2 left 1
						board[row + 2][col - 1] == i)&& // down 2 left 1
					   (board[row - 1][col - 1] == 0 || // up 1 left 1
						board[row - 1][col - 1] == i)&& // up 1 left 1
					   (board[row - 2][col - 1] == 0 || // up 2 left 1
						board[row - 2][col - 1] == i)){ // up 2 left 1
							
							 preSmallOpenLPositions.addUnique(new Move(row + 1, col - 1, player, type)); // down 1 left 1
					} 
					if((board[row + 2][col]		== 0 || // down 2
						board[row + 2][col]     == i)&& // down 2
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row - 2][col]     == 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row + 1][col - 2] == 0 || // down 1 left 2 
						board[row + 1][col - 2] == i)&& // down 1 left 2 
					   (board[row + 1][col + 1] == 0 || // down 1 right 1 
						board[row + 1][col + 1] == i)&& // down 1 right 1 
					   (board[row + 1][col + 2] == 0 || // down 1 right 2 
						board[row + 1][col + 2] == i)){ // down 1 right 2 
							
							 preSmallOpenLPositions.addUnique(new Move(row + 1, col - 1, player, type)); // down 1 left 1
						} // end else if
					} // end if
			} // end if
		} // end if
		if(row == 3 || row == 4 || row == 5 || row == 6){
			if(board[row - 1][col + 1] == 0){ // diagonal up right
				if(board[row][col + 1] == 0 && // right 1
				   board[row - 1][col] == 0) { // up 1
						
						if((board[row][col + 2]     == 0 || // right 2
							board[row][col + 2]     == i)&& // right 2
						   (board[row][col - 1]     == 0 || // left 1
							board[row][col - 1]     == i)&& // left 1
						   (board[row][col - 2]     == 0 || // left 2
							board[row][col - 2]     == i)&& // left 2
						   (board[row + 1][col + 1] == 0 || // down 1 right 1
							board[row + 1][col + 1] == i)&& // down 1 right 1
						   (board[row - 2][col + 1] == 0 || // up 2 right 1
							board[row - 2][col + 1] == i)&& // up 2 right 1
						   (board[row - 3][col + 1] == 0 || // up 3 right 1
							board[row - 3][col + 1] == i)){ // up 3 right 1
							
							 preSmallOpenLPositions.addUnique(new Move(row - 1, col + 1, player, type)); // up 1 right 1
						} 
						if((board[row - 2][col]== 0 || 		// up 2
							board[row - 2][col]     == i)&& // up 2
						   (board[row - 3][col]     == 0 || // up 3
							board[row - 3][col]     == i)&& // up 3
						   (board[row + 1][col]     == 0 || // down 1
							board[row + 1][col]     == i)&& // down 1
						   (board[row - 1][col - 1] == 0 || // up 1 left 1 
							board[row - 1][col - 1] == i)&& // up 1 left 1 
						   (board[row - 1][col - 2] == 0 || // up 1 left 2 
							board[row - 1][col - 2] == i)&& // up 1 left 2
						   (board[row - 1][col + 2] == 0 || // up 1 right 2 
							board[row - 1][col + 2] == i)){ // up 1 right 2 
							
							 preSmallOpenLPositions.addUnique(new Move(row - 1, col + 1, player, type)); // up 1 right 1
						} // end if
				
				} // end if
			} // end if
			if(board[row - 1][col - 1] == 0){   // diagonal up left
				if(board[row][col - 1] == 0 &&  // left 1
					board[row - 1][col] == 0) { // up 1
					
					if((board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row][col + 2]     == 0 || // right 2
						board[row][col + 2]     == i)&& // right 2
					   (board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row + 1][col - 1] == 0 || // down 1 left 1
						board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row - 2][col - 1] == 0 || // up 2 left 1
						board[row - 2][col - 1] == i)&& // up 2 left 1
					   (board[row - 3][col - 1] == 0 || // up 3 left 1
						board[row - 3][col - 1] == i)){ // up 3 left 1
							
						 preSmallOpenLPositions.addUnique(new Move(row - 1, col - 1, player, type)); // up 1 left 1
					} 
					if((board[row - 2][col]		== 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i)&& // up 3
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row - 1][col - 2] == 0 || // up 1 left 2 
						board[row - 1][col - 2] == i)&& // up 1 left 2 
					   (board[row - 1][col + 1] == 0 || // up 1 right 1 
						board[row - 1][col + 1] == i)&& // up 1 right 1 
					   (board[row - 1][col + 2] == 0 || // up 1 right 2 
						board[row - 1][col + 2] == i)){ // up 1 right 2 
						
						
						 preSmallOpenLPositions.addUnique(new Move(row - 1, col - 1, player, type)); // up 1 left 1
					} // end else if
				} // end if

			} // end if

		} // end if
		
		return preSmallOpenLPositions;
	} // end method column2PreOpenL xx
	
	public static MoveSet preSmallOpenL_Columns_3_4_5_6(BoardNode node, Move move, MoveType type){ 
		MoveSet preSmallOpenLPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(row == 1 || row == 2 || row == 3 || row == 4){ // column 6
			if(board[row + 1][col - 1] == 0){ // diagonal down left
				if(board[row][col - 1] == 0 && // left 1
				   board[row + 1][col] == 0) { // down 1
						
					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row - 1][col - 1] == 0 || // up 1 left 1
						board[row - 1][col - 1] == i)&& // up 1 left 1
					   (board[row + 2][col - 1] == 0 || // down 2 left 1
						board[row + 2][col - 1] == i)&& // down 2 left 1
					   (board[row + 3][col - 1] == 0 || // down 3 left 1
						board[row + 3][col - 1] == i)){ // down 3 left 1
						
						 preSmallOpenLPositions.addUnique(new Move(row + 1, col - 1, player, type)); // down 1 left 1
					} 
					if((board[row + 2][col]		== 0 || // down 2
						board[row + 2][col]     == i)&& // down 2
					   (board[row + 3][col]     == 0 || // down 3
						board[row + 3][col]     == i)&& // down 3
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row + 1][col + 1] == 0 || // down 1 right 1 
						board[row + 1][col + 1] == i)&& // down 1 right 1 
					   (board[row + 1][col - 2] == 0 || // down 1 left 2 
						board[row + 1][col - 2] == i)&& // down 1 left 2 
					   (board[row + 1][col - 3] == 0 || // down 1 left 3 
						board[row + 1][col - 3] == i)){ // down 1 left 3 
						
						 preSmallOpenLPositions.addUnique(new Move(row + 1, col - 1, player, type)); // down 1 left 1
					} // end else if
				} // end if

			} // end if
		} // end if
		if(row == 2 || row == 3 || row == 4 || row == 5){
			if(board[row - 1][col - 1] == 0){ // diagonal up left
				if(board[row][col - 1] == 0 && // left 1
				   board[row - 1][col] == 0) { // up 1
						
					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row - 2][col - 1] == 0 || // up 2 left 1
						board[row - 2][col - 1] == i)&& // up 2 left 1
					   (board[row + 1][col - 1] == 0 || // down 1 left 1
						board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row + 2][col - 1] == 0 || // down 2 left 1
						board[row + 2][col - 1] == i)){ // down 2 left 1
						
						 preSmallOpenLPositions.addUnique(new Move(row - 1, col - 1, player, type)); // up 1 left 1
					} // end if
					if((board[row - 2][col] == 0 || // up 2
						board[row - 2][col] == i)&& // up 2
					   (board[row + 1][col] == 0 || // down 1
						board[row + 1][col] == i)&& // down 1
					   (board[row + 2][col] == 0 || // down 2
						board[row + 2][col] == i)&& // down 2
					   (board[row][col + 1] == 0 || // right 1 
						board[row][col + 1] == i)&& // right 1 
					   (board[row][col - 2] == 0 || // left 2 
						board[row][col - 2] == i)&& // left 2 
					   (board[row][col - 3] == 0 || // left 3
						board[row][col - 3] == i)){ // left 3
						
						 preSmallOpenLPositions.addUnique(new Move(row - 1, col - 1, player, type)); // up 1 left 1
					} // end else if
				} // end if
			} // end if
			if(board[row + 1][col - 1] == 0){ // diagonal down left
				if(board[row][col - 1] == 0 && // left 1
					board[row + 1][col] == 0) { // down 1
						
					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row + 2][col - 1] == 0 || // down 2 left 1
						board[row + 2][col - 1] == i)&& // down 2 left 1
					   (board[row - 1][col - 1] == 0 || // up 1 left 1
						board[row - 1][col - 1] == i)&& // up 1 left 1
					   (board[row - 2][col - 1] == 0 || // up 2 left 1
						board[row - 2][col - 1] == i)){ // up 2 left 1
						
						 preSmallOpenLPositions.addUnique(new Move(row + 1, col - 1, player, type)); // down 1 left 1
					} // end if
					if((board[row + 2][col] == 0 || // down 2
						board[row + 2][col] == i)&& // down 2
					   (board[row - 1][col] == 0 || // up 1
					    board[row - 1][col] == i)&& // up 1
					   (board[row - 2][col] == 0 || // up 2
						board[row - 2][col] == i)&& // up 2
					   (board[row][col + 1] == 0 || // right 1 
						board[row][col + 1] == i)&& // right 1 
					   (board[row][col - 2] == 0 || // left 2 
						board[row][col - 2] == i)&& // left 2 
					   (board[row][col - 3] == 0 || // left 3
						board[row][col - 3] == i)){ // left 3
						
						 preSmallOpenLPositions.addUnique(new Move(row + 1, col - 1, player, type)); // down 1 left 1
					} // end else if
				} // end if
			} // end if
		}
		if(row == 3 || row == 4 || row == 5 || row == 6){ // row 6 column 6
			if(board[row - 1][col - 1] == 0){ // diagonal up left
				if(board[row][col - 1] == 0 && // left 1
					board[row - 1][col] == 0) { // up 1
						
					if((board[row][col - 2]     == 0 || // left 2
						board[row][col - 2]     == i)&& // left 2
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row + 1][col - 1] == 0 || // down 1 left 1
						board[row + 1][col - 1] == i)&& // down 1 left 1
					   (board[row - 2][col - 1] == 0 || // up 2 left 1
						board[row - 2][col - 1] == i)&& // up 2 left 1
					   (board[row - 3][col - 1] == 0 || // up 3 left 1
						board[row - 3][col - 1] == i)){ // up 3 left 1
						
						
						 preSmallOpenLPositions.addUnique(new Move(row - 1, col - 1, player, type)); // up 1 left 1
					} // end if
					if((board[row - 2][col]		== 0 || // up 2
						board[row - 2][col]     == i)&& // up 2
					   (board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i)&& // up 3
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row - 1][col + 1] == 0 || // up 1 right 1 
						board[row - 1][col + 1] == i)&& // up 1 right 1 
					   (board[row - 1][col - 2] == 0 || // up 1 left 2 
						board[row - 1][col - 2] == i)&& // up 1 left 2 
					   (board[row - 1][col - 3] == 0 || // up 1 left 3 
						board[row - 1][col - 3] == i)){ // up 1 left 3 
						
						
						 preSmallOpenLPositions.addUnique(new Move(row - 1, col - 1, player, type)); // up 1 left 1
					} // end else if

				} // end if
			} // end if
		} // end if 

		return preSmallOpenLPositions;

	} // end method
 // end method column6PreOpenL xx
	
	private static MoveSet getBigOpenLPositionsList(BoardNode node, boolean block){ 
		MoveSet bigOpenLPositions = new MoveSet();
		int value = OPEN_L_VALUE;
		int player = 2/node.lastMove.player;
		MoveType type;
		
		if(player == 1){ 
			value = -1*value;
		}
		
		for(Move move : node.moves){
			int row = move.row;
			int col = move.column;
			
			if(block){
				if(move.player == player){ // attack
					continue;
				} else { // block
					type = new MoveType(MoveType.Type.BLOCK_BIG_OPEN_L, getMultiplierValue(value, BLOCK_MULT));
				} // end else 
			} else {
				if(move.player != player){ // block
					continue;
				} else { // attack
					type = new MoveType(MoveType.Type.BIG_OPEN_L, value);
				} // end else
			} // end else
						
			if(row > 0 && row < 7 && col > 0 && row < 7){ // move not on perimeter
				if(col == 1 || col == 2 || col == 3 || col == 4){
					bigOpenLPositions.addAll(bigOpenL_columns_1_2_3_4(node, move, type));
				} // end if
				if(col == 3 || col == 4 || col == 5 || col == 6){
					bigOpenLPositions.addAll(bigOpenL_columns_3_4_5_6(node, move, type));
				} // end if
			} // end for
		}
//		bigOpenLPositions.print("bigOpenLPositions");
		return bigOpenLPositions;
	} // end method bigOpenL

	public static MoveSet bigOpenL_columns_1_2_3_4(BoardNode node, Move move, MoveType type){
		MoveSet bigOpenLPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		boolean block;

		if(type.toString().contains("BLOCK")){
			block = true;
		} else {
			block = false;
		}
		
		if(row == 1 || row == 2){ // row 1-2, column 1
			if(board[row + 2][col + 2] == i){ // diagonal down 2 right 2	
				
				if(board[row][col + 2] == 0){ // right 2
					
					if((board[row][col - 1]     == 0 || // left 1
					    board[row][col - 1]     == i)|| // left 1
					   (board[row][col + 3]     == 0 || // right 3
					    board[row][col + 3]     == i)&& // right 3
					   (board[row][col + 1]     == 0 || // right 1
					    board[row][col + 1]     == i)&& // right 1
					   (board[row - 1][col + 2] == 0 || // up 1 right 2
					    board[row - 1][col + 2] == i)&& // up 1 right 2
					   (board[row + 1][col + 2] == 0 || // down 1 right 2
					    board[row + 1][col + 2] == i)&& // down 1 right 2
					   (board[row + 3][col + 2] == 0 || // down 3 right 2
					    board[row + 3][col + 2] == i)){ // down 3 right 2
						
						if(block){
							if(board[row][col + 1]     == 0 ){ // right 1
								bigOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
							}
							if(board[row + 1][col + 2] == 0 ){ // down 1 right 2
								bigOpenLPositions.addUnique(new Move(row + 1, col + 2, player, type)); // down 1 right 2
							}
						}
						bigOpenLPositions.addUnique(new Move(row, col + 2, player, type)); // right 2
					} // end if
				} // end if
				if(board[row + 2][col] == 0){ // down 2
					
					if((board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)|| // up 1
					   (board[row + 3][col]     == 0 || // down 3
						board[row + 3][col]     == i)&& // down 3
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row + 2][col - 1] == 0 || // down 2 left 1 
						board[row + 2][col - 1] == i)&& // down 2 left 1 
					   (board[row + 2][col + 1] == 0 || // down 2 right 1 
						board[row + 2][col + 1] == i)&& // down 2 right 1 
					   (board[row + 2][col + 3] == 0 || // down 2 right 3 
						board[row + 2][col + 3] == i)){ // down 2 right 3 
						
						if(block){
							if(board[row + 1][col]     == 0 ){ // down 1
								bigOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
							}
							if(board[row + 2][col + 1] == 0 ){ // down 2 right 1 
								bigOpenLPositions.addUnique(new Move(row + 2, col + 1, player, type)); // right 1 down 2
							}
						}

						bigOpenLPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
					} // end if
				} // end if
			} // end if
		} if(row == 5 || row == 6){ // row 5-6, column 1
		if(board[row - 2][col + 2] == i){ // diagonal up 2 right 2	
			
			if(board[row][col + 2] == 0){ // right 2
				
				if((board[row][col - 1]     == 0 || // left 1
					board[row][col - 1]     == i)|| // left 1
				   (board[row][col + 3]     == 0 || // right 3
					board[row][col + 3]     == i)&& // right 3
				   (board[row][col + 1]     == 0 || // right 1
					board[row][col + 1]     == i)&& // right 1
				   (board[row + 1][col + 2] == 0 || // down 1 right 2
					board[row + 1][col + 2] == i)&& // down 1 right 2
				   (board[row - 1][col + 2] == 0 || // up 1 right 2
					board[row - 1][col + 2] == i)&& // up 1 right 2
				   (board[row - 3][col + 2] == 0 || // up 3 right 2
					board[row - 3][col + 2] == i)){ // up 3 right 2
				
					if(block){
						if(board[row][col + 1]     == 0 ){ // right 1
							bigOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
						}
						if(board[row - 1][col + 2] == 0 ){ // up 1 right 2
							bigOpenLPositions.addUnique(new Move(row - 1, col + 2, player, type)); // up 1 right 2
						}
					} // end if
					bigOpenLPositions.addUnique(new Move(row, col + 2, player, type)); // right 2
				} // end if
			} // end if
			if(board[row - 2][col] == 0){ // up 2
				
				if((board[row + 1][col]     == 0 || // down 1
					board[row + 1][col]     == i)|| // down 1
				   (board[row - 3][col]     == 0 || // up 3
					board[row - 3][col]     == i)&& // up 3
				   (board[row - 1][col]     == 0 || // up 1
					board[row - 1][col]     == i)&& // up 1
				   (board[row - 2][col - 1] == 0 || // up 2 left 1 
					board[row - 2][col - 1] == i)&& // up 2 left 1 
				   (board[row - 2][col + 1] == 0 || // up 2 right 1 
					board[row - 2][col + 1] == i)&& // up 2 right 1 
				   (board[row - 2][col + 3] == 0 || // up 2 right 3 
					board[row - 2][col + 3] == i)){ // up 2 right 3 
				  
					if(block){
						if(board[row - 1][col]     == 0 ){ // up 1
							bigOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
						}
						if(board[row - 2][col + 1] == 0 ){ // up 2 right 1 
							bigOpenLPositions.addUnique(new Move(row - 2, col + 1, player, type)); // right 1 up 2
						}
					} // end if

					bigOpenLPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
				} // end if
			} // end if
		} // end if
	} else if(row == 3 || row == 4){
			if(board[row + 2][col + 2] == i){ // diagonal down 2 right 2	
				
				if(board[row][col + 2] == 0){ // right 2
					
					if((board[row][col - 1]     == 0 || // left 1
					    board[row][col - 1]     == i)|| // left 1
					   (board[row][col + 3]     == 0 || // right 3
						board[row][col + 3]     == i)&& // right 3
					   (board[row][col + 1]     == 0 || // right 1
					    board[row][col + 1]     == i)&& // right 1
					   (board[row - 1][col + 2] == 0 || // up 1 right 2
					    board[row - 1][col + 2] == i)&& // up 1 right 2
					   (board[row + 1][col + 2] == 0 || // down 1 right 2
					    board[row + 1][col + 2] == i)&& // down 1 right 2
					   (board[row + 3][col + 2] == 0 || // down 3 right 2
					    board[row + 3][col + 2] == i)){ // down 3 right 2
					  
						if(block){
							if(board[row][col + 1]     == 0 ){ // right 1
								bigOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
							}
							if(board[row + 1][col + 2] == 0 ){ // down 1 right 2
								bigOpenLPositions.addUnique(new Move(row + 1, col + 2, player, type)); // down 1 right 2
							}
						} // end if
						
						bigOpenLPositions.addUnique(new Move(row, col + 2, player, type)); // right 2
					} // end if
				} // end if
				if(board[row + 2][col] == 0){ // down 2
					
					if((board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)|| // up 1
					   (board[row + 3][col]     == 0 || // down 3
						board[row + 3][col]     == i)&& // down 3
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row + 2][col - 1] == 0 || // down 2 left 1 
						board[row + 2][col - 1] == i)&& // down 2 left 1 
					   (board[row + 2][col + 1] == 0 || // down 2 right 1 
						board[row + 2][col + 1] == i)&& // down 2 right 1 
					   (board[row + 2][col + 3] == 0 || // down 2 right 3 
						board[row + 2][col + 3] == i)){ // down 2 right 3 
					  
						if(block){
							if(board[row + 1][col]     == 0 ){ // down 1
								bigOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
							}
							if(board[row + 2][col + 1] == 0 ){ // down 2 right 1 
								bigOpenLPositions.addUnique(new Move(row + 2, col + 1, player, type)); // right 1 down 2
							}
						} // end if
						
						bigOpenLPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
					} // end if
				} // end if
			} // end if
		if(board[row - 2][col + 2] == i){ // diagonal up 2 right 2	
			
			if(board[row][col + 2] == 0){ // right 2
				
				if((board[row][col - 1]     == 0 || // left 1
					board[row][col - 1]     == i)|| // left 1
				   (board[row][col + 3]     == 0 || // right 3
					board[row][col + 3]     == i)&& // right 3
				   (board[row][col + 1]     == 0 || // right 1
					board[row][col + 1]     == i)&& // right 1
				   (board[row + 1][col + 2] == 0 || // down 1 right 2
					board[row + 1][col + 2] == i)&& // down 1 right 2
				   (board[row - 1][col + 2] == 0 || // up 1 right 2
					board[row - 1][col + 2] == i)&& // up 1 right 2
				   (board[row - 3][col + 2] == 0 || // up 3 right 2
					board[row - 3][col + 2] == i)){ // up 3 right 2
					
					if(block){
						if(board[row][col + 1]     == 0 ){ // right 1
							bigOpenLPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
						}
						if(board[row - 1][col + 2] == 0 ){ // up 1 right 2
							bigOpenLPositions.addUnique(new Move(row - 1, col + 2, player, type)); // up 1 right 2
						}
					} // end if
					
					bigOpenLPositions.addUnique(new Move(row, col + 2, player, type)); // right 2
				} // end if
			} // end if
			if(board[row - 2][col] == 0){ // up 2
				
				if((board[row + 1][col]     == 0 || // down 1
					board[row + 1][col]     == i)|| // down 1
				   (board[row - 3][col]     == 0 || // up 3
					board[row - 3][col]     == i)&& // up 3
				   (board[row - 1][col]     == 0 || // up 1
					board[row - 1][col]     == i)&& // up 1
				   (board[row - 2][col - 1] == 0 || // up 2 left 1 
					board[row - 2][col - 1] == i)&& // up 2 left 1 
				   (board[row - 2][col + 1] == 0 || // up 2 right 1 
					board[row - 2][col + 1] == i)&& // up 2 right 1 
				   (board[row - 2][col + 3] == 0 || // up 2 right 3 
					board[row - 2][col + 3] == i)){ // up 2 right 3 
					
					if(block){
						if(board[row - 1][col]     == 0 ){ // up 1
							bigOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
						}
						if(board[row - 2][col + 1] == 0 ){ // up 2 right 1 
							bigOpenLPositions.addUnique(new Move(row - 2, col + 1, player, type)); // right 1 up 2
						}
					} // end if
					
					bigOpenLPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
				} // end if
			} // end if
		} // end if
	} // end if
	return bigOpenLPositions;
} // end class column1B
	
	public static MoveSet bigOpenL_columns_3_4_5_6(BoardNode node, Move move, MoveType type){
		MoveSet bigOpenLPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		boolean block;
		
		if(type.toString().contains("BLOCK")){
			block = true;
		} else {
			block = false;
		}
		
		if(row == 1 || row == 2){ // row 1-2, column 6
			if(board[row + 2][col - 2] == i){ // diagonal down 2 left 2	
				
				if(board[row][col - 2] == 0){ // left 2
					
					if((board[row][col + 1]     == 0 || // right 1
					    board[row][col + 1]     == i)|| // right 1
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row][col - 1]     == 0 || // left 1
					    board[row][col - 1]     == i)&& // left 1
					   (board[row - 1][col - 2] == 0 || // up 1 left 2
					    board[row - 1][col - 2] == i)&& // up 1 left 2
					   (board[row + 1][col - 2] == 0 || // down 1 left 2
					    board[row + 1][col - 2] == i)&& // down 1 left 2
					   (board[row + 3][col - 2] == 0 || // down 3 left 2
					    board[row + 3][col - 2] == i)){ // down 3 left 2
						
						if(block){
							if(board[row][col - 1]     == 0 ){ // left 1
								bigOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
							}
							if(board[row + 1][col - 2] == 0 ){ // down 1 left 2
								bigOpenLPositions.addUnique(new Move(row + 1, col - 2, player, type)); // down 1 left 2
							}
						} // end if
						
						bigOpenLPositions.addUnique(new Move(row, col - 2, player, type)); // left 2
					} // end if
				} // end if
				if(board[row + 2][col] == 0){ // down 2
					
					if((board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)|| // up 1
					   (board[row + 3][col]     == 0 || // down 3
						board[row + 3][col]     == i)&& // down 3
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row + 2][col + 1] == 0 || // down 2 right 1 
						board[row + 2][col + 1] == i)&& // down 2 right 1 
					   (board[row + 2][col - 1] == 0 || // down 2 left 1 
						board[row + 2][col - 1] == i)&& // down 2 left 1 
					   (board[row + 2][col - 3] == 0 || // down 2 left 3 
						board[row + 2][col - 3] == i)){ // down 2 left 3 
					  
						if(block){
							if(board[row + 1][col]     == 0 ){ // down 1
								bigOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
							}
							if(board[row + 2][col - 1] == 0 ){ // down 2 left 1 
								bigOpenLPositions.addUnique(new Move(row + 2, col - 1, player, type)); // left 1 down 2
							}
						}
						
						bigOpenLPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
					} // end if
				} // end if
			} // end if
		} if(row == 5 || row == 6){ // row 5-6, column 6
			if(board[row - 2][col - 2] == i){ // diagonal up 2 left 2	
				
				if(board[row][col - 2] == 0){ // left 2
					
					if((board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)|| // right 1
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row + 1][col - 2] == 0 || // down 1 left 2
						board[row + 1][col - 2] == i)&& // down 1 left 2
					   (board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i)&& // up 1 left 2
					   (board[row - 3][col - 2] == 0 || // up 3 left 2
						board[row - 3][col - 2] == i)){ // up 3 left 2
						
						if(block){
							if(board[row][col - 1]     == 0 ){ // left 1
								bigOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
							}
							if(board[row - 1][col - 2] == 0 ){ // up 1 left 2
								bigOpenLPositions.addUnique(new Move(row - 1, col - 2, player, type)); // up 1 left 2
							}
						} // end if
						
						bigOpenLPositions.addUnique(new Move(row, col - 2, player, type)); // left 2
					} // end if
				} // end if
				if(board[row - 2][col] == 0){ // up 2
					
					if((board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)|| // down 1
					   (board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i)&& // up 3
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row - 2][col + 1] == 0 || // up 2 right 1 
						board[row - 2][col + 1] == i)&& // up 2 right 1 
					   (board[row - 2][col - 1] == 0 || // up 2 left 1 
						board[row - 2][col - 1] == i)&& // up 2 left 1 
					   (board[row - 2][col - 3] == 0 || // up 2 left 3 
						board[row - 2][col - 3] == i)){ // up 2 left 3 
						
						if(block){
							if(board[row - 1][col]     == 0 ){ // up 1
								bigOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
							}
							if(board[row - 2][col - 1] == 0 ){ // up 2 left 1 
								bigOpenLPositions.addUnique(new Move(row - 2, col - 1, player, type)); // left 1 up 2
							}
						} // end if
						
						bigOpenLPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
					} // end if
				} // end if
			} // end if
		} else if(row == 3 || row == 4){
				if(board[row + 2][col - 2] == i){ // diagonal down 2 left 2	
					
					if(board[row][col - 2] == 0){ // left 2
						
						if((board[row][col + 1]     == 0 || // right 1
						    board[row][col + 1]     == i)|| // right 1
						   (board[row][col - 3]     == 0 || // left 3
							board[row][col - 3]     == i)&& // left 3
						   (board[row][col - 1]     == 0 || // left 1
						    board[row][col - 1]     == i)&& // left 1
						   (board[row - 1][col - 2] == 0 || // up 1 left 2
						    board[row - 1][col - 2] == i)&& // up 1 left 2
						   (board[row + 1][col - 2] == 0 || // down 1 left 2
						    board[row + 1][col - 2] == i)&& // down 1 left 2
						   (board[row + 3][col - 2] == 0 || // down 3 left 2
						    board[row + 3][col - 2] == i)){ // down 3 left 2
							
							if(block){
								if(board[row][col - 1]     == 0 ){ // left 1
									bigOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
								}
								if(board[row + 1][col - 2] == 0 ){ // down 1 left 2
									bigOpenLPositions.addUnique(new Move(row + 1, col - 2, player, type)); // down 1 left 2
								}
							} // end if
							
							bigOpenLPositions.addUnique(new Move(row, col - 2, player, type)); // left 2
						} // end if
					} // end if
					if(board[row + 2][col] == 0){ // down 2
						
						if((board[row - 1][col]     == 0 || // up 1
							board[row - 1][col]     == i)|| // up 1
						   (board[row + 3][col]     == 0 || // down 3
							board[row + 3][col]     == i)&& // down 3
						   (board[row + 1][col]     == 0 || // down 1
							board[row + 1][col]     == i)&& // down 1
						   (board[row + 2][col + 1] == 0 || // down 2 right 1 
							board[row + 2][col + 1] == i)&& // down 2 right 1 
						   (board[row + 2][col - 1] == 0 || // down 2 left 1 
							board[row + 2][col - 1] == i)&& // down 2 left 1 
						   (board[row + 2][col - 3] == 0 || // down 2 left 3 
							board[row + 2][col - 3] == i)){ // down 2 left 3 
						  
							if(block){
								if(board[row + 1][col]     == 0 ){ // down 1
									bigOpenLPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
								}
								if(board[row + 2][col - 1] == 0 ){ // down 2 left 1 
									bigOpenLPositions.addUnique(new Move(row + 2, col - 1, player, type)); // left 1 down 2
								}
							} // end if
							
							bigOpenLPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
						} // end if
					} // end if
				} // end if
			if(board[row - 2][col - 2] == i){ // diagonal up 2 left 2	
				
				if(board[row][col - 2] == 0){ // left 2
					
					if((board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)|| // right 1
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row + 1][col - 2] == 0 || // down 1 left 2
						board[row + 1][col - 2] == i)&& // down 1 left 2
					   (board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i)&& // up 1 left 2
					   (board[row - 3][col - 2] == 0 || // up 3 left 2
						board[row - 3][col - 2] == i)){ // up 3 left 2

						if(block){
							if(board[row][col - 1]     == 0 ){ // left 1
								bigOpenLPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
							}
							if(board[row - 1][col - 2] == 0 ){ // up 1 left 2
								bigOpenLPositions.addUnique(new Move(row - 1, col - 2, player, type)); // up 1 left 2
							}
						} // end if
						
						bigOpenLPositions.addUnique(new Move(row, col - 2, player, type)); // left 2
					} // end if
				} // end if
				if(board[row - 2][col] == 0){ // up 2
					
					if((board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)|| // down 1
					   (board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i)&& // up 3
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row - 2][col + 1] == 0 || // up 2 right 1 
						board[row - 2][col + 1] == i)&& // up 2 right 1 
					   (board[row - 2][col - 1] == 0 || // up 2 left 1 
						board[row - 2][col - 1] == i)&& // up 2 left 1 
					   (board[row - 2][col - 3] == 0 || // up 2 left 3 
						board[row - 2][col - 3] == i)){ // up 2 left 3 
						
						if(block){
							if(board[row - 1][col]     == 0 ){ // up 1
								bigOpenLPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
							}
							if(board[row - 2][col - 1] == 0 ){ // up 2 left 1 
								bigOpenLPositions.addUnique(new Move(row - 2, col - 1, player, type)); // left 1 up 2
							}
						} // end if
						
						bigOpenLPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
					} // end if
				} // end if
			} // end if
		} // end if
		return bigOpenLPositions;
	} // end method column6B
	
	public static MoveSet getPreBigOpenLPositionsList(BoardNode node, boolean block){
		MoveSet preBigOpenLPositions = new MoveSet();
		int player = 2/node.lastMove.player;
		int value = OPEN_L_VALUE;
		MoveType type;
		
		if(player == 1){ 
			value = -1*value;
		}
		
		for(Move move : node.moves){
			int row = move.row;
			int col = move.column;
			
			if(block){
				if(move.player == player){ // attack
					continue;
				} else { // block
					type = new MoveType(MoveType.Type.BLOCK_PRE_BIG_OPEN_L, getMultiplierValue(value, PRE_MULT, BLOCK_MULT));
				} // end else 
			} else {
				if(move.player != player){ // block
					continue;
				} else { // attack
					type = new MoveType(MoveType.Type.PRE_BIG_OPEN_L, getMultiplierValue(value, PRE_MULT));
				} // end else
			} // end else
			
			if(row > 0 && row < 7 && col > 0 && col < 7){ // move not on perimeter
				if(col == 1){
					preBigOpenLPositions.addAll(preBigOpenL_Column_1(node, move, type));
				} // end if
				if(col == 2 || col == 3 || col == 4){
					preBigOpenLPositions.addAll(preBigOpenL_Columns_2_3_4(node, move, type));
				} // end if
				if(col == 3 || col == 4 || col == 5){
					preBigOpenLPositions.addAll(preBigOpenL_Columns_3_4_5(node, move, type));
				} // end if
				if(col == 6){
					preBigOpenLPositions.addAll(preBigOpenL_Column_6(node, move, type));
				} // end if
			 } // end if
		} // end for
//		preBigOpenLPositions.print("preBigOpenLPositions");
		return preBigOpenLPositions;
	} // end method preOpenL
	
	public static MoveSet preBigOpenL_Column_1(BoardNode node, Move move, MoveType type){
		
		MoveSet preBigOpenLPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(row == 1 || row == 2 || row == 3 || row == 4){ // rows 1-4 column 1
			if(board[row + 2][col + 2] == 0){ // diagonal down 2 right 2
				if(board[row][col + 2] == 0) { // right 2
						
					if((board[row][col + 1]     == 0 || // right 1
					    board[row][col + 1]     == i)&& // right 1
					   (board[row][col + 3]     == 0 || // right 3
					    board[row][col + 3]     == i)&& // right 3
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row - 1][col + 2] == 0 || // up 1 right 2
					    board[row - 1][col + 2] == i)&& // up 1 right 2
					   (board[row + 1][col + 2] == 0 || // down 1 right 2
					    board[row + 1][col + 2] == i)&& // down 1 right 2
					   (board[row + 3][col + 1] == 0 || // down 3 right 1
						board[row + 3][col + 1] == i)){ // down 3 right 1
						
						 preBigOpenLPositions.addUnique(new Move(row + 2, col + 2, player, type)); // down 2 right 2
					} // end if
					
				} // end if	
				if(board[row][col + 2] == 0) { // down 2
					
					if((board[row + 1][col]     == 0 || // down 1
					    board[row + 1][col]     == i)&& // down 1
					   (board[row + 3][col]     == 0 || // down 3
					    board[row + 3][col]     == i)&& // down 3
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row + 2][col - 1] == 0 || // down 2 left 1 
					    board[row + 2][col - 1] == i)&& // down 2 left 1 
					   (board[row + 2][col + 1] == 0 || // down 2 right 1 
					    board[row + 2][col + 1] == i)&& // down 2 right 1 
					   (board[row + 1][col + 3] == 0 || // down 1 right 3 
						board[row + 1][col + 3] == i)){ // down 1 right 3 
						
						 preBigOpenLPositions.addUnique(new Move(row + 2, col + 2, player, type)); // right 2 down 2
					} // end if
					
				} // end if	
			} // end if
		} // end if
		if(row == 3 || row == 4 || row == 5 || row == 6){ // row 6 column 1
			if(board[row - 2][col + 2] == 0){ // diagonal up 2 right 2
				if(board[row][col + 2] == 0) { // right 2
						
					if((board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row][col + 3]     == 0 || // right 3
						board[row][col + 3]     == i)&& // right 3
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row + 1][col + 2] == 0 || // down 1 right 2
						board[row + 1][col + 2] == i)&& // down 1 right 2
					   (board[row - 1][col + 2] == 0 || // up 1 right 2
						board[row - 1][col + 2] == i)&& // up 1 right 2
					   (board[row - 3][col + 1] == 0 || // up 3 right 1
						board[row - 3][col + 1] == i)){ // up 3 right 1
						
						 preBigOpenLPositions.addUnique(new Move(row - 2, col + 2, player, type)); // up 2 right 2
					} // end if
					
				} // end if	
				if(board[row][col + 2] == 0) { // up 2
					
					if((board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i)&& // up 3
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row - 2][col - 1] == 0 || // up 2 left 1 
						board[row - 2][col - 1] == i)&& // up 2 left 1 
					   (board[row - 2][col + 1] == 0 || // up 2 right 1 
						board[row - 2][col + 1] == i)&& // up 2 right 1 
					   (board[row - 1][col + 3] == 0 || // up 1 right 3 
						board[row - 1][col + 3] == i)){ // up 1 right 3 
						
						 preBigOpenLPositions.addUnique(new Move(row - 2, col + 2, player, type)); // right 2 up 2
					} // end if
					
				} // end if	
			} // end if
		} // end if 

		return preBigOpenLPositions;
	} // end method column1PreOpenL
	
		public static MoveSet preBigOpenL_Columns_2_3_4(BoardNode node, Move move, MoveType type){
			MoveSet preBigOpenLPositions = new MoveSet();
			int[][] board = node.board;
			int player = 2/node.lastMove.player;
			int i = move.player;
			int row = move.row;
			int col = move.column;
			
			if(row == 1 || row == 2 || row == 3 || row == 4){// rows 1-4 column 2
				if(board[row + 2][col + 2] == 0){ // diagonal down 2 right 2	
					
					if(board[row][col + 2] == 0){ // right 2
						
						if((board[row][col - 1]     == 0 || // left 1
							board[row][col - 1]     == i)&& // left 1
						   (board[row][col + 1]     == 0 || // right 1
							board[row][col + 1]     == i)&& // right 1
						   (board[row][col + 3]     == 0 || // right 3
							board[row][col + 3]     == i)&& // right 3
						   (board[row - 1][col + 2] == 0 || // up 1 right 2
							board[row - 1][col + 2] == i)&& // up 1 right 2
						   (board[row + 1][col + 2] == 0 || // down 1 right 2
							board[row + 1][col + 2] == i)&& // down 1 right 2
						   (board[row + 3][col + 2] == 0 || // down 3 right 2
							board[row + 3][col + 2] == i)){ // down 3 right 2
						  
							preBigOpenLPositions.addUnique(new Move(row + 2, col + 2, player, type)); // down 2 right 2
						} // end if
					} // end if
					if(board[row + 2][col] == 0){ // down 2
						
						if((board[row - 1][col]     == 0 || // up 1
							board[row - 1][col]     == i)&& // up 1
						   (board[row + 1][col]     == 0 || // down 1
							board[row + 1][col]     == i)&& // down 1
						   (board[row + 3][col]     == 0 || // down 3
							board[row + 3][col]     == i)&& // down 3
						   (board[row + 2][col - 1] == 0 || // down 2 left 1 
							board[row + 2][col - 1] == i)&& // down 2 left 1 
						   (board[row + 2][col + 1] == 0 || // down 2 right 1 
							board[row + 2][col + 1] == i)&& // down 2 right 1 
						   (board[row + 2][col + 3] == 0 || // down 2 right 3 
							board[row + 2][col + 3] == i)){ // down 2 right 3 
						  
							preBigOpenLPositions.addUnique(new Move(row + 2, col + 2, player, type)); // down 2 right 2
						} // end if
					} // end if
				} // end if
			} // end of
		if(row == 3 || row == 4 || row == 5 || row == 6){// rows 3-6 column 2
			if(board[row - 2][col + 2] == 0){ // diagonal up 2 right 2	
				
				if(board[row][col + 2] == 0){ // right 2
					
					if((board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row][col + 3]     == 0 || // right 3
						board[row][col + 3]     == i)&& // right 3
					   (board[row + 1][col + 2] == 0 || // down 1 right 2
						board[row + 1][col + 2] == i)&& // down 1 right 2
					   (board[row - 1][col + 2] == 0 || // up 1 right 2
						board[row - 1][col + 2] == i)&& // up 1 right 2
					   (board[row - 3][col + 2] == 0 || // up 3 right 2
						board[row - 3][col + 2] == i)){ // up 3 right 2
						
						preBigOpenLPositions.addUnique(new Move(row - 2, col + 2, player, type)); // up 2 right 2
					} // end if
				} // end if
				if(board[row - 2][col] == 0){ // up 2
					
					if((board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i)&& // up 3
					   (board[row - 2][col - 1] == 0 || // up 2 left 1 
						board[row - 2][col - 1] == i)&& // up 2 left 1 
					   (board[row - 2][col + 1] == 0 || // up 2 right 1 
						board[row - 2][col + 1] == i)&& // up 2 right 1 
					   (board[row - 2][col + 3] == 0 || // up 2 right 3 
						board[row - 2][col + 3] == i)){ // up 2 right 3 
					  
						preBigOpenLPositions.addUnique(new Move(row - 2, col + 2, player, type)); // up 2 right 2
					} // end if
				} // end if
			} // end if
		} // end if
		
		return preBigOpenLPositions;
	} // end method column2PreOpenL xx
	

		public static MoveSet preBigOpenL_Columns_3_4_5(BoardNode node, Move move, MoveType type){
			MoveSet preBigOpenLPositions = new MoveSet();
			int[][] board = node.board;
			int player = 2/node.lastMove.player;
			int i = move.player;
			int row = move.row;
			int col = move.column;
			
			if(row == 1 || row == 2 || row == 3 || row == 4){// rows 1-4 column 5
				if(board[row + 2][col - 2] == 0){ // diagonal down 2 left 2	
					
					if(board[row][col - 2] == 0){ // left 2
						
						if((board[row][col + 1]     == 0 || // right 1
							board[row][col + 1]     == i)&& // right 1
						   (board[row][col - 1]     == 0 || // left 1
							board[row][col - 1]     == i)&& // left 1
						   (board[row][col - 3]     == 0 || // left 3
							board[row][col - 3]     == i)&& // left 3
						   (board[row - 1][col - 2] == 0 || // up 1 left 2
							board[row - 1][col - 2] == i)&& // up 1 left 2
						   (board[row + 1][col - 2] == 0 || // down 1 left 2
							board[row + 1][col - 2] == i)&& // down 1 left 2
						   (board[row + 3][col - 2] == 0 || // down 3 left 2
							board[row + 3][col - 2] == i)){ // down 3 left 2
						  
							preBigOpenLPositions.addUnique(new Move(row + 2, col - 2, player, type)); // down 2 left 2
						} // end if
					} // end if
					if(board[row + 2][col] == 0){ // down 2
						
						if((board[row - 1][col]     == 0 || // up 1
							board[row - 1][col]     == i)&& // up 1
						   (board[row + 1][col]     == 0 || // down 1
							board[row + 1][col]     == i)&& // down 1
						   (board[row + 3][col]     == 0 || // down 3
							board[row + 3][col]     == i)&& // down 3
						   (board[row + 2][col + 1] == 0 || // down 2 right 1 
							board[row + 2][col + 1] == i)&& // down 2 right 1 
						   (board[row + 2][col - 1] == 0 || // down 2 left 1 
							board[row + 2][col - 1] == i)&& // down 2 left 1 
						   (board[row + 2][col - 3] == 0 || // down 2 left 3 
							board[row + 2][col - 3] == i)){ // down 2 left 3 
						  
							preBigOpenLPositions.addUnique(new Move(row + 2, col - 2, player, type)); // down 2 left 2
						} // end if
					} // end if
				} // end if
			} // end if
		if(row == 3 || row == 4 || row == 5 || row == 6){// rows 3-6 column 0

			if(board[row - 2][col - 2] == 0){ // diagonal up 2 left 2	
			
				if(board[row][col - 2] == 0){ // left 2
				
					if((board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row + 1][col - 2] == 0 || // down 1 left 2
						board[row + 1][col - 2] == i)&& // down 1 left 2
					   (board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i)&& // up 1 left 2
					   (board[row - 3][col - 2] == 0 || // up 3 left 2
						board[row - 3][col - 2] == i)){ // up 3 left 2
					  
						preBigOpenLPositions.addUnique(new Move(row - 2, col - 2, player, type)); // up 2 left 2
					} // end if
				} // end if
				if(board[row - 2][col] == 0){ // up 2
					
					if((board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i)&& // up 3
					   (board[row - 2][col + 1] == 0 || // up 2 right 1 
						board[row - 2][col + 1] == i)&& // up 2 right 1 
					   (board[row - 2][col - 1] == 0 || // up 2 left 1 
						board[row - 2][col - 1] == i)&& // up 2 left 1 
					   (board[row - 2][col - 3] == 0 || // up 2 left 3 
						board[row - 2][col - 3] == i)){ // up 2 left 3 
						
						preBigOpenLPositions.addUnique(new Move(row - 2, col - 2, player, type)); // up 2 left 2
					} // end if
				} // end if
			} // end if
		} // end if

	return preBigOpenLPositions;
} // end method column5PreOpenL xx
	
	public static MoveSet preBigOpenL_Column_6(BoardNode node, Move move, MoveType type){
		MoveSet preBigOpenLPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(row == 1 || row == 2 || row == 3 || row == 4){ // column 6
			if(board[row + 2][col - 2] == 0){ // diagonal down 2 left 2
				if(board[row][col - 2] == 0) { // left 2
						
					if((board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i)&& // up 1 left 2
					   (board[row + 1][col - 2] == 0 || // down 1 left 2
						board[row + 1][col - 2] == i)&& // down 1 left 2
					   (board[row + 3][col - 1] == 0 || // down 3 left 1
						board[row + 3][col - 1] == i)){ // down 3 left 1
						
						 preBigOpenLPositions.addUnique(new Move(row + 2, col - 2, player, type)); // down 2 left 2
					} // end if
					
				} // end if	
				if(board[row + 2][col] == 0) { // down 2
					
					if((board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row + 3][col]     == 0 || // down 3
						board[row + 3][col]     == i)&& // down 3
					   (board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row + 2][col + 1] == 0 || // down 2 right 1 
						board[row + 2][col + 1] == i)&& // down 2 right 1 
					   (board[row + 2][col - 1] == 0 || // down 2 left 1 
						board[row + 2][col - 1] == i)&& // down 2 left 1 
					   (board[row + 1][col - 3] == 0 || // down 1 left 3 
						board[row + 1][col - 3] == i)){ // down 1 left 3 
						
						 preBigOpenLPositions.addUnique(new Move(row + 2, col - 2, player, type)); // left 2 down 2
					} // end if
					
				} // end if	
			} // end if
		} // end if
		if(row == 3 || row == 4 || row == 5 || row == 6){ // row 6 column 6
			if(board[row - 1][col - 1] == 0){} // end if
			if(board[row - 2][col - 2] == 0){ // diagonal up 2 left 2
				if(board[row][col - 2] == 0) { // left 2
						
					if((board[row][col - 1]     == 0 || // left 1
						board[row][col - 1]     == i)&& // left 1
					   (board[row][col - 3]     == 0 || // left 3
						board[row][col - 3]     == i)&& // left 3
					   (board[row][col + 1]     == 0 || // right 1
						board[row][col + 1]     == i)&& // right 1
					   (board[row + 1][col - 2] == 0 || // down 1 left 2
						board[row + 1][col - 2] == i)&& // down 1 left 2
					   (board[row - 1][col - 2] == 0 || // up 1 left 2
						board[row - 1][col - 2] == i)&& // up 1 left 2
					   (board[row - 3][col - 1] == 0 || // up 3 left 1
						board[row - 3][col - 1] == i)){ // up 3 left 1
						
						 preBigOpenLPositions.addUnique(new Move(row - 2, col - 2, player, type)); // up 2 left 2
					} // end if
					
				} // end if	
				if(board[row - 2][col] == 0) { // up 2
					
					if((board[row - 1][col]     == 0 || // up 1
						board[row - 1][col]     == i)&& // up 1
					   (board[row - 3][col]     == 0 || // up 3
						board[row - 3][col]     == i)&& // up 3
					   (board[row + 1][col]     == 0 || // down 1
						board[row + 1][col]     == i)&& // down 1
					   (board[row - 2][col + 1] == 0 || // up 2 right 1 
						board[row - 2][col + 1] == i)&& // up 2 right 1 
					   (board[row - 2][col - 1] == 0 || // up 2 left 1 
						board[row - 2][col - 1] == i)&& // up 2 left 1 
					   (board[row - 1][col - 3] == 0 || // up 1 left 3 
						board[row - 1][col - 3] == i)){ // up 1 left 3 
						
						 preBigOpenLPositions.addUnique(new Move(row - 2, col - 2, player, type)); // left 2 up 2
					} // end if
					
				} // end if	
			} // end if
		} // end if 

		return preBigOpenLPositions;

	} // end method
	
	private static MoveSet getThreeOfFourPositionsList(BoardNode node, boolean block){
		MoveSet threeOfFourPositions = new MoveSet();
		int value = THREE_OF_FOUR_VALUE;
		int player = 2/node.lastMove.player;
		MoveType vertical, horizontal;
		
		if(player == 1){ 
			value = -1 * value;
		}
		
		for(Move move : node.moves){
			int col = move.column;
			
			if(block){
				if(move.player == player){ // attack
					continue;
				} else { // block
					vertical = new MoveType(MoveType.Type.BLOCK_THREE_4_VERT, getMultiplierValue(value, BLOCK_MULT));
					horizontal = new MoveType(MoveType.Type.BLOCK_THREE_4_HORIZ, getMultiplierValue(value, BLOCK_MULT));
				} // end else 
			} else {
				if(move.player != player){ // block
					continue;
				} else { // attack
					vertical = new MoveType(MoveType.Type.THREE_4_VERT, value);
					horizontal = new MoveType(MoveType.Type.THREE_4_HORIZ, value);
				} // end else
			} // end else
			
			if(col == 0 || col == 1 || col == 2 || col == 3 || col == 4){
				threeOfFourPositions.addAll(threeOfFour_Columns_0_1_2_3_4(node, move, horizontal));
			}
			if(col == 1 || col == 2 || col == 3 || col == 4 || col == 5){
				threeOfFourPositions.addAll(threeOfFour_Columns_1_2_3_4_5(node, move, horizontal));
			} 
			if(col == 2 || col == 3 || col == 4 || col == 5 || col == 6){
				threeOfFourPositions.addAll(threeOfFour_Columns_2_3_4_5_6(node, move, horizontal));
			} // end if
			if(col == 3 || col == 4 || col == 5 || col == 6 || col == 7){
				threeOfFourPositions.addAll(threeOfFour_Columns_3_4_5_6_7(node, move, horizontal));
			} // end if
			
			threeOfFourPositions.addAll(threeOfFour_AllColumns(node, move, vertical));
		} // end for
//		threeOfFourPositions.print("threeOfFourPositions");
		return threeOfFourPositions;
	} // end method threeOfFour

	private static MoveSet threeOfFour_Columns_1_2_3_4_5(BoardNode node, Move move, MoveType type) {
		MoveSet threeOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(board[row][col - 1] == i){ // left 1
			
		    if(board[row][col + 1] == 0 && // right 1
		       board[row][col + 2] == 0){  // right 2
				   
			    threeOfFourPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				threeOfFourPositions.addUnique(new Move(row, col + 2, player, type)); // right 2
		    } // end if
		} else if(board[row][col - 1] == 0){ // left 1
			
		    if(board[row][col + 1] == i && // right 1
		       board[row][col + 2] == 0){  // right 2
				   
			    threeOfFourPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				threeOfFourPositions.addUnique(new Move(row, col + 2, player, type)); // right 2
		    } else if(board[row][col + 1] == 0 && // right 1
				       board[row][col + 2] == i){  // right 2
				   
			    threeOfFourPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				threeOfFourPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
		    } // end if
		} // end if
				
		return threeOfFourPositions;
	}

	private static MoveSet threeOfFour_Columns_2_3_4_5_6(BoardNode node, Move move, MoveType type) {
		MoveSet threeOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;

		if(board[row][col + 1] == i){ // right 1
			
			if(board[row][col - 1] == 0 && // left 1
			   board[row][col - 2] == 0){  // left 2
				   
				threeOfFourPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
				threeOfFourPositions.addUnique(new Move(row, col - 2, player, type)); // left 2
			} // end if
		} else if(board[row][col + 1] == 0){ // right 1
			
			if(board[row][col - 1] == i && // left 1
			   board[row][col - 2] == 0){  // left 2
				   
				threeOfFourPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				threeOfFourPositions.addUnique(new Move(row, col - 2, player, type)); // left 2
			} else if(board[row][col - 1] == 0 && // left 1
					   board[row][col - 2] == i){  // left 2
				   
				threeOfFourPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				threeOfFourPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
			} // end if
		} // end if
				
		return threeOfFourPositions;
	}

	private static MoveSet threeOfFour_Columns_3_4_5_6_7(BoardNode node, Move move, MoveType type) { // horizontal 3 of 4
		MoveSet threeOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(board[row][col - 1] == i){ 	   // left 1
			if(board[row][col - 2] == 0 && // left 2
			   board[row][col - 3] == 0){  // left 3
			
				threeOfFourPositions.addUnique(new Move(row, col - 2, player, type)); // left 2
				threeOfFourPositions.addUnique(new Move(row, col - 3, player, type)); // left 3
			} // end if
		} else if(board[row][col - 1] == 0){  // left 1
			
			   if(board[row][col - 2] == i && // left 2
				  board[row][col - 3] == 0){  // left 3
				   
					threeOfFourPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
					threeOfFourPositions.addUnique(new Move(row, col - 3, player, type)); // left 3
			   } else if(board[row][col - 2] == 0 && // left 2
						 board[row][col - 3] == i){  // left 3
					
							threeOfFourPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
							threeOfFourPositions.addUnique(new Move(row, col - 2, player, type)); // left 2
			   } // end if
		} // end else if

		return threeOfFourPositions;
	}

	private static MoveSet threeOfFour_Columns_0_1_2_3_4(BoardNode node, Move move, MoveType type) { // horizontal 3 of 4
		MoveSet threeOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
	
		if(board[row][col + 1] == i){ 	   // right 1
			if(board[row][col + 2] == 0 && // right 2
			   board[row][col + 3] == 0){  // right 3
			
				threeOfFourPositions.addUnique(new Move(row, col + 2, player, type)); // right 2
				threeOfFourPositions.addUnique(new Move(row, col + 3, player, type)); // right 3
			} // end if
		} else if(board[row][col + 1] == 0){  // right 1
			
			   if(board[row][col + 2] == i && // right 2
				  board[row][col + 3] == 0){  // right 3
				   
				    threeOfFourPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
					threeOfFourPositions.addUnique(new Move(row, col + 3, player, type)); // right 3
			   } else if(board[row][col + 2] == 0 && // right 2
						 board[row][col + 3] == i){  // right 3
					
				   			threeOfFourPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
							threeOfFourPositions.addUnique(new Move(row, col + 2, player, type)); // right 2
			   } // end if
		} // end else if

		return threeOfFourPositions;
	} // end method 
	
	private static MoveSet threeOfFour_AllColumns(BoardNode node, Move move, MoveType type) { // vertical 3 of 4
		MoveSet threeOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(row == 0 || row == 1 || row == 2 || row == 3 || row == 4){
			
			if(board[row + 1][col] == i){ 	   // down 1
				
				if(board[row + 2][col] == 0 && // down 2
				   board[row + 3][col] == 0){  // down 3
				
					threeOfFourPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
					threeOfFourPositions.addUnique(new Move(row + 3, col, player, type)); // down 3
				} // end if
			} else if(board[row + 1][col] == 0){  // down 1
				
				   if(board[row + 2][col] == i && // down 2
					  board[row + 3][col] == 0){  // down 3
				
					   threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					   threeOfFourPositions.addUnique(new Move(row + 3, col, player, type)); // down 3
				   } else if(board[row + 2][col] == 0 && // down 2
						     board[row + 3][col] == i){  // down 3
				
					   threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					   threeOfFourPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
				   } // end else if
			} // end else if
		} // end if
		
		if(row == 1 || row == 2 || row == 3 || row == 4){
			if(board[row - 1][col] == i){ // up 1
				if(board[row + 1][col] == 0 && // down 1
			       board[row + 2][col] == 0){  // down 2
				
			    	threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
			    	threeOfFourPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
				} // end if
			} else if(board[row - 1][col] == 0){ // up 1
				
				if(board[row + 1][col] == i && // down 1
			       board[row + 2][col] == 0){  // down 2
				
			    	threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
			    	threeOfFourPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
				} else if(board[row + 1][col] == 0 && // down 1
					      board[row + 2][col] == i){  // down 2
					
							threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					    	threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				} // end else if
			} // end else if
			
			if(board[row + 1][col] == i){	   // down 1
			    if(board[row + 2][col] == 0 && // down 2
			       board[row + 3][col] == 0){  // down 3
				
			    	threeOfFourPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
			    	threeOfFourPositions.addUnique(new Move(row + 3, col, player, type)); // down 3
			    } // end if
			} else if(board[row + 1][col] == 0){  // down 1
				
				   if(board[row + 2][col] == i && // down 2
					  board[row + 3][col] == 0){  // down 3
				
						threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
						threeOfFourPositions.addUnique(new Move(row + 3, col, player, type)); // down 3
				   } else if(board[row + 2][col] == 0 && // down 2
							 board[row + 3][col] == i){  // down 3
						
					    threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					    threeOfFourPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
				   } // end else if
			} // end else if
		} // end if row
		
		if(row == 2 || row == 3 || row == 4){
			
			if(board[row + 1][col] == i){ 	   // down 1
				
				if(board[row + 2][col] == 0 && // down 2
				   board[row + 3][col] == 0){  // down 3
						
						threeOfFourPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
						threeOfFourPositions.addUnique(new Move(row + 3, col, player, type)); // down 3
				} 
				if(board[row - 1][col] == 0){     // up 1 
					if(board[row - 2][col] == 0){ // up 2 
						
						threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
						threeOfFourPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
					} // end if
					if(board[row + 2][col] == 0){ // down 2 
						
						threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
						threeOfFourPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
					}
				} // end if
			} else if(board[row + 1][col] == 0){  // down 1
				
				   if(board[row + 2][col] == i && // down 2
					  board[row + 3][col] == 0){  // down 3
					
						threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
						threeOfFourPositions.addUnique(new Move(row + 3, col, player, type)); // down 3
				   } else if(board[row + 2][col] == 0 && // down 2
							 board[row + 3][col] == i){  // down 3
						
						threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
						threeOfFourPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
				   } // end else if
				   
				   if(board[row - 1][col] == i){  // up 1 
						if(board[row - 2][col] == 0){ // up 2 
							
							threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
							threeOfFourPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
						} // end if
						if(board[row + 2][col] == 0){ // down 2 
							
							threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
							threeOfFourPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
						}
					} else if(board[row - 1][col] == 0){  // up 1 
						if(board[row - 2][col] == i){ // up 2 
							
							threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
							threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
						} // end if
						if(board[row + 2][col] == i){ // down 2 
							
							threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
							threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
						}
					} // end if
			} // end else if
		} // end if
		
		if(row == 3 || row == 4 || row == 5){ // row 3-5
			
			if(board[row - 1][col] == i){ 	   // up 1
				
				if(board[row - 2][col] == 0 && // up 2
				   board[row - 3][col] == 0){  // up 3
						
						threeOfFourPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
						threeOfFourPositions.addUnique(new Move(row - 3, col, player, type)); // up 3
				} 
				if(board[row + 1][col] == 0){     // down 1 
					if(board[row + 2][col] == 0){ // down 2 
						
						threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
						threeOfFourPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
					} // end if
					if(board[row - 2][col] == 0){ // up 2 
						
						threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
						threeOfFourPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
					}
				} // end if
			} else if(board[row - 1][col] == 0){  // up 1
				
				   if(board[row - 2][col] == i && // up 2
					  board[row - 3][col] == 0){  // up 3
					
						threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
						threeOfFourPositions.addUnique(new Move(row - 3, col, player, type)); // up 3
				   } else if(board[row - 2][col] == 0 && // up 2
							 board[row - 3][col] == i){  // up 3
						
						threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
						threeOfFourPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
				   } // end else if
				   
				   if(board[row + 1][col] == i){  // down 1 
						if(board[row + 2][col] == 0){ // down 2 
							
							threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
							threeOfFourPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
						} // end if
						if(board[row - 2][col] == 0){ // up 2 
							
							threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
							threeOfFourPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
						}
					} else if(board[row + 1][col] == 0){  // down 1 
						if(board[row + 2][col] == i){ // down 2 
							
							threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
							threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
						} // end if
						if(board[row - 2][col] == i){ // up 2 
							
							threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
							threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
						}
					} // end if
			} // end else if
		} // end if
		
		if(row == 3 || row == 4 || row == 5 || row == 6){ // row 3-6
			
			if(board[row + 1][col] == i){ // down 1
				if(board[row - 1][col] == 0 && // up 1
				   board[row - 2][col] == 0){  // up 2
				
					threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					threeOfFourPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
				} // end if
			} else if(board[row + 1][col] == 0){ // down 1
				
				if(board[row - 1][col] == i && // up 1
				   board[row - 2][col] == 0){  // up 2
				
					threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					threeOfFourPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
				} else if(board[row - 1][col] == 0 && // up 1
						  board[row - 2][col] == i){  // up 2
			
					threeOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
					threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				} // end else if
			} // end else if
			
			if(board[row - 1][col] == i){	   // up 1
				if(board[row - 2][col] == 0 && // up 2
				   board[row - 3][col] == 0){  // up 3
				
					threeOfFourPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
					threeOfFourPositions.addUnique(new Move(row - 3, col, player, type)); // up 3
				} // end if
			} else if(board[row - 1][col] == 0){  // up 1
				
				   if(board[row - 2][col] == i && // up 2
					  board[row - 3][col] == 0){  // up 3
				
						threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
						threeOfFourPositions.addUnique(new Move(row - 3, col, player, type)); // up 3
				   } else if(board[row - 2][col] == 0 && // up 2
							 board[row - 3][col] == i){  // up 3
						
						threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
						threeOfFourPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
				   } // end else if
			} // end else if
		} // end if
		
		if(row == 3 || row == 4 || row == 5 || row == 6 || row == 7){ // rows 3-7 columns 0-4
			if(board[row - 1][col] == i){ 	   // up 1
				
				if(board[row - 2][col] == 0 && // up 2
				   board[row - 3][col] == 0){  // up 3
				
					threeOfFourPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
					threeOfFourPositions.addUnique(new Move(row - 3, col, player, type)); // up 3
				} // end if
			} else if(board[row - 1][col] == 0){  // up 1
				
				   if(board[row - 2][col] == i && // up 2
					  board[row - 3][col] == 0){  // up 3
				
					   threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					   threeOfFourPositions.addUnique(new Move(row - 3, col, player, type)); // up 3
				   } else if(board[row - 2][col] == 0 && // up 2
							 board[row - 3][col] == i){  // up 3
				
					   threeOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
					   threeOfFourPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
				   } // end else if
			} // end else if
		} // end if
		return threeOfFourPositions;
	}

	private static MoveSet getTwoOfFourPositionsList(BoardNode node, boolean block){ // two of four, with possible open 3 next move
		MoveSet twoOfFourPositions = new MoveSet();
		int value = TWO_OF_FOUR_VALUE;
		int player = 2/node.lastMove.player;
		MoveType vertical, horizontal;
		
		if(player == 1){ 
			value = -1*value;
		}
		
		for(Move move : node.moves){
			int col = move.column;
			
			if(block){ // block
				if(move.player == player){ // attack
					continue;
				} else { // block
					vertical = new MoveType(MoveType.Type.BLOCK_TWO_4_VERT, getMultiplierValue(value, BLOCK_MULT));
					horizontal = new MoveType(MoveType.Type.BLOCK_TWO_4_HORIZ, getMultiplierValue(value, BLOCK_MULT));
				} // end else 
			} else { // attack
				if(move.player != player){ // block
					continue;
				} else { // attack
					vertical = new MoveType(MoveType.Type.TWO_4_VERT, value);
					horizontal = new MoveType(MoveType.Type.TWO_4_HORIZ, value);
				} // end else
			} // end else
			
			if(col == 0 || col == 1 || col == 2 || col == 3 || col == 4){
				twoOfFourPositions.addAll(twoOfFour_Columns_0_1_2_3_4(node, move, horizontal));
			} else if(col == 1 || col == 2 || col == 3 || col == 4 || col == 5){
				twoOfFourPositions.addAll(twoOfFour_Columns_1_2_3_4_5(node, move, horizontal));
			} else if(col == 2 || col == 3 || col == 4 || col == 5 || col == 6){
				twoOfFourPositions.addAll(twoOfFour_Columns_2_3_4_5_6(node, move, horizontal));
			} // end if
			if(col == 3 || col == 4 || col == 5 || col == 6 || col == 7){
				twoOfFourPositions.addAll(twoOfFour_Columns_3_4_5_6_7(node, move, horizontal));
			} // end if
			twoOfFourPositions.addAll(twoOfFour_AllColumns(node, move, vertical));
		} // end for
//		twoOfFourPositions.print("twoOfFourPositions");
		return twoOfFourPositions;
	} // end method
	
	private static MoveSet twoOfFour_AllColumns(BoardNode node, Move move, MoveType type) { // vertical 2 of 4
		
		MoveSet twoOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = move.row;
		int col = move.column;
		
		if(row == 0 || row == 1 || row == 2 || row == 3 || row == 4){
			
			if(board[row + 1][col] == 0 && // down 1
			   board[row + 2][col] == 0 && // down 2
			   board[row + 3][col] == 0){  // down 3
				
				twoOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
				twoOfFourPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
				twoOfFourPositions.addUnique(new Move(row + 3, col, player, type)); // down 3
			} // end if
		} // end if
		
		if(row == 1 || row == 2 || row == 3 || row == 4 || row == 5){
			
			if(board[row - 1][col] == 0 && // up 1
			   board[row + 1][col] == 0 && // down 1
			   board[row + 2][col] == 0){  // down 2
				
				twoOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
		    	twoOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
		    	twoOfFourPositions.addUnique(new Move(row + 2, col, player, type)); // down 2
			} // end if
		} // end if
		
		if(row == 2 || row == 3 || row == 4 || row == 5 || row == 6){ // row 2-6
			if(board[row - 1][col] == 0 && // up 1
			   board[row - 2][col] == 0 && // up 2
			   board[row + 1][col] == 0){  // down 1
				
				twoOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				twoOfFourPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
				twoOfFourPositions.addUnique(new Move(row + 1, col, player, type)); // down 1
			} // end if
		} // end if
		
		if(row == 3 || row == 4 || row == 5 || row == 6 || row == 7){ // rows 3-7
			if(board[row - 1][col] == 0 && // up 1
			   board[row - 2][col] == 0 && // up 2
			   board[row - 3][col] == 0){  // up 3
				
				twoOfFourPositions.addUnique(new Move(row - 1, col, player, type)); // up 1
				twoOfFourPositions.addUnique(new Move(row - 2, col, player, type)); // up 2
				twoOfFourPositions.addUnique(new Move(row - 3, col, player, type)); // up 3
			} // end if
		} // end if
		
		return twoOfFourPositions;
	} // end method

	private static MoveSet twoOfFour_Columns_0_1_2_3_4(BoardNode node, Move move, MoveType type) {

		MoveSet twoOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = move.row;
		int col = move.column;
	
		if(board[row][col + 1] == 0 && // right 1
		   board[row][col + 2] == 0 && // right 2
		   board[row][col + 3] == 0){  // right 3
		    	
			twoOfFourPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
	    	twoOfFourPositions.addUnique(new Move(row, col + 2, player, type)); // right 2
	    	twoOfFourPositions.addUnique(new Move(row, col + 3, player, type)); // right 3
	    } 
		
		return twoOfFourPositions;	
	} // end method

	private static MoveSet twoOfFour_Columns_1_2_3_4_5(BoardNode node, Move move, MoveType type) {
		MoveSet twoOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = move.row;
		int col = move.column;
		
		if(board[row][col - 1] == 0 && // left 1
		   board[row][col + 1] == 0 && // right 1
		   board[row][col + 2] == 0){  // right 2
				   
			    twoOfFourPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
			    twoOfFourPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
				twoOfFourPositions.addUnique(new Move(row, col + 2, player, type)); // right 2
	    } // end if

		return twoOfFourPositions;
	} // end method 
		
	private static MoveSet twoOfFour_Columns_2_3_4_5_6(BoardNode node, Move move, MoveType type) {
		MoveSet twoOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = move.row;
		int col = move.column;

		if(board[row][col + 1] == 0 && // right 1
		   board[row][col - 1] == 0 && // left 1
		   board[row][col - 2] == 0){  // left 2
				   
			twoOfFourPositions.addUnique(new Move(row, col + 1, player, type)); // right 1
			twoOfFourPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
			twoOfFourPositions.addUnique(new Move(row, col - 2, player, type)); // left 2
		} // end if

		return twoOfFourPositions;
	} // end method
	
	private static MoveSet twoOfFour_Columns_3_4_5_6_7(BoardNode node, Move move, MoveType type) {
		MoveSet twoOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = move.row;
		int col = move.column;
		
		if(board[row][col - 1] == 0 && // left 1
		   board[row][col - 2] == 0 && // left 2
		   board[row][col - 3] == 0){  // left 3
				
			twoOfFourPositions.addUnique(new Move(row, col - 1, player, type)); // left 1
			twoOfFourPositions.addUnique(new Move(row, col - 2, player, type)); // left 2
			twoOfFourPositions.addUnique(new Move(row, col - 3, player, type)); // left 3
		} 

		return twoOfFourPositions;
	} // end method
	
	private static MoveSet getOneOfFourPositionsList(BoardNode node, MoveSet zeros){ // one move, with possible open 3 in 2 moves
		MoveSet oneOfFourPositions = new MoveSet();
		int player = 2/node.lastMove.player;
		int value = ONE_OF_FOUR_VALUE;
		
		if(player == 1){ 
			value = -1*value;
		}
		
		for(Move move : zeros){
			int col = move.column;
			MoveType type = new MoveType( MoveType.Type.ZEROS, value);
			
			if(col == 0 || col == 1 || col == 2 || col == 3 || col == 4){
				oneOfFourPositions.addAll(oneOfFour_Columns_0_1_2_3_4(node, move, type));
			} 
			if(col == 1 || col == 2 || col == 3 || col == 4 || col == 5){
				oneOfFourPositions.addAll(oneOfFour_Columns_1_2_3_4_5(node, move, type));
			} 
			if(col == 2 || col == 3 || col == 4 || col == 5 || col == 6){
				oneOfFourPositions.addAll(oneOfFour_Columns_2_3_4_5_6(node, move, type));
			} // end if
			if(col == 3 || col == 4 || col == 5 || col == 6 || col == 7){
				oneOfFourPositions.addAll(oneOfFour_Columns_3_4_5_6_7(node, move, type));
			} // end if
		} // end for
		return oneOfFourPositions;
	} // end method oneOfFour
	
	private static MoveSet oneOfFour_Columns_0_1_2_3_4(BoardNode node, Move openMove, MoveType type) {
		MoveSet oneOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = openMove.row;
		int col = openMove.column;
	
		if(board[row][col + 1] == 0 && // right 1
		   board[row][col + 2] == 0 && // right 2
		   board[row][col + 3] == 0){  // right 3
			oneOfFourPositions.addUnique(new Move(row, col, player, type));
		} // end if
		
		if(row == 0 || row == 1 || row == 2 || row == 3 || row == 4){
			if(board[row + 1][col] == 0 && // down 1
			   board[row + 2][col] == 0 && // down 2
			   board[row + 3][col] == 0){  // down 3
				
				oneOfFourPositions.addUnique(new Move(row, col, player, type));
			} // end if
		} if(row == 1 || row == 2 || row == 3 || row == 4){
			if(board[row + 1][col] == 0 && // down 1
			   board[row + 2][col] == 0 && // down 2
			  (board[row - 1][col] == 0 || // up 1
			   board[row + 3][col] == 0)){ // down 3
				
				oneOfFourPositions.addUnique(new Move(row, col, player, type));
			} // end if
		} // end if
		if(row == 2 || row == 3 || row == 4){
			if(board[row + 1][col] == 0){ // down 1
				if(board[row + 2][col] == 0){// down 2
					if(board[row - 1][col] == 0 || // up 1
					   board[row + 3][col] == 0){ // down 3
						
						oneOfFourPositions.addUnique(new Move(row, col, player, type));
					   } // end if
				} else if(board[row - 1][col] == 0 && // up 1 
						  board[row - 2][col] == 0){   // up 2 

					oneOfFourPositions.addUnique(new Move(row, col, player, type));
				} // end else if
			} // end if
		} // end if
		if(row == 3 || row == 4 || row == 5){ // row 3 or 4 column 0
			if(board[row - 1][col] == 0){ // up 1
				if(board[row - 2][col] == 0){// up 2
					if(board[row + 1][col] == 0 || // down 1
					   board[row - 3][col] == 0){ // up 3
						
						oneOfFourPositions.addUnique(new Move(row, col, player, type));
					   } // end if
				} else if(board[row + 1][col] == 0 && // down 1 
						  board[row + 2][col] == 0){   // down 2 

					oneOfFourPositions.addUnique(new Move(row, col, player, type));
				} // end else if
			} // end if
		} // end if
		if(row == 3 || row == 4 || row == 5 || row == 6){ 
			if(board[row - 1][col] == 0 && // up 1
			   board[row - 2][col] == 0 && // up 2
			  (board[row + 1][col] == 0 || // down 1
			   board[row - 3][col] == 0)){ // up 3
				
				oneOfFourPositions.addUnique(new Move(row, col, player, type));
			} // end if
		} // end if
		if(row == 3 || row == 4 || row == 5 || row == 6 || row == 7){ // row 3 or 4 column 0
			if(board[row - 1][col] == 0 && // up 1
			   board[row - 2][col] == 0 && // up 2
			   board[row - 3][col] == 0){  // up 3
				
				oneOfFourPositions.addUnique(new Move(row, col, player, type));
			} // end if
		} // end if

		return oneOfFourPositions;
	} // end method 
	
	private static MoveSet oneOfFour_Columns_1_2_3_4_5(BoardNode node, Move openMove, MoveType type) {
		MoveSet oneOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = openMove.row;
		int col = openMove.column;
	
		if(board[row][col - 1] == 0 && // left 1
		   board[row][col + 1] == 0 && // right 1
		   board[row][col + 2] == 0){  // right 2
		   
			oneOfFourPositions.addUnique(new Move(row, col, player, type));
		} // end if
		
		return oneOfFourPositions;
	} // end method 
	
	private static MoveSet oneOfFour_Columns_2_3_4_5_6(BoardNode node, Move openMove, MoveType type) {
		MoveSet oneOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = openMove.row;
		int col = openMove.column;
	
		if(board[row][col - 1] == 0 && // left 1
		   board[row][col - 2] == 0 && // left 2
		   board[row][col + 1] == 0){  // right 1
		   
			oneOfFourPositions.addUnique(new Move(row, col, player, type));
		} // end if
		
		return oneOfFourPositions;
	} // end method
	
	private static MoveSet oneOfFour_Columns_3_4_5_6_7(BoardNode node, Move openMove, MoveType type) {
		MoveSet oneOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = openMove.row;
		int col = openMove.column;
	
		if(board[row][col - 1] == 0 && // left 1
		   board[row][col - 2] == 0 && // left 2
		   board[row][col - 3] == 0){  // left 3
		   
			oneOfFourPositions.addUnique(new Move(row, col, player, type));
		} // end if
		
		return oneOfFourPositions;
	} // end method
	
	private static void printWinner(int player) {
		if(player == 2){
			System.out.println("\n  I win!");
		} else{
			System.out.println("\n  You win!");
		} // end else
	} // end method printWinner

	private static boolean gameOver(BoardNode root) {
		if(possibleConnect4(root, root.lastMove)){
			return true;
		} // end if
		return false;
	} // end method gameOver
	
	static int getRow(String next) {
		int row = 0;
		switch (next) {
			case "a" : row = 0;
				break;
			case "b" : row = 1;
				break;
			case "c" : row = 2;
				break;
			case "d" : row = 3;
				break;
			case "e" : row = 4;
				break;
			case "f" : row = 5;
				break;
			case "g" : row = 6;
				break;
			case "h" : row = 7;
				break;
		} // end switch
		return row;
	} // end method getRow
	
	static String getRow(int next) {
		String ans = "";
			switch (next) {
				case  0: ans =  "a";
					break;
				case  1: ans =  "b";
					break;
				case  2: ans =  "c";
					break;
				case  3: ans =  "d";
					break;
				case  4: ans =  "e";
					break;
				case  5: ans =  "f";
					break;
				case  6: ans =  "g";
					break;
				case  7: ans =  "h";
					break;
			} // end switch
			if( ans.equals("")){
				System.err.println("Error in method getRow: " + next );
				System.exit(0);
			} // end if
			return ans;
	} // end method getRow
	
} // end class Connect4
