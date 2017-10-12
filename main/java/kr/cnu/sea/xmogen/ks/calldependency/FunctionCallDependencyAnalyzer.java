package kr.cnu.sea.xmogen.ks.calldependency;

import java.io.IOException;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.cnu.sea.xmogen.blackboard.XMoGenLogger;
import kr.cnu.sea.xmogen.ks.core.KnowledgeSource;

public class FunctionCallDependencyAnalyzer implements KnowledgeSource {

	private ObjectMapper mapper;
	
	private JsonNode functionList;
	private DefaultDirectedGraph<String, DefaultEdge> callDependencyGraph;

	public void setUp(JsonNode input) {
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		logger.log(this.getClass().getName(), "Start", input);
		
		mapper = new ObjectMapper();
		try {
			
			String jsonText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(input);
			functionList = mapper.readTree(jsonText);
		} 
		catch (IOException e) {

			e.printStackTrace();
		}

		callDependencyGraph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
	}

	public void execute() {

		for (JsonNode function : functionList) {
			
			String name = function.path("name").asText();
			
			callDependencyGraph.addVertex(name);
			
			CalleeExtractor calleeExtractor = new CalleeExtractor();
			calleeExtractor.setUp(function);
			calleeExtractor.execute();
			
			JsonNode calleeExtractorResult = calleeExtractor.tearDown();
			for (JsonNode callee : calleeExtractorResult.path("calleeList")) {
				
				String calleeName = callee.asText();
				
				callDependencyGraph.addVertex(calleeName);
				callDependencyGraph.addEdge(name, calleeName);
			}
		}
	}

	public JsonNode tearDown() {
		
		ArrayNode vertexList = mapper.createArrayNode();
		for (String vertex : callDependencyGraph.vertexSet()) {
			
			vertexList.add(vertex);
		}
		
		ArrayNode edgeList = mapper.createArrayNode();
		for (DefaultEdge edge : callDependencyGraph.edgeSet()) {
			
			ObjectNode edgeNode = mapper.createObjectNode();
			edgeNode.put("source", callDependencyGraph.getEdgeSource(edge));
			edgeNode.put("target", callDependencyGraph.getEdgeTarget(edge));
			
			edgeList.add(edgeNode);
		}
		
		ObjectNode cdgNode = mapper.createObjectNode();
		cdgNode.set("vertex", vertexList);
		cdgNode.set("edge", edgeList);
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		logger.log(this.getClass().getName(), "End", cdgNode);

		return cdgNode;
	}

	
}
