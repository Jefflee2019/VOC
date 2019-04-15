package com.bfd.parse.reprocess;

import java.security.MessageDigest;
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
 * 站点名：安卓中文网
 * <p>
 * 主要功能：处理作者发表时间等字段，生成评论任务
 * @author bfd_01
 *
 */
public class NandroidchineseContentRe implements ReProcessor {
	private static final Log LOG = LogFactory
			.getLog(NandroidchineseContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!resultData.isEmpty()) {
			if (resultData.containsKey(Constants.AUTHOR)) {
				String author = resultData.get(Constants.AUTHOR).toString();
				resultData.put(Constants.AUTHOR, author.replace("作者：", ""));
			}
			if (resultData.containsKey(Constants.SOURCE)) {
				String author = resultData.get(Constants.SOURCE).toString();
				resultData.put(Constants.SOURCE, author.replace("来源：", ""));
			}
			if (resultData.containsKey(Constants.POST_TIME)) {
				String author = resultData.get(Constants.POST_TIME).toString();
				resultData.put(Constants.POST_TIME, author.replace("发布时间：", ""));
			}
			
			// 生成评论任务
//			getCommentUrl(unit, result);
		}
		return new ReProcessResult(processcode, processdata);

	}
	
	/**
	 * 生成评论页任务
	 * @param unit
	 * @param result
	 */
	@SuppressWarnings("unchecked")
	private void getCommentUrl(ParseUnit unit, ParseResult result) {
		// 生成评论任务
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = result.getSpiderdata().get("location").toString();
		Map<String, Object> commentTask = new HashMap<String, Object>();
		String urlHead = "http://comment.tgbus.com/comment/?domain=android.tgbus.com&token=";
		String comUrl = urlHead + string2md5(url);
		commentTask.put("link", comUrl);
		commentTask.put("rawlink", comUrl);
		commentTask.put("linktype", "newscomment");
		LOG.info("url:" + url + "taskdata is " + commentTask.get("link")
				+ commentTask.get("rawlink")
				+ commentTask.get("linktype"));
		if (!resultData.isEmpty()) {
			resultData.put("comment_url", comUrl);
			List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get("tasks");
			tasks.add(commentTask);
		}
		// 后处理插件加上iid
		ParseUtils.getIid(unit, result);
	}
	
	private String string2md5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		try {
			byte[] btInput = s.getBytes();
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
