package com.hadean777.ums.service;

import com.hadean777.ums.entity.Device;
import com.hadean777.ums.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static com.hadean777.ums.Constants.ONE_YEAR_MILLIS;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;


    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Device generateNewDevice(Long userId) throws Exception {
        Device device = new Device();

        //TODO: this is sample version. Need to be completed correctly.
        final long now = new Date().getTime();
        final long expireTime = now + ONE_YEAR_MILLIS;
        final String description = "Test description for user=" + userId + " at time " + now;
        final String publicKey = "PUBLIC_KEY=" + now;
        final String privateKey = "PRIVATE_KEY=" + now;
        Integer prefix16 = 0x2001;
        Integer prefix32 = 0x9000;
        Integer prefix32_48 = 0x1234;
        Integer prefix48_56 = 0x56;
        Integer prefix56_64 = 0x64;
        Long slaac = 0x88881123456789abL;

        final Inet6Address ipAddress = convertToInet6Address(
                prefix16,
                prefix32,
                prefix32_48,
                prefix48_56,
                prefix56_64,
                slaac);


        device.setUserId(userId);
        device.setDescription(description);
        device.setPublicKey(publicKey);
        device.setPrivateKey(privateKey);
        device.setIpPrefix16(prefix16);
        device.setIpPrefix32(prefix32);
        device.setIpPrefix32_48(prefix32_48);
        device.setIpPrefix48_56(prefix48_56);
        device.setIpPrefix56_64(prefix56_64);
        device.setIpPrefixSlaac(slaac);
        device.setIpAddress(ipAddress.getHostAddress());
        device.setCreatedAt(now);
        device.setUpdatedAt(now);
        device.setExpiresAt(expireTime);

        deviceRepository.save(device);

        return device;
    }

    public List<Device> getDevicesForUser(Long userId) {
        return deviceRepository.findByUserId(userId);
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
