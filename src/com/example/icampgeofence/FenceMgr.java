package com.example.icampgeofence;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
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
	private static final String FENCES_FILE_NAME = "fences.json";
	private static final String DEFAULT_FENCES_FILE_NAME = "default_fences.json";

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
		add(readDefaultFences());
	}

	public void add(Fence fence) {
		if (!fences.contains(fence)) {
			// add to list
			fences.add(fence);
		}

		// sync json
		writeFences();
	}

	public void add(List<Fence> fenceList) {
		for (Fence f : fenceList) {
			if (!fences.contains(f)) {
				// add to list
				fences.add(f);
			}
		}

		// sync json
		writeFences();
	}

	public void delete(Fence fence) {
		// remove from list
		fences.remove(fence);

		// sync json
		writeFences();
	}
	
	public void delete(List<Fence> fenceList) {
		fences.removeAll(fenceList);

		// sync json
		writeFences();
	}

	public void deleteAll() {
		fences.clear();
		writeFences();
	}

	public List<Fence> readDefaultFences() {
		return readFencesFromStream(appContext.getResources().openRawResource(R.raw.default_fences));
	}
	
	public List<Fence> readFences(String fileName) throws FileNotFoundException {
		return readFencesFromStream(appContext.openFileInput(fileName));
	}

	public List<Fence> readFencesFromStream(InputStream is) {
		List<Fence> fenceList = null;

		BufferedReader br = null; 
		try {
			InputStreamReader isr = new InputStreamReader(is);
			br = new BufferedReader(isr);

			Type type = new TypeToken<List<Fence>>(){}.getType();
			fenceList = new Gson().fromJson(br, type);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { br.close(); } catch (Exception e) { }
		}
		
		return fenceList;
	}

	public void writeFences() {
		try {
			FileOutputStream os = appContext.openFileOutput(FENCES_FILE_NAME, Context.MODE_PRIVATE);
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

	public Fence getFenceById(String id) {
		Fence match = null;
		for (Fence f : fences) {
			if (f.getId().equals(id)) {
				match = f;
			}
		}
		return match;
	}
}
