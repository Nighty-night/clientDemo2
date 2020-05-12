package com.sdb.netool.ssl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyStore;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;



/***
 * HTTPS客户端
 * 
 * */

public class CupsHttpClient {
	
    private static SSLSocketFactory ssf = null;
	private static String authtype = "C";
	
	private String encoding;
	boolean hostnameVerifier = false;
	
	private static int iProviderType = 0; // 0-Sun,1-IBM
	private static final String ibmJavaVmVendor = "IBM Corporation";
	private static final String sunJavaVmVendor = "Sun Microsystems Inc.";
	
	private static String KEY_STORE_PASSWORD = "123456";;
	//private static String JKSFILE = "c:\\sslclient.jks";
	private static String JKSFILE = "c:\\SDBTESTCA.store";
	
	private int ConnectTimeOut = 3000;
	private int readTimeout = 30000;
	
	private String serverUrl;

	private static int getVendorType() {
		Properties tSysProperties = System.getProperties();
		String tJvmVendor = tSysProperties.getProperty("java.vm.vendor");

		if (tJvmVendor.equals(ibmJavaVmVendor)) {
			iProviderType = 1;
		} else {
			iProviderType = 0;
		}

		//System.err.println("common_info" + "Current Jvm Vendor Type is :[" + iProviderType + "][" + tJvmVendor + "]");

		return iProviderType;
	}
	
