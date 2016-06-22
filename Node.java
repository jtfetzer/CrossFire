public class Node {
	int[][] board;
	Node[] child = new Node[10000];
	Node parent = null;
	int children = 0;
	Move move;
	
	Node(int[][] board){
		this.board = board;		
	} // end constructor
	
	Node(Node parent, Move move, int player){
		this.move = move;
		this.parent = parent;
		this.parent.child[children++] = this;
		this.board = parent.board;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				this.board[i][j] = parent.board[i][j];
			} // end for j
		} // end for i
		if(this.board[move.row][move.column] == 0){
			this.board[move.row][move.column] = player;
		} // end if
	} // end constructor
	
	public void add(Move move, int playerId, Player player) { 	
		if(move == null){
			return;
		} // end if
		if(Connect4.board[move.row][move.column] != 0){
			return; // if move already taken
		} // end if
		this.child[children] = new Node(this, move, playerId );
		this.child[children].move = new Move(move.row,move.column,move.value, player);
		this.child[children].board[move.row][move.column] = playerId;
		this.child[children].parent = this;
		++children;
	} // end method add
	
	public void remove(int i){
		this.child[i].parent = null;
		this.child[i] = null;
		children--;
	} // end method remove

} // end class Node
