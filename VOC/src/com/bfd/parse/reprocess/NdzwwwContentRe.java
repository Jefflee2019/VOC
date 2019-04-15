package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Ndzwww
 * 
 * 标准化部分字段
 * 
 * @author bfd_06
 */

public class NdzwwwContentRe implements ReProcessor {
	private Pattern pattenTime = Pattern.compile("\\d+-\\d+-\\d+\\s+\\d+:\\d+(:\\d+)?");
	private Pattern pattenSource = Pattern.compile("来源[:：] (.*) ?作者");
	private Pattern pattenAuthor = Pattern.compile("作者[:：] (.*)");
	private Pattern pattenSourceTwo = Pattern.compile("来源[:：] (.*) ?我要评论");
	private Pattern pattenAuthorTwo = Pattern.compile("作者[:：] (.*) ?来源");
	
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		
		String markUrl = unit.getUrl().replace("http://", "")
				.replace("https://", "");
		markUrl = markUrl.substring(0, markUrl.indexOf(".com") + 4);
		switch (markUrl) {
		case "www.dzwww.com":
			// AUTHOR
			if (resultData.containsKey(Constants.AUTHOR)) {
				formatAuthor(Constants.AUTHOR,
						(String) resultData.get(Constants.AUTHOR), resultData);
			}
			// SOURCE
			if (resultData.containsKey(Constants.SOURCE)) {
				formatSource(Constants.SOURCE,
						(String) resultData.get(Constants.SOURCE), resultData);
			}
			// BRIEF
			if (resultData.containsKey(Constants.BRIEF)) {
				formatBrief(Constants.BRIEF,
						(String) resultData.get(Constants.BRIEF), resultData);
			}
			// CONTENT
			if (resultData.containsKey(Constants.CONTENT)) {
				formatContent(Constants.CONTENT,
						(String) resultData.get(Constants.CONTENT), resultData);
			}
			// posttime 20160721
			if (resultData.containsKey(Constants.POST_TIME)) {
				format_Posttime(resultData);
			}
			break;
//		case "qingdao.dzwww.com":
//			// AUTHOR
//			if (resultData.containsKey(Constants.AUTHOR)) {
//				formatAuthor(Constants.AUTHOR,
//						(String) resultData.get(Constants.AUTHOR), resultData);
//			}
//			// SOURCE
//			if (resultData.containsKey(Constants.SOURCE)) {
//				formatSource(Constants.SOURCE,
//						(String) resultData.get(Constants.SOURCE), resultData);
//			}
//			// KEYWORD
//			if (resultData.containsKey(Constants.KEYWORD)) {
//				formatKeyword(Constants.KEYWORD,
//						(String) resultData.get(Constants.KEYWORD), resultData);
//			}
//			break;
		case "weifang.dzwww.com":
			// POST_TIME
			if (resultData.containsKey(Constants.POST_TIME)) {
				Matcher matcher = pattenTime.matcher((String) resultData.get(Constants.POST_TIME));
				if(matcher.find())
					resultData.put(Constants.POST_TIME, matcher.group());
			}
			// SOURCE
			if (resultData.containsKey(Constants.SOURCE)) {
				Matcher matcher = pattenSourceTwo.matcher((String) resultData.get(Constants.SOURCE));
				if(matcher.find())
					resultData.put(Constants.SOURCE, matcher.group(1));
			}
			// AUTHOR
			if (resultData.containsKey(Constants.AUTHOR)) {
				Matcher matcher = pattenAuthorTwo.matcher((String) resultData.get(Constants.AUTHOR));
				if(matcher.find())
					resultData.put(Constants.AUTHOR, matcher.group(1));
			}
			
			// BRIEF
			if (resultData.containsKey(Constants.BRIEF)) {
				formatBrief2(Constants.BRIEF,
						(String) resultData.get(Constants.BRIEF), resultData);
			}
			break;
		case "jinan.dzwww.com":
			matchPSA(Constants.POST_TIME,Constants.SOURCE,Constants.AUTHOR,resultData);
			// KEYWORD
			if (resultData.containsKey(Constants.KEYWORD)) {
				formatKeyword(Constants.KEYWORD,
						(String) resultData.get(Constants.KEYWORD), resultData);
			}
			break;
		case "jining.dzwww.com":
			String markStr = (String) resultData.get(Constants.POST_TIME);
			if (!markStr.contains("作者")) {
				resultData.remove(Constants.AUTHOR);
			}
			if (!markStr.contains("来源")) {
				resultData.remove(Constants.SOURCE);
			}
			formatAtrr(Constants.POST_TIME, markStr, resultData);
			// AUTHOR
			if (resultData.containsKey(Constants.AUTHOR)) {
				formatAtrr(Constants.AUTHOR, markStr, resultData);
			}
			// SOURCE
			if (resultData.containsKey(Constants.SOURCE)) {
				formatAtrr(Constants.SOURCE, markStr, resultData);
			}
			break;
		case "binzhou.dzwww.com":
			// POST_TIME
			if (resultData.containsKey(Constants.POST_TIME)) {
				Matcher matcher = pattenTime.matcher((String) resultData.get(Constants.POST_TIME));
				if(matcher.find())
					resultData.put(Constants.POST_TIME, matcher.group());
			}
			// SOURCE
			if (resultData.containsKey(Constants.SOURCE)) {
				Matcher matcher = pattenSourceTwo.matcher((String) resultData.get(Constants.SOURCE));
				if(matcher.find())
					resultData.put(Constants.SOURCE, matcher.group(1));
			}
			// AUTHOR
			if (resultData.containsKey(Constants.AUTHOR)) {
				Matcher matcher = pattenAuthorTwo.matcher((String) resultData.get(Constants.AUTHOR));
				if(matcher.find())
					resultData.put(Constants.AUTHOR, matcher.group(1));
			}
			
			// BRIEF
			if (resultData.containsKey(Constants.BRIEF)) {
				formatBrief2(Constants.BRIEF,
						(String) resultData.get(Constants.BRIEF), resultData);
			}
			break;
		case "heze.dzwww.com":
			String markStr2 = (String) resultData.get(Constants.POST_TIME);
			if (!markStr2.contains("作者")) {
				resultData.remove(Constants.AUTHOR);
			}
			if (!markStr2.contains("来源")) {
				resultData.remove(Constants.SOURCE);
			}
			formatAtrr2(Constants.POST_TIME,
					(String) resultData.get(Constants.POST_TIME), resultData);
			// AUTHOR
			if (resultData.containsKey(Constants.AUTHOR)) {
				formatAtrr2(Constants.AUTHOR,
						(String) resultData.get(Constants.AUTHOR), resultData);
			}
			// SOURCE
			if (resultData.containsKey(Constants.SOURCE)) {
				formatAtrr2(Constants.SOURCE,
						(String) resultData.get(Constants.SOURCE), resultData);
			}
			break;
		case "zaozhuang.dzwww.com":
		case "rizhao.dzwww.com":
		case "linyi.dzwww.com":
		case "taian.dzwww.com":
		case "laiwu.dzwww.com":
		case "comic.dzwww.com":
			String url = unit.getUrl();
			if(url.startsWith("http://taian.dzwww.com/2013sy/dhzx")){
				matchPSA(Constants.POST_TIME,Constants.SOURCE,Constants.AUTHOR,resultData);
				break;
			}
			// AUTHOR
			if (resultData.containsKey(Constants.AUTHOR)) {
				formatAuthor(Constants.AUTHOR,
						(String) resultData.get(Constants.AUTHOR), resultData);
			}
			// SOURCE
			if (resultData.containsKey(Constants.SOURCE)) {
				formatSource(Constants.SOURCE,
						(String) resultData.get(Constants.SOURCE), resultData);
			}
			break;
		default:
			// AUTHOR
			if (resultData.containsKey(Constants.AUTHOR)) {
				formatAuthor(Constants.AUTHOR,
						(String) resultData.get(Constants.AUTHOR), resultData);
			}
			// SOURCE
			if (resultData.containsKey(Constants.SOURCE)) {
				formatSource(Constants.SOURCE,
						(String) resultData.get(Constants.SOURCE), resultData);
			}
			// KEYWORD
			if (resultData.containsKey(Constants.KEYWORD)) {
				formatKeyword(Constants.KEYWORD,
						(String) resultData.get(Constants.KEYWORD), resultData);
			}
			break;
		}

