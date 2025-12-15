package com.sh.bdt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class CustomCorsFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain) throws IOException, ServletException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String remoteAddr = request.getRemoteAddr();

        boolean isLocal =
            remoteAddr.equals("127.0.0.1") || // IPv4
                remoteAddr.equals("0:0:0:0:0:0:0:1") || // IPv6
                remoteAddr.equals("::1"); // IPv6

        if (!isLocal) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            response.getWriter().write("Access denied: local requests only");
            return;
        }

        chain.doFilter(request, response);
    }
}

