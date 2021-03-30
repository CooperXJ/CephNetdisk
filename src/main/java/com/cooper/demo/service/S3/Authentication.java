package com.cooper.demo.service.S3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Queue;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import javax.crypto.spec.SecretKeySpec;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

//import Decoder.BASE64Encoder;
import org.apache.commons.codec.binary.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;;

public class Authentication {

    private static final String UTF8_CHARSET = "UTF-8";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    public String endpoint  = "http://172.23.27.119:7480";
    public String awsAccessKeyId = "U6DVKQNYCPHCIIP0XZFJ";
    public String awsSecretKey = "A32aqcbXEECBtfxL2UqqBoMs2zyJJBMnRpG0gj7i";
    public String DEL_METHOD = "DELETE";
    public String PUT_METHOD = "PUT";
    public String HEAD_METHOD = "HEAD";//http head 请求资源的头部信息获取文件大小信息  无响应体
    public String GET_METHOD = "GET";    //http get  请求资源  有响应体
    public String POST_METHOD = "POST";
    public String GET_ACL = "?acl";
    public String SET_ACL = "?acl";
    public String Etag = null;
    int object_size = 0;

    //初始化公钥和密钥
    public Authentication(String awsAccessKeyId,String awsSecretKey)
    {
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretKey = awsSecretKey;
    }

    //设置时间戳
    String timestamp() {
        String timestamp = null;
        Calendar cal = Calendar.getInstance();
        DateFormat dfm = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss '+0000'", Locale.UK);
        dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
        timestamp = dfm.format(cal.getTime());
        return timestamp;
    }

    //得到指定的范围
    String get_range(String offset, String len) {
        return (offset + "-" + Integer.toString((Integer.parseInt(offset) + Integer.parseInt(len) - 1)));
    }

