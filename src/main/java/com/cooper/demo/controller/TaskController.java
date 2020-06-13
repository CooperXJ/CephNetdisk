package com.cooper.demo.controller;

import com.cooper.demo.Bean.UploadBigObject;
import com.cooper.demo.Bean.UploadSmallObject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "user/Task")

public class TaskController {

    @Autowired
    HttpServletRequest request;

    @GetMapping("/sum")
    public String toTaskPage()
    {
        return "Task/Task";
    }

    @GetMapping("/upload/small")
    @ResponseBody
    public JSONArray getThreadSmall(HttpSession session)
    {
        UploadSmallObject uploadObject1 = (UploadSmallObject) session.getAttribute("Thread_1_upload");
        UploadSmallObject uploadObject2 = (UploadSmallObject) session.getAttribute("Thread_2_upload");

        if(uploadObject1==null&&uploadObject2==null)
        {
            return null;
        }
        else {
            do {
                JSONArray jsonArray = new JSONArray();
                jsonArray.add(uploadObject1.toJson());
                jsonArray.add(uploadObject2.toJson());
//                System.out.println("send  "+jsonArray.toString());
//                System.out.println("object1 "+uploadObject1.getFileUploadProgress()+"  object2  "+uploadObject2.getFileUploadProgress());
                return jsonArray;
            }while (uploadObject1.getFileUploadProgress().equals("ok")&&uploadObject2.getFileUploadProgress().equals("ok"));
        }
    }

    @GetMapping("/upload/big")
    @ResponseBody
    public JSONArray getThreadBig(HttpSession session)
    {
        UploadBigObject uploadObject3 = (UploadBigObject) session.getAttribute("Thread_3_upload");
        UploadBigObject uploadObject4 = (UploadBigObject) session.getAttribute("Thread_4_upload");

        if(uploadObject3==null&&uploadObject4==null)
        {
            return null;
        }
        else {
            do {
                JSONArray jsonArray = new JSONArray();
                if(uploadObject3!=null)
                {
                    jsonArray.add(uploadObject3.toJson());
                }
                if(uploadObject4!=null)
                    jsonArray.add(uploadObject4.toJson());
                return jsonArray;
            }while (uploadObject3.getFileUploadProgress()==uploadObject3.getFileSize()&&uploadObject4.getFileUploadProgress()==uploadObject4.getFileSize());
        }
    }

}
