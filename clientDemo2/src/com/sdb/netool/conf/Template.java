package com.sdb.netool.conf;

public class Template {
    String code;
    String name;
    String content;

    public Template() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Template(String code, String name, String content) {
        super();
        this.code = code;
        this.name = name;
        this.content = content;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
