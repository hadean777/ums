package com.hadean777.ums.entity;

import jakarta.persistence.*;

import static com.hadean777.ums.Constants.DEFAULT_PREFIX_LENGTH;

@Entity
@Table(name = "device", indexes = {
        @Index(name = "ix_fk_device_user", columnList = "user_id"),
        @Index(name = "un_device_public_key", columnList = "public_key", unique = true)
})
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "device_seq")
    @SequenceGenerator(name = "device_seq", sequenceName = "device_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "public_key", nullable = false, unique = true, columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "private_key", nullable = false, columnDefinition = "TEXT")
    private String privateKey;

    @Column(name = "prefix_length", nullable = false)
    private Short prefixLength = DEFAULT_PREFIX_LENGTH;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Column(name = "expires_at", nullable = false)
    private Long expiresAt;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "ip_prefix_16", nullable = false)
    private Integer ipPrefix16;

    @Column(name = "ip_prefix_32", nullable = false)
    private Integer ipPrefix32;

    @Column(name = "ip_prefix_32_48", nullable = false)
    private Integer ipPrefix32_48;

    @Column(name = "ip_prefix_48_56", nullable = false)
    private Integer ipPrefix48_56;

    @Column(name = "ip_prefix_56_64", nullable = false)
    private Integer ipPrefix56_64;

    @Column(name = "ip_prefix_slaac", nullable = false)
    private Long ipPrefixSlaac;

    @Column(name = "ip_address", nullable = false, length = 40)
    private String ipAddress;





    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public Short getPrefixLength() {
        return prefixLength;
    }

    public void setPrefixLength(Short prefixLength) {
        this.prefixLength = prefixLength;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getIpPrefix16() {
        return ipPrefix16;
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
