package Connect4;

import java.util.Comparator;

public class NodeComparator implements Comparator<Node>{

	@Override
	public int compare(Node node1, Node node2) {
		if(node1.lastMove.value == node2.lastMove.value){
			return AlphaSort.compare(node1, node2);
		}
		return node1.lastMove.value - node2.lastMove.value;
	}

}
