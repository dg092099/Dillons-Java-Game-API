package dillon.gameAPI.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import dillon.gameAPI.errors.GeneralRuntimeException;
import dillon.gameAPI.event.EventSystem;
import dillon.gameAPI.event.ShutdownEvent;
import dillon.gameAPI.gui.GuiSystem;
import dillon.gameAPI.modding.ModdingCore;
import dillon.gameAPI.networking.NetworkConnection;
import dillon.gameAPI.networking.NetworkServer;
import dillon.gameAPI.scroller.Camera;
import dillon.gameAPI.scroller.ScrollManager;
import dillon.gameAPI.security.RequestedAction;
import dillon.gameAPI.security.SecurityKey;
import dillon.gameAPI.security.SecuritySystem;

/**
 * Core file that has starting and stopping methods. A policy file should be
 * used to isolate the game to a certain directory.
 *
 * @author Dillon - Github dg092099
 */
public class Core {
	private static String TITLE; // The game's title.
	private static Image ICON; // The icon for the game.
	private static JFrame frame; // The JFrame window.
	public static final String ENGINE_VERSION = "v1.13"; // The engine's
															// version.
	public static final int TILES = 1; // Constant: Render method, tile.
	public static final int SIDESCROLLER = 2; // Constant: render method,
												// sidescroller.

	/**
	 * This method starts the game with the specified background and fps. <b>Use
	 * setup method before this method.</b>
	 *
	 * @param FPS
	 *            The maximum frames per second to use.
	 * @param background
	 *            The default background image. to change it.
	 * @param key
	 *            The security key for the security module.
	 */
	public static void startGame(final int FPS, final BufferedImage background, final SecurityKey key) {
		if (frame == null)
			throw new GeneralRuntimeException("Invalid state: Use setup method first.");
		SecuritySystem.checkPermission(key, RequestedAction.START_GAME);
		Logger.getLogger("Core").info("Starting game.");
		controller.start();
		controller.setFps(FPS);
		final Thread t = new Thread(controller); // So that the game loop can't
		// interfere with other programming.
		t.setName("Canvas Controller");
		t.start();
		if (background != null)
			CanvasController.setBackgroundImage(background);
		new ScrollManager(engineKey);
		new Camera();
		new GuiSystem(engineKey);
		ModdingCore.sendPostStart();
	}

	/**
	 * Pauses the game.
	 *
	 * @param k
	 *            The security key.
	 */
	public static void pauseUpdate(SecurityKey k) {
		SecuritySystem.checkPermission(k, RequestedAction.PAUSE);
		controller.pauseUpdate();
	}

	/**
	 * Unpauses the game.
	 *
	 * @param k
	 *            The security key.
	 */
	public static void unpauseUpdate(SecurityKey k) {
		SecuritySystem.checkPermission(k, RequestedAction.UNPAUSE);
		controller.unpauseUpdate();
	}

	/**
	 * The background color.
	 *
	 * @return The background color.
	 */
	public static Color getBackColor() {
		return controller.getBackground();
	}

	/**
	 * This returns the title you specified for the game.
	 *
	 * @return Name
	 */
	public String getTitle() {
		return TITLE;
	}

	/**
	 * This returns the icon on the taskbar that you requested to be used.
	 *
	 * @return taskbar icon
	 */
	public Image getIcon() {
		return ICON;
	}

	private static SecurityKey engineKey;

