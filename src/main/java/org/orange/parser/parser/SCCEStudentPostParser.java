package org.orange.parser.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.orange.parser.entity.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SCCEStudentPostParser extends BasePostParser {
    private static final Pattern PATTERN_PAGE_NUMBER = Pattern.compile("共(\\d+)页");

    @Override
    public List<Post> parse() throws IOException {
        super.parse();
        final List<Post> result = new LinkedList<>();
        final List<String> categories;
        if(mFilterCategories == null) {
            categories = Arrays.asList(Post.CATEGORYS.IN_STUDENT_WEBSITE_OF_SCCE);
        } else {
            categories = new ArrayList<>(mFilterCategories);
        }
        if(mFilterMax == null) {
            for (String category : categories) {
                result.addAll(parsePostsFromSCCEStudent(category, null));
            }
        } else {
            for (String category : categories) {
                if (result.size() >= mFilterMax)
                    break;
                result.addAll(parsePostsFromSCCEStudent(category, mFilterMax - result.size()));
            }
        }
        return result;
    }

    /**
     * 以category、max为限制条件，利用readHelper，从SCCE学生网站解析posts
     * @param category 类别，例如Post.CATEGORYS.SCCE_STUDENT_NOTICES。不可为null
     * @param max      用于限制返回的Posts的数量，最多返回约max条Post。max<=0时返回空List
     * @return 符合条件的posts；如果category不可识别，返回空List
     */
    private List<Post> parsePostsFromSCCEStudent(String category, final Long max)
            throws IOException {
        if (category == null) {
            throw new NullPointerException("category is null");
        }
        if (max != null) {
            if (max < 0) throw new IllegalArgumentException("max < 0");
            else if (max == 0) return Collections.emptyList();
        }

        final List<Post> result = new LinkedList<>();
        String url = generateUrl(category);
        if (url == null) return result;
        url += "?page=";

        try {
            Document doc = mConnectionAgent.url(url + "1").get();
            result.addAll(parsePostsFromSCCEStudent(category, max, doc));
            Matcher matcher = PATTERN_PAGE_NUMBER.matcher(doc.select(".oright .page").first().text());
            int page = 0;
            if (matcher.find()) {
                page = Integer.parseInt(matcher.group(1));
            } else {
                mParseListener.onWarn(ParseListener.WARNING_CANNOT_PARSE_PAGE_NUMBER,
                        "不能从计算机学院学生网站解析页数，现仅解析第一页内容。");
            }

            for (int i = 2; i <= page; i++) {
                if (max != null && result.size() >= max) {
                    break;
                }
                if (mFilterMinDate != null) {
                    if(result.isEmpty()) {
                        // ∵有max、时间两种筛选器 && max>0 && 此category的page>=2
                        // ∴是时间限制导致result.isEmpty()
                        break;
                    }
                    Date last = result.get(result.size() - 1).getDate();
                    if (last != null && last.before(mFilterMinDate)) {
                        break;
                    } else if (last == null) {
                        mParseListener.onWarn(ParseListener.WARNING_CANNOT_PARSE_DATE,
                                "解析计算机学院学生网站通知/文章的日期失败，导致时间过滤器失效 ");
                    }
                }
                doc = mConnectionAgent.url(url + i).get();
                Long maxParam = max == null ? null : max - result.size();
                result.addAll(parsePostsFromSCCEStudent(category, maxParam, doc));
            }
        } catch (IOException e) {
            mParseListener.onError(ParseListener.ERROR_IO,
                    "遇到IO异常，无法打开页面，解析计算机学院学生网站信息失败。 " + e.getMessage());
            throw e;
        }

        return result;
    }

    /**
     * 以category、max为限制条件，从计算机学院学生网站的指定网页中解析posts
     * @param aCategory 类别，例如Post.CATEGORYS.SCCE_STUDENT_NOTICES
     * @param max       用于限制返回的Posts的数量，最多返回约max条Post。max<=0时返回空List
     * @param doc       包含post列表的 某计算机学院学生网站网页的 Document
     * @return 符合条件的posts
     */
    private List<Post> parsePostsFromSCCEStudent(String aCategory, final Long max, Document doc) {
        List<Post> result = new LinkedList<>();
        if (max != null) {
            if (max < 0) throw new IllegalArgumentException("max < 0");
            else if (max == 0) return result;
        }
        Elements posts = doc.select(".oright .orbg ul li");
        for (Element postLi : posts) {
            if (max != null && result.size() >= max) {
                break;
            }
            Post post = new Post();
            try {
                post.setDate(postLi.getElementsByClass("date").first().text().substring(1, 11));
                if (mFilterMinDate != null && post.getDate().before(mFilterMinDate)) {
                    break;
                }
            } catch (java.text.ParseException e) {
                mParseListener.onWarn(ParseListener.WARNING_CANNOT_PARSE_DATE,
                        "解析计算机学院学生网站通知/文章的日期失败。 " + e.getMessage());
            }
            Element link = postLi.getElementsByTag("a").first();
            post.setTitle(link.text().trim());
            post.setUrl(link.attr("abs:href"));
            post.setSource(Post.SOURCES.STUDENT_WEBSITE_OF_SCCE).setCategory(aCategory);
            result.add(post);
        }
        return result;
    }

    private String generateUrl(String category) {
        //TODO 统一管理URL常量
        final String url = "http://59.67.152.6/Channels/";
        switch (category) {
            case Post.CATEGORYS.SCCE_STUDENT_NEWS:
                return url + "7";
            case Post.CATEGORYS.SCCE_STUDENT_NOTICES:
                return url + "9";
            case Post.CATEGORYS.SCCE_STUDENT_UNION:
                return url + "45";
            case Post.CATEGORYS.SCCE_STUDENT_EMPLOYMENT:
                return url + "43";
            case Post.CATEGORYS.SCCE_STUDENT_YOUTH_LEAGUE:
                return url + "29";
            case Post.CATEGORYS.SCCE_STUDENT_DOWNLOADS:
                return url + "16";
            case Post.CATEGORYS.SCCE_STUDENT_JOBS:
                return url + "55";
            default:
                System.err.println(getClass().getName() + ": Unknown category:" + category);
                return null;
        }
    }

}
