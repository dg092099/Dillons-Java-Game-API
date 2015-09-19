package dillon.gameAPI.utils;

import java.util.ArrayList;

import dillon.gameAPI.errors.GeneralRuntimeException;

/**
 * This class contains miscellaneous methods that are more or less utilities for
 * the engine.
 * 
 * @author Dillon - Github dg092099
 *
 */
public class MainUtilities {
	/**
	 * This method will get the inverse of the coordinate according to the
	 * image's height. This is used to invert the x position of the camera
	 * because the image is rendered from the top left corner. This makes it so
	 * that when it is rendered, it is in the correct position.
	 * 
	 * @param x
	 *            The value to invert
	 * @param height
	 *            The height of the image to invert around.
	 * @return The inverted number.
	 */
	public static int getInverse(int x, int height) {
		int half = height / 2;
		int diff = half - x;
		return half + diff;
	}

	private static final ArrayList<Runnable> queue = new ArrayList<Runnable>(); // Things
																				// to
																				// run
																				// in
																				// sync
																				// with
																				// the
																				// engine.

	/**
	 * Sets the action to run on time with the game engine.
	 * 
	 * @param r
	 *            The action
	 */
	public static synchronized void executeWithEngine(Runnable r) {
		queue.add(r);
	}

	/**
	 * Executes the queue. Only works when invoked by the canvas controller.
	 */
	public static synchronized void executeQueue() {
		if (!(new Throwable().getStackTrace()[1].getClassName()
				.equalsIgnoreCase("dillon.gameapi.core.canvascontroller"))) {
			throw new GeneralRuntimeException("Illegal access, must be done with the canvas controller.");
		}
		if (queue.size() == 0)
			return;
		for (int i = 0; i < queue.size(); i++) {
			queue.get(i).run();
		}
		queue.clear();
	}

	/**
	 * Returns the blue in a rgb value
	 * 
	 * @param rgb
	 *            The rgb value
	 * @return the blue.
	 */
	public static int getBlue(int rgb) {
		return rgb & 0xFF;
	}

	/**
	 * Returns the red in a rgb value
	 * 
	 * @param rgb
	 *            The rgb value
	 * @return the red.
	 */
	public static int getRed(int rgb) {
		return (rgb >> 16) & 0xFF;
	}

	/**
	 * Returns the green in a rgb value
	 * 
	 * @param rgb
	 *            The rgb value
	 * @return the green.
	 */
	public static int getGreen(int rgb) {
		return (rgb >> 8) & 0xFF;
	}
}
