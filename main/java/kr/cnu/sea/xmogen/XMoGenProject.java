package kr.cnu.sea.xmogen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.cnu.sea.xmogen.blackboard.XMoGenRepository;
import kr.cnu.sea.xmogen.ks.FunctionDistributor;
import kr.cnu.sea.xmogen.ks.FunctionExtractor;
import kr.cnu.sea.xmogen.ks.FunctionInformationExtractor;
import kr.cnu.sea.xmogen.ks.GlobalVariableExtractor;
import kr.cnu.sea.xmogen.ks.UserDefineTypeExtractor;
import kr.cnu.sea.xmogen.ks.ast.ASTGenerator;
import kr.cnu.sea.xmogen.ks.calldependency.FunctionCallDependencyAnalyzer;
import kr.cnu.sea.xmogen.ui.Observer;

public class XMoGenProject {
	
	private static XMoGenProject instance;

	private File projectFile;
	private String name;
	private ArrayList<File> sourceFiles;
	
	private ArrayList<XMoGenFilter> filters;
	
	private ArrayList<Observer> refreshObject;

	private XMoGenProject() {
		
		sourceFiles = new ArrayList<File>();
		refreshObject = new ArrayList<Observer>();
	}
	
    public static XMoGenProject getInstance() {
        
    	if(instance == null) {
        	
            instance = new XMoGenProject();
        }
        
        return instance;
    }
	
	private void generateModel(File projectFile) {
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			
			rootNode = mapper.readTree(projectFile).path("project");
		} 
		catch (IOException e) {

			e.printStackTrace();
		}
		
