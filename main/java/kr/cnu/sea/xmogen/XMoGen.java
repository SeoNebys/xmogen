package kr.cnu.sea.xmogen;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import kr.cnu.sea.xmogen.ui.XMoGenMainUI;

import javax.swing.UIManager.LookAndFeelInfo;

public class XMoGen {

	public static void main(String[] args) {
		
		String currentDir = System.getProperty("user.dir");
		
		XMoGenProject project = XMoGenProject.getInstance();
		project.inputProjectFilePath(currentDir + "\\project.json");
		
		try {
		   	for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {            
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
		
		new XMoGenMainUI();
	}

}
