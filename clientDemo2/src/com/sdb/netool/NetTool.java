package com.sdb.netool;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.sdb.netool.conf.Account;
import com.sdb.netool.conf.BankServer;
import com.sdb.netool.conf.CompanyData;
import com.sdb.netool.conf.Template;

public class NetTool extends JFrame {

    private static final long serialVersionUID = -4171589383520209054L;
    
    private static final String SERVER_LOCAL = "127.0.0.1";
    private static final int SERVER_PORT = 7070;
    private int iPort = SERVER_PORT;
    
    //顶部元数据
    private JPanel panServerPort;
    private JLabel labServerPort;
    private JComboBox comboServerPort;
    
    private JCheckBox chkAutoHead;
    private JComboBox comboTmpl;
    
    private JPanel panYqdm;
    private JLabel labYqdm;
    private JComboBox comboYqdm;
    
    private JPanel panTranCode;
    private JLabel labTranCode;
    private JTextField txtTranCode;
    
    //自动加签名长度
    private JCheckBox chkAutoSubfixLenth;
    private JCheckBox chk0x00;
    
    //中部 发送、接收报文
    //private JPanel panCenter;
    
    private JPanel panSend;
    private JLabel labSend;
    private JPanel spSend;
    private JTextArea taSend;
    
    private JPanel panReceive;
    private JLabel labReceive;
    private JScrollPane spReceive;
    private JTextArea taReceive;
    
    //操作按钮
    private JPanel panButtons;
    private JButton btnClear;
    private JButton btnSend;
    
    //底部声明
    //private JPanel panEnd;
    private JLabel labCopyrightCN;
    private JLabel labCopyrightEN;
    
    private String fmtDate;
    
    private List<BankServer> servers = new ArrayList<BankServer>();
    private List<CompanyData> companies = new ArrayList<CompanyData>();
    private List<Template> templates = new ArrayList<Template>();
    
    int protocal = YQUtil.PROTOCAL_TCP;

    public NetTool() throws Exception {
        
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        fmtDate = String.format("%tY%tm%td", now, now, now);
        parseConfig(new File("conf.xml"));
        
        Container con = this.getContentPane();
        con.setLayout(new FlowLayout());
        
        panServerPort = new JPanel();
        labServerPort = new JLabel("服务端口");
        comboServerPort = new JComboBox();
        comboServerPort.setEditable(true);
        initializeServerPort();
        panServerPort.add(labServerPort);
        panServerPort.add(comboServerPort);
        panServerPort.add(placeholder(10));
        con.add(panServerPort);
        
        chkAutoHead = new JCheckBox("自动生成报文头");
        chkAutoHead.setSelected(true);
        con.add(chkAutoHead);
        
        comboTmpl = new JComboBox();
        initTemplates();
        con.add(comboTmpl);
        
        panYqdm = new JPanel();
        labYqdm = new JLabel("银企代码");
        comboYqdm = new JComboBox();
        comboYqdm.setEditable(true);
        comboYqdm.setSize(50, 0);
        
        initializeYqdm();
        panYqdm.add(labYqdm);
        panYqdm.add(comboYqdm);
        //panYqdm.add(placeholder(10));
        con.add(panYqdm);
        
        labTranCode = new JLabel("交易码");
        txtTranCode = new JTextField(4);
        panTranCode = new JPanel();
        panTranCode.add(labTranCode);
        panTranCode.add(txtTranCode);
        panTranCode.add(placeholder(3));
        con.add(panTranCode);
        
        chkAutoSubfixLenth = new JCheckBox("签名长度");
        chkAutoSubfixLenth.setSelected(false);
        con.add(chkAutoSubfixLenth);
        
        chk0x00 = new JCheckBox("0x00");
        chk0x00.setSelected(false);
        con.add(chk0x00);
        
        //中间部分
        panSend = new JPanel();
        panSend.setLayout(new BorderLayout());
        
        labSend = new JLabel("发送报文：");
        taSend = new JTextArea(10, 50);
        taSend.setLineWrap(true);
        spSend = new JPanel();
        spSend.add(new JScrollPane(taSend));
        panSend.add(labSend, BorderLayout.NORTH);
        panSend.add(spSend, BorderLayout.CENTER);
        con.add(panSend);
        
        panReceive = new JPanel();
        panReceive.setLayout(new BorderLayout());
        
        labReceive = new JLabel("接收报文");
        
        taReceive = new JTextArea(10, 50);
        taReceive.setLineWrap(true);
        spReceive = new JScrollPane(taReceive);
        
        panReceive.add(labReceive, BorderLayout.NORTH);
        panReceive.add(spReceive, BorderLayout.CENTER);
        
        con.add(panReceive);
        
        panButtons = new JPanel();
        btnSend = new JButton("发送");
        btnClear = new JButton("清空结果");
        panButtons.add(btnSend);
        panButtons.add(btnClear);
        initilizeButtons();
        
        con.add(panButtons);
        
        //底部声明
        //panEnd = new JPanel();
        labCopyrightCN = new JLabel("版权所有 \u00A9 中国平安保险（集团）股份有限公司 未经许可不得复制、转载或摘编，违者必究!");
        labCopyrightEN = new JLabel("Copyright \u00A9 PING AN INSURANCE (GROUP) COMPANY OF CHINA ,LTD. All Rights Reserved");
        con.add(labCopyrightCN);
        con.add(labCopyrightEN);
    }
    
