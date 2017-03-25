package Connect4;

public class AlphaSort {
	
		public static int compare(BoardNode node1, BoardNode node2) {
			Move move1 = node1.lastMove;
			Move move2 = node2.lastMove;
			
			if(move1.row < move2.row) {
				return -1;
			} else if(move1.row > move2.row) {
				return 1;
			} else {
				return move1.column - move2.column;
			}
		}

}

