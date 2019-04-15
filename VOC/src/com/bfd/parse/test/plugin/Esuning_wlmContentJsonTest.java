package com.bfd.parse.test.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.json.Esuning_wlmContentJson;
import com.bfd.parse.util.JsonUtil;

public class Esuning_wlmContentJsonTest {
	public static void main(String[] args) throws Exception {
		testDemo();
	}

	public static void testDemo() throws Exception {
		// 要抓取链接
		String url1 = "https://pas.suning.com/nspcsale_1_000000000601080074_000000000601080074_0070141301_20_755_7550101_343507_1000267_9264_12113_Z001___R9001387_5.0_1___0004618T0_.html?callback=pcData&_=1532420227756";
//		 动态数据
		 List<String> ajaxlist = new ArrayList<String>();
		 ajaxlist.add("https://pas.suning.com/nspcsale_1_000000000601080074_000000000601080074_0070141301_20_755_7550101_343507_1000267_9264_12113_Z001___R9001387_5.0_1___0004618T0_.html?callback=pcData&_=1532420227756");
		// 生成解析使用的bean
		Map<String, Object> map = CreateParseUnit.createMap("test", "test", 100, 100, url1, "gbk", null, ajaxlist);
		// 生成解析需要结构
		ParseUnit unit = ParseUnit.fromMap(map, System.currentTimeMillis());
		// 模板
//		String tmplStr = FileUtils.readFileToString(new File("tmpl/Ejd_content_3.txt"));
		// 解析输出字段
		String outputFiled = "";
		ParserFace face = new ParserFace("test");
		// 执行解析流程
		// 参数分别是 预处理插件,json插件,后处理插件,模板,ParseFace,输出字段
		ParsePlugin testPlugin = new ParsePlugin(null, new Esuning_wlmContentJson(), null, null, face, outputFiled);
		// ParsePlugin testPlugin = new ParsePlugin(null, null, null, tmplStr,
		// face, outputFiled);
		// 输出结果
		System.out.println(JsonUtil.toJSONString(testPlugin.parse(unit)));
	}
}
