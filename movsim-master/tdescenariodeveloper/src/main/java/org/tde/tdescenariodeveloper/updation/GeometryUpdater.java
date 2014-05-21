package org.tde.tdescenariodeveloper.updation;

import org.movsim.input.network.OpenDriveHandlerJaxb;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.PlanView.Geometry;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.PlanView.Geometry.Arc;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.PlanView.Geometry.Line;
import org.movsim.roadmappings.RoadMapping;
import org.movsim.roadmappings.RoadMapping.PosTheta;
import org.movsim.roadmappings.RoadMappingPoly;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.tde.tdescenariodeveloper.exception.InvalidInputException;
import org.tde.tdescenariodeveloper.ui.RoadPropertiesPanel;
import org.tde.tdescenariodeveloper.utils.GraphicsHelper;
import org.tde.tdescenariodeveloper.utils.RoadNetworkUtils;

public class GeometryUpdater {
	RoadPropertiesPanel rdPrPnl;
	public GeometryUpdater(RoadPropertiesPanel rdPrPnl) {
		this.rdPrPnl=rdPrPnl;
	}

	public void updateSoffset() {
		if(rdPrPnl.getSelectedRoad().roadMapping() instanceof RoadMappingPoly){
			Geometry g=rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(rdPrPnl.getGmPnl().getSelectedIndex());
			Geometry prevG=rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(rdPrPnl.getGmPnl().getSelectedIndex()-1);
			
			double s=Double.parseDouble(rdPrPnl.getGmPnl().getS().getText());
			g.setS(s);
			g.setLength(getNext()!=null?getNext().getS()-s:rdPrPnl.getSelectedRoad().roadLength()-s);
			prevG.setLength(s-prevG.getS());
			RoadNetworkUtils.updateCoordinatesAndHeadings(rdPrPnl);
			RoadNetworkUtils.refresh(rdPrPnl);
		}
	}
	public void updateLength() throws InvalidInputException{
		double l=Double.parseDouble(rdPrPnl.getGmPnl().getL().getText());
		if(rdPrPnl.getSelectedRoad().roadMapping() instanceof RoadMappingPoly){
			RoadMappingPoly rmp=(RoadMappingPoly) rdPrPnl.getSelectedRoad().roadMapping();
			try{
				if(l<0.0)throw new InvalidInputException("Length can't be -ve");
				rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(rdPrPnl.getGmPnl().getSelectedIndex()).setLength(l);
				for(int i=rdPrPnl.getGmPnl().getSelectedIndex()+1;i<rmp.getRoadMappings().size();i++){
					rmp=(RoadMappingPoly) OpenDriveHandlerJaxb.createRoadMapping(rdPrPnl.getSelectedRoad().getOdrRoad());
					Geometry preGm=rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(i-1);
					RoadMapping prerm=rmp.getRoadMappings().get(i-1);
					Geometry gm=rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(i);
					gm.setS(preGm.getS()+preGm.getLength());
					PosTheta p=prerm.map(prerm.roadLength());
					gm.setX(p.x);
					gm.setY(p.y);
					gm.setHdg(p.theta());
				}
				rdPrPnl.getSelectedRoad().getOdrRoad().setLength(getGmSum());
				RoadNetworkUtils.updateCoordinatesAndHeadings(rdPrPnl);
				RoadNetworkUtils.refresh(rdPrPnl);
			}catch(NumberFormatException e){
				GraphicsHelper.showToast(e.getMessage(), 5000);
			}
		}
		else{
			try{
				rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(0).setLength(l);
				rdPrPnl.getSelectedRoad().getOdrRoad().setLength(l);
				RoadNetworkUtils.updateCoordinatesAndHeadings(rdPrPnl);
				RoadNetworkUtils.refresh(rdPrPnl);
			}catch(NumberFormatException e){
				GraphicsHelper.showToast(e.getMessage(), 5000);
			}
		}
	}
	public double getGmSum(){
		double sum=0.0;
		for(Geometry g:rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry()){
			sum+=g.getLength();
		}
		return sum;
	}


	public boolean isNextGmExist(){
		return rdPrPnl.getGmPnl().getSelectedIndex()+1<rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().size();
	}
	public boolean isPrevGmExist(){
		return rdPrPnl.getGmPnl().getSelectedIndex()>0;
	}
	public Geometry getNext(){
		if(isNextGmExist()){
			return rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(rdPrPnl.getGmPnl().getSelectedIndex()+1);
		}
		return null;
	}

