package org.orange.parser.parser;

/**
 * 解析监听器，用于返回状体信息，解析进度等。
 * @author Bai Jie
 */
public interface ParseListener extends Cloneable{
    public static final int NONE = 0;
    public static final int NULL_POINTER = 1;
    public static final int ERROR_CANNOT_LOGIN = 2;
    public static final int ERROR_IO = 3;
    public static final int ERROR_UNSUPPORTED_ENCODING = 4;
    /**不能读取网页中的课程（成绩）表头，无法继续解析。*/
    public static final int ERROR_CANNOT_PARSE_TABLE_HEADING = 5;
    /**Post的来源或者URL不明，无法解析Post正文。*/
    public static final int ERROR_INSUFFICIENT_INFORMATION = 6;
    /** 文档结构发生了变化 */
    public static final int WARNING_STRUCTURE_CHANGED = 9;
    /**不能读取总页面数，这样的话可能仅读取第一页*/
    public static final int WARNING_CANNOT_PARSE_PAGE_NUMBER = 10;
    /**结果为空，可能最新成绩、已选课程等页面已失效*/
    public static final int WARNING_RESULT_IS_EMPTY = 11;
    /**课程（成绩）表中遇到未知的列（数据项）。*/
    public static final int WARNING_UNKNOWN_COLUMN = 12;
    /**解析时间失败。*/
    public static final int WARNING_CANNOT_PARSE_DATE = 13;
    public static final int WARNING_CANNOT_PARSE_URL = 14;
    public static final int WARNING_CANNOT_PARSE_STRING_DATA = 15;
    public static final int WARNING_CANNOT_PARSE_NUMBER_DATA = 16;
    public static final int WARNINT_CANNOT_PARSE_TIME_AND_ADDRESS = 17;
    public static final int WARNINT_CANNOT_PARSE_SEMESTER = 18;
    public static final int WARNINT_CANNOT_PARSE_ACADEMIC_YEAR_AND_SEMESTER = 19;
    public static final int WARNING_CANNOT_PARSE_STUDENT_INFO = 20;
    public static final int WARNING_CANNOT_PARSE_STUDENT_BIRTHDAY = 21;
    public static final int WARNING_CANNOT_PARSE_STUDENT_ADMISSION_TIME = 22;
    public static final int WARNING_CANNOT_PARSE_STUDENT_ACADEMIC_PERIOD = 23;
    /**跳过/忽略某信息。*/
    public static final int INFO_SKIP = 30;
    /**
     * 当遇到错误时，调用此方法。错误意味着解释失败，停止解析。很可能此调用后解析方法抛出异常
     * @param code 错误代码
     * @param message 错误信息
     */
    public void onError(int code, String message);
    /**
     * 当遇到警告信息时，调用此方法。警告意味着可能有错误的解析结果，需要用户检查正误，解析方法本身会正常返回结果（可能包含错误信息）。
     * @param code 警告代码
     * @param message 警告信息
     */
    public void onWarn(int code, String message);
    /**
     * 当返回消息信息时，调用此方法。这并不代表程序不正常，只是用于返回一些关于当前状态的消息。
     * @param code 消息代码
     * @param message 消息信息
     */
    public void onInformation(int code, String message);
    /**
     * 当解析进度改变时。用于报告解析进度。用current/total表示，0<=current<=total，current==total时解析完成。
     * @param current 当前进度。
     * @param total 总进度。
     */
    public void onProgressChange(int current, int total);
    /**
     * 实现深克隆
     * @see Object#clone()
     */
    public ParseListener clone() throws CloneNotSupportedException;
}
