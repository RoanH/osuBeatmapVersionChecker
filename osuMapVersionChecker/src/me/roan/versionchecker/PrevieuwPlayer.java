package me.roan.versionchecker;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javazoom.jl.player.advanced.AdvancedPlayer;

/**
 * Simple class to play beatmap song previews
 * @author RoanH
 */
public class PrevieuwPlayer {
	/**
	 * Audio player
	 */
	private static AdvancedPlayer player;
	/**
	 * Audio executor
	 */
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	/**
	 * Plays the preview
	 * for the given beatmap
	 * @param data The beatmap to play
	 */
	public static void playFile(BeatmapItem data){
		stop();
		executor.submit(()->{
			try {				
				player = new AdvancedPlayer(new FileInputStream(new File(data.file, data.local.audiofile)));
				player.setLineGain(-20F);
				player.playSection(data.local.previeuw_time, 30000);
			} catch (Throwable t) {
				//Not a crucial operation
			}
			data.cancelPlayingState();
		});
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
	}
	
	/**
	 * Stops the song currently
	 * being played
	 */
	public static void stop(){
		if(player != null){
			player.close();
			player = null;
		}
	}
}
