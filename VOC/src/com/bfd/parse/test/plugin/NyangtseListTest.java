package com.bfd.parse.test.plugin;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.reprocess.NyangtseListRe;
import com.bfd.parse.util.JsonUtil;

public class NyangtseListTest {
	public static void main(String[] args) throws Exception {
		testDemo();
	}

	public static void testDemo() throws Exception {
		// 要抓取链接
		String url = "https://www.so.com/s?q=%E5%8D%8E%E4%B8%BA+site%3Ayangtse.com&src=srp&psid=4eaa29b19ef7ed289d1bbbad6c8839f4";
		// 动态数据
//		List<String> ajaxlist = new ArrayList<String>();
//		ajaxlist.add(null);
		// 生成解析使用的bean
		Map<String, Object> map = CreateParseUnit.createMap("test", "test", 100, 100, url, "gbk", null, null);
		// 生成解析需要结构
		ParseUnit unit = ParseUnit.fromMap(map, System.currentTimeMillis());
		// 模板
		String tmplStr = FileUtils.readFileToString(new File("tmpl/Nyangtse_list.txt"));
		// 解析输出字段
		String outputFiled = "";
		ParserFace face = new ParserFace("test");
		// 执行解析流程
		// 参数分别是 预处理插件,json插件,后处理插件,模板,ParseFace,输出字段
		ParsePlugin testPlugin = new ParsePlugin(null, null, new NyangtseListRe(), tmplStr, face,
				outputFiled);
//		ParsePlugin testPlugin = new ParsePlugin(null, null, null, tmplStr, face, outputFiled);
		// 输出结果
		System.out.println(JsonUtil.toJSONString(testPlugin.parse(unit)));
	}
}
