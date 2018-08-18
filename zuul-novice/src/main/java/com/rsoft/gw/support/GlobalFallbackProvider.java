package com.rsoft.gw.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.netflix.hystrix.exception.HystrixTimeoutException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GlobalFallbackProvider implements FallbackProvider {
	private static final String FALLBACK_MESSAGE= "Service is temporarily unavailable,Please try again later.";
	
	@Override
	public String getRoute() {
		return "*";
	}

	@Override
	public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
		if (cause instanceof HystrixTimeoutException) {
			log.warn("Service is timeout, request.route:{}",route);
			return this.response(HttpStatus.GATEWAY_TIMEOUT);
		} else {
			log.error("Service is temporarily unavailable:{}", cause.getStackTrace());
			return this.response(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private ClientHttpResponse response(final HttpStatus status) {
		return new ClientHttpResponse() {
			@Override
			public HttpStatus getStatusCode() throws IOException {
				return status;
			}

			@Override
			public int getRawStatusCode() throws IOException {
				return status.value();
			}

			@Override
			public String getStatusText() throws IOException {
				return status.getReasonPhrase();
			}

			@Override
			public void close() {
			}

			@Override
			public InputStream getBody() throws IOException {
				return new ByteArrayInputStream(FALLBACK_MESSAGE.getBytes());
			}

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders headers = new HttpHeaders();
				MediaType mt = new MediaType("application", "json", Charset.forName("UTF-8"));
				headers.setContentType(mt);
				return headers;
			}
		};
	}

}
