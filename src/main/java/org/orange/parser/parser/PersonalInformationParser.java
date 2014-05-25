package org.orange.parser.parser;

import com.google.gson.Gson;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersonalInformationParser extends AbstractParser<Map<String, Map<String, String>>> {
    private static final Pattern SELECTRANGE_URL_PATTERN = Pattern.compile(
            "ssfwConfig\\s*=\\s*\\{.*selectrangeUrl\\s*:\\s*['\"]([\\w/\\.]+)['\"].*\\}",
            Pattern.DOTALL);


    /**
     * 解析个人信息
     * @return 解析结果，外层{@link Map}的{@code key}是组别(group)名，内层的{@code key}是字段名。
     * <p>示例：result.get("基本信息").get("姓名")</p>
     */
    @Override
    public Map<String, Map<String, String>> parse() throws IOException {
        super.parse();
        Document doc = mConnectionAgent.url(Constant.url.PERSONAL_INFORMATION).get();
        return parse1(doc);
    }

    Map<String, Map<String, String>> parse1(Document input) throws IOException {
        Elements groups = input.select("#form1 .tableGroup");
        List<Item> itemsNeedTranslate = new ArrayList<>();
        Map<String, Map<String, String>> result = new HashMap<>(groups.size());
        for(Element group : groups) {
            String groupName = group.getElementsByTag("h4").text();
            Elements tables = group.getElementsByTag("table");
            if(groupName.length() == 0 || tables.isEmpty()) {
                mParseListener.onWarn(ParseListener.WARNING_STRUCTURE_CHANGED, "解析个人信息时，遇到空键值对group");
            } else {
                if(tables.size() > 1)
                    mParseListener.onWarn(ParseListener.WARNING_STRUCTURE_CHANGED, "解析个人信息时，键值对group中有多个tables");
                List<Item> items = readItems(tables.first(), groupName);
                if (!items.isEmpty()) {
                    result.put(groupName, toKeyValues(items));
                    itemsNeedTranslate.addAll(itemsNeedTranslate(items));
                }
            }
        }
        translateItem(input, result, itemsNeedTranslate);
        return result;
    }

    /**
     * 向网站查询，把itemsNeedTranslate的code value转换为字符串。例如它会把证件类型“1”转换为“身份证”
     * @param input 个人信息原始网页
     * @param result 需要更新的最终解析结果
     * @param itemsNeedTranslate 需要翻译的个人信息项目
     */
    //此方法的实现参考自
    //        http://ssfw.tjut.edu.cn/ssfw/resources/js/ssfw/widgets/base/jquery.ui.editableForm.js
    void translateItem(Document input, Map<String, Map<String, String>> result,
            List<Item> itemsNeedTranslate) throws IOException {
        Connection connection = mConnectionAgent.getConnection();
        connection.request().data().clear();
        int id = 1; // id从1开始，即id = itemsNeedTranslate的index + 1
        for (Item item : itemsNeedTranslate) {
            connection.data("baseTypes", item.baseType)
                    .data("values", item.value)
                    .data("keys", String.valueOf(id));
            id++;
        }
        Matcher matcher = SELECTRANGE_URL_PATTERN.matcher(input.select("script:not([src])").html());
        if (!matcher.find()) {
            mParseListener.onWarn(ParseListener.WARNING_STRUCTURE_CHANGED,
                    "解析个人信息时，无法解析到用于翻译code的URL");
            return;
        }
        Document resultDocument;
        try {
            resultDocument = connection
                    .ignoreContentType(true)
                    .url(new URL(new URL(input.location()), matcher.group(1)))
                    .post();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        TranslateResultArray translateResults =
                new Gson().fromJson(resultDocument.text(), TranslateResultArray.class);
        for(TranslateResult translateResult : translateResults.selectRange) {
            Item item = itemsNeedTranslate.get(translateResult.key - 1);
            assert Objects.equals(item.baseType, translateResult.baseType);
            assert Objects.equals(item.value, translateResult.value);
            result.get(item.groupName).put(item.key, translateResult.label);
        }
    }

    private List<Item> readItems(Element from, String grounName) {
        final List<Item> items = new LinkedList<>();
        for(Element valueElement : from.select("td:has(input[value~=\\S+])")) {
            try {
                final Item item = new Item();
                item.groupName = grounName;
                //previousElementSibling() may be null ↓
                item.key = valueElement.previousElementSibling().text();
                Elements inputElements = valueElement.getElementsByTag("input");
                item.baseType = inputElements.attr("basetype");
                item.value = inputElements.attr("value");
                items.add(item);
            } catch (NullPointerException e) {
                mParseListener.onWarn(ParseListener.WARNING_STRUCTURE_CHANGED, "解析键值对时，找不到值的键");
            }
        }
        return items;
    }
    private Map<String, String> toKeyValues(List<Item> items) {
        Map<String, String> result = new HashMap<>();
        for (Item item : items) {
            result.put(item.key, item.value);
        }
        return result;
    }
    private List<Item> itemsNeedTranslate(List<Item> items) {
        List<Item> result = new LinkedList<>();
        // 时机成熟时，用Java8的filter替换此循环
        for (Item item : items) {
            if (!item.isBaseComponent()) result.add(item);
        }
        return result;
    }

    private static class Item {
        private static final List<String> BASE_TYPES_OF_BASE_COMPONENT = Arrays.asList(
                "text","date","photo","textarea","kindedit","attachment","monthPicker");

        String groupName;
        String key;
        String baseType;
        String value;

        boolean isBaseComponent() {
            if (baseType == null) throw new NullPointerException("baseType is null. Item: " + this);
            return BASE_TYPES_OF_BASE_COMPONENT.contains(baseType);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + '{' +
                    "groupName=" + groupName +
                    ", key=" + key +
                    ", baseType=" + baseType +
                    ", value=" + value +
                    '}';
        }
    }
    private static class TranslateResult {
        int key;
        String value;
        String label;
        String baseType;
    }
    private static class TranslateResultArray {
        TranslateResult[] selectRange;
    }

}
