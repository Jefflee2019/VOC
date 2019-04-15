package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site 中国日报网(Nchinadaily)
 * @function 新闻内容页后处理插件
 */
public class NchinadailyContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NchinadailyContentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
//			if (resultData.containsKey(Constants.POST_TIME)) {
//				resultData.put(Constants.POST_TIME, ConstantFunc.getDate((String)resultData.get(Constants.POST_TIME)));
//			}
			if (resultData.containsKey(Constants.AUTHOR)) { // 作者字段
				String source = (String)resultData.get(Constants.AUTHOR);
				int indx = source.indexOf("作者：");
				if(indx >= 0) {
					String[] s = source.substring(indx+3).split(" ");
					resultData.put(Constants.AUTHOR, s[0]);
				} else if(source.length() > 15) { // 如果长度大于15个字，认为不是合法的名字
					resultData.put(Constants.AUTHOR, "");
				}
//				resultData.put(Constants.AUTHOR, ConstantFunc.getAuthor((String)resultData.get(Constants.AUTHOR)));
			}
			if (resultData.containsKey(Constants.SOURCE)) { // 来源字段
				resultData.put(Constants.SOURCE, ConstantFunc.getSource((String)resultData.get(Constants.SOURCE)));
			}
			
			String editor = (String) resultData.get(Constants.EDITOR);
			if(editor != null) {
				resultData.put(Constants.EDITOR, ConstantFunc.getEditor(editor));
			} else {
				editor = (String)resultData.get(Constants.CONTENT); // 编辑字段
				if(editor != null) {
					int i = editor.lastIndexOf("编辑：");
					if(i >= 0) { // 文章底部有编辑
						String[] s = editor.substring(i+3).split(" ");
						resultData.put(Constants.EDITOR, s[0]);
					}
				}
			}
		} else {
			LOG.warn("resultData=" + resultData);
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}