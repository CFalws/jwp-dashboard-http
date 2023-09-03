package org.apache.coyote.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.coyote.http.HttpHeader.HEADER_KEY;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHeaderConverter {

    private static final Logger log = LoggerFactory.getLogger(HttpHeaderConverter.class);

    private static final String HEADER_KEY_VALUE_DELIMITER = ": ";
    private static final String MULTIPLE_VALUES_DELIMITER = ",";

    private HttpHeaderConverter() {
    }

    public static HttpHeader decode(String headerString) {
        String stripped = headerString.strip();
        var headerLines = Arrays.stream(stripped.split("\r\n", 0))
                                .filter(line -> !line.isBlank() && !line.matches("\\x00+"))
                                .collect(Collectors.toList());
        log.info("header 값: {}", stripped);

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

    public static String encode(HttpHeader httpHeader) {
        Map<String, List<String>> headers = httpHeader.getHeader();

        String contentType = httpHeader.getContentType();
        if (contentType != null) {
            headers.put(HEADER_KEY.CONTENT_TYPE.value, List.of(contentType));
        }

        return headers.entrySet()
                      .stream()
                      .map(header -> header.getKey()
                          + HEADER_KEY_VALUE_DELIMITER
                          + String.join(MULTIPLE_VALUES_DELIMITER, header.getValue())
                      ).collect(Collectors.joining(" \r\n")) + " \r\n\r\n";
    }
}