    //合并下载的片段并输出到指定的路径
    //Element 构建xml中节点的类
    public void combination_file(Element root, String path, String objectname) {
        InputStream in = null;
        OutputStream out = null;
        byte[] bytes = new byte[2048];
        File f = new File(path + objectname);
        f.delete();
        String range = null;
        String file_path = null;

        try {
            out = new FileOutputStream(path + objectname, true);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        //合并下载的文件片段
        Iterator<?> ruleNodes = root.elementIterator("Part");//获取xml文件下所有标签为part的节点信息
        while (ruleNodes.hasNext()) {
            Element ruleElement = (Element) ruleNodes.next();
            //当part标签下的is_ok标签为true的时候则证明下载完成
            if (!(ruleElement.element("is_ok").getStringValue().equals("true"))) {

                continue;
            }

            //得到具体的下载范围 即偏移量和需要下载的大小 以此来获得对应的文件
            range = get_range(ruleElement.element("offset").getStringValue(),
                    ruleElement.element("lenth").getStringValue());
            file_path = path + range + "-" + objectname;

            //打开xml文件生成输入流
            try {
                in = new FileInputStream(file_path);
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            int n = -1;
            try {
                while ((n = in.read(bytes, 0, bytes.length)) != -1) {
                    try {
                        //写入文件
                        out.write(bytes, 0, n);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } catch (UnsupportedEncodingException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            try {
                in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            f = new File(file_path);
            f.delete();
        }

        try {
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //写xml文件
    void write_xml_to_file(String path, Document document) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //设置输出的xml格式
        //createPrettyPrint  漂亮的格式
        //createCompactFormat  紧凑的格式 这种格式可以使得生成的xml文件变小
        OutputFormat format = OutputFormat.createPrettyPrint();
        // 1.创建写出对象
        XMLWriter writer = null;
        try {
            writer = new XMLWriter(out, format);
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // 2.写出Document对象
        try {
            writer.write(document);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // 3.关闭流
        try {
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    //获得文件大小
    int getfile_size(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            return (int) file.length();
        } else {
            return -1;
        }
    }

    //将中文字符进行编码发送请求
    String to_utf8(String stringToSign) {
        String out = null;
        try {
            //http get请求不能传输中文参数问题。http请求是不接受中文参数的，因此需要使用编码发送带中文字符的请求
            out = URLEncoder.encode(stringToSign, UTF8_CHARSET);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        return out;
    }

    //加密消息
    String calculateRFC2104HMAC(String data, String key) {
        //对密钥进行加密编码，防止请求的时候泄露密钥
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

        Mac mac = null;
        try {
            mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            mac.init(signingKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        byte[] rawHmac = mac.doFinal(data.getBytes());

//        BASE64Encoder BASE64Encoder = new BASE64Encoder();
//        return BASE64Encoder.encode(rawHmac);
        return Base64.encodeBase64String(rawHmac);
    }

//    byte[] HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
//        byte[] data = encryptKey.getBytes(UTF8_CHARSET);
//        SecretKey secretKey = new SecretKeySpec(data, HMAC_SHA1_ALGORITHM);
//        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
//        mac.init(secretKey);
//
//        byte[] text = encryptText.getBytes(UTF8_CHARSET);
//        return mac.doFinal(text);
//    }
//
//    byte[] hmacSHA1(String data, String key) throws java.security.SignatureException {
//        try {
//            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
//
//            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
//            mac.init(signingKey);
//
//            byte[] rawHmac = mac.doFinal(data.getBytes());
//
//            return rawHmac;
//
//        } catch (Exception e) {
//            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
//        }
//    }

    //连接终端  向服务端发送http请求
    HttpURLConnection connectEndpoint(String get_url, String method, String Authorization, String ti) {
        URL url = null;
        HttpURLConnection connection = null;

        try {
            //创建http请求
            url = new URL(get_url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            connection = (HttpURLConnection) url.openConnection();//HttpConnection对象，这一步只是创建了对象并没有连接远程服务器
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            connection.setRequestMethod(method);//设置发送请求的方法  是get还是post方法
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        connection.addRequestProperty("Authorization", Authorization);//设置http请求头参数  在原来的基础上增加
        connection.addRequestProperty("x-amz-date", ti);
        connection.setDoInput(true);//允许读
        connection.setDoOutput(true);//允许写
        try {
            connection.connect();//真正的连接服务器
        } catch (IOException e) {
            e.printStackTrace();
        }

        return connection;//返回连接
    }

    HttpURLConnection connectEndpoint(String get_url, String method, String Authorization, String ti, String range) {
        URL url = null;
        HttpURLConnection connection = null;

        try {
            url = new URL(get_url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            connection.setRequestMethod(method);
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        connection.addRequestProperty("Authorization", Authorization);
        connection.addRequestProperty("x-amz-date", ti);
        String tmp = "bytes=" + range;
        connection.addRequestProperty("Range", tmp);//设置range的value 也就是请求下载资源的片段 服务端则对称的响应Content-Range
        connection.setDoInput(true);
        connection.setDoOutput(true);
        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return connection;
    }

    //分段下载文件资源
    void downloadObjectPart(String get_url, String method, String Authorization, String ti, String path, String range) {
        HttpURLConnection connection = null;
        InputStream is = null;
        FileOutputStream fos = null;
        int len = 0;
        byte[] buff = new byte[8 * 1024];

        URL url = null;

        //发送分段请求的http请求
        connection = connectEndpoint(get_url, method, Authorization, ti, range);
        try {
            url = new URL(get_url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            connection.setRequestMethod(method);
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        connection.addRequestProperty("Authorization", Authorization);
        connection.addRequestProperty("x-amz-date", ti);
        String tmp = "bytes=" + range;
        connection.addRequestProperty("Range", tmp);

        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fos = new FileOutputStream(new File(path), false);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        try {
            is = connection.getInputStream();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while ((len = is.read(buff)) != -1) {
                fos.write(buff, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        getConnectionRes(connection);

        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.disconnect();

        return;
    }

    void downloadObject(String get_url, String method, String Authorization, String ti, String path) {
        HttpURLConnection connection = null;
        InputStream is = null;
        FileOutputStream fos = null;
        int len = 0;
        byte[] buff = new byte[8 * 1024];

        URL url = null;

        connection = connectEndpoint(get_url, method, Authorization, ti);
        try {
            url = new URL(get_url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            connection.setRequestMethod(method);
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        connection.addRequestProperty("Authorization", Authorization);
        connection.addRequestProperty("x-amz-date", ti);

        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fos = new FileOutputStream(new File(path), false);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        try {
            is = connection.getInputStream();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while ((len = is.read(buff)) != -1) {
                fos.write(buff, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        getConnectionRes(connection);

        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.disconnect();

        return;
    }

    /*
     * private static String MD5(byte[] sourceStr) throws Exception {
     * MessageDigest mdInst = MessageDigest.getInstance("MD5");
     * mdInst.update(sourceStr); byte[] md = mdInst.digest(); StringBuffer buf =
     * new StringBuffer(); for (int i = 0; i < md.length; i++) { int tmp =
     * md[i]; if (tmp < 0) tmp += 256; if (tmp < 16) buf.append("0");
     * buf.append(Integer.toHexString(tmp)); } return buf.toString(); }
     */
//    public static String MD5(String path) {
//        FileInputStream fis = null;
//        File f = new File(path);
//        try {
//            fis = new FileInputStream(f);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        int size = 0;
//        try {
//            size = fis.available();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        byte[] buffer = new byte[size];
//        try {
//            fis.read(buffer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        MessageDigest mdInst = null;
//        try {
//            mdInst = MessageDigest.getInstance("MD5");
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        mdInst.update(buffer);
//        byte[] md = mdInst.digest();
//        StringBuffer buf = new StringBuffer();
//        for (int i = 0; i < md.length; i++) {
//            int tmp = md[i];
//            if (tmp < 0)
//                tmp += 256;
//            if (tmp < 16)
//                buf.append("0");
//            buf.append(Integer.toHexString(tmp));
//        }
//        return buf.toString();
//    }

//    public String parse_xml(String path) {
//        SAXReader reader = new SAXReader();
//        Document document = null;
//        try {
//            document = reader.read(new File(path));
//        } catch (DocumentException e1) {
//            e1.printStackTrace();
//        }
//
//        Element root = document.getRootElement();
//
//        Element conElem = root.element("UploadId");
//        return conElem.getText();
//    }

//    int multiple_put(String get_url, String method, String Authorization, String ti, byte[] buffer, int size) {
//        HttpURLConnection connection = null;
//        OutputStream outputStream = null;
//        String etag = null;
//        int status = 0;
//
//        // System.out.println(get_url);
//        connection = connectEndpoint(get_url, method, Authorization, ti);
//
//        try {
//            outputStream = connection.getOutputStream();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            outputStream.write(buffer, 0, size);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            outputStream.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        etag = connection.getHeaderField("ETag");
//        Etag = etag;
//        InputStream errorInputStream = null;
//        try {
//            try {
//                status = connection.getResponseCode();
//                if (status >= 400) {
//                    errorInputStream = connection.getErrorStream();
//                    throw new Exception("xxxxxxxxx ");
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } finally {
//            if (errorInputStream != null) {
//                try {
//                    errorInputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            try {
//                outputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            connection.disconnect();
//        }
//
//        try {
//            System.out.println(connection.getResponseCode());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return status;
//    }

//    public void createXML(String part_num, String etag, Element root) {
//        Element part = root.addElement("Part");
//        part.addElement("PartNumber").addText(part_num);
//        part.addElement("ETag").addText(etag);
//    }

    //创建xml文件并设置相应的节点
    public void createXML(String offset, String len, String is_ok, Element root) {
        Element part = root.addElement("Part");
        part.addElement("offset").addText(offset);
        part.addElement("lenth").addText(len);
        part.addElement("is_ok").addText(is_ok);
    }

//    String putobject(String get_url, String method, String Authorization, String ti, String path) {
//        HttpURLConnection connection = null;
//        FileInputStream fis = null;
//        File f = null;
//        OutputStream outputStream = null;
//
//        connection = connectEndpoint(get_url, method, Authorization, ti);
//
//        f = new File(path);
//        try {
//            fis = new FileInputStream(f);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        int size = 0;
//        try {
//            size = fis.available();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        byte[] buffer = new byte[size];
//        try {
//            fis.read(buffer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            outputStream = connection.getOutputStream();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            outputStream.write(buffer, 0, size);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            outputStream.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        getConnectionRes(connection);
//
//        try {
//            fis.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            outputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        connection.disconnect();
//        return null;
//    }

    //获得connection信息  主要目的是为了 获取下载目标文件的长度
    void getConnectionRes(HttpURLConnection connection) {
        InputStream errorInputStream = null;
        try {
            try {
                if (connection.getResponseCode() >= 400) {
                    errorInputStream = connection.getErrorStream();
                    throw new Exception("connect err ");
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            if (errorInputStream != null) {
                try {
                    errorInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        try {
            System.out.println(connection.getResponseCode());
            //获得返回的报文中华的Content-length长度，也就是文件的大小
            object_size = connection.getContentLength();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    String putMethodBody(String get_url, String method, String Authorization, String ti, String body) {
//        HttpURLConnection connection = null;
//        OutputStream outputStream = null;
//
//        connection = connectEndpoint(get_url, method, Authorization, ti);
//
//        try {
//            outputStream = connection.getOutputStream();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            outputStream.write(body.getBytes(), 0, body.length());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            outputStream.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        getConnectionRes(connection);
//
//        try {
//            outputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        connection.disconnect();
//
//        return null;
//    }


    String putMethod(String get_url, String method, String Authorization, String ti) {
        HttpURLConnection connection = null;

        connection = connectEndpoint(get_url, method, Authorization, ti);
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(connection.getInputStream(), UTF8_CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        getConnectionRes(connection);

        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.disconnect();
        return sb.toString();

    }

}

























