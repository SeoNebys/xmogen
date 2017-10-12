package kr.cnu.sea.xmogen.ks.core;

import com.fasterxml.jackson.databind.JsonNode;

public interface KnowledgeSource {

	public abstract void setUp(JsonNode input);
	public abstract void execute();
	public abstract JsonNode tearDown();
}
