package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
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
 * 站点名：Nkaixian
 * 
 * 主要功能：处理来源，发表时间和生成评论任务
 * 
 * @author bfd_01
 */
public class NkaixianContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NkaixianContentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData != null && !resultData.isEmpty()) {
			String url = result.getSpiderdata().get("location").toString();
			String data = unit.getPageData();
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString();
				source = source.split(" ")[2];
				resultData.put(Constants.SOURCE, source.replace("来源：", ""));
			}
			if (resultData.containsKey(Constants.POST_TIME)) {
				String posttime = resultData.get(Constants.POST_TIME).toString();
				posttime = posttime.split(" ")[0] + " " + posttime.split(" ")[1];
				resultData.put(Constants.POST_TIME, posttime);
			}
			
			//comments
			// 处理评论链接
			Map<String, Object> commentTask = new HashMap<String, Object>();
			String urlHead = "http://changyan.sohu.com/node/html?client_id=";
					//cyqSGofPc&topicurl=http://www.kaixian.tv/gd/2016/0301/296421.html";
			
			String clientId = getClientId(data);
			if (!"".equals(clientId)) {
				String comUrl = urlHead + clientId + "&topicurl=" + url;
				commentTask.put("link", comUrl);
				commentTask.put("rawlink", comUrl);
				commentTask.put("linktype", "newscomment");
				LOG.info("url:" + url + "taskdata is "
						+ commentTask.get("link") + commentTask.get("rawlink")
						+ commentTask.get("linktype"));
				if (resultData != null && !resultData.isEmpty()) {
					resultData.put("comment_url", comUrl);
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
							.get("tasks");
					tasks.add(commentTask);
				}
				// 后处理插件加上iid
				ParseUtils.getIid(unit, result);
			}
		}
		return new ReProcessResult(processcode, processdata);
	}

	private String getClientId(String data) {
		// var appid = 'cyqSGofPc'
		String clientId = "";
		Pattern p = Pattern.compile("appid = '(.*?)'");
		Matcher m = p.matcher(data);
		while (m.find()) {
			clientId = m.group(1);
		}
		return clientId;
	}
}
