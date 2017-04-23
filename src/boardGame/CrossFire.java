package boardGame;
import java.awt.GridLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * This class contains the main method.
 * @author Jonathan Fetzer
 */

public class CrossFire extends JFrame{
	
	public static final long serialVersionUID = 1L;	
	static JPanel p;
	static Button[] buttons;
	static int firstPlayer = 1;
	static int whosTurn = 2;
	static GameLogic logic;
	static BoardNode root;
	static boolean load = false; // fix
	boolean wait = true;
	
	CrossFire(){
		super("CrossFire");
		ImageIcon img = new ImageIcon(this.getClass().getResource("/images/good_light.png"));
		setIconImage(img.getImage());
		p = new JPanel();
		buttons = new Button[64];
		setSize(800,800);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		p.setLayout(new GridLayout(8,8));
		for (int i = 0; i < 64; i++) {
			buttons[i] = new Button(i);
			p.add(buttons[i]);
		} // end for i
		add(p);
		root = new BoardNode(firstPlayer); 
		if(load){
			synchronized(this){
				GameLogic.load(); // updates root.board
			} 
		}
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new CrossFire(); 	
		load = false; // board loaded, game can continue normally
	} // end method main

	public static int turn() {
		if(whosTurn /2 == 1){
			whosTurn = 1;
		} else {
			whosTurn = 2;
		}
		return whosTurn;
	}
	
	/**
	 * This is the code to change the losing oponent's ImageIcons.
	 */
	public static void showVictory() {
		int[] toBlack = {0,1,2,3,4,5,6,7,15,23,31,39,47,55,63,
		                 62,61,60,59,58,57,56,48,40,32,24,16,8,
		                 9,10,11,12,13,14,22,30,38,46,54,53,52,
		                 51,50,49,41,33,25,17,18,19,20,21,29,
		                 37,45,44,43,42,34,26,27,28,36,35};
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(Button.winner != 0){
			for (int i = 0; i < buttons.length; i++) {
				buttons[i].showWinner();
			} // end for i
		} else {
			for (int i = 0; i < buttons.length; i++) {
				buttons[i].showDraw();
			} // end for i
		}
		
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(int i : toBlack){
			buttons[i].toBlack();
		}
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		GameLogic.save(root);
		load = false;		
		for(int i = 0; i < 64; i++){
			buttons[i].playAgain(i);
		}
		Button.gameOver = true;
	}
}
