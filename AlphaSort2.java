package boardGame;

public class AlphaSort2 {
	
		public static int compare(Move move1, Move move2) {
			
			if(move1.row < move2.row) {
				return -1;
			} else if(move1.row > move2.row) {
				return 1;
			} else {
				return move1.column - move2.column;
			}
		}

}

