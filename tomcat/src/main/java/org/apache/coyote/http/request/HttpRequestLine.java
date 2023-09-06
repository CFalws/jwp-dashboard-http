package org.apache.coyote.http.request;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestLine {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestLine.class);
    private static final int REQUEST_LINE_TOKENS_SIZE = 3;

    private final HttpMethod httpMethod;
    private final String path;
    private final Map<String, String> parameters;
    private final String httpVersion;

    private HttpRequestLine(HttpMethod httpMethod, String path, Map<String, String> parameters, String httpVersion) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.parameters = parameters;
        this.httpVersion = httpVersion;
    }

    public static HttpRequestLine decode(HttpMethod httpMethod, String path, Map<String, String> parameters, String httpVersion) {
        return new HttpRequestLine(httpMethod, path, parameters, httpVersion);
    }

    public static HttpRequestLine decode(String requestLineString) {
        log.info("request line: {}", requestLineString);
        String[] tokens = requestLineString.split(" ");
        validateRequestLine(tokens);

        HttpMethod httpMethod = HttpMethod.from(tokens[0]);
        String path = decodePath(tokens[1]);
        Map<String, String> parameters = decodeParameters(tokens[1]);

        return new HttpRequestLine(httpMethod, path, parameters, tokens[2]);
    }

    private static void validateRequestLine(String[] tokens) {
        if (tokens.length != REQUEST_LINE_TOKENS_SIZE || !tokens[2].startsWith("HTTP")) {
            String requestLine = String.join(" ", tokens);
            log.error("request line: {}", requestLine);
        }
    }

    private static String decodePath(String requestUri) {
        String path = requestUri.split("\\?")[0];
        if (path.equals("/")) {
            return "/home";
        }
        return path;
    }

    private static Map<String, String> decodeParameters(String requestUri) {
        String[] split = requestUri.split("\\?");
        if (split.length < 2) {
            return Collections.emptyMap();
        }

        String parametersString = split[1];
        return HttpParameterDecoder.decode(parametersString);
    }

    public HttpMethod getMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParameters() {
        return new HashMap<>(parameters);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HttpRequestLine that = (HttpRequestLine) o;
        return httpMethod == that.httpMethod && Objects.equals(path, that.path)
            && Objects.equals(parameters, that.parameters) && Objects.equals(
            httpVersion, that.httpVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpMethod, path, parameters, httpVersion);
    }

    @Override
    public String toString() {
        return "HttpRequestLine{" +
            "httpMethod=" + httpMethod +
            ", path='" + path + '\'' +
            ", parameters=" + parameters +
            ", httpVersion='" + httpVersion + '\'' +
            '}';
    }
}
