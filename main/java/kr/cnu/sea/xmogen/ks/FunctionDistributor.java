package kr.cnu.sea.xmogen.ks;

import java.util.HashMap;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.cnu.sea.xmogen.blackboard.XMoGenLogger;
import kr.cnu.sea.xmogen.ks.core.KnowledgeSource;

public class FunctionDistributor implements KnowledgeSource {

	private HashMap<String, Integer> markingTable;
	private JsonNode dependencyList;

	public void setUp(JsonNode input) {
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		logger.log(this.getClass().getName(), "Start", input);
		
		markingTable = new HashMap<String, Integer>();
		
		for (JsonNode node : input.path("vertex")) {
			
			markingTable.put(node.asText(), 0);
		}
		
		dependencyList = input.path("edge");
	}

	public void execute() {

		int partitionNumber = 1;
		for (JsonNode node : dependencyList) {
		
			String depElement1 = node.path("source").asText();
			String depElement2 = node.path("target").asText();
			
			if (markingTable.get(depElement1) == 0 && markingTable.get(depElement2) == 0) {
				
				markingTable.replace(depElement1, partitionNumber);
				markingTable.replace(depElement2, partitionNumber);
				
				partitionNumber++;
			}
			else if (markingTable.get(depElement1) != 0 && markingTable.get(depElement2) == 0) {
				
				markingTable.replace(depElement2, markingTable.get(depElement1));
			}
			else if (markingTable.get(depElement1) == 0 && markingTable.get(depElement2) != 0) {
				
				markingTable.replace(depElement1, markingTable.get(depElement2));
			}
			else if (markingTable.get(depElement1) != markingTable.get(depElement2)) {
				
				for (String key : markingTable.keySet()) {
					
					if (markingTable.get(key) == markingTable.get(depElement2) 
							&& key.equals(depElement2) == false) {
						
						markingTable.replace(key, markingTable.get(depElement1));
					}
				}
				
				markingTable.replace(depElement2, markingTable.get(depElement1));
			}
		}
	}

	public JsonNode tearDown() {

		ArrayListValuedHashMap<Integer, String> reverseTable = new ArrayListValuedHashMap<Integer, String>();
		for (String key : markingTable.keySet()) {
			
			reverseTable.put(markingTable.get(key), key);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode partitionList = mapper.createArrayNode();
		for (int key : reverseTable.keySet()) {
			
			ObjectNode partition = mapper.createObjectNode();
			partition.put("number", key);
			
			ArrayNode functionList = mapper.createArrayNode();
			for (String function : reverseTable.get(key)) {
				
				functionList.add(function);
			}
			
			partition.set("element", functionList);
			partitionList.add(partition);
		}

		XMoGenLogger logger = XMoGenLogger.getInstance();
		logger.log(this.getClass().getName(), "End", partitionList);
		
		return partitionList;
	}

}
