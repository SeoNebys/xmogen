package kr.cnu.sea.xmogen.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.cnu.sea.xmogen.XMoGenData;
import kr.cnu.sea.xmogen.XMoGenFilter;
import kr.cnu.sea.xmogen.XMoGenPort;
import kr.cnu.sea.xmogen.XMoGenProject;
import kr.cnu.sea.xmogen.blackboard.XMoGenLogger;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import javax.swing.JTextArea;

public class XMoGenFilterEditor extends JInternalFrame implements Observer, ListSelectionListener {
	
	private JTextField idText;
	private JTextField packageText;
	private JTextField nameText;
	private JTextField vendorText;
	private JTable table;
	private JTree portTree;
	private JTextArea detailText;
	
	private XMoGenFilter filter;
	
	private String columnNames[] = { "Timestamp", "Name", "Type" };
	
	public XMoGenFilterEditor(JDesktopPane desktop, String name) {

		super(name);
		
		XMoGenProject projectModel = XMoGenProject.getInstance();
		filter = projectModel.getFilter(name);
		
		for (JInternalFrame frame : desktop.getAllFrames()) {
			
			if (frame.getTitle().equals(name)) {
				
				desktop.getDesktopManager().activateFrame(frame);
				return ;
			}
		}
		setClosable(true);
		
		desktop.add(this);
		
		this.setSize(400, 416);
		this.setVisible(true);
		try {
			this.setSelected(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		
		initView();
		
		nameText.setText(filter.getName());
		packageText.setText(filter.getPackageName());
		vendorText.setText("CNU SEAL");
		
		projectModel.registObserver(this);
	}
	
	@Override
	protected void finalize() throws Throwable {
		
		XMoGenProject projectModel = XMoGenProject.getInstance();
		projectModel.unregistObserver(this);
		
		super.finalize();
	}
	
	public void refresh() {
	
		portTree.setModel(new DefaultTreeModel(getPortTreeNodes()));
		portTree.repaint();

		table.setModel(getTraceTableData());
		table.repaint();
	}
	
	private DefaultTableModel getTraceTableData() {
		
		DefaultTableModel defaultTableModel = new DefaultTableModel();
		defaultTableModel.setColumnIdentifiers(columnNames);
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		for (JsonNode node : logger.getLog(filter.getPackageName() + "." + filter.getName())) {
			
			ArrayList<String> row = new ArrayList<String>();
			
			row.add(node.path("timestamp").asText());
			row.add(node.path("name").asText());
			row.add(node.path("type").asText());
			
			defaultTableModel.addRow(row.toArray());
		}

		return defaultTableModel;
	}
	
	private DefaultMutableTreeNode getPortTreeNodes() {
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Ports");
		for (XMoGenPort port : filter.getPorts()) {
			
			DefaultMutableTreeNode portNode = new DefaultMutableTreeNode(port.getType());
			
			for (XMoGenData data : port.getData()) {
				
				DefaultMutableTreeNode dataNode = new DefaultMutableTreeNode(data.getName() + ":" + data.getType());
				
				portNode.add(dataNode);
			}
			
			root.add(portNode);
		}

		return root;
	}
	
	private DefaultMutableTreeNode getJsonTreeNodes() {
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		JsonNode node = logger.getLog(filter.getPackageName() + "." + filter.getName());
		
		return getJsonTreeNodes(node);
	}
	
	private DefaultMutableTreeNode getJsonTreeNodes(JsonNode node) {
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		
		if (node.isMissingNode() != false) {
		
			
		}
		
		return root;
	}
	
	private void initView() {
		
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		getContentPane().add(tabbedPane);
		
		JPanel overviewPanel = new JPanel();
		tabbedPane.addTab("Overview", null, overviewPanel, null);
		GridBagLayout gbl_overviewPanel = new GridBagLayout();
		gbl_overviewPanel.columnWidths = new int[]{379, 0};
		gbl_overviewPanel.rowHeights = new int[]{134, 0, 0};
		gbl_overviewPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_overviewPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		overviewPanel.setLayout(gbl_overviewPanel);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "General Information", TitledBorder.LEADING, TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		overviewPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblName = new JLabel("ID");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		panel.add(lblName, gbc_lblName);
		
		idText = new JTextField();
		GridBagConstraints gbc_idText = new GridBagConstraints();
		gbc_idText.insets = new Insets(0, 0, 5, 0);
		gbc_idText.fill = GridBagConstraints.HORIZONTAL;
		gbc_idText.gridx = 1;
		gbc_idText.gridy = 0;
		panel.add(idText, gbc_idText);
		idText.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Package");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		packageText = new JTextField();
		GridBagConstraints gbc_packageText = new GridBagConstraints();
		gbc_packageText.insets = new Insets(0, 0, 5, 0);
		gbc_packageText.fill = GridBagConstraints.HORIZONTAL;
		gbc_packageText.gridx = 1;
		gbc_packageText.gridy = 1;
		panel.add(packageText, gbc_packageText);
		packageText.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Name");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		nameText = new JTextField();
		GridBagConstraints gbc_nameText = new GridBagConstraints();
		gbc_nameText.insets = new Insets(0, 0, 5, 0);
		gbc_nameText.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameText.gridx = 1;
		gbc_nameText.gridy = 2;
		panel.add(nameText, gbc_nameText);
		nameText.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("Vendor");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 3;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		vendorText = new JTextField();
		GridBagConstraints gbc_vendorText = new GridBagConstraints();
		gbc_vendorText.fill = GridBagConstraints.HORIZONTAL;
		gbc_vendorText.gridx = 1;
		gbc_vendorText.gridy = 3;
		panel.add(vendorText, gbc_vendorText);
		vendorText.setColumns(10);
		
		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		overviewPanel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{187, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Port Information", TitledBorder.LEADING, TitledBorder.ABOVE_TOP, null, null));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 0, 5);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 0;
		panel_1.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0};
		gbl_panel_2.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		portTree = new JTree(getPortTreeNodes());
		GridBagConstraints gbc_portTree = new GridBagConstraints();
		gbc_portTree.fill = GridBagConstraints.BOTH;
		gbc_portTree.gridx = 0;
		gbc_portTree.gridy = 0;
		panel_2.add(portTree, gbc_portTree);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new TitledBorder(null, "Connection Information", TitledBorder.LEADING, TitledBorder.ABOVE_TOP, null, null));
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.fill = GridBagConstraints.BOTH;
		gbc_panel_5.gridx = 1;
		gbc_panel_5.gridy = 0;
		panel_1.add(panel_5, gbc_panel_5);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		gbl_panel_5.columnWidths = new int[]{0, 0};
		gbl_panel_5.rowHeights = new int[]{0, 0};
		gbl_panel_5.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_5.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_5.setLayout(gbl_panel_5);
		
		JList connectionList = new JList();
		GridBagConstraints gbc_connectionList = new GridBagConstraints();
		gbc_connectionList.fill = GridBagConstraints.BOTH;
		gbc_connectionList.gridx = 0;
		gbc_connectionList.gridy = 0;
		panel_5.add(connectionList, gbc_connectionList);
		
		JPanel tracePanel = new JPanel();
		tabbedPane.addTab("Trace", null, tracePanel, null);
		GridBagLayout gbl_tracePanel = new GridBagLayout();
		gbl_tracePanel.columnWidths = new int[]{0, 0};
		gbl_tracePanel.rowHeights = new int[]{0, 0, 0};
		gbl_tracePanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_tracePanel.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		tracePanel.setLayout(gbl_tracePanel);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(null, "Execution Trace", TitledBorder.LEADING, TitledBorder.ABOVE_TOP, null, null));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 0;
		tracePanel.add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{0, 0};
		gbl_panel_3.rowHeights = new int[]{0, 0};
		gbl_panel_3.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 0;
		gbc_scrollPane_2.gridy = 0;
		panel_3.add(scrollPane_2, gbc_scrollPane_2);
		
		table = new JTable(getTraceTableData());

		ListSelectionModel cellSelectionModel = table.getSelectionModel();
	    cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	    cellSelectionModel.addListSelectionListener(this);

		scrollPane_2.setViewportView(table);
		
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(null, "Trace Detail", TitledBorder.LEADING, TitledBorder.ABOVE_TOP, null, null));
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 1;
		tracePanel.add(panel_4, gbc_panel_4);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[]{185, 0};
		gbl_panel_4.rowHeights = new int[]{0, 0};
		gbl_panel_4.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_4.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_4.setLayout(gbl_panel_4);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel_4.add(scrollPane, gbc_scrollPane);
		
		detailText = new JTextArea();
		scrollPane.setViewportView(detailText);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		int[] selectedRow = table.getSelectedRows();
        
		XMoGenLogger logger = XMoGenLogger.getInstance();
		JsonNode selectData = logger.getLog(filter.getPackageName() + "." + filter.getName()).get(selectedRow[0]);
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			String jsonText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(selectData.path("data"));
			detailText.setText("");
			detailText.setText(jsonText);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
	}
}
