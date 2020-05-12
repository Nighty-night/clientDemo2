package com.sdb.netool;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.sdb.netool.ssl.NoCheckX509TrustManager;

/**
 * 银企直连实用函数
 * 
 * @author ZHANGXUELING871
 * @version 0.1
 * @since 2014-01-03
 */
public class YQUtil {
    
    public static final int HEAD_LEN_NEW = 222;
    public static final int HEAD_LEN_OLD = 6;
    
    public static final String CHARSET = "GBK";
    private static final String fmtTime = "yyyyMMddHHmmss";
    private static final int TIME_OUT = 120000; //超时时间，单位为毫秒，默认2分钟
    
    public static final int PROTOCAL_TCP = 0;
    public static final int PROTOCAL_HTTP = 1;
    public static final int PROTOCAL_HTTPS = 2;
    
    
    /**
     * 组装报文
     * 这里append比较多，是为了展现报文头的各个字段；实际使用中请按需减少
     * 
     * @param yqdm 20位银企代码
     * @param bsnCode 交易代码
     * @param xmlBody xml主体报文
     * @return
     */
    public static String asemblyPackets(String yqdm, String bsnCode, String xmlBody) {
        
        Date now = Calendar.getInstance().getTime();
        
        StringBuilder buf = new StringBuilder();
        buf.append("A00101");
        
        //编码
        String encoding = "01";
        if (CHARSET.equalsIgnoreCase("GBK")) {
            encoding = "01";
        } else if(CHARSET.equalsIgnoreCase("utf-8") || CHARSET.equalsIgnoreCase("utf8")) {
            encoding = "02";
        }
        buf.append(encoding);//编码
        
        buf.append("01");//通讯协议为TCP/IP
        buf.append(String.format("%20s", yqdm));//银企代码
        try {
            buf.append(String.format("%010d", xmlBody.getBytes(CHARSET).length));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        buf.append(String.format("%-6s", bsnCode));//交易码-左对齐
        buf.append("12345");//操作员代码-用户可自定义
        buf.append("01");//服务类型 01请求
        
        String fmtNow = new SimpleDateFormat(fmtTime).format(now);
        buf.append(fmtNow); //请求日期时间
        
        String requestLogNo = "YQTEST" + fmtNow;
        buf.append(requestLogNo);//请求方系统流水号
        
        buf.append(String.format("%6s", "")); //返回码
        buf.append(String.format("%100s", ""));
        
        buf.append(0); //后续包标志
        buf.append(String.format("%03d", 0));//请求次数
        buf.append("0");//签名标识 0不签
        buf.append("1");//签名数据包格式
        buf.append(String.format("%12s", "")); //签名算法
        buf.append(String.format("%010d", 0)); //签名数据长度
        buf.append(0);//附件数目
        buf.append(xmlBody);//报文体
        
        return buf.toString();
    }
    
    /**
     * 发送报文
     * @param serverIp 服务器IP地址（SCP地址）
     * @param iPort 端口号
     * @param packetsRQ 请求报文头
     * @return
     */
    public static Packets send2server(String serverIp, int iPort, String packetsRQ, int protocal) throws Exception {
        
        Packets packets = null;
        
        URL url = null;
        switch(protocal) {
        case PROTOCAL_TCP:
            packets = send2TcpServer(serverIp, iPort, packetsRQ);
            break;
        case PROTOCAL_HTTP:
            url = new URL("http://" + serverIp + ":" + iPort);
            packets = send2httpServer(url, packetsRQ);
            break;
        case PROTOCAL_HTTPS:
            url = new URL("https://" + serverIp + ":" + iPort);
            packets = send2httpServer(url, packetsRQ);
            break;
        }
        return packets;
    }
    
    
    private static Packets send2TcpServer(String serverIp, int iPort, String packetsRQ) {
        Packets packetsRP = new Packets();
        
        boolean isOld = false;
        if (!packetsRQ.startsWith("A001")) {
            isOld = true;
        }
        
        OutputStream out = null;
        InputStream in = null;
        Socket socket = null;
        try {
            socket = new Socket(serverIp, iPort);
            socket.setSendBufferSize(4096);
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(TIME_OUT);
            socket.setKeepAlive(true);
            out = socket.getOutputStream();
            in = socket.getInputStream();
            out.write(packetsRQ.getBytes(CHARSET));
            out.flush();
            
            int headLen = HEAD_LEN_NEW;
            if (isOld) {
                headLen = HEAD_LEN_OLD;
            }
            
            //读取报文头
            byte[] head = new byte[headLen];
            int headTotal = 0;
            int len = 0;
            while (headTotal < headLen && len > -1) {
                len = in.read(head, headTotal, headLen - headTotal);
                headTotal += len;
            }
            packetsRP.setHead(head);
            
            //读取报文体
            int bodyLen = 0;
            if(isOld) {
                bodyLen = Integer.parseInt(new String(head));
            } else {
                bodyLen = Integer.parseInt(new String(head, 30, 10, CHARSET));
            }
            
            if(bodyLen > 0) {
                packetsRP.setLen(bodyLen);
                
                byte[] body = new byte[bodyLen];
                
                int bodyTotal = 0;
                len = 0;
                
                while (bodyTotal < bodyLen && len > -1) {
                    len = in.read(body, bodyTotal, bodyLen - bodyTotal);
                    bodyTotal += len;
                }
                packetsRP.setBody(body);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        
        return packetsRP;
    }
    
    private static SSLSocketFactory ssf = null;
    
    private static Packets send2httpServer(URL url, String packetsRQ) throws Exception {
        //URL url = new URL("http://" + serverIp + ":" + iPort); //127.0.0.1:7072
        
        String protocal = url.getProtocol();
        
        if (protocal.equalsIgnoreCase("https")) {
            SSLContext sslContext = null;
            String ksPwd = "123456";
            
            try {
                InputStream in = YQUtil.class.getClassLoader().getResourceAsStream("trust.store"); //class目录下
                
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(in, ksPwd.toCharArray());
                
                sslContext = SSLContext.getInstance("TLS", "SunJSSE");
                
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, ksPwd.toCharArray());
                
                TrustManager[] tm = {new NoCheckX509TrustManager()};
                sslContext.init(kmf.getKeyManagers(), tm, new java.security.SecureRandom());
                ssf = sslContext.getSocketFactory();
                HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        
        http.setConnectTimeout(60000);
        http.setReadTimeout(60000);
        http.setDoOutput(true);
        http.setDoInput(true);
        http.setAllowUserInteraction(false);
        http.setUseCaches(false);
        http.setRequestMethod("POST");
        http.setRequestProperty("content-type","text/xml; charset=GBK");
        
        byte[] packets = packetsRQ.getBytes("GBK");
        http.setRequestProperty("Content-Length", String.valueOf(packets.length));
        http.setRequestProperty("Connection", "close");
        http.setRequestProperty("User-Agent","sdb client");
        http.setRequestProperty("Accept", "text/xml");
        OutputStream out = http.getOutputStream();
        
        out.write(packets);
        out.flush();
        
        InputStream in = http.getInputStream();
        int code = http.getResponseCode();
        if (code != 200) {
            throw new Exception("找不到web服务");
        }
        
        Packets packetRep = new Packets();
        
        byte[] head = new byte[HEAD_LEN_NEW];
        int recvLen = 0;
        while (recvLen < HEAD_LEN_NEW) {
            recvLen = in.read(head, recvLen, HEAD_LEN_NEW - recvLen);
        }
        
        packetRep.setHead(head);
        
        int bodyLen = Integer.parseInt(new String(head, 30, 10, CHARSET));;
        packetRep.setLen(bodyLen);
        
        if (bodyLen > 0) {
            byte[] body = new byte[bodyLen];
            recvLen = 0;
            while (recvLen < bodyLen) {
                recvLen = in.read(body, recvLen, bodyLen - recvLen);
            }
            packetRep.setBody(body);
        }
        
        return packetRep;
    }
}
