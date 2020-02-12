package com.linkto.main.core;

import android.util.Log;

import com.linkto.main.util.Util;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class Server {
	private static final int PORT = 50005;

	private static WebSocketServer sWebSocketServer;
	private static boolean sIsRunning = false;

	private static Scatter sScatter;

	public static synchronized void init() {
		if (sScatter != null) {
			return;
		}

		sScatter = new Scatter();

		start();
	}

	public static Scatter getScatter() {
		return sScatter;
	}

	public static synchronized boolean isRunning() {
		return sIsRunning;
	}

	private synchronized static void start() {
		if (sWebSocketServer != null) {
			try {
				sWebSocketServer.stop();
			} catch (Exception e) {
				Log.e(Util.TAG, "WebSocket.stop", e);
			}
		}

		sWebSocketServer = new WebSocketServer(new InetSocketAddress(PORT)) {
			@Override
			public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
				Log.d(Util.TAG, "WebSocket.onOpen()");
			}

			@Override
			public void onClose(WebSocket webSocket, int code, String reason, boolean byUser) {
				Log.d(Util.TAG, "WebSocket.onClose() " + code + " " + reason + " " + byUser);
			}

			@Override
			public void onMessage(WebSocket webSocket, String message) {
				Log.d(Util.TAG, "WebSocket.onMessage() " + message);

				try {
					sScatter.onMessage(webSocket, message);
				} catch (Exception e) {
					Log.e(Util.TAG, "WebSocket.onMessage", e);
				}
			}

			@Override
			public void onError(WebSocket webSocket, Exception e) {
				Log.d(Util.TAG, "WebSocket.onError()", e);

				synchronized (Server.class) {
					sIsRunning = false;
				}

				new Thread(() -> {
					try {
						Thread.sleep(1500);
					} catch (Exception ex) {
						Log.e(Util.TAG, "sleep", ex);
					}

					Server.start();
				}).start();
			}

			@Override
			public void onStart() {
				Log.d(Util.TAG, "WebSocket.onStart()");

				synchronized (Server.class) {
					sIsRunning = true;
				}
			}
		};

		sWebSocketServer.setReuseAddr(true);
		sWebSocketServer.start();
	}
}
