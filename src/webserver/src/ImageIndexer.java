import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

import org.json.*;
import org.w3c.dom.*;   
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.analysis;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;


import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.TokenStream;  
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;  


import javax.xml.parsers.*; 
import org.apache.lucene.index.IndexOptions;
public class ImageIndexer {
	private Analyzer analyzer; 
    private IndexWriter indexWriter;
    private float averageLength=1.0f;
    public static final String INDEXPREFIX = "/home/mored/workspace/image_search/ImageSearch/forIndex";
    public static int imin(int i, int j) {return i < j ? i : j;}
    public ImageIndexer(String indexDir){
    	analyzer = new SmartChineseAnalyzer();
    	try{
    		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    		String temp[] = new String[1]; temp[0] = indexDir;
    		Path path = FileSystems.getDefault().getPath(INDEXPREFIX, temp);
    		Directory dir = FSDirectory.open(path);
    		indexWriter = new IndexWriter(dir,iwc);
    		indexWriter.deleteAll();
    		// indexWriter.setSimilarity(new SimpleSimilarity());
    	} catch(IOException e){
    		e.printStackTrace();
    	}
    }
    private static String getMainString(String str) {
    	
    	TreeMap<Integer, Integer> symbol = new TreeMap<Integer, Integer>();
    	char ch[] = {',', '.', '，', '。', ' ', '-', '/', '<', '>', '_', '\\', '|', '、'
    			,'【', '】', '\'', '[', ']' ,';', '；', '‘', '“', '’', '”', '《', '》','（', '）', '+', '-', '$'};
    	for (char i : ch) {
    		symbol.put((int)i, 1);
    	}
    	symbol.put((int)'\t', 4);
    	symbol.put((int)'\n', 4);
    	symbol.put((int)'(', 4);
    	symbol.put((int)')', 4);
    	symbol.put((int)'\"', 2);
    	symbol.put((int)';', 8);
    	symbol.put(160, 1);
    	int l = 0, r = imin(str.length(), 100);
    	int cnt = 0;
    	for (int i = l; i < r; i ++) {
    		int v = symbol.get((int)(str.charAt(i))) == null ? 0 : symbol.get((int)(str.charAt(i)));
    		
    		if (v == 1) {
    			cnt += 1;
    		}
//    		System.out.println(i + " " + cnt);
//    		System.out.println((int)(str.charAt(i))); 
    	}
    	for (; ; l ++, r ++) {
    		if (str.substring(l, r).startsWith("cutSummary")) {
    			for (int z = 0; z < 12; z ++) {
    				if (r >= str.length()) return "";
    				cnt -= (symbol.get((int)str.charAt(l))) == null ? 0 : (symbol.get((int)str.charAt(l)));
    	    		cnt += (symbol.get((int)str.charAt(r))) == null ? 0 : (symbol.get((int)str.charAt(r)));
    				l += 1; r += 1;
    				if (r >= str.length()) return "";
    			}
    		}
    		if (cnt <= (r - l + 1) / 10 && l < str.length() && str.charAt(l) != ' ' && str.charAt(l) != '(' && str.charAt(l) != 'c') {
//    			System.out.println(str.substring(l, r));
//    			System.out.println(cnt);
    			return str.substring(l, r);
    		}
//    		System.out.println(cnt);
    		if (r == str.length()) return "";
    		cnt -= (symbol.get((int)str.charAt(l))) == null ? 0 : (symbol.get((int)str.charAt(l)));
    		cnt += (symbol.get((int)str.charAt(r))) == null ? 0 : (symbol.get((int)str.charAt(r)));
    	}
    	
    	
    }
    
    
    public void saveGlobals(String filename){
    	try{
    		PrintWriter pw=new PrintWriter(new File(filename));
    		pw.println(averageLength);
    		pw.close();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
	
	/** 
	 * <p>
	 * index sogou.xml 
	 * 
	 */
    private Field createField(String name, String content, IndexOptions index_opt) {
    	FieldType ft = new FieldType();
    	ft.setIndexOptions(index_opt);
    	ft.setStored(true);
    	Field f = new Field(name, content, ft);
    	return f;
    }
	public void indexSpecialFile(String[] filenames){
		String html_keywords[] = {"text","h1", "h2", "h3",  "a", "title", };
		String other_keywords[] = { "author", "text", "title"};
		TreeMap<String, Double> pr = new TreeMap<String, Double>();
		TreeMap<String, String> hrefs = new TreeMap<String, String>();
		TreeMap<String, String> imgs = new TreeMap<String, String>();
		
		try{
			System.out.println("load page rank");
			String pr_path = "/home/mored/workspace/campusearch/results.json3";
			System.out.println(pr_path);
//			Reader readers = new InputStreamReader(new FileInputStream(new File(pr_path)));
			BufferedReader readers = new BufferedReader(new FileReader(new File(pr_path)));
			
			String jsonstr = "";
			jsonstr = readers.readLine();
			System.out.println("read page rank fin");
			JSONObject objs = new JSONObject(jsonstr);
			Iterator keyss = objs.keys();
			while( keyss.hasNext() ) {
			    String file_name = (String)keyss.next();
			    Double rank = objs.getDouble(file_name);
			    pr.put(file_name, rank);
			}

			String href_path = "/home/mored/workspace/campusearch/url.json2";
			readers = new BufferedReader(new FileReader(new File(href_path)));
			jsonstr = readers.readLine();
			System.out.println("read href rank fin");
			objs = new JSONObject(jsonstr);
			keyss = objs.keys();
			while( keyss.hasNext() ) {
			    String file_name = (String)keyss.next();
			    String href = objs.getString(file_name);
			    hrefs.put(file_name, href);
//			    System.out.println(href);
			}
			

			String img_path = "/home/mored/workspace/campusearch/img.json2";
			readers = new BufferedReader(new FileReader(new File(img_path)));
			jsonstr = readers.readLine();
			System.out.println("read img rank fin");
			objs = new JSONObject(jsonstr);
			keyss = objs.keys();
			while( keyss.hasNext() ) {
			    String file_name = (String)keyss.next();
			    String img = objs.getString(file_name);
			    imgs.put(file_name, img);
			}

			char t[] = new char[1000000];
			for (int file_id = 0; file_id < filenames.length; file_id ++) {
				String buffer = ""; 
				System.out.println(filenames[file_id]);
				Reader reader = new InputStreamReader(new FileInputStream(new File(filenames[file_id])));
				int temp = 0, index = 0;
				while ((temp = reader.read()) != -1) { 
					t[index] = (char)temp;
					index += 1;
					if (index == 1000000) {
						index = 0;
						buffer = buffer.concat(String.copyValueOf(t));
					}
					// System.out.println(String.copyValueOf(t));
				}
				if (index != 0) {
					char sub[] = new char[index];
					for (int i = 0; i < index;i ++)
						sub[i] = t[i];
					buffer = buffer.concat(String.copyValueOf(sub));
				}
				reader.close();
				System.out.println(buffer.length());
				System.out.println("start parse");
				JSONObject obj = new JSONObject(buffer);
				Iterator keys = obj.keys();
				while( keys.hasNext() ) {
				    String file_name = (String)keys.next();
				    // System.out.println(file_name);
				    JSONObject content = obj.getJSONObject(file_name);
				    String str_content = "";
				    Document document = new Document();
				    Field file_dir = createField("file_dir", file_name, IndexOptions.NONE);
			    	document.add(file_dir);
			    String href = hrefs.get(file_name);
			    if (href == null) {
			    	System.out.println(file_name);
			    	href = "";
			    }
				Field href_dir = createField("href", href, IndexOptions.NONE);
				document.add(href_dir);
				Field img_dir = null;
				if (imgs.containsKey(file_name) && imgs.get(file_name) != null)
					img_dir = createField("img", imgs.get(file_name), IndexOptions.NONE);
				else 
					img_dir = createField("img", "", IndexOptions.NONE);
				document.add(img_dir);
			    	String keywords[];
			    	double pagerank = 1.0;
				    if (file_name.endsWith("html")) {
				    	keywords = html_keywords;
				    	pagerank = pr.get(file_name);
				    } else {
				    	keywords = other_keywords;
				    	pagerank = pr.get("pdf");
				    }
				    for (int z = 0; z < keywords.length; z ++) {
			    		String keyword = keywords[z];
			    		if (content.has(keyword)) {
			    			//System.out.println(keyword);
			    			//System.out.println(content.getString(keyword));
			    			Field field = createField(keyword, content.getString(keyword), IndexOptions.DOCS);
			    			field.setBoost((float)pagerank);
			    			if (str_content.length() < 100) {
			    				String json_content = getMainString(content.getString(keyword)); 
//			    				System.out.println(json_content);
			    				str_content = str_content.concat(json_content.substring(0, imin(100 - str_content.length(), json_content.length())));
			    			}
					    	document.add(field);
			    		}
			    	}
//				    IndexDocValuesField idvf = new IndexDocValuesField("pagerank");
//				    System.out.println(str_content);
				    Field content_field = createField("content", str_content, IndexOptions.NONE);
				    document.add(content_field);
				    indexWriter.addDocument(document);
				}
			}
			indexWriter.close();
			System.out.println("index build finish");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private static void printAnalysisResult(Analyzer analyzer, String keyWord)  
            throws Exception {  
        System.out.println("["+keyWord+"]分词效果如下");  
//        TokenStream tokenStream = analyzer.tokenStream("content",  
//                new StringReader(keyWord));  
        TokenStream tokenStream = analyzer.tokenStream("", keyWord);
        tokenStream.reset();
        //tokenStream.addAttribute(CharTermAttribute.class);  
        while (tokenStream.incrementToken()) {  
           // CharTermAttribute charTermAttribute = (CharTermAttribute) tokenStream.getAttribute(CharTermAttribute.class);  
            System.out.println(tokenStream.toString());  
  
        }  
        tokenStream.end();
    }
	public static void main(String[] args) {
//		System.out.println(getMainString("中文版  Admissions     Faculty & Research     Research Centers     Careers         Alumni         News "));
//		return;
		ImageIndexer indexer=new ImageIndexer("index");
		int file_cnt = 24;
//		int file_cnt = 1;
		String file_dirs[] = new String[file_cnt];
		for (int i = 0; i < file_cnt; i ++) {
			String file_dir = "index_" + new Integer(i * 10000).toString() + ".json";
			file_dirs[i] = "/home/mored/workspace/campusearch/" + file_dir;
		}
		indexer.indexSpecialFile(file_dirs);
		
//		Analyzer analyzer = new SmartChineseAnalyzer();
//		IndexReader reader;
//		IndexSearcher searcher = null;
//		try{
//			String temp[] = new String[1]; temp[0] = "index";
//    		Path path = FileSystems.getDefault().getPath(INDEXPREFIX, temp);
//    		Directory dir = FSDirectory.open(path);
//			reader = DirectoryReader.open(dir);
//			searcher = new IndexSearcher(reader);
////			searcher.setSimilarity(new SimpleSimilarity());
//		}catch(IOException e){
//			e.printStackTrace();
//		}
//		
//		Term term=new Term("title","清华");
//		double av = 1.0;
//		Query query = new TermQuery(term);
//		try{
//		TopDocs results = searcher.search(query, 10);
//	    for (ScoreDoc match : results.scoreDocs) {
//        Explanation explanation
//           = searcher.explain(query, match.doc);
//        System.out.println("----------");
//        Document doc = searcher.doc(match.doc);
//        System.out.println(doc.get("title"));
//        System.out.println(explanation.toString());
//	    }
//		}
//		catch (Exception e) {
//			
//		}

//		Query query=new SimpleQuery(term,avgLength);
//		query.setBoost(1.0f);
//		Query query = new TermQuery(term);
//		Weight w=searcher.createNormalizedWeight(query);
		//System.out.println(w.getClass());
		
		
//		 String keyWord = "IKAnalyzer的分词效果到底怎么样呢，我们来看一下吧";
//		String keyWord = "你 是 一个 禅机 的 好人 钱迪晨  yes";
	        //创建IKAnalyzer中文分词对象  
//	        IKAnalyzer analyzer = new IKAnalyzer();  
//		SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
		// 使用智能分词  	   
	        
	        // analyzer.setUseSmart(true);  
	        // 打印分词结果  
//	        try {  
//	            printAnalysisResult(analyzer, keyWord);  
//	        } catch (Exception e) {  
//	            e.printStackTrace();  
//	        }  
	}
}
