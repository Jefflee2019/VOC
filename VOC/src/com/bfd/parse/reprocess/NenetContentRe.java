package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:硅谷动力-新闻 (Nenet)
 * @function 新闻内容页后处理插件
 * 
 * @author bfd_02
 *
 */

public class NenetContentRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(NenetContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}

		/**
		 * @param source
		 * @function 多模板新闻来源结构标准化 
		 *  eg1: "http://www.enet.com.cn/networks/ 2109年09月27日11：38 来源：eNet硅谷动力 字号： 小 | 大";
		 *  eg2: "http://www.enet.com.cn/enews/ 2015年04月14日15：56 来源：eNet硅谷动力 作者:王若林"
		 */

		if (resultData.containsKey(Constants.SOURCE)) {
			String oldSource = (String) resultData.get(Constants.SOURCE);
			if (oldSource != null && !oldSource.equals("")) {
				Pattern ptn = Pattern.compile("来源：(\\S+)\\s");
				Matcher match = ptn.matcher(oldSource);
				if(match.find()) {
					String newSource = match.group(1);
					resultData.put(Constants.SOURCE, newSource);
				}
			}
		}

		/**
		 * @param post_time
		 * @function 多模板发表时间标准化 → 2015年04月14日15：56
		 *   eg1:"http://www.enet.com.cn/enews/ 2015年04月14日15：56 来源：eNet硅谷动力 作者:王若林"
		 *   eg2:"http://www.enet.com.cn/networks/ 2109年09月27日11：38 来源：eNet硅谷动力 字号： 小 | 大"
		 * 
		 */
		if (resultData.containsKey(Constants.POST_TIME)) {
			String oldPostTime = (String) resultData.get(Constants.POST_TIME);
			if (oldPostTime != null && !oldPostTime.equals("")) {
				String regex = "\\d{4}\\S\\d{1,2}\\S\\d{1,2}\\S\\s*\\d{2}\\S\\d{2}";
				Pattern ptn = Pattern.compile(regex);
				Matcher match = ptn.matcher(oldPostTime);
				if (match.find()) {
					String newPostTime = match.group();
					resultData.put(Constants.POST_TIME, newPostTime);
				}
			}
		}
		
		/**
		 * @param author
		 * @function 作者字段格式化
		 *   eg:"author": "http://www.enet.com.cn/enews/ 2015年04月14日15：56 来源：eNet硅谷动力 作者:王若林", 
		 */
		if(resultData.containsKey(Constants.AUTHOR)) {
			String oldAuthor = (String) resultData.get(Constants.AUTHOR);
			if(oldAuthor != null && !oldAuthor.equals("")) {
				Pattern ptn = Pattern.compile("作者:(\\S+)");
				Matcher match = ptn.matcher(oldAuthor);
				if(match.find()) {
					String newAuthor = match.group(1);
					resultData.put(Constants.AUTHOR, newAuthor);
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}