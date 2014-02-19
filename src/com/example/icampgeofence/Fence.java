package com.example.icampgeofence;

import com.google.android.gms.location.Geofence;

/**
 * Data model for a circular geofence.
 */
public class Fence implements Comparable<Fence> {
	private final String id;
	private final String name;
	private final double lat;
	private final double lon;
	private final float radius;
	private final long duration;
	private final int transition;

	/**
	 * @param requestId The Geofence's request ID
	 * @param latitude Latitude of the Geofence's center.
	 * @param longitude Longitude of the Geofence's center.
	 * @param radius Radius of the geofence circle.
	 * @param duration Geofence expiration duration
	 * @param transition Type of Geofence transition.
	 */
	public Fence(
			String name,
			double latitude,
			double longitude,
			float radius,
			long duration,
			int transition) {

		id = name.toLowerCase();
		this.name = name; 
		lat = latitude;
		lon = longitude;
		this.radius = radius;
		this.duration = duration;
		this.transition = transition;
	}

	@Override
	public String toString() {
		return String.format("%s: %s,  %s,  %sm", name, lat, lon, radius);
	}

	public long getDuration() {
		return duration;
	}

	public int getTransition() {
		return transition;
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
		.build();
	}

	@Override
	public int compareTo(Fence that) {
		return this.id.equalsIgnoreCase(that.getId()) ? 1 : 0;
	}
}
