package Connect4;
import java.util.ArrayList;

public class MoveTypeSet extends ArrayList<MoveType>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	MoveTypeSet(){
		super();
	}
	public boolean add(MoveType move){ // returns position of move in MoveList, else -1
		boolean updated = false;
		
		for(MoveType next : this){
			if(next.type == move.type){
				next.increment();
				updated = true;
			}
		}
		if(!updated){
			super.add(move);
		}
		return updated;
	}
	
	public String toString(){
		String list = "";
		for(MoveType type : this){
			list += type.type + ": " + type.count + ", ";
		}
		return list;
	}
}
