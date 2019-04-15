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
 * 站点名：ITBear科技资讯
 * <p>
 * 主要功能：处理字段中多余的数据
 * @author bfd_01
 *
 */
public class NitbearContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NitbearContentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			String data = unit.getPageData();
			String url = result.getSpiderdata().get("location").toString();
			if (resultData.containsKey(Constants.AUTHOR)
					&& resultData.get(Constants.AUTHOR).toString()
							.contains("背景： A- A A+")) {
				String temp = resultData.get(Constants.AUTHOR).toString()
						.split("背景： A- A A+")[0];
				if (temp.contains("编辑：")) {
					String author = temp.split("编辑：")[1].trim();
					resultData.put(Constants.AUTHOR, author);					
				}
			}
			if (resultData.containsKey(Constants.SOURCE)
					&& resultData.get(Constants.SOURCE).toString()
					.contains("背景： A- A A+")) {
				String temp = resultData.get(Constants.SOURCE).toString()
						.split("背景： A- A A+")[0];
				if (temp.contains("来源：")) {					
					String source = temp.split("来源：")[1].split(" ")[0];
					resultData.put(Constants.SOURCE, source);
				}
			}
			if (resultData.containsKey(Constants.POST_TIME)
					&& resultData.get(Constants.POST_TIME).toString()
					.contains("背景： A- A A+")) {
				String temp = resultData.get(Constants.POST_TIME).toString()
						.split("背景： A- A A+")[0];
				String posttime = null;
				Pattern p = Pattern.compile("(\\d+-\\d+-\\d+ \\d+:\\d+:\\d+)");
				Matcher m = p.matcher(temp);
				while (m.find()) {
					posttime = m.group(1);
				}
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
		String clientId = "";
		Pattern p = Pattern.compile("appid: '(.*?)'");
		Matcher m = p.matcher(data);
		while (m.find()) {
			clientId = m.group(1);
		}
		return clientId;
	}
}
