package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:265G苹果网
 * @function 处理编辑字段
 * 
 * @author bfd_02
 * 评论链接与内容页的关系，暂时未生成评论页
 * http://www.265g.com/news/gamenews/753409.html
 * http://changyan.sohu.com/api/2/topic/comments?callback=jQuery&client_id=cyqP5L8D1&page_size=10&topic_id=1104789777&page_no=1
 */

public class N265gContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(N265gContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未找到解析数据");
			return null;
		}

		/**
		 * @param editor
		 * @function 清洗字段
		 *  
		 */

		if (resultData.containsKey(Constants.EDITOR)) {
			String editor = resultData.get(Constants.EDITOR).toString();
			if (editor.contains("编辑：")) {
				int index = editor.indexOf("编辑：");
				editor = editor.substring(index+3);
			}
			resultData.put(Constants.EDITOR, editor);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}