		return new ReProcessResult(processcode, processdata);
	}

	private void matchPSA(String postTime, String source, String author, Map<String, Object> resultData) {
		// POST_TIME
		if (resultData.containsKey(postTime)) {
			Matcher matcher = pattenTime.matcher((String) resultData.get(Constants.POST_TIME));
			if(matcher.find())
				resultData.put(Constants.POST_TIME, matcher.group());
		}
		// SOURCE
		if (resultData.containsKey(source)) {
			Matcher matcher = pattenSource.matcher((String) resultData.get(Constants.SOURCE));
			if(matcher.find())
				resultData.put(Constants.SOURCE, matcher.group(1));
		}
		// AUTHOR
		if (resultData.containsKey(author)) {
			Matcher matcher = pattenAuthor.matcher((String) resultData.get(Constants.AUTHOR));
			if(matcher.find())
				resultData.put(Constants.AUTHOR, matcher.group(1));
		}
		
	}

	private void format_Posttime(Map<String, Object> resultData) {
		String value=(String) resultData.get(Constants.POST_TIME);
		int indexA = value.indexOf("来源");
		if (indexA>0) {
			int indexS = value.indexOf("来源");
			value = value.substring(0,indexS - 1).trim();
			resultData.put(Constants.POST_TIME, value);
		} 
	}

	private void formatAtrr(String keyName, String value,
			Map<String, Object> result) {
		switch (keyName) {
		case Constants.POST_TIME:
			value = value.substring(0, 16);
			result.put(keyName, value);
			if (value.equals("")) {
				result.remove(keyName);
			}
			break;
		case Constants.AUTHOR:
			int indexA = value.indexOf("作者");
			if (value.contains("来源")) {
				int indexS = value.indexOf("来源");
				value = value.substring(indexA + 3, indexS - 1);
			} else {
				int indexA2 = value.indexOf("作者");
				value = value.substring(indexA2 + 3);
			}
			result.put(keyName, value);
			if (value.equals("")) {
				result.remove(keyName);
			}
			break;
		case Constants.SOURCE:
			value = value.substring(value.indexOf("来源") + 3);
			result.put(keyName, value);
			if (value.equals("")) {
				result.remove(keyName);
			}
			break;
		default:
			break;
		}
	}

	private void formatAtrr2(String keyName, String value,
			Map<String, Object> result) {
		switch (keyName) {
		case Constants.POST_TIME:
			value = value.substring(0, 16);
			result.put(keyName, value);
			if (value.equals("")) {
				result.remove(keyName);
			}
			break;
		case Constants.SOURCE:
			int indexS = value.indexOf("来源");
			if (value.contains("作者")) {
				int indexA = value.indexOf("作者");
				value = value.substring(indexS + 3, indexA - 1);
			} else {
				int indexS2 = value.indexOf("来源");
				value = value.substring(indexS2 + 3);
			}
			result.put(keyName, value);
			if (value.equals("")) {
				result.remove(keyName);
			}
			break;
		case Constants.AUTHOR:
			value = value.substring(value.indexOf("作者") + 3);
			result.put(keyName, value);
			if (value.equals("")) {
				result.remove(keyName);
			}
			break;
		default:
			break;
		}
	}

	private void formatAuthor(String keyName, String value,
			Map<String, Object> result) {
		value = value.replace("作者：", "").trim();
		result.put(keyName, value);
		if (value.equals("")) {
			result.remove(Constants.AUTHOR);
			//针对不是空的，取数据操作 20160720
			//2016-07-08 11:19:48 来源: 荆楚网 作者: 夏中华
		}else{
		String author[]=value.split ("作者:");
			if (author.length >1) {
				if (StringUtils.isNotEmpty(author[1])) {
					result.put(Constants.AUTHOR, author[1].trim());
				} else {
					result.remove(Constants.AUTHOR);
				}
			}else {
				result.remove(Constants.AUTHOR);
			}
		}
	}

	private void formatSource(String keyName, String value,
			Map<String, Object> result) {
		value = value.replace("来源：", "").trim();
		result.put(keyName, value);
		if (value.equals("")) {
			result.remove(Constants.SOURCE);
		}else{
			String source[] = value.split("来源:");
			if (source.length > 1) {
				if (StringUtils.isNotEmpty(source[1])) {
					String sourceRes = source[1].substring(1, source[1].trim()
							.indexOf(" ") + 1);
					result.put(Constants.SOURCE, sourceRes);
				} else {
					result.remove(Constants.SOURCE);
				}
			} else {
				result.remove(Constants.SOURCE);
			}
		}
	}

	private void formatKeyword(String keyName, String value,
			Map<String, Object> result) {
		value = value.replace("关键词：", "").trim();
		result.put(keyName, value);
		if (value.equals("")) {
			result.remove(Constants.KEYWORD);
		}
	}

	private void formatBrief(String keyName, String value,
			Map<String, Object> result) {
		value = value.replace("【摘要】", "");
		result.put(keyName, value);
		if (value.equals("")) {
			result.remove(Constants.BRIEF);
		}
	}

	private void formatBrief2(String keyName, String value,
			Map<String, Object> result) {
		if (value.contains("[提要]")) {
			value = value.substring(value.indexOf("[提要]") + 4);
		}
		result.put(keyName, value);
		if (value.equals("")) {
			result.remove(Constants.BRIEF);
		}
	}

	private void formatContent(String keyName, String value,
			Map<String, Object> result) {
		if (value.contains("【摘要】")) {
			value = value.replace("【摘要】", "");
		}
		result.put(keyName, value);
	}

}
