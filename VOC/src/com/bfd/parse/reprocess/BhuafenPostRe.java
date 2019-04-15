package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：花粉俱乐部
 * <p>
 * 主要功能：处理帖子总数和今日帖子数、昨日帖子数 note：页面发表时间显示模糊，其准确时间放在标签内部。正则匹配出所有准确时间，再放入相应楼层
 * 
 * @author bfd_01
 *
 */
public class BhuafenPostRe implements ReProcessor {

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		String pageData = unit.getPageData();

		if (resultData != null && !resultData.isEmpty()) {
			String newstime = "";

			if (resultData.containsKey(Constants.NEWSTIME)) {
				newstime = resultData.get(Constants.NEWSTIME).toString().replace("发表于 ", "");
			}
			resultData.put(Constants.NEWSTIME, newstime);

			/**
			 * 2019-03-21新增 清理主贴和回帖内容中的图片信息
			 */
			if (resultData.containsKey(Constants.CONTENTS)) {
				String contents = (String) resultData.get(Constants.CONTENTS);
				contents = contents.replaceAll("本帖最后由.*?编辑", "").replaceAll("%2Fstorage.*?上传", "")
						.replaceAll("\\d*.(jpg|png|bmp|gif|jpeg).*?上传\\S*", "").trim();
				resultData.put(Constants.CONTENTS, contents);
			}

			// 最新回复时间
			if (resultData.containsKey("best_content")) {
				String newsReplys = resultData.get("best_content").toString();
				resultData.put(Constants.NEW_REPLY_TIME, ConstantFunc.convertTime(newsReplys));
				resultData.remove("best_content");
			}

			List listTime = getTime(pageData);
			if (resultData.containsKey(Constants.REPLYS)) {
				List<Map<String, Object>> list = (List<Map<String, Object>>) resultData.get(Constants.REPLYS);
				// 如果帖子抓取的时候，发表时间已经是精准时间，就不能再通过匹配赋值
				// 只有在发表时间是模糊时间时，并且正匹配的时间个数与楼层数相等，把第一个时间给楼主发表时间
				Matcher newstimeMatch = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}\\s*\\d{1,2}:\\d{1,2}(:\\d{1,2})?")
						.matcher(newstime);
				if (!newstimeMatch.find()) {
					// 如果发表时间模糊，那么匹配出来的第一个精准时间必然是发表时间
					resultData.put(Constants.NEWSTIME, listTime.get(0));
				}

				if (list.get(0).containsKey(Constants.REPLYFLOOR)) {
					if ("楼主".equals(list.get(0).get(Constants.REPLYFLOOR))) {
						list.remove(0);
						if (listTime.size() > 0 && listTime.size() > list.size()) {
							listTime.remove(0);
						}
					} else {
						resultData.remove(Constants.CONTENTS);
						resultData.remove(Constants.AUTHOR);
						resultData.remove(Constants.NEWSTIME);
					}
				}
				for (int i = 0; i < list.size(); i++) {
					Map<String, Object> map = (Map<String, Object>) list.get(i);
					// 回复时间
					if (map.containsKey(Constants.REPLYDATE)) {
						String replydate = map.get(Constants.REPLYDATE).toString();
						replydate = replydate.replace("发表于 ", "");
						map.put(Constants.REPLYDATE, ConstantFunc.convertTime(replydate));
					}
					// 正则匹配的时间数与楼层数相等时，说明所有时间都是模糊时间，依次赋值
					if (listTime.size() == list.size()) {
						map.put(Constants.REPLYDATE, listTime.get(i));
					} else if (listTime.size() < list.size() && listTime.size() > 0) {
						// 楼层数为2-9，匹配时间数有4个，说明7，8，9，10楼层的时间是模糊 时间，需要替换
						// list.size=9,listTime.size=4;从i=6开始替换
						if (i >= (list.size() - listTime.size())) {
							map.put(Constants.REPLYDATE, listTime.get(i - (list.size() - listTime.size())));
						}

					}

					// 楼层数
					if (map.containsKey(Constants.REPLYFLOOR)) {
						String replyfloor = map.get(Constants.REPLYFLOOR).toString();
						// 不为空的话，说明有楼层需要从正则中取时间
						// if(emptyMap){
						// if(floorAndTime.containsKey(replyfloor)){
						// //回复时间
						// String reptime = (String)
						// floorAndTime.get(replyfloor);
						// // map.put(Constants.REPLYDATE, reptime);
						// }
						// }
						replyfloor = replyfloor.replace("沙发", "2").replace("板凳", "3").replace("地板", "4")
								.replace("地下室", "5").replace("楼主", "").replace("楼", "").replace("#", "");
						map.put(Constants.REPLYFLOOR, replyfloor);
					}

					// 回复来自
					if (map.containsKey("best_author_name")) {
						String reply_from = map.get("best_author_name").toString();
						map.put(Constants.REPLY_FROM, reply_from.replace("来自：", ""));
						map.remove("best_author_name");
					}
					if (map.containsKey(Constants.REPLYCONTENT)) {
						String replycontent = (String) map.get(Constants.REPLYCONTENT);
						replycontent = replycontent.replaceAll("本帖最后由.*?编辑", "").replaceAll("%2Fstorage.*?上传", "")
								.replaceAll("\\d*.(jpg|png|bmp|gif|jpeg).*?上传\\S*", "").trim();
						map.put(Constants.REPLYCONTENT, replycontent);

						if (map.containsKey("refer_comment")) {
							String refer_comment = (String) map.get("refer_comment");
							map.put(Constants.REPLYCONTENT,
									map.get(Constants.REPLYCONTENT).toString().replace(refer_comment, "")
											.replace("\t", ""));
							Map<String, Object> refer = new HashMap<String, Object>();
							String[] strs = refer_comment.split("\t");
							if (strs.length == 2) {
								refer.put("refer_comment", strs[1]);
								String[] user = strs[0].split("发表于");
								if (user.length == 2) {
									refer.put("refer_username", user[0]);
									refer.put("refer_time", user[1]);
								}
							}
							map.put("refer_comment", refer);
						}

						if (map.get(Constants.REPLYCONTENT).toString().startsWith(": ")) {
							map.put(Constants.REPLYCONTENT, map.get(Constants.REPLYCONTENT).toString().substring(2));
						}
					}
				}
			}
			// 帖子人气
			if (resultData.containsKey(Constants.POPULARITY)) {
				String popularity = resultData.get(Constants.POPULARITY).toString();
				resultData.put(Constants.POPULARITY, popularity.replace("+", ""));
			}
			// 威望
			if (resultData.containsKey(Constants.PRESTIGE)) {
				String prestige = resultData.get(Constants.PRESTIGE).toString();
				resultData.put(Constants.PRESTIGE, prestige.replace("+", ""));
			}
			// 最后编辑人/最后编辑时间
			if (resultData.containsKey("reply_fans_cnt")) {
				String[] last = resultData.get("reply_fans_cnt").toString().split(" ");
				if (last.length > 4) {
					resultData.put(Constants.LAST_EDITOR, last[1]);
					resultData.put(Constants.LAST_EDIT_TIME, ConstantFunc.convertTime(last[3] + last[4]));
				}
				resultData.remove("reply_fans_cnt");
			}

		}
		return new ReProcessResult(processcode, processdata);
	}

	// 正则匹配出精确时间
	public List<String> getTime(String pageData) {
		List<String> listTime = new ArrayList<String>();
		Pattern patten = Pattern
				.compile("<em\\s*id=\"authorposton\\d+\">发表于 <span title=\"([\\d-\\s:]+)\">(\\S*)</span>");
		Matcher matcher = patten.matcher(pageData);
		while (matcher.find()) {
			String postTime = matcher.group(1);
			listTime.add(postTime);
		}
		return listTime;
	}

}