    private void parseConfig(File configFile) throws Exception {
        
        SAXReader reader = new SAXReader();
        Document document = reader.read(configFile);
        Element root = document.getRootElement();
        
        Node protocalNode = root.selectSingleNode("protocal");
        if (protocalNode != null) {
            String strProtocal = protocalNode.getText();
            if (strProtocal != null) {
                if (strProtocal.equalsIgnoreCase("http")) {
                    protocal = YQUtil.PROTOCAL_HTTP;
                } else if (strProtocal.equalsIgnoreCase("https")) {
                    protocal = YQUtil.PROTOCAL_HTTPS;
                }
            }
        }
        
        //服务
        List<Node> serverNodes = (List<Node>) root.selectNodes("servers/server");
        for (int i = 0; i < serverNodes.size(); i++) {
            Node serverNode = serverNodes.get(i);
            Node hostNode = serverNode.selectSingleNode("host");
            Node portNode = serverNode.selectSingleNode("port");
            
            String host = hostNode.getText().trim();
            int port = Integer.parseInt(portNode.getText().trim());
                
            BankServer server = new BankServer(host, port);
            servers.add(server);
        }
        
        //企业数据
        List<Node> companyNodes = root.selectNodes("companies/company");
        for (int i = 0; i < companyNodes.size(); i++) {
            Node companyNode = companyNodes.get(i);
            Node qydmNode = companyNode.selectSingleNode("qydm");
            Node nameNode = companyNode.selectSingleNode("name");
            String qydm = qydmNode.getText().trim();
            String name = nameNode.getText().trim();
            
            List<Node> accountNodes = companyNode.selectNodes("accounts/account");
            List<Account> accounts = new ArrayList<Account>();
            for (int j = 0; j < accountNodes.size(); j++) {
                Node accountNode = accountNodes.get(j);
                
                Node accNoNode = accountNode.selectSingleNode("accNo");
                Node accNameNode = accountNode.selectSingleNode("accName");
                
                Account account = new Account(accNoNode.getText().trim(), accNameNode.getText().trim());
                accounts.add(account);
            }
            CompanyData comData = new CompanyData(qydm, name, accounts);
            companies.add(comData);
        }
        
        //模板
        List<Node> templateNodes = (List<Node>) root.selectNodes("templates/template");
        for (int i = 0; i < templateNodes.size(); i++) {
            Node templateNode = templateNodes.get(i);
            
            Node codeNode = templateNode.selectSingleNode("code");
            Node nameNode = templateNode.selectSingleNode("name");
            String code = codeNode.getText().trim();
            
            String name = "";
            if (nameNode != null) {
                String nameText = nameNode.getText().trim();
                name = StringUtils.abbreviate(nameText, 8);
            }
            
            String content = null;
            
            Node contentNode = templateNode.selectSingleNode("content");
            if (contentNode != null) {
                content = contentNode.getText().trim();
            }
            if (content == null || content.equals("")) {
                String tmplFilename = null;
                Node tmplFilenameNode = templateNode.selectSingleNode("fileName");
                if (tmplFilenameNode == null) {
                    tmplFilename = code + ".xml";
                } else {
                    tmplFilename = tmplFilenameNode.getText().trim();
                }
                
                content = readFileContent(String.format("templates/%s", tmplFilename));
            }
            
            if (content != null && !content.equals("")) {
                content = content.replace("yyyyMMdd", fmtDate);
            }
            
            Template template = new Template(code, name, content);
            templates.add(template);
            //templateMap.put(code + name, content);
        }
    }
    
