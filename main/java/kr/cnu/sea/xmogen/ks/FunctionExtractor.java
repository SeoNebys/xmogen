package kr.cnu.sea.xmogen.ks;

import java.util.ArrayList;

import org.antlr.v4.runtime.ParserRuleContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.cnu.sea.xmogen.blackboard.XMoGenLogger;
import kr.cnu.sea.xmogen.blackboard.XMoGenRepository;
import kr.cnu.sea.xmogen.ks.ast.CParser.FunctionDefinitionContext;
import kr.cnu.sea.xmogen.ks.core.ASTBasedKnowledgeSource;

public class FunctionExtractor extends ASTBasedKnowledgeSource {

	private ArrayList<ParserRuleContext> functionAST;
	private ArrayList<String> functionName;
	
	@Override
	public void setUp(JsonNode input) {
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		logger.log(this.getClass().getName(), "Start", input);
		
		super.setUp(input);
		
		functionAST = new ArrayList<ParserRuleContext>();
		functionName = new ArrayList<String>();
	}
	
	@Override
	public void enterFunctionDefinition(FunctionDefinitionContext context) {
		
		functionAST.add(context);
		functionName.add(context.declarator().directDeclarator().directDeclarator().Identifier().getText());
	}
	
	@Override
	public JsonNode tearDown() {
		
		super.tearDown();
		
		ObjectMapper mapper = new ObjectMapper();
		
		ArrayNode functions = mapper.createArrayNode();
		for (int i = 0; i < functionAST.size(); i++) {
			
			XMoGenRepository astRegistry = XMoGenRepository.getInstance();
			String key = astRegistry.registAst(functionAST.get(i));
			
			ObjectNode function = mapper.createObjectNode();
			function.put("name", functionName.get(i));
			function.put("ast", key);
			
			functions.add(function);
		}
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		logger.log(this.getClass().getName(), "End", functions);
		
		return functions;
	}
}
