package kr.cnu.sea.xmogen.blackboard;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.antlr.v4.runtime.ParserRuleContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class XMoGenRepository {
	
	public enum KindOfInformation {File, Function, Type, Variable};
	
	private static XMoGenRepository instance;
	private ObjectMapper mapper;
	
	private JsonNode blackboard;
	private HashMap<String, ParserRuleContext> astMap;
     
    private XMoGenRepository() {
    	
    	init();
    }
     
    public static XMoGenRepository getInstance() {
        
    	if(instance == null) {
        	
            instance = new XMoGenRepository();
        }
        
        return instance;
    }
    
    public void init() {
    	
    	mapper = new ObjectMapper();
    	
    	try {
    		
    		String key = UUID.randomUUID().toString();
			blackboard = mapper.readTree("{\"xMoGen\":{\"uuid\":\"" + key + "\",\"name\":\"Partitioning\",\"file\":[],\"function\":[],\"variable\":[],\"type\":[],\"dependency\":{},\"partitioning\":{}}}");
    		// blackboard = mapper.readTree(new File("E:\\University\\LabProject\\2016\\NSL\\Tool\\test.txt"));
		} 
    	catch (IOException e) {
			
			e.printStackTrace();
		}
    	
    	astMap = new HashMap<String, ParserRuleContext>();
    }
    
    public String registAst(ParserRuleContext ast) {
    	
    	String key = UUID.randomUUID().toString(); 
    	while (isExist(key)) {
    		
    		key = UUID.randomUUID().toString();
    	}
    	
    	astMap.put(key, ast);
    	
    	return key;
    }
    
    public String getKey() {
    	
    	String key = UUID.randomUUID().toString(); 
    	while (isExist(key)) {
    		
    		key = UUID.randomUUID().toString();
    	}
    	
    	return key;
    }
    
    public ParserRuleContext accessAst(String key) {
    	
    	ParserRuleContext ast = null;
    	if (isExist(key)) {
    		
    		ast = astMap.get(key);
    	}
    	
    	return ast;
    }
    
    public void update(String id, JsonNode insertInformation) {
    	
    	for (JsonNode node : blackboard.findParents("uuid")) {
    		
    		if (node.path("uuid").asText().equals(id)) {
    			
    			((ObjectNode) node).setAll((ObjectNode) insertInformation);
    			break;
    		}
    	}
    }
    
    public void insert(String path, JsonNode insertInformation) {
    	
    	String[] pathElement = path.split("/");
    	
    	JsonNode node = blackboard.path("xMoGen");
    	for (int i = 1; i < pathElement.length; i++) {
			
    		node = node.path(pathElement[i]);
		}
    	
    	ObjectNode insertNode = mapper.createObjectNode();
    		
    	String key = UUID.randomUUID().toString(); 
    	while (isExist(key)) {
    		
    		key = UUID.randomUUID().toString();
    	}
		
    	if (insertInformation.isArray()) {
    		
    		insertNode.set("partitions", insertInformation);
    	}
    	else {
    		
    		insertNode.put("uuid", key);
    		insertNode.setAll((ObjectNode) insertInformation);
    	}
    	
		if (node.isArray()) {
		
			((ArrayNode) node).add(insertNode);
		}
		else {
			
			((ObjectNode) node).setAll(insertNode);
		}
    }
    
    public JsonNode select(String path) {
    	
    	String[] pathElement = path.split("/");
    	
    	JsonNode node = blackboard.path("xMoGen").path(pathElement[1]);
    	
    	if (pathElement.length == 3) {
    		
    		ArrayNode queryResult = mapper.createArrayNode();
    		
	    	for (JsonNode itemNode : node) {
				
	    		ObjectNode queryItem = mapper.createObjectNode();
	    		
	    		queryItem.put("uuid", itemNode.path("uuid").asText());
	    		queryItem.set(pathElement[2], itemNode.path(pathElement[2]));
	    		
	    		queryResult.add(queryItem);
			}
	    	
	    	return queryResult;
    	}
    	
    	return node;
    }
    
    public void print() {
    	
    	System.out.println(blackboard.toString());
    }
    
    private boolean isExist(String uuidKey) {
    	
    	boolean isExistUuid = false;
    	for (String uuid : blackboard.findValuesAsText("uuid")) {
    		
    		if (uuid.equals(uuidKey)) {
    			
    			isExistUuid = true;
    		}
    	}
    	
    	return astMap.containsKey(uuidKey) || isExistUuid;
    }
}
