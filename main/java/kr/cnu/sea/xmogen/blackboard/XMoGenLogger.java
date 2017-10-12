package kr.cnu.sea.xmogen.blackboard;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class XMoGenLogger {

	private static XMoGenLogger instance;
	private JsonNode logger;
	private ObjectMapper mapper;
	
    private XMoGenLogger() {
    	
    	mapper = new ObjectMapper();
    	logger = (JsonNode) mapper.createArrayNode();
    }
	
    public static XMoGenLogger getInstance() {
        
    	if(instance == null) {
        	
            instance = new XMoGenLogger();
        }
        
        return instance;
    }
    
    public void log(String name, String type, JsonNode info) {
    	
    	ObjectNode logNode = mapper.createObjectNode();
    	
    	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    	Calendar calendar = Calendar.getInstance();
    	
    	logNode.put("timestamp", dateFormat.format(calendar.getTime()));
    	logNode.put("name", name);
    	logNode.put("type", type);
    	logNode.set("data", info);
    	
    	((ArrayNode)logger).add(logNode);
    }
    
    public JsonNode getLog(String name) {
    	
    	if (name.equals("All")) {
    		
    		return logger;
    	}
    	
    	ArrayNode returnLogs = mapper.createArrayNode();
    	for (JsonNode log : logger) {
			
    		if (log.path("name").asText().equals(name)) {
    			
    			returnLogs.add(log);
    		}
		} 
    	
    	return returnLogs;
    }
    
    public void print() {
    	
    	System.out.println(logger.toString());
    }
}
