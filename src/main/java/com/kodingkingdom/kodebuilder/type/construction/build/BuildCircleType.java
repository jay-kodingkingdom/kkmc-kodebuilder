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

public class BuildCircleType extends AsyncType{

	long slotNum;
	long radiusOne;
	long radiusTwo;
	BlockFace directionOne;
	BlockFace directionTwo;
	
	private Location pos;
	private KodeDroid droid;
	private long[] diskOne;
	private long[] diskTwo;
	
	private long loopNum;
	
	private static final Pattern buildcirclepattern = Pattern.compile("build(.+)(?:circle)(north|south|east|west|up|down)(north|south|east|west|up|down)(.+)");
	private static final Pattern buildellipsepattern = Pattern.compile("build(.+)(?:circle)(north|south|east|west|up|down)(.+)(north|south|east|west|up|down)(.+)");
	@Override
	public BuildCircleBlock makeKodeBlock(String kodeString) {
		Matcher circlematcher = buildcirclepattern.matcher(kodeString.replaceAll("\\s", "").toLowerCase());
		Matcher ellipsematcher = buildellipsepattern.matcher(kodeString.replaceAll("\\s", "").toLowerCase());
		if (circlematcher.matches()){
			Parser<Expression> slotExpr = KodeParser.getExpressionParser(circlematcher.group(1));
			if (slotExpr==null)return null;
			Parser<Expression> radiusExpr = KodeParser.getExpressionParser(circlematcher.group(4));
			if (radiusExpr==null)return null;
			return new BuildCircleBlock(this,kodeString,
					slotExpr,
					BlockFace.valueOf(circlematcher.group(2).toUpperCase()),
					radiusExpr,
					BlockFace.valueOf(circlematcher.group(3).toUpperCase()),
					radiusExpr);}
		else if (ellipsematcher.matches()){
			Parser<Expression> slotExpr = KodeParser.getExpressionParser(ellipsematcher.group(1));
			if (slotExpr==null)return null;
			Parser<Expression> radiusOneExpr = KodeParser.getExpressionParser(ellipsematcher.group(3));
			if (radiusOneExpr==null)return null;
			Parser<Expression> radiusTwoExpr = KodeParser.getExpressionParser(ellipsematcher.group(5));
			if (radiusTwoExpr==null)return null;
			return new BuildCircleBlock(this,kodeString,
					slotExpr,
					BlockFace.valueOf(ellipsematcher.group(2).toUpperCase()),
					radiusOneExpr,
					BlockFace.valueOf(ellipsematcher.group(4).toUpperCase()),
					radiusTwoExpr);}
		else return null;}

	private static long[] getDisk(long XRadius,long YRadius){
		long[] disk = new long[(int)XRadius+1];

		long X, Y, XChange, YChange,EllipseError,StoppingX,StoppingY;	
		final long TwoASquare,TwoBSquare;
		TwoASquare = 2*XRadius*XRadius;TwoBSquare = 2*YRadius*YRadius;
		
		X=XRadius;Y=0;
		XChange=YRadius*YRadius*(1-2*XRadius);YChange=XRadius*XRadius;
		EllipseError=0;StoppingX=TwoBSquare*XRadius;StoppingY=0;
		while (StoppingX>=StoppingY){
			disk[(int)X]=Y;
			Y++;
			StoppingY+=TwoASquare;
			EllipseError+=YChange;
			YChange+=TwoASquare;
			if ((2*EllipseError + XChange) > 0 ){
				X--;
				StoppingX-=TwoBSquare;
				EllipseError+=XChange;
				XChange+=TwoBSquare;}}		
		X=0;Y=YRadius;
		XChange=YRadius*YRadius;YChange=XRadius*XRadius*(1-2*YRadius);
		EllipseError=0;StoppingX=0;StoppingY=TwoASquare*YRadius;
		while (StoppingX<=StoppingY){
			disk[(int)X]=Y;
			X++;
			StoppingX+=TwoBSquare;
			EllipseError+=XChange;         
			XChange+=TwoBSquare;         
			if ((2*EllipseError + YChange) > 0 ){
				Y--;
				StoppingY-=TwoASquare;
				EllipseError+=YChange;
				YChange+=TwoASquare;}}

		return disk;}
	
