package kr.cnu.sea.xmogen.ks.ast;

import java.io.File;
import java.io.IOException;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.cnu.sea.xmogen.blackboard.XMoGenLogger;
import kr.cnu.sea.xmogen.blackboard.XMoGenRepository;
import kr.cnu.sea.xmogen.ks.core.KnowledgeSource;

public class ASTGenerator implements KnowledgeSource {

	private File file;
	private ParserRuleContext ast;

	public void setUp(JsonNode input) {
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		logger.log(this.getClass().getName(), "Start", input);
		
		file = new File(input.path("path").asText());
	}

	public void execute() {
		
        try {
        	
            ANTLRFileStream input = new ANTLRFileStream(file.getAbsolutePath());

            // Create a lexer that feeds off of input CharStream
            CLexer lexer = new CLexer(input);

            // Create a buffer of tokens pulled from the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // Create a parser that feeds off the tokens buffer
            CParser parser = new CParser(tokens);

            // Begin parsing at rule prog
            this.ast = parser.translationUnit();
        } 
        catch (IOException e) {
           
            e.printStackTrace();
        }
	}

	public JsonNode tearDown() {

		XMoGenRepository repository = XMoGenRepository.getInstance();
		String key = repository.registAst(ast);
		
		ObjectMapper mapper = new ObjectMapper();
		
		ObjectNode jsonMessage = mapper.createObjectNode();
		jsonMessage.put("name", file.getName());
		jsonMessage.put("ast", key);
		
		XMoGenLogger logger = XMoGenLogger.getInstance();
		logger.log(this.getClass().getName(), "End", jsonMessage);

		return jsonMessage;
	}
}
