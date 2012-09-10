package baijie.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import util.webpage.ReadPageHelper;

public class TestWhenMake {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			switch(3){
			case 1:
				ReadPageHelper helper1 = new ReadPageHelper();
				Document doc1 = helper1.getWithDocument("http://59.67.148.66:8080/view.jsp?id=4002");
				System.out.println(doc1.select("table tr:eq(2) table td:eq(1) table table tr:eq(4) td").html());
				break;
			case 2:
				ReadPageHelper helper2 = new ReadPageHelper();
				Document doc2 = helper2.getWithDocumentForParsePostsFromSCCE("http://59.67.152.3/wnoticecontent2.aspx?id=3074");
				System.out.println(doc2.select("#Label2").html());
				break;
			case 3:
				ReadPageHelper helper3 = new ReadPageHelper();
				Document doc3 = helper3.getWithDocumentForParsePostsFromSCCE("http://59.67.152.6/pages/1063");
				System.out.println(doc3.select(".content div").html());
				System.out.println(doc3.baseUri());
				break;
			default:break;
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

	}
}