	private class BuildCircleBlock extends KodeBlock{
		Parser<Expression> slotExpr;
		BlockFace directionOne;
		BlockFace directionTwo;
		Parser<Expression> radiusOneExpr;
		Parser<Expression> radiusTwoExpr;
		public KodeBlock clone() {
			return new BuildCircleBlock((BuildCircleType)blockType,kodeString,slotExpr,directionOne,radiusOneExpr,directionTwo,radiusTwoExpr);}
		private BuildCircleBlock(BuildCircleType Type, String Code, Parser<Expression> SlotExpr, BlockFace DirectionOne, Parser<Expression> RadiusOneExpr, BlockFace DirectionTwo, Parser<Expression> RadiusTwoExpr){
			blockType=Type;kodeString=Code;slotExpr=SlotExpr;directionOne=DirectionOne;radiusOneExpr=RadiusOneExpr;directionTwo=DirectionTwo;radiusTwoExpr=RadiusTwoExpr;}}

	@Override
	protected void preprocess(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof BuildCircleBlock)) throw new RuntimeException();
		BuildCircleBlock b = (BuildCircleBlock)blk;
		try{
			this.droid=droid;
			slotNum = b.slotExpr.Parse().getEvaluator(sch).call();
			radiusOne = b.radiusOneExpr.Parse().getEvaluator(sch).call();
			radiusTwo = b.radiusTwoExpr.Parse().getEvaluator(sch).call();
			droid.checkSlotPut(slotNum);
			directionOne=droid.checkFace(radiusOne, b.directionOne);
			directionTwo=droid.checkFace(radiusTwo, b.directionTwo);
			radiusOne=droid.checkSide(radiusOne);
			radiusTwo=droid.checkSide(radiusTwo);
			if (radiusOne==0 || radiusTwo==0)return;
			pos = droid.getFurnace().getLocation();
			diskOne = getDisk(radiusOne,radiusTwo);
			diskTwo = getDisk(radiusTwo,radiusOne);
			loopNum=0;}			
		catch(Exception e){throw new KodeException(b).from(e);}}

	@Override
	protected boolean process() {
		if (radiusOne==0||radiusTwo==0)return false;
		
		if (loopNum<=radiusOne){
			long i = loopNum;
			droid.put(pos.clone().add(
					i*directionOne.getModX()+diskOne[(int)i]*directionTwo.getModX(),
					i*directionOne.getModY()+diskOne[(int)i]*directionTwo.getModY(),
					i*directionOne.getModZ()+diskOne[(int)i]*directionTwo.getModZ()),
					slotNum);
			droid.put(pos.clone().add(
					i*directionOne.getModX()-diskOne[(int)i]*directionTwo.getModX(),
					i*directionOne.getModY()-diskOne[(int)i]*directionTwo.getModY(),
					i*directionOne.getModZ()-diskOne[(int)i]*directionTwo.getModZ()),
					slotNum);
			droid.put(pos.clone().add(
					-i*directionOne.getModX()+diskOne[(int)i]*directionTwo.getModX(),
					-i*directionOne.getModY()+diskOne[(int)i]*directionTwo.getModY(),
					-i*directionOne.getModZ()+diskOne[(int)i]*directionTwo.getModZ()),
					slotNum);
			droid.put(pos.clone().add(
					-i*directionOne.getModX()-diskOne[(int)i]*directionTwo.getModX(),
					-i*directionOne.getModY()-diskOne[(int)i]*directionTwo.getModY(),
					-i*directionOne.getModZ()-diskOne[(int)i]*directionTwo.getModZ()),
					slotNum);}
		else if (loopNum-radiusOne-1<=radiusTwo) {
			long j=loopNum-radiusOne-1;
			droid.put(pos.clone().add(
					diskTwo[(int)j]*directionOne.getModX()+j*directionTwo.getModX(),
					diskTwo[(int)j]*directionOne.getModY()+j*directionTwo.getModY(),
					diskTwo[(int)j]*directionOne.getModZ()+j*directionTwo.getModZ()),
					slotNum);
			droid.put(pos.clone().add(
					diskTwo[(int)j]*directionOne.getModX()-j*directionTwo.getModX(),
					diskTwo[(int)j]*directionOne.getModY()-j*directionTwo.getModY(),
					diskTwo[(int)j]*directionOne.getModZ()-j*directionTwo.getModZ()),
					slotNum);
			droid.put(pos.clone().add(
					-diskTwo[(int)j]*directionOne.getModX()+j*directionTwo.getModX(),
					-diskTwo[(int)j]*directionOne.getModY()+j*directionTwo.getModY(),
					-diskTwo[(int)j]*directionOne.getModZ()+j*directionTwo.getModZ()),
					slotNum);
			droid.put(pos.clone().add(
					-diskTwo[(int)j]*directionOne.getModX()-j*directionTwo.getModX(),
					-diskTwo[(int)j]*directionOne.getModY()-j*directionTwo.getModY(),
					-diskTwo[(int)j]*directionOne.getModZ()-j*directionTwo.getModZ()),
					slotNum);}
		else return false;
		loopNum++;
		return true;}}
