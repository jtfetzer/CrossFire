package Connect4;

public class MoveType {
	public enum Type { CONNECT_4, OPEN_3, THREE_4, TWO_4, ONE_4, SMALL_OPEN_L, BIG_OPEN_L, PRE_BIG_OPEN_L, PRE_SMALL_OPEN_L, BLOCK_MOST_SPACE, ZEROS}
	public Type type;
	public int count = 0;
	public int value;
	
	MoveType(Type type, int value){
		this.type = type;
		this.value = value;
		++count; 
	}
	
	public void changeValue(int value){
		this.value = value;
	}
	
	public void increment(){
		++count;
	}
}
