package com.example.icampgeofence;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.location.Geofence;

public class FenceListAdapter extends ArrayAdapter<Fence> {
	private final Context ctx;
	private final List<Fence> items;

	public FenceListAdapter(Context context, List<Fence> itemList) {
		super(context, R.layout.fence_list_item, itemList);
		this.ctx = context;
		this.items = itemList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater =
				(LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.fence_list_item, parent, false);
		Fence f = items.get(position);

		if (f.isTriggered()) {
			rowView.setBackgroundResource(R.drawable.border_ui);
		}

		TextView nameView = (TextView) rowView.findViewById(R.id.fence_list_item_name);
		nameView.setText(f.getName());

		String triggerFmt = "%.4f, %.4f, %,.0f meters";
		if (f.getTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {
			triggerFmt = "Pass within %,.0f meters of %.4f, %.4f";
		}
		if (f.getTransition() == Geofence.GEOFENCE_TRANSITION_EXIT) {
			triggerFmt = "Travel more than %,.0f meters from %.4f, %.4f";
		}
		if (f.getTransition() == Geofence.GEOFENCE_TRANSITION_DWELL) {
			triggerFmt = "Dwell within %,.0f meters of %.4f, %.4f";
		}
		if (f.getTransition() == (Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)) {
			triggerFmt = "Enter or Exit within %,.0f meters of %.4f, %.4f";
		}
		String trigger = String.format(triggerFmt, f.getRadius(), f.getLat(), f.getLon());

		TextView triggerView = (TextView) rowView.findViewById(R.id.fence_list_item_trigger);
		triggerView.setText(trigger);

		return rowView;
	}
}
