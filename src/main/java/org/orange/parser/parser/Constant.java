package org.orange.parser.parser;

public final class Constant {
	public static final class url{
		public static final String DEFAULT_PAGE = "http://example.com/";

		public static final String LOGIN_PAGE = "http://my.tjut.edu.cn/userPasswordValidate.portal";
		/** 取得教务处网站（含课程信息等）的会话cookie的页面 */
		public static final String TEACHING_AFFAIRS_SESSION_PAGE = "http://ssfw.tjut.edu.cn/ssfw/j_spring_ids_security_check";
		/** 师生服务网站“学生信息”中的“我的基本信息” */
		public static final String PERSONAL_INFORMATION = "http://ssfw.tjut.edu.cn/ssfw/xjgl/jbxx.do";
		public static final String LEARNING_COURSES = "http://ssfw.tjut.edu.cn/ssfw/xkgl/xkjgcx.do";
		public static final String SELECTED_COURSES_FOR_NEXT_SEMESTER = "http://59.67.148.66/selection/bxqxkcx_z_new.asp";
		public static final String FINAL_EXAM_GRADES = "http://59.67.148.66/score/xszxcjcy.asp";
		public static final String ALL_PERSONAL_GRADES = "http://ssfw.tjut.edu.cn/ssfw/zhcx/cjxx.do";

		public static final String PREPARE_PAGE_FOR_GET_POSTS_FROM_SCCE = "http://59.67.152.3";
	}
}
