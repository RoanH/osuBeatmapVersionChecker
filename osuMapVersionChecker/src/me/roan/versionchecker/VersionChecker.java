package me.roan.versionchecker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.google.gson.Gson;

import me.roan.infinity.graphics.ui.RListUI;
import me.roan.infinity.graphics.ui.RListUI.ListRenderable;
import me.roan.versionchecker.FileManager.BeatmapItem;

public class VersionChecker {
	
	public static File OSUDIR = new File("D://osu!");
	private static final Gson gson = new Gson();
	protected static Queue<Runnable> updateQueue = new ConcurrentLinkedQueue<Runnable>();
	private static final JList<ListRenderable> beatmaps = new JList<ListRenderable>(FileManager.getBeatmaps());
	
	public static void main(String[] args){
		try {
			Database.readDatabase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileManager.init();
		createGUI();
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->{
			updateQueue.poll().run();
			beatmaps.repaint();
		}, 0, 2, TimeUnit.SECONDS);//TODO change
	}
	
	public static void createGUI(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
		}
		JFrame frame = new JFrame();
		JPanel content = new JPanel(new BorderLayout());
		JTabbedPane categories = new JTabbedPane();
		
		BeatmapItemMouseListener listener = new BeatmapItemMouseListener();
		
		beatmaps.addMouseListener(listener);
		beatmaps.setUI(new RListUI());
		((RListUI)beatmaps.getUI()).setBackground(Color.LIGHT_GRAY.brighter());
		beatmaps.setFixedCellHeight(16 * 3);
		beatmaps.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		categories.addTab("All unranked beatmaps (" + beatmaps.getModel().getSize() + ")", new JScrollPane(beatmaps));
		
		categories.addTab("State changed", new JLabel("TODO"));
		categories.addTab("Update available", new JLabel("TODO"));
				
		content.add(categories, BorderLayout.CENTER);
		
		frame.add(content);
		frame.setMinimumSize(new Dimension(760, 400));
		frame.setSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void mainl(String[] args){
		String APIKEY = args[0];
		try {
			Database.readDatabase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileManager.init();
		for(BeatmapData data : Database.maps){
			if(!(data.status == 4 || data.status == 5)){//0=unknow,4=ranked,5=approved,7=loved?,2=graveyard/pending,1=not submited>
				System.out.println(data.status + " " + data.title + " " + data.diff);
			}
		}
		//String hash = Database.maps.get(0).hash;
		//String req = getPage("https://osu.ppy.sh/api/get_beatmaps?k=" + APIKEY + "&h=" + hash);

		//System.out.println(req);
		
		//BeatmapData map = gson.fromJson(req.substring(1, req.length() - 1), BeatmapData.class);
		
		System.out.println(Database.maps.size());
	}
	
	protected static BeatmapData checkState(BeatmapData local){
		return local;//TODO implement
	}
	
	/**
	 * Used to make API calls. This method
	 * gets the JSON string returned
	 * by API calls
	 * @param url The API call 'url' to make
	 * @return The JSON string returned
	 */
	private static final String getPage(String url){
		try{
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(10000);
			
		    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		    String line = reader.readLine();
		    reader.close();
		    return line;
		}catch(Exception e){
			return null;
		}
	}
	
	private static final class BeatmapItemMouseListener implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent e) {
			@SuppressWarnings("unchecked")
			ListRenderable map = ((JList<ListRenderable>)e.getSource()).getSelectedValue();
			if(map != null){
				((BeatmapItem)map).onMouseEvent(e);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {					
		}

		@Override
		public void mouseReleased(MouseEvent e) {				
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	}
}
