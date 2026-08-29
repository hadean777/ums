package com.hadean777.ums.service;

import com.hadean777.ums.entity.Device;
import com.hadean777.ums.model.Ip;
import com.hadean777.ums.repository.DeviceRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
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
    private final WireGuardService wireGuardService;


    public DeviceService(DeviceRepository deviceRepository, WireGuardService wireGuardService) {
        this.deviceRepository = deviceRepository;
        this.wireGuardService = wireGuardService;
    }

    public void generateNewDevice(Long userId, String generationMode, String description) throws Exception {
        Device device = new Device();

        final boolean isShortIp = "Short IP".equals(generationMode);
        short prefixLength = DEFAULT_PREFIX_LENGTH;
        if (generationMode != null && !isShortIp) {
            try {
                prefixLength = Short.parseShort(generationMode);
            } catch (NumberFormatException e) {
                // ignore, use default
            }
        }
        
        final long now = new Date().getTime();
        final long expireTime = now + ONE_YEAR_MILLIS;
        final String deviceDescription = description != null && !description.isEmpty() ? description : "";
        
        WireGuardService.WireGuardKeyPair keyPair = wireGuardService.generateKeyPair();
        final String publicKey = keyPair.getPublicKey();
        final String privateKey = keyPair.getPrivateKey();

        Ip ip = generateIp(prefixLength, isShortIp);

        device.setUserId(userId);
        device.setDescription(deviceDescription);
        device.setPublicKey(publicKey);
        device.setPrivateKey(privateKey);
        device.setPrefixLength(ip.getPrefixLength());
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

        wireGuardService.addPeer(device.getPublicKey(), CLIENT_ALLOWED_IPS);
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

    public void deleteDevice(Long deviceId) throws Exception {
        deviceRepository.findById(deviceId).ifPresent(device -> {
            try {
                wireGuardService.removePeer(device.getPublicKey());
            } catch (Exception e) {
                // Log the error but continue with DB deletion? 
                // Given the requirement "it should remove record from DB and from Wirgguard", 
                // if WG fails, maybe we should still try to clean up DB or vice versa.
                // Usually it's better to try both.
                e.printStackTrace();
            }
            deviceRepository.delete(device);
        });
    }

    public String generateDeviceConfig(Device device) {
        StringBuilder sb = new StringBuilder();
        sb.append("[Interface]\n");
        sb.append("PrivateKey = ").append(device.getPrivateKey()).append("\n");
        sb.append("Address = ").append(device.getIpAddress()).append("/").append(device.getPrefixLength()).append("\n");
        sb.append("\n");
        sb.append("[Peer]\n");
        sb.append("PublicKey = ").append(wireGuardService.getServerPublicKey()).append("\n");
        sb.append("Endpoint = ").append(wireGuardService.getServerEndpoint()).append("\n");
        sb.append("AllowedIPs = ").append(CLIENT_ALLOWED_IPS).append("\n");
        return sb.toString();
    }

    public byte[] generateQRCode(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    private Ip generateIp(Short prefixLength, boolean isShortIp) {
        Ip ip = new Ip();

        Random rand = new Random();
        int prefix32_48 = rand.nextInt(WORD_LIMIT);
        int prefix48_56 = rand.nextInt(BYTE_LIMIT);
        int prefix56_64 = rand.nextInt(BYTE_LIMIT);
        long slaac = rand.nextLong();

        final int prefix16 = GLOBAL_PREFIX_16;
        int prefix32 = START_PREFIX_128;
        if (prefixLength == 64) {
            prefix32 = START_PREFIX_64;
            slaac = 1;
        } else if (prefixLength == 56) {
            prefix32 = START_PREFIX_56;
            prefix56_64 = 0;
            slaac = 1;
        } else if (prefixLength == 48) {
            prefix32 = START_PREFIX_48;
            prefix48_56 = 0;
            prefix56_64 = 0;
            slaac = 1;
        } else if (prefixLength == 32) {
            prefix32 = START_PREFIX_32;
            prefix32_48 = 0;
            prefix48_56 = 0;
            prefix56_64 = 0;
            slaac = 1;
        } else if (isShortIp) {
            slaac = rand.nextInt(BYTE_LIMIT);//TODO: use sequence or something similar
        }

        prefix32 = prefix32 + rand.nextInt(DEFAULT_PREFIX_32_LIMIT);


        final String ipAddress = toRfc5952(convertToInet6Address(
                prefix16,
                prefix32,
                prefix32_48,
                prefix48_56,
                prefix56_64,
                slaac));

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

    private String toRfc5952(Inet6Address address) {
        byte[] bytes = address.getAddress();

        int[] groups = new int[8];
        for (int i = 0; i < 8; i++) {
            groups[i] = ((bytes[i * 2] & 0xff) << 8)
                    | (bytes[i * 2 + 1] & 0xff);
        }

        // Find longest run of zero groups (must be at least 2).
        int bestStart = -1;
        int bestLength = 0;

        for (int i = 0; i < 8; ) {
            if (groups[i] != 0) {
                i++;
                continue;
            }

            int start = i;
            while (i < 8 && groups[i] == 0) {
                i++;
            }

            int length = i - start;
            if (length > bestLength && length >= 2) {
                bestStart = start;
                bestLength = length;
            }
        }

        // Build RFC 5952 representation.
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            if (i == bestStart) {
                sb.append("::");
                i += bestLength - 1;
                continue;
            }

            if (i > 0 && i != bestStart + bestLength) {
                sb.append(':');
            }

            sb.append(Integer.toHexString(groups[i]));
        }

        return sb.toString();
    }

}
