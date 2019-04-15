package com.bfd.parse.test.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.reprocess.BkoolshareContentRe;
import com.bfd.parse.reprocess.BkoolshareListRe;
import com.bfd.parse.reprocess.N20ukContentRe;
import com.bfd.parse.util.JsonUtil;

public class N20ukContentReTest {
	public static void main(String[] args) throws Exception {
		testDemo();
	}

	public static void testDemo() throws Exception {
		// 要抓取链接
		String url1 = "http://www.20uk.com/news/582.html";
		// 动态数据
		 List<String> ajaxlist = new ArrayList<String>();
		 ajaxlist.add("https://comment.bilibili.com/11011073.xml");
		// 生成解析使用的bean
		Map<String, Object> map = CreateParseUnit.createMap("test", "test", 100, 100, url1, "utf-8", null, ajaxlist);
		// 生成解析需要结构
		ParseUnit unit = ParseUnit.fromMap(map, System.currentTimeMillis());
		// 模板
		String tmplStr = FileUtils.readFileToString(new File("tmpl/N20ukContentRe.txt"));
		// 解析输出字段
		String outputFiled = "";
		ParserFace face = new ParserFace("test");
		// 执行解析流程
		// 参数分别是 预处理插件,json插件,后处理插件,模板,ParseFace,输出字段
		ParsePlugin testPlugin = new ParsePlugin(null, null, new N20ukContentRe(), tmplStr, face, outputFiled);
		// ParsePlugin testPlugin = new ParsePlugin(null, null, null, tmplStr,
		// face, outputFiled);
		// 输出结果
		System.out.println(JsonUtil.toJSONString(testPlugin.parse(unit)));
	}
}
