package com.bfd.parse.reprocess;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点名：移动叔叔
 * <p>
 * 主要功能：处理生成任务的链接
 * @author bfd_01
 *
 */
public class N360ListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(N360ListRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			//做url的截取，避免url中每次都变得部分导致列表页一直刷
			if(resultData.containsKey(Constants.TASKS)){
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
				for (Map<String, Object> task : tasks) {
					if (task.containsKey(Constants.LINK)
							&& "newscontent".equals(task.get("linktype")
									.toString())) {
						String link = task.get(Constants.LINK).toString();
						decodeLink(task, link);
					}
				}
			}
			
		}
		 ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
	private void decodeLink(Map<String, Object> task, String link) {
		try {
			if (link.contains("url=")) {
				int beginIndex = link.indexOf("url=");
				int endIndex = link.indexOf("&", beginIndex);
				if(endIndex != -1){
					link = link.substring(beginIndex + 4, endIndex);
				}else{
					link = link.substring(beginIndex + 4);
				}
				link = URLDecoder.decode(link, "utf-8");
				task.put(Constants.LINK, link);
				task.put(Constants.RAWLINK, link);
			}
		} catch (UnsupportedEncodingException e) {
			LOG.error("Nydss eror" + "decode错误");
		}
	}

}