	public void addnew() {
		Geometry g=rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().size()-1);
		double s=g.getS()+g.getLength();
		Geometry g2=new Geometry();
		RoadSegment rs=rdPrPnl.getSelectedRoad();
		if(rs.roadMapping() instanceof RoadMappingPoly){
			RoadMappingPoly rmp=(RoadMappingPoly)rs.roadMapping();
			PosTheta p=rmp.getRoadMappings().get(rmp.getRoadMappings().size()-1).map(rmp.getRoadMappings().get(rmp.getRoadMappings().size()-1).roadLength());
			g2.setX(p.x);
			g2.setY(p.y);
			g2.setHdg(p.theta());
		}else{
			RoadMapping rm=rs.roadMapping();
			PosTheta p=rm.map(rm.roadLength());
			g2.setX(p.x);
			g2.setY(p.y);
			g2.setHdg(p.theta());
		}
		g2.setS(s);
		g2.setLine(new Line());
		g2.setLength(g.getLength());
		rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().add(g2);
		rdPrPnl.getSelectedRoad().getOdrRoad().setLength(getGmSum());
		RoadNetworkUtils.updateCoordinatesAndHeadings(rdPrPnl);
		RoadNetworkUtils.refresh(rdPrPnl);
	}

	public void removeCurrent() {
		if(rdPrPnl.getSelectedRoad().roadMapping() instanceof RoadMappingPoly){
			if(rdPrPnl.getGmPnl().getSelectedIndex()>0){
				rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().remove(rdPrPnl.getGmPnl().getSelectedIndex());
				rdPrPnl.getGmPnl().updateGeomPanel();
				rdPrPnl.getSelectedRoad().getOdrRoad().setLength(getGmSum());
				RoadNetworkUtils.updateCoordinatesAndHeadings(rdPrPnl);
				RoadNetworkUtils.refresh(rdPrPnl);
			}
			else{
				GraphicsHelper.showToast("Remove geometries otherthan first one.", 4000);
			}
		}
		else{
			GraphicsHelper.showToast("Not deleted: A road must have one geometry.", 4000);
			rdPrPnl.getGmPnl().getRemove().setEnabled(false);
		}
	}

	public void updateXY() {
		double x=Double.parseDouble(rdPrPnl.getGmPnl().getTfX().getText());
		double y=Double.parseDouble(rdPrPnl.getGmPnl().getTfY().getText());
		rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(0).setX(x);
		rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(0).setY(y);
		RoadNetworkUtils.updateCoordinatesAndHeadings(rdPrPnl);
		RoadNetworkUtils.refresh(rdPrPnl);
	}
	public void updateHdg(){
		double hdg=Double.parseDouble(rdPrPnl.getGmPnl().getHdg().getText());
		rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(0).setHdg(hdg);
		RoadNetworkUtils.updateCoordinatesAndHeadings(rdPrPnl);
		RoadNetworkUtils.refresh(rdPrPnl);
	}
	public void updateCurv(){
		double curv=Double.parseDouble(rdPrPnl.getGmPnl().getCurvature().getText());
		rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(rdPrPnl.getGmPnl().getSelectedIndex()).getArc().setCurvature(curv);
		RoadNetworkUtils.updateCoordinatesAndHeadings(rdPrPnl);
		RoadNetworkUtils.refresh(rdPrPnl);
	}
	public void updateGmType(){
		if(((String)rdPrPnl.getGmPnl().getCbGmType().getSelectedItem()).equals("line")){
			rdPrPnl.getGmPnl().getarcTypePnl().setVisible(false);
			if(rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(rdPrPnl.getGmPnl().getSelectedIndex()).isSetLine()){
				return;
			}
			else{
				rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(rdPrPnl.getGmPnl().getSelectedIndex()).setArc(null);
				rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(rdPrPnl.getGmPnl().getSelectedIndex()).setLine(new Line());
				RoadNetworkUtils.updateCoordinatesAndHeadings(rdPrPnl);
				RoadNetworkUtils.refresh(rdPrPnl);
			}
		}else{
			rdPrPnl.getGmPnl().getarcTypePnl().setVisible(true);
			if(rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(rdPrPnl.getGmPnl().getSelectedIndex()).isSetArc()){
				rdPrPnl.getGmPnl().getCurvature().setText(rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(rdPrPnl.getGmPnl().getSelectedIndex()).getArc().getCurvature()+"");
				return;
			}
			else{
				Arc arc=new Arc();
				String s=GraphicsHelper.valueFromUser("Enter curvature: e.g -0.001456");
				if(GraphicsHelper.isDouble(s)){
					arc.setCurvature(Double.parseDouble(s));
					rdPrPnl.getGmPnl().getCurvature().setText(arc.getCurvature()+"");
					rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(rdPrPnl.getGmPnl().getSelectedIndex()).setLine(null);
					rdPrPnl.getSelectedRoad().getOdrRoad().getPlanView().getGeometry().get(rdPrPnl.getGmPnl().getSelectedIndex()).setArc(arc);
					RoadNetworkUtils.updateCoordinatesAndHeadings(rdPrPnl);
					RoadNetworkUtils.refresh(rdPrPnl);
				}else{
					rdPrPnl.getGmPnl().getCbGmType().setSelectedItem("line");
					GraphicsHelper.showToast("Value entered is not a number", 4000);
				}
			}
		}
	}
}