package me.roan.versionchecker;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;

/**
 * Class that manages all the list
 * models for the beatmaps
 * @author RoanH
 */
public class FileManager{
	/**
	 * List model for all that unranked beatmaps
	 */
	protected static final DefaultListModel<BeatmapItem> beatmapsModel = new DefaultListModel<BeatmapItem>();
	/**
	 * List model for all the unranked beatmaps that have an update
	 */
	protected static final DefaultListModel<BeatmapItem> beatmapsUpdateModel = new DefaultListModel<BeatmapItem>();
	/**
	 * List model for all the unranked beatmaps that have a changed status
	 */
	protected static final DefaultListModel<BeatmapItem> beatmapsStateModel = new DefaultListModel<BeatmapItem>();
	/**
	 * Executor that load the beatmaps thumbnails
	 */
	private static final ExecutorService imageLoader = Executors.newSingleThreadExecutor();
	/**
	 * Cache for the loaded beatmap thumbnails
	 */
	protected static final Map<Integer, Image> icons = new HashMap<Integer, Image>();

	/**
	 * Disables the 'update' and 'don't update'
	 * buttons for all beatmap items
	 */
	protected static void disableControls(){
		for(int i = 0; i < beatmapsUpdateModel.size(); i++){
			BeatmapItem item = beatmapsUpdateModel.getElementAt(i);
			item.showControls = false;
		}
		VersionChecker.frame.repaint();
	}

	/**
	 * Submits an update task for every
	 * beatmap that's selected to be updated
	 * @see VersionChecker#updateQueue
	 */
	protected static void submitUpdateList(){
		for(int i = 0; i < beatmapsUpdateModel.size(); i++){
			BeatmapItem item = beatmapsUpdateModel.getElementAt(i);
			if(item.download != null && item.download == true){
				VersionChecker.updateQueue.add(()->{
					VersionChecker.updateBeatmap(item);
					return true;
				});
			}
		}
	}

	/**
	 * Set the selected flag for every beatmap
	 * to 'update'. If override is false maps
	 * that already have a state won't have their
	 * state forced to 'update'
	 * @param override Whether or not to update the
	 *        flag of a map if it already has one
	 */
	protected static void setSelected(boolean override){
		for(int i = 0; i < beatmapsUpdateModel.size(); i++){
			BeatmapItem item = beatmapsUpdateModel.getElementAt(i);
			if(item.download == null || override){
				if(item.download == null){
					BeatmapItem.choiceMade++;
				}
				item.download = true;
			}
		}
	}

	/**
	 * Set the selected flag for every beatmap
	 * that does not yet have a state to 'don't update'.
	 */
	protected static void setUnselected(){
		for(int i = 0; i < beatmapsUpdateModel.size(); i++){
			BeatmapItem item = beatmapsUpdateModel.getElementAt(i);
			if(item.download == null){
				BeatmapItem.choiceMade++;
				item.download = false;
			}
		}
	}

	/**
	 * Loads the beatmap icon for the given
	 * map into the thumbnail cache
	 * @param item The beatmap to load the icon for
	 * @see #icons
	 */
	protected static synchronized void getBeatmapIcon(BeatmapItem item){
		if(!icons.containsKey(item.local.setid)){
			icons.put(item.local.setid, null);
			imageLoader.submit(()->{
				try{
					File f = new File(VersionChecker.OSUDIR + File.separator + "Data" + File.separator + "bt" + File.separator + item.local.setid + ".jpg");
					if(!f.exists()){
						f = new File(VersionChecker.OSUDIR + File.separator + "Data" + File.separator + "bt" + File.separator + item.local.setid + "l.jpg");
					}
					if(f.exists()){
						icons.put(item.local.setid, ImageIO.read(f).getScaledInstance(-1, 16 * 2 + 8, Image.SCALE_SMOOTH));
					}
				}catch(IOException | NullPointerException | ArrayIndexOutOfBoundsException e){
					//This error is not very important, it just means that osu! hasn't cached
					//the image for this beatmap yet
				}
			});
		}
	}
}
