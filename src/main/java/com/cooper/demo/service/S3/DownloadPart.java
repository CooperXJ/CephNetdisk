package com.cooper.demo.service.S3;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.amazonaws.services.s3.model.Bucket;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DownloadPart extends HttpServlet{

    private S3ServiceImpl s3Service;
    //private static String bucket_name = "/test-597eeea97aed46409820396cb2239de5/";
    //private static String object_name = "SSR.dmg";
    private static String GET_URL = null;
    //private static String path = "/Users/xuejin/Desktop/S3/download/";
    //private static String xml_path = "/Users/xuejin/Desktop/S3/download/" + object_name + "_map.xml";
    private Authentication auth;

    private List<Integer> flag = new ArrayList<>();

    private final String lock1 = "lock1";
    private final String lock2 = "lock2";
    private final String lock3 = "lock3";
    private final String lock4 = "lock4";
    private static int flag_user = 1;

    static int len = 1 * 1024 * 1024;//每一个分段下载的长度

    public static int getFlag_user() {
        return flag_user;
    }

    public static void setFlag_user(int flag_user) {
        DownloadPart.flag_user = flag_user;
    }

    public DownloadPart(String ACCESS_KEY, String SECRET_KEY)
    {
        //初始化flag数组
        flag.add(1);
        flag.add(1);

        auth = new Authentication(ACCESS_KEY,SECRET_KEY);
    }

    public int getSize(String ACCESS_KEY, String SECRET_KEY, String bucket_name, String object_name)
    {
        bucket_name = "/"+bucket_name+"/";
        String ti = auth.timestamp();//获得时间戳
        String toSign = auth.HEAD_METHOD + "\n" + "\n" + "\n" + "\n" + "x-amz-date:" + ti + "\n" +bucket_name + object_name;
        String sign = auth.calculateRFC2104HMAC(toSign, SECRET_KEY);//私钥加密
        String id = "AWS" + " " + ACCESS_KEY + ":" + sign;//公钥
        GET_URL = auth.endpoint + bucket_name + object_name;//生成请求的url
        // 获取到object的size
        auth.putMethod(GET_URL, auth.HEAD_METHOD, id, ti);
        return auth.object_size;
    }

    public void download(String ACCESS_KEY, String SECRET_KEY, String bucket_name,String object_name,String downloadPath)
    {
        bucket_name = "/"+bucket_name+"/";
        String xml_path = downloadPath + object_name + "_map.xml";
        String path = downloadPath;
        String ti = auth.timestamp();//获得时间戳
        String toSign = null;
        int i = 0;
        int size = 0;
        //object_name = auth.to_utf8(object_name);//为了防止有中文字符出现，所以将其编码
        toSign = auth.HEAD_METHOD + "\n" + "\n" + "\n" + "\n" + "x-amz-date:" + ti + "\n"+bucket_name + object_name;

        //String sign = auth.calculateRFC2104HMAC(toSign, auth.awsSecretKey);//私钥加密
        //String id = "AWS" + " " + auth.awsAccessKeyId + ":" + sign;//公钥

        String sign = auth.calculateRFC2104HMAC(toSign, SECRET_KEY);//私钥加密
        String id = "AWS" + " " + ACCESS_KEY + ":" + sign;//公钥

        GET_URL = auth.endpoint + bucket_name + object_name;//生成请求的url
        // 获取到object的size
        auth.putMethod(GET_URL, auth.HEAD_METHOD, id, ti);
        size = auth.object_size;

        //生成xml文件并初始化  根据文件大小分成相应的标签块
        File file = new File(xml_path);
        Document document = null;
        Element root = null;
        if (!file.exists()) {
            document = DocumentHelper.createDocument();
            root = document.addElement("download");//设置根标签

            while (len <= size) {
                auth.createXML(Integer.toString(i * len), Integer.toString(len), "false", root);//将下载段设置为false表示还没有开始下载 初始化
                size = size - len;
                i++;
            }
            if (size > 0) {
                auth.createXML(Integer.toString(i * len), Integer.toString(size), "false", root);
            }
            auth.write_xml_to_file(xml_path, document);
        }

        //session赋值
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        session.setAttribute("xmlName",xml_path);
        session.setAttribute("downloadSize",i);
        session.setAttribute("downloadName",object_name);

        // open file 得到对于的xml中的信息，主要是分段下载哪些内容
        try {
            document = new SAXReader().read(new File(xml_path));
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String range = null;//这里range字段决定了下载目标文件的哪一部分
        //Queue<String> queue = new LinkedList<>();

        root = document.getRootElement();//得到根标签 即download
        Element root1 = root;
        Iterator<?> ruleNodes = root.elementIterator("Part");//获取Part中的内容然后决定下载的内容 迭代器 这样是为了顺序下载

        List<Element> ruleNode_list = new ArrayList<>();
        List<String> list_download_part = new ArrayList<>();
        List<String> range_list = new ArrayList<>();
        //先要进行文件名的编码
        //object_name = auth.to_utf8(object_name);
        GET_URL = auth.endpoint + bucket_name + object_name;

        while (ruleNodes.hasNext())
        {
            Element ruleElement = (Element) ruleNodes.next();
            ruleNode_list.add(ruleElement);

            range = auth.get_range(ruleElement.element("offset").getStringValue(), ruleElement.element("lenth").getStringValue());
            //ti = auth.timestamp();
            String path_tmp = path  + range + "-" + object_name;

            range_list.add(range);
            list_download_part.add(path_tmp);
        }

        int len = range_list.size();


        Document document1 = document;
        String fileName = object_name;
        String bucketName = bucket_name;
        //下载线程1
        Runnable r1 = () ->{
            for(int j = 0;j<len/4;j++)
            {
                if(ruleNode_list.get(j).element("is_ok").getStringValue().equals("false")) {
                    String time = auth.timestamp();
                    String tosign = auth.GET_METHOD + "\n" + "\n" + "\n" + "\n" + "x-amz-date:" + time + "\n" + bucketName
                            + fileName;
                    String Sign = auth.calculateRFC2104HMAC(tosign, auth.awsSecretKey);
                    String ID = "AWS" + " " + auth.awsAccessKeyId + ":" + Sign;
                    auth.downloadObjectPart(GET_URL, auth.GET_METHOD, ID, time, list_download_part.get(j), range_list.get(j));
                    ruleNode_list.get(j).element("is_ok").setText("true");
                    auth.write_xml_to_file(xml_path, document1);
                }
                else
                    continue;
            }
        };
        //下载线程2
        Runnable r2 = () ->{
                for(int j = len/4;j<2*len/4;j++) {
                    if (ruleNode_list.get(j).element("is_ok").getStringValue().equals("false")) {
                        String time = auth.timestamp();
                        String tosign = auth.GET_METHOD + "\n" + "\n" + "\n" + "\n" + "x-amz-date:" + time + "\n" + bucketName
                                + fileName;
                        String Sign = auth.calculateRFC2104HMAC(tosign, auth.awsSecretKey);
                        String ID = "AWS" + " " + auth.awsAccessKeyId + ":" + Sign;
                        auth.downloadObjectPart(GET_URL, auth.GET_METHOD, ID, time, list_download_part.get(j), range_list.get(j));
                        ruleNode_list.get(j).element("is_ok").setText("true");
                        auth.write_xml_to_file(xml_path, document1);
                    }
                }
        };
        //下载线程3
        Runnable r3 = () ->{
            for(int j = 2*len/4;j<3*len/4;j++) {
                if(ruleNode_list.get(j).element("is_ok").getStringValue().equals("false")) {
                    String time = auth.timestamp();
                    String tosign = auth.GET_METHOD + "\n" + "\n" + "\n" + "\n" + "x-amz-date:" + time + "\n" + bucketName
                            + fileName;
                    String Sign = auth.calculateRFC2104HMAC(tosign, auth.awsSecretKey);
                    String ID = "AWS" + " " + auth.awsAccessKeyId + ":" + Sign;
                    auth.downloadObjectPart(GET_URL, auth.GET_METHOD, ID, time, list_download_part.get(j), range_list.get(j));
                    ruleNode_list.get(j).element("is_ok").setText("true");
                    auth.write_xml_to_file(xml_path, document1);
                }
            }
        };
        //下载线程4
        Runnable r4 = () ->{
            for(int j = 3*len/4;j<len;j++) {
                if(ruleNode_list.get(j).element("is_ok").getStringValue().equals("false")) {
                    String time = auth.timestamp();
                    String tosign = auth.GET_METHOD + "\n" + "\n" + "\n" + "\n" + "x-amz-date:" + time + "\n" + bucketName
                            + fileName;
                    String Sign = auth.calculateRFC2104HMAC(tosign, auth.awsSecretKey);
                    String ID = "AWS" + " " + auth.awsAccessKeyId + ":" + Sign;
                    auth.downloadObjectPart(GET_URL, auth.GET_METHOD, ID, time, list_download_part.get(j), range_list.get(j));
                    ruleNode_list.get(j).element("is_ok").setText("true");
                    auth.write_xml_to_file(xml_path, document1);
                }
            }
        };

        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);
        Thread t3 = new Thread(r3);
        Thread t4 = new Thread(r4);

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        //等待线程执行完之后再合并文件
        try{
            t1.join();
            t2.join();
            t3.join();
            t4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        while (ruleNodes.hasNext()) {
//            Element ruleElement = (Element) ruleNodes.next();
//            //ruleElement = (Element) ruleNodes.next();
//
//            //如果Part中的is_ok字段是true则表示已经下载过了  不需要下载
//            if (ruleElement.element("is_ok").getStringValue().equals("true")) {
//                continue;
//            }
//
//            range = auth.get_range(ruleElement.element("offset").getStringValue(), ruleElement.element("lenth").getStringValue());
//            System.out.println("range:" + range);
//            ti = auth.timestamp();
//
//            object_name = auth.to_utf8(object_name);
//            toSign = auth.GET_METHOD + "\n" + "\n" + "\n" + "\n" + "x-amz-date:" + ti + "\n" + bucket_name
//                    + object_name;
//
//            sign = auth.calculateRFC2104HMAC(toSign, auth.awsSecretKey);
//            id = "AWS" + " " + auth.awsAccessKeyId + ":" + sign;
//
//            GET_URL = auth.endpoint + bucket_name + object_name;
//
//            String path_tmp = path  + range + "-" + object_name;
//            System.out.println(path_tmp);
//            //queue.offer(path_tmp);
//
//            //执行分段下载
//            auth.downloadObjectPart(GET_URL, auth.GET_METHOD, id, ti, path_tmp, range);
//
//            //设置下载过的xml中的Part为true
//            ruleElement.element("is_ok").setText("true");
//            auth.write_xml_to_file(xml_path, document);
//        }

        //System.out.println("这是最后一次执行！");
        float startTime=System.nanoTime();
        auth.combination_file(root1, path, object_name);
        System.out.println(System.nanoTime() - startTime);
        file.delete();
    }

}
