package kr.cnu.sea.xmogen.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import kr.cnu.sea.xmogen.XMoGenProject;

public class XMoGenMainUI extends JFrame implements ActionListener, MouseListener, Observer {
	
	private static JTree treeView = null;
	private JDesktopPane desktop;
	
	private static JMenuBar menuBar = null;
	private JMenuItem openMenuItem = null;
	private JMenuItem runMenuItem = null;
	
	public XMoGenMainUI() {
		
		init();
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
				
		this.setSize((screen.width*4)/5, (screen.height*4)/5);
		this.setLocation(((screen.width - frameSize.width) / 2) - (((screen.width*4)/5) / 2),
								((screen.height - frameSize.height) / 2) - (((screen.height*4)/5) / 2));
		
		this.setVisible(true);
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE) ;
		
		// Content Pane Setting
		JPanel pan = new JPanel();
		
		pan.setLayout(null);
		this.setContentPane(pan);
		
		// Internal Frame Setting
		desktop = new JDesktopPane();
		desktop.setBounds((this.getWidth() / 5) + 1 , 0, (this.getWidth() * 4) / 5, this.getHeight());
		
		pan.add(desktop, BorderLayout.CENTER);
				
	     // Tree View Setting
		XMoGenProject projectModel = XMoGenProject.getInstance();
		
 		treeView = new JTree(projectModel.getProjectTreeNodes());
 		JScrollPane treeJSP = new JScrollPane(treeView);
 		treeJSP.setSize(this.getWidth() / 5, this.getHeight());
 		
 		treeView.addMouseListener(this);
 		
 		pan.add(treeJSP, BorderLayout.WEST);
		
		//Make dragging a little faster but perhaps uglier.
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.repaint();
        
        projectModel.registObserver(this);
	}

	protected void finalize() throws Throwable {
		
		XMoGenProject projectModel = XMoGenProject.getInstance();
		projectModel.unregistObserver(this);
		
		super.finalize();
	}
	
	public void init() {
		// Add Menubar
		menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		
		addMenuBar();
	}
	
	public void refresh() {
		
		XMoGenProject projectModel = XMoGenProject.getInstance();
		
		treeView.setModel(new DefaultTreeModel(projectModel.getProjectTreeNodes()));
		treeView.repaint();
	}
	
	public void addMenuBar() {
		
		JMenu fileMenu = new JMenu("File");
		JMenu runMenu = new JMenu("Run");
				
		menuBar.add(fileMenu);
		menuBar.add(runMenu);
		
		openMenuItem = new JMenuItem("Open");
		openMenuItem.addActionListener(this);
		fileMenu.add(openMenuItem);

		runMenuItem = new JMenuItem("Run");
		runMenuItem.addActionListener(this);
		runMenu.add(runMenuItem);
		
		fileMenu.addActionListener(this);
		runMenu.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {

		if(e.getSource() == openMenuItem){
			
			JFileChooser fileDlg = new JFileChooser("E:\\University\\LabProject\\2016\\NSL\\Tool\\src\\testTarget\\Preprocessing\\ori");
			
			fileDlg.setFileFilter(new javax.swing.filechooser.FileFilter() {
                
                @Override
                public String getDescription() {
                    return "Preprocessing File (.pp)";
                }
                
                @Override
                public boolean accept(File f) {
                    String fileName = f.getName();

                    if(fileName.indexOf(".pp") != -1) {
                        return true;
                    }
                    else if(f.isDirectory()) {
                        return true;
                    }
                    
                    return false;
                }
            });

		    int result  = fileDlg.showOpenDialog(null);
            if(result == JFileChooser.APPROVE_OPTION) {

                File selectedFile = fileDlg.getSelectedFile();
                
                XMoGenProject.getInstance().addSourceFilePath(selectedFile.getAbsolutePath());
            }
		}
		else if (e.getSource().equals(runMenuItem)) {
			
			 boolean isSuccess = XMoGenProject.getInstance().start();
			 if (isSuccess) {
				 
				 JOptionPane.showMessageDialog(null, "�۾� �Ϸ�", "Information", JOptionPane.INFORMATION_MESSAGE);
			 }
			 else {
				 
				 JOptionPane.showMessageDialog(null, "�Է� ������ �����ϴ�", "Error", JOptionPane.ERROR_MESSAGE);
			 }
		}
	}

	public void mouseClicked(MouseEvent e) {
		
		if(SwingUtilities.isLeftMouseButton(e)) {
		
			if(e.getClickCount() == 2) {
			
				TreePath tp = treeView.getPathForLocation(e.getX(), e.getY());
				if (tp.getPath().length == 3 && tp.getPathComponent(1).toString().equals("Filters")) {
					
					new XMoGenFilterEditor(desktop, tp.getPathComponent(2).toString());
				}
			}
		}
	}

	public void mouseEntered(MouseEvent e) {

		
	}

	public void mouseExited(MouseEvent e) {

		
	}

	public void mousePressed(MouseEvent e) {

		
	}

	public void mouseReleased(MouseEvent e) {

		
	}
}
