package Connect4;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class Connect4 {

//	private static int plies = 1; // Max depth of search
	private static Scanner in = new Scanner(System.in);
	private static boolean test = false;
	
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
//		System.out.print("Load Board?: y/n: ");
//		String debug = in.nextLine();
//		
//		if(debug.toLowerCase().equals("y")){
//			test = true;
//		}
		Node node = null;
		if(test){

			try (ObjectInputStream ois
				= new ObjectInputStream(new FileInputStream("last_game.ser"))) {

				node = (Node) ois.readObject();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			node.updateStaticFields();
			node.printBoard();
		} 
		playGame(node);
		in.close();
	} // end method main
	
	private static void playGame(Node node) {
		Node root = node;
		int firstPlayer;
		if(test == false){
			System.out.print("Would you like to go first? (y/n): ");
			String playFirst = in.nextLine().toLowerCase();
			
			if(playFirst.equals("y")){
				firstPlayer = 1; // human
			} else {
				firstPlayer = 2; // computer
			} // end else
			if(firstPlayer == 2){ // computer makes first move
				root = new Node(getRandomStartMove());
				root.printBoard();					//lastMove.column +1 because board starts at column 1						
				System.out.println("\n  My current move is: " + getRow(root.lastMove.row) + (root.lastMove.column + 1));
				root.update(validate(root));
			} // end if
			if(firstPlayer == 1){	
				root = new Node(firstPlayer); 
				root.update(validate(firstPlayer, root)); 
			} 
		} else {
			root.update(validate(root));
		}
		while(true){
			root.update(computeAndMakeMove(root)); 
			testGameOver(root);
			root.removeAllChildren();
			root.update(validate(root));
			testGameOver(root);
			root.removeAllChildren();
			
		} // end while
	} // end method playGame

	private static void testGameOver(Node root) {

		if(!gameOver(root)){
			
			if(root.lastMove.player != 1){
				root.printBoard();
			}
			if(root.lastMove.player == 2){
				System.out.println("\n  My current move is: " + getRow(root.lastMove.row) + (root.lastMove.column + 1));
			}
		} // end if
	} // end method testGameOver

//	public static void printEmptyBoard(){
//		String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H"};
//		int[] cols = {1,2,3,4,5,6,7,8};
//		System.out.print("\n  ");
//		for (int i = 0; i < cols.length; i++) {
//			System.out.print(cols[i] + " ");
//		} // end for i
//		System.out.println();
//		for (int i = 0; i < 8; i++) {
//			System.out.print(rows[i] + " ");
//			for (int j = 0; j < 8; j++) {
//				System.out.print("_ ");
//			} // end for i
//			System.out.println();
//		} // end for j
//	} // end method printEmptyBoard
	
	private static Move validate(int player, Node node) { // used for human first move
		int row = 0, column = 0;
		String moveString;
		while(true) {

			node.printBoard();
			System.out.print("\n  Choose your next move: ");
			moveString = in.nextLine();
			
			if(moveString.toLowerCase().equals("load")){
				node.board = load();
				node.updateStaticFields();
				node.printBoard();
				System.out.print("\n  Choose your next move: ");
				moveString = in.nextLine();
			} // end else if
			
			row = getRow(String.valueOf(moveString.charAt(0)).toLowerCase());
			column = Integer.valueOf(String.valueOf(moveString.charAt(1))) - 1;
			if(row > 7 || column > 7){
				System.out.println("  Invalid entry, try again.");
			} else {
				return new Move(row, column, player, null); // new MoveType(MoveType.Type.ONE_4, 30)
			} // end else
		} // end while
	} // end method validate
	
	private static Move validate(Node node) {
		int row = 0, column = 0;
		int player = 2 / node.lastMove.player;
		String moveString;
		if(node.isStalemate()){
			System.out.println("Stalemate!");
			System.exit(0);
		} // end if
		while(true) {
			System.out.print("\n  Choose your next move: ");
			moveString = in.nextLine();
			if(moveString.toLowerCase().equals("save")){
				save(node); 	// Used in testing. Saves the game at the position 
				System.exit(0); // the board was in before the last piece was played.
			} else if(moveString.toLowerCase().equals("load")){
				node.board = load();
				node.updateStaticFields();
				node.printBoard();
				System.out.print("\n  Choose your next move: ");
				moveString = in.nextLine();
			} // end else if
			
			row = getRow(String.valueOf(moveString.charAt(0)).toLowerCase());
			column = Integer.valueOf(String.valueOf(moveString.charAt(1))) - 1;
			if(row > 7 || column > 7){
				System.out.println("  Invalid entry, try again.");
			} else {
				if(node.map.containsValue(moveString)) {
					System.out.println("  ~ " + getRow(row) + (column + 1)+ " is already taken.");
					//System.out.println(node.map.values());
				} else {
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
			for (int i = 0; i < board.length - 1; i++) {
				try {
					line = br.readLine().split(" ");
				} catch (IOException e) {
					e.printStackTrace();
				}
				for (int j = 1; j < board.length; j++) {
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

	private static void save(Node node) {
		node.priorState();
		try (ObjectOutputStream oos =
				new ObjectOutputStream(new FileOutputStream("last_game.ser"))) {

			oos.writeObject(node);
			System.out.println("First Player: " + Node.firstPlayer);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // end method save

	private static Move computeAndMakeMove(Node node) {
		//System.out.println("Node num Children: " + node.children.size());
		MoveSet bestMoves = possibilities(node);
			for(Move best: bestMoves){	
//				System.out.println(best.moveString + ": " + best.list.toString() + "~ " + best.value);
				node.addNode(best);	// each move added is a child node and part of the beam search
			} // end for					
		return evaluate(node); // perform a stochastic beam search implementing minimax with alpha-beta pruning
	} // end getBestMove
	
	public static MoveSet possibilities(Node node) {
		// Use a threadpool here
		MoveSet best = new MoveSet();
		MoveSet connect4Positions = getConnect4PositionsList(node); // saves all possible 4-in-a-row moves to global list connect4Positions
		MoveSet open3Positions = getOpen3PositionsList(node);
		MoveSet smallOpenLPositions = getSmallOpenLPositionsList(node);
		MoveSet bigOpenLPositions = getBigOpenLPositionsList(node);
		MoveSet preSmallOpenLPositions = getPreSmallOpenLPositionsList(node);
		MoveSet preBigOpenLPositions = getPreBigOpenLPositionsList(node);
		MoveSet threeOfFourPositions = getThreeOfFourPositionsList(node);
		MoveSet twoOfFourPositions = getTwoOfFourPositionsList(node);
		
		if(connect4Positions.size() > 0) {
//			System.out.println("CONNECT 4: ");
//			for(Move move : connect4Positions){
//				System.out.println(move.moveString);
//			}
			return connect4Positions;
//			return removeDups(connect4Positions);
		} // end if
		if(open3Positions.size() > 0){
			//System.out.println("Open 3");
			best.addAll(open3Positions);
		} // end if
		if(smallOpenLPositions.size() > 0){
			//System.out.println("Small Open L");
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
		if(best.size() < 1){
			best.addAll(getOneOfFourPositionsList(node, Node.getZeros(node)));
		}
		if(node.getNumEmptySpaces() != 0){
			best.addAll(blockMostSpace(node));
		} // end if
		return best;
	} // end method possibilities
	
	public static Move evaluate(Node root) {
		if(root.getNumEmptySpaces() == 0){ // Change to check to see if game will end in stalemate.
			if(possibleConnect4(root.board, root.lastMove)){
				printWinner(root.lastMove.player);
				System.exit(0);
			} else {
				System.out.println("Draw!");
				System.exit(0);
			} // end else
		} // end if

		if(root.children.size() > 0){ // correct this
			Move best = root.children.get(0).lastMove;
			for (int i = 1; i < root.children.size(); i++) {
				if(root.board[best.row][best.column] != 0){
					best = root.children.get(i).lastMove;
				}
				Move move = root.children.get(i).lastMove;
				if(move.value > best.value){
					best = move;
	//				best.value = move.value;
					//System.out.println("Best Loop: " + getRow(best.row) + (best.column + 1) + ": " + best.value);
				} // end if
			} // end for if
			//System.out.println("Return Best: " + getRow(best.row) + (best.column + 1) + ": " + best.value);
			if(root.board[best.row][best.column] != 0){
				System.out.println("Evaluation error");
				root.printBoard();
				System.exit(0);
			} // end if
//			System.out.println(best.list.toString());
			return best;
		} else {
			return getAnyAvailableMove(root);
		}
	} // end method evaluate
	
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
		Move start = new Move(3,4,2, new MoveType(MoveType.Type.ONE_4, 30));
//		Move start = new Move(row,col,1,2);
			return start;
	}
	
	public static Move getAnyAvailableMove(Node node) {
		MoveSet zeros = Node.getZeros(node); // return random available move
		return zeros.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(0, zeros.size()));
	}
	
	public static boolean possibleConnect4(int[][] board, Move move) {
		
		int row = move.row;
		int col = move.column;
		int player = move.player;
		boolean isConnect4 = false;
		//System.out.print(move.moveString);
			if(player != 0){
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
			} else{
				//System.out.println("ERROR: PLAYER = 0 IN possibleConnect4 METHOD");
			}
			//System.out.print(": isConnct4: " + isConnect4 + ", ");
			return isConnect4;
		} // end method connect4
	
	public static MoveSet blockMostSpace(Node node) { // rewrite
		MoveSet list = new MoveSet();
		int top, left, right, bottom;
		int player = 2 / node.lastMove.player;
		int value = 150;
		top = getTop(node);
		bottom = getBottom(node);
		left = getLeft(node);
		right = getRight(node);
		Move next = getAnyAvailableMove(node);
		String block = "";
		if(top == 0 && bottom == 0 && right == 0 && left == 0){
			list.add(next);
			return list;
		}
		if(top >= bottom){
			if(left >= right){
				if(top >= left){
					block += "top";
				} else if(left >= top){
					block += "left";
				} 
			} else { // right > left
				if(top >= right){
					block += "top";
				} else if(right >= top) {
					block += "right";
				} // end else if
			} // end else
		} else if( bottom >= top){
			if(left >= right){
				if(bottom >= left){
					block += "bottom";
				} else if(left >= bottom){
					block += "left";
				}
			} else if(right >= left){
				if(bottom >= right){
					block += "bottom";
				} else if(right >= bottom){
					block += "right";
				}
			} // end else
		} 
		if(block.contains("left")){
			list.add(new Move(node.lastMove.row, node.lastMove.column - 1, player, new MoveType(MoveType.Type.BLOCK_MOST_SPACE, value)));
		} 
		if(block.contains("right")){
			list.add(new Move(node.lastMove.row, node.lastMove.column + 1, player, new MoveType(MoveType.Type.BLOCK_MOST_SPACE, value)));
		} 
		if(block.contains("top")){
			list.add(new Move(node.lastMove.row - 1, node.lastMove.column, player, new MoveType(MoveType.Type.BLOCK_MOST_SPACE, value)));
		} 
		if(block.contains("bottom")){
			list.add(new Move(node.lastMove.row + 1, node.lastMove.column, player, new MoveType(MoveType.Type.BLOCK_MOST_SPACE, value)));
		} // end else
		return list;
	} // end method blockMostSpace

	public static int getRight(Node node) {
		int i = 1;
		if((node.lastMove.column + 1) >= node.board.length) return 0;
		for (i = 1 ; i <= (node.board.length - 1 - node.lastMove.column); i++) {
			if(node.board[node.lastMove.row][node.lastMove.column + i] != 0){
				break;
			} // end if
		} // end for i
		return --i;
	} // end method getRight

	public static int getLeft(Node node) {
		int i = 1;
		if((node.lastMove.column - 1) < 0) return 0;
		for (i = 1 ; i <= node.lastMove.column; i++) {
			if(node.board[node.lastMove.row][node.lastMove.column - i] != 0){
				break;
			} // end if
		} // end for i
		return --i;
	} // end method getLeft

	public static int getTop(Node node) {
		int i = 1;
		if((node.lastMove.row - 1) < 0) return 0;
		for (i = 1 ; i <= node.lastMove.row; i++) {
			if(node.board[node.lastMove.row - i][node.lastMove.column] != 0){
				break;
			} // end if
		} // end for i
		return --i;
	} // end method getTop

	public static int getBottom(Node node) {
		int i = 1;
		if((node.lastMove.row + 1) >= node.board.length) return 0;
		for (i = 1 ; i <= node.board.length - 1 - node.lastMove.row; i++) {
			if(node.board[node.lastMove.row + i][node.lastMove.column] != 0){
				break;
			} // end if
		} // end for i
		return --i;
	} // end method getBottom
	
	public static MoveSet getConnect4PositionsList(Node node) { // check if one more move can give a 4 in a row.
		MoveSet connect4Positions = new MoveSet();
		int value;
		int player = 2/node.lastMove.player;
		if(node.getNumMoves() < 5) return connect4Positions;
		MoveType type;
		for(Move move: Node.moves){
			int row = move.row;
			int col = move.column;
			// tests the positions above, below, right, and left of move.
			if(player == move.player){ // human made last move, i.e. this is a block
				value = 1300;  // computer can block a connect 4
			} else {
				value = 1200; // computer can make a connect 4
			} // end else

			type = new MoveType(MoveType.Type.CONNECT_4, value);
			
			if(row - 1 >= 0){ // prevents null pointer exceptions
				if(node.board[row - 1][col] == 0){
					node.board[row - 1][col] = move.player; // test move
					//System.out.println(1);
					if(possibleConnect4(node.board, move)){
						connect4Positions.add(new Move(row - 1, col, player, type));
						//System.out.print("conn4: " + getRow(row - 1) + (col + 1) + ", value: " + value + ", player: " + player);
					} // end if
					node.board[row - 1][col] = 0; // undo test move
				} // end if
			} // end if
			if(row + 1 < node.board.length){
				if(node.board[row + 1][col] == 0){
					node.board[row + 1][col] = move.player; // test move
					//System.out.println(2);
					if(possibleConnect4(node.board, move)){
						connect4Positions.add(new Move(row + 1, col, player, type));
						//System.out.print("conn4: " + getRow(row + 1) + (col + 1) + ", value: " + value + " ");
					} // end if
					node.board[row + 1][col] = 0; // undo test move
				} // end if
			} // end if
			if(col - 1 >= 0) {
				if(node.board[row][col - 1] == 0){
					node.board[row][col - 1] = move.player; // test move
					//System.out.println(3);
					if(possibleConnect4(node.board, move)){
						connect4Positions.add(new Move(row, col - 1, player, type));
						//System.out.print("conn4: " + getRow(row) + (col) + ", value: " + value + " ");
					} // end if
					node.board[row][col - 1] = 0; // undo test move
				} // end if
			} 
			if(col + 1 < node.board.length){
				if(node.board[row][col + 1] == 0){
					node.board[row][col + 1] = move.player; // test move
					//System.out.println(4);
					if(possibleConnect4(node.board, move)){
						connect4Positions.add(new Move(row, col + 1, player, type));
						//System.out.print("conn4: " + getRow(row) + (col + 2) + ", value: " + value + " ");
					} // end else if
					node.board[row][col + 1] = 0; // undo test move
				}// end if
			} // end else if
		} // end for 
		return connect4Positions;
	} // end method connect4

	private static MoveSet getOpen3PositionsList(Node node) {
		MoveSet open3Positions = new MoveSet();
		int[][] board = node.board;
		int value;
		int player = 2/node.lastMove.player;
		MoveType type;
		if(node.getNumMoves() < 3) return open3Positions;

		for(Move move : Node.moves){
			int row = move.row;
			int col = move.column;
			int i = move.player;
			
			if(i == player){ // human made last move, i.e. this is a block
				value = 590;  // computer can block an open 3
			} else {
				value = 585; // computer can make an open 3
			} // end else
			
			type = new MoveType(MoveType.Type.OPEN_3, value);
			
			if(col == 0){ // column 0
				if(row == 1){ // row 1 column 0
					
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0 && // down 1
							   board[row + 2][col] == i){ // down 2
								
								open3Positions.add( new Move(row + 1, col, player, type)); // down 1
								//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								
								open3Positions.add( new Move(row + 2, col, player, type)); // down 2
								//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
							} // end else if
						} // end if
					} // end if
				} else if(row == 2){ // row 2 column 0
					
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0 && // down 1
							   board[row + 2][col] == i){ // down 2
								
								open3Positions.add( new Move(row + 1, col, player, type)); // down 1
								//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								
								open3Positions.add( new Move(row + 2, col, player, type)); // down 2
								//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
							} // else if
						} // end if
					} else if(board[row - 1][col] == i){ // up 1
						if((board[row - 2][col] == 0)){ // up 2
							if(board[row + 1][col] == 0){ // down 1
								if((board[row + 2][col] == 0)){ // down 2
									open3Positions.add( new Move(row + 1, col, player, type)); // down 1
									//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
								} // end if
							} // end if
						} // end if
					} // end else if
				} else if(row == 3 || row == 4){ // row 3 or 4 column 0
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0 && // down 1
							   board[row + 2][col] == i){ // down 2
								open3Positions.add( new Move(row + 1, col, player, type)); // down 1
								//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								open3Positions.add( new Move(row + 2, col, player, type)); // down 2
								//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
							}
						} // end if
					} else if(board[row - 1][col] == i){ // up 1
						if((board[row - 2][col] == 0)){ // up 2
							if(board[row + 1][col] == 0){ // down 1
								if((board[row + 2][col] == 0)){ // down 2
									open3Positions.add( new Move(row + 1, col, player, type)); // down 1
									//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
								} // end if
							} // end if
						} // end if
					} if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0 && // up 1
							   board[row - 2][col] == i){ // up 2
								open3Positions.add( new Move(row - 1, col, player, type)); // up 1
								//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								open3Positions.add( new Move(row - 2, col, player, type)); // up 2
								//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
							} // else if
						} // end if
					} else if(board[row + 1][col] == i){ // down 1
						if((board[row + 2][col] == 0)){ // down 2
							if(board[row - 1][col] == 0){ // up 1
								if((board[row - 2][col] == 0)){ // up 2
									open3Positions.add( new Move(row - 1, col, player, type)); // up 1
									//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
								} // end if
							} // end if
						} // end if
					} // end else if
				} else if(row == 5){ // row 5 column 0
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0 && // up 1
							   board[row - 2][col] == i){ // up 2
								open3Positions.add( new Move(row - 1, col, player, type)); // up 1
								//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								open3Positions.add( new Move(row - 2, col, player, type)); // up 2
								//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
							} // end else if
						} // end if
					} else if(board[row + 1][col] == i){ // down 1
						if((board[row + 2][col] == 0)){ // down 2
							if(board[row - 1][col] == 0){ // up 1
								if((board[row - 2][col] == 0)){ // up 2
									open3Positions.add( new Move(row - 1, col, player, type)); // up 1
									//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
								} // end if
							} // end if
						} // end if
					} // end else if
				} else if(row == 6){ // row 6 column 0
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0 && // up 1
							   board[row - 2][col] == i){ // up 2
								open3Positions.add( new Move(row - 1, col, player, type)); // up 1
								//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								open3Positions.add( new Move(row - 2, col, player, type)); // up 2
								//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
							} // end else if
						} // end if
					} // end if
				} // end else if
			} else if(col == 1){ // column 1
				if(row == 0 || row == 7){ // row 0 or 7 column 1
					if(board[row][col - 1] == 0){ // left 1
						if(board[row][col + 1] == 0){ // right 1
							if(board[row][col + 2] == i){ // right 2
								if(board[row][col + 3] == 0){ // right 3
									open3Positions.add( new Move(row, col + 1, player, type)); // right 1
									//System.out.print("PosOpen3: " + getRow(row) + (col + 2) + ": " + value + ", "); // right 1
								} // end if
							} // end if
						} else if(board[row][col + 1] == i){ // right 1
							if(board[row][col + 2] == 0){ // right 2
								if(board[row][col + 3] == 0){ // right 3
									open3Positions.add( new Move(row, col + 2, player, type)); // right 2
									//System.out.print("PosOpen3: " + getRow(row) + (col + 3) + ": " + value + ", "); // right 2
								} // end if
							} // end if
						} // end else if
					} // end if
				} else if(row == 1){ // row 1 column 1
					if(board[row][col - 1] == 0){ // left 1
						if(board[row][col + 1] == 0){ // right 1
							if(board[row][col + 2] == i){ // right 2
								if(board[row][col + 3] == 0){ // right 3
									open3Positions.add( new Move(row, col + 1, player, type)); // right 1
									//System.out.print("PosOpen3: " + getRow(row) + (col + 2) + ": " + value + ", "); // right 1
								} // end if
							} // end if
						} else if(board[row][col + 1] == i){ // right 1
							if(board[row][col + 2] == 0){ // right 2
								if(board[row][col + 3] == 0){ // right 3
									open3Positions.add( new Move(row, col + 2, player, type)); // right 2
									//System.out.print("PosOpen3: " + getRow(row) + (col + 3) + ": " + value + ", "); // right 2
								} // end if
							} // end if
						} // end else if
					} // end if
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 1][col] == i){ // down 1
							if(board[row + 2][col] == 0){ // down 2
								if(board[row + 3][col] == 0){ // down 3
									open3Positions.add(new Move(row + 2, col, player, type)); // down 2
									//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
								} // end if
							} // end if
						} else if(board[row + 1][col] == 0){ // down 1
							if(board[row + 2][col] == i){ // down 2
								if(board[row + 3][col] == 0){ // down 3
									open3Positions.add(new Move(row + 1, col, player, type)); // down 1
									//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
								} // end if
							} // end if
						} // end if
					} // end if
				} else if(row == 2){ // row 2 column 1
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0 && // down 1
							   board[row + 2][col] == i){ // down 2
								open3Positions.add( new Move(row + 1, col, player, type)); // down 1
								//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								open3Positions.add( new Move(row + 2, col, player, type)); // down 2
								//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
							} // end else if
						} // end if
					} else if(board[row - 1][col] == i && // up 1
							  board[row - 2][col] == 0 ){ // up 2
						if(board[row + 1][col] == 0 && // down 1
						   board[row + 2][col] == 0){ // down 2
							open3Positions.add( new Move(row + 1, col, player, type)); // down 1
							//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
						} // end if
					} if(board[row][col - 1] == 0){ // left 1
						if(board[row][col + 3] == 0){ // right 3
							if(board[row][col + 1] == 0 && // right 1
							   board[row][col + 2] == i){ // right 2
								open3Positions.add( new Move(row, col + 1, player, type)); // right 1
								//System.out.print("PosOpen3: " + getRow(row) + (col + 2) + ": " + value + ", "); // right 1
							} else if(board[row][col + 1] == i && // right 1
									  board[row][col + 2] == 0){ // right 2
								open3Positions.add( new Move(row, col + 2, player, type)); // right 2
								//System.out.print("PosOpen3: " + getRow(row) + (col + 3) + ": " + value + ", "); // right 2
							} // end else if
						} // end if
					} // end if
				} else if(row == 3 || row == 4){ // rows 3 or 4 column 1
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0 && // down 1
							   board[row + 2][col] == i){ // down 2
								open3Positions.add( new Move(row + 1, col, player, type)); // down 1
								//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								open3Positions.add( new Move(row + 2, col, player, type)); // down 2
								//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
							} // end else if
						} // end if
					} else if(board[row - 1][col] == i && // up 1
								  board[row - 2][col] == 0 ){ // up 2
						if(board[row + 1][col] == 0 && // down 1
						   board[row + 2][col] == 0){ // down 2
							open3Positions.add( new Move(row + 1, col, player, type)); // down 1
							//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
						} // end if
					} if(board[row][col - 1] == 0){ // left 1
						if(board[row][col + 3] == 0){ // right 3
							if(board[row][col + 1] == 0 && // right 1
							   board[row][col + 2] == i){ // right 2
								open3Positions.add( new Move(row, col + 1, player, type)); // right 1
								//System.out.print("PosOpen3: " + getRow(row) + (col + 2) + ": " + value + ", "); // right 1
							} else if(board[row][col + 1] == i && // right 1
									  board[row][col + 2] == 0){ // right 2
								open3Positions.add( new Move(row, col + 2, player, type)); // right 2
								//System.out.print("PosOpen3: " + getRow(row) + (col + 3) + ": " + value + ", "); // right 2
							} // end else if
						} // end if
					} // end if
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0 && // up 1
							   board[row - 2][col] == i){ // up 2
								open3Positions.add( new Move(row - 1, col, player, type)); // up 1
								//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								open3Positions.add( new Move(row - 2, col, player, type)); // up 2
								//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
							} // end else if
						} // end if
					} else if(board[row + 1][col] == i && // down 1
							  board[row + 2][col] == 0 ){ // down 2
						if(board[row - 1][col] == 0 && // up 1
						   board[row - 2][col] == 0){ // up 2
							open3Positions.add( new Move(row - 1, col, player, type)); // up 1
							//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
						} // end if
					} // end else if
				} else if(row == 5){ // row 5 column 1
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0 && // up 1
							   board[row - 2][col] == i){ // up 2
								open3Positions.add( new Move(row - 1, col, player, type)); // up 1
								//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								open3Positions.add( new Move(row - 2, col, player, type)); // up 2
								//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
							} // end else if
						} // end if
					} else if(board[row + 1][col] == i && // down 1
							  board[row + 2][col] == 0 ){ // down 2
						if(board[row - 1][col] == 0 && // up 1
						   board[row - 2][col] == 0){ // up 2
							open3Positions.add( new Move(row - 1, col, player, type)); // up 1
							//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
						} // end if
					} if(board[row][col - 1] == 0){ // left 1
						if(board[row][col + 3] == 0){ // right 3
							if(board[row][col + 1] == 0 && // right 1
							   board[row][col + 2] == i){ // right 2
								open3Positions.add( new Move(row, col + 1, player, type)); // right 1
								//System.out.print("PosOpen3: " + getRow(row) + (col + 2) + ": " + value + ", "); // right 1
							} else if(board[row][col + 1] == i && // right 1
									  board[row][col + 2] == 0){ // right 2
								open3Positions.add( new Move(row, col + 2, player, type)); // right 2
								//System.out.print("PosOpen3: " + getRow(row) + (col + 3) + ": " + value + ", "); // right 2
							} // end else if
						} // end if
					} // end if
				} if(row == 6){ // row 6 column 1
					if(board[row][col - 1] == 0){ // left 1
						if(board[row][col + 1] == 0){ // right 1
							if(board[row][col + 2] == i){ // right 2
								if(board[row][col + 3] == 0){ // right 3
									open3Positions.add( new Move(row, col + 1, player, type)); // right 1
									//System.out.print("PosOpen3: " + getRow(row) + (col + 2) + ": " + value + ", "); // right 1
								} // end if
							} // end if
						} else if(board[row][col + 1] == i){ // right 1
							if(board[row][col + 2] == 0){ // right 2
								if(board[row][col + 3] == 0){ // right 3
									open3Positions.add( new Move(row, col + 2, player, type)); // right 2
									//System.out.print("PosOpen3: " + getRow(row) + (col + 3) + ": " + value + ", "); // right 2
								} // end if
							} // end if
						} // end else if
					} // end if
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 1][col] == i){ // up 1
							if(board[row - 2][col] == 0){ // up 2
								if(board[row - 3][col] == 0){ // up 3
									open3Positions.add(new Move(row - 2, col, player, type)); // up 2
									//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
								} // end if
							} // end if
						} else if(board[row - 1][col] == 0){ // up 1
							if(board[row - 2][col] == i){ // up 2
								if(board[row - 3][col] == 0){ // up 3
									open3Positions.add(new Move(row - 1, col, player, type)); // up 1
									//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
								} // end if
							} // end if
						} // end if
					} // end if
			    } // end if(row == 6)
			} else if(col == 2){ // column 2
				if(board[row][col - 1] == 0){ // left 1
					if(board[row][col + 1] == 0){ // right 1
						
						if(board[row][col + 2] == i){ // right 2
							if(board[row][col + 3] == 0){ // right 3
								
								open3Positions.add( new Move(row, col + 1, player, type)); // right 1
								//System.out.print("PosOpen3: " + getRow(row) + (col + 2) + ": " + value + ", "); // right 1
							} // end if
						} // end if
					} else if(board[row][col + 1] == i){ // right 1
						if(board[row][col + 2] == 0){ // right 2								
							if(board[row][col - 2] == 0){ // left 2
									
									open3Positions.add( new Move(row, col - 1, player, type)); // left 1
									//System.out.print("PosOpen3: " + getRow(row) + (col) + ": " + value + ", "); // left 1
								} // end if
							} // end if
						}  // end else if
				} else if(board[row][col - 1] == i){ // left 1
					if(board[row][col - 2] == 0){ // left 2
						if(board[row][col + 1] == 0){ // right 1
							if(board[row][col + 2] == 0){ // right 2
									
								open3Positions.add( new Move(row, col + 1, player, type)); // right 1
								//System.out.print("PosOpen3: " + getRow(row) + (col + 2) + ": " + value + ", "); // right 1
							} // end if
						} // end if
					} // end if
				} // end else if
				if(row == 1 || row == 2 || row == 3 | row == 4){ // row 1-4 column 2
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 1][col] == i){ // down 1
							if(board[row + 2][col] == 0){ // down 2
								if(board[row + 3][col] == 0){ // down 3
										
									open3Positions.add( new Move(row + 2, col, player, type)); // down 2
									//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
								} // end if
							} // end if
						} // end if
					} else if(board[row - 1][col] == 0){ // up 1
						if(board[row + 1][col] == 0){ // down 1
							if(board[row + 2][col] == i){ // down 2
								if(board[row + 3][col] == 0){ // down 3
										
									open3Positions.add( new Move(row + 1, col, player, type)); // down 1
									//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
								} // end if
							} // end if
						} // end if
					} // end if
				} // end if
				if(row == 2 | row == 3 | row == 4 | row == 5){ // row 2-5 column 2
					if(board[row - 1][col] == i){ // up 1
						if(board[row - 2][col] == 0){ // up 2
							if(board[row + 1][col] == 0){ // down 1
								if(board[row + 2][col] == 0){ // down 2
										
									open3Positions.add( new Move(row + 1, col, player, type)); // down 1
									//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
								} // end if
							} // end if
						} // end if
					} else if(board[row - 1][col] == 0){ // up 1
						if(board[row - 2][col] == 0){ // up 2
							if(board[row + 1][col] == i){ // down 1
								if(board[row + 2][col] == 0){ // down 2
										
									open3Positions.add( new Move(row - 1, col, player, type)); // up 1
									//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
								} // end if
							} // end if
						} // end if
					}
				} else if(row == 3 | row == 4 | row == 5 | row == 6){ // row 3-6 column 2
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 1][col] == i){ // up 1
							if(board[row - 2][col] == 0){ // up 2
								if(board[row - 3][col] == 0){ // up 3
										
									open3Positions.add( new Move(row - 2, col, player, type)); // up 2
									//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
								} // end if
							} // end if
						} // end if
					} else if(board[row + 1][col] == 0){ // down 1
						if(board[row - 1][col] == 0){ // up 1
							if(board[row - 2][col] == i){ // up 2
								if(board[row - 3][col] == 0){ // up 3
										
									open3Positions.add( new Move(row - 1, col, player, type)); // up 1
									//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
								} // end if
							} // end if
						} // end if
					} // end if
				} // end else if
			} else if(col == 3 || col == 4){ // columns 3 and 4
				if(board[row][col - 1] == 0){ // left 1
					if(board[row][col + 1] == 0){ // right 1
						
						if(board[row][col + 2] == i){ // right 2
							if(board[row][col + 3] == 0){ // right 3
								
								open3Positions.add( new Move(row, col + 1, player, type)); // right 1
								//System.out.print("PosOpen3: " + getRow(row) + (col + 2) + ": " + value + ", "); // right 1
							} // end if
						} // end if
					} else if(board[row][col + 1] == i){ // right 1
						if(board[row][col + 2] == 0){ // right 2								
							if(board[row][col - 2] == 0){ // left 2
									
									open3Positions.add( new Move(row, col - 1, player, type)); // left 1
									//System.out.print("PosOpen3: " + getRow(row) + (col) + ": " + value + ", "); // left 1
								} // end if
							} // end if
						}  // end else if
				} else if(board[row][col - 1] == i){ // left 1
					if(board[row][col - 2] == 0){ // left 2
						if(board[row][col + 1] == 0){ // right 1
							if(board[row][col + 2] == 0){ // right 2
									
								open3Positions.add( new Move(row, col + 1, player, type)); // right 1
								//System.out.print("PosOpen3: " + getRow(row) + (col + 2) + ": " + value + ", "); // right 1
							} // end if
						} // end if
					} // end if
				} // end else if
				
				if(board[row][col + 1] == 0){ // right 1
					if(board[row][col - 1] == 0){ // left 1
						
						if(board[row][col - 2] == i){ // left 2
							if(board[row][col - 3] == 0){ // left 3
								
								open3Positions.add( new Move(row, col - 1, player, type)); // left 1
								//System.out.print("PosOpen3: " + getRow(row) + (col) + ": " + value + ", "); // left 1
							} // end if
						} // end if
					} else if(board[row][col - 1] == i){ // left 1
						if(board[row][col - 2] == 0){ // left 2								
							if(board[row][col + 2] == 0){ // right 2
									
									open3Positions.add( new Move(row, col + 1, player, type)); // right 1
									//System.out.print("PosOpen3: " + getRow(row) + (col + 2) + ": " + value + ", "); // right 1
								} // end if
							} // end if
						}  // end else if
				} else if(board[row][col + 1] == i){ // right 1
					if(board[row][col + 2] == 0){ // right 2
						if(board[row][col - 1] == 0){ // left 1
							if(board[row][col - 2] == 0){ // left 2
									
								open3Positions.add( new Move(row, col - 1, player, type)); // left 1
								//System.out.print("PosOpen3: " + getRow(row) + (col) + ": " + value + ", "); // left 1
							} // end if
						} // end if
					} // end if
				} // end else if
				if(row == 1 || row == 2 || row == 3 | row == 4){ // row 1-4 column 0
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 1][col] == i){ // down 1
							if(board[row + 2][col] == 0){ // down 2
								if(board[row + 3][col] == 0){ // down 3
										
									open3Positions.add( new Move(row + 2, col, player, type)); // down 2
									//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
								} // end if
							} // end if
						} // end if
					} else if(board[row - 1][col] == 0){ // up 1
						if(board[row + 1][col] == 0){ // down 1
							if(board[row + 2][col] == i){ // down 2
								if(board[row + 3][col] == 0){ // down 3
										
									open3Positions.add( new Move(row + 1, col, player, type)); // down 1
									//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
								} // end if
							} // end if
						} // end if
					} // end if
				} // end if
				if(row == 2 | row == 3 | row == 4 | row == 5){ // row 2-5 column 0
					if(board[row - 1][col] == i){ // up 1
						if(board[row - 2][col] == 0){ // up 2
							if(board[row + 1][col] == 0){ // down 1
								if(board[row + 2][col] == 0){ // down 2
										
									open3Positions.add( new Move(row + 1, col, player, type)); // down 1
									//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
								} // end if
							} // end if
						} // end if
					} else if(board[row - 1][col] == 0){ // up 1
						if(board[row - 2][col] == 0){ // up 2
							if(board[row + 1][col] == i){ // down 1
								if(board[row + 2][col] == 0){ // down 2
										
									open3Positions.add( new Move(row - 1, col, player, type)); // up 1
									//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
								} // end if
							} // end if
						} // end if
					} // end else if
				} if(row == 3 | row == 4 | row == 5 | row == 6){ // row 3-6 column 0
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 1][col] == i){ // up 1
							if(board[row - 2][col] == 0){ // up 2
								if(board[row - 3][col] == 0){ // up 3
										
									open3Positions.add( new Move(row - 2, col, player, type)); // up 2
									//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
								} // end if
							} // end if
						} // end if
						if(board[row - 1][col] == 0){ // up 1
							if(board[row - 2][col] == i){ // up 2
								if(board[row - 3][col] == 0){ // up 3
										
									open3Positions.add( new Move(row - 1, col, player, type)); // up 1
									//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
								} // end if
							} // end if
						} // end if
					} // end if
				} // end else if
			} else if(col == 5){ // column 5
				
				if(board[row][col + 1] == 0){ // right 1
					if(board[row][col - 1] == 0){ // left 1
						
						if(board[row][col - 2] == i){ // left 2
							if(board[row][col - 3] == 0){ // left 3
								
								open3Positions.add( new Move(row, col - 1, player, type)); // left 1
								//System.out.print("PosOpen3: " + getRow(row) + (col) + ": " + value + ", "); // left 1
							} // end if
						} // end if
					} else if(board[row][col - 1] == i){ // left 1
						if(board[row][col - 2] == 0){ // left 2								
							if(board[row][col + 2] == 0){ // right 2
									
									open3Positions.add( new Move(row, col + 1, player, type)); // right 1
									//System.out.print("PosOpen3: " + getRow(row) + (col + 2) + ": " + value + ", "); // right 1
								} // end if
							} // end if
						}  // end else if
				} else if(board[row][col + 1] == i){ // right 1
					if(board[row][col + 2] == 0){ // right 2
						if(board[row][col - 1] == 0){ // left 1
							if(board[row][col - 2] == 0){ // left 2
									
								open3Positions.add( new Move(row, col - 1, player, type)); // left 1
								//System.out.print("PosOpen3: " + getRow(row) + (col) + ": " + value + ", "); // left 1
							} // end if
						} // end if
					} // end if
				} // end else if
				if(row == 1 || row == 2 || row == 3 | row == 4){ // row 1-4 column 0
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 1][col] == i){ // down 1
							if(board[row + 2][col] == 0){ // down 2
								if(board[row + 3][col] == 0){ // down 3
										
									open3Positions.add( new Move(row + 2, col, player, type)); // down 2
									//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
								} // end if
							} // end if
						} // end if
					} else if(board[row - 1][col] == 0){ // up 1
						if(board[row + 1][col] == 0){ // down 1
							if(board[row + 2][col] == i){ // down 2
								if(board[row + 3][col] == 0){ // down 3
										
									open3Positions.add( new Move(row + 1, col, player, type)); // down 1
									//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
								} // end if
							} // end if
						} // end if
					} // end if
				} // end if
				if(row == 2 | row == 3 | row == 4 | row == 5){ // row 2-5 column 0
					if(board[row - 1][col] == i){ // up 1
						if(board[row - 2][col] == 0){ // up 2
							if(board[row + 1][col] == 0){ // down 1
								if(board[row + 2][col] == 0){ // down 2
										
									open3Positions.add( new Move(row + 1, col, player, type)); // down 1
									//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
								} // end if
							} // end if
						} // end if
					} else if(board[row - 1][col] == 0){ // up 1
						if(board[row - 2][col] == 0){ // up 2
							if(board[row + 1][col] == i){ // down 1
								if(board[row + 2][col] == 0){ // down 2
										
									open3Positions.add( new Move(row - 1, col, player, type)); // up 1
									//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
								} // end if
							} // end if
						} // end if
					} // end else if
				} if(row == 3 | row == 4 | row == 5 | row == 6){ // row 3-6 column 0
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 1][col] == i){ // up 1
							if(board[row - 2][col] == 0){ // up 2
								if(board[row - 3][col] == 0){ // up 3
										
									open3Positions.add( new Move(row - 2, col, player, type)); // up 2
									//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
								} // end if
							} // end if
						} else if(board[row - 1][col] == 0){ // up 1
							if(board[row - 2][col] == i){ // up 2
								if(board[row - 3][col] == 0){ // up 3
										
									open3Positions.add( new Move(row - 1, col, player, type)); // up 1
									//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
								} // end if
							} // end if
						} // end if
					} // end if
				} // end else if
			} if(col == 6){ // column 6
				if(row == 0){ // row 0 column 6
					if(board[row][col + 1] == 0){ // right 1
						if(board[row][col - 1] == 0){ // left 1
							if(board[row][col - 2] == i){ // left 2
								if(board[row][col - 3] == 0){ // left 3
									open3Positions.add( new Move(row, col - 1, player, type)); // left 1
									//System.out.print("PosOpen3: " + getRow(row) + (col) + ": " + value + ", "); // left 1
								} // end if
							} // end if
						} else if(board[row][col - 1] == i){ // left 1
							if(board[row][col - 2] == 0){ // left 2
								if(board[row][col - 3] == 0){ // left 3
									open3Positions.add( new Move(row, col - 2, player, type)); // left 2
									//System.out.print("PosOpen3: " + getRow(row) + (col - 1) + ": " + value + ", "); // left 2
								} // end if
							} // end if
						} // end else if
					} // end if
				} else if(row == 1){ // row 1 column 6
					if(board[row][col + 1] == 0){ // right 1
						if(board[row][col - 1] == 0){ // left 1
							if(board[row][col - 2] == i){ // left 2
								if(board[row][col - 3] == 0){ // left 3
									open3Positions.add( new Move(row, col - 1, player, type)); // left 1
									//System.out.print("PosOpen3: " + getRow(row) + (col) + ": " + value + ", "); // left 1
								} // end if
							} // end if
						} else if(board[row][col - 1] == i){ // left 1
							if(board[row][col - 2] == 0){ // left 2
								if(board[row][col - 3] == 0){ // left 3
									open3Positions.add( new Move(row, col - 2, player, type)); // left 2
									//System.out.print("PosOpen3: " + getRow(row) + (col - 1) + ": " + value + ", "); // left 2
								} // end if
							} // end if
						} // end else if
					} // end if
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 1][col] == i){ // down 1
							if(board[row + 2][col] == 0){ // down 2
								if(board[row + 3][col] == 0){ // down 3
									open3Positions.add(new Move(row + 2, col, player, type)); // down 2
									//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
								} // end if
							} // end if
						} else if(board[row + 1][col] == 0){ // down 1
							if(board[row + 2][col] == i){ // down 2
								if(board[row + 3][col] == 0){ // down 3
									open3Positions.add(new Move(row + 1, col, player, type)); // down 1
									//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
								} // end if
							} // end if
						} // end if
					} // end if
				} else if(row == 2){ // row 2 column 6
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0 && // down 1
							   board[row + 2][col] == i){ // down 2
								open3Positions.add( new Move(row + 1, col, player, type)); // down 1
								//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								open3Positions.add( new Move(row + 2, col, player, type)); // down 2
								//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
							} // end else if
						} // end if
					} else if(board[row - 1][col] == i && // up 1
								  board[row - 2][col] == 0 ){ // up 2
						if(board[row + 1][col] == 0 && // down 1
						   board[row + 2][col] == 0){ // down 2
							open3Positions.add( new Move(row + 1, col, player, type)); // down 1
							//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
						} // end if
					} if(board[row][col + 1] == 0){ // right 1
						if(board[row][col - 3] == 0){ // left 3
							if(board[row][col - 1] == 0 && // left 1
							   board[row][col - 2] == i){ // left 2
								open3Positions.add( new Move(row, col - 1, player, type)); // left 1
								//System.out.print("PosOpen3: " + getRow(row) + (col) + ": " + value + ", "); // left 1
							} else if(board[row][col - 1] == i && // left 1
									  board[row][col - 2] == 0){ // left 2
								open3Positions.add( new Move(row, col - 2, player, type)); // left 2
								//System.out.print("PosOpen3: " + getRow(row) + (col - 1) + ": " + value + ", "); // left 2
							} // end else if
						} // end if
					} // end if
				} else if(row == 3 || row == 4){ // rows 3 or 4 column 6
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0 && // down 1
							   board[row + 2][col] == i){ // down 2
								open3Positions.add( new Move(row + 1, col, player, type)); // down 1
								//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								open3Positions.add( new Move(row + 2, col, player, type)); // down 2
								//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
							} // end else if
						} // end if
					} else if(board[row - 1][col] == i && // up 1
							  board[row - 2][col] == 0 ){ // up 2
						if(board[row + 1][col] == 0 && // down 1
						   board[row + 2][col] == 0){ // down 2
							open3Positions.add( new Move(row + 1, col, player, type)); // down 1
							//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
						} // end if
					} if(board[row][col + 1] == 0){ // right 1
						if(board[row][col - 3] == 0){ // left 3
							if(board[row][col - 1] == 0 && // left 1
							   board[row][col - 2] == i){ // left 2
								open3Positions.add( new Move(row, col - 1, player, type)); // left 1
								//System.out.print("PosOpen3: " + getRow(row) + (col) + ": " + value + ", "); // left 1
							} else if(board[row][col - 1] == i && // left 1
									  board[row][col - 2] == 0){ // left 2
								open3Positions.add( new Move(row, col - 2, player, type)); // left 2
								//System.out.print("PosOpen3: " + getRow(row) + (col - 1) + ": " + value + ", "); // left 2
							} // end else if
						} // end if
					} // end if
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0 && // up 1
							   board[row - 2][col] == i){ // up 2
								open3Positions.add( new Move(row - 1, col, player, type)); // up 1
								//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								open3Positions.add( new Move(row - 2, col, player, type)); // up 2
								//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
							} // end else if
						} // end if
					} else if(board[row + 1][col] == i && // down 1
							  board[row + 2][col] == 0 ){ // down 2
						if(board[row - 1][col] == 0 && // up 1
						   board[row - 2][col] == 0){ // up 2
							open3Positions.add( new Move(row - 1, col, player, type)); // up 1
							//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
						} // end if
					} // end else if
				} else if(row == 5){ // row 5 column 6
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0 && // up 1
							   board[row - 2][col] == i){ // up 2
								open3Positions.add( new Move(row - 1, col, player, type)); // up 1
								//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								open3Positions.add( new Move(row - 2, col, player, type)); // up 2
								//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
							} // end else if
						} // end if
					} else if(board[row + 1][col] == i && // down 1
							  board[row + 2][col] == 0 ){ // down 2
						if(board[row - 1][col] == 0 && // up 1
						   board[row - 2][col] == 0){ // up 2
							open3Positions.add( new Move(row - 1, col, player, type)); // up 1
							//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
						} // end if
					} if(board[row][col + 1] == 0){ // right 1
						if(board[row][col - 3] == 0){ // left 3
							if(board[row][col - 1] == 0 && // left 1
							   board[row][col - 2] == i){ // left 2
								open3Positions.add( new Move(row, col - 1, player, type)); // left 1
								//System.out.print("PosOpen3: " + getRow(row) + (col - 2) + ": " + value + ", "); // left 1
							} else if(board[row][col - 1] == i && // left 1
									  board[row][col - 2] == 0){ // left 2
								open3Positions.add( new Move(row, col - 2, player, type)); // left 2
								//System.out.print("PosOpen3: " + getRow(row) + (col - 1) + ": " + value + ", "); // left 2
							} // end else if
						} // end if
					} // end if
				} if(row == 6){ // row 6 column 6
					if(board[row][col + 1] == 0){ // right 1
						if(board[row][col - 1] == 0){ // left 1
							if(board[row][col - 2] == i){ // left 2
								if(board[row][col - 3] == 0){ // left 3
									open3Positions.add( new Move(row, col - 1, player, type)); // left 1
									//System.out.print("PosOpen3: " + getRow(row) + (col) + ": " + value + ", "); // left 1
								} // end if
							} // end if
						} else if(board[row][col - 1] == i){ // left 1
							if(board[row][col - 2] == 0){ // left 2
								if(board[row][col - 3] == 0){ // left 3
									open3Positions.add( new Move(row, col - 2, player, type)); // left 2
									//System.out.print("PosOpen3: " + getRow(row) + (col - 1) + ": " + value + ", "); // left 2
								} // end if
							} // end if
						} // end else if
					} // end if
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 1][col] == i){ // up 1
							if(board[row - 2][col] == 0){ // up 2
								if(board[row - 3][col] == 0){ // up 3
									open3Positions.add(new Move(row - 2, col, player, type)); // up 2
									//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
								} // end if
							} // end if
						} else if(board[row - 1][col] == 0){ // up 1
							if(board[row - 2][col] == i){ // up 2
								if(board[row - 3][col] == 0){ // up 3
									open3Positions.add(new Move(row - 1, col, player, type)); // up 1
									//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
								} // end if
							} // end if
						} // end if
					} // end if
			    } else if(row == 7){ // row 7 column 6
					if(board[row][col + 1] == 0){ // right 1
						if(board[row][col - 1] == 0){ // left 1
							if(board[row][col - 2] == i){ // left 2
								if(board[row][col - 3] == 0){ // left 3
									open3Positions.add( new Move(row, col - 1, player, type)); // left 1
									//System.out.print("PosOpen3: " + getRow(row) + (col) + ": " + value + ", "); // left 1
								} // end if
							} // end if
						} else if(board[row][col - 1] == i){ // left 1
							if(board[row][col - 2] == 0){ // left 2
								if(board[row][col - 3] == 0){ // left 3
									open3Positions.add( new Move(row, col - 2, player, type)); // left 2
									//System.out.print("PosOpen3: " + getRow(row) + (col - 1) + ": " + value + ", "); // left 2
								} // end if
							} // end if
						} // end else if
					} // end if
			   } // end else if(row == 7).
			} else if(col == 7){ // column 7
				if(row == 1){ // row 1 column 7
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0 && // down 1
							   board[row + 2][col] == i){ // down 2
								open3Positions.add( new Move(row + 1, col, player, type)); // down 1
								//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								open3Positions.add( new Move(row + 2, col, player, type)); // down 2
								//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
							} // end else if
						} // end if
					} // end if
				} else if(row == 2){ // row 2 column 7
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0 && // down 1
							   board[row + 2][col] == i){ // down 2
								open3Positions.add( new Move(row + 1, col, player, type)); // down 1
								//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								open3Positions.add( new Move(row + 2, col, player, type)); // down 2
								//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
							} // end else if
						} // end if
					} else if(board[row - 1][col] == i){ // up 1
						if((board[row - 2][col] == 0)){ // up 2
							if(board[row + 1][col] == 0){ // down 1
								if((board[row + 2][col] == 0)){ // down 2
									open3Positions.add( new Move(row + 1, col, player, type)); // down 1
									//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
								} // end if
							} // end if
						} // end if
					} // end else if
				} else if(row == 3 || row == 4){ // row 3 or 4 column 7
					if(board[row - 1][col] == 0){ // up 1
						if(board[row + 3][col] == 0){ // down 3
							if(board[row + 1][col] == 0 && // down 1
							   board[row + 2][col] == i){ // down 2
								open3Positions.add( new Move(row + 1, col, player, type)); // down 1
								//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
							} else if(board[row + 1][col] == i && // down 1
									  board[row + 2][col] == 0){ // down 2
								open3Positions.add( new Move(row + 2, col, player, type)); // down 2
								//System.out.print("PosOpen3: " + getRow(row + 2) + (col + 1) + ": " + value + ", "); // down 2
							} // end else if
						} // end if
					} else if(board[row - 1][col] == i){ // up 1
						if((board[row - 2][col] == 0)){ // up 2
							if(board[row + 1][col] == 0){ // down 1
								if((board[row + 2][col] == 0)){ // down 2
									open3Positions.add( new Move(row + 1, col, player, type)); // down 1
									//System.out.print("PosOpen3: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
								} // end if
							} // end if
						} // end if
					} if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0 && // up 1
							   board[row - 2][col] == i){ // up 2
								open3Positions.add( new Move(row - 1, col, player, type)); // up 1
								//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								open3Positions.add( new Move(row - 2, col, player, type)); // up 2
								//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
							}
						} // end if
					} else if(board[row + 1][col] == i){ // down 1
						if((board[row + 2][col] == 0)){ // down 2
							if(board[row - 1][col] == 0){ // up 1
								if((board[row - 2][col] == 0)){ // up 2
									open3Positions.add( new Move(row - 1, col, player, type)); // up 1
									//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
								} // end if
							} // end if
						} // end if
					} // end else if
				} else if(row == 5){ // row 5 column 7
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0 && // up 1
							   board[row - 2][col] == i){ // up 2
								open3Positions.add( new Move(row - 1, col, player, type)); // up 1
								//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								open3Positions.add( new Move(row - 2, col, player, type)); // up 2
								//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
							} // end else if
						} // end if
					} else if(board[row + 1][col] == i){ // down 1
						if((board[row + 2][col] == 0)){ // down 2
							if(board[row - 1][col] == 0){ // up 1
								if((board[row - 2][col] == 0)){ // up 2
									open3Positions.add( new Move(row - 1, col, player, type)); // up 1
									//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
								} // end if
							} // end if
						} // end if
					} // end else if
				} else if(row == 6){ // row 6 column 7
					if(board[row + 1][col] == 0){ // down 1
						if(board[row - 3][col] == 0){ // up 3
							if(board[row - 1][col] == 0 && // up 1
							   board[row - 2][col] == i){ // up 2
								open3Positions.add( new Move(row - 1, col, player, type)); // up 1
								//System.out.print("PosOpen3: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
							} else if(board[row - 1][col] == i && // up 1
									  board[row - 2][col] == 0){ // up 2
								open3Positions.add( new Move(row - 2, col, player, type)); // up 2
								//System.out.print("PosOpen3: " + getRow(row - 2) + (col + 1) + ": " + value + ", "); // up 2
							} // end else if
						} // end if
					} // end if
				} // end else if
			} // end if(col == 7) // column 7
		} // end for
		
		return open3Positions;
	} // end class updateOpen3PositionsList, check open3Positions
	
	private static MoveSet getSmallOpenLPositionsList(Node node){ 
		MoveSet smallOpenLPositions = new MoveSet();
		int value;
		int player = 2/node.lastMove.player;
		for(Move move : Node.moves){
			int row = move.row;
			int col = move.column;
			if(player == move.player){ 
				value = 80;
			} else {
				value = 75;
			} // end else
			
			MoveType type = new MoveType(MoveType.Type.SMALL_OPEN_L, value);
			
			if(row > 0 && row < 7 && col > 0 && row < 7){ // move not on perimeter
				if(col == 1){
					smallOpenLPositions.addAll(smallOpenLColumn1(node, move, type));
				} else if(col == 2 || col == 3 || col == 4){
//					System.out.print("1Move: " + move.moveString + "~ ");
					smallOpenLPositions.addAll(smallOpenLColumns_2_3_4(node, move, type));
//					for(Move myMove: smallOpenLPositions){
//						System.out.print(myMove.moveString + ", ");
//					}
//					System.out.println("1END Move " + move.moveString );
				} if(col == 3 || col == 4 || col == 5){
//					System.out.print("2Move: " + move.moveString + "~ ");
					smallOpenLPositions.addAll(smallOpenLColumns_3_4_5(node, move, type));
//					for(Move myMove: smallOpenLPositions){
//						System.out.print(myMove.moveString + ", ");
//					}
//					System.out.println("2END Move " + move.moveString );
				} else if(col == 6){
					smallOpenLPositions.addAll(smallOpenLColumn6(node, move, type));
				} 
			 } // end if
		} // end for
//		System.out.println("small L");
//		for(Move move : smallOpenLPositions){
//			System.out.println(move.moveString + ": " + move.value);
//		}
//		System.out.println("End Small L");
		return smallOpenLPositions;
	} // end method smallOpenLPositions
	
	public static MoveSet smallOpenLColumn1(Node node, Move move, MoveType type){
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
						
						 smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
//						 System.out.print("1PosSmallOpenL: " + getRow(row) + (col + 2) + ": " + type.value + ", "); // right 1
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
						
						smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
//						System.out.print("2PosSmallOpenL: " + getRow(row + 1) + (col + 1) + ": " + type.value + ", "); // down 1
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
						
					smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
					//System.out.print("3PosSmallOpenL: " + getRow(row + 1) + (col + 1) + ": " + type.value + ", "); // down 1
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
					
					smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
					//System.out.print("4PosSmallOpenL: " + getRow(row) + (col + 2) + ": " + type.value + ", "); // right 1
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
						
						 smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
						 //System.out.print("5PosSmallOpenL: " + getRow(row) + (col + 2) + ": " + type.value + ", "); // right 1
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
						
						smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
						//System.out.print("6PosSmallOpenL: " + getRow(row - 1) + (col) + ": " + type.value + ", "); // up 1
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
						
					smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
					//System.out.print("7PosSmallOpenL: " + getRow(row - 1) + (col + 1) + ": " + type.value + ", "); // up 1
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
					
					smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
					//System.out.print("8PosSmallOpenL: " + getRow(row) + (col + 2) + ": " + type.value + ", "); // right 1
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

						smallOpenLPositions.add(new Move(row + 1, col + 1, player, type)); // down 1 right 1
						//System.out.print("9PosSmallOpenL: " + getRow(row + 1) + (col + 2) + ": " + type.value + ", "); // down 1 right 1
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
						  
							smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
							//System.out.print("A PosSmallOpenL: " + getRow(row) + (col + 2) + ": " + type.value + ", "); // right 1
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

							
							smallOpenLPositions.add(new Move(row + 1, col + 1, player, type)); // down 1 right 1
							//System.out.print("B PosSmallOpenL: " + getRow(row + 1) + (col + 2) + ": " + type.value + ", "); // down 1 right 1
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
							
							smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
							//System.out.print("C PosSmallOpenL: " + getRow(row + 1) + (col + 1) + ": " + type.value + ", "); // down 1
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

						smallOpenLPositions.add(new Move(row - 1, col + 1, player, type)); // down 1 right 1
						//System.out.print("D PosSmallOpenL: " + getRow(row - 1) + (col + 2) + ": " + type.value + ", "); // down 1 right 1	  
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

						smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
						//System.out.print("E PosSmallOpenL: " + getRow(row) + (col + 2) + ": " + type.value + ", "); // right 1	  
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

					smallOpenLPositions.add(new Move(row - 1, col + 1, player, type)); // up 1 right 1
					//System.out.print("F PosSmallOpenL: " + getRow(row - 1) + (col + 2) + ": " + type.value + ", "); // up 1 right 1
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

						smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
						//System.out.print("G PosSmallOpenL: " + getRow(row) + (col + 2) + ": " + type.value + ", "); // right 1
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
	   
					smallOpenLPositions.add(new Move(row + 1, col + 1, player, type)); // down 1 right 1
					//System.out.print("H PosSmallOpenL: " + getRow(row + 1) + (col + 2) + ": " + type.value + ", "); // down 1 right 1
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
	   
					smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
					//System.out.print("I PosSmallOpenL: " + getRow(row) + (col + 2) + ": " + type.value + ", "); // right 1
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

					smallOpenLPositions.add(new Move(row - 1, col + 1, player, type)); // up 1 right 1
					//System.out.print("J PosSmallOpenL: " + getRow(row - 1) + (col + 2) + ": " + type.value + ", "); // up 1 right 1
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

					smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
					//System.out.print("K PosSmallOpenL: " + getRow(row) + (col + 2) + ": " + type.value + ", "); // right 1
				} // end if	
			} // end else if
		} // end else if
	
		
		return smallOpenLPositions;
	} // checked smallOpenLPositions
	public static MoveSet smallOpenLColumn6(Node node, Move move, MoveType type){
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
						
						 smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
						 //System.out.print("PosSmallOpenL: " + getRow(row) + (col) + ": " + value + ", "); // left 1
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
						 board[row + 3][col]   == i)){ // down 3
						
						smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
						//System.out.print("PosSmallOpenL: " + getRow(row + 1) + (col) + ": " + value + ", "); // down 1
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
						
					smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
					//System.out.print("PosSmallOpenL: " + getRow(row + 1) + (col) + ": " + value + ", "); // down 1
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
					
					smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
					//System.out.print("PosSmallOpenL: " + getRow(row) + (col) + ": " + value + ", "); // left 1
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
						
						 smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
						 //System.out.print("PosSmallOpenL: " + getRow(row) + (col) + ": " + value + ", "); // left 1
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
						
						smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
						//System.out.print("PosSmallOpenL: " + getRow(row - 1) + (col) + ": " + value + ", "); // up 1
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
						
					smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
					//System.out.print("PosSmallOpenL: " + getRow(row - 1) + (col + 1) + ": " + value + ", "); // up 1
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
					
					smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
					//System.out.print("PosSmallOpenL: " + getRow(row) + (col) + ": " + value + ", "); // left 1
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

						smallOpenLPositions.add(new Move(row + 1, col - 1, player, type)); // down 1 left 1
						//System.out.print("PosSmallOpenL: " + getRow(row + 1) + (col) + ": " + value + ", "); // down 1 left 1
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
						  
							smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
							//System.out.print("PosSmallOpenL: " + getRow(row) + (col) + ": " + value + ", "); // left 1
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
							
							smallOpenLPositions.add(new Move(row + 1, col - 1, player, type)); // down 1 left 1
							//System.out.print("PosSmallOpenL: " + getRow(row + 1) + (col) + ": " + value + ", "); // down 1 left 1
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
							
							smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
							//System.out.print("PosSmallOpenL: " + getRow(row + 1) + (col + 1) + ": " + value + ", "); // down 1
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

						smallOpenLPositions.add(new Move(row - 1, col - 1, player, type)); // up 1 left 1
						//System.out.print("PosSmallOpenL: " + getRow(row - 1) + (col) + ": " + value + ", "); // up 1 left 1	  
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

						smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
						//System.out.print("PosSmallOpenL: " + getRow(row) + (col) + ": " + value + ", "); // left 1	  
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

					smallOpenLPositions.add(new Move(row - 1, col - 1, player, type)); // up 1 left 1
					//System.out.print("PosSmallOpenL: " + getRow(row - 1) + (col) + ": " + value + ", "); // up 1 left 1
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

						smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
						//System.out.print("PosSmallOpenL: " + getRow(row) + (col) + ": " + value + ", "); // left 1
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

						smallOpenLPositions.add(new Move(row + 1, col - 1, player, type)); // down 1 left 1
						//System.out.print("PosSmallOpenL: " + getRow(row + 1) + (col) + ": " + value + ", "); // down 1 left 1
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

							smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
							//System.out.print("PosSmallOpenL: " + getRow(row) + (col) + ": " + value + ", "); // left 1
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
	   
					smallOpenLPositions.add(new Move(row + 1, col - 1, player, type)); // down 1 left 1
					//System.out.print("PosSmallOpenL: " + getRow(row + 1) + (col) + ": " + value + ", "); // down 1 left 1
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
	   
					smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
					//System.out.print("PosSmallOpenL: " + getRow(row) + (col) + ": " + value + ", "); // left 1
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

					smallOpenLPositions.add(new Move(row - 1, col - 1, player, type)); // up 1 left 1
					//System.out.print("PosSmallOpenL: " + getRow(row - 1) + (col) + ": " + value + ", "); // up 1 left 1
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

					smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
					//System.out.print("PosSmallOpenL: " + getRow(row) + (col) + ": " + value + ", "); // left 1
				} // end if	
			} // end else if
		} // end else if
		return smallOpenLPositions;
	} // end method column2
	
	public static MoveSet smallOpenLColumns_2_3_4(Node node, Move move, MoveType type){
		MoveSet smallOpenLPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(row == 1){ // row 1, column 2
			if(board[row + 1][col - 1] == i){ // diagonal down left								
				//System.out.println("sL: " + "dl");
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
					  
						smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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


						smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
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
				  
					smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row + 1][col]  == i){  // down 1
					 
					smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
				} // end else if
			} // end if						
			if(board[row + 1][col + 1] == i){ // diagonal down right
				if(board[row][col + 1] == 0){  // right 1
					//System.out.println("sL: " + "dr");
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

					  
						smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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

						
						smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
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
				  
				   smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col + 1]  == 0 && // right 1
					      board[row + 1][col]  == i){ // down 1
					smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
				} // end else if
			} // end if				  
		} if(row == 6){ // row 6 column 2
			if(board[row - 1][col - 1] == i){ // diagonal up left								
				//System.out.println("sL: " + "dl");
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
					  
						smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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


						smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
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
				  
					smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row - 1][col]  == i){  // up 1
					 
					smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
				} // end else if
			} // end if						
			if(board[row - 1][col + 1] == i){ // diagonal up right
				if(board[row][col + 1] == 0){ // right 1
					//System.out.println("sL: " + "dr");
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

					  
						smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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

						
						smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
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
				  
				   smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col + 1]  == 0 && // right 1
						  board[row - 1][col]  == i){ // up 1
					smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
				} // end else if
			} // end if				  
		} else if(row == 2 || row == 3 || row == 4){ // row 2-4, column 2
			if(board[row - 1][col - 1] == i){ // diagonal up left
				//System.out.println("up left");
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

						
						  smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
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

						
						  smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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
						  
					  smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col - 1]  == 0 && // left 1
					board[row - 1][col] == i){ // up 1
				
				smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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

						
						  smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
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

						
						  smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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
						  
					  smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col + 1]  == 0 && // right 1
					      board[row - 1][col]  == i){ // up 1
				
					  smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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

						
						  smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
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

						
						  smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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
					  
					  smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col - 1]  == 0 && // left 1
						board[row + 1][col]  == i){ // down 1
										
					smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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

					
					  smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
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

						
						  smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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
					
					smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col + 1]  == 0 && // right 1
						  board[row + 1][col]  == i){  // down 1
					  
					  smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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

						
						  smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
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

						
						  smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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
						  
					  smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col - 1]  == 0 && // left 1
					board[row + 1][col] == i){ // down 1
				
				smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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

						
						  smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
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

						
						  smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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
						  
					  smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col + 1]  == 0 && // right 1
						  board[row + 1][col]  == i){ // down 1
				
					  smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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

						
						  smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
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

						
						  smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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
					  
					  smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col - 1]  == 0 && // left 1
						board[row - 1][col]  == i){ // up 1
										
					smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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

					
					  smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
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

						
						  smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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
					
					smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col + 1]  == 0 && // right 1
						  board[row - 1][col]  == i){  // up 1
					  
					  smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
				} // end if						
			} // end if
		}// end if
	
		return smallOpenLPositions;
	} // checked smallOpenLPositions
	
	public static MoveSet smallOpenLColumns_3_4_5(Node node, Move move, MoveType type){
		MoveSet smallOpenLPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(row == 1){ // row 1, column 5
			if(board[row + 1][col + 1] == i){ // diagonal down right								
				//System.out.println("sL: " + "dl");
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
					  
						smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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


						smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
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
				  
					smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col + 1]  == 0 && // right 1
						  board[row + 1][col]  == i){  // down 1
					 
					smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
				} // end else if
			} // end if						
			if(board[row + 1][col - 1] == i){ // diagonal down left
				if(board[row][col - 1] == 0){  // left 1
					//System.out.println("sL: " + "dr");
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

					  
						smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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

						
						smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
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
				  
				   smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row + 1][col]  == i){ // down 1
					smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
				} // end else if
			} // end if				  
		} if(row == 6){ // row 6 column 5
			if(board[row - 1][col + 1] == i){ // diagonal up right								
				//System.out.println("sL: " + "dl");
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
					  
						smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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


						smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
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
				  
					smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col + 1]  == 0 && // right 1
						  board[row - 1][col]  == i){  // up 1
					 
					smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
				} // end else if
			} // end if						
			if(board[row - 1][col - 1] == i){ // diagonal up left
				if(board[row][col - 1] == 0){ // left 1
					//System.out.println("sL: " + "dr");
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

					  
						smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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

						
						smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
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
				  
				   smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row - 1][col]  == i){ // up 1
					smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
				} // end else if
			} // end if				  
		} else if(row == 2 || row == 3 || row == 4){ // row 2-4, column 5
			if(board[row - 1][col + 1] == i){ // diagonal up right
				//System.out.println("up right");
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

						
						  smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
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

						
						  smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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
						  
					  smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col + 1]  == 0 && // right 1
					board[row - 1][col] == i){ // up 1
				
				smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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

						
						  smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
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

						
						  smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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
						  
					  smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row - 1][col]  == i){ // up 1
				
					  smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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

						
						  smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
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

						
						  smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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
					  
					  smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col + 1]  == 0 && // right 1
						board[row + 1][col]  == i){ // down 1
										
					smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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

					
					  smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
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

						
						  smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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
					
					smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row + 1][col]  == i){  // down 1
					  
					  smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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

						
						  smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
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

						
						  smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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
						  
					  smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col + 1]  == 0 && // right 1
					board[row + 1][col] == i){ // down 1
				
				smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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

						
						  smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
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

						
						  smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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
						  
					  smallOpenLPositions.add(new Move(row + 1, col, player, type)); // down 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row + 1][col]  == i){ // down 1
				
					  smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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

						
						  smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
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

						
						  smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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
					  
					  smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col + 1]  == 0 && // right 1
						board[row - 1][col]  == i){ // up 1
										
					smallOpenLPositions.add(new Move(row, col + 1, player, type)); // right 1
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

					
					  smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
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

						
						  smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
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
					
					smallOpenLPositions.add(new Move(row - 1, col, player, type)); // up 1
				} else if(board[row][col - 1]  == 0 && // left 1
						  board[row - 1][col]  == i){  // up 1
					  
					  smallOpenLPositions.add(new Move(row, col - 1, player, type)); // left 1
				} // end if						
			} // end if
		} // end if

		return smallOpenLPositions;
} // end method column 5 , checked small OpenLPositions
	
	// create/block open Big L
	private static MoveSet getBigOpenLPositionsList(Node node){ 
		MoveSet bigOpenLPositions = new MoveSet();
		int value;
		int currentPlayer = 2/node.lastMove.player;
		for(Move move : Node.moves){
			int row = move.row;
			int col = move.column;
			if(move.player == currentPlayer){ 
				value = 80;
			} else {
				value = 75;
			} // end else
			
			MoveType type = new MoveType(MoveType.Type.BIG_OPEN_L, value);
			
			if(row > 0 && row < 7 && col > 0 && row < 7){ // move not on perimeter
				if(col == 1 || col == 2 || col == 3 || col == 4){
					bigOpenLPositions.addAll(columns1and2B(node, move, type));
				} // end if
				if(col == 3 || col == 4 || col == 5 || col == 6){
					bigOpenLPositions.addAll(columns5and6B(node, move, type));
				} // end if
			 } // end if
		} // end for
//		System.out.println("Big L");
//		for(Move move : bigOpenLPositions){
//			System.out.println(move.moveString + ": " + move.value);
//		}
//		System.out.println("End Big L");
		return bigOpenLPositions;
	} // end method bigOpenL

	public static MoveSet columns1and2B(Node node, Move move, MoveType type){
		MoveSet bigOpenLPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(row == 1 || row == 2){ // row 1-2, column 1
			if(board[row + 2][col + 2] == i){ // diagonal down 2 right 2	
				
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
					  
						
						bigOpenLPositions.add(new Move(row, col + 2, player, type)); // right 2
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
					  
						
						bigOpenLPositions.add(new Move(row + 2, col, player, type)); // down 2
					} // end if
				} // end if
			} // end if
		} if(row == 5 || row == 6){ // row 5-6, column 1
		if(board[row - 2][col + 2] == i){ // diagonal up 2 right 2	
			
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
				  
					
					bigOpenLPositions.add(new Move(row, col + 2, player, type)); // right 2
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
				  
					
					bigOpenLPositions.add(new Move(row - 2, col, player, type)); // up 2
				} // end if
			} // end if
		} // end if
	} else if(row == 3 || row == 4){
			if(board[row + 2][col + 2] == i){ // diagonal down 2 right 2	
				
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
					  
						
						bigOpenLPositions.add(new Move(row, col + 2, player, type)); // right 2
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
					  
						
						bigOpenLPositions.add(new Move(row + 2, col, player, type)); // down 2
					} // end if
				} // end if
			} // end if
		if(board[row - 2][col + 2] == i){ // diagonal up 2 right 2	
			
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
				  
					
					bigOpenLPositions.add(new Move(row, col + 2, player, type)); // right 2
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
				  
					
					bigOpenLPositions.add(new Move(row - 2, col, player, type)); // up 2
				} // end if
			} // end if
		} // end if
	} // end if
	return bigOpenLPositions;
} // end class column1B
	
	public static MoveSet columns5and6B(Node node, Move move, MoveType type){
		MoveSet bigOpenLPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		if(row == 1 || row == 2){ // row 1-2, column 6
			if(board[row + 2][col - 2] == i){ // diagonal down 2 left 2	
				
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
					  
						
						bigOpenLPositions.add(new Move(row, col - 2, player, type)); // left 2
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
					  
						
						bigOpenLPositions.add(new Move(row + 2, col, player, type)); // down 2
					} // end if
				} // end if
			} // end if
		} if(row == 5 || row == 6){ // row 5-6, column 6
			if(board[row - 2][col - 2] == i){ // diagonal up 2 left 2	
				
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
					  
						
						bigOpenLPositions.add(new Move(row, col - 2, player, type)); // left 2
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
					  
						
						bigOpenLPositions.add(new Move(row - 2, col, player, type)); // up 2
					} // end if
				} // end if
			} // end if
		} else if(row == 3 || row == 4){
				if(board[row + 2][col - 2] == i){ // diagonal down 2 left 2	
					
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
						  
							
							bigOpenLPositions.add(new Move(row, col - 2, player, type)); // left 2
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
						  
							
							bigOpenLPositions.add(new Move(row + 2, col, player, type)); // down 2
						} // end if
					} // end if
				} // end if
			if(board[row - 2][col - 2] == i){ // diagonal up 2 left 2	
				
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
					  
						
						bigOpenLPositions.add(new Move(row, col - 2, player, type)); // left 2
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
					  
						
						bigOpenLPositions.add(new Move(row - 2, col, player, type)); // up 2
					} // end if
				} // end if
			} // end if
		} // end if
		return bigOpenLPositions;
	} // end method column6B

	public static MoveSet getPreSmallOpenLPositionsList(Node node){
		MoveSet preSmallOpenLPositions = new MoveSet();
		int currentPlayer = 2/node.lastMove.player;
		int value;
		for(Move move : Node.moves){
			int row = move.row;
			int col = move.column;
			if(move.player == currentPlayer){ 
				value = 50;
			} else {
				value = 15;
			} // end else
			
			MoveType type = new MoveType(MoveType.Type.PRE_SMALL_OPEN_L, value);
			
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
		return preSmallOpenLPositions;
	} // end method preOpenL
	
	public static MoveSet preSmallOpenL_Columns_1_2_3_4(Node node, Move move, MoveType type){
		
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
						
						
						 preSmallOpenLPositions.add(new Move(row + 1, col + 1, player, type)); // down 1 right 1
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
						
						
						 preSmallOpenLPositions.add(new Move(row + 1, col + 1, player, type)); // down 1 right 1
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
						
						 preSmallOpenLPositions.add(new Move(row - 1, col + 1, player, type)); // up 1 right 1
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
						
						 preSmallOpenLPositions.add(new Move(row - 1, col + 1, player, type)); // up 1 right 1
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
						
						 preSmallOpenLPositions.add(new Move(row + 1, col + 1, player, type)); // down 1 right 1
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
						
						 preSmallOpenLPositions.add(new Move(row + 1, col + 1, player, type)); // down 1 right 1
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
						
						
						 preSmallOpenLPositions.add(new Move(row - 1, col + 1, player, type)); // up 1 right 1
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
							
							
							 preSmallOpenLPositions.add(new Move(row - 1, col + 1, player, type)); // up 1 right 1
					} // end else if
				} // end if
			} // end if
		} // end if 

		return preSmallOpenLPositions;
	} // end method column1PreOpenL
	
		public static MoveSet preSmallOpenL_Columns_2_3_4_5(Node node, Move move, MoveType type){
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
								
								 preSmallOpenLPositions.add(new Move(row + 1, col + 1, player, type)); // down 1 right 1
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
								
								 preSmallOpenLPositions.add(new Move(row + 1, col + 1, player, type)); // down 1 right 1
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
								
								
							 preSmallOpenLPositions.add(new Move(row + 1, col - 1, player, type)); // down 1 left 1
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
							
							
							 preSmallOpenLPositions.add(new Move(row + 1, col - 1, player, type)); // down 1 left 1
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
							
							 preSmallOpenLPositions.add(new Move(row - 1, col + 1, player, type)); // up 1 right 1
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
							
							
							 preSmallOpenLPositions.add(new Move(row - 1, col + 1, player, type)); // up 1 right 1
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
							
							
							 preSmallOpenLPositions.add(new Move(row - 1, col - 1, player, type)); // up 1 left 1
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
							
							
							 preSmallOpenLPositions.add(new Move(row - 1, col - 1, player, type)); // up 1 left 1
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
							
							 preSmallOpenLPositions.add(new Move(row + 1, col + 1, player, type)); // down 1 right 1
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
							
							
							 preSmallOpenLPositions.add(new Move(row + 1, col + 1, player, type)); // down 1 right 1
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
							
							
							 preSmallOpenLPositions.add(new Move(row + 1, col - 1, player, type)); // down 1 left 1
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
							
							
							 preSmallOpenLPositions.add(new Move(row + 1, col - 1, player, type)); // down 1 left 1
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
							
							 preSmallOpenLPositions.add(new Move(row - 1, col + 1, player, type)); // up 1 right 1
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
							
							 preSmallOpenLPositions.add(new Move(row - 1, col + 1, player, type)); // up 1 right 1
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
							
							
						 preSmallOpenLPositions.add(new Move(row - 1, col - 1, player, type)); // up 1 left 1
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
						
						
						 preSmallOpenLPositions.add(new Move(row - 1, col - 1, player, type)); // up 1 left 1
					} // end else if
				} // end if

			} // end if

		} // end if
		
		return preSmallOpenLPositions;
	} // end method column2PreOpenL xx
	

	
	public static MoveSet preSmallOpenL_Columns_3_4_5_6(Node node, Move move, MoveType type){ 
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
						
						
						 preSmallOpenLPositions.add(new Move(row + 1, col - 1, player, type)); // down 1 left 1
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
						
						
						 preSmallOpenLPositions.add(new Move(row + 1, col - 1, player, type)); // down 1 left 1
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
						
						 preSmallOpenLPositions.add(new Move(row - 1, col - 1, player, type)); // up 1 left 1
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
						
						 preSmallOpenLPositions.add(new Move(row - 1, col - 1, player, type)); // up 1 left 1
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
						
						 preSmallOpenLPositions.add(new Move(row + 1, col - 1, player, type)); // down 1 left 1
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
						
						 preSmallOpenLPositions.add(new Move(row + 1, col - 1, player, type)); // down 1 left 1
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
						
						
						 preSmallOpenLPositions.add(new Move(row - 1, col - 1, player, type)); // up 1 left 1
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
						
						
						 preSmallOpenLPositions.add(new Move(row - 1, col - 1, player, type)); // up 1 left 1
					} // end else if

				} // end if
			} // end if
		} // end if 

		return preSmallOpenLPositions;

	} // end method
 // end method column6PreOpenL xx
	
	public static MoveSet getPreBigOpenLPositionsList(Node node){
		MoveSet preBigOpenLPositions = new MoveSet();
		int currentPlayer = 2/node.lastMove.player;
		int value;
		for(Move move : Node.moves){
			int row = move.row;
			int col = move.column;
			if(move.player == currentPlayer){ 
				value = 48;
			} else {
				value = 43;
			} // end else
			
			MoveType type = new MoveType(MoveType.Type.PRE_BIG_OPEN_L, value);
			
			if(row > 0 && row < 7 && col > 0 && col < 7){ // move not on perimeter
				if(col == 1){
					preBigOpenLPositions.addAll(preBigOpenL_Column_1(node, move, type));
				} // end if
				if(col == 2 || col == 3 || col == 4){
					preBigOpenLPositions.addAll(preBigOpenL_Columns_2_3_4(node, move, type));
				} // end if
//				System.out.println("1: " + preBigOpenLPositions.size());
//				for(Move myMove : preBigOpenLPositions){
//					System.out.print(myMove.moveString + ", ");
//				}
//				System.out.println();
				if(col == 3 || col == 4 || col == 5){
					preBigOpenLPositions.addAll(preBigOpenL_Columns_3_4_5(node, move, type));
				} // end if
//				System.out.println("2: " + preBigOpenLPositions.size());
//				for(Move myMove : preBigOpenLPositions){
//					System.out.print(myMove.moveString + ", ");
//				}
//				System.out.println();
				if(col == 6){
					preBigOpenLPositions.addAll(preBigOpenL_Column_6(node, move, type));
				} // end if
			 } // end if
		} // end for
		return preBigOpenLPositions;
	} // end method preOpenL
	
	public static MoveSet preBigOpenL_Column_1(Node node, Move move, MoveType type){
		
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
						
						
						 preBigOpenLPositions.add(new Move(row + 2, col + 2, player, type)); // down 2 right 2
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
						
						
						 preBigOpenLPositions.add(new Move(row + 2, col + 2, player, type)); // right 2 down 2
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
						
						
						 preBigOpenLPositions.add(new Move(row - 2, col + 2, player, type)); // up 2 right 2
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
						
						
						 preBigOpenLPositions.add(new Move(row - 2, col + 2, player, type)); // right 2 up 2
					} // end if
					
				} // end if	
			} // end if
		} // end if 

		return preBigOpenLPositions;
	} // end method column1PreOpenL
	
		public static MoveSet preBigOpenL_Columns_2_3_4(Node node, Move move, MoveType type){
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
						  
							
							preBigOpenLPositions.add(new Move(row + 2, col + 2, player, type)); // down 2 right 2
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
						  
							
							preBigOpenLPositions.add(new Move(row + 2, col + 2, player, type)); // down 2 right 2
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
					  
						
						preBigOpenLPositions.add(new Move(row - 2, col + 2, player, type)); // up 2 right 2
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
					  
						
						preBigOpenLPositions.add(new Move(row - 2, col + 2, player, type)); // up 2 right 2
					} // end if
				} // end if
			} // end if
		} // end if
		
		return preBigOpenLPositions;
	} // end method column2PreOpenL xx
	

		public static MoveSet preBigOpenL_Columns_3_4_5(Node node, Move move, MoveType type){
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
						  
							
							preBigOpenLPositions.add(new Move(row + 2, col - 2, player, type)); // down 2 left 2
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
						  
							
							preBigOpenLPositions.add(new Move(row + 2, col - 2, player, type)); // down 2 left 2
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
					  
						
						preBigOpenLPositions.add(new Move(row - 2, col - 2, player, type)); // up 2 left 2
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
					  
						
						preBigOpenLPositions.add(new Move(row - 2, col - 2, player, type)); // up 2 left 2
					} // end if
				} // end if
			} // end if
		} // end if

	return preBigOpenLPositions;
} // end method column5PreOpenL xx
	
	public static MoveSet preBigOpenL_Column_6(Node node, Move move, MoveType type){
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
						
						
						 preBigOpenLPositions.add(new Move(row + 2, col - 2, player, type)); // down 2 left 2
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
						
						
						 preBigOpenLPositions.add(new Move(row + 2, col - 2, player, type)); // left 2 down 2
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
						
						
						 preBigOpenLPositions.add(new Move(row - 2, col - 2, player, type)); // up 2 left 2
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
						
						
						 preBigOpenLPositions.add(new Move(row - 2, col - 2, player, type)); // left 2 up 2
					} // end if
					
				} // end if	
			} // end if
		} // end if 

		return preBigOpenLPositions;

	} // end method
 // end method column6PreOpenL xx
	
	
	private static MoveSet getThreeOfFourPositionsList(Node node){
		MoveSet threeOfFourPositions = new MoveSet();
		int value;
		int currentPlayer = 2/node.lastMove.player;
		for(Move move : Node.moves){
			int col = move.column;
			if(move.player == currentPlayer){ 
				value = 60;
			} else {
				value = 55;
			} // end else
			
			MoveType type = new MoveType(MoveType.Type.THREE_4, value);
			
			if(col == 0 || col == 1 || col == 2 || col == 3 || col == 4){
				threeOfFourPositions.addAll(threeOfFour_Columns_0_1_2_3_4(node, move, type));
			} else if(col == 1 || col == 2 || col == 3 || col == 4 || col == 5){
				threeOfFourPositions.addAll(threeOfFour_Columns_1_2_3_4_5(node, move, type));
			} else if(col == 2 || col == 3 || col == 4 || col == 5 || col == 6){
				threeOfFourPositions.addAll(threeOfFour_Columns_2_3_4_5_6(node, move, type));
			} // end if
			if(col == 3 || col == 4 || col == 5 || col == 6 || col == 7){
				threeOfFourPositions.addAll(threeOfFour_Columns_3_4_5_6_7(node, move, type));
			} // end if
			threeOfFourPositions.addAll(threeOfFour_AllColumns(node, move, type));
		} // end for
		return threeOfFourPositions;
	} // end method threeOfFour

	private static MoveSet threeOfFour_Columns_1_2_3_4_5(Node node, Move move, MoveType type) {
		MoveSet threeOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(board[row][col - 1] == i){ // left 1
			
		    if(board[row][col + 1] == 0 && // right 1
		       board[row][col + 2] == 0){  // right 2
				   
			    threeOfFourPositions.add(new Move(row, col + 1, player, type)); // right 1
				threeOfFourPositions.add(new Move(row, col + 2, player, type)); // right 2
		    } // end if
		} else if(board[row][col - 1] == 0){ // left 1
			
		    if(board[row][col + 1] == i && // right 1
		       board[row][col + 2] == 0){  // right 2
				   
			    threeOfFourPositions.add(new Move(row, col - 1, player, type)); // left 1
				threeOfFourPositions.add(new Move(row, col + 2, player, type)); // right 2
		    } else if(board[row][col + 1] == 0 && // right 1
				       board[row][col + 2] == i){  // right 2
				   
			    threeOfFourPositions.add(new Move(row, col - 1, player, type)); // left 1
				threeOfFourPositions.add(new Move(row, col + 1, player, type)); // right 1
		    } // end if
		} // end if
				
		return threeOfFourPositions;
	}

	private static MoveSet threeOfFour_Columns_2_3_4_5_6(Node node, Move move, MoveType type) {
		MoveSet threeOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;

		if(board[row][col + 1] == i){ // right 1
			
			if(board[row][col - 1] == 0 && // left 1
			   board[row][col - 2] == 0){  // left 2
				   
				threeOfFourPositions.add(new Move(row, col - 1, player, type)); // left 1
				threeOfFourPositions.add(new Move(row, col - 2, player, type)); // left 2
			} // end if
		} else if(board[row][col + 1] == 0){ // right 1
			
			if(board[row][col - 1] == i && // left 1
			   board[row][col - 2] == 0){  // left 2
				   
				threeOfFourPositions.add(new Move(row, col + 1, player, type)); // right 1
				threeOfFourPositions.add(new Move(row, col - 2, player, type)); // left 2
			} else if(board[row][col - 1] == 0 && // left 1
					   board[row][col - 2] == i){  // left 2
				   
				threeOfFourPositions.add(new Move(row, col + 1, player, type)); // right 1
				threeOfFourPositions.add(new Move(row, col - 1, player, type)); // left 1
			} // end if
		} // end if
				
		return threeOfFourPositions;

	}

	private static MoveSet threeOfFour_Columns_3_4_5_6_7(Node node, Move move, MoveType type) { // horizontal 3 of 4
		MoveSet threeOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		if(board[row][col - 1] == i){ 	   // left 1
			if(board[row][col - 2] == 0 && // left 2
			   board[row][col - 3] == 0){  // left 3
			
				threeOfFourPositions.add(new Move(row, col - 2, player, type)); // left 2
				threeOfFourPositions.add(new Move(row, col - 3, player, type)); // left 3
			} // end if
		} else if(board[row][col - 1] == 0){  // left 1
			
			   if(board[row][col - 2] == i && // left 2
				  board[row][col - 3] == 0){  // left 3
				   
					threeOfFourPositions.add(new Move(row, col - 1, player, type)); // left 1
					threeOfFourPositions.add(new Move(row, col - 3, player, type)); // left 3
			   } else if(board[row][col - 2] == 0 && // left 2
						 board[row][col - 3] == i){  // left 3
					
							threeOfFourPositions.add(new Move(row, col - 1, player, type)); // left 1
							threeOfFourPositions.add(new Move(row, col - 2, player, type)); // left 2
			   } // end if
		} // end else if

		return threeOfFourPositions;
	}

	private static MoveSet threeOfFour_Columns_0_1_2_3_4(Node node, Move move, MoveType type) { // horizontal 3 of 4
		MoveSet threeOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
	
		if(board[row][col + 1] == i){ 	   // right 1
			if(board[row][col + 2] == 0 && // right 2
			   board[row][col + 3] == 0){  // right 3
			
				threeOfFourPositions.add(new Move(row, col + 2, player, type)); // right 2
				threeOfFourPositions.add(new Move(row, col + 3, player, type)); // right 3
			} // end if
		} else if(board[row][col + 1] == 0){  // right 1
			
			   if(board[row][col + 2] == i && // right 2
				  board[row][col + 3] == 0){  // right 3
				   
				    threeOfFourPositions.add(new Move(row, col + 1, player, type)); // right 1
					threeOfFourPositions.add(new Move(row, col + 3, player, type)); // right 3
			   } else if(board[row][col + 2] == 0 && // right 2
						 board[row][col + 3] == i){  // right 3
					
				   			threeOfFourPositions.add(new Move(row, col + 1, player, type)); // right 1
							threeOfFourPositions.add(new Move(row, col + 2, player, type)); // right 2
			   } // end if
		} // end else if

		return threeOfFourPositions;
	} // end method 
	
	private static MoveSet threeOfFour_AllColumns(Node node, Move move, MoveType type) { // vertical 3 of 4
		MoveSet threeOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int i = move.player;
		int row = move.row;
		int col = move.column;
		
		//System.out.println(move.moveString + ": ");
		if(row == 0 || row == 1 || row == 2 || row == 3 || row == 4){
			
			if(board[row + 1][col] == i){ 	   // down 1
				
				if(board[row + 2][col] == 0 && // down 2
				   board[row + 3][col] == 0){  // down 3
				
					threeOfFourPositions.add(new Move(row + 2, col, player, type)); // down 2
					threeOfFourPositions.add(new Move(row + 3, col, player, type)); // down 3
				} // end if
			} else if(board[row + 1][col] == 0){  // down 1
				
				   if(board[row + 2][col] == i && // down 2
					  board[row + 3][col] == 0){  // down 3
				
					   threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
					   threeOfFourPositions.add(new Move(row + 3, col, player, type)); // down 3
				   } else if(board[row + 2][col] == 0 && // down 2
						     board[row + 3][col] == i){  // down 3
				
					   threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
					   threeOfFourPositions.add(new Move(row + 2, col, player, type)); // down 2
				   } // end else if
			} // end else if
//			System.out.println("1: " + threeOfFourPositions.size());
		} // end if
		
		if(row == 1 || row == 2 || row == 3 || row == 4){
			if(board[row - 1][col] == i){ // up 1
				if(board[row + 1][col] == 0 && // down 1
			       board[row + 2][col] == 0){  // down 2
				
			    	threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
			    	threeOfFourPositions.add(new Move(row + 2, col, player, type)); // down 2
				} // end if
			} else if(board[row - 1][col] == 0){ // up 1
				
				if(board[row + 1][col] == i && // down 1
			       board[row + 2][col] == 0){  // down 2
				
			    	threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
			    	threeOfFourPositions.add(new Move(row + 2, col, player, type)); // down 2
				} else if(board[row + 1][col] == 0 && // down 1
					      board[row + 2][col] == i){  // down 2
					
							threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
					    	threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
				} // end else if
			} // end else if
			
			if(board[row + 1][col] == i){	   // down 1
			    if(board[row + 2][col] == 0 && // down 2
			       board[row + 3][col] == 0){  // down 3
				
			    	threeOfFourPositions.add(new Move(row + 2, col, player, type)); // down 2
			    	threeOfFourPositions.add(new Move(row + 3, col, player, type)); // down 3
			    } // end if
			} else if(board[row + 1][col] == 0){  // down 1
				
				   if(board[row + 2][col] == i && // down 2
					  board[row + 3][col] == 0){  // down 3
				
						threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
						threeOfFourPositions.add(new Move(row + 3, col, player, type)); // down 3
				   } else if(board[row + 2][col] == 0 && // down 2
							 board[row + 3][col] == i){  // down 3
						
					    threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
					    threeOfFourPositions.add(new Move(row + 2, col, player, type)); // down 2
				   } // end else if
			} // end else if
//			System.out.println("2: " + threeOfFourPositions.size());
//			for(Move myMove : threeOfFourPositions){
//				System.out.print(myMove.moveString + ", ");
//			}
//			System.out.println();
		} // end if row
		
		if(row == 2 || row == 3 || row == 4){
			
			if(board[row + 1][col] == i){ 	   // down 1
				
				if(board[row + 2][col] == 0 && // down 2
				   board[row + 3][col] == 0){  // down 3
						
						threeOfFourPositions.add(new Move(row + 2, col, player, type)); // down 2
						threeOfFourPositions.add(new Move(row + 3, col, player, type)); // down 3
				} 
				if(board[row - 1][col] == 0){     // up 1 
					if(board[row - 2][col] == 0){ // up 2 
						
						threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
						threeOfFourPositions.add(new Move(row - 2, col, player, type)); // up 2
					} // end if
					if(board[row + 2][col] == 0){ // down 2 
						
						threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
						threeOfFourPositions.add(new Move(row + 2, col, player, type)); // down 2
					}
				} // end if
			} else if(board[row + 1][col] == 0){  // down 1
				
				   if(board[row + 2][col] == i && // down 2
					  board[row + 3][col] == 0){  // down 3
					
						threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
						threeOfFourPositions.add(new Move(row + 3, col, player, type)); // down 3
				   } else if(board[row + 2][col] == 0 && // down 2
							 board[row + 3][col] == i){  // down 3
						
						threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
						threeOfFourPositions.add(new Move(row + 2, col, player, type)); // down 2
				   } // end else if
				   
				   if(board[row - 1][col] == i){  // up 1 
						if(board[row - 2][col] == 0){ // up 2 
							
							threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
							threeOfFourPositions.add(new Move(row - 2, col, player, type)); // up 2
						} // end if
						if(board[row + 2][col] == 0){ // down 2 
							
							threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
							threeOfFourPositions.add(new Move(row + 2, col, player, type)); // down 2
						}
					} else if(board[row - 1][col] == 0){  // up 1 
						if(board[row - 2][col] == i){ // up 2 
							
							threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
							threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
						} // end if
						if(board[row + 2][col] == i){ // down 2 
							
							threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
							threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
						}
					} // end if
			} // end else if
//			System.out.println("3: " + threeOfFourPositions.size());
//			for(Move myMove : threeOfFourPositions){
//				System.out.print(myMove.moveString + ", ");
//			}
//			System.out.println();
		} // end if
		
		if(row == 3 || row == 4 || row == 5){ // row 3-5
			
			if(board[row - 1][col] == i){ 	   // up 1
				
				if(board[row - 2][col] == 0 && // up 2
				   board[row - 3][col] == 0){  // up 3
						
						threeOfFourPositions.add(new Move(row - 2, col, player, type)); // up 2
						threeOfFourPositions.add(new Move(row - 3, col, player, type)); // up 3
				} 
				if(board[row + 1][col] == 0){     // down 1 
					if(board[row + 2][col] == 0){ // down 2 
						
						threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
						threeOfFourPositions.add(new Move(row + 2, col, player, type)); // down 2
					} // end if
					if(board[row - 2][col] == 0){ // up 2 
						
						threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
						threeOfFourPositions.add(new Move(row - 2, col, player, type)); // up 2
					}
				} // end if
			} else if(board[row - 1][col] == 0){  // up 1
				
				   if(board[row - 2][col] == i && // up 2
					  board[row - 3][col] == 0){  // up 3
					
						threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
						threeOfFourPositions.add(new Move(row - 3, col, player, type)); // up 3
				   } else if(board[row - 2][col] == 0 && // up 2
							 board[row - 3][col] == i){  // up 3
						
						threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
						threeOfFourPositions.add(new Move(row - 2, col, player, type)); // up 2
				   } // end else if
				   
				   if(board[row + 1][col] == i){  // down 1 
						if(board[row + 2][col] == 0){ // down 2 
							
							threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
							threeOfFourPositions.add(new Move(row + 2, col, player, type)); // down 2
						} // end if
						if(board[row - 2][col] == 0){ // up 2 
							
							threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
							threeOfFourPositions.add(new Move(row - 2, col, player, type)); // up 2
						}
					} else if(board[row + 1][col] == 0){  // down 1 
						if(board[row + 2][col] == i){ // down 2 
							
							threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
							threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
						} // end if
						if(board[row - 2][col] == i){ // up 2 
							
							threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
							threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
						}
					} // end if
			} // end else if
//			System.out.println("4: " + threeOfFourPositions.size());
//			for(Move myMove : threeOfFourPositions){
//				System.out.print(myMove.moveString + ", ");
//			}
//			System.out.println();
		} // end if
		
		if(row == 3 || row == 4 || row == 5 || row == 6){ // row 3-6
			
			if(board[row + 1][col] == i){ // down 1
				if(board[row - 1][col] == 0 && // up 1
				   board[row - 2][col] == 0){  // up 2
				
					threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
					threeOfFourPositions.add(new Move(row - 2, col, player, type)); // up 2
				} // end if
			} else if(board[row + 1][col] == 0){ // down 1
				
				if(board[row - 1][col] == i && // up 1
				   board[row - 2][col] == 0){  // up 2
				
					threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
					threeOfFourPositions.add(new Move(row - 2, col, player, type)); // up 2
				} else if(board[row - 1][col] == 0 && // up 1
						  board[row - 2][col] == i){  // up 2
					
							threeOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
							threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
				} // end else if
			} // end else if
			
			if(board[row - 1][col] == i){	   // up 1
				if(board[row - 2][col] == 0 && // up 2
				   board[row - 3][col] == 0){  // up 3
				
					threeOfFourPositions.add(new Move(row - 2, col, player, type)); // up 2
					threeOfFourPositions.add(new Move(row - 3, col, player, type)); // up 3
				} // end if
			} else if(board[row - 1][col] == 0){  // up 1
				
				   if(board[row - 2][col] == i && // up 2
					  board[row - 3][col] == 0){  // up 3
				
						threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
						threeOfFourPositions.add(new Move(row - 3, col, player, type)); // up 3
				   } else if(board[row - 2][col] == 0 && // up 2
							 board[row - 3][col] == i){  // up 3
						
						threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
						threeOfFourPositions.add(new Move(row - 2, col, player, type)); // up 2
				   } // end else if
			} // end else if
//			System.out.println("5: " + threeOfFourPositions.size());
//			for(Move myMove : threeOfFourPositions){
//				System.out.print(myMove.moveString + ", ");
//			}
//			System.out.println();
		} // end if
		
		if(row == 3 || row == 4 || row == 5 || row == 6 || row == 7){ // rows 3-7 columns 0-4
			if(board[row - 1][col] == i){ 	   // up 1
				
				if(board[row - 2][col] == 0 && // up 2
				   board[row - 3][col] == 0){  // up 3
				
					threeOfFourPositions.add(new Move(row - 2, col, player, type)); // up 2
					threeOfFourPositions.add(new Move(row - 3, col, player, type)); // up 3
				} // end if
			} else if(board[row - 1][col] == 0){  // up 1
				
				   if(board[row - 2][col] == i && // up 2
					  board[row - 3][col] == 0){  // up 3
				
					   threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
					   threeOfFourPositions.add(new Move(row - 3, col, player, type)); // up 3
				   } else if(board[row - 2][col] == 0 && // up 2
							 board[row - 3][col] == i){  // up 3
				
					   threeOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
					   threeOfFourPositions.add(new Move(row - 2, col, player, type)); // up 2
				   } // end else if
			} // end else if
//			System.out.println("6: " + threeOfFourPositions.size());
//			for(Move myMove : threeOfFourPositions){
//				System.out.print(myMove.moveString + ", ");
//			}
//			System.out.println();
		} // end if
//		if(threeOfFourPositions.size() > 0)
//		System.out.println(threeOfFourPositions.get(threeOfFourPositions.size() - 1).moveString + ", value: " + type.value);
		
		return threeOfFourPositions;
	}

	@SuppressWarnings("unused")
	private static MoveSet getTwoOfFourPositionsList(Node node){ // two of four, with possible open 3 next move
		MoveSet twoOfFourPositions = new MoveSet();
		int value;
		int currentPlayer = 2/node.lastMove.player;
		for(Move move : Node.moves){
			int row = move.row;
			int col = move.column;
			if(move.player == currentPlayer){ 
				value = 40;
			} else {
				value = 35;
			} // end else
			
			MoveType type = new MoveType(MoveType.Type.TWO_4, value);
			
			if(col == 0 || col == 1 || col == 2 || col == 3 || col == 4){
				twoOfFourPositions.addAll(twoOfFour_Columns_0_1_2_3_4(node, move, type));
			} else if(col == 1 || col == 2 || col == 3 || col == 4 || col == 5){
				twoOfFourPositions.addAll(twoOfFour_Columns_1_2_3_4_5(node, move, type));
			} else if(col == 2 || col == 3 || col == 4 || col == 5 || col == 6){
				twoOfFourPositions.addAll(twoOfFour_Columns_2_3_4_5_6(node, move, type));
			} // end if
			if(col == 3 || col == 4 || col == 5 || col == 6 || col == 7){
				twoOfFourPositions.addAll(twoOfFour_Columns_3_4_5_6_7(node, move, type));
			} // end if
			twoOfFourPositions.addAll(twoOfFour_AllColumns(node, move, type));
		} // end for
		return twoOfFourPositions;
	} // end method
	
	private static MoveSet twoOfFour_AllColumns(Node node, Move move, MoveType type) { // vertical 2 of 4
		
		MoveSet twoOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = move.row;
		int col = move.column;
		
		if(row == 0 || row == 1 || row == 2 || row == 3 || row == 4){
			
			if(board[row + 1][col] == 0 && // down 1
			   board[row + 2][col] == 0 && // down 2
			   board[row + 3][col] == 0){  // down 3
				
				twoOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
				twoOfFourPositions.add(new Move(row + 2, col, player, type)); // down 2
				twoOfFourPositions.add(new Move(row + 3, col, player, type)); // down 3
			} // end if
		} // end if
		
		if(row == 1 || row == 2 || row == 3 || row == 4 || row == 5){
			
			if(board[row - 1][col] == 0 && // up 1
			   board[row + 1][col] == 0 && // down 1
			   board[row + 2][col] == 0){  // down 2
				
				twoOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
		    	twoOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
		    	twoOfFourPositions.add(new Move(row + 2, col, player, type)); // down 2
			} // end if
		} // end if
		
		if(row == 2 || row == 3 || row == 4 || row == 5 || row == 6){ // row 2-6
			if(board[row - 1][col] == 0 && // up 1
			   board[row - 2][col] == 0 && // up 2
			   board[row + 1][col] == 0){  // down 1
				
				twoOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
				twoOfFourPositions.add(new Move(row - 2, col, player, type)); // up 2
				twoOfFourPositions.add(new Move(row + 1, col, player, type)); // down 1
			} // end if
		} // end if
		
		if(row == 3 || row == 4 || row == 5 || row == 6 || row == 7){ // rows 3-7
			if(board[row - 1][col] == 0 && // up 1
			   board[row - 2][col] == 0 && // up 2
			   board[row - 3][col] == 0){  // up 3
				
				twoOfFourPositions.add(new Move(row - 1, col, player, type)); // up 1
				twoOfFourPositions.add(new Move(row - 2, col, player, type)); // up 2
				twoOfFourPositions.add(new Move(row - 3, col, player, type)); // up 3
			} // end if
		} // end if
		
		return twoOfFourPositions;
	} // end method

	private static MoveSet twoOfFour_Columns_0_1_2_3_4(Node node, Move move, MoveType type) {

		MoveSet twoOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = move.row;
		int col = move.column;
	
		if(board[row][col + 1] == 0 && // right 1
		   board[row][col + 2] == 0 && // right 2
		   board[row][col + 3] == 0){  // right 3
		    	
			twoOfFourPositions.add(new Move(row, col + 1, player, type)); // right 1
	    	twoOfFourPositions.add(new Move(row, col + 2, player, type)); // right 2
	    	twoOfFourPositions.add(new Move(row, col + 3, player, type)); // right 3
	    } 
		
		return twoOfFourPositions;	
	} // end method

	private static MoveSet twoOfFour_Columns_1_2_3_4_5(Node node, Move move, MoveType type) {
		MoveSet twoOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = move.row;
		int col = move.column;
		
		if(board[row][col - 1] == 0 && // left 1
		   board[row][col + 1] == 0 && // right 1
		   board[row][col + 2] == 0){  // right 2
				   
			    twoOfFourPositions.add(new Move(row, col - 1, player, type)); // left 1
			    twoOfFourPositions.add(new Move(row, col + 1, player, type)); // right 1
				twoOfFourPositions.add(new Move(row, col + 2, player, type)); // right 2
	    } // end if

		return twoOfFourPositions;
	} // end method 
		
	private static MoveSet twoOfFour_Columns_2_3_4_5_6(Node node, Move move, MoveType type) {
		MoveSet twoOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = move.row;
		int col = move.column;

		if(board[row][col + 1] == 0 && // right 1
		   board[row][col - 1] == 0 && // left 1
		   board[row][col - 2] == 0){  // left 2
				   
				twoOfFourPositions.add(new Move(row, col + 1, player, type)); // right 1
				twoOfFourPositions.add(new Move(row, col - 1, player, type)); // left 1
				twoOfFourPositions.add(new Move(row, col - 2, player, type)); // left 2
		} // end if

		return twoOfFourPositions;
	} // end method
	
	private static MoveSet twoOfFour_Columns_3_4_5_6_7(Node node, Move move, MoveType type) {
		MoveSet twoOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = move.row;
		int col = move.column;
		
		if(board[row][col - 1] == 0 && // left 1
		   board[row][col - 2] == 0 && // left 2
		   board[row][col - 3] == 0){  // left 3
				
			twoOfFourPositions.add(new Move(row, col - 1, player, type)); // left 1
			twoOfFourPositions.add(new Move(row, col - 2, player, type)); // left 2
			twoOfFourPositions.add(new Move(row, col - 3, player, type)); // left 3
		} 

		return twoOfFourPositions;
	} // end method
	
	private static MoveSet getOneOfFourPositionsList(Node node, MoveSet zeros){ // one move, with possible open 3 in 2 moves
		MoveSet oneOfFourPositions = new MoveSet();
		int currentPlayer = 2/node.lastMove.player;
		int value;
		for(Move move : zeros){
			int col = move.column;
			if(move.player == currentPlayer){ 
				value = 30;
			} else {
				value = 25;
			} // end else
			
			MoveType type = new MoveType(MoveType.Type.ZEROS, value);
			
			if(col == 0 || col == 1 || col == 2 || col == 3 || col == 4){
				oneOfFourPositions.addAll(oneOfFour_Columns_0_1_2_3_4(node, move, type));
			} else if(col == 1 || col == 2 || col == 3 || col == 4 || col == 5){
				oneOfFourPositions.addAll(oneOfFour_Columns_1_2_3_4_5(node, move, type));
			} else if(col == 2 || col == 3 || col == 4 || col == 5 || col == 6){
				oneOfFourPositions.addAll(oneOfFour_Columns_2_3_4_5_6(node, move, type));
			} // end if
			if(col == 3 || col == 4 || col == 5 || col == 6 || col == 7){
				oneOfFourPositions.addAll(oneOfFour_Columns_3_4_5_6_7(node, move, type));
			} // end if
		} // end for
		return oneOfFourPositions;
} // end method oneOfFour
	
	private static MoveSet oneOfFour_Columns_0_1_2_3_4(Node node, Move openMove, MoveType type) {
		MoveSet oneOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = openMove.row;
		int col = openMove.column;
	
		if(board[row][col + 1] == 0 && // right 1
		   board[row][col + 2] == 0 && // right 2
		   board[row][col + 3] == 0){  // right 3
			oneOfFourPositions.add(new Move(row, col, player, type));
		} // end if
		
		if(row == 0 || row == 1 || row == 2 || row == 3 || row == 4){
			if(board[row + 1][col] == 0 && // down 1
			   board[row + 2][col] == 0 && // down 2
			   board[row + 3][col] == 0){  // down 3
				
				oneOfFourPositions.add(new Move(row, col, player, type));
			} // end if
		} if(row == 1 || row == 2 || row == 3 || row == 4){
			if(board[row + 1][col] == 0 && // down 1
			   board[row + 2][col] == 0 && // down 2
			  (board[row - 1][col] == 0 || // up 1
			   board[row + 3][col] == 0)){ // down 3
				
				oneOfFourPositions.add(new Move(row, col, player, type));
			} // end if
		} // end if
		if(row == 2 || row == 3 || row == 4){
			if(board[row + 1][col] == 0){ // down 1
				if(board[row + 2][col] == 0){// down 2
					if(board[row - 1][col] == 0 || // up 1
					   board[row + 3][col] == 0){ // down 3
						
						oneOfFourPositions.add(new Move(row, col, player, type));
					   } // end if
				} else if(board[row - 1][col] == 0 && // up 1 
						  board[row - 2][col] == 0){   // up 2 

					oneOfFourPositions.add(new Move(row, col, player, type));
				} // end else if
			} // end if
		} // end if
		if(row == 3 || row == 4 || row == 5){ // row 3 or 4 column 0
			if(board[row - 1][col] == 0){ // up 1
				if(board[row - 2][col] == 0){// up 2
					if(board[row + 1][col] == 0 || // down 1
					   board[row - 3][col] == 0){ // up 3
						
						oneOfFourPositions.add(new Move(row, col, player, type));
					   } // end if
				} else if(board[row + 1][col] == 0 && // down 1 
						  board[row + 2][col] == 0){   // down 2 

					oneOfFourPositions.add(new Move(row, col, player, type));
				} // end else if
			} // end if
		} // end if
		if(row == 3 || row == 4 || row == 5 || row == 6){ 
			if(board[row - 1][col] == 0 && // up 1
			   board[row - 2][col] == 0 && // up 2
			  (board[row + 1][col] == 0 || // down 1
			   board[row - 3][col] == 0)){ // up 3
				
				oneOfFourPositions.add(new Move(row, col, player, type));
			} // end if
		} // end if
		if(row == 3 || row == 4 || row == 5 || row == 6 || row == 7){ // row 3 or 4 column 0
			if(board[row - 1][col] == 0 && // up 1
			   board[row - 2][col] == 0 && // up 2
			   board[row - 3][col] == 0){  // up 3
				
				oneOfFourPositions.add(new Move(row, col, player, type));
			} // end if
		} // end if

		return oneOfFourPositions;
	} // end method 
	
	private static MoveSet oneOfFour_Columns_1_2_3_4_5(Node node, Move openMove, MoveType type) {
		MoveSet oneOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = openMove.row;
		int col = openMove.column;
	
		if(board[row][col - 1] == 0 && // left 1
		   board[row][col + 1] == 0 && // right 1
		   board[row][col + 2] == 0){  // right 2
		   
			oneOfFourPositions.add(new Move(row, col, player, type));
		} // end if
		
		return oneOfFourPositions;
	} // end method 
	
	private static MoveSet oneOfFour_Columns_2_3_4_5_6(Node node, Move openMove, MoveType type) {
		MoveSet oneOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = openMove.row;
		int col = openMove.column;
	
		if(board[row][col - 1] == 0 && // left 1
		   board[row][col - 2] == 0 && // left 2
		   board[row][col + 1] == 0){  // right 1
		   
			oneOfFourPositions.add(new Move(row, col, player, type));
		} // end if
		
		return oneOfFourPositions;
	} // end method
	
	private static MoveSet oneOfFour_Columns_3_4_5_6_7(Node node, Move openMove, MoveType type) {
		MoveSet oneOfFourPositions = new MoveSet();
		int[][] board = node.board;
		int player = 2/node.lastMove.player;
		int row = openMove.row;
		int col = openMove.column;
	
		if(board[row][col - 1] == 0 && // left 1
		   board[row][col - 2] == 0 && // left 2
		   board[row][col - 3] == 0){  // left 3
		   
			oneOfFourPositions.add(new Move(row, col, player, type));
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

	private static boolean gameOver(Node root) {
		if(possibleConnect4(root.board, root.lastMove)){
			root.printBoard();
			printWinner(root.lastMove.player);
			System.exit(0);
		} // end if
		if(root.getNumEmptySpaces() == 0){
			
			if(root.lastMove.player != 1){
				root.printBoard();
			}
			if(root.getNumEmptySpaces() == 0){ // indexOutOfBounds exception at last move
				System.out.println("-Draw!");
				System.exit(0);
			} // end if	
		} // end if
		return false;
	} // end method gameOver

	@SuppressWarnings("unused")
	private static String getMoveString(Move move) {
		return getMoveString(move.row, move.column);
	} // end method getMoveString
	
	private static String getMoveString(int row, int column) {
		String move = getRow(row) + (column + 1);
		return move;
	} // end method getMoveString

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
