package Connect4;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Node implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int[][] board = new int[8][8];
	ArrayList<Node> children = new ArrayList<>();
	Node parent = null;
	Move lastMove;
	HashMap<String, String> map = new HashMap<>();
	public static ArrayList<Move> moves = new ArrayList<>();
	public static int firstPlayer;
	
	Node(int firstPlayer){
		Node.firstPlayer = firstPlayer;
	}
	
	Node(Move move){
		update(move);
		firstPlayer = move.player;
	}
	
	Node(Node parent, Move move){
		this.lastMove = move;
		this.parent = parent;
		this.parent.children.add(this);
		this.map.putAll(parent.map);
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				this.board[i][j] = parent.board[i][j];
			} // end for j
		} // end for i
		if(this.board[move.row][move.column] == 0){
			this.board[move.row][move.column] = move.player;
			this.map.put(move.moveString, move.moveString);
		} // end if
	} // end constructor
	
	public void addNode(Move move) { // adds a node containing all the played moves plus this move to children
//		System.out.println("Add Move!!");
		if(move == null){
			return;
		} // end if
		if(board[move.row][move.column] != 0){
//			System.out.println("Move taken: " + Connect4.getRow(move.row) + (move.column + 1) + ": " + move.value);
			return; // if move already taken
		} // end if
		
//		System.out.println("Add: " + move.moveString);
		if(map.containsValue(move.moveString)){
//			System.out.println("Duplicate " + move.moveString);
//			for (int i = 0; i < children.size(); i++) {
//				if(this.children.get(i).lastMove.moveString == move.moveString){
//					this.children.get(i).lastMove.value += move.value;
//				}
//			} // end for i
		} else {
			this.children.add(new Node(this, move));
		}
	} // end method add

	int getNumEmptySpaces() {
		int numSpaces = 0;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				if(board[i][j] == 0){
					++numSpaces;
				}
			} // end for j
		} // end for i
		return numSpaces;
	}

	void printBoard(){
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
				if(board[i][j] == 0) {
					System.out.print("_ ");
				} else if(board[i][j] == firstPlayer) {
					System.out.print("X ");
				} else { // if (board[i][j] == 2)
					System.out.print("O ");
				} // end else
			} // end for i
			System.out.println();
		} // end for j
	} // end method printBoard

	int getNumMoves() {
		return 64 - this.getNumEmptySpaces();
	}

	boolean isStalemate() {
		
		return false;
	}

	public void update(Move move) {
//		System.out.println("UPDATE MOVE: " + move.moveString);
		lastMove = move;
		int row = lastMove.row;
		int col = lastMove.column;
		board[row][col] = lastMove.player;
		moves.add(move);
		map.put(move.moveString, move.moveString);
		removeAllChildren();
	}
	
	public void removeAllChildren() {
		for (int i = 0; i < children.size(); i++) {
			children.remove(i);
		} // end for i
		assert children.size() == 0;
	}

	public void priorState() {
//		map.remove(this.lastMove.moveString);
//		moves.remove(moves.size() - 1);
//		int row = Connect4.getRow(lastMove.moveString.substring(0,1));
//		int col = Integer.parseInt(lastMove.moveString.substring(1, 2));
//		board[row][col] = 0;
	}

	public void updateStaticFields() {
		int x = 0;
		int o = 0;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				if(board[i][j] == 1){
					++x;
				} else if(board[i][j] == 2){
					++o;
				}
			} // end for j
		} // end for i
		
		if(x > o){
			firstPlayer = 2; // computer
		} else {
			firstPlayer = 1;
		}
		
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				if(board[i][j] == 1){
					moves.add(new Move(i,j,firstPlayer,null));
				} else if(board[i][j] == 2){
					moves.add(new Move(i,j,2/firstPlayer,null));
				}
			} // end for j
		} // end for i
	}

	public static MoveSet getZeros(Node node) {
		MoveSet zeros = new MoveSet();
		for (int i = 0; i < node.board.length; i++) {
			for (int j = 0; j < node.board.length; j++) {
				if(node.board[i][j] == 0){
					zeros.add(new Move(i, j, 2/node.lastMove.player,null));
				}
			} // end for j
		} // end for i
		return zeros;
	}
	
/*
 * 
 * Add debug mode with board set to certain arrangement.
 */
} // end class Node
