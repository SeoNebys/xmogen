package kr.cnu.sea.xmogen.ks.core;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.fasterxml.jackson.databind.JsonNode;

import kr.cnu.sea.xmogen.blackboard.XMoGenRepository;
import kr.cnu.sea.xmogen.ks.ast.CBaseListener;

public class ASTBasedKnowledgeSource extends CBaseListener implements KnowledgeSource {

	private ParserRuleContext context;

	public void setUp(JsonNode input) {
		
		String key = input.path("ast").asText();
		XMoGenRepository astRegistry = XMoGenRepository.getInstance();
		
		this.context = astRegistry.accessAst(key);
		if (this.context == null) {
			
			System.out.println(key);
			astRegistry.print();
		}
	}

	public void execute() {

		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(this, context);
	}

	public JsonNode tearDown() {
		
		return null;
	}

}
