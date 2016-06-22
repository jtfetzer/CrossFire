import java.util.ArrayList;
import java.util.Scanner;

public class Connect4 {

	static int[][] board = new int[8][8];
	static int computeTime = 0;
	static Move lastMove;
	static int next;
	static int current;
	static boolean connect4 = false;
	static int computer = 2;
	static int human = 1;
	static int plies = 1; // Max depth of search
	static ArrayList<Move> moves = new ArrayList<>();
	static ArrayList<Move> connect4Position = new ArrayList<>();
	static ArrayList<Move> possibleOpen3 = new ArrayList<>();
	static ArrayList<Move> smallOpenL = new ArrayList<>();
	static Move maxBlocks;
	static String playFirst;
	static Scanner in = new Scanner(System.in);
	static Scanner input;
	
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		
		System.out.print("Would you like to go first? (y/n): ");
		playFirst = in.next().toLowerCase();
		if(playFirst.equals("y") ){
			current = human;
		} else {
			current = computer;
		} // end else
		
		do{
			System.out.print("How long should the computer think about its moves (in seconds)?: ");
			computeTime = in.nextInt();
			if(computeTime <= 0) System.out.println("Time must be greater than 0.\n");
		} while (computeTime <= 0);
		
		playGame(playFirst);
		in.close();
	} // end method main
	
	private static void playGame(String playFirst) {
		String taken;
		boolean valid1 = true, valid2;
		int row = 0, column = 0;
		
		if(playFirst.equals("n")){
			board[3][4] = current();
			lastMove = new Move(3,4,1, Player.COMPUTER);
			moves.add(lastMove);
			printBoard();
			System.out.println("\n  My current move is: " + getRow(lastMove.row) + (lastMove.column + 1));
		} else {
			printBoard();
		}
		while( valid1){
			valid2 = true;
			while(valid2){ // user move
				if(moves.size() >= board.length * board.length){
					System.out.println("Draw!");
					System.exit(0);
				} // end if						
				input = new Scanner(validate());
				Move move = new Move(input.nextInt(),input.nextInt(),input.nextInt(),getPlayer(current));
				row = move.row;
				column = move.column;
				if(board[row][column] == 0){ // move is not taken
					board[row][column] = current();
					lastMove = move;
					moves.add(lastMove);
					isConnect4(row, column);
					valid2 = false;
				} else {
					taken = getLastMove(row, column);
					System.out.println("  ~ " + taken + " is already taken.");
					valid2 = true;
				} // end else
			} // end while
			if(!gameOver()){
				computerMove(); // computer move
				printBoard();
				if(lastMove != null) {
					if(moves.size() >= board.length * board.length){
						System.out.println("Draw!");
						System.exit(0);
					} // end if				
					System.out.println("\n  My current move is: " + getRow(lastMove.row) + (lastMove.column + 1));
				} // end if
				if(connect4){
					printBoard();
					printWinner();
					valid1 = false;
				} // end if
			} else {
				printBoard();
				printWinner();
				valid1 = false;
			} // end else
		} // end while
	} // end method playGame
	
	private static int current() { // returns the current value and increments next;
		int temp = current;
		if(current == computer){
			current = human;
		} else {
			current = computer;
		} // end else
		return temp;
	} // end method current

	private static String validate() {
		int row = 0, column = 0;
		boolean error = true;
		String move;
		if(moves.size() == board.length * board.length - 1){
			if(playFirst.equals("y")){
				Move finalMove = getAnyAvailableMove(Player.COMPUTER);
				board[finalMove.row][finalMove.column] = getPlayerId(finalMove.player);
				printBoard();
			} else {
				Move finalMove = getAnyAvailableMove(Player.HUMAN);
				board[finalMove.row][finalMove.column] = getPlayerId(finalMove.player);
				printBoard();
			} // end else
			if(connect4){
				printWinner();
				System.exit(0);
			} else {
				System.out.println("Draw!");
				System.exit(0);
			}
		} // end if
		while(error) {
			System.out.print("\n  Choose your next move: ");
			move = in.nextLine();
			row = getRow(String.valueOf(move.charAt(0)).toLowerCase());
			column = Integer.valueOf(String.valueOf(move.charAt(1))) - 1;
			if(row > 7 || column > 7){
				System.out.println("  Invalid entry, try again.");
			} else {
				error = false;
			} // end else
		} // end while
		return row + " " + column  + " " +  1;
	} // end method vaidate

	private static Player getPlayer(int current) {
		if(current == human){
			return Player.HUMAN;
		} else {
			return Player.COMPUTER;
		} // end else
	} // end method getPlayer

	@SuppressWarnings("unused")
	private static boolean isConnect4(Move move) {
		return isConnect4(move.row, move.column);
	} // end method isConnect4

	public static void possible4inRow(Move move) { // check is one more move can give a 4 in a row.
		int value;
		int playerID = getPlayerId(move.player);
		if(moves.size() < 5) return;
		if(computer == getPlayerId(move.player)){ // human made last move, i.e. this is a block
			value = 100;
		} else {
			value = 95;
		} // end else
		 
		for (int i = 2; i > 0; i--) {
			if(move.row - 1 >= 0){
				if(board[move.row - 1][move.column] == 0){
					board[move.row - 1][move.column] = i;
					if(isConnect4(move.row - 1, move.column)){
						board[move.row - 1][move.column] = 0; // undo test move
						connect4Position.add(new Move(move.row - 1, move.column, value, getPlayer(playerID)));
						// return true;
					} // end if
					board[move.row - 1][move.column] = 0;
				} // end if
			} 
			if(move.row + 1 < board.length){
				if(board[move.row + 1][move.column] == 0){
					board[move.row + 1][move.column] = i;
					if(isConnect4(move.row + 1, move.column)){
						board[move.row + 1][move.column] = 0;
						connect4Position.add(new Move(move.row + 1, move.column, value, getPlayer(playerID)));
						// return true;
					} // end if
					board[move.row + 1][move.column] = 0;
				} // end if
			} 
			if(move.column - 1 >= 0) {
				if(board[move.row][move.column - 1] == 0){
					board[move.row][move.column - 1] = i;
					if(isConnect4(move.row, move.column - 1)){
						board[move.row][move.column - 1] = 0;
						connect4Position.add(new Move(move.row, move.column - 1, value, getPlayer(playerID)));
						// return true;
					} // end if
					board[move.row][move.column - 1] = 0;
				} // end if
			} 
			if(move.column + 1 < board.length){
				if(board[move.row][move.column + 1] == 0){
					board[move.row][move.column + 1] = i;
					if(isConnect4(move.row, move.column + 1)){
						board[move.row][move.column + 1] = 0;
						connect4Position.add(new Move(move.row, move.column + 1, value, getPlayer(playerID)));
						// return true;
					} // end else if
					board[move.row][move.column + 1] = 0;
				}// end if
			} // end else if
		} // end for i
		// return false;
	} // end method connect4
	
	private static boolean isConnect4(int row, int column) {
		int player = board[row][column];
		connect4 = false;
		if(player != 0){
			int i = -1, j = 1;
			while((row + i) >= 0 && (board[row + i][column] == player)){ // check left
				--i;
			} // end while
			i += 1;
			
			while((row + j) < board.length && (board[row + j][column] == player)){ // check right
				++j;
			} // end while
			j -= 1;
			if(j + 1 - i >= 4){
				connect4 = true;
			}
			
			i = -1;
			j = 1;
			while((column + i) >= 0 && (board[row][column + i] == player)){ // check up
				--i;
			} // end while
			i += 1;
			
			while((column + j) < board.length && (board[row][column + j] == player)){ // check down
				++j;
			} // end while
			j -= 1;
			if(j + 1 - i >= 4){
				connect4 = true;
			} // end if
		} else{
			System.out.println("PLAYER = 0");
		}
		return connect4;
	} // end method connect4
	
	private static void printWinner() {
		if(getPlayerId(lastMove.player) == computer){
			System.out.println("\n  I win!");
		} else{
			System.out.println("\n  You win!");
		} // end else
	} // end method printWinner

	private static boolean gameOver() {
		if(connect4){
			return true;
		} // end if
		return false;
	} // end method gameOver

	private static String getLastMove(int row, int column) {
		String move = getRow(row) + (column + 1);
		return move;
	} // end method getLastMove

	static int getRow(String next) {
		int row;
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
			default : row = 9;
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

	private static void computerMove() {
		int row = 0, column = 0;
		lastMove = getBestMove(current);
		if(lastMove != null){ 
			row = lastMove.row;
			column = lastMove.column;
			board[row][column] = current();
			moves.add(lastMove);
			isConnect4(row, column);
		} // end if
	} // end method computerMove

	private static Move getBestMove(int player){ // Best play for second player

		int[][] tempBoard = new int[8][8];
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				tempBoard[i][j] = board[i][j];
			} // end for j
		} // end for i
		tempBoard[lastMove.row][lastMove.column] = player;
		Node root = new Node(tempBoard);				// Evaluate best moves using minimax and 
		root.move = lastMove;							// alpha-beta pruning up to "plies" depth.
		ArrayList<Move> bestMoves;						// Add best move as child node. 
		for (int i = 0; i < moves.size(); i++) {		
			bestMoves = possibilities(tempBoard, moves.get(i), player);	 
			for(Move best: bestMoves){		
				Move next = new Move(best.row, best.column, best.value, getPlayer(player));
				root.add(next, player, getPlayer(player));	// only checks one ply right now for each move
			} // end for
		} // end for i
		return  evaluate(root, plies);// plies is a global variable, and refers to the depth of the search
	} // end getBestMove
	
	public static ArrayList<Move> possibilities(int[][] board, Move move, int player) {
		ArrayList<Move> best = new ArrayList<Move>();
			possible4inRow(move);
			if(connect4Position.size() > 0) {
//				Move bestMove = new Move(connect4Position.row, connect4Position.column, connect4Position.value, getPlayer(player));
				for (int i = 0; i < connect4Position.size(); i++) {
					best.add(connect4Position.remove(i));
				} // end for i
//				best.add(bestMove);	// complete or block the formation of a 4 in a row
				return best;
			} 
			possibleOpen3inRow(move);
			if(possibleOpen3.size() > 0){
				for (int i = 0; i < possibleOpen3.size(); i++) {
					best.add(possibleOpen3.remove(i));
				} // end for i
			} // end if
			else { // supposed to be only for first response
				if(moves.size() < board.length * board.length - 1){
					Move next = blockMostSpace(move);
					best.add(next);
				} // end if
			} // end else
		return best;
	} // end method possibilities
	
	private static void possibleOpen3inRow(Move move) {
		int value;
		int playerId = getPlayerId(move.player);
		if(current == playerId){
			value = 90;
		} else {
			value = 85;
		} // end else
		if(move.column == 1){	// row 0, column 1
			if(board[move.row][move.column - 1] == 0){ // vertical open 3
				if(board[move.row][move.column + 1] == 0){
					if(board[move.row][move.column + 2] == playerId){
						if(board[move.row][move.column + 3] == 0){
							possibleOpen3.add( new Move(move.row, move.column + 1, value, getPlayer(current)));
							// return true;
						} // end if
					} // end if
				} // end if
			} // end if
		} // end if
		else if(move.column == 2){
			if(board[move.row][move.column - 1] == 0){
				if(board[move.row][move.column + 1] == 0){
					if(board[move.row][move.column + 2] == playerId){
						if(board[move.row][move.column + 3] == 0){
							possibleOpen3.add(new Move(move.row, move.column + 1, value, getPlayer(current)));
							// return true;
						} // end if
					} // end if
				} // end if
			} // end if
			if(board[move.row][move.column - 1] == 0){
				if(board[move.row][move.column + 1] == playerId){
					if(board[move.row][move.column + 2] == 0){
						possibleOpen3.add(new Move(move.row, move.column + 2, value, getPlayer(current)));
						// return true;
					} // end if
				} // end if
			} // end if
		} // end if
		else if(move.column == 3 || move.column == 4){
			if(board[move.row][move.column - 1] == 0){
				if(board[move.row][move.column + 1] == 0){
					if(board[move.row][move.column + 2] == playerId){
						if(board[move.row][move.column + 3] == 0){
							possibleOpen3.add(new Move(move.row, move.column + 1, value, getPlayer(current)));
							// return true;
						} // end if
					} // end if
					if(board[move.row][move.column - 2] == playerId){ // problems with horizontal right to left
						if(board[move.row][move.column - 3] == 0){
							possibleOpen3.add(new Move(move.row, move.column - 1, value, getPlayer(current)));
							// return true;
						} // end else if
					} // end if
					} // end if
				} // end if
				if(board[move.row][move.column + 1] == 0){
					if(board[move.row][move.column - 1] == playerId){
						if(board[move.row][move.column - 2] == 0){
							possibleOpen3.add(new Move(move.row, move.column + 1, value, getPlayer(current)));
							// return true;
						} // end if
					} // end if
				} // end if
				if(board[move.row][move.column - 1] == 0){
					if(board[move.row][move.column + 1] == playerId){
						if(board[move.row][move.column + 2] == 0){
							possibleOpen3.add(new Move(move.row, move.column + 2, value, getPlayer(current)));
							// return true;
						} // end if
					} // end if
				} // end if
			} // end if
		else if(move.column == 5){
			if(board[move.row][move.column - 1] == 0){
				if(board[move.row][move.column + 1] == 0){
					if(board[move.row][move.column - 2] == playerId){ // problems with horizontal right to left
						if(board[move.row][move.column - 3] == 0){
							possibleOpen3.add(new Move(move.row, move.column - 1, value, getPlayer(current)));
							// return true;
						} // end else if
					} // end if
				} // end if
			} // end if
			if(board[move.row][move.column + 1] == 0){
				if(board[move.row][move.column - 1] == playerId){
					if(board[move.row][move.column - 2] == 0){
						possibleOpen3.add(new Move(move.row, move.column + 1, value, getPlayer(current)));
						// return true;
					} // end if
				} // end if
			} // end if
			if(board[move.row][move.column - 1] == 0){
				if(board[move.row][move.column + 1] == playerId){
					if(board[move.row][move.column + 2] == 0){
						possibleOpen3.add(new Move(move.row, move.column + 2, value, getPlayer(current)));
						// return true;
					} // end if
				} // end if
			} // end if
		} // end else if
		else if(move.column == board.length - 2){ //column 6
			if(board[move.row][move.column + 1] == 0){
				if(board[move.row][move.column - 1] == 0){
					if(board[move.row][move.column - 2] == playerId){
						if(board[move.row][move.column - 3] == 0){
							possibleOpen3.add(new Move(move.row, move.column - 1, value, getPlayer(current)));
							// return true;
						} // end if
					} // end if
				} // end if
			} // end if
			if(board[move.row][move.column + 1] == 0){
				if(board[move.row][move.column - 1] == playerId){
					if(board[move.row][move.column - 2] == 0){
						possibleOpen3.add(new Move(move.row, move.column + 1, value, getPlayer(current)));
						// return true;
					} // end if
				} // end if
			} // end if
		} // end else if
		
		if(move.row == 0){ // row 0 or row 7
			if(move.column == 0 || move.column == board.length - 1 ){
				// return false;
			} else if(move.column == 1){	// row 0, column 1
				if(board[move.row][move.column - 1] == 0){ // vertical open 3
					if(board[move.row][move.column + 1] == playerId){
						if(board[move.row][move.column + 2] == 0){
							possibleOpen3.add(new Move(move.row, move.column + 2, value, getPlayer(current)));
							// return true;
						} // end if
					} // end if
				} // end if
				// return false;
			} else if(move.column == board.length - 2){ // row 0, column 6
				if(board[move.row][move.column + 1] == 0){
					if(board[move.row][move.column - 1] == playerId){
						if(board[move.row][move.column - 2] == 0){
							possibleOpen3.add(new Move(move.row, move.column - 2, value, getPlayer(current)));
							// return true;
						} // end if
					} // end if
				} // end if
				// return false;
			} else { // row 0, columns 2 through 5
				if(board[move.row][move.column - 1] == 0){ // horizontal open 3
					if(board[move.row][move.column + 1] == playerId){
						if(board[move.row][move.column + 2] == 0){
							possibleOpen3.add(new Move(move.row, move.column + 2, value, getPlayer(current)));
							// return true;
						} // end if
					} 
				} else if(board[move.row][move.column - 1] == playerId){
					if(board[move.row][move.column - 2] == 0){
						if(board[move.row][move.column + 1] == 0){
							possibleOpen3.add(new Move(move.row, move.column - 2, value, getPlayer(current)));
							// return true;
						} // end if
					} // end if
				}
				// return false;
			} // end else
		} else { // rows 1 - 6
			if(move.row == 1){
				if(board[move.row - 1][move.column] == 0){
					if(board[move.row + 1][move.column] == 0){
						if(board[move.row + 2][move.column] == playerId){
							if(board[move.row + 3][move.column] == 0){
								possibleOpen3.add(new Move(move.row + 1, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
				} // end if
				if(move.column == 0){ // row 1, column 0
					if(board[move.row - 1][move.column] == 0){
						if(board[move.row + 1][move.column] == playerId){
							if (board[move.row + 2][move.column] == 0) {
								possibleOpen3.add(new Move(move.row + 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					// return false;
				} else if(move.column == board.length - 1){ // row 1, column 7
					if(board[move.row - 1][move.column] == 0){
						if(board[move.row + 1][move.column] == playerId){
							if(board[move.row + 2][move.column] == 0){
								possibleOpen3.add(new Move(move.row + 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} 
					// return false;
				} else if(move.column == board.length - 2){ // row 1, column 6
					if(board[move.row - 1][move.column] == 0){ // vertical open 3
						if(board[move.row + 1][move.column] == playerId){
							if(board[move.row + 2][move.column] == 0){
								possibleOpen3.add(new Move(move.row + 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} else if(board[move.row][move.column + 1] == 0){ // horizontal open 3
						if(board[move.row][move.column - 1] == playerId){
							if(board[move.row][move.column - 2] == 0){
								possibleOpen3.add(new Move(move.row, move.column - 2, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} 
					// return false;
				} else if(move.column == 1){ // row 1, column 1
					if(board[move.row - 1][move.column] == 0){
						if(board[move.row + 1][move.column] == playerId){
							if(board[move.row + 2][move.column] == 0){
								possibleOpen3.add(new Move(move.row + 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} else if (board[move.row][move.column - 1] == 0) {
						if(board[move.row][move.column + 1] == playerId){
							if(board[move.row][move.column + 2] == 0){
								possibleOpen3.add(new Move(move.row, move.column + 2, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end else if
					// return false;
				} else if(move.column == 0){ // row 1, column 0
					if(board[move.row - 1][move.column] == 0){
						if(board[move.row + 1][move.column] == playerId){
							if(board[move.row + 2][move.column] == 0){
								possibleOpen3.add(new Move(move.row + 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					// return false;
				} else { // row 1, columns 2 through 5
					if(board[move.row - 1][move.column] == 0){ // possible vertical open 3
						if(board[move.row + 1][move.column] == playerId){
							if(board[move.row + 2][move.column] == 0){
								possibleOpen3.add(new Move(move.row + 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} 
					if(board[move.row][move.column - 1] == playerId){ // horizontal open 3
						if(board[move.row][move.column - 2] == 0){
							if (board[move.row][move.column + 1] == 0) {
								possibleOpen3.add(new Move(move.row, move.column - 2, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					if(board[move.row][move.column - 1] == 0) { // horizontal open 3
						if(board[move.row ][move.column + 1] == playerId){
							if (board[move.row ][move.column + 2] == 0) {
								possibleOpen3.add(new Move(move.row, move.column + 2, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					// return false;
				} // end else
			} else if(move.row == 6){
				if(board[move.row + 1][move.column] == 0){
					if(board[move.row - 1][move.column] == 0){
						if(board[move.row - 2][move.column] == playerId){
							if(board[move.row - 3][move.column] == 0){
								possibleOpen3.add(new Move(move.row - 1, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
				} // end if
				if(move.column == 0 || move.column == board.length - 1) {
					if(board[move.row + 1][move.column] == 0){ // vertical open 3
						if(board[move.row - 1][move.column] == playerId){
							if(board[move.row - 2][move.column] == 0){ 
								possibleOpen3.add(new Move(move.row - 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
				} else if(move.column == 1) { // row 6, column 1
					if (board[move.row][move.column - 1] == 0) { // horizontal open 3
						if (board[move.row][move.column + 1] == playerId) {
							if (board[move.row][move.column + 2] == 0) {
								possibleOpen3.add(new Move(move.row, move.column + 2, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					if(board[move.row + 1][move.column] == 0) { // vertical open 3
						if (board[move.row - 1][move.column] == playerId) {
							if (board[move.row - 2][move.column] == 0) {
								possibleOpen3.add(new Move(move.row - 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if;
						} // end if;
					} // end else if
					// return false;
				} else if (move.column == board.length - 2) { // row 6, column 6
					if(board[move.row][move.column + 1] == 0) { // horizontal open 3
						if(board[move.row][move.column - 1] == playerId) {
							if(board[move.row][move.column - 2] == 0) {
								possibleOpen3.add(new Move(move.row, move.column - 2, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} 
					if (board[move.row + 1][move.column] == 0) { // vertical open 3
						if(board[move.row - 1][move.column] == playerId) {
							if(board[move.row - 2][move.column] == 0) {
								possibleOpen3.add(new Move(move.row - 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					// return false;
				} else { // row 6, columns 2 through 5
					if(move.row - 1 == 0){
						if(move.row + 1 == 0){
							if(move.row - 2 == playerId){
								possibleOpen3.add(new Move(move.row - 1, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					if(board[move.row + 1][move.column] == 0){ // vertical open 3
						if(board[move.row - 1][move.column] == playerId){
							if (board[move.row - 2][move.column] == 0) {
								possibleOpen3.add(new Move(move.row - 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					if (board[move.row][move.column - 1] == 0) { // horizontal open 3
						if(board[move.row][move.column + 1] == playerId){
							if (board[move.row][move.column + 2] == 0) {
								possibleOpen3.add(new Move(move.row, move.column + 2, value, getPlayer(current)));
								// return true;
							} // end if 
						} // end if
					} // end if
					if (board[move.row][move.column + 1] == 0) { // horizontal open 3
						if (board[move.row][move.column - 1] == playerId) {
							if (board[move.row][move.column - 2] == 0) {
								possibleOpen3.add(new Move(move.row, move.column - 2, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					// return false;
				} // end else
			} else if (move.row == 7) {
				if(move.column == 0 || move.column == 7){
					// return false;
				} else if (move.column == 1) {
					if (board[move.row][move.column - 1] == 0) { // horizontal open 3
						if(board[move.row][move.column + 1] == playerId){
							if (board[move.row][move.column + 2] == 0) {
								possibleOpen3.add(new Move(move.row, move.column + 2, value, getPlayer(current)));
								// return true;
							} // end if 
						} // end if
					} // end if
				} else if(move.column == 6){
					if (board[move.row][move.column + 1] == 0) { // horizontal open 3
						if (board[move.row][move.column - 1] == playerId) {
							if (board[move.row][move.column - 2] == 0) {
								possibleOpen3.add(new Move(move.row, move.column - 2, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
				}else { // row 7, columns 2 - 5
					if (board[move.row][move.column - 1] == 0) { // horizontal open 3
						if(board[move.row][move.column + 1] == playerId){
							if (board[move.row][move.column + 2] == 0) {
								possibleOpen3.add(new Move(move.row, move.column + 2, value, getPlayer(current)));
								// return true;
							} // end if 
						} // end if
					} // end if
					if (board[move.row][move.column + 1] == 0) { // horizontal open 3
						if (board[move.row][move.column - 1] == playerId) {
							if (board[move.row][move.column - 2] == 0) {
								possibleOpen3.add(new Move(move.row, move.column - 2, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					// return false;
				} // end else
			} // end else if
			else { // rows 2 through 5
				if(move.row == 2){
					if(board[move.row - 1][move.column] == 0){
						if(board[move.row + 1][move.column] == 0){
							if(board[move.row + 2][move.column] == playerId){
								if(board[move.row + 3][move.column] == 0){
									possibleOpen3.add(new Move(move.row + 1, move.column, value, getPlayer(current)));
									// return true;
								} // end if
							} // end if
						} // end if
					} // end if
				} // end if
				if(move.row == 3 || move.row == 4){
					if(board[move.row - 1][move.column] == 0){
						if(board[move.row + 1][move.column] == 0){
							if(board[move.row + 2][move.column] == playerId){
								if(board[move.row + 3][move.column] == 0){
									possibleOpen3.add(new Move(move.row + 1, move.column, value, getPlayer(current)));
									// return true;
								} // end if
							} // end if
						} // end if
					} // end if
					if(board[move.row + 1][move.column] == 0){
						if(board[move.row - 1][move.column] == 0){
							if(board[move.row - 2][move.column] == playerId){
								if(board[move.row - 3][move.column] == 0){
									possibleOpen3.add(new Move(move.row - 1, move.column, value, getPlayer(current)));
									// return true;
								} // end if
							} // end if
						} // end if
					} // end if
				} // end if
				if(move.row == 5){
					if(board[move.row + 1][move.column] == 0){
						if(board[move.row - 1][move.column] == 0){
							if(board[move.row - 2][move.column] == playerId){
								if(board[move.row - 3][move.column] == 0){
									possibleOpen3.add(new Move(move.row - 1, move.column, value, getPlayer(current)));
									// return true;
								} // end if
							} // end if
						} // end if
					} // end if
				} // end if
				if (move.column == 0) {
					if(board[move.row - 1][move.column] == 0){ // vertical open 3
						if (board[move.row + 1][move.column] == playerId) {
							if (board[move.row + 2][move.column] == 0) {
								possibleOpen3.add(new Move(move.row + 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					if (board[move.row - 1][move.column] == playerId) {
						if (board[move.row - 2][move.column] == 0) {
							if (board[move.row + 1][move.column] == 0) {
								possibleOpen3.add(new Move(move.row - 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
				} else if(move.column == 1){ // column 1 rows 2 through 5
					if(board[move.row][move.column - 1] == 0){ // horizontal open 3
						if (board[move.row][move.column + 1] == playerId) {
							if (board[move.row][move.column + 2] == 0) {
								possibleOpen3.add(new Move(move.row, move.column + 2, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					if(board[move.row - 1][move.column] == 0){ // vertical open 3
						if (board[move.row + 1][move.column] == playerId) {
							if (board[move.row + 2][move.column] == 0) {
								possibleOpen3.add(new Move(move.row + 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					if (board[move.row - 1][move.column] == playerId) {
						if (board[move.row - 2][move.column] == 0) {
							if (board[move.row + 1][move.column] == 0) {
								possibleOpen3.add(new Move(move.row - 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} 
					// return false;
				} else if(move.column == 6){ // column 6, rows 2 through 5
					if(board[move.row][move.column + 1] == 0){ // horizontal open 3
						if (board[move.row][move.column - 1] == playerId) {
							if (board[move.row][move.column - 2] == 0) {
								possibleOpen3.add(new Move(move.row, move.column - 2, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					if(board[move.row + 1][move.column] == 0){ // vertical open 3
						if (board[move.row - 1][move.column] == playerId) {
							if (board[move.row - 2][move.column] == 0) {
								possibleOpen3.add(new Move(move.row - 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					if(board[move.row - 1][move.column] == 0){ // vertical open 3
						if (board[move.row + 1][move.column] == playerId) {
							if (board[move.row + 2][move.column] == 0) {
								possibleOpen3.add(new Move(move.row + 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					// return false;
					// column 7
				} else if(move.column == board.length - 1){ // column 7
						if(move.column == board.length - 2){ // row 6, column 7
							if(board[move.row][move.column + 1] == 0){ // horizontal open 3
								if(board[move.row][move.column - 1] == playerId) {
									if(board[move.row][move.column - 2] == 0){
										possibleOpen3.add(new Move(move.row, move.column - 2, value, getPlayer(current)));
										// return true;
									} // end if
								} // end if
							} // end if
							if(board[move.row + 1][move.column] == 0){
								if(board[move.row - 1][move.column] == playerId) { // vertical open 3
									if(board[move.row - 2][move.column] == 0){
										possibleOpen3.add(new Move(move.row - 2, move.column, value, getPlayer(current)));
										// return true;
									} // end if
								} // end if
							} // end if
						} // end if
						if(move.column == 1){ // row 1, column 7
							if(board[move.row][move.column + 1] == 0){ // horizontal open 3
								if(board[move.row][move.column - 1] == playerId) {
									if(board[move.row][move.column - 2] == 0){
										possibleOpen3.add(new Move(move.row, move.column - 2, value, getPlayer(current)));
										// return true;
									} // end if
								} // end if
							} // end if
							if(board[move.row - 1][move.column] == 0){ // row 1, column 7
								if(board[move.row + 1][move.column] == playerId) { // vertical open 3
									if(board[move.row + 2][move.column] == 0){
										possibleOpen3.add(new Move(move.row + 2, move.column, value, getPlayer(current)));
										// return true;
									} // end if
								} // end if
							} // end if
						}
						if(board[move.row - 1][move.column] == 0){ // row 2 - 5, column 7
							if(board[move.row + 1][move.column] == playerId){
								if(board[move.row + 2][move.column] == 0){
									possibleOpen3.add(new Move(move.row + 2, move.column, value, getPlayer(current)));
									// return true;
								} // end if
							} // end if
						} // end if
						// return false;
				} else { // columns 2-5, rows 2-5
					if (board[move.row - 1][move.column] == 0) { // vertical open 3
						if (board[move.row + 1][move.column] == playerId) {
							if (board[move.row + 2][move.column] == 0) {
								possibleOpen3.add(new Move(move.row + 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					if (board[move.row - 1][move.column] == playerId) { // vertical open 3
						if (board[move.row - 2][move.column] == 0) {
							if (board[move.row + 1][move.column] == 0) {
								possibleOpen3.add(new Move(move.row - 2, move.column, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					if (board[move.row][move.column - 1] == playerId) {
						if (board[move.row][move.column - 2] == 0) {
							if (board[move.row][move.column + 1] == 0) {
								possibleOpen3.add(new Move(move.row, move.column - 2, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					if (board[move.row][move.column - 1] == 0) {
						if (board[move.row][move.column + 1] == playerId) {
							if (board[move.row][move.column + 2] == 0) {
								possibleOpen3.add(new Move(move.row, move.column + 2, value, getPlayer(current)));
								// return true;
							} // end if
						} // end if
					} // end if
					// return false;
				} // end else
			} // end else
			// return false;
		} // end else rows 1-6
	} // end method possibleOpen3inRow
	
	// create/block open little l
	
	// create/blcok open Big L
	
//	private static boolean blockMaxPaths(Move move){
//		
//		int value;
//		int paths)))0;
//		int playerId = getPlayerId(move.player);
//		if(current == playerId){
//			return false;
//		} else {
//			value = 80;
//		} // end else
//	} // end method blockMaxPaths

	private static boolean smallOpenL(Move move){
		int value;
		int playerId = getPlayerId(move.player);
		if(current == playerId){
			value = 80;
		} else {
			value = 75;
		} // end else
		if(move.column == 1){	// row 0, column 1
			if(board[move.row][move.column - 1] == 0){ // vertical open 3
				if(board[move.row][move.column + 1] == 0){
					if(board[move.row][move.column + 2] == playerId){
						if(board[move.row][move.column + 3] == 0){
//							smallOpenL = new Move(move.row, move.column + 1, value, getPlayer(current));
							return true;
						} // end if
					} // end if
				} // end if
			} // end if
		} // end if
		return true;
	} // end method smallOpenL
	
	private static Move bigOpenL(Move move){
		
		return move;
	} // end method bigOpenL
	
	private static int getPlayerId(Player player) {
		String p = player.name();
		if(p.equals("COMPUTER")){
			return computer;
		} else {
			return human;
		} // end else
	}

	private static Move blockMostSpace(Move move) {
		int top, left, right, bottom;
		top = getTop(move);
		bottom = getBottom(move);
		left = getLeft(move);
		right = getRight(move);
		Move next;
		String block = "allEqual";
		if(top > bottom){
			if(left > right){
				if(top > left){
					block = "top";
				} else {
					block = "left";
				}
			} else { // right > left
				if(top > right){
					block = "top";
				} else {
					block = "right";
				}
			} // end else
		} else if( bottom > top){
			if(left > right){
				if(bottom > left){
					block = "bottom";
				} else {
					block = "left";
				}
			} else { // right > left
				if(bottom > right){
					block = "bottom";
				} else {
					block = "right";
				}
			} // end else
		} else {
			if(top == 0){ // no available moves at length 1
				return getAnyAvailableMove(move.player);
			} // end if
			else{
				if(left > right){
					block = "left";
				} else {
					if(move.column < 7) {
						block = "right"; // expand this to optimize
					} else if(move.row > 0){
						block = "top";
					} else {
						block = "bottom";
					} // end else
				} // end else
			} // end else
		} // end else
		if(block.equals("left")){
			next = new Move(move.row, move.column - 1, 2, move.player);
		} else if(block.equals("right")){
			next = new Move(move.row, move.column + 1, 2, move.player);
		} else if(block.equals("top")){
			next = new Move(move.row - 1, move.column, 2, move.player);
		} else { // bottom
			next = new Move(move.row + 1, move.column, 2, move.player);
		} // end else
		return next;
	} // end method blockMostSpace

	private static Move getAnyAvailableMove(Player player) {
		Move move = null;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				if(board[i][j] == 0){
					move = new Move(i,j,0,getPlayer(getPlayerId(player)));
					return move;
				} // end if
			} // end for j
		} // end for i
		return move;
	}

	private static int getRight(Move move) {
		int i = 1;
		if((move.column + 1) >= board.length) return 0;
		for (i = 1 ; i <= (board.length - 1 - move.column); i++) {
			if(board[move.row][move.column + i] != 0){
				break;
			} // end if
		} // end for i
		return --i;
	} // end method getRight

	private static int getLeft(Move move) {
		int i = 1;
		if((move.column - 1) < 0) return 0;
		for (i = 1 ; i <= move.column; i++) {
			if(board[move.row][move.column - i] != 0){
				break;
			} // end if
		} // end for i
		return --i;
	} // end method getLeft

	private static int getTop(Move move) {
		int i = 1;
		if((move.row - 1) < 0) return 0;
		for (i = 1 ; i <= move.row; i++) {
			if(board[move.row - i][move.column] != 0){
				break;
			} // end if
		} // end for i
		return --i;
	} // end method getTop

	private static int getBottom(Move move) {
		int i = 1;
		if((move.row + 1) >= board.length) return 0;
		for (i = 1 ; i <= board.length - 1 - move.row; i++) {
			if(board[move.row + i][move.column] != 0){
				break;
			} // end if
		} // end for i
		return --i;
	} // end method getBottom

	public static Move evaluate(Node root, int depth) {
		Move best = null;
		if(root.children > 0){
			best = root.child[0].move;
			best.value = root.child[0].move.value;
			int children = root.children;
			for (int i = 1; i < children; i++) {
				if(root.child[i].move.value > best.value){
					best = root.child[i].move;
					best.value = root.child[i].move.value;
				} // end if
			} // end for if
		} // end if
		return best;
	} // end method evaluate
	
	public static void printBoard(){
		String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H"};
		int[] cols = {1,2,3,4,5,6,7,8};
		System.out.print("\n  ");
		for (int i = 0; i < cols.length; i++) {
			System.out.print(cols[i] + " ");
		} // end for i
		System.out.println();
		for (int i = 0; i < board.length; i++) {
			System.out.print(rows[i] + " ");
			for (int j = 0; j < board.length; j++) {
				if (board[i][j] == 0) {
					System.out.print("_ ");
				} else if (board[i][j] == 1) {
					System.out.print("X ");
				} else { // if (board[i][j] == 2)
					System.out.print("O ");
				} // end else
			} // end for i
			System.out.println();
		} // end for j
	} // end method printBoard

} // end class Connect4
