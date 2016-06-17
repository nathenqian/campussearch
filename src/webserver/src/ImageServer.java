import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.util.*;

import java.math.*;
import java.net.*;
import java.io.*;


public class ImageServer extends HttpServlet{
	public static final int PAGE_RESULT=10;
	public static final String indexDir="forIndex";
	public static final String picDir="";
	private ImageSearcher search=null;
	private String path;
	public static String INDEXPREFIX = "/home/mored/workspace/image_search/ImageSearch/forIndex";
	private Analyzer sca = null;
	public void init(final ServletConfig config) {
//		path = config.getServletContext().getRealPath(File.separator) + "/..";
		search = new ImageSearcher(INDEXPREFIX, "index");
		
//		search.loadGlobals(new String(path + "/" + indexDir+"/global.txt"));
//		System.out.println(path);
	}
	
	public ImageServer(){
		super();
	}
	
	public ScoreDoc[] showList(ScoreDoc[] results,int page){
		if(results==null || results.length < (page-1)*PAGE_RESULT){
			return null;
		}
		int start = Math.max((page-1)*PAGE_RESULT, 0);
		int docnum=Math.min(results.length-start,PAGE_RESULT);
		ScoreDoc[] ret=new ScoreDoc[docnum];
		for(int i=0;i<docnum;i++){
			ret[i]=results[start+i];
		}
		return ret;
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
        	ret.add(s.substring(st,en));
        }  
        tokenStream.end();
        return ret;
    }
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		String queryString=request.getParameter("query");
		String pageString=request.getParameter("page");
		int page=1;
		
			sca = new SmartChineseAnalyzer();
		if(pageString!=null){
			page=Integer.parseInt(pageString);
		}
		if(queryString==null){
			System.out.println("null query");
			//request.getRequestDispatcher("/Image.jsp").forward(request, response);
		}else{
			System.out.println(queryString);
			System.out.println(URLDecoder.decode(queryString,"utf-8"));
			System.out.println(URLDecoder.decode(queryString,"gb2312"));
			String[] contents=null;
			String[] titles=null;
			String[] hrefs=null;
			String[] imgs=null;
			List<String> queryStrings;
			try {
				queryStrings = analysisResult(sca, queryString);
			}
			catch (Exception e) {
				queryStrings = new ArrayList<String>();
				queryStrings.add(queryString);
			}
			System.out.println(queryStrings.toString());
			TopDocs results=search.searchQuery(queryString, 1000);
			if (results != null) {
				ScoreDoc[] hits = showList(results.scoreDocs, page);
				if (hits != null) {
					contents = new String[hits.length];
					titles = new String[hits.length];
					hrefs = new String[hits.length];
					imgs = new String[hits.length];
					for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
						Document doc = search.getDoc(hits[i].doc);
						System.out.println("doc=" + hits[i].doc + " score="
								+ hits[i].score + " picPath= "
								+ doc.get("picPath")+ " tag= "+doc.get("abstract"));
						titles[i] = doc.get("title");// + doc.get("file_dir");
//						titles[i] = doc.get("file_dir");
						contents[i] = doc.get("content");
						imgs[i] = doc.get("img");
						hrefs[i] = doc.get("href");
						
						for (String qs : queryStrings) {
							contents[i] = contents[i].replace(qs, "<font color='red'>" + qs + "</font>");
							titles[i] = titles[i].replace(qs, "<font color='red'>" + qs + "</font>");
						}
						//paths[i] = picDir + doc.get("file_dir");
					}

				} else {
					System.out.println("page null");
				}
			}else{
				System.out.println("result null");
			}
			request.setAttribute("currentQuery",queryString);
			request.setAttribute("currentPage", page);
			request.setAttribute("maxPage", contents.length);
			request.setAttribute("contents", contents);
			request.setAttribute("hrefs", hrefs);
			request.setAttribute("imgs", imgs);
			String cutstr[] = new String[hrefs.length];
			for (int i = 0; i < hrefs.length; i ++)
				cutstr[i] = (hrefs[i].length() > 60) ? 
						hrefs[i].substring(0, 60) + "..." :
							hrefs[i];
			request.setAttribute("cutstr", cutstr);
			request.setAttribute("titles", titles);
			request.getRequestDispatcher("/imageshow.jsp").forward(request,
					response);
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doGet(request, response);
	}
}
