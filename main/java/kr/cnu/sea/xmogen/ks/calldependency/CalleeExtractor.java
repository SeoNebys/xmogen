package kr.cnu.sea.xmogen.ks.calldependency;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.cnu.sea.xmogen.blackboard.XMoGenLogger;
import kr.cnu.sea.xmogen.ks.ast.CParser.PostfixExpressionContext;
import kr.cnu.sea.xmogen.ks.core.ASTBasedKnowledgeSource;

public class CalleeExtractor extends ASTBasedKnowledgeSource {

	private ArrayList<String> calleeList;
	
	@Override
	public void setUp(JsonNode input) {
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		logger.log(this.getClass().getName(), "Start", input);
		
		super.setUp(input);
		
		calleeList = new ArrayList<String>();
	}
	
	@Override
	public JsonNode tearDown() {
		
		ObjectMapper mapper = new ObjectMapper();
		
		ObjectNode result = mapper.createObjectNode();
		ArrayNode callees = mapper.createArrayNode();
		for (String callee : calleeList) {
			
			callees.add(callee);
		}
		
		result.set("calleeList", callees);
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		logger.log(this.getClass().getName(), "End", result);
		
		return result;
	}
	
	@Override
	public void enterPostfixExpression(PostfixExpressionContext ctx) {
	
		if (ctx.argumentExpressionList() != null) {
		
			String functionName = ctx.postfixExpression().getText();
			if (functionName.contains("Uint32") == false) {
				
				calleeList.add(functionName);
			}
		}
		else if (ctx.getChildCount() == 3 && ctx.getChild(1).equals("(")) {
			
			calleeList.add(ctx.postfixExpression().getText());
		}
	}
}
