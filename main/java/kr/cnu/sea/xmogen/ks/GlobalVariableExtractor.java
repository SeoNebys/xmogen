package kr.cnu.sea.xmogen.ks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.antlr.v4.runtime.ParserRuleContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.cnu.sea.xmogen.blackboard.XMoGenLogger;
import kr.cnu.sea.xmogen.ks.ast.CParser.DeclarationContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.DeclaratorContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.DirectDeclaratorContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.EnumSpecifierContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.FunctionDefinitionContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.ParameterDeclarationContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.StorageClassSpecifierContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.StructDeclarationContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.StructOrUnionSpecifierContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.TypeSpecifierContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.TypedefNameContext;
import kr.cnu.sea.xmogen.ks.core.ASTBasedKnowledgeSource;

public class GlobalVariableExtractor extends ASTBasedKnowledgeSource {

	private ObjectMapper mapper;
	
	private ArrayList<String> userDefineTypeList;
	
	private boolean isTypedef;
	private boolean isParameter;
	private boolean isGlobal;
	private boolean isFunctionDeclaration;
	
	private int enterCountOnStructOrUnionContext;
	
	private String typeStr;
	private Stack<String> typeStrStack;
	
	private HashMap<Integer, JsonNode> structFieldStorage;
	
	private ArrayList<JsonNode> globalVariables;
	
	@Override
	public void setUp(JsonNode input) {
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		logger.log(this.getClass().getName(), "Start", input);
		
		mapper = new ObjectMapper();
		
		super.setUp(input);
		
		isGlobal = true;
		userDefineTypeList = new ArrayList<String>();
		
		enterCountOnStructOrUnionContext = 0;
		
		typeStr = "";
		typeStrStack = new Stack<String>();
		structFieldStorage = new HashMap<Integer, JsonNode>();
		globalVariables = new ArrayList<JsonNode>();
		
		for (JsonNode node : input.path("type")) {
			
			userDefineTypeList.add(node.path("userDefineType").asText());
		}
	}
	
	@Override
	public JsonNode tearDown() {
		
		ArrayNode globalVariableArray = mapper.createArrayNode();
		for (JsonNode jsonObject : globalVariables) {
			
			globalVariableArray.add(jsonObject);
		}
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		logger.log(this.getClass().getName(), "End", globalVariableArray);
		
		return globalVariableArray;
	}
	
	private boolean checkFunctionDeclaration(DirectDeclaratorContext ctx) {
		
		int childCount = ctx.getChildCount();
		
		if (childCount == 4 
				&& (ctx.parameterTypeList() != null || ctx.identifierList() != null)) {
			
			return true;
		}
		else if (childCount == 3 
				&& ctx.getChild(1).getText().equals("(")) {
			
			return true;
		}

		return false;
	}
	
	@Override
	public void enterDeclaration(DeclarationContext ctx) {

		DirectDeclaratorContext checkContext = null;
		try {
			
			checkContext = ctx.initDeclaratorList().initDeclarator().declarator().directDeclarator();
			isFunctionDeclaration = checkFunctionDeclaration(checkContext);
			
		} catch (Exception e) {
			
			isFunctionDeclaration = false;
		}
		
		typeStr = "";
	}
	
	@Override
	public void exitDeclaration(DeclarationContext ctx) {
		
		isTypedef = false;
		isFunctionDeclaration = false;
	}
	
	@Override
	public void enterFunctionDefinition(FunctionDefinitionContext ctx) {

		isGlobal = false;
	}
	
	@Override
	public void exitFunctionDefinition(FunctionDefinitionContext ctx) {

		isGlobal = true;;
	}
	
	@Override
	public void enterStorageClassSpecifier(StorageClassSpecifierContext ctx) {

		if (ctx.getText().equals("typedef")) {
			
			isTypedef = true;
		}
	}
	
	@Override
	public void enterTypeSpecifier(TypeSpecifierContext ctx) {
		
		if (ctx.getChild(0) instanceof StructOrUnionSpecifierContext) {
		
			enterCountOnStructOrUnionContext++;
			
			ParserRuleContext childContext = (ParserRuleContext) ctx.getChild(0);
			
			typeStr = childContext.getChild(0).getText() + " ";
			if (childContext.getChild(1).getText().equals("{") == false) {
				
				typeStr += childContext.getChild(1).getText() + " ";
			}
			
			typeStrStack.push(typeStr);
		}
		else if (ctx.getChild(0) instanceof EnumSpecifierContext) {
			
			
		}
		else {
			
			typeStr += ctx.getText() + " ";
		}
	}
	
	@Override
	public void enterStructDeclaration(StructDeclarationContext ctx) {
		
		typeStr = "";
	}
	
