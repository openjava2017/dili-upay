package com.diligrp.upay.boot.util;

import com.diligrp.upay.shared.domain.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * HTTP工具类
 */
public final class HttpUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

    public static String httpBody(HttpServletRequest request) {
        StringBuilder payload = new StringBuilder();
        try {
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                payload.append(line);
            }
        } catch (IOException iex) {
            LOG.error("Failed to extract http body", iex);
        }

        return payload.toString();
    }

    public static void sendResponse(HttpServletResponse response, String payload) {
        try {
            response.setContentType(Constants.CONTENT_TYPE);
            byte[] responseBytes = payload.getBytes(StandardCharsets.UTF_8);
            response.setContentLength(responseBytes.length);
            response.getOutputStream().write(responseBytes);
            response.flushBuffer();
        } catch (IOException iex) {
            LOG.error("Failed to write data packet back");
        }
    }

    public static RequestContext requestContext(HttpServletRequest request) {
        RequestContext context = new RequestContext();
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String name = headers.nextElement();
            String value = request.getHeader(name);
            context.put(name, value);
        }

        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String name = params.nextElement();
            String value = request.getParameter(name);
            context.put(name, value);
        }
        return context;
    }
}