    //初始化模板
    private void initTemplates() {
        
        for(int i = 0; i < templates.size(); i++) {
            Template template = templates.get(i);
            //templateMap.put(tranCode, telegram.replace(strFmt, fmtDate));
            comboTmpl.addItem(template.getCode() + template.getName());
        }
        
        comboTmpl.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int idx = comboTmpl.getSelectedIndex();
                    Template tmpl = templates.get(idx);
                    
                    taSend.setText(tmpl.getContent().replace("yyyyMMdd", fmtDate));
                    txtTranCode.setText(tmpl.getCode());
                }
            }
        });
    }
    
    //初始化银企代码
    private void initializeYqdm() {
        for(int i = 0; i < companies.size(); i++) {
            CompanyData company = companies.get(i);
            comboYqdm.addItem(company.getQydm() + company.getName());
        }
    }
    
    //初始化服务器及端口
    private void initializeServerPort() {
        
        for(int i = 0; i < servers.size(); i++) {
            BankServer server = servers.get(i);
            comboServerPort.addItem(server.getHost() + ":" + server.getPort());
        }
        
        comboServerPort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                String serverPort = ((JTextField) comboServerPort.getEditor().getEditorComponent()).getText();
                int idxColon = serverPort.indexOf(":");
                if(idxColon > 0 && idxColon < serverPort.length()) {
                    String port = serverPort.substring(idxColon + 1).trim();
                    if (port.endsWith("9999")) {
                        chkAutoSubfixLenth.setSelected(true);
                    } else {
                        chkAutoSubfixLenth.setSelected(false);
                    }
                }
            }
            
        });
    }
    
    //初始化按钮点击事件
    private void initilizeButtons() {
        
        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                
                //首先清空接收报文
                taReceive.setText("");
                
                String errMsg = null;
                
                String serverIp = SERVER_LOCAL;
                String port = "7070";
                
                String serverPort = ((JTextField) comboServerPort.getEditor().getEditorComponent()).getText();
                int idxColon = serverPort.indexOf(":");
                if (idxColon > 0 && idxColon < serverPort.length()) {
                    serverIp = serverPort.substring(0, idxColon).trim();
                    port = serverPort.substring(idxColon + 1).trim();
                } else {
                    errMsg = "服务器端口不能为空！";
                }
                
                try {
                    iPort = Integer.parseInt(port);
                } catch(NumberFormatException nfe) {
                    errMsg = "端口必须为整数";
                }
                
                String txtSend = taSend.getText().trim();
                String packets = null;
                
                if(txtSend == "") {
                    errMsg = "发送报文不能为空！";
                    taSend.setFocusable(true);
                }
                
                boolean autoHead = chkAutoHead.isSelected();
                if (autoHead) {
                    String txtYqdm = ((JTextField) comboYqdm.getEditor().getEditorComponent()).getText();
                    String yqdm = txtYqdm;
                    
                    if (yqdm == null || yqdm.length() < 1) {
                        errMsg = "银企代码不能为空";
                        comboYqdm.setFocusable(true);
                    }
                    if (txtYqdm.length() > 20) {
                        yqdm = txtYqdm.substring(0, 20);
                    }
                    
                    String tranCode = txtTranCode.getText().trim();
                    if (tranCode == "") {
                        errMsg = "交易码不能为空！";
                        txtTranCode.setFocusable(true);
                    }
                    
                    packets = YQUtil.asemblyPackets(yqdm, tranCode, txtSend);
                    
                    //加上默认签名长度000000
                    if (chkAutoSubfixLenth.isSelected()) {
                        packets += "000000";
                    }
                } else {
                    packets = txtSend;
                }
                
                if (chk0x00.isSelected()) {
                    packets += new String(new byte[]{0x00});
                }
                
                if (errMsg != null) {
                    taReceive.setText(errMsg);
                    return;
                }
                
                try {
                    long stime = System.currentTimeMillis();
                    Packets packetsRP = YQUtil.send2server(serverIp, iPort, packets, protocal);
                    byte[] headRP = packetsRP.getHead();
                    int bodyRpLen = packetsRP.getLen();
                    byte[] bodyRP = packetsRP.getBody();
                    
                    StringBuilder rcvMsg = new StringBuilder();
                    if(headRP != null) {
                        rcvMsg.append(new String(headRP, YQUtil.CHARSET));
                    }
                    if(bodyRpLen > 0 && bodyRP != null) {
                        rcvMsg.append(new String(bodyRP, YQUtil.CHARSET));
                    }
                    
                    taReceive.setText(rcvMsg.toString());
                    taReceive.setCaretPosition(0);
                    
                    long etime = System.currentTimeMillis();
                    long totalMillis = etime - stime;
                    int seconds = (int) (totalMillis / 1000);
                    int millis = (int)(totalMillis % 1000);
                    
                    StringBuilder duration = new StringBuilder("接收报文，耗时：");
                    if (seconds > 0) {
                        duration.append(seconds).append("秒");
                    }
                    duration.append(millis).append("毫秒");
                    
                    labReceive.setText(duration.toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    taReceive.setText(ex.getMessage());
                }
            }
            
        });
        
        //结果清空按钮
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // TODO Auto-generated method stub
                taReceive.setText("");
            }
            
        });
    }
    
    private JLabel placeholder(int num) {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < num; i++) {
            buf.append(" ");
        }
        return new JLabel(buf.toString());
    }
    
    private String readFileContent(String fileName) throws Exception {
        StringBuilder contentBuffer = new StringBuilder();
        
        File tmplFile = new File(fileName);
        if (tmplFile.exists()) { //文件存在
            BufferedReader freader = null;
            try {
                freader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "GBK"));
                String line = null;
                while ((line = freader.readLine()) != null) {
                    contentBuffer.append(line).append("\n");
                }
                contentBuffer.deleteCharAt(contentBuffer.length() - 1);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (freader != null) {
                    freader.close();
                }
            }
        }
        
        return contentBuffer.toString();
    }
    

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        NetTool netool = new NetTool();
        netool.setTitle("银企直连模拟客户端 V0.3");
        netool.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //设置socket代理
        Properties proxyProperties = new Properties();
        try {
            proxyProperties.load(new FileInputStream("proxy.properties"));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        boolean proxySet = Boolean.parseBoolean(proxyProperties.getProperty("socksProxySet"));
        if (proxySet) {
            System.getProperties().put("socksProxySet", "true");
            
            System.getProperties().put("socksProxyHost", proxyProperties.getProperty("socksProxyHost"));
            System.getProperties().put("socksProxyPort", proxyProperties.getProperty("socksProxyPort"));
        }
        
        //netool.pack();
        netool.setSize(600, 650);
        netool.setLocationRelativeTo(null); //居中显示
        netool.setVisible(true);
    }
}

