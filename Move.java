
public class Move {
	int row;
	int column;
	int value = 0;
	Player player;

	Move(int row, int column, Player player){
		this.row = row;
		this.column = column;
		this.player = player;
	} // end constructor
	
	Move(int row, int column, int value, Player player){
		this.row = row;
		this.column = column;
		this.value = value;
		this.player = player;
	} // end constructor

} // end class Move
