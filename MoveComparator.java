package Connect4;

import java.util.Comparator;

public class MoveComparator implements Comparator<Move> {

	@Override
	public int compare(Move move1, Move move2) {
			if(move1.value == move2.value){
				return AlphaSort2.compare(move1, move2);
			}
			return move1.value - move2.value;
		}
}
