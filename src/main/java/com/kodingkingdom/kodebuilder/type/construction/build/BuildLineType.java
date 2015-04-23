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

public class BuildLineType extends AsyncType{

	long slotNum;
	long length;
	KodeDroid droid;
	BlockFace direction;
	Location pos;
	
	private long loopNum;
		
	private static final Pattern buildlinepattern = Pattern.compile("build(?:|from|blockfrom)(.+)(?:line)(north|south|east|west|up|down)(.+)");
	@Override
	public BuildLineBlock makeKodeBlock(String kodeString) {
		Matcher matcher = buildlinepattern.matcher(kodeString.replaceAll("\\s", "").toLowerCase());
		if (!matcher.matches())return null;
		Parser<Expression> slotExpr = KodeParser.getExpressionParser(matcher.group(1));
		if (slotExpr==null)return null;
		Parser<Expression> lengthExpr = KodeParser.getExpressionParser(matcher.group(3));
		if (lengthExpr==null)return null;
		return new BuildLineBlock(this,kodeString,
			slotExpr,
			BlockFace.valueOf(matcher.group(2).toUpperCase()),
			lengthExpr);}
	
	private class BuildLineBlock extends KodeBlock{
		Parser<Expression> slotExpr;
		BlockFace direction;
		Parser<Expression> lengthExpr;
		public KodeBlock clone() {
			return new BuildLineBlock((BuildLineType)blockType,kodeString,slotExpr,direction,lengthExpr);}
		private BuildLineBlock(BuildLineType Type, String Code, Parser<Expression> SlotExpr, BlockFace Direction, Parser<Expression> LengthExpr){
			blockType=Type;kodeString=Code;slotExpr=SlotExpr;direction=Direction;lengthExpr=LengthExpr;}}

	@Override
	protected void preprocess(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof BuildLineBlock)) throw new RuntimeException();
		BuildLineBlock b = (BuildLineBlock)blk;
		try{
			this.droid=droid;
		slotNum=b.slotExpr.Parse().getEvaluator(sch).call();
		length=b.lengthExpr.Parse().getEvaluator(sch).call();
		droid.checkSlotPut(slotNum);
		direction=droid.checkFace(length, b.direction);
		pos = droid.getFurnace().getLocation();
		length=droid.checkSide(length);
		loopNum=0;}
		catch(Exception e){throw new KodeException(b);}}

	@Override
	protected boolean process() {
		if (loopNum<length){
			loopNum++;
			droid.put(pos.clone().add(loopNum*direction.getModX(),loopNum*direction.getModY(),loopNum*direction.getModZ()), slotNum);
			return true;}
		else return false;}}
