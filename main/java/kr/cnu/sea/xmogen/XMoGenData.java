package kr.cnu.sea.xmogen;

import com.fasterxml.jackson.databind.JsonNode;

public class XMoGenData {

	private String name;
	private String type;
	
	public XMoGenData(JsonNode node) {
		
		name = node.path("name").asText();
		type = node.path("type").asText();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
