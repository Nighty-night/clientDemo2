package com.sdb.netool.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.X509TrustManager;

import javax.net.ssl.TrustManager;

/** 
 * This trustManager is dedicated to server certificate verifications.  
 * It does not check that the certificate is present in the VM's keyStore. 
 * <BR> 
 * @author Olivier PETRUCCI 
 * <BR> 
 * <BR>Areca Build ID : 8290826359148479344 
 * http://read.pudn.com/downloads102/sourcecode/windows/file/417883/areca-5.5.7/src/com/myJava/ssl/NoCheckX509TrustManager.java__.htm
 */  

public class NoCheckX509TrustManager implements X509TrustManager, TrustManager {

	private Set alreadyLogged = new HashSet();  
    
    public void checkClientTrusted(X509Certificate[] certs, String arg1) throws CertificateException {  
        throw new CertificateException("NoCheckX509TrustManager can't be used for client certification.");  
    }  
      
    public void checkServerTrusted(X509Certificate[] certs, String alg) throws CertificateException {  
        for (int i=0; i<certs.length; i++) {  
            String name = certs[i].getIssuerDN().getName();  
            if (!alreadyLogged.contains(name)) {  
                System.out.println("Server data (" + alg + ") : " + name+ " NoCheckX509TrustManager");  
                alreadyLogged.add(name);  
            }  
        }  
    }  
      
    public X509Certificate[] getAcceptedIssuers() {  
        return new X509Certificate[0];  
    }  


}
