package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点名：太平洋电脑网
 * <p>
 * 主要功能：处理cate，楼层数,回帖引用关系
 * @author bfd_01
 *
 */
public class BpconlinePostRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(BpconlinePostRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData != null && !resultData.isEmpty()) {
			// 删除1楼信息
			if (resultData.containsKey(Constants.CONTENT)) {
				List<String> list = (List<String>)resultData.get("replys");
				list.remove(0);
			}

			// 处理楼层数
			if (resultData.containsKey(Constants.REPLYS)) {
				List<Object> replys = (List<Object>) resultData.get("replys");
				for (Object obj : replys) {
					if (obj instanceof Map) {
						Map<String, Object> reply = (Map<String, Object>) obj;
						if (reply.containsKey("replyfloor")) {
							String replyfloor = reply.get("replyfloor")
									.toString();
							replyfloor = replyfloor.replace("楼主", "1")
									.replace("沙发", "2").replace("板凳", "3")
									.replace("地板", "4").replace("地下室", "5")
									.replace("楼", "");
							reply.put("replyfloor", replyfloor);
						}

						// 处理引用关系
						if (reply.containsKey(Constants.REFER_COMMENTS)) {
							Map<String,Object> refer = new HashMap<String,Object>();
							String referComments = reply.get(
									Constants.REFER_COMMENTS).toString();
							Matcher mTime = Pattern.compile(
									"\\d+-\\d+-\\d+\\s*\\d+:\\d+").matcher(
									referComments);
							Matcher mFloor = Pattern.compile("(\\d+)楼")
									.matcher(referComments);
							Matcher mContent = Pattern.compile("\\t(.*)")
									.matcher(referComments);
							Matcher mName = Pattern.compile("(.*)\\s发表于")
									.matcher(referComments);
							String referCommTime = null;
							String referReplyfloor = null;
							String referCommusername = null;
							String referCommContent = null;
							while (mTime.find()) {
								referCommTime = mTime.group();
							}
							while (mFloor.find()) {
								referReplyfloor = mFloor.group(1);
							}
							while (mContent.find()) {
								referCommContent = mContent.group(1);
							}
							while (mName.find()) {
								referCommusername = mName.group(1);
							}
							
							refer.put(Constants.REFER_COMM_TIME,
									referCommTime);
							refer.put(Constants.REFER_REPLYFLOOR,
									referReplyfloor);
							refer.put(Constants.REFER_COMM_USERNAME,
									referCommusername);
							refer.put(Constants.REFER_COMM_CONTENT,
									referCommContent);

							reply.put(
									Constants.REPLYCONTENT,
									reply.get(Constants.REPLYCONTENT)
											.toString()
											.replace(
													reply.get(
															Constants.REFER_COMMENTS)
															.toString(), ""));
							reply.put(Constants.REFER_COMMENTS, refer);
						}
					}
				}
			}

			// cate
			if (resultData.containsKey(Constants.CATE)) {
				List<String> cate = (List<String>) resultData.get(Constants.CATE);
				cate.remove(0);
				cate.remove(cate.size() - 1);
			}
		
			String data = unit.getPageData();
			if (data.contains("下一页")) {
				String url = unit.getUrl();
				Pattern p = Pattern.compile("(\\d+)_(\\d+).html");
				Matcher m = p.matcher(url);
				int page = 0;
				String iid = null;
				while (m.find()) {
					page = Integer.valueOf(m.group(2));
					iid = m.group(1);
				}
				String nextpage = null;
				if (page == 0) {
					nextpage = url.split(".html")[0] + "_2.html";
				} else {
					nextpage = url.split(iid)[0] + iid + "_" + (page +1) + ".html";
				}
				if (nextpage != null) {

					// 处理下一页链接
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put("link", nextpage);
					nextpageTask.put("rawlink", nextpage);
					nextpageTask.put("linktype", "bbspost");
					if (!resultData.isEmpty()) {
						resultData.put("nextpage", nextpage);
						List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
								.get("tasks");
						tasks.add(nextpageTask);
					}
					// 后处理插件加上iid
					ParseUtils.getIid(unit, result);
				}
			}
			
		}
		return new ReProcessResult(processcode, processdata);
	}
}
