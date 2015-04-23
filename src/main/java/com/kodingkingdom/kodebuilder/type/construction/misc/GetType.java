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

public class GetType extends KodeType{

	long slotNum;
	KodeDroid droid;
	
	private static Pattern getpattern = Pattern.compile("get(.+)");
	@Override
	public GetBlock makeKodeBlock(String kodeString) {
		String code = kodeString.replaceAll("\\s", "").toLowerCase();
		Matcher m = getpattern.matcher(code);
		if (!m.matches()) return null;
		Parser<Expression> slotExpr = KodeParser.getExpressionParser(m.group(1));
		if (slotExpr==null) return null;
		return new GetBlock(this,code,slotExpr);}
	
	public class GetBlock extends KodeBlock{
		Parser<Expression> slotExpr;
		public KodeBlock clone() {
			return new GetBlock((GetType)blockType,kodeString,slotExpr);}
		private GetBlock(KodeType Type, String code, Parser<Expression> SlotExpr){blockType=Type;kodeString=code;slotExpr=SlotExpr;}}

	@Override
	public void action(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof GetBlock)) throw new RuntimeException();
		GetBlock block = (GetBlock)blk;
		try{slotNum = block.slotExpr.Parse().getEvaluator(sch).call();
			droid.checkSlotGet(slotNum);}
		catch(Exception e){throw new KodeException(block).from(e);}
		try {
			droid.get(droid.getFurnace().getLocation(), slotNum);}
		catch (InvalidDroidException e) {
			e.printStackTrace();}}}
