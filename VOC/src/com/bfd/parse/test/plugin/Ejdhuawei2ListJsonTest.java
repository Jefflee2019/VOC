package com.bfd.parse.test.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.json.Ejdhuawei2ListJson;
import com.bfd.parse.util.JsonUtil;

public class Ejdhuawei2ListJsonTest {
	public static void main(String[] args) throws Exception {
		testDemo();
	}

	public static void testDemo() throws Exception {
		// 要抓取链接
		String url1 = "http://172.24.5.78:8080/Ejdhuawei2.html";
//		String url1 = "http://cn.club.vmall.com/thread-10090650-1-1.html";
		// 动态数据
		 List<String> ajaxlist = new ArrayList<String>();
		 ajaxlist.add("http://172.24.5.78:8080/Ejdhuawei2.html");
		// 生成解析使用的bean
		Map<String, Object> map = CreateParseUnit.createMap("test", "test", 100, 100, url1, "utf8", null, ajaxlist);
		// 生成解析需要结构
		ParseUnit unit = ParseUnit.fromMap(map, System.currentTimeMillis());
		// 模板
//		String tmplStr = FileUtils.readFileToString(new File("tmpl/Ejd_list_1.txt"));
		// 解析输出字段
		String outputFiled = "";
		ParserFace face = new ParserFace("test");
		// 执行解析流程
		// 参数分别是 预处理插件,json插件,后处理插件,模板,ParseFace,输出字段
		ParsePlugin testPlugin = new ParsePlugin(null, new Ejdhuawei2ListJson(), null , null, face, outputFiled);
		// ParsePlugin testPlugin = new ParsePlugin(null, null, null, tmplStr,
		// face, outputFiled);
		// 输出结果
		System.out.println(JsonUtil.toJSONString(testPlugin.parse(unit)));
	}
}