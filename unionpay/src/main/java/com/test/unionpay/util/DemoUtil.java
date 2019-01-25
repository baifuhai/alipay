package com.test.unionpay.util;

import com.unionpay.acp.sdk.SDKConfig;
import com.unionpay.acp.sdk.SDKConstants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DemoUtil {

    // 商户发送交易时间
    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    // 商户订单号，不能含"-"或"_"
    public static String getOrderId() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    }

    /**
     * 组装请求，返回报文字符串用于显示
     * @param data
     * @return
     */
    public static String genHtmlResult(Map<String, String> data) {
        TreeMap<String, String> tree = new TreeMap<String, String>();
        Iterator<Map.Entry<String, String>> it = data.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> en = it.next();
            tree.put(en.getKey(), en.getValue());
        }

        it = tree.entrySet().iterator();
        StringBuffer sf = new StringBuffer();
        while (it.hasNext()) {
            Map.Entry<String, String> en = it.next();
            String key = en.getKey();
            String value =  en.getValue();
            if("respCode".equals(key)){
                sf.append("<b>" + key + SDKConstants.EQUAL + value + "</br></b>");
            } else {
                sf.append(key + SDKConstants.EQUAL + value + "</br>");
            }
        }
        return sf.toString();
    }

    /**
     * 获取请求参数中所有的信息
     * 当商户上送frontUrl或backUrl地址中带有参数信息的时候，
     * 这种方式会将url地址中的参数读到map中，会导多出来这些信息从而致验签失败，这个时候可以自行修改过滤掉url中的参数或者使用getAllRequestParamStream方法
     *
     * @param request
     * @return
     */
    public static Map<String, String> getAllRequestParam(HttpServletRequest request) {
        Map<String, String> res = new HashMap<>();

        Enumeration<String> names = request.getParameterNames();
        if (names != null) {
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                String value = request.getParameter(name);
                if (StringUtils.isNotBlank(value)) {
                    res.put(name, value);
                }
            }
        }

        return res;
    }

    /**
     * 获取请求参数中所有的信息
     * 非struts可以改用此方法获取，好处是可以过滤掉request.getParameter方法过滤不掉的url中的参数
     * struts可能对某些content-type会提前读取参数导致从inputstream读不到信息，所以可能用不了这个方法。理论应该可以调整struts配置使不影响，但请自己去研究
     * 调用本方法之前不能调用req.getParameter("key")，这种方法，否则会导致request取不到输入流
     *
     * @param request
     * @return
     */
    public static Map<String, String> getAllRequestParamStream(HttpServletRequest request) throws Exception {
        SDKConfig config = SDKConfig.getInstance();

        Map<String, String> res = new HashMap<>();

        String notifyStr = new String(IOUtils.toByteArray(request.getInputStream()), config.getEncoding());

        String[] kvs = notifyStr.split("&");
        for (String kv : kvs) {
            String[] tmp = kv.split("=");
            if (tmp.length >= 2) {
                String key = tmp[0];
                String value = URLDecoder.decode(tmp[1], config.getEncoding());
                res.put(key, value);
            }
        }

        return res;
    }

    /**
     * 功能：解析全渠道商户对账文件中的ZM文件并以List<Map>方式返回
     * 适用交易：对账文件下载后对文件的查看
     * @param filePath ZM文件全路径
     * @return 包含每一笔交易中 序列号 和 值 的map序列
     */
    public static List<Map> parseZMFile(String filePath){
        int lengthArray[] = {3,11,11,6,10,19,12,4,2,21,2,32,2,6,10,13,13,4,15,2,2,6,2,4,32,1,21,15,1,15,32,13,13,8,32,13,13,12,2,1,32,98};
        return parseFile(filePath,lengthArray);
    }

    /**
     * 功能：解析全渠道商户对账文件中的ZME文件并以List<Map>方式返回
     * 适用交易：对账文件下载后对文件的查看
     * @param filePath ZME文件全路径
     * @return 包含每一笔交易中 序列号 和 值 的map序列
     */
    public static List<Map> parseZMEFile(String filePath){
        int lengthArray[] = {3,11,11,6,10,19,12,4,2,2,6,10,4,12,13,13,15,15,1,12,2,135};
        return parseFile(filePath,lengthArray);
    }

    /**
     * 功能：解析全渠道商户 ZM,ZME对账文件
     * @param filePath
     * @param lengthArray 参照《全渠道平台接入接口规范 第3部分 文件接口》 全渠道商户对账文件 6.1 ZM文件和6.2 ZME 文件 格式的类型长度组成int型数组
     * @return
     */
    private static List<Map> parseFile(String filePath,int lengthArray[]){
        List<Map> ZmDataList = new ArrayList<Map>();
        try {
            String encoding="gbk"; //文件是gbk编码
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), "iso-8859-1");
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    byte[] bs = lineTxt.getBytes("iso-8859-1");
                    //解析的结果MAP，key为对账文件列序号，value为解析的值
                    Map<Integer,String> ZmDataMap = new LinkedHashMap<Integer,String>();
                    //左侧游标
                    int leftIndex = 0;
                    //右侧游标
                    int rightIndex = 0;
                    for(int i=0;i<lengthArray.length;i++){
                        rightIndex = leftIndex + lengthArray[i];
                        String filed = new String(Arrays.copyOfRange(bs, leftIndex,rightIndex), encoding);
                        leftIndex = rightIndex+1;
                        ZmDataMap.put(i, filed);
                    }
                    ZmDataList.add(ZmDataMap);
                }
                read.close();
            }else{
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }

        return ZmDataList;
    }

    public static String getFileContentTable(List<Map> dataList,String file){
        StringBuffer  tableSb = new StringBuffer("对账文件的规范参考 https://open.unionpay.com/ajweb/help/file/ 产品接口规范->平台接口规范:文件接口</br> 文件【"+file + "】解析后内容如下：");
        tableSb.append("<table border=\"1\">");
        if(dataList.size() > 0){
            Map<Integer,String> dataMapTmp = dataList.get(0);
            tableSb.append("<tr>");
            for(Iterator<Integer> it = dataMapTmp.keySet().iterator();it.hasNext();){
                Integer key = it.next();
                String value = dataMapTmp.get(key);
                System.out.println("序号："+ (key+1) + " 值: '"+ value +"'");
                tableSb.append("<td>序号"+(key+1)+"</td>");
            }
            tableSb.append("</tr>");
        }

        for(int i=0;i<dataList.size();i++){
            System.out.println("行数: "+ (i+1));
            Map<Integer,String> dataMapTmp = dataList.get(i);
            tableSb.append("<tr>");
            for(Iterator<Integer> it = dataMapTmp.keySet().iterator();it.hasNext();){
                Integer key = it.next();
                String value = dataMapTmp.get(key);
                System.out.println("序号："+ (key+1) + " 值: '"+ value +"'");
                tableSb.append("<td>"+value+"</td>");
            }
            tableSb.append("</tr>");
        }
        tableSb.append("</table>");
        return tableSb.toString();
    }


    public static List<String> unzip(String zipFilePath,String outPutDirectory){
        List<String> fileList = new ArrayList<String>();
        try {
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFilePath));//输入源zip路径
            BufferedInputStream bin = new BufferedInputStream(zin);
            BufferedOutputStream bout = null;
            File file=null;
            ZipEntry entry;
            try {
                while((entry = zin.getNextEntry())!=null && !entry.isDirectory()){
                    file = new File(outPutDirectory,entry.getName());
                    if(!file.exists()){
                        (new File(file.getParent())).mkdirs();
                    }
                    bout = new BufferedOutputStream(new FileOutputStream(file));
                    int b;
                    while((b=bin.read())!=-1){
                        bout.write(b);
                    }
                    bout.flush();
                    fileList.add(file.getAbsolutePath());
                    System.out.println(file+"解压成功");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                try {
                    bin.close();
                    zin.close();
                    if(bout!=null){
                        bout.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fileList;
    }

}
