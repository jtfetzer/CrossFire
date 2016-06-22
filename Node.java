import java.util.ArrayList;

public class Node {
	int[][] board = new int[8][8];
//	Node[] child = new Node[10000];
	ArrayList<Node> child = new ArrayList<>();
	Node parent = null;
	int children = 0;
	Move move;
	
	Node(int[][] board){	// used to create root node
		this.board = board;		
	} // end constructor
	
	Node(Node parent, Move move, int player){
		this.move = move;
		this.parent = parent;
		this.parent.child.add(this);
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				this.board[i][j] = parent.board[i][j];
			} // end for j
		} // end for i
		if(this.board[move.row][move.column] == 0){
			this.board[move.row][move.column] = player;
		} // end if
	} // end constructor
	
	public void add(Move move, int playerId) { 	
		if(move == null){
			return;
		} // end if
		if(Connect4.board[move.row][move.column] != 0){
			return; // if move already taken
		} // end if
		this.child.add(new Node(this, move, playerId ));
		++children;
	} // end method add
	
//	public void remove(int i){
//		this.child[i].parent = null;
//		this.child[i] = null;
//		children--;
//	} // end method remove

} // end class Node
