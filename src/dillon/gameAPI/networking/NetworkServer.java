package dillon.gameAPI.networking;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

import dillon.gameAPI.errors.NetworkingError;
import dillon.gameAPI.event.EventSystem;
import dillon.gameAPI.event.NetworkEvent;

/**
 * This manages the networking server and clients.
 * 
 * @author Dillon - Github dg092099
 *
 */
public class NetworkServer {
	private static volatile boolean runServer = false; // Whether if the server
														// should be running.
	private static ServerSocket server; // The server socket itself.

	/**
	 * Starts a server.
	 * 
	 * @param port
	 *            The port number to use.
	 * @return The host's IP
	 * @throws NetworkingError
	 *             Thrown when it cannot connect to the port.
	 */
	public static String startServer(int port) throws NetworkingError {
		try {
			server = new ServerSocket(port, 100);
			runServer = true;
			Thread t = new Thread(new server());
			t.start();
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkingError("Error when connecting port.");
		}
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * If the server is running.
	 * 
	 * @return running
	 */
	public static boolean getServerRunning() {
		return runServer;
	}

	/**
	 * Class for running the server.
	 * 
	 * @author Dillon - Github dg092099
	 *
	 */
	static class server implements Runnable {
		@Override
		public void run() {
			while (runServer) {
				try {
					Socket s = server.accept();
					Logger.getLogger("Networking").info("Got client, " + s.getRemoteSocketAddress().toString());
					ClientConnector cc = new ClientConnector(s);
					connectors.add(cc);
					EventSystem.broadcastMessage(new NetworkEvent(NetworkEvent.CONNECT, cc, null), NetworkEvent.class);
				} catch (IOException e) {
				}
			}
		}
	}

	private static ArrayList<ClientConnector> connectors = new ArrayList<ClientConnector>(); // The
																								// connected
																								// clients.

	/**
	 * Gets the arraylist of connectors.
	 * 
	 * @return The arraylist
	 */
	public static ArrayList<ClientConnector> getConnectors() {
		return connectors;
	}

	/**
	 * Shuts down the server. Internal use only.
	 */
	private static void shutdown() {
		try {
			for (int i = 0; i < connectors.size(); i++) {
				connectors.get(i).shutdown();
			}
			server.close();
		} catch (Exception e) {
		}
	}

	/**
	 * Attempts to shutdown the server. Called automatically while game shuts
	 * down.
	 */
	public static void stopServer() {
		runServer = false;
		shutdown();
	}

	/**
	 * Enables server discovery.
	 * 
	 * @param name
	 *            Game name
	 * @param version
	 *            Game version
	 * @param useCode
	 *            If the server should use a code.
	 * @param port
	 *            The port the game's on. Not what port it uses to allow
	 *            discovery.
	 * @return The code if one is asked for, if not, null is returned.
	 */
	public static String enableDiscovery(String name, String version, boolean useCode, int port) {
		String code = Discovery.start(name, version, useCode, port);
		if (code != null) {
			return code;
		}
		return null;
	}

	/**
	 * Stops server discovery.
	 */
	public static void disableDiscovery() {
		Discovery.stop();
	}
}
