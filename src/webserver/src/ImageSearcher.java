import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
//import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause;
//import org.apache.lucene.queryParser.ParseException;
//import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.Weight;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class ImageSearcher {
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer = null;
	private float avgLength=1.0f;
	
	public ImageSearcher(String indexDir, String filename){

		try{
    		String temp[] = new String[1]; temp[0] = filename;
    		Path path = FileSystems.getDefault().getPath(indexDir, filename);
    		Directory dir = FSDirectory.open(path);
			reader = DirectoryReader.open(dir);
			searcher = new IndexSearcher(reader);
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public TopDocs searchQuery(String queryString, int maxnum){
		analyzer = new SmartChineseAnalyzer();
		try {
			//System.out.println("field, queryString: " + field + " " + queryString);
//			Term term=new Term("title",queryString);
			List<String> strs = analysisResult(analyzer, queryString);
			String fields[] = {"title", "h1", "h2", "h3", "a", "text", "author"};
			Float scores[] = {(float) 1, (float) 0.05, (float) 0.03, (float) 0.02, (float) 0.03, (float) 0.01, (float) 0.5};
//			Float scores[] = {(float) 1, (float) 0, (float) 0, (float) 0, (float) 0., (float) 0., (float) 0.};
			Map<String, Float> boosts = new TreeMap<String, Float>();
			for (int i = 0; i < fields.length; i ++)
				boosts.put(fields[i], scores[i]);
			BooleanQuery.Builder builder = new BooleanQuery.Builder();
			for (String s : strs) {
			
				for (int i = 0; i < fields.length; i ++) {
					Term t = new Term(fields[i], s);
					TermQuery tq = new TermQuery(t);
					BoostQuery bq = new BoostQuery(tq, scores[i]);
					builder.add(bq, BooleanClause.Occur.SHOULD);
				}
			}
			BooleanQuery query = builder.build();
//			MultiFieldQueryParser mfqp = new MultiFieldQueryParser(fields, analyzer, boosts);
//			mfqp.setDefaultOperator(Operator.AND);
//			Query query = mfqp.parse(queryString);
//			Query query = MultiFieldQueryParser.parse(queryString, fields, flags, analyzer, boosts);
			//Query query=new QueryParser(Version.LUCENE_35, field, new StopAnalyzer(Version.LUCENE_35)).parse(queryString); System.out.println("Default"); 
			//query.setBoost(1.0f);
			//Weight w=searcher.createNormalizedWeight(query);
			//System.out.println(w.getClass());
			TopDocs results = searcher.search(query, maxnum);
//			System.out.println(results);
			return results;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Document getDoc(int docID){
		try{
			return searcher.doc(docID);
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public void loadGlobals(String filename){
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String line=reader.readLine();
			avgLength=Float.parseFloat(line);
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public float getAvg(){
		return avgLength;
	}
	private static List<String> analysisResult(Analyzer analyzer, String keyWord)  
            throws Exception {  
//        System.out.println("["+keyWord+"]分词效果如下");  
//        TokenStream tokenStream = analyzer.tokenStream("content",  
//                new StringReader(keyWord));  
        TokenStream tokenStream = analyzer.tokenStream("", keyWord);
        tokenStream.reset();
        List<String> ret = new ArrayList<String>();
        //tokenStream.addAttribute(CharTermAttribute.class);  
        while (tokenStream.incrementToken()) {  
           // CharTermAttribute charTermAttribute = (CharTermAttribute) tokenStream.getAttribute(CharTermAttribute.class);  
//            System.out.println(tokenStream.);
        	String s = tokenStream.toString();
        	int st = s.indexOf("term="); st += 5;
        	int en = s.indexOf(",");
        	System.out.println(s.substring(st, en));
        	ret.add(s.substring(st,en));
        }  
        tokenStream.end();
        return ret;
    }
	public static void main(String[] args){
		ImageSearcher search=new ImageSearcher("/home/mored/workspace/image_search/ImageSearch/forIndex", "index");

		List<String> queryStrings = null;
		TopDocs results=search.searchQuery("新闻", 10);
		try {
			queryStrings = analysisResult(new SmartChineseAnalyzer(), "清华开始");
//			System.out.println(queryStrings.toString());
		} catch (Exception e) {}
		
                for (int i = 0; i < queryStrings.size(); i ++)
			System.out.println(queryStrings.get(i));
		
		ScoreDoc[] hits = results.scoreDocs;
		for (int i = 0; i < hits.length; i++) { // output raw format
			Document doc = search.getDoc(hits[i].doc);
			System.out.println("doc=" + hits[i].doc + " score="
					+ hits[i].score+" picPath= "+doc.get("title")+doc.get("author"));
		}
	}
}
