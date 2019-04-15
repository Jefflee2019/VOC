package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;


public class NeeworldContentRe
    implements ReProcessor
{

    public NeeworldContentRe()
    {
    }

    public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace)
    {
        Map resultData = new HashMap();
        Map processdata = new HashMap();
        resultData = result.getParsedata().getData();
        if(resultData != null)
        {
            if(resultData.containsKey("post_time"))
            {
                String sPostTime = resultData.get("post_time").toString();
                Pattern pattern = Pattern.compile("\\d{4}.\\d{1,2}.\\d{1,2}.\\d{1,2}:\\d{1,2}:\\d{1,2}");
                Matcher matcher = pattern.matcher(sPostTime);
                if(matcher.find())
                {
                    sPostTime = matcher.group();
                    resultData.put("post_time", sPostTime);
                }
            }
            if(resultData.containsKey("source"))
            {
                String sSource = resultData.get("source").toString();
                sSource = sSource.replace("\uFF1A", ":");
                Pattern pattern = Pattern.compile("\u6765\u6E90:\\s*(\\S+)");
                Matcher matcher = pattern.matcher(sSource);
                if(matcher.find())
                {
                    sSource = matcher.group(1);
                    resultData.put("source", sSource);
                }
            }
            getCommentUrl(unit, result);
        }
        return new ReProcessResult(0, processdata);
    }

    private void getCommentUrl(ParseUnit unit, ParseResult result)
    {
        Map resultData = result.getParsedata().getData();
        Map commentTask = new HashMap();
        String pageData = unit.getPageData();
        Pattern pattern = Pattern.compile("<iframe src=\"(\\S+?)\".*?id=\"comment_iframe\"");
        Matcher matcher = pattern.matcher(pageData);
        if(!matcher.find())
            return;
        String sCommUrl = "http://www.eeworld.com.cn" + matcher.group(1);
        commentTask.put("link", sCommUrl);
        commentTask.put("rawlink", sCommUrl);
        commentTask.put("linktype", "newscomment");
        if(resultData != null && !resultData.isEmpty())
        {
            resultData.put("comment_url", sCommUrl);
            List tasks = (List)resultData.get("tasks");
            tasks.add(commentTask);
        }
        ParseUtils.getIid(unit, result);
    }

}

