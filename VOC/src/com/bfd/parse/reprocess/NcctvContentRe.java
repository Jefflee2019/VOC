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
 * @site:CCTV-新闻 (Ncctv)
 * @function 新闻内容页后处理插件
 * 
 * @author bfd_02
 *
 */

public class NcctvContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NcctvContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未获取到解析数据");
			return null;
		}

		/**
		 * @param source
		 * @function 多模板新闻来源结构标准化 
		 * 			 eg1:"北京日报 2015年08月21日 08:36";
		 *           eg2:"发布时间:2014年07月22日 16:10 | 进入复兴论坛 | 来源：互联网 | 手机看新闻"
		 *           eg3:"来源：新华网 发布时间:2015年10月22日"
		 */

		if (resultData.containsKey(Constants.SOURCE)) {
			String oldSource = (String) resultData.get(Constants.SOURCE);
			if (!oldSource.isEmpty()) {
				if (oldSource.contains("|")) {
					String[] sourcearr = oldSource.split("\\|");
					String sourcetemp = sourcearr[2];
					int index = sourcetemp.indexOf("：");
					String newSource = sourcetemp.substring(index + 1);
					resultData.put(Constants.SOURCE, newSource);
				}else {
					String[] sourcearr = oldSource.trim().split(" ");
					String newSource = sourcearr[0];
					if(newSource.contains("来源")){
						int index = newSource.indexOf("：");
						newSource = newSource.substring(index + 1);
					}
					resultData.put(Constants.SOURCE, newSource);
				}
			}
		}

		/**
		 * @param post_time
		 * @function 多模板发表时间标准化 
		 *           eg1:发布时间:2014年12月18日 12:22 |
		 *           eg2:发布时间:2014年07月22日 16:10 | 进入复兴论坛 | 来源：互联网 | 手机看新闻
		 *           eg3:北京日报 2015年08月21日 08:36
		 *           eg4:来源：新华网 发布时间:2015年10月22日
		 * 
		 */

		if (resultData.containsKey(Constants.POST_TIME)) {
			String oldPostTime = (String) resultData.get(Constants.POST_TIME);
			if (!oldPostTime.isEmpty()) {
				String regex = "\\d{4}\\S\\d{1,2}\\S\\d{1,2}\\S\\s*(\\d{2}:\\d{2})?";
				Pattern ptn = Pattern.compile(regex);
				Matcher m = ptn.matcher(oldPostTime);
				if (m.find()) {
					String newPostTime = m.group();
					resultData.put(Constants.POST_TIME, newPostTime);
				}
			}
		}
		
		/**
		 * @param source
		 * @function 某些模板来源格式化 
		 *           eg：新华网 2015年10月22日 14:42
		 * 
		 */
			if(resultData.containsKey(Constants.SOURCE)) {
				  String source = resultData.get(Constants.SOURCE).toString();
				  Matcher match = Pattern.compile("(\\S*)\\s*\\d+年").matcher(source);
				  if(match.find()) {
					  source = match.group(1);
					  resultData.put(Constants.SOURCE, source);
				  }
			}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}