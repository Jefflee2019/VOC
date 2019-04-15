package com.bfd.parse.test.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.json.NqqvideoContentJson;
import com.bfd.parse.util.JsonUtil;

public class NqqvideoContentJsonTest {
	public static void main(String[] args) throws Exception {
		testDemo();
	}

	public static void testDemo() throws Exception {
		// 要抓取链接
		String url = "https://ncgi.video.qq.com/fcgi-bin/video_comment_id?otype=json&callback=jQuery1910310119910780409_1505814063163&op=3&vid=m0550qvc20qhttps://v.qq.com/x/page/m0550qvc20q.html";
		// 动态数据
		List<String> ajaxlist = new ArrayList<String>();
		ajaxlist.add("https://ncgi.video.qq.com/fcgi-bin/video_comment_id?otype=json&callback=jQuery1910310119910780409_1505814063163&op=3&vid=m0550qvc20q");
		// 生成解析使用的bean
		Map<String, Object> map = CreateParseUnit.createMap("test", "test", 100, 100, url, "UTF-8", null, ajaxlist);
		// 生成解析需要结构
		ParseUnit unit = ParseUnit.fromMap(map, System.currentTimeMillis());
		// 模板
		String tmplStr = FileUtils.readFileToString(new File("tmpl/Nqqvideo_content.txt"));
		// 解析输出字段
		String outputFiled = "";
		ParserFace face = new ParserFace("test");
		// 执行解析流程
		// 参数分别是 预处理插件,json插件,后处理插件,模板,ParseFace,输出字段
		ParsePlugin testPlugin = new ParsePlugin(null, new NqqvideoContentJson(), null, tmplStr, face,
				outputFiled);
//		ParsePlugin testPlugin = new ParsePlugin(null, null, null, tmplStr, face, outputFiled);
		// 输出结果
		System.out.println(JsonUtil.toJSONString(testPlugin.parse(unit)));
	}
}
