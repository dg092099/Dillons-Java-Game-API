package dillon.gameAPI.gui;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import dillon.gameAPI.event.EEHandler;
import dillon.gameAPI.event.EventSystem;
import dillon.gameAPI.event.KeyEngineEvent;
import dillon.gameAPI.event.MouseEngineEvent;
import dillon.gameAPI.event.RenderEvent;
import dillon.gameAPI.event.UpdateEvent;
import dillon.gameAPI.security.RequestedAction;
import dillon.gameAPI.security.SecurityKey;
import dillon.gameAPI.security.SecuritySystem;

/**
 * This class regulates all on-screen objects that are not entities, or a level.
 *
 * @since 1.11
 * @author Dillon - Github dg092099
 *
 */
public class GuiSystem {
	private static ArrayList<GuiComponent> components = new ArrayList<GuiComponent>();
	private static int lowestIndex = Integer.MAX_VALUE, highestIndex = Integer.MIN_VALUE;
	private static int activeGuiComponent = -1;

	/**
	 * This shows a gui on the screen.
	 *
	 * @param gc
	 *            The component.
	 * @param k
	 *            The security key.
	 */
	public static void startGui(GuiComponent gc, SecurityKey k) {
		SecuritySystem.checkPermission(k, RequestedAction.SHOW_GUI); // Security
																		// Check
		if (gc == null) {
			throw new IllegalArgumentException("The gui component must not be null.");
		}
		components.add(gc);
		// Adjust zIndex.
		int zIndex = gc.getZIndex();
		if (zIndex < lowestIndex) {
			lowestIndex = zIndex;
		}
		if (zIndex > highestIndex) {
			highestIndex = zIndex;
		}
		activeGuiComponent = components.indexOf(gc);
	}

	/**
	 * This removes a gui component from the screen.
	 *
	 * @param gc
	 *            The gui component.
	 * @param k
	 *            The security key.
	 */
	public static void removeGui(GuiComponent gc, SecurityKey k) {
		SecuritySystem.checkPermission(k, RequestedAction.SHOW_GUI);
		if (gc == null) {
			throw new IllegalArgumentException("The gui must not be null.");
		}
		components.remove(gc);
		// Adjust zIndex
		lowestIndex = Integer.MAX_VALUE;
		highestIndex = Integer.MIN_VALUE;
		for (GuiComponent comp : components) {
			if (comp.getZIndex() > highestIndex) {
				highestIndex = comp.getZIndex();
				activeGuiComponent = components.indexOf(comp);
			}
		}
		if (components.isEmpty()) {
			activeGuiComponent = -1;
		}
	}

	/**
	 * Get the component that has the mouse control.
	 * 
	 * @return A component.
	 */
	public static int getActiveComponent() {
		return activeGuiComponent;
	}

	/**
	 * Instantiates and sets up the GUI system.
	 * 
	 * @param k
	 *            A security key
	 */
	public GuiSystem(SecurityKey k) {
		EventSystem.addHandler(new EEHandler<RenderEvent>() {
			@Override
			public void handle(RenderEvent evt) {
				Graphics2D g = evt.getGraphics();
				for (GuiComponent comp : components) {
					// Render each component
					comp.render(g);
				}
			}

			@Override
			public int getPriority() {
				return 0;
			}
		}, k);
		EventSystem.addHandler(new EEHandler<UpdateEvent>() {
			@Override
			public void handle(UpdateEvent evt) {
				for (GuiComponent comp : components) {
					// Update each component.
					comp.onUpdate();
				}
			}

			@Override
			public int getPriority() {
				return 0;
			}
		}, k);
		EventSystem.addHandler(new EEHandler<MouseEngineEvent>() {
			@Override
			public void handle(MouseEngineEvent evt) {
				if (evt.getMouseMode() == MouseEngineEvent.MouseMode.HOLD) {
					if (evt.getMouseButton() == MouseEngineEvent.MouseButton.LEFT) {
						for (GuiComponent comp : components) {
							comp.onMouseClickLeft(evt.getLocation().getX(), evt.getLocation().getY());
						}
					}
				}
				if (evt.getMouseMode() == MouseEngineEvent.MouseMode.CLICK) {
					if (evt.getMouseButton() == MouseEngineEvent.MouseButton.RIGHT) {
						for (GuiComponent comp : components) {
							comp.onMouseClickRight(evt.getLocation().getX(), evt.getLocation().getY());
						}
					}
				}
				// Change over the upper most gui.
				ArrayList<GuiComponent> candidates = new ArrayList<>();
				Point p = evt.getLocation();
				for (GuiComponent comp : components) {
					if (p.getX() >= comp.getTopLeftCorner()[0] && p.getY() >= comp.getTopLeftCorner()[1]
							&& p.getX() <= comp.getTopLeftCorner()[0] + comp.getSize()[0]
							&& p.getY() <= comp.getTopLeftCorner()[1] + comp.getSize()[1]) {
						candidates.add(comp);
					}
				}
				int lowestIndex = Integer.MAX_VALUE;
				for (GuiComponent comp : candidates) {
					if (comp.getZIndex() < lowestIndex) {
						lowestIndex = comp.getZIndex();
						activeGuiComponent = components.indexOf(comp);
						comp.bringToFront();
					}
				}
				if (candidates.size() == 0) {
					activeGuiComponent = -1;
				}
			}

			@Override
			public int getPriority() {
				return 0;
			}
		}, k);
		EventSystem.addHandler(new EEHandler<KeyEngineEvent>() {
			@Override
			public void handle(KeyEngineEvent evt) {
				KeyEvent e = evt.getKeyEvent();
				if (evt.getMode() != KeyEngineEvent.KeyMode.KEY_PRESS) {
					return;
				}
				if (activeGuiComponent != -1) {
					components.get(activeGuiComponent).onKeyPress(e);
				}
			}

			@Override
			public int getPriority() {
				return 0;
			}
		}, k);
	}

	public static String getDebug() {
		String str = "";
		str += "\n\ndillon.gameAPI.gui.GuiSystem\n";
		str += String.format("%-10s %-5s\n", "Key", "Value");
		str += String.format("%-10s %-5s\n", "---", "-----");
		str += String.format("%-10s %-5d\n", "Lowest index", lowestIndex);
		str += String.format("%-10s %-5d\n", "Highest Index", highestIndex);
		str += String.format("%-10s %-5d\n", "Active comp.", activeGuiComponent);
		str += "Components:\n";
		for (GuiComponent c : components) {
			str += c.getDebug();
			str += "\n";
		}
		return str;
	}
}
