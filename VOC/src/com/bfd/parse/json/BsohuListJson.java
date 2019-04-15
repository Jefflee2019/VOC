package com.bfd.parse.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.parse.entity.Constants;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;
import com.bfd.parse.util.Unicode2UTF;

/**
 * 站点名：Bsohu
 * 
 * 动态解析列表页
 * 
 * @author bfd_06
 * 
 */
public class BsohuListJson implements JsonParser {
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		/**
		 * JsonData为List的原因为jsEngine有时会请求好几个链接
		 */
		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			// int indexA = json.indexOf("(");
			// int indexB = json.lastIndexOf(")");
			// if (indexA >= 0 && indexB >= 0 && indexA < indexB) {
			// json = json.substring(indexA + 1, indexB);
			// }
			executeParse(parsedata, json, data.getUrl(), unit);
		}
		JsonParserResult result = new JsonParserResult();
		result.setParsecode(parsecode);
		result.setData(parsedata);
		return result;
	}

	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		/**
		 * 加上tasks
		 */
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", tasks);
		String regular1 = "\"#bbs_list\",\"content\":\"(.*?)</script>";
		Pattern patten1 = Pattern.compile(regular1);
		Matcher matcher1 = patten1.matcher(json);
		String regular2 = "http://.*?/";
		Pattern patten2 = Pattern.compile(regular2);
		Matcher matcher2 = patten2.matcher(url);
		if (matcher1.find() && matcher2.find()) {
			String htmlContent = matcher1.group(1);
			String domain = matcher2.group();
			domain = domain.substring(0, domain.length() - 1);
			htmlContent = htmlContent.substring(0,
					htmlContent.lastIndexOf("css") - 3);
			htmlContent = htmlContent.replace("\\\"", "\"").replace("\\/", "/")
					.replace("\\n", "").replace("\\r", ""); // 去除多余字符
			htmlContent = Unicode2UTF.decodeUnicode(htmlContent); // 解码
			HtmlCleaner cleaner = new HtmlCleaner(); // 封装成HtmlCleaner
			TagNode root = cleaner.clean(htmlContent);
			try {
				TagNode div = (TagNode) root
						.evaluateXPath("//div[@id='bbs_list']")[0];
				TagNode tbody = div.getChildTags()[0].getChildTags()[1];
				int childsSize = tbody.getChildTagList().size();
				int i = 0;
				List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
				for (; i < childsSize - 1; i++) {
					TagNode tr = tbody.getChildTags()[i];
					if (tr.hasAttribute("id")) {
						continue;
					}
					Map<String, Object> itemlink = new HashMap<String, Object>();
					Map<String, Object> item = new HashMap<String, Object>();
					String iteamName = tr.getChildTags()[1].getChildTags()[0]
							.getAttributeByName("title");
					if(iteamName==null)
						iteamName = tr.getChildTags()[1].getChildTags()[1]
								.getAttributeByName("title");
					String href = tr.getChildTags()[1].getChildTags()[0]
							.getAttributeByName("href");
					String iteamUrl = domain + href;
					String replyNum = tr.getChildTags()[0].getText().toString();
					replyNum = replyNum.substring(0, replyNum.indexOf(" "))
							.replace(" ", "");
					String time = tr.getChildTags()[3].getChildTags()[1]
							.getText().toString();
					
					SimpleDateFormat format = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm");
					if (time.matches("[0-9]+")) {
						long timeL = Long.parseLong(time) * 1000;
						item.put(Constants.POSTTIME, format.format(new Date(timeL)));
					} else 
						item.put(Constants.POSTTIME, format.format(new Date()));
					
					itemlink.put(Constants.LINK, iteamUrl);
					itemlink.put(Constants.RAWLINK, iteamUrl);
					itemlink.put(Constants.LINKTYPE, "bbspost");
					item.put(Constants.ITEMNAME, iteamName);
					item.put(Constants.ITEMLINK, itemlink);
					item.put(Constants.REPLY_CNT, Integer.parseInt(replyNum));
					items.add(item);
					tasks.add(itemlink);
				}
				parsedata.put("items", items);
				/**
				 * 判断是否含有下一页 有则给出
				 */
				TagNode form = div.getChildTags()[1];
				if (form.getChildTags().length > 1) {
					TagNode divPages = form.getChildTags()[1];
					TagNode[] divPagesChildTags = divPages.getChildTags();
					int j = divPagesChildTags.length - 1;
					for (; j >= 0; j--) {
						String tagText = divPagesChildTags[j].getText()
								.toString();
						tagText = tagText.replace(" ", "");
						if (tagText.equals("下一页")) {
							Map<String, Object> task = new HashMap<String, Object>();
							String nextPageHref = divPagesChildTags[j]
									.getChildTags()[0]
									.getAttributeByName("href");
							String nextPageUrl = domain + nextPageHref;
							task.put("link", nextPageUrl);
							task.put("rawlink", nextPageUrl);
							task.put("linktype", "bbspostlist");
							tasks.add(task);
							parsedata.put(Constants.NEXTPAGE, task);
						}
					}
				}
			} catch (XPatherException e) {
				e.printStackTrace();
			}
		}

		// System.out.println(parsedata.toString());

	}

	public int matchCstart(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return 0;
	}

}
