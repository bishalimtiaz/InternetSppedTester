package com.blz.internetsppedtester;

public class Info {
    private String info_tittle;
    private String info_val;

    public Info(String info_tittle, String info_val) {
        this.info_tittle = info_tittle;
        this.info_val = info_val;
    }

    public String getInfo_tittle() {
        return info_tittle;
    }

    public void setInfo_tittle(String info_tittle) {
        this.info_tittle = info_tittle;
    }

    public String getInfo_val() {
        return info_val;
    }

    public void setInfo_val(String info_val) {
        this.info_val = info_val;
    }
}
