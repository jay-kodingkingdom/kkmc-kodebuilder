package com.kodingkingdom.kodebuilder.type.construction.build;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import com.kodingkingdom.kodebuilder.KodeDroid;
import com.kodingkingdom.kodebuilder.misc.KodeParser;
import com.kodingkingdom.kodebuilder.misc.KodeParser.Expression;
import com.kodingkingdom.kodebuilder.misc.KodeParser.Parser;
import com.kodingkingdom.kodebuilder.schedule.AsyncType;
import com.kodingkingdom.kodebuilder.schedule.KodeBlock;
import com.kodingkingdom.kodebuilder.schedule.KodeException;
import com.kodingkingdom.kodebuilder.schedule.KodeSchedule;

public class BuildSphereType extends AsyncType{

	long slotNum;
	long radiusOne=0;
	long radiusTwo=0;
	long radiusThr=0;
	KodeDroid droid;
	Location pos;
	
	double radiusX;
    double radiusY;
    double radiusZ;
    double invRadiusX;
    double invRadiusY;
    double invRadiusZ;
    long ceilRadiusX;
    long ceilRadiusY;
    long ceilRadiusZ;
	
    private boolean done=false;
    private long x, y, z;
	private double xn, nextXn, yn, nextYn, zn, nextZn;
    
	private static final Pattern buildspherepattern = Pattern.compile("build(.+)(?:sphere)(.+)");
	private static final Pattern buildellipsoidpattern = Pattern.compile("build(.+)(?:sphere)(north|south|east|west|up|down)(.+)(north|south|east|west|up|down)(.+)(north|south|east|west|up|down)(.+)");
	@Override
	public BuildSphereBlock makeKodeBlock(String kodeString) {
		Matcher spherematcher = buildspherepattern.matcher(kodeString.replaceAll("\\s", "").toLowerCase());
		Matcher ellipsoidmatcher = buildellipsoidpattern.matcher(kodeString.replaceAll("\\s", "").toLowerCase());
		
		if (ellipsoidmatcher.matches()){
			Parser<Expression> slotExpr = KodeParser.getExpressionParser(ellipsoidmatcher.group(1));
			if (slotExpr==null)return null;
			Parser<Expression> radiusOneExpr = KodeParser.getExpressionParser(ellipsoidmatcher.group(3));
			if (radiusOneExpr==null)return null;
			Parser<Expression> radiusTwoExpr = KodeParser.getExpressionParser(ellipsoidmatcher.group(5));
			if (radiusTwoExpr==null)return null;
			Parser<Expression> radiusThrExpr = KodeParser.getExpressionParser(ellipsoidmatcher.group(7));
			if (radiusThrExpr==null)return null;
			return new BuildSphereBlock(this,kodeString,
					slotExpr,
					BlockFace.valueOf(ellipsoidmatcher.group(2).toUpperCase()),
					BlockFace.valueOf(ellipsoidmatcher.group(4).toUpperCase()),
					BlockFace.valueOf(ellipsoidmatcher.group(6).toUpperCase()),
					radiusOneExpr,
					radiusTwoExpr,
					radiusThrExpr);}
		else if (spherematcher.matches()){
			Parser<Expression> slotExpr = KodeParser.getExpressionParser(spherematcher.group(1));
			if (slotExpr==null)return null;
			Parser<Expression> radiusExpr = KodeParser.getExpressionParser(spherematcher.group(2));
			if (radiusExpr==null)return null;
			return new BuildSphereBlock(this,kodeString,
					slotExpr,
					BlockFace.UP,
					BlockFace.NORTH,
					BlockFace.EAST,
					radiusExpr,
					radiusExpr,
					radiusExpr);}
		else return null;}
	
	private class BuildSphereBlock extends KodeBlock{
		Parser<Expression> slotExpr;
		BlockFace directionOne;
		BlockFace directionTwo;
		BlockFace directionThr;
		Parser<Expression> radiusOneExpr;
		Parser<Expression> radiusTwoExpr;
		Parser<Expression> radiusThrExpr;
		public KodeBlock clone() {
			return new BuildSphereBlock((BuildSphereType)blockType,kodeString,slotExpr,directionOne,directionTwo,directionThr,radiusOneExpr,radiusTwoExpr,radiusThrExpr);}
		private BuildSphereBlock(BuildSphereType Type, String Code, Parser<Expression> SlotExpr,  BlockFace DirectionOne, BlockFace DirectionTwo, BlockFace DirectionThr, Parser<Expression> RadiusOneExpr, Parser<Expression> RadiusTwoExpr, Parser<Expression> RadiusThrExpr){
			blockType=Type;kodeString=Code;slotExpr=SlotExpr;directionOne=DirectionOne;directionTwo=DirectionTwo;directionThr=DirectionThr;radiusOneExpr=RadiusOneExpr;radiusTwoExpr=RadiusTwoExpr;radiusThrExpr=RadiusThrExpr;}}

