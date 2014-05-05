package com.hearthstonegeek.webview;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundPlay {

	private SoundPool sp;
	private HashMap<String, Integer> spMap = new HashMap<String, Integer>();
	private Context context;
	float audioMaxVolumn, audioCurrentVolumn, volumnRatio;

	public SoundPlay(Context context) {
		sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		this.context = context;

		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		volumnRatio = audioCurrentVolumn / audioMaxVolumn;
	}

	public void loadSound(String fileName) {
		try {
			spMap.put(fileName, sp.load(context.getAssets().openFd(fileName), 1));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void palySound(String sound) {
		Integer id = spMap.get(sound);
		if (id != null) {
			sp.play(spMap.get(sound), volumnRatio, volumnRatio, 1, 0, 1);
		}
	}

}
