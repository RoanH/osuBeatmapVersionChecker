package me.roan.versionchecker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileSystemView;

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
	private static JProgressBar progress;
	private static JLabel time;
	private static Runnable openUpdateControls;
	private static Runnable enableUpdateButton;
	
	private static boolean backup = false;
	
	private static ScheduledFuture<?> task;
	
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
	}
	
	public static final void startChecking(){
		progress.setMinimum(0);
		progress.setMaximum(updateQueue.size());
		progress.setValue(0);
		task = Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()->{
			try{
				if(!updateQueue.isEmpty()){
					Callable<Boolean> task = updateQueue.poll();
					if(!task.call()){
						updateQueue.add(task);
					}else{
						beatmaps.repaint();
					}
				}else{
					openUpdateControls.run();
					Thread.sleep(Long.MAX_VALUE);
				}
				System.out.println("Queue size: " + updateQueue.size());
			}catch(Throwable t){
				System.out.println("Error");
				t.printStackTrace();
			}
			progress.setValue(progress.getMaximum() - updateQueue.size());
			time.setText(String.format("Estimated time until completion: %1$.2f minutes", ((double)updateQueue.size() / (double)pollRate)));
		}, 0, TimeUnit.MINUTES.toNanos(1) / pollRate, TimeUnit.NANOSECONDS);
	}
	
	public static final void startUpdating(){
		task.cancel(true);
		if(pollRate > 10){
			pollRate = 10;
		}
		FileManager.disableControls();
		FileManager.submitUpdateList();
		progress.setMinimum(0);
		progress.setMaximum(updateQueue.size());
		progress.setValue(0);
		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()->{
			try{
				if(!updateQueue.isEmpty()){
					updateQueue.poll().call();
				}else{
					Thread.sleep(Long.MAX_VALUE);
				}
				System.out.println("Queue size: " + updateQueue.size());
			}catch(Throwable t){
				System.out.println("Error");
				t.printStackTrace();
			}
			progress.setValue(progress.getMaximum() - updateQueue.size());
			time.setText(String.format("Estimated time until completion: %1$.2f minutes", ((double)updateQueue.size() / (double)pollRate)));
		}, 0, TimeUnit.MINUTES.toNanos(1) / pollRate, TimeUnit.NANOSECONDS);
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
		JPanel checking = new JPanel(new BorderLayout());
		JButton start = new JButton("Start");
		JLabel l_rate = new JLabel("API poll rate: ");
		JSpinner s_rate = new JSpinner(new SpinnerNumberModel(pollRate, 1, 1400, 1));
		JLabel l_rate_2 = new JLabel(" requests/minute");
		start.addActionListener((e)->{
			s_rate.setEnabled(false);
			start.setEnabled(false);
			startChecking();
		});
		JPanel rate = new JPanel(new BorderLayout());
		rate.add(l_rate, BorderLayout.LINE_START);
		rate.add(s_rate, BorderLayout.CENTER);
		rate.add(l_rate_2, BorderLayout.LINE_END);
		checking.setPreferredSize(new Dimension(220, 0));
		time = new JLabel(String.format("Estimated time until completion: %1$.2f minutes", ((double)updateQueue.size() / (double)pollRate)));
		time.setHorizontalAlignment(SwingConstants.CENTER);
		s_rate.addChangeListener(new ChangeListener(){
			
			private int prev = pollRate;

			@Override
			public void stateChanged(ChangeEvent arg0) {
				int newValue = (int) s_rate.getValue();
				if(newValue > 60 && prev <= 60){
					if(JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(frame, "It's advised to inform peppy when using a poll rate over 60.", "Version Checker", JOptionPane.WARNING_MESSAGE)){
						s_rate.setValue(prev = 60);
						return;
					}
				}
				if(newValue > 1200 && prev <= 1200){
					if(JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(frame, "By going over 1200 you enter the burst capability zone of the API it's advised not to do this.", "Version Checker", JOptionPane.WARNING_MESSAGE)){
						s_rate.setValue(prev = 1200);
						return;
					}
				}
				time.setText(String.format("Estimated time until completion: %1$.2f minutes", ((double)updateQueue.size() / (double)newValue)));
				prev = newValue;
				pollRate = newValue;
			}
		});
		checking.add(rate, BorderLayout.PAGE_START);
		checking.add(start, BorderLayout.CENTER);
		header.add(checking, BorderLayout.LINE_START);
		
		JButton sel_all = new JButton("Set all maps to 'update'");
		JButton sel_unmarked = new JButton("Set all unmarked maps to 'update'");
		JButton desel_unmarked = new JButton("Set all unmarked maps to 'don't update'");
		JPanel modes = new JPanel(new GridLayout(3, 1));
		modes.add(sel_all);
		modes.add(sel_unmarked);
		modes.add(desel_unmarked);
		JButton update = new JButton("Update all selected maps");
		enableUpdateButton = ()->{
			update.setEnabled(true);
		};
		sel_all.addActionListener((e)->{
			FileManager.setSelected(true);
			enableUpdateButton();
			frame.repaint();
		});
		sel_unmarked.addActionListener((e)->{
			FileManager.setSelected(false);
			sel_unmarked.setEnabled(false);
			desel_unmarked.setEnabled(false);
			enableUpdateButton();
			frame.repaint();
		});
		desel_unmarked.addActionListener((e)->{
			FileManager.setUnselected();
			sel_unmarked.setEnabled(false);
			desel_unmarked.setEnabled(false);
			enableUpdateButton();
			frame.repaint();
		});
		sel_all.setEnabled(false);
		sel_unmarked.setEnabled(false);
		desel_unmarked.setEnabled(false);
		update.setEnabled(false);
		openUpdateControls = ()->{
			sel_all.setEnabled(true);
			sel_unmarked.setEnabled(true);
			desel_unmarked.setEnabled(true);
		};
		update.addActionListener((e)->{
			if(JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(frame, "Maps will be updated automatically, but F5 has to be pressed ingame to load the new versions.", "Version Checker", JOptionPane.OK_CANCEL_OPTION)){
				return;
			}
			sel_all.setEnabled(false);
			sel_unmarked.setEnabled(false);
			desel_unmarked.setEnabled(false);
			update.setEnabled(false);
			startUpdating();
		});
		JCheckBox makeBackup = new JCheckBox("Create backups", false);
		makeBackup.addActionListener(new ActionListener(){
			
			private boolean informed = false;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(!informed){
					JOptionPane.showMessageDialog(frame, "<html><center>A copy will now be made for each map.<br>After updating finishes the copies can be found in:<br>" + new File(FileSystemView.getFileSystemView().getDefaultDirectory(), "backup").getAbsolutePath() + "</center></html>", "Version Checker", JOptionPane.INFORMATION_MESSAGE);
				}
				backup = makeBackup.isSelected();
			}
		});
		makeBackup.setHorizontalAlignment(SwingConstants.CENTER);
		JPanel side = new JPanel(new BorderLayout());
		side.add(update, BorderLayout.CENTER);
		side.add(makeBackup, BorderLayout.PAGE_START);
		
		JPanel update_panel = new JPanel(new BorderLayout());
		update_panel.add(modes, BorderLayout.CENTER);
		update_panel.add(side, BorderLayout.LINE_END);
		
		JPanel help_panel = new JPanel(new BorderLayout());
		JButton help = new JButton("Help");
		help.addActionListener((e)->{
			showHelp();
		});
		help_panel.add(help, BorderLayout.CENTER);
		header.add(help_panel, BorderLayout.LINE_END);
		
		checking.setBorder(BorderFactory.createTitledBorder("Checking"));
		update_panel.setBorder(BorderFactory.createTitledBorder("Updating"));
		help_panel.setBorder(BorderFactory.createTitledBorder("Info"));
		
		header.add(update_panel, BorderLayout.CENTER);
		
		JPanel progress_panel = new JPanel(new BorderLayout());
		progress = new JProgressBar();
		progress_panel.setBorder(BorderFactory.createTitledBorder("Progress"));
		progress_panel.add(time, BorderLayout.PAGE_START);
		progress_panel.add(progress, BorderLayout.CENTER);
		header.add(progress_panel, BorderLayout.PAGE_END);
		
		content.add(header, BorderLayout.PAGE_START);
		content.setBorder(BorderFactory.createEtchedBorder());
		
		frame.add(content);
		frame.setMinimumSize(new Dimension(800, 400));
		frame.setLocationRelativeTo(null);
		frame.setSize(new Dimension(800, Toolkit.getDefaultToolkit().getScreenSize().height / 2));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	private static boolean debug = true;
	protected static OnlineBeatmapData checkState(LocalBeatmapData local){
		if(debug){
			OnlineBeatmapData d = new OnlineBeatmapData();
			d.generated = true;
			return d;
		}
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
		//parsing errors shouldn't happen and if they do we 
		//never want to return null since we don't want to trigger a retry
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
	
	protected static final void updateBeatmap(BeatmapItem item) throws IOException{
		File osu = new File(item.file, item.local.osufilename);
		if(backup){
			File dest = new File(FileSystemView.getFileSystemView().getDefaultDirectory(), "backup");
			dest.mkdirs();
			dest = new File(dest, item.local.osufilename);
			dest.createNewFile();
			FileOutputStream out = new FileOutputStream(dest);
			FileInputStream in = new FileInputStream(osu);
			Util.copyAllData(in, out);
			in.close();
			out.flush();
			out.close();
		}
		if(debug){
			return;
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

	protected static void enableUpdateButton() {
		if(FileManager.beatmapsUpdateModel.size() == BeatmapItem.choiceMade){
			enableUpdateButton.run();
		}
	}
	
	private static void showHelp(){
		JPanel info = new JPanel(new BorderLayout());
		JLabel general = new JLabel("<html><u>Checking:</u><br>"
				+ "When checking the local version of a beatmap is compared to the online<br>"
				+ "version to check for changes. After that the map may get listed under<br>"
				+ "'State changed' or 'Update available'.<br><br>"
				+ "<u>Updating:</u><br>"
				+ "When updating the local version of a beatmap is replaced by the online version.<br><br>"
				+ "<u>State changed:</u><br>"
				+ "Maps get listed under 'state changed' if their ranked status changed.<br>"
				+ "Simple going to the map ingame will update its status.<br><br>"
				+ "<u>Update available:</u><br>"
				+ "Maps get listed under 'update avaiable' if an newer version of the map exists.<br>"
				+ "Maps can be updated automatically, but after updating finishes F5 has to be<br>"
				+ "pressed ingame to load the update versions.</html>");//TODO
		general.setBorder(BorderFactory.createTitledBorder("General"));
		info.add(general, BorderLayout.PAGE_START);
		JLabel api = new JLabel("<html><u>Statement with regard to the API poll rate:</u><br>"
				+ "<i>\"Current rate limit is set at an insanely high 1200 requests per minute, with burst capability of up to<br>"
				+ "200 beyond that. If you require more, you probably fall into the above category of abuse. If you are<br>"
				+ "doing more than 60 requests a minute, you should probably give peppy a yell.\"</i></html>");
		api.setBorder(BorderFactory.createTitledBorder("API"));
		info.add(api, BorderLayout.CENTER);
		JPanel programinfo = new JPanel(new GridLayout(3, 1));
		String v = checkVersion();
		JLabel version = new JLabel("Version: v1.0, latest version: " + (v == null ? "Unknow" : v));//XXX version number
		JLabel gitlink = new JLabel("<html>GitHub: <font color=blue><i><u>https://github.com/RoanH/osuMapVersionChecker</u></i></font></html>");
		JLabel forumlink = new JLabel("<html>Forum post: <font color=blue><i><u>https://osu.ppy.sh/community/forums/topics/</u></i></font></html>");
		programinfo.add(forumlink);
		programinfo.add(gitlink);
		programinfo.add(version);
		programinfo.setBorder(BorderFactory.createTitledBorder("Info"));
		info.add(programinfo, BorderLayout.PAGE_END);
		gitlink.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				if(Desktop.isDesktopSupported()){
					try {
						Desktop.getDesktop().browse(new URL("https://github.com/RoanH/osuMapVersionChecker").toURI());//TODO fill-in
					} catch (IOException | URISyntaxException e1) {
						//pity
					}
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
		});
		forumlink.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				if(Desktop.isDesktopSupported()){
					try {
						Desktop.getDesktop().browse(new URL("https://osu.ppy.sh/community/forums/topics/").toURI());//TODO fill-in
					} catch (IOException | URISyntaxException e1) {
						//pity
					}
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
		});
		JOptionPane.showMessageDialog(frame, info, "Version Checker", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Check the KeysPerSecond version to see
	 * if we are running the latest version
	 * @return The latest version
	 */
	private static final String checkVersion(){
		try{ 			
			HttpURLConnection con = (HttpURLConnection) new URL("https://api.github.com/repos/RoanH/osuMapVersionChecker/tags").openConnection(); 			
			con.setRequestMethod("GET"); 		
			con.setConnectTimeout(10000); 					   
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream())); 	
			String line = reader.readLine(); 		
			reader.close(); 	
			String[] versions = line.split("\"name\":\"v");
			int max_main = 3;
			int max_sub = 0;
			String[] tmp;
			for(int i = 1; i < versions.length; i++){
				tmp = versions[i].split("\",\"")[0].split("\\.");
				if(Integer.parseInt(tmp[0]) > max_main){
					max_main = Integer.parseInt(tmp[0]);
					max_sub = Integer.parseInt(tmp[1]);
				}else if(Integer.parseInt(tmp[0]) < max_main){
					continue;
				}else{
					if(Integer.parseInt(tmp[1]) > max_sub){
						max_sub = Integer.parseInt(tmp[1]);
					}
				}
			}
			return "v" + max_main + "." + max_sub;
		}catch(Exception e){ 	
			return null;
			//No Internet access or something else is wrong,
			//No problem though since this isn't a critical function
		}
	}
}
