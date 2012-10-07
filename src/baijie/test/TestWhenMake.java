package baijie.test;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import util.webpage.Constant;
import util.webpage.Course;
import util.webpage.ReadPageHelper;

public class TestWhenMake {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			switch(7){
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
			case 4:
				Course course4 = new Course();
				ArrayList<String> teachers4 = new ArrayList<String>();
				teachers4.add("tetew");
				teachers4.add("45243");
				course4.setTeachers(teachers4);
				System.out.println(course4);
				teachers4.add("jlkjbai");
//				course4.setTeachers(teachers4);
				System.out.println(teachers4+"\n"+course4);
				break;
			case 5:
				ReadPageHelper helper5 = new ReadPageHelper("20106173", "2010617");
				helper5.setCharset("GB2312");
				if(helper5.doLogin()){
					Document result5 = helper5.getWithDocument(Constant.url.本学期修读课程);
					System.out.println(result5.html());
				}else
					System.out.println("登录失败！");
				break;
			case 6:
				doLogin(Constant.url.LOGIN_PAGE);
				break;
			case 7:
				ReadPageHelper helper7 = new ReadPageHelper();
				helper7.setUser("20106173", "20106173");
				System.out.println(helper7.doLogin());
				break;
			default:break;
			}
		}catch(HttpStatusException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

	}

	public static boolean doLogin(String loginPageURL) throws IOException{
		Response res = Jsoup
				.connect(loginPageURL).followRedirects(false)
				.data("name","2010617","pswd", "20106173")
				.method(Method.POST).execute();
		System.err.println(""+loginPageURL+res.statusCode()+res.statusMessage()+res.bodyAsBytes().length);

		//System.out.println(res.body());
		return false;
	}
}
