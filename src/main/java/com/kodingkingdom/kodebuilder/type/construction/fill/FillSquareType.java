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

public class FillSquareType extends AsyncType{

	long slotNum;
	long sideOne;
	long sideTwo;
	KodeDroid droid;
	BlockFace directionOne;
	BlockFace directionTwo;
	Location pos;
	
	private long loopNum;
	
	private static final Pattern fillsquarepattern = Pattern.compile("fill(.+)(?:square)(north|south|east|west|up|down)(north|south|east|west|up|down)(.+)");
	private static final Pattern fillrectanglepattern = Pattern.compile("fill(.+)(?:square)(north|south|east|west|up|down)(.+)(north|south|east|west|up|down)(.+)");
	@Override
	public FillSquareBlock makeKodeBlock(String kodeString) {
		Matcher squarematcher = fillsquarepattern.matcher(kodeString.replaceAll("\\s", "").toLowerCase());
		Matcher rectanglematcher = fillrectanglepattern.matcher(kodeString.replaceAll("\\s", "").toLowerCase());
		if (squarematcher.matches()){
			Parser<Expression> slotExpr = KodeParser.getExpressionParser(squarematcher.group(1));
			if (slotExpr==null)return null;
			Parser<Expression> sideExpr = KodeParser.getExpressionParser(squarematcher.group(4));
			if (sideExpr==null)return null;
			return new FillSquareBlock(this,kodeString,
					slotExpr,
					BlockFace.valueOf(squarematcher.group(2).toUpperCase()),
					sideExpr,
					BlockFace.valueOf(squarematcher.group(3).toUpperCase()),
					sideExpr);}
		else if (rectanglematcher.matches()){
			Parser<Expression> slotExpr = KodeParser.getExpressionParser(rectanglematcher.group(1));
			if (slotExpr==null)return null;
			Parser<Expression> sideOneExpr = KodeParser.getExpressionParser(rectanglematcher.group(3));
			if (sideOneExpr==null)return null;
			Parser<Expression> sideTwoExpr = KodeParser.getExpressionParser(rectanglematcher.group(5));
			if (sideTwoExpr==null)return null;
			return new FillSquareBlock(this,kodeString,
					slotExpr,
					BlockFace.valueOf(rectanglematcher.group(2).toUpperCase()),
					sideOneExpr,
					BlockFace.valueOf(rectanglematcher.group(4).toUpperCase()),
					sideTwoExpr);}
		return null;}

	private class FillSquareBlock extends KodeBlock{
		Parser<Expression> slotExpr;
		BlockFace directionOne;
		BlockFace directionTwo;
		Parser<Expression> sideOneExpr;
		Parser<Expression> sideTwoExpr;
		public KodeBlock clone() {
			return new FillSquareBlock((FillSquareType)blockType,kodeString,slotExpr,directionOne,sideOneExpr,directionTwo,sideTwoExpr);}
		private FillSquareBlock(FillSquareType Type, String Code, Parser<Expression> SlotExpr, BlockFace DirectionOne, Parser<Expression> SideOneExpr, BlockFace DirectionTwo, Parser<Expression> SideTwoExpr){
			blockType=Type;kodeString=Code;slotExpr=SlotExpr;directionOne=DirectionOne;sideOneExpr=SideOneExpr;directionTwo=DirectionTwo;sideTwoExpr=SideTwoExpr;}}

	@Override
	protected void preprocess(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof FillSquareBlock)) throw new RuntimeException();
		FillSquareBlock b = (FillSquareBlock)blk;
		try{
			this.droid=droid;
			slotNum = b.slotExpr.Parse().getEvaluator(sch).call();
			sideOne = b.sideOneExpr.Parse().getEvaluator(sch).call();
			sideTwo = b.sideTwoExpr.Parse().getEvaluator(sch).call();
			droid.checkSlotPut(slotNum);
			directionOne=droid.checkFace(sideOne, b.directionOne);
			directionTwo=droid.checkFace(sideTwo, b.directionTwo);
			sideOne=droid.checkSide(sideOne);
			sideTwo=droid.checkSide(sideTwo);
			pos = droid.getFurnace().getLocation();
			loopNum=0;}
		catch(Exception e){throw new KodeException(b).from(e);}}

	@Override
	protected boolean process() {
		if (loopNum<sideOne*sideTwo) {
			long x=loopNum/sideTwo;
			long y=loopNum%sideTwo;
			droid.put(pos.clone().add(x*directionOne.getModX()+y*directionTwo.getModX(),
					  x*directionOne.getModY()+y*directionTwo.getModY(),
					  x*directionOne.getModZ()+y*directionTwo.getModZ()),
					  slotNum);
			loopNum++;
			return true;}
		else return false;}}
