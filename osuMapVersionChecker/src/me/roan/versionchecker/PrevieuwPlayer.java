package me.roan.versionchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

public class PrevieuwPlayer {
	
	private static AdvancedPlayer player;
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	public static void playFile(BeatmapItem data){
		if(player != null){
			player.close();
		}
		executor.submit(()->{
			try {				
				player = new AdvancedPlayer(new FileInputStream(new File(data.file, data.local.audiofile)));
				player.setLineGain(-20F);
				player.playSection(data.local.previeuw_time, 30000);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JavaLayerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			data.cancelPlayingState();
		});
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
	}
	
	public static void stop(){
		if(player != null){
			player.close();
			player = null;
		}
	}
}
