package baijie.test;

import java.io.IOException;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class UseJsoup {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection conn = null;
		Document doc = null;
		Response response = null;
		try {
			conn = Jsoup.connect("http://59.67.148.66").timeout(12000).data("name","20106173","pswd", "01021061").followRedirects(false);
			conn.post();
			response = conn.response();
			System.out.println(response.statusCode()+"\n"+response.statusMessage());
			Map<String, String> cookies = response.cookies();
			//if cookie!=empty
			String key = (String)cookies.keySet().toArray()[0];
			System.out.println(key);
			System.out.println(cookies.get(key));
			
			conn = Jsoup.connect("http://59.67.148.66/selection/bxqxkcx_z.asp").cookie(key,cookies.get(key));
			doc = ((HttpConnection)conn).get("GB2312");
			
			response = conn.response();
			System.out.println(response.headers());
			System.out.println(response.charset());
			
			//doc.outputSettings().charset("GB2312");
			System.out.println(doc.outputSettings().charset());
			//no-break space	0xA0	&nbsp(HTML);
			String[] result = doc.body().child(1).child(0).ownText().split("\u00A0");
			for(String s:result){
				System.out.println(deleteSpace(s));
			}
			
			Element table = doc.getElementsByTag("table").get(0);
			Elements courses = table.getElementsByTag("tr");
			Elements cols = courses.get(5).getElementsByTag("td");
			//for(int i=0;i<cols.size();i++)
				//System.out.println(deleteSpace(cols.get(i).text()));
			//for(int i=1;i<courses.size()-1;i++)
			//	System.out.println(courses.get(i).outerHtml());
			String[] times = cols.get(6).getElementsByTag("font").get(0).html().split("<.*>");
			for(String time:times){
				System.out.println(time);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e);
		}
	}

    private static String deleteSpace(String src){
    	if(src!=null)
    		return src.replaceAll("\\s|\u3000", "");
    		//	ideographic space	0x3000	&#12288(HTML);
    	else
    		return null;
    }
}
