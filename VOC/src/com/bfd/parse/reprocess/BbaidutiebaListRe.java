package com.bfd.parse.reprocess;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

public class BbaidutiebaListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(BbaidutiebaListRe.class);
	private static final Pattern IIDPATTER = Pattern.compile("kz=(\\d+)");

	@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map processdata = new HashMap();
		Map resultData = result.getParsedata().getData();
				
		try {
			if ((resultData != null) && !resultData.isEmpty()) {
				List items = (List) resultData.get("items");
				List tasks = (List) resultData.get("tasks");
				long dt = System.currentTimeMillis();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				String nowTime = df.format(Long.valueOf(dt));
				if ((tasks != null) && !tasks.isEmpty()) {
					for (int i = 0; i < tasks.size();) {
						Map taskmap = (Map) tasks.get(i);
						if (taskmap.isEmpty()){
							continue;
						}	
						String sLink = (String)taskmap.get("link");
						Pattern pattern = Pattern.compile("kz=(\\d+)");
						Matcher matcher = pattern.matcher(sLink);
						String sItemNo = "";
						if(matcher.find()){
							sItemNo = matcher.group(1);
						}
						if(sItemNo.equals("")){
							tasks.remove(i);
							continue;
						}
						taskmap.put("link", "http://tieba.baidu.com/p/" + sItemNo);
						taskmap.put("rawlink", "p/" + sItemNo);
						
						++i;
					}

				}

				if ((items != null) && !items.isEmpty()){
					for (int i = 0; i < items.size(); ) {
						Map itemmap = (Map) items.get(i);
						LOG.info("url:" + unit.getUrl() + " get item >>>" + i
								+ ">>>is  " + itemmap);
						if (!(itemmap.isEmpty())) {
							Map itemlink = (Map) itemmap.get("itemlink");
							String info = (String) itemmap.get("reply_cnt");
							String[] infos = info.split(" ");
							int viewStart = infos[0].indexOf("点");
							int replyStart = infos[1].indexOf("回");
							itemmap.put("reply_cnt",
									infos[1].substring(replyStart + 1));
							if (infos[2].contains("-")) {
								String[] date = infos[2].split("-");
								if (date.length == 3){
									itemmap.put("posttime", infos[2]);
								}else{
									itemmap.put("posttime", nowTime);
								}	
							} else {
								itemmap.put("posttime", nowTime);
							}
							String link = (String) itemlink.get("link");
							Matcher iidMatch = IIDPATTER.matcher(link);
							if (iidMatch.find()) {
								String iid = iidMatch.group(1);
								itemlink.put("link",
										"http://tieba.baidu.com/p/" + iid);
								itemlink.put("rawlink", "p/" + iid);
							}else{
								items.remove(i);
								continue;
							}
						}
						++i;
					}
				}
			}
		} catch (Exception e) {
		
		}

		getNextPageUrl(unit, resultData);
		return new ReProcessResult(processcode, processdata);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getNextPageUrl(ParseUnit unit, Map resultData) {
		String url = unit.getUrl();
		String pageData = unit.getPageData();
		Pattern pattern = Pattern.compile("pn=(\\d+)\">下一页");
		Matcher matcher = pattern.matcher(pageData);
		
		if(!matcher.find()){
			return;
		}
		int nextPageNo = Integer.parseInt(matcher.group(1))/10;
				
		String nextPageUrl = "";
		if(url.contains("&pn=")){
			nextPageUrl = url.replaceFirst("&pn=\\d+", "&pn="+nextPageNo+"0");			
		}else{
			nextPageUrl = url + "&pn="+nextPageNo+"0";
		}
		
		Map nextpageTask = new HashMap();
		nextpageTask.put(Constants.LINK, nextPageUrl);
		nextpageTask.put(Constants.RAWLINK, nextPageUrl);
		nextpageTask.put(Constants.LINKTYPE, "bbspostlist");
		if (resultData != null && !resultData.isEmpty()) {
			resultData.put(Constants.NEXTPAGE, nextPageUrl);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			tasks.add(nextpageTask);	
		}
		
	}

	@SuppressWarnings("unused")
	private static boolean isNumber(String str) {
		return str.matches("[0-9]+");
	}
}