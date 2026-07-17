package com.hadean777.ums.model;

public class Ip {

    private Short prefixLength;
    private Integer ipPrefix16;
    private Integer ipPrefix32;
    private Integer ipPrefix32_48;
    private Integer ipPrefix48_56;
    private Integer ipPrefix56_64;
    private Long ipPrefixSlaac;
    private String ipAddress;

    public Ip() {

    }

    public Short getPrefixLength() {
        return prefixLength;
    }

    public Integer getIpPrefix16() {
        return ipPrefix16;
    }

    public void setPrefixLength(Short prefixLength) {
        this.prefixLength = prefixLength;
    }

    public void setIpPrefix16(Integer ipPrefix16) {
        this.ipPrefix16 = ipPrefix16;
    }

    public Integer getIpPrefix32() {
        return ipPrefix32;
    }

    public void setIpPrefix32(Integer ipPrefix32) {
        this.ipPrefix32 = ipPrefix32;
    }

    public Integer getIpPrefix32_48() {
        return ipPrefix32_48;
    }

    public void setIpPrefix32_48(Integer ipPrefix32_48) {
        this.ipPrefix32_48 = ipPrefix32_48;
    }

    public Integer getIpPrefix48_56() {
        return ipPrefix48_56;
    }

    public void setIpPrefix48_56(Integer ipPrefix48_56) {
        this.ipPrefix48_56 = ipPrefix48_56;
    }

    public Integer getIpPrefix56_64() {
        return ipPrefix56_64;
    }

    public void setIpPrefix56_64(Integer ipPrefix56_64) {
        this.ipPrefix56_64 = ipPrefix56_64;
    }

    public Long getIpPrefixSlaac() {
        return ipPrefixSlaac;
    }

    public void setIpPrefixSlaac(Long ipPrefixSlaac) {
        this.ipPrefixSlaac = ipPrefixSlaac;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

}
