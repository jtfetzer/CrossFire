package Connect4;

import java.util.Comparator;

public class MinMoveComparator implements Comparator<Move> {

	public int compare(Move move1, Move move2) {
		if(move1.getValue() == move2.getValue()){
			return AlphaSort2.compare(move1, move2);
		}
		return move1.getValue() - move2.getValue();
	}
}
