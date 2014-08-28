package GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class Reset extends JFrame implements ActionListener, WindowListener {
	public JPanel panel;
	public JPanel subPanel;
	public JLabel label;
	public JButton okButton;
	
	public Reset()
	{   	
		panel = new JPanel(new BorderLayout());
		subPanel = new JPanel(new FlowLayout());
		label = new JLabel("  Configurations have been reset!");
		okButton = new JButton("Ok");
		this.add(panel);
		panel.add(label, BorderLayout.CENTER);
		subPanel.add(okButton);
		panel.add(subPanel, BorderLayout.SOUTH);

		okButton.addActionListener(this);
	}	
	
	public void initialise() {	
		Reset frame = new Reset ();
	
		this.setResizable(false); 
		this.setSize(200,100);
		
		this.setLocation(200, 100);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == okButton) {
			windowClosed(null);
		}
	}
	
	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		dispose();
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
