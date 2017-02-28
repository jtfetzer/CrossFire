package Connect4;

import java.io.Serializable;

public class Move implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int row;
	int column;
	int value = 0;
	String moveString;
	int player;
	MoveTypeSet list = new MoveTypeSet();
	
	Move(int row, int column, int player, MoveType type){
		this.row = row;
		this.column = column;
		this.moveString = Connect4.getRow(row) + (column + 1);
		this.player = player;
		if(type != null){
			this.list.add(type);
			this.value = type.value;
		}
	} // end constructor
	
	protected void update(Move move){ // needs to be expanded
		value += move.value;
		if(move.list.size() > 0){ // handles cases where move type is null
			list.add(move.list.get(0));
		}
	} // end method updateValue

} // end class Move
