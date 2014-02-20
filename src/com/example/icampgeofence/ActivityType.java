package com.example.icampgeofence;

import com.google.android.gms.location.DetectedActivity;

public enum ActivityType {
	ON_FOOT("On foot", DetectedActivity.ON_FOOT),
	IN_VEHICLE("In vehicle", DetectedActivity.IN_VEHICLE),
	ON_BICYCLE("On bicycle", DetectedActivity.ON_BICYCLE),
	STILL("Still", DetectedActivity.STILL),
	TILTING("Tilting", DetectedActivity.TILTING),
	UNKNOWN("Unknown", DetectedActivity.UNKNOWN);
	
	public final String displayName;
	public final int typeId;

	ActivityType(String name, int type) {
		displayName = name;
		typeId = type;
	}
	
	@Override
	public String toString() {
		return displayName;
	}

	public static ActivityType fromTypeId(int typeId) {
		ActivityType match = null;
		for (ActivityType at : values()) {
			if (at.typeId == typeId) {
				match = at;
			}
		}
		return match;
	}
}
