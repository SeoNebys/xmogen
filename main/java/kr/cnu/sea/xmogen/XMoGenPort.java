package kr.cnu.sea.xmogen;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;

public class XMoGenPort {

	private String type;
	private ArrayList<XMoGenData> data;
	
	public XMoGenPort(JsonNode node) {
		
		data = new ArrayList<XMoGenData>();
		initByProjectFile(node);
	}

	public void initByProjectFile(JsonNode node) {
		
		type = node.path("type").asText();
		
		for (JsonNode dataNode : node.path("data")) {
			
			data.add(new XMoGenData(dataNode));
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<XMoGenData> getData() {
		return data;
	}

	public void setData(ArrayList<XMoGenData> data) {
		this.data = data;
	}
	
	
}
