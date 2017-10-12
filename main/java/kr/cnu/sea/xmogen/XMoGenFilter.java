package kr.cnu.sea.xmogen;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;

public class XMoGenFilter {

	private String name;
	private String packageName;
	
	private ArrayList<XMoGenPort> ports;
	
	public XMoGenFilter(JsonNode node) {
	
		ports = new ArrayList<XMoGenPort>();
		
		initByProjectFile(node);
	}
	
	public void initByProjectFile(JsonNode node) {
		
		name = node.path("name").asText();
		packageName = node.path("package").asText();
		
		for (JsonNode portNode : node.path("port")) {
			
			ports.add(new XMoGenPort(portNode));
		}
	}
	
	public String getName() {
		
		return name;
	}
	
	public void setName(String name) {
		
		this.name = name;
	}
	
	public String getPackageName() {
		
		return packageName;
	}
	
	public void setPackageName(String packageName) {
		
		this.packageName = packageName;
	}

	public ArrayList<XMoGenPort> getPorts() {
		
		return ports;
	}

	public void setPorts(ArrayList<XMoGenPort> ports) {
		
		this.ports = ports;
	}
	
	
}
