package com.kodingkingdom.kodebuilder.type.construction.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import com.kodingkingdom.kodebuilder.KodeDroid;
import com.kodingkingdom.kodebuilder.misc.KodeParser;
import com.kodingkingdom.kodebuilder.misc.KodeParser.Expression;
import com.kodingkingdom.kodebuilder.misc.KodeParser.Parser;
import com.kodingkingdom.kodebuilder.schedule.KodeBlock;
import com.kodingkingdom.kodebuilder.schedule.KodeException;
import com.kodingkingdom.kodebuilder.schedule.KodeSchedule;
import com.kodingkingdom.kodebuilder.schedule.KodeType;

public class GoType extends KodeType{

	long steps;
	BlockFace direction;
	KodeDroid droid;
	Location pos;
	
	private static final Pattern gopattern = Pattern.compile("go(east|west|north|south|up|down)");
	private static final Pattern gosteppattern = Pattern.compile("go(east|west|north|south|up|down)(.+)");
	@Override
	public GoBlock makeKodeBlock(String kodeString) {
		String code = kodeString.replaceAll("\\s", "").toLowerCase();
		Matcher m = gopattern.matcher(code);
		if (!m.matches()) {
			m=gosteppattern.matcher(code);
			if (!m.matches())return null;
			Parser<Expression> stepsExpr=KodeParser.getExpressionParser(m.group(2));
			if (stepsExpr==null)return null;
			return new GoBlock(this,code,BlockFace.valueOf(m.group(1).toUpperCase()),stepsExpr);}
		return new GoBlock(this,code,BlockFace.valueOf(m.group(1).toUpperCase()),KodeParser.getConstantParser("1"));}

	
	public class GoBlock extends KodeBlock{
		BlockFace whereface;
		Parser<Expression> stepsExpr;
		public KodeBlock clone() {
			return new GoBlock((GoType)blockType,kodeString,whereface,stepsExpr);}
		private GoBlock(KodeType Type, String code, BlockFace WhereFace, Parser<Expression> StepsExpr){blockType=Type;kodeString=code;whereface=WhereFace;stepsExpr=StepsExpr;}}

	@Override
	public void action(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof GoBlock)) throw new RuntimeException();
		GoBlock block = (GoBlock)blk;
		try{
			steps = block.stepsExpr.Parse().getEvaluator(sch).call();
			direction=droid.checkFace(steps, block.whereface);
			steps=droid.checkSide(steps);
			pos = droid.getFurnace().getLocation();}
		catch(Exception e){e.printStackTrace();throw new KodeException(block);}
		droid.move(
				pos.clone().add(direction.getModX()*steps,
						direction.getModY()*steps,
						direction.getModZ()*steps));}}
