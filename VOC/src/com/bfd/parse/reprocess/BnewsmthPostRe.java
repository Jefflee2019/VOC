package com.bfd.parse.reprocess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

public class BnewsmthPostRe implements ReProcessor{
	private static final Log log = LogFactory.getLog(BnewsmthPostRe.class);

	@SuppressWarnings("unchecked")
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!resultData.isEmpty()) {
			String url = unit.getUrl();
			int curPageNum = getPageNo(url); // 当前页数
			if (1 == curPageNum) { // 在第一页，去除楼主信息
				List<String> list = (List<String>) resultData.get("replys");
				list.remove(0);
			} else { // 其他页时去除作者信息
				if (resultData.containsKey("author")) {
					resultData.remove("author");
				}
				if (resultData.containsKey("authorname")) {
					resultData.remove("authorname");
				}
				if (resultData.containsKey("contents")) {
					resultData.remove("contents");
				}
				if (resultData.containsKey("author_level")) {
					resultData.remove("author_level");
				}
			}
			SimpleDateFormat sdfus = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Pattern pTime = Pattern.compile("([Wed|Thu|Fri|Sat|Sun|Mon|Tue]{3} [A-Wa-y]{3} \\d{1,2} \\d{2}:\\d{2}:\\d{2} \\d{4})");
			if (resultData.containsKey("replys")) {
				List<Object> replys = (List<Object>) resultData.get("replys");
				for (Object obj : replys) {
					if ((obj instanceof Map)) {
						Map<String, Object> reply = (Map<String, Object>) obj;
						if (reply.containsKey("replyfloor")) { // 楼层
							String replyfloor = reply.get("replyfloor").toString();
							reply.put("replyfloor", replyfloor.replace("第", "").replace("楼", ""));
						}
						if (reply.containsKey("replycontent")) { // 解析回复时间
							Matcher matcher = pTime.matcher(reply.get("replycontent").toString());
							if(matcher.find()) {
								try {
									reply.put("replydate", sdf.format(sdfus.parse(matcher.group(1))));
								} catch (ParseException e) {
									log.error("replydate : " + matcher.group(1), e);
								}
							}
							//回复人截取  发信人: Richbunny (Richbunny),
							String replycontent = reply.get(Constants.REPLYCONTENT).toString();
							String replyusername = this.getCresult(replycontent, "发信人:(.*), 信区").trim();
							replyusername = this.getCresult(replyusername, "(.*)\\(").trim();
							System.err.println(replyusername);
							reply.put("replyusername", replyusername);
							//回复内容截取
							replycontent = this.getCresult(replycontent, "站内\t(.*)\t※ 来源").trim();
							reply.put("replycontent", replycontent);
						}
					}
				}
			}
			if (resultData.containsKey("contents")) {
				Matcher matcher = pTime.matcher(resultData.get("contents").toString());
				if(matcher.find()) {
					try { // 处理楼主帖子发表时间
						resultData.put("newstime", sdf.format(sdfus.parse(matcher.group(1))));
					} catch (ParseException e) {
						log.error("newstime : " + matcher.group(1), e);
					}
				}
				String contents = (String) resultData.get("contents");
				contents = this.getCresult(contents, "站内\t(.*)\t※ 来源").trim();
				resultData.put("contents", contents);
			}
			
			int replyCount = Integer.parseInt(resultData.get("replycount").toString()); // replycount为关键项
			if(replyCount > 10) { // 帖子回复一页显示10条
				int pageTotal = (replyCount + 9)/10; // 总页数
				if(curPageNum > 0 && curPageNum < pageTotal) { // 当前页数小于总页数
					Map<String, Object> nextpageTask = new HashMap<>();
					if (url.contains("p=")) {
						url = url.replace("p=" + curPageNum, "p=" + (++curPageNum)); // 得到下一页
					} else if (url.contains("?")) {
						url = url + "&p=" + (++curPageNum); // 得到下一页
					} else {
						url = url + "?p=" + (++curPageNum); // 得到下一页
					}
					nextpageTask.put("link", url);
					nextpageTask.put("rawlink", url);
					nextpageTask.put("linktype", "bbspost");
					resultData.put("nextpage", url);
					List<Object> tasks = (List<Object>) resultData.get("tasks");
					tasks.add(nextpageTask);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
	
	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(1);
		}
		return str;
	}

	private int getPageNo(String url) {
//		if (url.contains("www.newsmth.net")) {}
		Matcher match = Pattern.compile("p=(\\d+)").matcher(url);
		if (match.find()) {
			return Integer.parseInt(match.group(1));
		}
		return 1;
	}
}