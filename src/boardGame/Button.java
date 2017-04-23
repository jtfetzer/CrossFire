package boardGame;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Button extends JButton implements ActionListener{

	public static final long serialVersionUID = 1L;	
	ImageIcon good_dark, good_light, bad_dark, bad_light, dark, light, black,
			  mad_bad_dark, mad_bad_light, mad_good_dark, mad_good_light;
	
	boolean played = false;
	static boolean gameOver = false;
	int row, col, element;
	String color;
	static int winner = 0;
	
	ImageIcon[] images = {
			new ImageIcon(this.getClass().getResource("/images/a10.png")),
			new ImageIcon(this.getClass().getResource("/images/a11.png")),
			new ImageIcon(this.getClass().getResource("/images/a12.png")),
			new ImageIcon(this.getClass().getResource("/images/a13.png")), 
			
			new ImageIcon(this.getClass().getResource("/images/a18.png")),
			new ImageIcon(this.getClass().getResource("/images/a19.png")),
			new ImageIcon(this.getClass().getResource("/images/a20.png")),
			new ImageIcon(this.getClass().getResource("/images/a21.png")),
			
			new ImageIcon(this.getClass().getResource("/images/a26.png")),
			new ImageIcon(this.getClass().getResource("/images/a27.png")),
			new ImageIcon(this.getClass().getResource("/images/a28.png")),
			new ImageIcon(this.getClass().getResource("/images/a29.png")),
			
			new ImageIcon(this.getClass().getResource("/images/a34.png")),
			new ImageIcon(this.getClass().getResource("/images/a35.png")),
			new ImageIcon(this.getClass().getResource("/images/a36.png")),
			new ImageIcon(this.getClass().getResource("/images/a37.png")),
			
			new ImageIcon(this.getClass().getResource("/images/a42.png")),
			new ImageIcon(this.getClass().getResource("/images/a43.png")),
			new ImageIcon(this.getClass().getResource("/images/a44.png")),
			new ImageIcon(this.getClass().getResource("/images/a45.png")),
			
			new ImageIcon(this.getClass().getResource("/images/a50.png")),
			new ImageIcon(this.getClass().getResource("/images/a51.png")),
			new ImageIcon(this.getClass().getResource("/images/a52.png")),
			new ImageIcon(this.getClass().getResource("/images/a53.png")),

			new ImageIcon(this.getClass().getResource("/images/a57.png")),
			new ImageIcon(this.getClass().getResource("/images/a58.png")),
			new ImageIcon(this.getClass().getResource("/images/a59.png")),
			new ImageIcon(this.getClass().getResource("/images/a60.png")),
			new ImageIcon(this.getClass().getResource("/images/a61.png")),
			new ImageIcon(this.getClass().getResource("/images/a62.png")),
	};
	
	public Button(int i){
		element = i;
		good_dark = new ImageIcon(this.getClass().getResource("/images/good_dark.png"));
		good_light = new ImageIcon(this.getClass().getResource("/images/good_light.png"));
		bad_dark = new ImageIcon(this.getClass().getResource("/images/bad_dark.png"));
		bad_light = new ImageIcon(this.getClass().getResource("/images/bad_light.png"));
		dark = new ImageIcon(this.getClass().getResource("/images/dark.png"));
		light = new ImageIcon(this.getClass().getResource("/images/light.png"));
		mad_bad_dark = new ImageIcon(this.getClass().getResource("/images/mad_bad_dark.png"));
		mad_bad_light = new ImageIcon(this.getClass().getResource("/images/mad_bad_light.png"));
		mad_good_dark = new ImageIcon(this.getClass().getResource("/images/mad_good_dark.png"));
		mad_good_light = new ImageIcon(this.getClass().getResource("/images/mad_good_light.png"));
		black = new ImageIcon(this.getClass().getResource("/images/black.png"));
		
		initializeBoard(i);
		this.addActionListener(this);
	}

	public void playAgain(int i) {
		played = false;
		winner = 0;
		
		if((i < 10)||(i > 13 && i < 18)||(i > 21 && i < 26)||(i > 29 && i < 34)||
		   (i > 37 && i < 42)||(i > 45 && i < 50)||(i > 53 && i < 57)||(i > 62)){
			setIcon(black);
		} else {
			setIcon(images[getImagicArrayLocation(i)]);
		} 
	}
	
	private int getImagicArrayLocation(int i) {
		if(i == 10){
			return 0;
		} else if(i == 11){
			return 1;
		} else if(i == 12){
			return 2;
		} else if(i == 13){
			return 3;
		} else if(i == 18){
			return 4;
		} else if(i == 19){
			return 5;
		} else if(i == 20){
			return 6;
		} else if(i == 21){
			return 7;
		} else if(i == 26){
			return 8;
		} else if(i == 27){
			return 9;
		} else if(i == 28){
			return 10;
		} else if(i == 29){
			return 11;
		} else if(i == 34){
			return 12;
		} else if(i == 35){
			return 13;
		} else if(i == 36){
			return 14;
		} else if(i == 37){
			return 15;
		} else if(i == 42){
			return 16;
		} else if(i == 43){
			return 17;
		} else if(i == 44){
			return 18;
		} else if(i == 45){
			return 19;
		} else if(i == 50){
			return 20;
		} else if(i == 51){
			return 21;
		} else if(i == 52){
			return 22;
		} else if(i == 53){
			return 23;
		} else if(i == 57){
			return 24;
		} else if(i == 58){
			return 25;
		} else if(i == 59){
			return 26;
		} else if(i == 60){
			return 27;
		} else if(i == 61){
			return 28;
		} else if(i == 62){
			return 29;
		}
		return 0;
	}

	public void actionPerformed(ActionEvent e) {
		if(played){
			return;
		}
		played = true;
		
		if(gameOver){
			if(element == 62){
				GameLogic.save(CrossFire.root);
				System.exit(0);
			} 
			else if(element == 57){  
				for (int i = 0; i < 64; i++) {
					CrossFire.buttons[i].initializeBoard(i);
				} // end for i
				
				CrossFire.root.reset();
				gameOver = false;
				if(CrossFire.firstPlayer == 1){
					CrossFire.firstPlayer = 2;
					CrossFire.whosTurn = 1;
					CrossFire.root.update(GameLogic.getRandomStartMove());
				} else {
					CrossFire.firstPlayer = 1;
					CrossFire.whosTurn = 2;
				}
				return;
			} else {
				return;
			}
		}
		if(!CrossFire.load){
			if(CrossFire.turn() == 1){
				if(color.equals("dark")){
					color = "good_dark";
					setIcon(good_dark);
				} else {
					color = "good_light";
					setIcon(good_light);
				}
				(new UpdateThread(row,col,1)).start();
			} else {
				if(color.equals("dark")){
					color = "bad_dark";
					setIcon(bad_dark);
				} else {
					color = "bad_light";
					setIcon(bad_light);
				}
			}
		} else {
			if(e.getActionCommand().equals("Human")){
				if(color.equals("dark")){
					color = "good_dark";
					setIcon(good_dark);
				} else {
					color = "good_light";
					setIcon(good_light);
				}
				(new UpdateThread(row,col,1)).start();
			} else {
				if(color.equals("dark")){
					color = "bad_dark";
					setIcon(bad_dark);
				} else {
					color = "bad_light";
					setIcon(bad_light);
				}
				(new UpdateThread(row,col,2)).start();
			}
		}
	}

	void initializeBoard(int i) {
		played = false;
		if(i < 8){
			row = 0;
			col = i % 8;
			if(i%2 == 0){
				color = "light";
				setIcon(light);
			} else {
				color = "dark";
				setIcon(dark);
			}
		} else if(i < 16){
			row = 1;
			if(i == 8){
				col = 0;
			} else {
				col = i % 8;
			}
			if(i%2 == 0){
				color = "dark";
				setIcon(dark);
			} else {
				color = "light";
				setIcon(light);
			}
		} else if(i < 24){
			row = 2;
			if(i == 16){
				col = 0;
			} else {
				col = i % 8;
			}
			if(i%2 == 0){
				color = "light";
				setIcon(light);
			} else {
				color = "dark";
				setIcon(dark);
			}
		} else if(i < 32){
			row = 3;
			if(i == 24){
				col = 0;
			} else {
				col = i % 8;
			}
			if(i%2 == 0){
				color = "dark";
				setIcon(dark);
			} else {
				color = "light";
				setIcon(light);
			}
		} else if(i < 40){
			row = 4;
			if(i == 32){
				col = 0;
			} else {
				col = i % 8;
			}
			if(i%2 == 0){
				color = "light";
				setIcon(light);
			} else {
				color = "dark";
				setIcon(dark);
			}
		} else if(i < 48){
			row = 5;
			if(i == 40){
				col = 0;
			} else {
				col = i % 8;
			}
			if(i%2 == 0){
				color = "dark";
				setIcon(dark);
			} else {
				color = "light";
				setIcon(light);
			}
		} else if(i < 56){
			row = 6;
			if(i == 48){
				col = 0;
			} else {
				col = i % 8;
			}
			if(i%2 == 0){
				color = "light";
				setIcon(light);
			} else {
				color = "dark";
				setIcon(dark);
			}
		} else if(i < 64){
			row = 7;
			if(i == 56){
				col = 0;
			} else {
				col = i % 8;
			}
			if(i%2 == 0){
				color = "dark";
				setIcon(dark);
			} else {
				color = "light";
				setIcon(light);
			}
		}
	}

	public void showWinner() {
		if(winner == 1 || winner == 0){
			if(color.equals("bad_dark")){
				color = "mad_bad_dark";
				setIcon(mad_bad_dark);
			} else if(color.equals("bad_light")){
				color = "mad_bad_light";
				setIcon(mad_bad_light);
			}
		} else if(winner == 2 || winner == 0){
			if(color.equals("good_dark")){
				color = "mad_good_dark";
				setIcon(mad_good_dark);
			} else if(color.equals("good_light")){
				color = "mad_good_light";
				setIcon(mad_good_light);
			}
		}
	}
	
	public void showDraw() {
		if(color.equals("bad_dark")){
			color = "mad_bad_dark";
			setIcon(mad_bad_dark);
		}
		if(color.equals("bad_light")){
			color = "mad_bad_light";
			setIcon(mad_bad_light);
		}
		if(color.equals("good_dark")){
			color = "mad_good_dark";
			setIcon(mad_good_dark);
		} 
		if(color.equals("good_light")){
			color = "mad_good_light";
			setIcon(mad_good_light);
		}
	}
	

	public void toBlack() {
		setIcon(black);
		played = false;
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
