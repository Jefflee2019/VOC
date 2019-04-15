package com.bfd.parse.test.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.json.N91CommentJson;
import com.bfd.parse.util.JsonUtil;

public class N91CommentJsonTest {
	public static void main(String[] args) throws Exception {
		testDemo();
	}

	public static void testDemo() throws Exception {
		// 要抓取链接
		String url = "http://changyan.sohu.com/api/3/topic/liteload?callback=jQuery&client_id=cyqUBq1L1&topic_url=http://news.91.com/android/s58189779e18d.html&page_size=10";
		// 动态数据
		List<String> ajaxlist = new ArrayList<String>();
		ajaxlist.add("http://changyan.sohu.com/api/3/topic/liteload?callback=jQuery&client_id=cyqUBq1L1&topic_url=http://news.91.com/android/s58189779e18d.html&page_size=10");
		// 生成解析使用的bean
		Map<String, Object> map = CreateParseUnit.createMap("test", "test", 100, 100, url, "UTF-8", null, ajaxlist);
		// 生成解析需要结构
		ParseUnit unit = ParseUnit.fromMap(map, System.currentTimeMillis());
		// 模板
		String tmplStr = FileUtils.readFileToString(new File("tmpl/N91_comment.txt"));
		// 解析输出字段
		String outputFiled = "";
		ParserFace face = new ParserFace("test");
		// 执行解析流程
		// 参数分别是 预处理插件,json插件,后处理插件,模板,ParseFace,输出字段
		ParsePlugin testPlugin = new ParsePlugin(null, new N91CommentJson(), null, tmplStr, face,
				outputFiled);
//		ParsePlugin testPlugin = new ParsePlugin(null, null, null, tmplStr, face, outputFiled);
		// 输出结果
		System.out.println(JsonUtil.toJSONString(testPlugin.parse(unit)));
	}
}
