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
 * @site:和讯网-论坛
 * @function 论坛帖子页后处理插件
 * 
 * @author bfd_02
 *
 */

public class BhexunPostRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BhexunPostRe.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}

		if (resultData.containsKey(Constants.REPLYS)) {
			List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData
					.get(Constants.REPLYS);
			if (replys != null && !replys.isEmpty()) {
				for (int i = 0; i < replys.size(); i++) {
					Map<String, Object> replyData = replys.get(i);
					/**
					 * @param replyfloor楼层数 
					 * @function 格式化，去掉非数字符号
					 * 		eg:"replyfloor": "1楼：中是净个"
					 */
					if (replyData.containsKey(Constants.REPLYFLOOR)) {
						String oldReplyfloor = (String) replyData
								.get(Constants.REPLYFLOOR);
						Pattern pattern = Pattern.compile("(\\d+)楼");
						Matcher match = pattern.matcher(oldReplyfloor);
						if (match.find()) {
							String newReplyfloor = match.group(1);
							replyData.put(Constants.REPLYFLOOR, newReplyfloor);
						}
					}

					/**
					 * @param replydate回复时间 
					 * @function 格式化，去掉非时间部分
					 *     eg："replydate": "1楼：中是净个 发表于：[2015-11-05 17:49:15]"
					 */
					if (replyData.containsKey(Constants.REPLYDATE)) {
						String oldReplydate = (String) replyData.get(Constants.REPLYDATE);
							Matcher match = Pattern.compile("\\d+-\\d+-\\d+\\s*\\d+:\\d+:\\d+").matcher(oldReplydate);
							if(match.find()) {
							String newReplydate = match.group();
							replyData.put(Constants.REPLYDATE, newReplydate);
						}
					}

					/**
					 * @param referComment 回复内容
					 * @param referComments 引用的回复
					 * @param referComUsername 所引用回复人昵称
					 * @param referComContent所引用的回复内容
					 * @param referComTime所引用的回复时间
					 * 
					 * @function 格式化并添加字段
					 *      eg:"replyContent": " 把酒看花 在 2015-11-06 08:04 写道： 这样的高配，才更吸引我 在哪里看高配", 
                            eg:"referComment": "把酒看花 在 2015-11-06 08:04 写道： 这样的高配，才更吸引我", 
					 */

					if (replyData.containsKey("refer_comment")) {
						//存放引用数据的Map
						Map referComments = new HashMap<String,Object>();
						String replyContent = replyData.get(Constants.REPLYCONTENT).toString();
						String referComment = replyData.get("refer_comment").toString();
						String regex ="(\\S+)\\s*在\\s*([\\d\\-\\s:]+)\\s*写道：\\s*(\\S+)";
						Matcher match = Pattern.compile(regex).matcher(referComment);
						if(match.find()) {
							String referComUsername = match.group(1).trim();
							String referComTime = match.group(2).trim();
							String referComContent = match.group(3).trim();
							referComments.put(Constants.REFER_COMM_USERNAME, referComUsername);
							referComments.put(Constants.REFER_COMM_TIME, referComTime);
							referComments.put(Constants.REFER_COMM_CONTENT, referComContent);
							replyData.put(Constants.REFER_COMMENTS, referComments);
						}
						//去掉replycontent索引0位置的" "
						replyContent = replyContent.trim();
						//去掉包含的refer_comment部分
						replyContent = replyContent.replace(referComment, "").trim();
						replyData.remove("refer_comment");
						replyData.put(Constants.REPLYCONTENT, replyContent);
					}
				}
				resultData.put(Constants.REPLYS, replys);
			}
		}

		/**
		 * @param newstime发表时间
		 * @function 格式化
		 *     eg："newstime": "发表于： [2015-11-05 16:45:06]"
		 */
		if (resultData.containsKey(Constants.NEWSTIME)) {
			String oldNewstime = (String) resultData.get(Constants.NEWSTIME);
			if (!oldNewstime.equals("")) {
				Matcher match = Pattern.compile("\\d+-\\d+-\\d+\\s*\\d+:\\d+:\\d+").matcher(oldNewstime);
				if(match.find()) {
					String newstime = match.group();
					resultData.put(Constants.NEWSTIME, newstime);
				}
			}
		}
		
		if (resultData.containsKey(Constants.AUTHOR)) {
			List<Map<String,String>> authorList = (List<Map<String,String>>) resultData.get(Constants.AUTHOR);
			if(authorList != null && authorList.size() > 1){
				Map<String,String> author = authorList.get(0);
				for (int i = 1; i < authorList.size();) {
					author.putAll(authorList.get(i));
					authorList.remove(i);
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
