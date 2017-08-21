package me.roan.versionchecker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.gson.Gson;

import me.roan.infinity.graphics.ui.RListUI;
import me.roan.infinity.graphics.ui.RListUI.ListRenderable;
import me.roan.infinity.util.Util;
import me.roan.versionchecker.BeatmapData.LocalBeatmapData;
import me.roan.versionchecker.BeatmapData.OnlineBeatmapData;

public class VersionChecker {
		
	public static File OSUDIR = new File("C://Users//RoanH//Documents//osu!");
	private static final Gson gson = new Gson();
	protected static Queue<Callable<Boolean>> updateQueue = new ConcurrentLinkedQueue<Callable<Boolean>>();
	private static final JList<BeatmapItem> beatmaps = new JList<BeatmapItem>(FileManager.beatmapsModel);
	private static final JList<BeatmapItem> beatmapsUpdate = new JList<BeatmapItem>(FileManager.beatmapsUpdateModel);
	private static final JList<BeatmapItem> beatmapsState = new JList<BeatmapItem>(FileManager.beatmapsStateModel);
	protected static String APIKEY;
	protected static JTabbedPane categories;
	protected static final JFrame frame = new JFrame();
	private static int pollRate = 30;
	
	private static boolean backup = true;
	
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
		}, 0, 1, TimeUnit.HOURS);//TODO change
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
				
		categories.setBorder(BorderFactory.createTitledBorder("Listing"));
		content.add(categories, BorderLayout.CENTER);
		
		JPanel header = new JPanel(new BorderLayout());
		JPanel checking = new JPanel(new GridLayout(3, 1));
		JButton start = new JButton("Start");
		JLabel l_rate = new JLabel("API poll rate: ");
		JSpinner s_rate = new JSpinner(new SpinnerNumberModel(pollRate, 1, 1400, 1));
		JLabel l_rate_2 = new JLabel(" requests/minute");
		JPanel rate = new JPanel(new BorderLayout());
		rate.add(l_rate, BorderLayout.LINE_START);
		rate.add(s_rate, BorderLayout.CENTER);
		rate.add(l_rate_2, BorderLayout.LINE_END);
		checking.setPreferredSize(new Dimension(220, 0));
		JLabel time = new JLabel(String.format("Estimated time: %1$.1f minutes", ((double)updateQueue.size() / (double)pollRate)));
		s_rate.addChangeListener(new ChangeListener(){
			
			private int prev = pollRate;

			@Override
			public void stateChanged(ChangeEvent arg0) {
				int newValue = (int) s_rate.getValue();
				if(newValue > 60 && prev <= 60){
					if(JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(frame, "It's advised to inform peppy when using a roll rate over 60.", "Version Checker", JOptionPane.WARNING_MESSAGE)){
						s_rate.setValue((int)s_rate.getValue() - 1);
						return;
					}
				}else if(newValue > 1200 && prev <= 1200){
					if(JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(frame, "By going over 1200 you enter the burst capability zone of the API it's advised not to do this.", "Version Checker", JOptionPane.WARNING_MESSAGE)){
						s_rate.setValue((int)s_rate.getValue() - 1);
						return;
					}
				}
				time.setText(String.format("Estimated time: %1$.1f minutes", ((double)updateQueue.size() / (double)newValue)));
				prev = newValue;
				pollRate = newValue;
			}
		});
		checking.add(rate);
		checking.add(start);
		checking.add(time);
		header.add(checking, BorderLayout.LINE_START);
		
		JButton sel_all = new JButton("Set all maps to 'update'");
		JButton sel_unmarked = new JButton("Set all unmarked maps to 'update'");
		JButton desel_unmarked = new JButton("Set all unmarked maps to 'don't update'");
		JPanel modes = new JPanel(new GridLayout(3, 1));
		modes.add(sel_all);
		modes.add(sel_unmarked);
		modes.add(desel_unmarked);
		
		JButton update = new JButton("Update all selected maps");
		
		JPanel update_panel = new JPanel(new BorderLayout());
		update_panel.add(modes, BorderLayout.CENTER);
		update_panel.add(update, BorderLayout.LINE_END);
		
		JPanel help_panel = new JPanel(new BorderLayout());
		JButton help = new JButton("Help");
		help_panel.add(help, BorderLayout.CENTER);
		header.add(help_panel, BorderLayout.LINE_END);
		
		checking.setBorder(BorderFactory.createTitledBorder("Checking"));
		update_panel.setBorder(BorderFactory.createTitledBorder("Updating"));
		help_panel.setBorder(BorderFactory.createTitledBorder("Info"));
		
		header.add(update_panel, BorderLayout.CENTER);
		
		content.add(header, BorderLayout.PAGE_START);
		content.setBorder(BorderFactory.createEtchedBorder());
		
		frame.add(content);
		frame.setMinimumSize(new Dimension(800, 400));
		frame.setLocationRelativeTo(null);
		frame.setSize(new Dimension(800, Toolkit.getDefaultToolkit().getScreenSize().height / 2));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	protected static OnlineBeatmapData checkState(LocalBeatmapData local){
		String req = getPage("https://osu.ppy.sh/api/get_beatmaps?k=" + APIKEY + "&h=" + local.hash);
		if(req == null){
			return null;//returning null will trigger a retry later on
		}
		if(req.equals("[]")){//no data means an update
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
	
	private static final void updateBeatmap(BeatmapItem item) throws IOException{
		File osu = new File(OSUDIR, "Songs" + File.separator + item.file + File.separator + item.local.osufilename);
		if(backup){
			File dest = new File("backup" + File.separator + item.local.osufilename);
			dest.createNewFile();
			FileOutputStream out = new FileOutputStream(dest);
			FileInputStream in = new FileInputStream(osu);
			Util.copyAllData(in, out);
			in.close();
			out.flush();
			out.close();
		}
		Path tmp = Files.createTempFile(item.local.osufilename, ".osu");
		PrintWriter writer = new PrintWriter(new FileOutputStream(tmp.toFile()));
		HttpURLConnection con = (HttpURLConnection) new URL("https://osu.ppy.sh/osu/" + item.local.mapid).openConnection();
		con.setRequestMethod("GET");
		con.setConnectTimeout(1000);

		BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while((line = reader.readLine()) != null){
			writer.println(line);
		}
		reader.close();
		writer.flush();
		writer.close();
		Files.move(tmp, osu.toPath(), StandardCopyOption.REPLACE_EXISTING);
		tmp.toFile().delete();
		tmp.toFile().deleteOnExit();
	}
}
