package boardGame;

import java.util.Comparator;

public class MinMoveComparator implements Comparator<Move> { // sorts into descending order

	@Override
	public int compare(Move move1, Move move2) {
		if(move1.getGreatestMoveValue() == move2.getGreatestMoveValue()){
			if(move1.getValue() == move2.getValue()){
				return AlphaSort2.compare(move1, move2);
			}
			return move1.getValue() - move2.getValue();
		}
		return move1.getGreatestMoveValue() - move2.getGreatestMoveValue();
	}
}
