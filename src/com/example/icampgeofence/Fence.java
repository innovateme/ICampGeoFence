package com.example.icampgeofence;

import com.google.android.gms.location.Geofence;

/**
 * Data model for a circular geofence.
 */
public class Fence implements Comparable<Fence> {
	public static final int DEFAULT_DWELL_TIME_SEC = 30;
	
	private final String id;
	private final String name;
	private final double lat;
	private final double lon;
	private final float radius;
	private final long duration;
	// TODO: automatic resetting?
	private final int transition;
	private final int dwellTime;
	private transient boolean triggered = false;

	/**
	 * Creates a new Fence with default dwell time.
	 */
	public Fence(
			String name,
			double latitude,
			double longitude,
			float radius,
			long duration,
			int transition) {

		this(name, latitude, longitude, radius, duration, transition, DEFAULT_DWELL_TIME_SEC);
	}

	/**
	 * @param requestId The Geofence's request ID
	 * @param latitude Latitude of the Geofence's center.
	 * @param longitude Longitude of the Geofence's center.
	 * @param radius Radius of the geofence circle.
	 * @param duration Geofence expiration duration
	 * @param transition Type of Geofence transition.
	 * @param dwell time in seconds - used only for dwell type
	 */
	public Fence(
			String name,
			double latitude,
			double longitude,
			float radius,
			long duration,
			int transition,
			int dwellTime) {

		id = name.toLowerCase();
		this.name = name; 
		lat = latitude;
		lon = longitude;
		this.radius = radius;
		this.duration = duration;
		this.transition = transition;
		this.dwellTime = dwellTime;
	}

	@Override
	public String toString() {
		return String.format("%s: %.4f,  %.4f,  %,.0f m", name, lat, lon, radius);
	}

	public long getDuration() {
		return duration;
	}

	public int getTransition() {
		return transition;
	}

	public int getDwellTime() {
		return dwellTime;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public float getRadius() {
		return radius;
	}

	public boolean isTriggered() {
		return triggered;
	}

	public void setTriggered(boolean state) {
		triggered = state;
	}

	/**
	 * Creates a Geofence instance from a Fence.
	 *
	 * @return A Geofence instance
	 */
	public Geofence asGeofence() {
		// Build a new Geofence object
		return new Geofence.Builder()
		.setRequestId(id)
		.setTransitionTypes(transition)
		.setCircularRegion(lat, lon, radius)
		.setExpirationDuration(duration)
		.setLoiteringDelay(dwellTime * 1000)
		.setNotificationResponsiveness(5000)
		.build();
	}

	@Override
	public int compareTo(Fence that) {
		return this.id.equalsIgnoreCase(that.getId()) ? 1 : 0;
	}
}
