package dillon.gameAPI.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import dillon.gameAPI.event.EEHandler;
import dillon.gameAPI.event.EventSystem;
import dillon.gameAPI.event.RenderEvent;
import dillon.gameAPI.event.TickEvent;
import dillon.gameAPI.scroller.ScrollManager;

/**
 * This class stores the position, direction and sprite of each entity.
 * 
 * @author Dillon - Github dg092099
 */
public class Entity implements Serializable {
	private static final long serialVersionUID = 1176042239171972455L;
	private static BufferedImage spr; // The sprite object for this entity.

	/**
	 * This method creates an entity.
	 * 
	 * @param sprite
	 *            The image to use.
	 */
	public Entity(Image sprite) {
		spr = (BufferedImage) sprite;
		x = 0;
		y = 0;
		dx = 0;
		dy = 0;
		EventSystem.addHandler(new EEHandler<TickEvent>() {
			@Override
			public void handle(TickEvent T) {
				if (!checkCollisionWithPos(x + dx, y + dy)) {
					x += dx;
					y += dy;
				}
				if (gravity) {
					if (!checkCollisionWithPos(x, y + fallspeed)) {
						if (!gravityOverride)
							y += fallspeed;
					} else {
						if (gravityOverride)
							gravityOverride = false;
					}
				}
				if (jumping) {
					System.out.println("Jump pix count: " + jumpPixCount);
					System.out.println("Max Jump Height: " + jumpHeight);
					if (jumpPixCount >= jumpHeight) {
						jumping = false;
						jumpPixCount = 0;
						setDirection(dx, 2);
					} else {
						jumpPixCount += 2;
						y += 2;
					}
				}
				if (autoMode == 1) { // Auto pilot is on.
					if (counter == 0) { // Limiter
						counter = autoLimit;
						double diffX = x - target.x; // The difference between
														// the two x values.
						double diffY = y - target.y; // The difference between
														// the two y values.
						double angle = Math.atan2(diffX, diffY);
						dx = Math.sin(angle * autoMultiplier);
						dy = Math.cos(angle * autoMultiplier);
					} else {
						counter--;
					}
				}
			}
		});
		EventSystem.addHandler(new EEHandler<RenderEvent>() {
			@Override
			public void handle(RenderEvent evt) {
				Graphics2D graphics = (Graphics2D) evt.getMetadata()[0];
				graphics.drawImage(spr, (int) x, (int) y, null);
				if (showHealth) {
					graphics.setColor(Color.RED);
					graphics.fillRect((int) x - 30, (int) y - 20, 100, 5);
					graphics.setColor(Color.GREEN);
					graphics.fillRect((int) x - 30, (int) y - 20, (int) (health / MaxHealth * 100), 5);
				}
			}
		});
	}

	private double x, y; // The entity's position values.
	private transient double dx, dy; // The entity's velocity values.

	/**
	 * Sets the x position of the entity.
	 * 
	 * @param X
	 *            the position
	 */
	public void setX(int X) {
		x = X;
	}

	/**
	 * Gets the x position.
	 * 
	 * @return X
	 */
	public double getX() {
		return x;
	}

	/**
	 * Sets the Y position.
	 * 
	 * @param Y
	 *            The y position
	 */
	public void setY(int Y) {
		y = Y;
	}

	/**
	 * Gets the Y position.
	 * 
	 * @return Y
	 */
	public double getY() {
		return y;
	}

	/**
	 * This sets the direction based on a velocity x and y.
	 * 
	 * @param DX
	 *            Directional x
	 * @param DY
	 *            Directional Y
	 */
	public void setDirection(double DX, double DY) {
		dx = DX;
		dy = DY;
	}

	/**
	 * This sets the direction based on an angle.
	 * 
	 * @param angle
	 *            The angle
	 */
	public void setDirection(double angle) {
		dx = Math.sin(angle);
		dy = Math.cos(angle);
	}

	/**
	 * Gets the current direction.
	 * 
	 * @return array: [x direction, y direction]
	 */
	public int[] getDirection() {
		return new int[] { (int) dx, (int) dy };
	}

	/**
	 * Sets the current sprite
	 * 
	 * @param img
	 *            The sprite.
	 */
	public void setSprite(Image img) {
		spr = (BufferedImage) img;
	}

