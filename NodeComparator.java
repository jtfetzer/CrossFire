package Connect4;

import java.util.Comparator;

public class NodeComparator implements Comparator<BoardNode>{

	@Override
	public int compare(BoardNode node1, BoardNode node2) {
		if(node1.value == node2.value){
			return AlphaSort.compare(node1, node2);
		}
		return node2.value - node1.value;
	}

}
