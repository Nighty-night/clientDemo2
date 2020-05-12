package com.sdb.netool;

/**
 * ±¨ÎÄÄÚÈİ
 * 
 * @author ZHANGXUELING871
 * @version 0.1
 * @since 2014-01-03
 */
public class Packets {
    byte[] head;
    byte[] body;

    int len;

    public byte[] getHead() {
        return head;
    }

    public void setHead(byte[] head) {
        this.head = head;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

}
