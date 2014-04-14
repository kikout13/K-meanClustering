package cluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;

import org.jsoup.Jsoup;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import vn.hus.nlp.tokenizer.Tokenizer;
import vn.hus.nlp.tokenizer.VietTokenizer;

import com.google.gson.Gson;

import article.Article;

public class Parser {
	
	private String path;
	private ArrayList<String> stopWord = new ArrayList<>();
	private ArrayList<Integer> documentsList = new ArrayList<>();
	
	public Parser(String path) {
		this.path = path;
		createStopWord();
	}

	
	public void createStopWord() {
		stopWord.add("."); stopWord.add(",");
		stopWord.add(":"); stopWord.add("(");
		stopWord.add(")"); stopWord.add("/");
		stopWord.add("…"); stopWord.add("-");
		stopWord.add("'"); stopWord.add("”");
		stopWord.add("“");
	}
	
	public ArrayList<Integer> getDocumentsList() {
		return documentsList;
	}
	
	public void contentParser(DB db, ConcurrentNavigableMap<Integer, Map<String, Double>> dTable,
			ConcurrentNavigableMap<String, Integer> wTable) throws IOException {
		
		//get all files in folder 
		File dir = new File(path);
		File[] files = dir.listFiles();
		int file_number = files.length;
		
		//parsering content
		for(int k=0;k<file_number;k++) {
			documentsList.add(k);
			String filename = path + String.format("%06d", k+1) + ".json";
			System.out.println("Parsing content from file: " + filename);
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String currentLine;
			StringBuffer strBuff = new StringBuffer();
			while ((currentLine = br.readLine()) !=null) {
				strBuff.append(currentLine);	
			}
			Article art = new Article();
			Gson gson = new Gson();
			art = gson.fromJson(strBuff.toString(), Article.class);
			StringBuffer content = new StringBuffer(art.getDescription());
			content.append(" " + Jsoup.parse(art.getContent()).text());
			
			//tokenize content
			VietTokenizer tokenizer = new VietTokenizer();
			String token[] = tokenizer.tokenize(content.toString());
			token = token[0].split(" ");
			
			int document_lenght=0;
			
			Map<String, Double> wordList = new HashMap<String, Double>();
			for(int i=0;i<token.length;i++) {
				if(!stopWord.contains(token[i])) {
					if(wordList.containsKey(token[i])) {
						wordList.put(token[i], wordList.get(token[i])+1);
						document_lenght++;
					}
					else {
						wordList.put(token[i], 1.0);
						document_lenght++;
					}
				}
			}
			
			//tf calculate
			for(Map.Entry entry : wordList.entrySet()) {
				String key = entry.getKey().toString();
				double tf = Math.log(wordList.get(key)+1)/Math.log(document_lenght);
				wordList.put(key, tf);
			}
			
			indexWordFromDocument(wTable, wordList);
			indexDocumentFromDocument(dTable, wordList);
			db.commit(); // persist changes into disk
			
			
			
		}	
		
		calculateIdf(dTable, wTable);
		db.commit();
		
		// db.rollback(); // revert recent changes
	}
	
	public void indexWordFromDocument(
			ConcurrentNavigableMap<String, Integer> wTable,
			Map<String, Double> dTempTable) {
		for (Map.Entry entry : dTempTable.entrySet()) {
			if (wTable.containsKey(entry.getKey()))
				wTable.put((String) entry.getKey(),
						wTable.get(entry.getKey()) + 1);
			else
				wTable.put((String) entry.getKey(), 1);
		}		
	}
	
	public void indexDocumentFromDocument(
			ConcurrentNavigableMap<Integer, Map<String, Double>> dTable,
			Map<String, Double> dtempTable) {
		dTable.put(dTable.size(), dtempTable);
	}
	
	public void calculateIdf(
			ConcurrentNavigableMap<Integer, Map<String, Double>> dTable,
			ConcurrentNavigableMap<String, Integer> wTable) {
		for (Map.Entry entry : dTable.entrySet()) {
			Map<String, Double> tempTable = new HashMap<String, Double>();
			tempTable = (Map<String, Double>) entry.getValue();
			for (Map.Entry tempEntry : tempTable.entrySet()) {
				double temp = (Double) tempEntry.getValue()
						* Math.log((1.00*dTable.size())
								/ wTable.get(tempEntry.getKey()));
				tempEntry.setValue(temp);
			}
			dTable.put((Integer) entry.getKey(), tempTable);
		}
	}

}