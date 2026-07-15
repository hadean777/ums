package com.hadean777.ums.service;

import com.hadean777.ums.entity.Device;
import com.hadean777.ums.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.net.Inet6Address;
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
        final Inet6Address ipAddress = (Inet6Address) Inet6Address.getByName("2001:0db8:85a3:0000:0000:8a2e:0370:7334");


        device.setUserId(userId);
        device.setDescription(description);
        device.setPublicKey(publicKey);
        device.setPrivateKey(privateKey);
        //device.setIpAddress(ipAddress);//TODO: it is not working. Need to be fixed
        device.setCreatedAt(now);
        device.setUpdatedAt(now);
        device.setExpiresAt(expireTime);

        deviceRepository.save(device);

        return device;
    }

    public List<Device> getDevicesForUser(Long userId) {
        return deviceRepository.findByUserId(userId);
    }

}
