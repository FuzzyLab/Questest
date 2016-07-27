/*
 * Copyright (c) 2013 CitrusPay. All Rights Reserved.
 *
 * This software is the proprietary information of CitrusPay.
 * Use is subject to license terms.
 */
package com.fuzzylabs.questest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Scanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class RestConnection {

	private static final int connectTimeout = 180000;
	private static final int readTimeout = 180000;

	public String sendPostForm(String url, Map<String, String> data)
			throws IOException {
		return createHttpConnectionForm(new URL(url), createURLParams(data));
	}

	public String sendPostJson(String url, String data)
			throws IOException {
		return createHttpConnectionJson(new URL(url), data);
	}

	public Bitmap getImage(String url)
			throws IOException {
		return createHttpConnectionBitmap(new URL(url));
	}

	private String createHttpConnectionJson(URL url, String data)
			throws IOException {
		try {
			HttpURLConnection httpConnection = (HttpURLConnection) url
					.openConnection();
			httpConnection.setRequestMethod("POST");
			httpConnection.setUseCaches(false);
			httpConnection.setDoInput(true);
			httpConnection.setDoOutput(true);
			httpConnection.setAllowUserInteraction(false);
			httpConnection.setConnectTimeout(connectTimeout);
			httpConnection.setReadTimeout(readTimeout);
			httpConnection.setRequestProperty("Content-Type", String
					.format("application/json"));
			httpConnection.setRequestProperty("Accept", String
					.format("application/json"));
			OutputStream output = null;
			try {
				output = httpConnection.getOutputStream();
				output.write(data.getBytes("UTF-8"));
			} finally {
				if (output != null)
					output.close();
			}
			String rBody = getResponseBody(httpConnection.getInputStream());
			return rBody;
		} catch (IOException e) {
		}
		return null;
	}

	private String createHttpConnectionForm(URL url, String data)
			throws IOException {
		try {
			HttpURLConnection httpConnection = (HttpURLConnection) url
					.openConnection();
			httpConnection.setRequestMethod("POST");
			httpConnection.setUseCaches(false);
			httpConnection.setDoInput(true);
			httpConnection.setDoOutput(true);
			httpConnection.setAllowUserInteraction(false);
			httpConnection.setConnectTimeout(connectTimeout);
			httpConnection.setReadTimeout(readTimeout);
			httpConnection.setRequestProperty("Content-Type", String
					.format("application/x-www-form-urlencoded;charset=UTF-8"));
			OutputStream output = null;
			try {
				output = httpConnection.getOutputStream();
				output.write(data.getBytes("UTF-8"));
			} finally {
				if (output != null)
					output.close();
			}
			String rBody = getResponseBody(httpConnection.getInputStream());
			return rBody;
		} catch (IOException e) {
		}
		return null;
	}

	private Bitmap createHttpConnectionBitmap(URL url)
			throws IOException {
		try {
			HttpURLConnection httpConnection = (HttpURLConnection) url
					.openConnection();
			httpConnection.setRequestMethod("GET");
			httpConnection.setUseCaches(false);
			httpConnection.setAllowUserInteraction(false);
			httpConnection.setConnectTimeout(connectTimeout);
			httpConnection.setReadTimeout(readTimeout);
			Bitmap rBody = BitmapFactory.decodeStream(httpConnection.getInputStream());
			return rBody;
		} catch (IOException e) {
		}
		return null;
	}

	private String createURLParams(Map<String, String> params) {
		StringBuilder queryStringBuffer = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			queryStringBuffer.append("&");
			try {
				queryStringBuffer.append(urlPair((String) entry.getKey(),
						(String) entry.getValue()));
			} catch (UnsupportedEncodingException e) {
			}
		}
		if (queryStringBuffer.length() > 0)
			queryStringBuffer.deleteCharAt(0);

		return queryStringBuffer.toString();
	}

	private String urlPair(String key, String value)
			throws UnsupportedEncodingException {
		return String
				.format("%s=%s",
						new Object[] {
								URLEncoder.encode(key, "UTF-8"),
								URLEncoder.encode(value == null ? "" : value,
										"UTF-8") });
	}

	@SuppressWarnings("resource")
	private String getResponseBody(InputStream responseStream) {
		try {
			String rBody = new Scanner(responseStream, "UTF-8").useDelimiter(
					"\\A").next();
			responseStream.close();
			return rBody;
		} catch (Exception e) {
		}
		return null;
	}
}