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
import kr.cnu.sea.xmogen.ks.ast.CParser.ParameterDeclarationContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.StorageClassSpecifierContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.StructDeclarationContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.StructOrUnionSpecifierContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.TypeSpecifierContext;
import kr.cnu.sea.xmogen.ks.ast.CParser.TypedefNameContext;
import kr.cnu.sea.xmogen.ks.core.ASTBasedKnowledgeSource;

public class UserDefineTypeExtractor extends ASTBasedKnowledgeSource {

	private ObjectMapper mapper;
	
	private ArrayList<String> userDefineTypeList;
	private ArrayList<JsonNode> originalTypeList;
	
	private boolean isTypedef;
	private boolean isEnumType;
	private boolean isParameter;
	
	private int enterCountOnStructOrUnionContext;
	
	private String typeStr;
	private Stack<String> typeStrStack;
	
	private HashMap<Integer, JsonNode> structFieldStorage;
	
	@Override
	public void setUp(JsonNode input) {
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		logger.log(this.getClass().getName(), "Start", input);
		
		super.setUp(input);
		
		mapper = new ObjectMapper();
		
		userDefineTypeList = new ArrayList<String>();
		originalTypeList = new ArrayList<JsonNode>();
		
		isTypedef = false;
		isEnumType = false;
		isParameter = false;
		
		enterCountOnStructOrUnionContext = 0;
		
		typeStr = "";
		typeStrStack = new Stack<String>();
		structFieldStorage = new HashMap<Integer, JsonNode>();
	}
	
	@Override
	public JsonNode tearDown() {
		
		ArrayNode typeList = mapper.createArrayNode();
		for (int i = 0; i < userDefineTypeList.size(); i++) {
			
			ObjectNode typedefOrigianlPair = mapper.createObjectNode();
			typedefOrigianlPair.put("userDefineType", userDefineTypeList.get(i));
			typedefOrigianlPair.set("originalType", originalTypeList.get(i));
			
			typeList.add(typedefOrigianlPair);
		}
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		logger.log(this.getClass().getName(), "End", typeList);
		
		return typeList;
	}
	
	@Override
	public void enterStorageClassSpecifier(StorageClassSpecifierContext ctx) {

		if (ctx.getText().equals("typedef")) {
			
			isTypedef = true;
		}
		
		typeStr = "";
	}
	
	@Override
	public void exitDeclaration(DeclarationContext ctx) {
		
		isTypedef = false;
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
	public void enterTypedefName(TypedefNameContext ctx) {
		
		if (isTypedef
				&& !isEnumType
				&& !isParameter) {
			
			String typedefName = ctx.getText();
			if (userDefineTypeList.contains(typedefName) == false) {
			
				if (enterCountOnStructOrUnionContext == 0) {
				
					userDefineTypeList.add(typedefName);
				}
				
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
				
				ObjectNode typeObject = mapper.createObjectNode();
				
				typeObject.put("type", typeStr);
				
				if (structFieldStorage.containsKey(enterCountOnStructOrUnionContext + 1)) {
					
					ArrayNode childFieldArray = (ArrayNode) structFieldStorage.get(enterCountOnStructOrUnionContext + 1);
					typeObject.set("fields", childFieldArray);
					
					structFieldStorage.remove(enterCountOnStructOrUnionContext + 1);
				}
				
				if (enterCountOnStructOrUnionContext > 0) {
					
					typeObject.put("name", varName);
					
					ArrayNode fieldArray = null;
					if (structFieldStorage.containsKey(enterCountOnStructOrUnionContext)) {
						
						fieldArray = (ArrayNode) structFieldStorage.get(enterCountOnStructOrUnionContext);
					}
					else {
						
						fieldArray = mapper.createArrayNode();
					}
					
					fieldArray.add(typeObject);
					structFieldStorage.put(enterCountOnStructOrUnionContext, fieldArray);	
				}
				else {
				
					originalTypeList.add(typeObject);
				}
			}
		}
	}
	
	@Override
	public void enterDirectDeclarator(DirectDeclaratorContext ctx) {
		
		if (isTypedef
				&& enterCountOnStructOrUnionContext == 0
				&& !isEnumType
				&& !isParameter) {
			
			String typedefName = ctx.getText();
			if (ctx.Identifier() != null
					&& userDefineTypeList.contains(typedefName) == false) {
			
				userDefineTypeList.add(typedefName);
			}
		}
	}
	
	@Override
	public void enterDeclarator(DeclaratorContext ctx) {
		
		if (isTypedef
				&& !isEnumType
				&& !isParameter) {
			
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
			
			ObjectNode typeObject = mapper.createObjectNode();
			
			typeObject.put("type", typeStr);
			
			if (structFieldStorage.containsKey(enterCountOnStructOrUnionContext + 1)) {
				
				ArrayNode childFieldArray = (ArrayNode) structFieldStorage.get(enterCountOnStructOrUnionContext + 1);
				typeObject.set("fields", childFieldArray);
				
				structFieldStorage.remove(enterCountOnStructOrUnionContext + 1);
			}
			
			if (enterCountOnStructOrUnionContext > 0) {
				
				typeObject.put("name", varName);
				
				ArrayNode fieldArray = null;
				if (structFieldStorage.containsKey(enterCountOnStructOrUnionContext)) {
					
					fieldArray = (ArrayNode) structFieldStorage.get(enterCountOnStructOrUnionContext);
				}
				else {
					
					fieldArray = mapper.createArrayNode();
				}
				
				fieldArray.add(typeObject);
				structFieldStorage.put(enterCountOnStructOrUnionContext, fieldArray);	
			}
			else {
			
				originalTypeList.add(typeObject);
			}
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
	public void enterEnumSpecifier(EnumSpecifierContext ctx) {
		
		isEnumType = true;
	}
	
	@Override
	public void exitEnumSpecifier(EnumSpecifierContext ctx) {
		
		isEnumType = false;
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
