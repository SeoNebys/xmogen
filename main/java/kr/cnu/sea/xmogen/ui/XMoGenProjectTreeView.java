package kr.cnu.sea.xmogen.ui;

import javax.swing.JDesktopPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.tree.DefaultMutableTreeNode;

public class XMoGenProjectTreeView extends JTree {
	
	private JDesktopPane desktop = null;
	public static DefaultMutableTreeNode top = new DefaultMutableTreeNode("Root");

	public XMoGenProjectTreeView(JDesktopPane desktop) {
		
		super(top);
		
		this.desktop = desktop;
		
		try {
		   	for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("com.sun.java.swing.plaf.windows.WindowsLookAndFeel".equals(info.getName())) {            
					UIManager.setLookAndFeel(info.getClassName());           
					break ;
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
}
