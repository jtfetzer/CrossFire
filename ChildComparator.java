package Connect4;

import java.util.Comparator;

public class ChildComparator implements Comparator<BoardNode>{

	@Override
	public int compare(BoardNode node1, BoardNode node2) {
		if(node1.lastMove.getValue() == node2.lastMove.getValue()){
			return AlphaSort.compare(node1, node2);
		}
		return node2.lastMove.getValue() - node1.lastMove.getValue(); // Descending order
	}

}

