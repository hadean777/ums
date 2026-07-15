package com.hadean777.ums.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Converter(autoApply = false)
public class Inet6AddressConverter
        implements AttributeConverter<Inet6Address, String> {

    @Override
    public String convertToDatabaseColumn(Inet6Address attribute) {
        return attribute == null ? null : attribute.getHostAddress();
    }

    @Override
    public Inet6Address convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            InetAddress addr = InetAddress.getByName(dbData);

            if (!(addr instanceof Inet6Address ipv6)) {
                throw new IllegalArgumentException("Address is not IPv6: " + dbData);
            }

            return ipv6;
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
