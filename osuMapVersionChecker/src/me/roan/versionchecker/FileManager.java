package me.roan.versionchecker;

import java.io.File;
import javax.swing.DefaultListModel;

import me.roan.infinity.graphics.ui.RListUI.ListRenderable;
import me.roan.versionchecker.BeatmapData.LocalBeatmapData;

public class FileManager{

	protected static final DefaultListModel<ListRenderable> beatmapsModel = new DefaultListModel<ListRenderable>();
	protected static final DefaultListModel<ListRenderable> beatmapsUpdateModel = new DefaultListModel<ListRenderable>();
	protected static final DefaultListModel<ListRenderable> beatmapsStateModel = new DefaultListModel<ListRenderable>();

	protected static void init(){
		addAll(beatmapsModel, parseB());
	}

	private static final void addAll(DefaultListModel<ListRenderable> model, ListRenderable[] items){
		for(ListRenderable p : items){
			model.addElement(p);
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
