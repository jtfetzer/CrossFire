package boardGame;

import java.util.Comparator;

public class MaxMoveComparator implements Comparator<Move> {

	@Override
	public int compare(Move move1, Move move2) {
		if(move1.getBestMoveValue() == move2.getBestMoveValue()){
			if(move1.getValue() == move2.getValue()){
				return AlphaSort2.compare(move1, move2);
			}
			return move2.getValue() - move1.getValue();
		}
		return move2.getBestMoveValue() - move1.getBestMoveValue();
	}
}
