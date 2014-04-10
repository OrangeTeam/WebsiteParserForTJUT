package baijie.test;

import java.util.List;

import util.webpage.Constant;
import util.webpage.Course;
import util.webpage.Post;
import util.webpage.ReadPageHelper;
import util.webpage.SchoolWebpageParser;
import util.webpage.SchoolWebpageParser.ParserListener;
import util.webpage.SchoolWebpageParser.ParserListenerAdapter;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
		switch(6){
		case 1:
			SchoolWebpageParser parser1 = new SchoolWebpageParser(new TestListener());
			parser1.setOnReadPageListener(new MyOnReadPageHelper());
			parser1.setUser("20106173", "20106173");
			List<Course> result1 = parser1.parseCourse(Constant.url.LEARNING_COURSES);
			if(result1.isEmpty())
				System.out.println("result1 is empty!");
			else
				for(Course course : result1)
					System.out.println(course);
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
			int[][] semesters = new int[4][3];
			for(int index = 0 ; index < semesters.length ; index++) {
				semesters[index][0] = 2010 + index;
				semesters[index][1] = 1;
				semesters[index][2] = 2;
			}
			SchoolWebpageParser parser2 = new SchoolWebpageParser(listener2, "20106173", "20106173");
			System.out.println(parser2.parseScores(semesters));
		break;
		case 3:
			SchoolWebpageParser parser3 = new SchoolWebpageParser(new TestListener(), "20106173", "20106173");
			List<Course> result3 = parser3.parseScores(2011, 2);
			for(Course course3:result3)
				System.out.println(course3.getGradePoint() + "\t" + course3.toString());
		break;
		case 4:
			SchoolWebpageParser parser4 = new SchoolWebpageParser(new TestListener());
			List<Post> result4 = parser4.parsePosts(Post.SOURCES.STUDENT_WEBSITE_OF_SCCE, null, null, 10);
			for(Post post4:result4)
				System.out.println(parser4.parsePostMainBody(post4)+"\n");
			System.out.println(parser4.parsePostMainBody(new Post()));
		break;
		case 5:
			SchoolWebpageParser parser5 = new SchoolWebpageParser(new TestListener());
			MyOnReadPageHelper onReadPageHelper = new MyOnReadPageHelper();
			parser5.setOnReadPageListener(onReadPageHelper);
//			String[] categories = new String[]{Post.CATEGORYS.IN_SCCE[4],Post.CATEGORYS.IN_TEACHING_AFFAIRS_WEBSITE[4],
//					Post.CATEGORYS.IN_STUDENT_WEBSITE_OF_SCCE[4]};
			List<Post> result5 = null;
//			result5 = parser5.parsePostsFromSCCEStudent(Post.CATEGORYS.IN_STUDENT_WEBSITE_OF_SCCE[5], null, null, 1);
			result5 = parser5.parsePosts(null, null, -1); 
//			for(Post post:result5)
//				System.out.println(post);
			System.out.println("共 "+result5.size()+" 条， "+onReadPageHelper.pageNumber+" 页 "+onReadPageHelper.totalSize/1024.0/1024+" MB");
		break;
		case 6:
			SchoolWebpageParser parser6 = new SchoolWebpageParser();
			parser6.setUser("20106173", "20106173");
			System.out.println(parser6.parsePersonalInformation());
			break;
		default:
		}
		}catch(Exception e){
			System.err.println("------------------Start catch------------------");
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.err.println("------------------End catch------------------");
		}
	}
	
	private static class MyOnReadPageHelper implements ReadPageHelper.OnReadPageListener{
		int totalSize = 0;
		int pageNumber = 0;

		@Override
		public void onRequest(String url, int statusCode, String statusMessage, int pageSize){
			System.out.println("URL: "+url+"\nStatus Code: "+statusCode+"\tStatusMessage: "
					+statusMessage+"\t Page Size: "+pageSize);
			totalSize+=pageSize;
			pageNumber++;
		}
	}
	private static class TestListener extends ParserListenerAdapter{
		public TestListener(){
			super();
		}
		@Override
		public void onError(int code, String message) {
			System.err.println("ERROR "+code+": "+message);
		}
		@Override
		public void onWarn(int code, String message) {
			System.err.println("WARN "+code+": "+message);
		}
		@Override
		public void onInformation(int code, String message) {
			System.out.println("INFO "+code+": "+message);
		}
		@Override
		public void onProgressChange(float current, float total) {
			System.err.println("Progress: "+current+"/"+total);
		}
	}
}
