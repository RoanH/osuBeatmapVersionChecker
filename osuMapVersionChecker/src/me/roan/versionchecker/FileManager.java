package me.roan.versionchecker;

import java.io.File;
import javax.swing.DefaultListModel;

import me.roan.infinity.graphics.ui.RListUI.ListRenderable;
import me.roan.versionchecker.BeatmapData.LocalBeatmapData;

public class FileManager{

	protected static final DefaultListModel<BeatmapItem> beatmapsModel = new DefaultListModel<BeatmapItem>();
	protected static final DefaultListModel<BeatmapItem> beatmapsUpdateModel = new DefaultListModel<BeatmapItem>();
	protected static final DefaultListModel<BeatmapItem> beatmapsStateModel = new DefaultListModel<BeatmapItem>();

	protected static void init(){
		addAll(beatmapsModel, parseB());
	}

	private static final void addAll(DefaultListModel<BeatmapItem> model, BeatmapItem[] items){
		for(BeatmapItem p : items){
			model.addElement(p);
		}
	}
	
	protected static void setSelected(boolean override){
		for(int i = 0; i < beatmapsUpdateModel.size(); i++){
			BeatmapItem item = beatmapsUpdateModel.getElementAt(i);
			if(item.download == null || override){
				item.download = true;
			}
		}
	}
	
	protected static void setUnselected(){
		for(int i = 0; i < beatmapsUpdateModel.size(); i++){
			BeatmapItem item = beatmapsUpdateModel.getElementAt(i);
			if(item.download == null){
				item.download = false;
			}
		}
	}
	
	private static BeatmapItem[] parseB(){
		BeatmapItem[] panels = new BeatmapItem[Database.maps.size()];
		int i = 0;
		for(LocalBeatmapData data : Database.maps){
			BeatmapItem local = new BeatmapItem(new File(VersionChecker.OSUDIR, "Songs" + File.separator + data.songfolder), data);
			panels[i] = local;
			VersionChecker.updateQueue.add(()->{
				System.out.println("Execute state check");
				local.setOnlineData(VersionChecker.checkState(data));
				System.out.println("Online data fetched");
				if(local.online == null){
					return false;
				}
				if(local.mapChanged()){
					beatmapsUpdateModel.addElement(local);
					VersionChecker.categories.setTitleAt(2, "Update available (" + beatmapsUpdateModel.size() + ")");
				}
				if(local.stateChanged()){
					beatmapsStateModel.addElement(local);
					VersionChecker.categories.setTitleAt(1, "State changed (" + beatmapsStateModel.size() + ")");
				}
				System.out.println("State check done");
				return true;
			});
			i++;
		}
		return panels;
	}
}
