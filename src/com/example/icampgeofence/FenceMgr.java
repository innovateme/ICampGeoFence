package com.example.icampgeofence;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Manages a list of {@link Fence}s.
 */
public class FenceMgr {
	private static final String JSON_FILE_NAME = "fences.json";

	private static FenceMgr instance;

	private final Context appContext;
	private final List<Fence> fences = new ArrayList<Fence>();

	public static void init(Context ctx) {
		if (instance == null) {
			instance = new FenceMgr(ctx.getApplicationContext());
		}
	}

	public static FenceMgr getDefault() {
		return instance;
	}

	public FenceMgr(Context ctx) {
		appContext = ctx;
		readFences();
	}

	public void add(Fence fence) {
		if (!fences.contains(fence)) {
			// add to list
			fences.add(fence);
		}

		// sync json
		writeFences();

		// TODO: call google create API
	}

	public void delete(Fence fence) {
		// remove from list
		fences.remove(fence);

		// sync json
		writeFences();

		// TODO: call google destroy API
	}
	
	public void deleteAll() {
		fences.clear();
		writeFences();
	}

	public void readFences() {
		fences.clear();

		BufferedReader br = null; 
		try {
			FileInputStream is = appContext.openFileInput(JSON_FILE_NAME);
			InputStreamReader isr = new InputStreamReader(is);
			br = new BufferedReader(isr);

			Type type = new TypeToken<List<Fence>>(){}.getType();
			List<Fence> fenceList = new Gson().fromJson(br, type);
			fences.addAll(fenceList);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { br.close(); } catch (Exception e) { }
		}
	}

	public void writeFences() {
		try {
			FileOutputStream os = appContext.openFileOutput(JSON_FILE_NAME, Context.MODE_PRIVATE);
			os.write(new Gson().toJson(fences).getBytes());
			os.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Fence> getFences() {
		return fences;
	}
}
