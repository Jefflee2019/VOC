package com.bfd.parse.test.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.json.Eyhd_wlmContentJson;
import com.bfd.parse.util.JsonUtil;

public class Eyhd_wlmContentJsonTest {
	public static void main(String[] args) throws Exception {
		testDemo();
	}

	public static void testDemo() throws Exception {
		// 要抓取链接
//		String url1 = "http://c0.3.cn/stock?extraParam=%7B%22originid%22:%221%22%7D&ch=1&skuId=4801907&area=22_1930_4773_0&cat=12218%2C12221%2C13556&venderId=1000077502";
		String url1 = "http://item.yhd.com/squ/item/ajaxGetVenderInfo.do?params.channelId=0&params.venderId=663284&params.popType=1";
		//动态数据
		 List<String> ajaxlist = new ArrayList<String>();
		 ajaxlist.add("http://item.yhd.com/squ/item/ajaxGetVenderInfo.do?params.channelId=0&params.venderId=663284&params.popType=1");
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
		ParsePlugin testPlugin = new ParsePlugin(null, new Eyhd_wlmContentJson(), null, null, face, outputFiled);
		// ParsePlugin testPlugin = new ParsePlugin(null, null, null, tmplStr,
		// face, outputFiled);
		// 输出结果
		System.out.println(JsonUtil.toJSONString(testPlugin.parse(unit)));
	}
}
