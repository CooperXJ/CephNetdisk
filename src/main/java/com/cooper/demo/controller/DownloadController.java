package com.cooper.demo.controller;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Iterator;

@Controller
@RequestMapping(value = "user/Task")

public class DownloadController {

    //得到下载的列表
    @GetMapping("download")
    @ResponseBody
    public JSONObject getDownloadList(HttpSession session){
        Object object = session.getAttribute("xmlName");
        Object object1 = session.getAttribute("downloadSize");
        Object object2 = session.getAttribute("downloadName");

        if(object==null)
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("progress",Float.valueOf(-1));
            jsonObject.put("name",(String)object2);

            return jsonObject;
        }
        else {
            int res = 0;

            //读取文件
            Document document = null;
            try {
                document = new SAXReader().read(new File((String)(object)));
            } catch (DocumentException e) {
                return null;
            }

            Element root = document.getRootElement();

            //合并下载的文件片段
            Iterator<?> ruleNodes = root.elementIterator("Part");//获取xml文件下所有标签为part的节点信息
            while (ruleNodes.hasNext()) {
                Element ruleElement = (Element) ruleNodes.next();
                //当part标签下的is_ok标签为true的时候则证明下载完成
                if ((ruleElement.element("is_ok").getStringValue().equals("true"))) {
                    res++;
                }
            }

            if((res)>=(int)object1)
            {
                session.removeAttribute("xmlName");
                session.removeAttribute("downloadSize");
                session.removeAttribute("downloadName");
//                File file = new File((String)object);
//                file.delete();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("progress",Float.valueOf(-2));
                jsonObject.put("name",(String)object2);

                return jsonObject;
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("progress",res/(float)((int)object1));
            jsonObject.put("name",(String)object2);
            return jsonObject;
        }
    }
}
