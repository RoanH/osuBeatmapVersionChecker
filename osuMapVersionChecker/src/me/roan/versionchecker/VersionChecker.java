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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.google.gson.Gson;

import me.roan.infinity.graphics.ui.RListUI;
import me.roan.infinity.graphics.ui.RListUI.ListRenderable;
import me.roan.versionchecker.BeatmapData.LocalBeatmapData;
import me.roan.versionchecker.BeatmapData.OnlineBeatmapData;

public class VersionChecker {
	
	//https://osu.ppy.sh/osu/874599
	
	public static File OSUDIR = new File("C://Users//RoanH//Documents//osu!");
	private static final Gson gson = new Gson();
	protected static Queue<Callable<Boolean>> updateQueue = new ConcurrentLinkedQueue<Callable<Boolean>>();
	private static final JList<ListRenderable> beatmaps = new JList<ListRenderable>(FileManager.beatmapsModel);
	private static final JList<ListRenderable> beatmapsUpdate = new JList<ListRenderable>(FileManager.beatmapsUpdateModel);
	private static final JList<ListRenderable> beatmapsState = new JList<ListRenderable>(FileManager.beatmapsStateModel);
	protected static String APIKEY;
	protected static JTabbedPane categories;
	protected static final JFrame frame = new JFrame();
	
	private static boolean backup = true;
	
	public static void maint(String[] args){
		System.out.println(getPage("https://osu.ppy.sh/osu/874599"));
	}

	
	public static void main(String[] args){
		APIKEY = args[0];
		try {
			Database.readDatabase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileManager.init();
		createGUI();
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->{
			try{
				if(!updateQueue.isEmpty()){
					Callable<Boolean> task = updateQueue.poll();
					if(!task.call()){
						updateQueue.add(task);
					}else{
						beatmaps.repaint();
					}
				}else{
					Thread.sleep(Long.MAX_VALUE);
				}
				System.out.println("Queue size: " + updateQueue.size());
			}catch(Throwable t){
				System.out.println("Error");
				t.printStackTrace();
			}
		}, 0, 1, TimeUnit.SECONDS);//TODO change
	}
	
	public static void createGUI(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
		}

		JPanel content = new JPanel(new BorderLayout());
		categories = new JTabbedPane();
		
		BeatmapItemMouseListener listener = new BeatmapItemMouseListener();
		
		beatmaps.addMouseListener(listener);
		beatmaps.setUI(new RListUI());
		((RListUI)beatmaps.getUI()).setBackground(Color.LIGHT_GRAY.brighter());
		beatmaps.setFixedCellHeight(16 * 3);
		beatmaps.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		beatmapsState.addMouseListener(listener);
		beatmapsState.setUI(new RListUI());
		((RListUI)beatmapsState.getUI()).setBackground(Color.LIGHT_GRAY.brighter());
		beatmapsState.setFixedCellHeight(16 * 3);
		beatmapsState.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		beatmapsUpdate.addMouseListener(listener);
		beatmapsUpdate.setUI(new RListUI());
		((RListUI)beatmapsUpdate.getUI()).setBackground(Color.LIGHT_GRAY.brighter());
		beatmapsUpdate.setFixedCellHeight(16 * 3);
		beatmapsUpdate.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		categories.addTab("All unranked beatmaps (" + beatmaps.getModel().getSize() + ")", new JScrollPane(beatmaps));
		
		categories.addTab("State changed (0)", new JScrollPane(beatmapsState));
		categories.addTab("Update available (0)", new JScrollPane(beatmapsUpdate));
				
		content.add(categories, BorderLayout.CENTER);
		
		JPanel header = new JPanel(new BorderLayout());
		header.setBorder(BorderFactory.createEtchedBorder());
		JButton start = new JButton("Start");
		JButton help = new JButton("Help");
		JSpinner pollRate = new JSpinner(new SpinnerNumberModel(30, 1, 1400, 1));
		JLabel time = new JLabel("Estimated time: ");
		header.add(help, BorderLayout.LINE_END);
		
		
		
		content.add(header, BorderLayout.PAGE_START);
		
		frame.add(content);
		frame.setMinimumSize(new Dimension(760, 400));
		frame.setSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	protected static OnlineBeatmapData checkState(LocalBeatmapData local){
		System.out.println("Start request");
		String req = getPage("https://osu.ppy.sh/api/get_beatmaps?k=" + APIKEY + "&h=" + local.hash);
		System.out.println(req);
		if(req == null){
			return null;
		}
		if(req.equals("[]")){
			System.out.println(local.title + " " + local.diff);
			OnlineBeatmapData d = new OnlineBeatmapData();
			d.generated = true;
			return d;
		}
		OnlineBeatmapData data = null;
		try{
			data = gson.fromJson(req.substring(1, req.length() - 1), OnlineBeatmapData.class);
		}catch(Throwable t){
			t.printStackTrace();
		}
		//System.out.println("data: " + data);
		//System.out.println(local.status + " " + data.approved);
		return data;
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
			con.setConnectTimeout(500);
			
		    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		    String line = reader.readLine();
		    reader.close();
		    return line;
		}catch(Exception e){
			System.out.println("Page error");
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
	
	private static final void updateBeatmap(BeatmapData local){
		
	}
}
