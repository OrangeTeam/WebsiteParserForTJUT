package org.orange.parser.parser;

import org.jsoup.nodes.Document;
import org.orange.parser.entity.Course;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ScoreParser extends BaseCourseParser {
    private List<AcademicYearAndSemester> mAcademicYearAndSemesters = new LinkedList<>();

    public List<AcademicYearAndSemester> getAcademicYearAndSemesters() {
        return mAcademicYearAndSemesters;
    }
    public ScoreParser clearAcademicYearAndSemesters() {
        mAcademicYearAndSemesters.clear();
        return this;
    }
    public ScoreParser addAcademicYearAndSemester(int academicYear, int semester) {
        addAcademicYearAndSemester(new AcademicYearAndSemester(academicYear, semester));
        return this;
    }
    public ScoreParser addAcademicYearAndSemester(AcademicYearAndSemester... academicYearAndSemesters) {
        mAcademicYearAndSemesters.addAll(Arrays.asList(academicYearAndSemesters));
        return this;
    }

    /**
     * 解析成绩
     * @param academicYear 学年
     * @param semester 学期
     * @return 满足条件的包含成绩信息的课程列表
     * @throws IOException 网络连接出现异常
     */
    public List<Course> parse(int academicYear, int semester) throws IOException{
        super.parse();
        try{
            List<Course> result = parse1(academicYear, semester);
            if(result.isEmpty())
                mParseListener.onWarn(ParseListener.WARNING_RESULT_IS_EMPTY, "结果为空，可能最新成绩等页面已失效。");
            return result;
        }catch(IOException e){
            mParseListener.onError(ParseListener.ERROR_IO, "遇到IO异常，无法打开页面，解析成绩信息失败。 "+e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Course> parse() throws IOException {
        super.parse();
        List<Course> result = new LinkedList<>();
        for(AcademicYearAndSemester academicYearAndSemester : mAcademicYearAndSemesters) {
            result.addAll(parse1(
                    academicYearAndSemester.getYear(),
                    academicYearAndSemester.getSemester()));
        }
        return result;
    }

    List<Course> parse0(Document document) {
        //TODO tabid的设置
        return readCourseTable(document.select("div#tab01 [tabid~=0*1] table.ui_table").first());
    }
    List<Course> parse1(int academicYear, int semester) throws IOException {
        mConnectionAgent.getConnection().request().data().clear();
        mConnectionAgent.getConnection()
                .data("qXndm_ys", academicYear + "-" + (academicYear+1))
                .data("qXqdm_ys", String.valueOf(semester));
        Document doc = mConnectionAgent.url(Constant.url.ALL_PERSONAL_GRADES).post();
        return parse0(doc);
    }

    @Override
    protected boolean parseTeachers(Course out, String rawData) {
        throw new AssertionError("Shouldn't go here");
    }

    @Override
    protected boolean parseTimeAndAddress(Course out, String rawTime, String rawAddress) {
        throw new AssertionError("Shouldn't go here");
    }

    /**
     * 学年、学期二元组
     */
    public static class AcademicYearAndSemester {
        private final int year;
        private final int semester;

        public AcademicYearAndSemester(int year, int semester) {
            this.year = year;
            this.semester = semester;
        }

        public int getYear() {
            return year;
        }

        public int getSemester() {
            return semester;
        }
    }
}
