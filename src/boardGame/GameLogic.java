/**
 * @author Jonathan Fetzer
 * This is an AI board game. The object of the game is to get four 
 * 'pieces' in a row, either vertically, or horizontally. This class
 * contains the methods which determine which moves to explore, and 
 * the Minimax class is used to determine which of those moves is chosen.
 */
package boardGame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GameLogic {

	private static Scanner in = new Scanner(System.in);
	static final int MAX_WIDTH = 6; 
	static final int MAX_WINS = 50000; // Computer wins with connect 4
	static final int MIN_WINS = -50000; // Human wins with connect 4
	static final int MAX_DEPTH = 9; // Max depth of search
	
	static final boolean ATTACK_MOVES = false;
	static final boolean BLOCK_MOVES = true;
	
	static boolean SHOW_BOARDS = false; // shows AI's search
	static boolean SHOW_MOVES = false; // shows the AI's best moves to explore
	static boolean SHOW_MOVES_MINIMAX = false; // shows the AI's best moves in minimax to explore
	
	static long TIME_LIMIT = 3; // search time in seconds
	
	static final int OPEN_3_VALUE = 30000;
	static final int OPEN_L_VALUE = 25000;
	static final int THREE_OF_FOUR_VALUE = 20000;
	static final int TWO_OF_FOUR_VALUE = 4000;
	static final int BLOCK_MOST_SPACE_VALUE = 10000;
	static final int ONE_OF_FOUR_VALUE = 500;
	static final double PRE_MULT = .6;			// reduce value of PRE and BLOCK moves by this multiplier,
	static final double BLOCK_MULT = .2; 		// e.g. a value of .2 will reduce block move values by 
												// 20% of their attack move value counterparts.
	/**
	 * This method takes a {@link BoardNode} and tests it to see whether the game has ended.
	 * @param root
	 */
	
	static void testGameOver(BoardNode root) {

		if(!gameOver(root)){
//			System.out.println("Not Over: " + root.lastMove.moveString);
//			root.printBoard();
//			System.out.println();
//			if(root.lastMove.player == 2){
//				System.out.println("\n  My current move is: " + getRow(root.lastMove.row) + (root.lastMove.column + 1));
//			}
		} else {
			if(possibleConnect4(root, root.lastMove)){
				
				Button.winner = root.lastMove.player;
				CrossFire.showVictory();
				
//				root.printBoard();
//				printWinner(root.lastMove.player);
//				System.exit(0);
			} // end if
			else {
				CrossFire.showVictory();
//				if(root.lastMove.player != 1){
//					root.printBoard();
//				}
//				System.out.println("-Draw!");
//				System.exit(0);
			} // end if
		} // end else
	} // end method testGameOver

	
	/**
	 * To load a file, select 'y' then 'l'. File 'board.txt' is loaded from the 'src' folder
	 * in Eclipse or from the 'boardGame' folder if compiling from the command line. The contents
	 * of that file should be a board game, which can be copied from the console output, and modified
	 * as desired.
	 * @return
	 */
	static synchronized int[][] load() {
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
			if(xCount > oCount){ // computer is first player
//				BoardNode.print(board, 2);
				CrossFire.firstPlayer = 2;
				for (int i = 0; i < board.length; i++) {
					for (int j = 0; j < board.length; j++) {
						if(board[i][j] == 1){
							board[i][j] = 2;
							ActionListener[] a = CrossFire.buttons[i * 8 + j].getActionListeners();
							a[0].actionPerformed(new ActionEvent(CrossFire.buttons[i * 8 + j], 2, "Computer"));
						} else if(board[i][j] == 2){
							board[i][j] = 1;
							ActionListener[] a = CrossFire.buttons[i * 8 + j].getActionListeners();
							a[0].actionPerformed(new ActionEvent(CrossFire.buttons[i * 8 + j], 1, "Human"));
						} // end else if
					} // end for j
				} // end for i
			} else {
//				BoardNode.print(board, 1); // human is first player
				CrossFire.firstPlayer = 1;
				for (int i = 0; i < board.length; i++) {
					for (int j = 0; j < board.length; j++) {
						if(board[i][j] == 1){
							ActionListener[] a = CrossFire.buttons[i * 8 + j].getActionListeners();
							a[0].actionPerformed(new ActionEvent(CrossFire.buttons[i * 8 + j], 1, "Human"));
						} else if(board[i][j] == 2){
							ActionListener[] a = CrossFire.buttons[i * 8 + j].getActionListeners();
							a[0].actionPerformed(new ActionEvent(CrossFire.buttons[i * 8 + j], 2, "Computer"));
						} // end else if
					} // end for j
				} // end for i
			}
		} catch (IOException e) {

			e.printStackTrace();

		} finally {
			CrossFire.root.board = board;
			
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

	/**
	 * Takes the {@code string} values: 'X', 'O', and '_'. 
	 * @param string
	 * @return  The digit representation used to store each {@link Move}.
	 */
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

	/**
	 * Saves current state of board to file "board.txt".
	 * @param node
	 */
	static void save(BoardNode node) {

		BufferedWriter bw = null;
		FileWriter fw = null;

		try {

			fw = new FileWriter("board.txt");
			bw = new BufferedWriter(fw);
			bw.write(BoardNode.boardToString(CrossFire.root.board));
			
		} catch (IOException e) {

			e.printStackTrace();

		} finally {
			
			try {

				bw.close();
				fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}
	} // end method save

	/**
	 * @param node
	 * @param block Determines whether the {@link Move moves} 
	 * returned are blocking or attacking moves.
	 * @return a {@link MoveSet set} of {@code Move moves} with the greatest values
	 * as determined by evaluating only the current state of the board.
	 * The number of {@code Move moves} returned is determined by the static constant MAX_WIDTH.
	 */
	static MoveSet getMostPromisingMoves(BoardNode node, boolean block) { 
		int player = 2/node.lastMove.player;

		MoveSet bestMoves = possibilities(node, block);
		if(player == 2){
			bestMoves.sort(new MaxMoveComparator());
		} else {
			bestMoves.sort(new MinMoveComparator());
		}
		
		// These moves are likely the most promising.
		// These moves values are determined via the minimax algorithm.
		// Each move is added as a child node and part of the beam search
		if(player == 2){ // computer
			bestMoves.reduceMax(MAX_WIDTH/2);
		} else {
			bestMoves.reduceMin(MAX_WIDTH/2);
		}
		return bestMoves;
	} // end getBestMove
	
	/**
	 * @param node The {@link BoardNode} being evaluated to determine the list of best {@link Move moves}.
	 * @param block Determines whether the {@code Move moves} are blocking or attacking moves.
	 * @return A list of the best moves as determined by evaluating only the current state of the board.
	 */
	public static MoveSet possibilities(BoardNode node, boolean block) {

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
	
	/**
	 * This method analyzes the root node.
	 * @param root
	 * @return The computer's chosen move as determined by the Minimax algorithm.
	 */
	public static Move chooseMove(BoardNode root) { // root.children are sorted into best order

		if(SHOW_BOARDS){
			root.printBoard();
			if(!SHOW_MOVES){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		MoveSet attacks = getMostPromisingMoves(root, false).reduceMax(MAX_WIDTH);
		MoveSet blocks = getMostPromisingMoves(root, true).reduceMax(MAX_WIDTH);

		if(!attacks.canWin()){
			if(blocks.canBlockWin()){
				return blocks.max();
			}
		} else {
			return attacks.max();
		}
		
		
		if(root.moves.size() >= 2){
			attacks.addAll(blocks);
		} else {
			return blocks.max();
		}
		
		Move best_block = blocks.max();
		
		if(SHOW_MOVES){
			attacks.print("Best Moves");
			System.out.println("Press enter to continue:");
			in.nextLine();
		}

		root.addAll(attacks);
		
		// Add a check to see if game will end in stalemate.
		
		int minWinDepth = 0;
		int maxWinDepth = 0;
		Move maxWinMove = null;
//		MoveSet losingMoves = new MoveSet();
		ExecutorService executor;
		
		if(SHOW_BOARDS || SHOW_MOVES || SHOW_MOVES_MINIMAX){
			executor = Executors.newFixedThreadPool(1);
		} else {
			executor = Executors.newFixedThreadPool(MAX_WIDTH);
		}
		
		for (int j = 0; j < root.size(); j++) { // each node is evaluated up to Max_Depth
			executor.submit(new Minimax(root, j));
		}
		
		try {
			executor.awaitTermination(TIME_LIMIT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int j = 0; j < root.size(); j++) {	
			if(root.getChild(j).value == MAX_WINS){ // attack successful
				if(root.getChild(j).maxWinDepth < maxWinDepth || maxWinDepth == 0){ // find win with least number of moves
					maxWinDepth = root.getChild(j).maxWinDepth;
					maxWinMove = root.getChild(j).lastMove;
				}
			} else if(root.getChild(j).value == MIN_WINS){ // attack unsuccessful
//				losingMoves.add(root.getChild(j).lastMove); // don't pick
				if(root.getChild(j).minWinDepth < minWinDepth || minWinDepth == 0){
					minWinDepth = root.getChild(j).minWinDepth; 
				} // end if
			} 
		} // end for j
		
//		root.printNodes();
		if(root.maxChildNodeValue() == MAX_WINS){ // attack successful
			root.removeAllChildNodes(); // REMOVE later to cache results
			return maxWinMove;
		}
		root.removeAllChildNodes(); // REMOVE later to cache results
		return best_block;
	} // end method evaluate
	
	/**
	 * @return A random start move for when the computer is first player.
	 */
	static Move getRandomStartMove() {
		int row = (int) Math.floor(Math.random() * 10);
		int col = (int) Math.floor(Math.random() * 10);
		if(row <= 1) {
			row = 1;
		} else if(row >= 6) {
			row = 6;
		} 
		if(col <= 1) {
			col = 1;
		} else if(col >= 6) {
			col = 6;
		} 
		return new Move(row,col,2, new MoveType(MoveType.Type.ONE_4, 30));
	}
	
	/**
	 * @param node
	 * @return Any available move.
	 */
	public static Move getAnyAvailableMove(BoardNode node) {
		MoveSet zeros = node.getZeros(); // return random available move
		return zeros.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(0, zeros.size()));
	}
	
	/**
	 * Determines whether the game can be won or lost with one more {@link Move move} by either player.
	 * @param node
	 * @param move
	 * @return True if game can be one in one {@code Move move}, False otherwise.
	 */
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
	
	/**
	 * Primarily used to start the game, returns the {@link MoveSet moves} right 
	 * next to the opponent's piece which block the most space. 
	 * @param node
	 * @return
	 */
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

	/**
	 * Helper method for blockMostSpace
	 * @param node
	 * @return The number of spaces to the right of {@code node.lastMove}.
	 */
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

	/**
	 * Helper method for blockMostSpace
	 * @param node
	 * @return The number of spaces to the left of {@code node.lastMove}.
	 */
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

	/**
	 * Helper method for blockMostSpace
	 * @param node
	 * @return The number of spaces to the top of {@code node.lastMove}.
	 */
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

	/**
	 * Helper method for blockMostSpace
	 * @param node
	 * @return The number of spaces to the bottom of {@code node.lastMove}.
	 */
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
	
	/**
	 * @param node
	 * @param block
	 * @return A {@link MoveSet set} of {@link Move moves} that can give a 4-in-a-row. If none, returns null.
	 */
	public static MoveSet getConnect4PositionsList(BoardNode node, boolean block) { 
		return getConnect4PositionsList(node, (2/node.lastMove.player), block);
	}
	
	/**
	 * @param node
	 * @param player
	 * @param block
	 * @return A {@link MoveSet set} of {@link Move moves} that can give a four-in-a-row. 
	 * If none, returns null.
	 */
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
	
	/**
	 * @param node
	 * @param block
	 * @return A {@link MoveSet set} of {@link Move moves} that can give an open-ended three-in-a-row. 
	 * If none, returns null.
	 */
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
						} 
	// add this correction throughout method as per symmetry.
						if(board[row + 2][col] == 0){ // down 2
							if(board[row - 2][col] == 0){ // up 2
								if(board[row - 1][col] == 0){ // up 1
									open3Positions.addUnique( new Move(row - 1, col, player, typePreOpen3Vert)); // up 1
									open3Positions.addUnique( new Move(row + 1, col, player, typePreOpen3Vert)); // down 1
								} else if(board[row - 1][col] == i){ // up 1
									
									open3Positions.addUnique( new Move(row + 1, col, player, typeOpen3Vert)); // down 1
								}
							}
						}
	///////
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
	
	/**
	 * Used to bias 'Pre' and 'Block' moves. 
	 * @param value
	 * @param mult1
	 * @param mult2
	 * @return A a {@code MoveType}'s value after applying the multipliers {@code mult1} and {@code mult2}.
	 */
	static int getMultiplierValue(int value, double mult1, double mult2) {
		
		return (int) (value - (value*mult1 + value*mult2));
	}

	/**
	 * Used to bias 'Pre' and 'Block' moves. 
	 * @param value
	 * @param mult1
	 * @param mult2
	 * @return A a {@code MoveType}'s value after applying the multiplier {@code mult1}.
	 */
	static int getMultiplierValue(int value, double mult1) {

		return (int) (value - (value*mult1));
	}

	/**
	 * @param node
	 * @param block
	 * @return A {@link MoveSet set} of {@link Move moves} that can give a small open-L. 
	 * If none, returns null.
	 */
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
	
	/**
	 * @param node
	 * @param block
	 * @return A {@link MoveSet set} of {@link Move moves} where small-open-L's can be created with one more move.
	 * If none, returns null.
	 */
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
	
	/**
	 * @param node
	 * @param block
	 * @return A {@link MoveSet set} of {@link Move moves} that can give a big open-L. 
	 * If none, returns null.
	 */
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
		} // end for
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
					    board[row][col - 1]     == i)&& // left 1
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
						board[row - 1][col]     == i)&& // up 1
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
					board[row][col - 1]     == i)&& // left 1
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
					board[row + 1][col]     == i)&& // down 1
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
					    board[row][col - 1]     == i)&& // left 1
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
						board[row - 1][col]     == i)&& // up 1
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
					board[row][col - 1]     == i)&& // left 1
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
					board[row + 1][col]     == i)&& // down 1
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
					    board[row][col + 1]     == i)&& // right 1
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
						board[row - 1][col]     == i)&& // up 1
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
						board[row][col + 1]     == i)&& // right 1
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
						board[row + 1][col]     == i)&& // down 1
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
						    board[row][col + 1]     == i)&& // right 1
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
							board[row - 1][col]     == i)&& // up 1
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
						board[row][col + 1]     == i)&& // right 1
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
						board[row + 1][col]     == i)&& // down 1
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
	
	/**
	 * @param node
	 * @param block
	 * @return A {@link MoveSet set} of {@link Move moves} where big-open-L's can be created with one more move.
	 * If none, returns null.
	 */
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
	
	/**
	 * @param node
	 * @param block
	 * @return A {@link MoveSet set} of {@link Move moves} where a three-of-four can be created with one more move.
	 * If none, returns null.
	 */
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
	/**
	 * @param node
	 * @param block
	 * @return A {@link MoveSet set} of {@link Move moves} where a two-of-four can be created with one more move.
	 * If none, returns null.
	 */
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
	
	/**
	 * @param node
	 * @param block
	 * @return A {@link MoveSet set} of {@link Move moves} where a one-of-four can be created with one more move.
	 * If none, returns null.
	 */
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
	
	@SuppressWarnings("unused")
	private static void printWinner(int player) {
		if(player == 2){
			System.out.println("\n  I win!");
		} else{
			System.out.println("\n  You win!");
		} // end else
	} // end method printWinner

	private static boolean gameOver(BoardNode root) {
		if(possibleConnect4(root, root.lastMove) || root.getNumEmptySpaces() == 0){
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