	/**
	 * This method sets up the jframe.
	 *
	 * @param width
	 *            width of jframe.
	 * @param height
	 *            height of jframe.
	 * @param title
	 *            title of game.
	 * @param icon
	 *            Icon to use in the corner of the program - optional, the
	 *            engine has its own, but it's ugly so use your own.
	 * @param k
	 *            The security key.
	 * @throws IOException
	 *             Results because the icon that you specified couldn't be
	 *             found.
	 */
	public static void setup(final int width, final int height, final String title, final Image icon, SecurityKey k)
			throws IOException {
		SecuritySystem.checkPermission(k, RequestedAction.SETUP_GAME);
		engineKey = SecuritySystem.init();
		TITLE = title;
		ICON = icon;
		Logger.getLogger("Core").info("Setting up...");
		frame = new JFrame(title);
		controller = new CanvasController(engineKey);
		frame.getContentPane().setSize(new Dimension(width, height));
		frame.setTitle(title);
		frame.setResizable(false);
		if (icon != null)
			frame.setIconImage(icon);
		else
			frame.setIconImage(
					ImageIO.read(Core.class.getClassLoader().getResourceAsStream("dillon/gameAPI/res/logo.png")));
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(final WindowEvent arg0) {
			}

			@Override
			public void windowClosed(final WindowEvent arg0) {
			}

			@Override
			public void windowClosing(final WindowEvent arg0) {
				shutdown(false, engineKey);
			}

			@Override
			public void windowDeactivated(final WindowEvent arg0) {
			}

			@Override
			public void windowDeiconified(final WindowEvent arg0) {
			}

			@Override
			public void windowIconified(final WindowEvent arg0) {
			}

			@Override
			public void windowOpened(final WindowEvent arg0) {
			}
		});
		controller = new CanvasController(engineKey);
		frame.add(controller);
		frame.pack();
		frame.setLocationRelativeTo(null);
		ModdingCore.sendInit();
		frame.setVisible(true);
	}

	private static boolean fullscreen = false;

	/**
	 * Sets if the screen should be fullscreen.
	 *
	 * @param b
	 *            Weather or not it should be fullscreen.
	 * @param k
	 *            The security key.
	 */
	public static void setFullScreen(final boolean b, SecurityKey k) {
		SecuritySystem.checkPermission(k, RequestedAction.SET_FULLSCREEN);
		final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		if (devices[0].isFullScreenSupported()) {
			fullscreen = b;
			devices[0].setFullScreenWindow(fullscreen ? frame : null);
		}

	}

	/**
	 * Returns jframe width
	 *
	 * @return width
	 */
	public static int getWidth() {
		return frame.getContentPane().getWidth();
	}

	/**
	 * Returns jframe height
	 *
	 * @return height
	 */
	public static int getHeight() {
		return frame.getContentPane().getHeight();
	}

	static CanvasController controller;

	/**
	 * This method shuts off the event system and activates CanvasController's
	 * Crash to stop the engine.
	 *
	 * @param e
	 *            The exception that was thrown.
	 * @param k
	 *            The security key.
	 */
	public static void crash(final Exception e, SecurityKey k) {
		SecuritySystem.checkPermission(k, RequestedAction.CRASH_GAME);
		try {
			NetworkServer.stopServer(engineKey);
			NetworkConnection.disconnect(engineKey);
			EventSystem.override();
			controller.crash(e);
		} catch (final Exception e2) {
			e2.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * This method will shutdown the game. This occurs when the x is clicked,
	 * the key combination shift + escape is used, or the game crashes.
	 *
	 * @param hard
	 *            Determines if the api would yield to the event system for
	 *            shutdown, or if it will shutdown directly.
	 * @param k
	 *            The security key.
	 */
	public static void shutdown(final boolean hard, SecurityKey k) {
		SecuritySystem.checkPermission(k, RequestedAction.SHUTDOWN);
		if (hard) {
			Logger.getLogger("Core").severe("Engine Shutting down...");
			NetworkServer.stopServer(engineKey);
			NetworkConnection.disconnect(engineKey);
			NetworkServer.disableDiscovery(engineKey);
			System.exit(0);
		} else {
			Logger.getLogger("Core").severe("Engine Shutting down...");
			EventSystem.broadcastMessage(new ShutdownEvent(), ShutdownEvent.class, engineKey);
			controller.stop();
			Logger.getLogger("Core").severe("Stopping server...");
			NetworkServer.stopServer(engineKey);
			NetworkConnection.disconnect(engineKey);
			NetworkServer.disableDiscovery(engineKey);
			System.exit(0);
		}
	}

	/**
	 * This method sets the maximum frames per second that this game should run
	 * on.
	 *
	 * @param fps
	 *            The new fps limit.
	 * @param k
	 *            The security key.
	 */
	public static void setFPS(final int fps, SecurityKey k) {
		SecuritySystem.checkPermission(k, RequestedAction.SET_FPS);
		controller.setFps(fps);
	}

	/**
	 * This method will change the color of the background on the game.
	 *
	 * @param c
	 *            The new color.
	 * @param k
	 *            The security key
	 */
	public static synchronized void setBackColor(final Color c, SecurityKey k) {
		SecuritySystem.checkPermission(k, RequestedAction.SET_BACKGROUND_COLOR);
		controller.setBackground(c);
	}

	/**
	 * This function will return the current version of the engine.
	 *
	 * @return Version string
	 */
	public static String getVersion() {
		return ENGINE_VERSION;
	}

	/**
	 * Gets the background image.
	 *
	 * @return The background image.
	 */
	public static BufferedImage getBackgroundImage() {
		return CanvasController.getBackgroundImage();
	}

	/**
	 * Gets the current fps.
	 *
	 * @return FPS
	 */
	public static int getFPS() {
		return CanvasController.getFPS();
	}

	/**
	 * Sets the background image
	 *
	 * @param background
	 *            The image
	 * @param k
	 *            The security key.
	 */
	public static void setBackgroundImage(final BufferedImage background, SecurityKey k) {
		SecuritySystem.checkPermission(k, RequestedAction.SET_BACKGROUND_IMAGE);
		CanvasController.setBackgroundImage(background);
	}

	/**
	 * Used for debugging a crash.
	 *
	 * @return Debugging string
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("\n\ndillon.gameAPI.core.Core Dump:\n");
		sb.append("Title: " + TITLE);
		sb.append("\n");
		sb.append("Icon: " + ICON.toString());
		sb.append("\n");
		sb.append("Frame: " + frame.toString());
		sb.append("\n");
		sb.append("Fullscreen: " + fullscreen);
		sb.append("\n");
		return sb.toString();
	}
}
