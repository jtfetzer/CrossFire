package Connect4;
import java.util.ArrayList;

public class MoveSet extends ArrayList<Move>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean add(Move move){ // returns position of move in MoveList, else -1
		boolean updated = false;
		
		for(Move next : this){
			if(next.moveString.equals(move.moveString)){ // MoveList contains this move already
				next.update(move);
				updated = true;
			}
		}
		if(!updated){
			super.add(move);
		}
		return updated;
	}
	
	public boolean addAll(MoveSet moveList){
		for(Move next : moveList){
			add(next);
		}
		return true;
	}
}
