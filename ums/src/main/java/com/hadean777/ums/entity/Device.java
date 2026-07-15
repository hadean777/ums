package com.hadean777.ums.entity;

import com.hadean777.ums.util.Inet6AddressConverter;
import jakarta.persistence.*;

import java.net.Inet6Address;
import java.time.LocalDateTime;

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
    private Short prefixLength = 128;

//    @Convert(converter = Inet6AddressConverter.class)
//    @Column(name = "ip_address", columnDefinition = "INET")
//    private Inet6Address ipAddress;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Column(name = "expires_at", nullable = false)
    private Long expiresAt;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;





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

//    public Inet6Address getIpAddress() {
//        return ipAddress;
//    }
//
//    public void setIpAddress(Inet6Address ipAddress) {
//        this.ipAddress = ipAddress;
//    }

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
}
