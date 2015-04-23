package com.kodingkingdom.kodebuilder.type.construction.fill;

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

public class FillCubeType extends AsyncType{

	long slotNum;
	long sideOne;
	long sideTwo;
	long sideThr;
	BlockFace directionOne;
	BlockFace directionTwo;
	BlockFace directionThr;
	KodeDroid droid;
	Location pos;
	
	long loopNum;
	
	private static final Pattern fillcubepattern = Pattern.compile("fill(.+)(?:cube)(north|south|east|west|up|down)(north|south|east|west|up|down)(north|south|east|west|up|down)(.+)");
	private static final Pattern fillcuboidpattern = Pattern.compile("fill(.+)(?:cube)(north|south|east|west|up|down)(.+)(north|south|east|west|up|down)(.+)(north|south|east|west|up|down)(.+)");
	@Override
	public FillCubeBlock makeKodeBlock(String kodeString) {
		Matcher cubematcher = fillcubepattern.matcher(kodeString.replaceAll("\\s", "").toLowerCase());
		Matcher cuboidmatcher = fillcuboidpattern.matcher(kodeString.replaceAll("\\s", "").toLowerCase());
		if (cubematcher.matches()){
			Parser<Expression> slotExpr = KodeParser.getExpressionParser(cubematcher.group(1));
			if (slotExpr==null)return null;
			Parser<Expression> sideExpr = KodeParser.getExpressionParser(cubematcher.group(5));
			if (sideExpr==null)return null;
			return new FillCubeBlock(this,kodeString,
					slotExpr,
					BlockFace.valueOf(cubematcher.group(2).toUpperCase()),
					BlockFace.valueOf(cubematcher.group(3).toUpperCase()),
					BlockFace.valueOf(cubematcher.group(4).toUpperCase()),
					sideExpr,
					sideExpr,
					sideExpr);}
		else if (cuboidmatcher.matches()){
			Parser<Expression> slotExpr = KodeParser.getExpressionParser(cuboidmatcher.group(1));
			if (slotExpr==null)return null;
			Parser<Expression> sideOneExpr = KodeParser.getExpressionParser(cuboidmatcher.group(3));
			if (sideOneExpr==null)return null;
			Parser<Expression> sideTwoExpr = KodeParser.getExpressionParser(cuboidmatcher.group(5));
			if (sideTwoExpr==null)return null;
			Parser<Expression> sideThrExpr = KodeParser.getExpressionParser(cuboidmatcher.group(7));
			if (sideThrExpr==null)return null;
			return new FillCubeBlock(this,kodeString,
					slotExpr,
					BlockFace.valueOf(cuboidmatcher.group(2).toUpperCase()),
					BlockFace.valueOf(cuboidmatcher.group(4).toUpperCase()),
					BlockFace.valueOf(cuboidmatcher.group(6).toUpperCase()),
					sideOneExpr,
					sideTwoExpr,
					sideThrExpr);} 
		else return null;}

	private class FillCubeBlock extends KodeBlock{
		Parser<Expression> slotExpr;
		BlockFace directionOne;
		BlockFace directionTwo;
		BlockFace directionThr;
		Parser<Expression> sideOneExpr;
		Parser<Expression> sideTwoExpr;
		Parser<Expression> sideThrExpr;
		public KodeBlock clone() {
			return new FillCubeBlock((FillCubeType)blockType,kodeString,slotExpr,directionOne,directionTwo,directionThr,sideOneExpr,sideTwoExpr,sideThrExpr);}
		private FillCubeBlock(FillCubeType Type, String Code, Parser<Expression> SlotExpr, BlockFace DirectionOne, BlockFace DirectionTwo, BlockFace DirectionThr, Parser<Expression> SideOneExpr, Parser<Expression> SideTwoExpr, Parser<Expression> SideThrExpr){
			blockType=Type;kodeString=Code;slotExpr=SlotExpr;directionOne=DirectionOne;directionTwo=DirectionTwo;directionThr=DirectionThr;sideOneExpr=SideOneExpr;sideTwoExpr=SideTwoExpr;sideThrExpr=SideThrExpr;}}

	@Override
	protected void preprocess(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof FillCubeBlock)) throw new RuntimeException();
		FillCubeBlock b = (FillCubeBlock)blk;
		try{
			this.droid=droid;
			slotNum = b.slotExpr.Parse().getEvaluator(sch).call();
			sideOne = b.sideOneExpr.Parse().getEvaluator(sch).call();
			sideTwo = b.sideTwoExpr.Parse().getEvaluator(sch).call();
			sideThr = b.sideThrExpr.Parse().getEvaluator(sch).call();
			droid.checkSlotPut(slotNum);
			directionOne=droid.checkFace(sideOne, b.directionOne);
			directionTwo=droid.checkFace(sideTwo, b.directionTwo);
			directionThr=droid.checkFace(sideThr, b.directionThr);
			sideOne=droid.checkSide(sideOne);
			sideTwo=droid.checkSide(sideTwo);
			sideThr=droid.checkSide(sideThr);
			pos = droid.getFurnace().getLocation();
			loopNum=0;}
		catch(Exception e){throw new KodeException(b).from(e);}}

	@Override
	protected boolean process() {	
		
		if (loopNum<sideOne*sideTwo*sideThr) {
			long x=(loopNum/sideThr)/sideTwo;
			long y=(loopNum/sideThr)%sideTwo;
			long z=loopNum%sideThr;
			droid.put(pos.clone().add(x*directionOne.getModX()+y*directionTwo.getModX()+z*directionThr.getModX(),
					  x*directionOne.getModY()+y*directionTwo.getModY()+z*directionThr.getModY(),
					  x*directionOne.getModZ()+y*directionTwo.getModZ()+z*directionThr.getModZ()),
					  slotNum);
			loopNum++;
			return true;}
		else return false;}}
