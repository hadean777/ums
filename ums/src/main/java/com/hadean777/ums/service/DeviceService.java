package com.hadean777.ums.service;

import com.hadean777.ums.entity.Device;
import com.hadean777.ums.model.Ip;
import com.hadean777.ums.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import com.hadean777.ums.service.WireGuardKeyService;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.hadean777.ums.Constants.*;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final WireGuardKeyService wireGuardKeyService;


    public DeviceService(DeviceRepository deviceRepository, WireGuardKeyService wireGuardKeyService) {
        this.deviceRepository = deviceRepository;
        this.wireGuardKeyService = wireGuardKeyService;
    }

    public void generateNewDevice(Long userId) throws Exception {
        Device device = new Device();

        //TODO: this is sample version. Need to be completed correctly.
        final short prefixLength = DEFAULT_PREFIX_LENGTH;
        final long now = new Date().getTime();
        final long expireTime = now + ONE_YEAR_MILLIS;
        final String description = "Test description for user=" + userId + " at time " + now;
        
        WireGuardKeyService.WireGuardKeyPair keyPair = wireGuardKeyService.generateKeyPair();
        final String publicKey = keyPair.getPublicKey();
        final String privateKey = keyPair.getPrivateKey();

        Ip ip = generateIp(prefixLength);

        device.setUserId(userId);
        device.setDescription(description);
        device.setPublicKey(publicKey);
        device.setPrivateKey(privateKey);
        device.setPrefixLength(prefixLength);
        device.setIpPrefix16(ip.getIpPrefix16());
        device.setIpPrefix32(ip.getIpPrefix32());
        device.setIpPrefix32_48(ip.getIpPrefix32_48());
        device.setIpPrefix48_56(ip.getIpPrefix48_56());
        device.setIpPrefix56_64(ip.getIpPrefix56_64());
        device.setIpPrefixSlaac(ip.getIpPrefixSlaac());
        device.setIpAddress(ip.getIpAddress());
        device.setCreatedAt(now);
        device.setUpdatedAt(now);
        device.setExpiresAt(expireTime);

        deviceRepository.save(device);
    }

    public List<Device> getDevicesForUser(Long userId) {
        return deviceRepository.findByUserId(userId);
    }

    public void updateDevice(Long deviceId, String description, Boolean enabled) {
        deviceRepository.findById(deviceId).ifPresent(device -> {
            device.setDescription(description);
            device.setEnabled(enabled);
            device.setUpdatedAt(new Date().getTime());
            deviceRepository.save(device);
        });
    }

    public java.util.Optional<Device> getDeviceById(Long deviceId) {
        return deviceRepository.findById(deviceId);
    }

    private Ip generateIp(Short prefixLength) {
        Ip ip = new Ip();

        final int prefix16 = GLOBAL_PREFIX_16;
        int prefix32 = START_PREFIX_128;
        if (prefixLength == 64) {
            prefix32 = START_PREFIX_64;
        } else if (prefixLength == 56) {
            prefix32 = START_PREFIX_56;
        } else if (prefixLength == 48) {
            prefix32 = START_PREFIX_48;
        } else if (prefixLength == 32) {
            prefix32 = START_PREFIX_32;
        }

        Random rand = new Random();

        prefix32 = prefix32 + rand.nextInt(DEFAULT_PREFIX_32_LIMIT);
        final int prefix32_48 = rand.nextInt(WORD_LIMIT);
        final int prefix48_56 = rand.nextInt(BYTE_LIMIT);
        final int prefix56_64 = rand.nextInt(BYTE_LIMIT);
        final long slaac = rand.nextLong();

        final String ipAddress = convertToInet6Address(
                prefix16,
                prefix32,
                prefix32_48,
                prefix48_56,
                prefix56_64,
                slaac).getHostAddress();

        ip.setPrefixLength(prefixLength);
        ip.setIpPrefix16(prefix16);
        ip.setIpPrefix32(prefix32);
        ip.setIpPrefix32_48(prefix32_48);
        ip.setIpPrefix48_56(prefix48_56);
        ip.setIpPrefix56_64(prefix56_64);
        ip.setIpPrefixSlaac(slaac);
        ip.setIpAddress(ipAddress);

        return ip;
    }

    private Inet6Address convertToInet6Address(Integer prefix16,
                                                      Integer prefix32,
                                                      Integer prefix32_48,
                                                      Integer prefix48_56,
                                                      Integer prefix56_64,
                                                      Long slaac) {

        byte[] bytes = new byte[16];

        bytes[0] = (byte) (prefix16 >>> 8);
        bytes[1] = (byte) prefix16.intValue();

        bytes[2] = (byte) (prefix32 >>> 8);
        bytes[3] = (byte) prefix32.intValue();

        bytes[4] = (byte) (prefix32_48 >>> 8);
        bytes[5] = (byte) prefix32_48.intValue();

        bytes[6] = (byte) prefix48_56.intValue();
        bytes[7] = (byte) prefix56_64.intValue();

        for (int i = 0; i < 8; i++) {
            bytes[8 + i] = (byte) (slaac >>> (56 - 8 * i));
        }

        try {
            return (Inet6Address) InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
