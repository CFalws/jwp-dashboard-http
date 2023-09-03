package org.apache.coyote.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class HttpHeaderDecoder {

    private static final Logger log = LoggerFactory.getLogger(HttpHeaderDecoder.class);

    private static final String HEADER_KEY_VALUE_DELIMITER = ": ";
    private static final String MULTIPLE_VALUES_DELIMITER = ",";

    private HttpHeaderDecoder() {
    }

    public static HttpHeader decode(String headerString) {
        String[] headerLines = headerString.split("\r\n");

        Map<String, List<String>> header = new HashMap<>();
        for (var headerLine : headerLines) {
            String[] keyValue = headerLine.split(HEADER_KEY_VALUE_DELIMITER);
            validateFormat(keyValue);
            String key = keyValue[0];
            String value = keyValue[1];

            List<String> values = header.computeIfAbsent(key, ignored -> new ArrayList<>());
            values.addAll(decodeMultipleValues(value));
        }

        return HttpHeader.from(header);
    }

    private static void validateFormat(String[] keyValue) {
        if (keyValue.length != 2) {
            log.error("헤더 키, 값 오류: {}", String.join(" ", keyValue));
        }
    }

    private static List<String> decodeMultipleValues(String headerValue) {
        return Arrays.stream(headerValue.split(MULTIPLE_VALUES_DELIMITER))
                .map(String::strip)
                .collect(Collectors.toList());
    }
}