	@Override
	protected void preprocess(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof BuildSphereBlock)) throw new RuntimeException();
		BuildSphereBlock b = (BuildSphereBlock)blk;
		try{
			this.droid=droid;
			slotNum = b.slotExpr.Parse().getEvaluator(sch).call();
			radiusOne = b.radiusOneExpr.Parse().getEvaluator(sch).call();
			radiusTwo = b.radiusTwoExpr.Parse().getEvaluator(sch).call();
			radiusThr = b.radiusThrExpr.Parse().getEvaluator(sch).call();
			droid.checkSlotPut(slotNum);
			radiusOne=droid.checkSide(radiusOne);
			radiusTwo=droid.checkSide(radiusTwo);
			radiusThr=droid.checkSide(radiusThr);

			pos = droid.getFurnace().getLocation();			
			radiusX = (b.directionOne.getModX()!=0?radiusOne:(b.directionTwo.getModX()!=0?radiusTwo:(b.directionThr.getModX()!=0?radiusThr:0))); 
			radiusY = (b.directionOne.getModY()!=0?radiusOne:(b.directionTwo.getModY()!=0?radiusTwo:(b.directionThr.getModY()!=0?radiusThr:0)));
			radiusZ = (b.directionOne.getModZ()!=0?radiusOne:(b.directionTwo.getModZ()!=0?radiusTwo:(b.directionThr.getModZ()!=0?radiusThr:0)));

			if (radiusX==0)return;
			if (radiusY==0)return;
			if (radiusZ==0)return;
			
			radiusX += 0.5;
	        radiusY += 0.5;
	        radiusZ += 0.5;
	        invRadiusX = 1 / radiusX;
	        invRadiusY = 1 / radiusY;
	        invRadiusZ = 1 / radiusZ;
	        ceilRadiusX = (long) Math.ceil(radiusX);
	        ceilRadiusY = (long) Math.ceil(radiusY);
	        ceilRadiusZ = (long) Math.ceil(radiusZ);

	        x=0;y=0;z=0;
	        nextXn = 0;
	        nextYn = 0;
	        nextZn = 0;
	        xn=invRadiusX;
	        yn=invRadiusY;
	        zn=invRadiusZ;
	        done=false;}
		catch(Exception e){throw new KodeException(b).from(e);}}

	@Override
	protected boolean process() {

        if (xn*xn+yn*yn+zn*zn > 1) {        	
            if (z == 0) {            	
                if (y == 0) {                	
                	x=ceilRadiusX;
                    y=ceilRadiusY;
                    z=ceilRadiusZ;
                    nextLoop();
            		if (done) return false;
            		else return true;}                
                y=ceilRadiusY;
                z=ceilRadiusZ;
                nextLoop();
        		if (done) return false;
        		else return true;}            
            z=ceilRadiusZ;
            nextLoop();
    		if (done) return false;
    		else return true;}

        if (nextXn*nextXn+yn*yn+zn*zn <= 1 && 
        		xn*xn+nextYn*nextYn+zn*zn <= 1 && 
        		xn*xn+yn*yn+nextZn*nextZn <= 1) {
        	nextLoop();
    		if (done) return false;
    		else return true;}

        droid.put(pos.clone().add(x, y, z), slotNum);
        droid.put(pos.clone().add(-x, y, z), slotNum);
        droid.put(pos.clone().add(x, -y, z), slotNum);
        droid.put(pos.clone().add(x, y, -z), slotNum);
        droid.put(pos.clone().add(-x, -y, z), slotNum);
        droid.put(pos.clone().add(x, -y, -z), slotNum);
        droid.put(pos.clone().add(-x, y, -z), slotNum);
        droid.put(pos.clone().add(-x, -y, -z), slotNum);
        
		nextLoop();
		if (done) return false;
		else return true;}
	

	private void nextLoop(){	
		z++;
		if (z>ceilRadiusZ){
			z=0;
			
        	y++;
        	if (y>ceilRadiusY){
        		y=0;            
            
            	x++;
            	if (x>ceilRadiusX){
            		done=true;//x=0;
            		return;}
            	            	 
                xn = nextXn;
                nextXn = (x + 1) * invRadiusX;
                nextYn = 0;}        	
            yn = nextYn;
            nextYn = (y + 1) * invRadiusY;
            nextZn = 0;}		
		zn = nextZn;
        nextZn = (z + 1) * invRadiusZ;}}
