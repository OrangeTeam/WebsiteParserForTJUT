package baijie.test;

import java.util.ArrayList;

import util.BitOperate;
import util.BitOperate.BitOperateException;
import util.webpage.Constant;
import util.webpage.Course;
import util.webpage.Course.TimeAndAddress;
import util.webpage.Post;
import util.webpage.ReadPageHelper;
import util.webpage.SchoolWebpageParser;

public class Test {

	/**
	 * @param args
	 */
	/*public static void main(String[] args) {
		InputStream is = null;
		String page = null;
		ReadPageHelper scores = null;
		ReadPageHelper readPageHelper = new ReadPageHelper("20106173","01021061");
		if(login.doLogin()){
			scores = new ReadPageHelper("http://59.67.148.66/score/xszxcjcy.asp",login.getSessionID());
			String result = scores.openInputStream();
			if(result.equals("OK")){
				is = scores.getInputStream();
				try{
					page = readIt(new BufferedInputStream(is),10000);
					System.out.println(page);
				}catch(Exception e){
					System.err.println(e);
				}
				scores.getConnection().disconnect();
			}
			else
				System.err.println(result);
		}
		else
			System.err.println("Can't login");
		

	}*/
	public static void main(String[] args) {
		switch(1){
		case 1:
			ReadPageHelper readHelper = new ReadPageHelper("20106135","20106135");
			try{
				if(readHelper.doLogin()){
					//System.out.println(readHelper.get(Constant.url.本学期修读课程));
					//ArrayList<Course> courses = SchoolWebpageParser.parseCourse(
						//	Constant.url.已选下学期课程, readHelper);
					ArrayList<Course> courses = SchoolWebpageParser.parseScores(
							Constant.url.期末最新成绩, readHelper);
					for(Course c:courses)
						System.out.println(c.toString());
				}
				else
					System.err.println("Can't log in!");
			}
			catch(Exception e){
				System.err.println(e);
			}
		break;
		case 2:
			boolean result = "2,4,6,8,10,12,14,18 1-21".matches(".*[^\\d\\s\u00a0\u3000;；,，\\-－\u2013\u2014\u2015].*");
			System.out.println(result);
			for(String s:"2,4,6,8,10,12,14,18 01-21".split("[\\s\u00a0\u3000;；,，]"))
				System.out.println(s);
			System.out.println(Integer.parseInt("01"));
			int week =  3 ;
			try {
				week = BitOperate.add1onCertainBit(0,"2, 4;6 ;8 ；;,，10，12 14,18- 20,");
			} catch (BitOperateException e) {
				//e.printStackTrace();
				System.out.println(e);
			}
			System.out.println(Integer.toBinaryString(week));
		break;
		case 3:
			try {
				TimeAndAddress t = new TimeAndAddress("5-0102");
				//t.addDays("星期一到星期三   星期五 到星期六 周日");
				//t.addDays("星期一,星期三,;，；   星期六");
				t.addDays("星期三 到 周日");
				System.out.println(Integer.toBinaryString(t.getDay()));
				t.addWeeks("1-6 9 12 13",true);
				System.out.println(Integer.toBinaryString(t.getWeek()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		break;
		/*case 4:
			try {
				String[] result1 = SchoolWebpageParser.splitTime("1-10,12-15,18 周 周一至周四，周五 3-4 节 ");
				for(String s:result1)
					System.out.println(s);
			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		break;*/
		case 5:
			try {
				System.out.println(BitOperate.convertIntToString(BitOperate.add1onCertainBit(0, "2, 4;6 ;8 ；;,，10，12 14,18-20")));
			} catch (BitOperateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		break;
		case 6:
			Post post = new Post("Title", "http://59.67.148.66", "2012-08-31", null, null);
			//post.setDate(1970, 1, 1);
			System.out.println(post.toString());
		break;
		}
	}
	// Reads an InputStream and converts it to a String.
	/*public static String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
	    Reader reader = null;
	    reader = new InputStreamReader(stream, "GB2312");        
	    char[] buffer = new char[len];
	    reader.read(buffer);
	    stream.close();
	    return new String(buffer);
	}*/
}
