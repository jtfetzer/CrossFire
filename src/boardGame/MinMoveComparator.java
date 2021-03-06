package boardGame;

import java.util.Comparator;

public class MinMoveComparator implements Comparator<Move> { // sorts into descending order

	@Override
	public int compare(Move move1, Move move2) {
		if(move1.getBestMoveValue() == move2.getBestMoveValue()){
//			if(move1.getValue() == move2.getValue()){ // Used with debugging
//				return AlphaSort2.compare(move1, move2);
//			}
			return move1.getValue() - move2.getValue();
		}
		return move1.getBestMoveValue() - move2.getBestMoveValue();
	} // end method compare
}