	public static final int AUTOPILOT_DIRECT = 1; // Constant: autopilot type
													// direct
	private int autoMode = -1; // The autopilot mode.
	private Entity target; // The target entity for the autopilot.
	private int autoLimit; // The limit to how many frames must pass until path
							// is recalculated.
	private int autoMultiplier; // The speed that it should go.

	/**
	 * Sets the sprite to target and follow an entity
	 * 
	 * @param mode
	 *            How it should move
	 * @param target
	 *            The entity it is targeting.
	 * @param limit
	 *            How many updates before recalculating the directions.
	 * @param speedMultiplier
	 *            A value needed to normalize the angles.
	 */
	public void setAutoPilot(int mode, Entity target, int limit, int speedMultiplier) {
		autoMode = mode;
		this.target = target;
		autoLimit = limit;
		autoMultiplier = speedMultiplier;
	}

	/**
	 * Shuts off the autopilot for this entity.
	 */
	public void unsetAutoPilot() {
		autoMode = 0;
		counter = 0;
	}

	private Double health = 0D; // The entity's health

	/**
	 * Sets the current health of an entity.
	 * 
	 * @param h
	 *            health.
	 */
	public void setHealth(Double h) {
		health = h;
	}

	/**
	 * Gets the current health.
	 * 
	 * @return health
	 */
	public Double getHealth() {
		return health;
	}

	private int MaxHealth = 100; // The entity's maximum health.

	/**
	 * Sets the maximum health.
	 * 
	 * @param max
	 *            Maximum health.
	 */
	public void setMaxHealth(int max) {
		MaxHealth = max;
	}

	/**
	 * Gets the maximum health.
	 * 
	 * @return The maximum health.
	 */
	public int getMaxHealth() {
		return MaxHealth;
	}

	/**
	 * Makes the entity take damage.
	 * 
	 * @param dm
	 *            The damage.
	 */
	public void takeDamage(int dm) {
		health -= dm;
	}

	private int counter = 0; // A counter for the autopilot.

	private boolean showHealth = false; // Whether the health should be
										// displayed.

	/**
	 * Sets if the health bar should be rendered.
	 * 
	 * @param b
	 *            Health bar
	 */
	public void setShowHealth(boolean b) {
		showHealth = b;
	}

	/**
	 * This method will check if the player is colliding with anything.
	 * 
	 * @return if it is colliding with something
	 */
	public boolean checkCollision() {
		return ScrollManager.getCollisionAny(x, y, spr.getWidth(), spr.getHeight());
	}

	/**
	 * Finds if the entity is colliding with a tile.
	 * 
	 * @param posx
	 *            tile's x position
	 * @param posy
	 *            tile's y position
	 * @return colliding
	 */
	private boolean checkCollisionWithPos(double posx, double posy) {
		return ScrollManager.getCollisionAny(posx, posy, spr.getWidth(), spr.getHeight());
	}

	private boolean gravity = false; // If gravity affects this entity.

	/**
	 * This method will set weather gravity should operate on entity.
	 * 
	 * @param g
	 *            whether it should or not.
	 */
	public void setGravity(boolean g) {
		gravity = g;
	}

	private int fallspeed = 5; // How quickly the entity should fall.

	/**
	 * This is for the gravity, it sets how fast the entity falls.
	 * 
	 * @param speed
	 *            the speed.
	 */
	public void setFallSpeed(int speed) {
		fallspeed = speed;
	}

	private boolean gravityOverride = false; // Temporarily shuts off the
												// gravity for a jump.

	/**
	 * This method will disable the gravity until the entity hits a surface.
	 */
	public void setGravityJumpOverride() {
		gravityOverride = true;
	}

	private boolean jumping = false; // Indicates if the entity is jumping.
	private int jumpHeight = 1; // The entity's jumping height.
	private int jumpPixCount = 0; // How many pixels has it gone.

	/**
	 * This method will send the entity upwards.
	 * 
	 * @param height
	 *            How high to move the entity.
	 */
	public void jump(int height) {
		jumpHeight = height;
		jumping = true;
		setGravityJumpOverride();
	}
}
