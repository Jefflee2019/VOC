package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.Arrays;
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
 * 站点名：京华时报
 * <p>
 * 主要功能：处理新闻页内容，发表时间，作者，来源等字段
 * @author bfd_01
 *
 */
public class NjinghuaContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NjinghuaContentRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!resultData.isEmpty()) {
			String url = result.getSpiderdata().get("location").toString();
			if (url.contains("epaper")) {
				String source =  resultData.get(Constants.SOURCE)
						.toString();;
				String posttime =  resultData.get(Constants.POST_TIME)
						.toString();;
				if (resultData.containsKey(Constants.AUTHOR)) {
					if (!resultData.get(Constants.AUTHOR).toString()
							.contains("|")) {
						String author = resultData.get(Constants.AUTHOR)
								.toString();
						if (author.split("记者：").length > 1) {
							resultData.put(Constants.AUTHOR,
									author.split("记者：")[1].trim());
						}

						if (resultData.containsKey(Constants.SOURCE)) {
							source = resultData.get(Constants.SOURCE)
									.toString();
							source = source
									.split("来源：")[1].split("记者")[0].trim();
						}
						if (resultData.containsKey(Constants.POST_TIME)) {
							posttime = resultData.get(
									Constants.POST_TIME).toString();
							posttime = posttime.split(" ")[1];
						}

						if (resultData.containsKey(Constants.CATE)) {
							// 取到cate
							List<String> cate = (List<String>) resultData.get(Constants.CATE);
							String[] catetemp = cate.get(0).toString()
									.replace("[", "").replace("]", "")
									.split(" >> ");
							resultData.put(Constants.CATE, Arrays.asList(catetemp));
						}
					} else {
						if (resultData.containsKey(Constants.AUTHOR)) {
							String author = resultData.get(Constants.AUTHOR)
									.toString();
							author = author.split("\\|")[0].replace("记者：", "")
									.trim();
							resultData.put(Constants.AUTHOR, author);
						}
						if (resultData.containsKey(Constants.POST_TIME)) {
							posttime = resultData.get(
									Constants.POST_TIME).toString();
							if (posttime.split("\\|").length > 1) {
								posttime = posttime.split("\\|")[1].replace(
										"时间：", "").trim();
							}
						}
						if (resultData.containsKey(Constants.CATE)) {
							// 取到cate
							List<String> cate = (List<String>) resultData.get(Constants.CATE);
							String[] catetemp = cate.get(0).toString()
									.replace("[", "").replace("]", "")
									.split(">>");
							resultData.put(Constants.CATE, Arrays.asList(catetemp));
						}
					}
				} else {
					if (resultData.containsKey(Constants.CATE)) {
						List<String> temp = new ArrayList<String>();
						List<String> list = (List<String>) resultData.get(Constants.CATE);
						for (int i = 0; i < list.size(); i++) {
							String cate = list.get(i).toString()
									.replace(">>", "").replace(" ", "");
							temp.add(cate);
						}
						resultData.put(Constants.CATE, temp);
					}
				}

				if (source != null && posttime != null) {
					source = source.replace("来源：", "").replace("时间：", "");
					posttime = posttime.replace("来源：", "").replace("时间：", "");
					Pattern p = Pattern.compile("(\\d+\\S\\d+\\S\\d+)");
					Matcher m = p.matcher(source);
					Matcher m1 = p.matcher(posttime);

					while (m.find() && (!m1.find())) {
						String temp = source;
						source = posttime;
						posttime = temp;
					}
				}
				resultData.put(Constants.POST_TIME, posttime);
				resultData.put(Constants.SOURCE, source);
				
				// 添加评论页任务
				Map<String, Object> commentTask = new HashMap<String, Object>();
				String urlHead = "http://changyan.sohu.com/node/html?client_id=cyqhI9Dlb";
				String comUrl = urlHead + "&topicurl=" + url;
				commentTask.put("link", comUrl);
				commentTask.put("rawlink", comUrl);
				commentTask.put("linktype", "newscomment");
				LOG.info("url:" + url + "taskdata is "
						+ commentTask.get("link")
						+ commentTask.get("rawlink")
						+ commentTask.get("linktype"));
				if (!resultData.isEmpty()) {
					resultData.put("comment_url", comUrl);
					List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get("tasks");
					tasks.add(commentTask);
				}
				// 后处理插件加上iid
				ParseUtils.getIid(unit, result);

			} else if(url.contains("it.jinghua")){
				if (resultData.containsKey(Constants.CATE)) {
					// 取到cate
					List<String> cate = (List<String>) resultData.get(Constants.CATE);
					String[] catetemp = cate.get(0).toString()
							.replace("[", "").replace("]", "")
							.replace("> >", ">").split(" > ");
					resultData.put(Constants.CATE, Arrays.asList(catetemp));
				}
				if (resultData.containsKey(Constants.AUTHOR)) {
					String author = resultData.get(Constants.AUTHOR).toString();
					resultData.put(Constants.AUTHOR,
							author.split("作者：")[1].split("出处：")[0].trim());
				}
				if (resultData.containsKey(Constants.SOURCE)) {
					String source = resultData.get(Constants.SOURCE).toString();
					resultData.put(Constants.SOURCE,
							source.split("出处：")[1].split("责编：")[0].trim());
				}
				if (resultData.containsKey(Constants.POST_TIME)) {
					String posttime = resultData.get(Constants.POST_TIME).toString();
					resultData.put(Constants.POST_TIME,
							posttime.split(" ")[0]);
				}
			} else if (url.contains("news.jinghua")) {
				if (resultData.containsKey(Constants.POST_TIME)) {
					String posttime = resultData.get(Constants.POST_TIME)
							.toString();
					if (posttime.split(" ").length > 2) {
						resultData.put(Constants.POST_TIME,
								posttime.split(" ")[1]
										+ " " + posttime.split(" ")[2]);
					}
				}
				
				if (resultData.containsKey(Constants.SOURCE)) {
					String source = resultData.get(Constants.SOURCE).toString();
					resultData.put(Constants.SOURCE, source.replace("来源：", ""));
				}
				
				if (resultData.containsKey(Constants.CATE)) {
					// 取到cate
					List<String> cate = (List<String>) resultData.get(Constants.CATE);
					String temp = null;
					if (cate.get(0).toString().contains("订阅京华手机报")) {
						temp = cate.get(0).toString().replace("订阅京华手机报", "");
					} else {
						temp = cate.get(0).toString().substring(2);
					}
					String[] catetemp = temp.replace("[", "").replace("]", "")
							.replace("\t", "").replace(" ", "").split(">>");
					resultData.put(Constants.CATE, Arrays.asList(catetemp));
				}
				
				// 添加评论页任务
				Map<String, Object> commentTask = new HashMap<String, Object>();
			    String urlHead = "http://changyan.sohu.com/node/html?client_id=cyqhI9Dlb";
			    String comUrl = urlHead + "&topicurl=" + url;
			    commentTask.put("link", comUrl);
				commentTask.put("rawlink", comUrl);
				commentTask.put("linktype", "newscomment");
				LOG.info("url:" + url + "taskdata is " + commentTask.get("link") + commentTask.get("rawlink") + commentTask.get("linktype"));
				if (!resultData.isEmpty()) {
					resultData.put("comment_url", comUrl);
					List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get("tasks");
					tasks.add(commentTask);
				}
				// 后处理插件加上iid
				ParseUtils.getIid(unit,result);
			} else if (url.contains("yesky.jinghua")) {
				if (resultData.containsKey(Constants.CATE)) {
					// 取到cate
					List<String> cate = (List<String>) resultData.get(Constants.CATE);
					String[] catetemp = cate.get(0).toString()
							.replace("[", "").replace("]", "").split(" > ");
					resultData.put(Constants.CATE, Arrays.asList(catetemp));
				}
			
				if (resultData.containsKey(Constants.AUTHOR)) {
					String author = resultData.get(Constants.AUTHOR).toString();
					resultData.put(Constants.AUTHOR,
							author.split(" ")[1].split("作者：")[1].trim());
				}
				if (resultData.containsKey(Constants.POST_TIME)) {
					String posttime = resultData.get(Constants.POST_TIME)
							.toString();
					resultData
							.put(Constants.POST_TIME, posttime.split(" ")[0]);
				}
				if (resultData.containsKey(Constants.SOURCE)) {
					String source = resultData.get(Constants.SOURCE).toString();
					resultData.put(Constants.SOURCE,
							source.split("出处：")[1].split(" ")[1].trim());
				}
			}
		}
		return new ReProcessResult(processcode, processdata);
	}
}