	@Override
	public void exitStructOrUnionSpecifier(StructOrUnionSpecifierContext ctx) {
		
		typeStr = typeStrStack.pop();
		enterCountOnStructOrUnionContext--;
	}
	
	@Override
	public void enterDeclarator(DeclaratorContext ctx) {
		
		if (isTypedef == false 
				&& isParameter == false 
				&& isFunctionDeclaration == false 
				&& isGlobal) {
			
			String varName = ctx.getText();
			int pointerIndex = varName.lastIndexOf('*');
			int arrayIndex = varName.indexOf('[');
			
			if (typeStr.length() > 0 
					&& typeStr.endsWith(" ")) {
				
				typeStr = typeStr.substring(0, typeStr.length() - 1);
			}
			
			if (pointerIndex > -1) {
			
				typeStr += varName.substring(0, pointerIndex + 1);
				varName = varName.substring(pointerIndex + 1);
			}
			else if (arrayIndex > -1) {
			
				typeStr += varName.substring(arrayIndex);
				varName = varName.substring(0, arrayIndex);
			}
			
			ObjectNode varObject = mapper.createObjectNode();
			
			varObject.put("type", typeStr);
			varObject.put("name", varName);
			
			if (structFieldStorage.containsKey(enterCountOnStructOrUnionContext + 1)) {
				
				ArrayNode childFieldArray = (ArrayNode) structFieldStorage.get(enterCountOnStructOrUnionContext + 1);
				varObject.set("fields", childFieldArray);
				
				structFieldStorage.remove(enterCountOnStructOrUnionContext + 1);
			}
			
			if (enterCountOnStructOrUnionContext > 0) {
				
				ArrayNode fieldArray = null;
				if (structFieldStorage.containsKey(enterCountOnStructOrUnionContext)) {
					
					fieldArray = (ArrayNode) structFieldStorage.get(enterCountOnStructOrUnionContext);
				}
				else {
					
					fieldArray = mapper.createArrayNode();
				}
				
				fieldArray.add(varObject);
				structFieldStorage.put(enterCountOnStructOrUnionContext, fieldArray);	
			}
			else {
			
				globalVariables.add(varObject);
			}
		}
	}
	
	@Override
	public void enterTypedefName(TypedefNameContext ctx) {
		
		if (isTypedef == false 
				&& isParameter == false 
				&& isFunctionDeclaration == false 
				&& isGlobal) {
			
			String typedefName = ctx.getText();
			if (userDefineTypeList.contains(typedefName) == false) {
			
				String varName = ctx.getText();
				int pointerIndex = varName.lastIndexOf('*');
				int arrayIndex = varName.indexOf('[');
				
				if (typeStr.contains(varName)) {
					
					typeStr = typeStr.substring(0, typeStr.indexOf(varName));
				}
				
				if (typeStr.length() > 0 
						&& typeStr.endsWith(" ")) {
					
					typeStr = typeStr.substring(0, typeStr.length() - 1);
				}
				
				if (pointerIndex > -1) {
				
					typeStr += varName.substring(0, pointerIndex + 1);
					varName = varName.substring(pointerIndex + 1);
				}
				else if (arrayIndex > -1) {
				
					typeStr += varName.substring(arrayIndex);
					varName = varName.substring(0, arrayIndex);
				}
				
				ObjectNode varObject = mapper.createObjectNode();
				
				varObject.put("type", typeStr);
				varObject.put("name", varName);
				
				if (structFieldStorage.containsKey(enterCountOnStructOrUnionContext + 1)) {
					
					ArrayNode childFieldArray = (ArrayNode) structFieldStorage.get(enterCountOnStructOrUnionContext + 1);
					varObject.set("fields", childFieldArray);
					
					structFieldStorage.remove(enterCountOnStructOrUnionContext + 1);
				}
				
				if (enterCountOnStructOrUnionContext > 0) {
				
					ArrayNode fieldArray = null;
					if (structFieldStorage.containsKey(enterCountOnStructOrUnionContext)) {
						
						fieldArray = (ArrayNode) structFieldStorage.get(enterCountOnStructOrUnionContext);
					}
					else {
						
						fieldArray = mapper.createArrayNode();
					}
					
					fieldArray.add(varObject);
					structFieldStorage.put(enterCountOnStructOrUnionContext, fieldArray);	
				}
				else {
				
					globalVariables.add(varObject);
				}
			}
		}
	}
	
	@Override
	public void enterParameterDeclaration(ParameterDeclarationContext ctx) {

		isParameter = true;
	}
	
	@Override
	public void exitParameterDeclaration(ParameterDeclarationContext ctx) {

		isParameter = false;
	}
}
