package com.kodingkingdom.kodebuilder.type.construction.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kodingkingdom.kodebuilder.KodeDroid;
import com.kodingkingdom.kodebuilder.KodeDroid.InvalidDroidException;
import com.kodingkingdom.kodebuilder.misc.KodeParser;
import com.kodingkingdom.kodebuilder.misc.KodeParser.Expression;
import com.kodingkingdom.kodebuilder.misc.KodeParser.Parser;
import com.kodingkingdom.kodebuilder.schedule.KodeBlock;
import com.kodingkingdom.kodebuilder.schedule.KodeException;
import com.kodingkingdom.kodebuilder.schedule.KodeSchedule;
import com.kodingkingdom.kodebuilder.schedule.KodeType;

public class PutType extends KodeType{

	long slotNum;
	KodeDroid droid;
	
	private static Pattern putpattern = Pattern.compile("put(.+)");
	@Override
	public PutBlock makeKodeBlock(String kodeString) {
		String code = kodeString.replaceAll("\\s", "").toLowerCase();
		Matcher m = putpattern.matcher(code);
		if (!m.matches()) return null;
		Parser<Expression> slotExpr=KodeParser.getExpressionParser(m.group(1));
		if (slotExpr==null) return null;
		return new PutBlock(this,code,slotExpr);}

	public class PutBlock extends KodeBlock{
		Parser<Expression> slotExpr;
		public KodeBlock clone() {
			return new PutBlock((PutType)blockType,kodeString,slotExpr);}
		private PutBlock(KodeType Type, String code, Parser<Expression> SlotExpr){blockType=Type;kodeString=code;slotExpr=SlotExpr;}}

	@Override
	public void action(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof PutBlock)) throw new RuntimeException();
		PutBlock block = (PutBlock)blk;
		try{slotNum = block.slotExpr.Parse().getEvaluator(sch).call();
			droid.checkSlotPut(slotNum);}
		catch(Exception e){throw new KodeException(block).from(e);}
		try {
			droid.put(droid.getFurnace().getLocation(), slotNum);}
		catch (InvalidDroidException e) {
			e.printStackTrace();}}}
