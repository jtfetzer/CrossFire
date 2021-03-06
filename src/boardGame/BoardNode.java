package boardGame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * 	This class is used in the context of a {@codeConnect4} game, 
 *  and contains the game board and moves played.	
 * @author Jonathan Fetzer
 *
 */
public class BoardNode implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	int[][] board = new int[8][8];
	private ArrayList<BoardNode> children = new ArrayList<>();
	BoardNode parent = null;
	Move lastMove; // The most recent move played
	HashMap<String, String> map = new HashMap<>(); // A HashMap of all moves played.
	public MoveSet moves = new MoveSet(); // A set of all moves played.
	public int firstPlayer;	// May not be necessary, consider removing
	public int depth = 0;
	public int value = 0;
	public int minWinDepth = 0;
	public int maxWinDepth = 0;
	public static String winner = "";
	
	BoardNode(){
		
	}
	/**
	 * Creates the root node for the game.
	 * @param firstPlayer
	 */
	BoardNode(int firstPlayer){
		this.firstPlayer = firstPlayer;
	}
	
	/**
	 * Adds a child node with {@code move}.
	 * @param move
	 */
	BoardNode(Move move){
		update(move);
		firstPlayer = move.player;
	}
	
	/**
	 * Adds a child node with {@code move} to the {@code parent} node. 
	 * @param parent
	 * @param move
	 */
	private BoardNode(BoardNode parent, Move move){ 
		this.depth = parent.depth + 1;
		this.firstPlayer = parent.firstPlayer; 
		this.lastMove = move;
		this.parent = parent; // never seem to use this, remove?
		this.parent.children.add(this);
		if(this.parent.moves.size() != 0){
			for(Move next : parent.moves){
				this.map.put(next.moveString, next.moveString);
			}
		}
		
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				this.board[i][j] = parent.board[i][j];
			} // end for j
		} // end for i
		
		if(this.board[move.row][move.column] == 0){
			this.board[move.row][move.column] = move.player;
			this.map.put(move.moveString, move.moveString);
			this.moves.addAll(parent.moves); // .removeValues()
		} // end if
	} // end constructor
	
	/**
	 * Adds a child node with {@code move}.
	 * @param move
	 */
	private void addNode(Move move) { 
			
		if(!this.map.containsKey(move.moveString)){
			map.put(move.moveString, move.moveString);
			BoardNode boardNode = new BoardNode(this, move);
			boardNode.moves.add(new Move(move.row, move.column, move.player, move.moveTypeSet.get(0)));
		}
	} // end method add

	/**
	 * Add a child node for every move in MoveSet {@code set}.
	 * @param set
	 */
	public void addAll(MoveSet set){
		for(Move move : set){
			addNode(move);
		}
	}
	
	/**
	 * Adds all the child nodes from the {@code source} node.
	 * @param source
	 */
	public void addAllChildNodes(BoardNode source){
		for(BoardNode next: source.children){
			addNode(next.lastMove);
		}
	}
	
	/**
	 * @return The number of empty spaces on the board.
	 */
	int getNumEmptySpaces(){
		return 64 - moves.size();
	}
	
	
	/**
	 * Prints the board state to the console.
	 */
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

	/**
	 * @return The number of moves played.
	 */
	int getNumMovesPlayed() {
		return moves.size();
	}

	/**
	 * @return True of False if game if neither player can win from the current game state.
	 */
	boolean isStalemate() { // update to check for stalemate
		if(getNumEmptySpaces() == 0){
			return true;
		} // end if
		return false;
	}

	/**
	 * This is how a {@code move} is played.
	 * @param move
	 */
	public synchronized void update(Move move) {
		if(!moves.contains(move)){
			
			moves.add(move);
			lastMove = move;
			int row = move.row; 
			int col = move.column;
			board[row][col] = move.player;
			map.put(move.moveString, move.moveString);
			
			if(!CrossFire.load){
				if(move.player == 2){
					ActionListener[] a = CrossFire.buttons[move.row * 8 + move.column].getActionListeners();
					a[0].actionPerformed(new ActionEvent(CrossFire.buttons[move.row * 8 + move.column], 2, "Computer"));
				}
			}
		} else {
			System.out.println("Error update: " + move.moveString);
			this.printBoard();
		}
		GameLogic.testGameOver(this);
	}
	
	/**
	 * Used to restore the state to before last move was played.
	 */
	public void priorState() {
		map.remove(this.lastMove.moveString);
		moves.remove(moves.size() - 1);
		int row = GameLogic.getRow(lastMove.moveString.substring(0,1));
		int col = Integer.parseInt(lastMove.moveString.substring(1, 2));
		board[row][col] = 0;
	}

	/**
	 * @param node
	 * @return A {@code MoveSet} containing the moves still available.
	 */
	public MoveSet getZeros() {
		MoveSet zeros = new MoveSet();
		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board.length; j++) {
				if(this.board[i][j] == 0){
					zeros.addUnique(new Move(i, j, 2/this.lastMove.player,  new MoveType(MoveType.Type.ZEROS, 100)));
				}
			} // end for j
		} // end for i
		return zeros;
	}

	/**
	 * Used for debugging, prints all child node moveStrings and move values, 
	 * where move values are based solely on applying the fitness function at depth 1.
	 * @param message
	 */
	public void printChildren() {
		printChildren("");
	}
	
	/**
	 * Used for debugging, prints {@code message} with all child node moveStrings and move values, 
	 * where move values are based solely on applying the fitness function at depth 1.
	 * @param message
	 */
	public void printChildren(String message) {
		if(!message.equals("")){
			System.out.println("\n" + message + ":");
		}
		System.out.println("Children size: " + children.size());
		for(BoardNode node : children){
			System.out.println(node.lastMove.moveString + ": val = " + node.lastMove.getValue() + ", depth = " + node.depth + "; ");
		}
		System.out.println("\n");
	}

	/**
	 * @param moveString
	 * @return True or False if {@code moveString} has been played.
	 */
	public boolean hasMoveBeenPlayed(String moveString) {
		return map.containsValue(moveString);
	}
	
	/**
	 * @return The number of child nodes of the node that calls this method.
	 */
	public int size(){
		return this.children.size();
	}
	
	/**
	 * Sorts the children of the node that calls this method into descending 
	 * order based on the value determined by the Minimax algorithm.
	 */
	private void sortNodes(){
		Collections.sort(this.children, new NodeComparator());
	}
	

	private void sortChildren() {
		Collections.sort(this.children, new ChildComparator());
	}
	
	/**
	 * @return The node with the minimum value as determined by the Minimax algorithm.
	 */
	public BoardNode minChildNode(){
		sortNodes();
		return this.children.get(this.size() - 1);
	}
	
	/**
	 * @return The node with the maximum value as determined by the Minimax algorithm.
	 */
	public BoardNode maxChildNode(){
		sortNodes();
		return this.children.get(0);
	}
	
	/**
	 * When Minimax depth is not enough to find a winning strategy, this method 
	 * determines the best move based solely on applying the fitness function at depth 1.
	 * @return The move at depth one with the minimum value.
	 */
	public Move minChildMove(){
		sortChildren();
		return this.children.get(this.size() - 1).lastMove;
	}
	
	/**
	 * When Minimax depth is not enough to find a winning strategy, this method 
	 * determines the best move based solely on applying the fitness function at depth 1.
	 * @return The move at depth one with the maximum value.
	 */
	public Move maxChildMove(){
		sortChildren();
		return this.children.get(0).lastMove;
	}
	
	/**
	 * @return The minimum move value of the children nodes.
	 */
	public int minChildMoveValue(){
		return minChildMove().getValue();
	}
	
	/**
	 * @return The greatest move value of the children nodes.
	 */
	public int maxChildMoveValue(){
		return maxChildMove().getValue();
	}
	
	/**
	 * @return The minimum {@link BoardNode} value of the calling object's child nodes.
	 */
	public int minChildNodeValue(){
		sortNodes();
		if(this.children.size() == 0) {
			System.out.println("MIN ERROR in BoardNode");
			return this.value;
		} 
		return this.children.get(this.children.size() - 1).value;
	}
	
	/**
	 * @return The maximum {@link BoardNode} value of the calling object's child nodes.
	 */
	public int maxChildNodeValue(){
		sortNodes();
		if(this.children.size() == 0) {
			System.out.println("MAX ERROR in BoardNode");
			return this.value;
		} 
		return this.children.get(0).value;
	}

	/**
	 * Removes all children from the node that calls this method.
	 */
	public void removeAllChildNodes() {
		for (int i = 0; i < this.size(); i++) {
			map.remove(children.get(i).lastMove.moveString);
			children.remove(i--);
		} // end for i
	}
	
	/**
	 * Removes all child nodes, except for the node passed as the parameter to this method.
	 * @param min
	 */
	public void removeAllChildNodesExcept(int node) {
		
		for (int i = 0; i < this.size(); i++) {
			if(i != node){
				map.remove(children.get(i).lastMove.moveString);
				this.children.remove(i--);
			} 
		} // end for i
	}

	/**
	 * Removes all child nodes, except for the node passed as the parameter to this method.
	 * @param min
	 */
	public void removeAllChildNodesExcept(BoardNode node) {
		for (int i = 0; i < this.size(); i++) {
			if(!this.children.get(i).equals(node)){
				map.remove(children.get(i).lastMove.moveString);
				this.children.remove(i--);
			} // end if
		} // end for i
	}

	/**
	 * Removes all child nodes, except for the node passed as the parameter to this method.
	 * @param min
	 */
	public void removeAllChildNodesExcept(Move min) {
		for (int i = 0; i < this.size(); i++) {
			if(!this.children.get(i).lastMove.equals(min)){
				map.remove(children.get(i).lastMove.moveString);
				this.children.remove(i--);
			} // end if
		} // end for i
	}
	
	/**
	 * Removes all child nodes, except for the one where the {@code lastMove} is {@code moveString}.
	 * @param moveString
	 */
	public void removeAllChildNodesExcept(String moveString) {
		for (int i = 0; i < this.size(); i++) {
			if(!this.children.get(i).lastMove.moveString.equals(moveString)){
				map.remove(children.get(i).lastMove.moveString);
				this.children.remove(i--);
			} // end if
		} // end for i
	}
	
	/**
	 * Removes the {@link BoardNode} passed as the parameter to this method.
	 * @param node
	 */
	public void removeChildNode(int node) {
		for (int i = 0; i < this.size(); i++) {
			if(i == node){
				map.remove(children.get(i).lastMove.moveString);
				this.children.remove(i--);
			} // end if
		} // end for i
	}
	
	/**
	 * Used for debugging, prints all child node moveStrings and values.
	 */
	public void printNodes() {
		printNodes("");
	} 
	
	/**
	 * Used for debugging, prints all child node moveStrings and node values.
	 * @param message
	 */
	public void printNodes(String message) {
		if(!message.equals("")){
			System.out.println("\n" + message + ":");
		}
		System.out.println("Child Nodes: " + children.size());
		for(BoardNode node : children){
			System.out.println("\t" + node.lastMove.moveString + ": nodeVal = " + node.value + ", " + "move val: " + node.lastMove.getValue() + ", depth: " + node.minWinDepth + "; ");
		}
		System.out.println("\n");
	}

	public BoardNode getChild(int i) {
		return children.get(i);
	}

	public void printWinnerNodes(String message) { // used for debugging, remove later
		if(!message.equals("")){
			System.out.println("\n" + message + ":");
		}
		System.out.println("Child Winner Nodes: " + children.size());
		for(BoardNode node : children){
				System.out.println(node.lastMove.moveString + ": val = " + node.value + ", depth = " + node.depth + " winner: " + BoardNode.winner + "; ");
		}
		System.out.println("\n");
	}

	public BoardNode ancestor(int i) {
		BoardNode point = this;
		for(; i>0; i--){
			point = this.parent;
		}
		System.out.println("Ancestor: " + point.lastMove.moveString);
		return point;
	}

	public void printTree() {
		BoardNode pointer = this;
		System.out.print(pointer.lastMove.moveString + " <- ");
		for (int j = 0; j < this.depth; j++) {
			if(pointer.parent == null) break;
			pointer = pointer.parent;
			System.out.print(pointer.lastMove.moveString + " <- ");
		} // end for j
		System.out.println();
	}
	
	public boolean containsChildNode(String moveString){
		
		for(BoardNode node : children){
			if(node.hasMoveBeenPlayed(moveString)){
				return true;
			}
		}
		return false;
	}

	public MoveSet maxChildNodeMoves() {
		int max = this.maxChildNodeValue();
		MoveSet maxMoves = new MoveSet();
		for (int i = 0; i < this.size(); i++) {
			if(this.getChild(i).value == max){
				maxMoves.add(this.getChild(i).lastMove);
			}
		} // end for i
		return maxMoves;
	}

	public int numMaxChildNodes() {
		int max = this.maxChildNodeValue();
		int count = 0;
		for (int i = 0; i < this.size(); i++) {
			if(this.getChild(i).value == max){
				++count;
			}
		} // end for i
		return count;
	}

	public BoardNode maxChildNodes() {
		BoardNode maxNodes = new BoardNode();
		int max = this.maxChildNodeValue();
		
		for (int i = 0; i < this.size(); i++) {
			if(this.getChild(i).value == max){
				maxNodes.addNode(this.getChild(i));
			}
		} // end for i
		return maxNodes;
	}
	
	public BoardNode minChildNodes() {
		BoardNode minNodes = new BoardNode();
		int min = this.minChildNodeValue();
		
		for (int i = 0; i < this.size(); i++) {
			if(this.getChild(i).value == min){
				minNodes.addNode(this.getChild(i));
			}
		} // end for i
		return minNodes;
	}
	/**
	 * Adds the {@code node} parameter node as a child.
	 * @param node
	 */
	private void addNode(BoardNode node) {
		if(!map.containsKey(node.lastMove.moveString)){
			this.children.add(node);
		}
	} // end method add
	
	public Move minDepthNodeMove(BoardNode node) {
		return minDepthNode().lastMove;
	}
	public int minWinDepth() {
		return minWinDepthNode().minWinDepth;
	}
	public Move minWinDepthMove() {
		return minWinDepthNode().lastMove;
	}
	public BoardNode minWinDepthNode() {
		int index = 0;
		int min = 1000;
		for (int i = 0; i < this.size(); i++) {
			if(this.children.get(i).minWinDepth < min){
				min = this.children.get(i).minWinDepth;
				index = i;
			}
		} // end for j
		return this.children.get(index);
	}
	public BoardNode minDepthNode() {
		int min_depth = 1000;
		int min_move_index = 0;
		for (int i = 0; i < this.size(); i++) {
			if(this.getChild(i).depth < min_depth){
				min_depth = this.getChild(i).depth;
				min_move_index = i;
			}
		} // end for i
		return this.getChild(min_move_index);
	}
	
	public static void print(int[][] board, int firstPlayer) {
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
	}
	public static String boardToString(int[][] board) {
		String boardString = "";
		
		String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H"};
		int[] cols = {1,2,3,4,5,6,7,8};

		for (int i = 0; i < cols.length; i++) {
			boardString += cols[i] + " ";
		} // end for i
		boardString += "\n";
		for (int i = 0; i < board.length; i++) {
			boardString += rows[i] + " ";
			for (int j = 0; j < board.length; j++) {
				if(board[i][j] == 0) {
					boardString += "_ ";
				} else if(board[i][j] == CrossFire.firstPlayer) {
					boardString += "X ";
				} else { // if (board[i][j] == 2)
					boardString += "O ";
				} // end else
			} // end for i
			boardString += "\n";
		} // end for j
		return boardString;
	}
	public void reset() {
		board = new int[8][8];
		children = new ArrayList<>();
		parent = null;
		lastMove = null;
		map = new HashMap<>(); // A HashMap of all moves played.
		moves = new MoveSet(); // A set of all moves played.
		firstPlayer = CrossFire.firstPlayer;
		depth = 0;
		value = 0;
		minWinDepth = 0;
		maxWinDepth = 0;
		winner = "";
		this.removeAllChildNodes();
	}
	
	
} // end class Node
