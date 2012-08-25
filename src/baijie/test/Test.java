package baijie.test;

import java.util.ArrayList;

import util.webpage.Constant;
import util.webpage.Course;
import util.webpage.SchoolWebpageParser;
import util.webpage.SchoolWebpageParser.ParserListener;
import util.webpage.SchoolWebpageParser.ParserListenerAdapter;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
		switch(2){
		case 1:
			SchoolWebpageParser parser1 = new SchoolWebpageParser();
			parser1.setUser("20106173", "20106173");
			ArrayList<Course> result1 = parser1.parseCourse(Constant.url.已选下学期课程);
			System.out.println(result1);
		break;
		case 2:
			ParserListener listener2 = new ParserListenerAdapter(){

				/* (non-Javadoc)
				 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onWarn(int, java.lang.String)
				 */
				@Override
				public void onWarn(int code, String message) {
					System.err.println("WARN "+code+": "+message);
				}

				/* (non-Javadoc)
				 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onInformation(int, java.lang.String)
				 */
				@Override
				public void onInformation(int code, String message) {
					System.out.println("INFO "+code+": "+message);
				}
				
			};
			SchoolWebpageParser parser2 = new SchoolWebpageParser(listener2, "20106173", "20106173");
			System.out.println(parser2.parseScores(Constant.url.个人全部成绩));
		break;
		default:;
		}
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
}