	public static void init(String file,String password) {

		getVendorType();
		
		SSLContext sslContext = null;
		String JKSFILE = file;
		
		//String KEY_STORE_PASSWORD = ThreeDes.decryptMode(password);
		String KEY_STORE_PASSWORD = "123456";
		
		try {
			TrustManager[] tm = { new NoCheckX509TrustManager() };
			KeyManagerFactory kmf; 
			TrustManagerFactory tmf; 

			if (iProviderType == 1) {
			    sslContext = SSLContext.getInstance("TLS", "IBMJSSE2");
			    kmf = KeyManagerFactory.getInstance("IbmX509");
				tmf = TrustManagerFactory.getInstance("IbmX509");
			} else {
			    sslContext = SSLContext.getInstance("TLS", "SunJSSE");
				kmf = KeyManagerFactory.getInstance("SunX509");
				tmf = TrustManagerFactory.getInstance("SunX509");
			}

			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(JKSFILE), KEY_STORE_PASSWORD.toCharArray()); 
			kmf.init(ks, KEY_STORE_PASSWORD.toCharArray());
			
			if ("C".equals(authtype)) {

			} else if ("S".equals(authtype)) {

			} else if ("D".equals(authtype)) {

			} else if ("N".equals(authtype)) {

			}

			KeyStore tks = KeyStore.getInstance("JKS");
			tks.load(new FileInputStream(JKSFILE), KEY_STORE_PASSWORD.toCharArray());
			tmf.init(tks);

//			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new java.security.SecureRandom());
			sslContext.init(kmf.getKeyManagers(), tm, new java.security.SecureRandom());
			//sslContext.init(null,null,new java.security.SecureRandom());
			ssf = sslContext.getSocketFactory();
			
			HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public byte[] sendData(String aServerUrl, byte[] aOutputData,
			int aConnTimeOut, int aReadTimeOut, String aEncoding) throws Exception {
//		StringBuffer recvbuf = new StringBuffer();
		
	    if (aServerUrl == null) {
			throw new Exception("没有设置serverUrl");
		}

		URL url = new URL(serverUrl);
		
		Proxy proxy = null;
		
		boolean ishttps = false;
		int indexhttps = serverUrl.indexOf("https");
		if (indexhttps >= 0) {
			ishttps = true;
		}

		HttpURLConnection http = null;

		if (proxy != null) {
			http = (HttpURLConnection) url.openConnection(proxy);
		} else {
			http = (HttpURLConnection) url.openConnection();
		}

		if (!hostnameVerifier) {
			if (ishttps) {
				((HttpsURLConnection) http).setHostnameVerifier(new TrustAnyHostnameVerifier());
			}
		}
		
		if (ishttps) {
			((HttpsURLConnection)http).setSSLSocketFactory(ssf);
		}
		
		http.setConnectTimeout(aConnTimeOut);
		http.setReadTimeout(aReadTimeOut);
		//http.getConnectTimeout();
		http.setDoOutput(true);
		http.setDoInput(true);
		http.setAllowUserInteraction(false);
		http.setUseCaches(false);
		http.setRequestMethod("POST");
		http.setRequestProperty("content-type","text/xml; charset=GBK");
		http.setRequestProperty("Content-Length", String.valueOf(aOutputData.length));
		http.setRequestProperty("Connection", "close");
		http.setRequestProperty("User-Agent","sdb client");
		http.setRequestProperty("Accept", "text/xml");
		OutputStream out = http.getOutputStream();

		out.write(aOutputData);
		out.flush();
		//ByteArrayOutputStream byteout = new ByteArrayOutputStream(4096);
		
		InputStream in = http.getInputStream();
		int code = http.getResponseCode();
		if (code != 200) {
			throw new Exception("找不到web服务");
		}
		
		int contentLength = http.getContentLength();
		byte[] recvBuf = new byte[contentLength];
		
		int recvLen = 0;
		while (recvLen < contentLength) {
		    recvLen = in.read(recvBuf, recvLen, contentLength - recvLen);
		}
		
		out.close();
		in.close();
		
		return recvBuf;
		//return byteout.toByteArray();
	}
	
	public static void test() {
	    
		System.out.println("开始测试");
		
		//CupsHttpClient.init("D:\\ccproject\\weixin-proj-20130722\\tanchang_Dev_EBANK_201304A_20130722\\vobs\\EBANK_VOB\\src\\WeiXin\\WebContent\\WEB-INF\\jks\\weixin.jks", "7B86E5967613CBC46D6A6A58E7A1597626489C723562EC70763D1C3232A8F8484FF8AA05146D70BA2E21C884CAE13568ABB95F43286DAD9BF0EA72F851F161D80A7244266DA87D09F53940184F464BE73F289DEAAEA89DA5B67C70136928C8649F29D4B46B48E1AE723344D197DAA60649BDE4AC894F7AD453116798D9041C1C3EE892A1B700DC65");
		//CupsHttpClient.init("D:\\workspace_ebank\\YQServer\\JavaSource\\cn\\com\\sdb\\tcpclient\\weixin.jks", "7B86E5967613CBC46D6A6A58E7A1597626489C723562EC70763D1C3232A8F8484FF8AA05146D70BA2E21C884CAE13568ABB95F43286DAD9BF0EA72F851F161D80A7244266DA87D09F53940184F464BE73F289DEAAEA89DA5B67C70136928C8649F29D4B46B48E1AE723344D197DAA60649BDE4AC894F7AD453116798D9041C1C3EE892A1B700DC65");
		
		CupsHttpClient.init("D:/b2bic_20140506/b2bic/cert/155_1_8.pfx", "");

		int tTimeout = 60;
		byte[] aOutputData = null;
		boolean tSendFlg = true; // 发送成功标志
		String sErrorMsg = "";

		byte[] tResult; // 返回的数据
		
		try {
			String sendstr= "";
			//sendstr = WeiXinFile.readTextFileToString("C:\\dist\\caidan.txt","utf-8");
			
//			String access_token = "9-mtzukatoZ3TSjUkBQj53WiB_dtvxBpLrPGzVP5sf-AxlvwBlPmh9f354ak5njZHpMs2Q0djLmQWC9lGk-P_HjxXzq86aE0q55Y5f_SvJ2YjawmBf6Lz6gPaRB7wmqWL0FmQQjqpEJwOIxISB0_Xg";
			
			String access_token = "m5JQE1EL8cL_Q7DbRZ7YwhbqEWrYmMAKsKdavv16D6Zs0cbnl4tkzJuBxNYroRuM3n-Nxdgkn6lEfnH0QbUfJrUuGGFBJl7tMO7s3qejm89nTXjaTm4174ewJtmze_9tlhlHvUWe6SLoNin6KmwiyQ";
			//access_token ceeate Time:20130924 18:26  生产
			
			/*
			sendstr = "";
			StringBuffer sSendData = new StringBuffer();
			sSendData.append(sendstr);
			aOutputData = sSendData.toString().getBytes();
			
			String AppId="wx76beada409792444";
			String AppSecret="60f3d62f500f906f9fbe177d67b59d9e";
			String proxyip="10.2.5.70";
			String proxyport="8080";
			CupsHttpClient ci = new CupsHttpClient();
			ci.setConnectTimeOut(tTimeout);
			ci.setReadTimeout(tTimeout);
			ci.setServerUrl("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + AppId + "&secret=" + AppSecret);

			ci.setProxyip(proxyip);
			ci.setProxyport(proxyport);
			ci.setEncoding("utf-8");
			tResult = ci.sendData(aOutputData);

			System.out.println(new String(tResult, "GBK"));

			String jsonStr = new String(tResult, "GBK");
			Map info = (Map) JSON.parse(jsonStr);
			access_token = (String) info.get("access_token");
			String expires_in = (String) info.get("expires_in");
			Map map = new HashMap();
			map.put("access_token", access_token);
			
			System.out.println("access_token="+access_token);
			*/
			//直接复制以下这段到浏览器即可，测试环境：https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx76beada409792444&secret=60f3d62f500f906f9fbe177d67b59d9e//获取token
			//生产如下：https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx29a4fc8b9b5bebdc&secret=3d33b753e8cc2f8526b4cbce0396bc40

			/*
			SendMsgToWeiXinUtil.setAppId("wx76beada409792444");
			SendMsgToWeiXinUtil.setAppSecret("60f3d62f500f906f9fbe177d67b59d9e");
			SendMsgToWeiXinUtil.setProxyip("10.2.5.70");
			SendMsgToWeiXinUtil.setProxyport("8080");
			access_token =SendMsgToWeiXinUtil.GetAccessToken() ;//"gVdaZoV7qQFI0FEOKqbi9JqRdRAFszbZl1LnEDZP0_jpm2u4yuLFlhkukSJmCqwNpg3edqJraN7iJub6lJs8lc-UukIgIJ9Yd9hOWLzUNai2BrJVey5yhP6LJI9s0xVX";
			
			System.out.println("access_token="+access_token);
			*/
			
			//谭昶
			//access_token =
//			sendstr = WeiXinFile.readTextFileToString("C:\\dist\\caidan-prod20130905.txt","gbk");
//			String[] user = {"oD4fnjrjBhCDg5gooV5-uWwqcwD4", "oD4fnjjjT2F83eIRxeD50rZhlKGw"};
			String[] user = {"oD4fnjrjBhCDg5gooV5-uWwqcwD4", "oD4fnjrjBhCDg5gooV5-uWwqcwD4"};
//			sendstr = "{\"touser\":\"oD4fnjrjBhCDg5gooV5-uWwqcwD4\",\"msgtype\":\"text\",\"text\":{\"content\":\"主动发送消息测试\"}}" ;
			
			System.out.println(sendstr);
			
			StringBuffer sSendData = new StringBuffer();
			sSendData.append(sendstr);
			aOutputData = sSendData.toString().getBytes();
			
			CupsHttpClient ci = new CupsHttpClient();
			ci.setConnectTimeOut(3000);
			ci.setReadTimeout(30000);
			
//			ci.setServerUrl("https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + access_token);//创建菜单
//			ci.setServerUrl("https://api.weixin.qq.com/cgi-bin/menu/get?access_token=" + access_token);//查询菜单
//			ci.setServerUrl("https://202.96.255.140:443/CUPSecureSR/servlet/SSLServlet");
//			ci.setServerUrl("https://api.weixin.qq.com/cgi-bin/user/info?access_token="+access_token+"&openid=oD4fnjrjBhCDg5gooV5-uWwqcwD4");//获取用户信息
			
			ci.setServerUrl("https://127.0.0.1:7072/PAb2bi"); //主动发送消息
			
//			ci.setProxyip("10.2.5.70");
//			ci.setProxyport("8080");
			
			ci.setEncoding("GBK");
			
			String sendData = g4001Package();
			System.out.println("send:" + sendData);
			
			tResult = null;//ci.sendData(sendData.getBytes());
			
			if (tResult == null) {
				sErrorMsg = "发送超时。";
				tSendFlg = false;
			} else {
				sErrorMsg = "发送成功。";
				tSendFlg = true;
			}
			
			System.out.println("rcv:" + new String(tResult,"GBK"));
			
		} catch (Exception e) {
			e.printStackTrace();
			tSendFlg = false;
			sErrorMsg = "发送失败。";
		}
	}
	
    public static String g4001Package() {
		//A00101010100201080500008015   0000000109S001       0120100809171028    2010080981026055999999000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
	    //"A00101010100201080500008015   0000000109S001       0120100809171028    2010080981026055999999000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000<?xml version=\"1.0\" encoding=\"UTF-8\"?><Result><Account>0512100008226</Account><CcyCode>RMB</CcyCode></Result>";
	    
        String str = "A0010101010100108000000zzzz0000000000053S001  123450120140801161552YQTEST20140801161552                                                                                                          000001            00000000000<?xml version=\"1.0\" encoding=\"GBK\"?><Result></Result>";
	    return str;
	}
    
    public static void main(String[] args) throws Exception {

		System.out.println("开始测试");
		
		//test();
		testHttp();
		
		/*
		if (true) {
			return;
		}
		
		CupsHttpClient.init("D:\\ccproject\\weixin-proj-20130722\\tanchang_Dev_EBANK_201304A_20130722\\vobs\\EBANK_VOB\\src\\WeiXin\\WebContent\\WEB-INF\\jks\\weixin.jks", "7B86E5967613CBC46D6A6A58E7A1597626489C723562EC70763D1C3232A8F8484FF8AA05146D70BA2E21C884CAE13568ABB95F43286DAD9BF0EA72F851F161D80A7244266DA87D09F53940184F464BE73F289DEAAEA89DA5B67C70136928C8649F29D4B46B48E1AE723344D197DAA60649BDE4AC894F7AD453116798D9041C1C3EE892A1B700DC65");

		int tTimeout = 60;

		byte[] aOutputData = null;

		boolean tSendFlg = true; // 发送成功标志

		String sErrorMsg = "";

		byte[] tResult; // 返回的数据

		try {
			String sendstr= "IrReq=PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48Q1VQU2VjdXJlPjxNZXNzYWdlIGlkPSJTREJfSVJSZXFfMjAxMDAzMTExNjQxNDlfODM5NzkxIj48SVJSZXE%2BPHZlcnNpb24%2BMS4wLjA8L3ZlcnNpb24%2BPE1lcmNoYW50PjxhY3FCSU4%2BMDMwNzAwMTA8L2FjcUJJTj48bWVySUQ%2BMzA3MDAxMDkzOTkwMDAxPC9tZXJJRD48L01lcmNoYW50PjxTeXN0ZW0%2BPHRyYWNlTnVtPjgzOTc5MTwvdHJhY2VOdW0%2BPGRhdGU%2BMjAxMDAzMTEgMTY6NDE6NDk8L2RhdGU%2BPC9TeXN0ZW0%2BPC9JUlJlcT48L01lc3NhZ2U%2BPC9DVVBTZWN1cmU%2B";
			//sendstr = WeiXinFile.readTextFileToString("C:\\dist\\caidan.txt","utf-8");
			String access_token = "ZTvtSTfvyde3S5OOhjBf49XUqVNHzEhh5UwYL40Jd8JCHmbzSF2xfjTZzHNA2sgMyuUTGxqJRVJ7GkP0dplNW7HhtT8w2oTBJlSHCH6F9uwZe3yZ7oz_rjYAxjcHp7qk";
			sendstr = WeiXinFile.readTextFileToString("C:\\dist\\caidan2.txt","gbk");
			StringBuffer sSendData = new StringBuffer();
			sSendData.append(sendstr);
			aOutputData = sSendData.toString().getBytes();
			CupsHttpClient ci = new CupsHttpClient();
			ci.setConnectTimeOut(3000);
			ci.setReadTimeout(30000);
			ci.setServerUrl("https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + access_token);
			
//			ci.setServerUrl("https://202.96.255.140:443/CUPSecureSR/servlet/SSLServlet");
//			ci.setProxyip("10.14.11.48");
//			ci.setProxyport("50005");
			ci.setEncoding("utf-8");
			tResult = ci.sendData(aOutputData);

			if (tResult == null) {
				sErrorMsg = "发送超时。";
				tSendFlg = false;
			} else {
				sErrorMsg = "发送成功。";
				tSendFlg = true;
			}
			
			System.out.println(new String(tResult,"UTF-8"));

		} catch (Exception e) {
			e.printStackTrace();
			tSendFlg = false;
			sErrorMsg = "发送失败。";
		}*/

	}
    
    public static void testHttp() throws Exception {
        
        byte[] packets = g4001Package().getBytes("GBK");
        
        URL url =  new URL("http://127.0.0.1:7072"); //https://127.0.0.1:7072/PAb2bi
        
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        
        http.setConnectTimeout(60000);
        http.setReadTimeout(60000);
        http.setDoOutput(true);
        http.setDoInput(true);
        http.setAllowUserInteraction(false);
        http.setUseCaches(false);
        http.setRequestMethod("POST");
        http.setRequestProperty("content-type","text/xml; charset=GBK");
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
        
        int contentLength = http.getContentLength();
        byte[] recvBuf = new byte[contentLength];
        
        int recvLen = 0;
        while (recvLen < contentLength) {
            recvLen = in.read(recvBuf, recvLen, contentLength - recvLen);
        }
        
        System.out.println(new String(recvBuf, "GBK"));
        
        return;
    }
    
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public boolean isHostnameVerifier() {
		return hostnameVerifier;
	}

	public void setHostnameVerifier(boolean hostnameVerifier) {
		this.hostnameVerifier = hostnameVerifier;
	}

	public int getConnectTimeOut() {
		return ConnectTimeOut;
	}

	public void setConnectTimeOut(int connectTimeOut) {
		ConnectTimeOut = connectTimeOut;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
}