		if (rootNode != null && rootNode.isMissingNode()!= true) {
			
			this.name = rootNode.path("name").asText();
			filters = new ArrayList<XMoGenFilter>();
			
			JsonNode filterNode = rootNode.path("filter");
			for (JsonNode node : filterNode) {
				
				filters.add(new XMoGenFilter(node));
			}
		}
	}
	
	public void inputProjectFilePath(String path) {
		
		projectFile = new File(path);
		generateModel(projectFile);
	}
	
	public DefaultMutableTreeNode getProjectTreeNodes() {
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(name);
		DefaultMutableTreeNode dataRoot = new DefaultMutableTreeNode("Input");
		DefaultMutableTreeNode filterRoot = new DefaultMutableTreeNode("Filters");
		DefaultMutableTreeNode observerRoot = new DefaultMutableTreeNode("Observers");
		
		root.add(dataRoot);
		root.add(filterRoot);
		root.add(observerRoot);
		
		if (sourceFiles.size() == 0) {
			
			dataRoot.add(new DefaultMutableTreeNode("(empty)"));
		}
		else {
		
			for (File file : sourceFiles) {
			
				dataRoot.add(new DefaultMutableTreeNode(file.getName()));
			}
		}
		
		for (XMoGenFilter filter : filters) {
			
			filterRoot.add(new DefaultMutableTreeNode(filter.getName()));
		}
		
		observerRoot.add(new DefaultMutableTreeNode("Partitioning"));
		
		return root;
	}

	public String getName() {
		
		return name;
	}

	public void setName(String name) {
		
		this.name = name;
	}

	public ArrayList<XMoGenFilter> getFilters() {
		
		return filters;
	}
	
	public XMoGenFilter getFilter(String name) {
		
		for (XMoGenFilter filter : filters) {
			
			if (filter.getName().equals(name)) {
				
				return filter;
			}
				
		}
		
		return null;
	}

	public void setFilters(ArrayList<XMoGenFilter> filters) {
		
		this.filters = filters;
	}

	public void addSourceFilePath(String path) {
		
		if (isExistFile(path)) {
			
			return ;
		}

		if (path == null || path.isEmpty()) {

		} else {

			File file = new File(path);
			if (file.isDirectory()) {

				addSourceFilesFromDirectory(path);
			} else if (file.isFile()) {

				int index = file.getName().indexOf('.');
				if (file.getName().substring(index + 1).compareTo("pp") == 0) {
				
					this.sourceFiles.add(file);
				}
			}
		}
		
		notifyToObserver();
	}

	private void addSourceFilesFromDirectory(String dirPath) {

		File directory = new File(dirPath);

		for (String fileName : directory.list()) {

			String filePath = dirPath + "\\" + fileName;
			addSourceFilePath(filePath);
		}
		
		notifyToObserver();
	}
	
	private boolean isExistFile(String path) {
		
		for (File file : sourceFiles) {
			
			if (file.getAbsolutePath().equals(path)) {
				
				return true;
			}
		}
		
		return false;
	}
	
	public ArrayList<File> getFileList() {
		
		return this.sourceFiles;
	}
	
	private void notifyToObserver() {
		
		for (Observer observer : refreshObject) {
			
			observer.refresh();
		}
	}
	
	public void registObserver(Observer view) {
		
		refreshObject.add(view);
	}
	
	public void unregistObserver(Observer view) {
		
		refreshObject.remove(view);
	}
	
	public boolean start() {
		
		if (sourceFiles.size() == 0) {
			
			return false;
		}
		
		XMoGenRepository repository = XMoGenRepository.getInstance();
		repository.init();
		
		ObjectMapper mapper = new ObjectMapper();
		
		// insert to file path
		for (File file : sourceFiles) {
			
			ObjectNode input = mapper.createObjectNode();
			input.put("path", file.getAbsolutePath());
			
			repository.insert("xMoGen/file", input);
		}
		
		// AST Generator
		for (JsonNode input : repository.select("xMoGen/file/path")) {
			
			String uuid = input.path("uuid").asText();
			
			ASTGenerator astGenerator = new ASTGenerator();
			
			astGenerator.setUp(input);
			astGenerator.execute();
			JsonNode astGeneratorResult = astGenerator.tearDown();
			
			repository.update(uuid, astGeneratorResult);
		}

		// User Define Type Extractor
		for (JsonNode input : repository.select("xMoGen/file/ast")) {
			
			UserDefineTypeExtractor userDefineTypeExtractor = new UserDefineTypeExtractor();
			
			userDefineTypeExtractor.setUp(input);
			userDefineTypeExtractor.execute();
			JsonNode userDefineTypeExtractorResult = userDefineTypeExtractor.tearDown();
			
			for (JsonNode typeNode : userDefineTypeExtractorResult) {
			
				repository.insert("xMoGen/type", typeNode);
			}
		}
		
		// Global Variable Extractor
		for (JsonNode input : repository.select("xMoGen/file/ast")) {
			
			ObjectNode mergeObject = mapper.createObjectNode();
			mergeObject.setAll((ObjectNode) input);
			mergeObject.set("type", repository.select("xMoGen/type"));
			
			GlobalVariableExtractor grobalVariableExtractor = new GlobalVariableExtractor();
			
			grobalVariableExtractor.setUp(mergeObject);
			grobalVariableExtractor.execute();
			JsonNode grobalVariableResult = grobalVariableExtractor.tearDown();
			
			for (JsonNode varNode : grobalVariableResult) {
				
				repository.insert("xMoGen/variable", varNode);
			}
		}
		
		// Function Extractor
		for (JsonNode input : repository.select("xMoGen/file/ast")) {
			
			FunctionExtractor functionExtrator = new FunctionExtractor();
			
			functionExtrator.setUp(input);
			functionExtrator.execute();
			JsonNode functionExtratorResult = functionExtrator.tearDown();
			
			for (JsonNode functionNode : functionExtratorResult) {
			
				repository.insert("xMoGen/function", functionNode);
			}
		}
		
		// Function Information Extractor
		for (JsonNode input : repository.select("xMoGen/function/ast")) {
			
			ObjectNode mergeObject = mapper.createObjectNode();
			mergeObject.setAll((ObjectNode) input);
			mergeObject.set("type", repository.select("xMoGen/type"));
			
			FunctionInformationExtractor functionInformationExtrator = new FunctionInformationExtractor();
			
			functionInformationExtrator.setUp(mergeObject);
			functionInformationExtrator.execute();
			JsonNode functionInformationResult = functionInformationExtrator.tearDown();
			
			repository.update(input.path("uuid").asText(), functionInformationResult);
		}
		
		// Call Dependency Analyzer
		JsonNode callDependencyAnalyzerInput = repository.select("xMoGen/function");
		FunctionCallDependencyAnalyzer callDependencyAnalyzer = new FunctionCallDependencyAnalyzer();
		
		callDependencyAnalyzer.setUp(callDependencyAnalyzerInput);
		callDependencyAnalyzer.execute();
		JsonNode callDependencyAnalyzerResult = callDependencyAnalyzer.tearDown();
		
		repository.insert("xMoGen/dependency", callDependencyAnalyzerResult);
		
		// Function Distributor 
		JsonNode functionDistributorInput = repository.select("xMoGen/dependency");
		FunctionDistributor functionDistributor = new FunctionDistributor();
		
		functionDistributor.setUp(functionDistributorInput);
		functionDistributor.execute();
		JsonNode functionDistributorResult = functionDistributor.tearDown();
		
		repository.insert("xMoGen/partitioning", functionDistributorResult);
		
		notifyToObserver();
		
		return true;
	}
}
