package org.orange.parser.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PersonalInformationParser extends AbstractParser<Map<String, Map<String, String>>> {

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

	//TODO 完善部分内部代码的解析，如现在的“证件类型=1”中的1
	Map<String, Map<String, String>> parse1(Document input) throws IOException {
		Elements groups = input.select("#form1 .tableGroup");
		Map<String, Map<String, String>> result = new HashMap<>(groups.size());
		for(Element group : groups) {
			String groupName = group.getElementsByTag("h4").text();
			Elements tables = group.getElementsByTag("table");
			if(groupName.length() == 0 || tables.isEmpty()) {
				mParseListener.onWarn(ParseListener.WARNING_STRUCTURE_CHANGED, "解析个人信息时，遇到空键值对group");
			} else {
				if(tables.size() > 1)
					mParseListener.onWarn(ParseListener.WARNING_STRUCTURE_CHANGED, "解析个人信息时，键值对group中有多个tables");
				Map<String, String> keyValues = readKeyValues(tables.first());
				if(!keyValues.isEmpty())
					result.put(groupName, keyValues);
			}
		}
		return result;
	}

	private Map<String, String> readKeyValues(Element from) {
		Map<String, String> result = new HashMap<>();
		for(Element valueElement : from.select("td:has(input[value~=\\S+])")) {
			try {
				String key = valueElement.previousElementSibling().text(); //previousElementSibling() may be null
				String value = valueElement.getElementsByTag("input").attr("value");
				result.put(key, value);
			} catch (NullPointerException e) {
				mParseListener.onWarn(ParseListener.WARNING_STRUCTURE_CHANGED, "解析键值对时，找不到值的键");
			}
		}
		return result;
	}

}
