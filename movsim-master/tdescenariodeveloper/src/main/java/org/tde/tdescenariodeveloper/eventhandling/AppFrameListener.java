package org.tde.tdescenariodeveloper.eventhandling;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.Simulator;
import org.movsim.viewer.App;
import org.movsim.viewer.graphics.TrafficCanvas;
import org.movsim.viewer.ui.ViewProperties;
import org.tde.tdescenariodeveloper.ui.MovsimConfigContext;
import org.tde.tdescenariodeveloper.updation.DataToViewerConverter;
import org.tde.tdescenariodeveloper.utils.FileUtils;
import org.tde.tdescenariodeveloper.utils.GraphicsHelper;
import org.tde.tdescenariodeveloper.utils.MovsimScenario;
/**
 * Class for Listening frame events triggered by user.
 * @author Shmeel
 * @see App
 *
 */
public class AppFrameListener implements ActionListener,Blockable {
	/**
	 * Contains reference to Movsim configuration loaded xml and panels added in it. 
	 */
	MovsimConfigContext mvCxt;
	JMenuItem open,save,run,reset;
	boolean blocked=true;
	/**
	 * 
	 * @param mvCxt MovsimConfigurationContext containing reference to loaded .xprj
	 */
	public AppFrameListener(MovsimConfigContext mvCxt) {
		this.mvCxt = mvCxt;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(blocked)return;
		JButton srcBtn=null;
		JMenuItem mi=null;
		if(e.getSource() instanceof JButton)srcBtn=(JButton)e.getSource();
		if(e.getSource() instanceof JMenuItem)mi=(JMenuItem)e.getSource();
		if(mi==open){
			final File f=FileUtils.chooseFile("xprj");
			if(f==null)return;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					mvCxt.getRdCxt().getAppFrame().getJp().reset();
					MovsimScenario.setScenario(f, mvCxt);
				}
			});
		}
		else if(mi==save){
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					File f=null;
					f=FileUtils.saveFile("xprj");
					if(f!=null){
						DataToViewerConverter.updateFractions(mvCxt);
						MovsimScenario.saveScenario(f,mvCxt);
					}
				}
			});
		}
		else if(mi==reset){
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					mvCxt.getRdCxt().getAppFrame().getJp().reset();
					MovsimScenario.resetScenario(mvCxt);
				}
			});
		}
		else if(mi==run){
			new Thread(new Runnable() {
				@Override
				public void run() {
					File f=new File(new File("").getAbsoluteFile()+"\\tmp.xprj");
					DataToViewerConverter.updateFractions(mvCxt);
					MovsimScenario.saveScenario(f, mvCxt);
					String[]s={"-f",f.getAbsolutePath()};
					try {
						App.main(s);
					} catch (URISyntaxException | IOException e) {
						GraphicsHelper.showToast(e.getMessage(), mvCxt.getRdCxt().getToastDurationMilis());
					}
				}
			}).start();
		}
	}
	public void setOpen(JMenuItem open) {
		this.open = open;
	}
	public void setSave(JMenuItem save) {
		this.save = save;
	}
	public void setReset(JMenuItem reset) {
		this.reset = reset;
	}
	public void setRun(JMenuItem run) {
		this.run = run;
	}
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

}
