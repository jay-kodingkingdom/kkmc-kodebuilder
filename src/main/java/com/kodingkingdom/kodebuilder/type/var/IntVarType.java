package com.kodingkingdom.kodebuilder.type.var;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kodingkingdom.kodebuilder.KodeDroid;
import com.kodingkingdom.kodebuilder.misc.KodeParser;
import com.kodingkingdom.kodebuilder.misc.KodeParser.Expression;
import com.kodingkingdom.kodebuilder.misc.KodeParser.Parser;
import com.kodingkingdom.kodebuilder.schedule.KodeBlock;
import com.kodingkingdom.kodebuilder.schedule.KodeException;
import com.kodingkingdom.kodebuilder.schedule.KodeSchedule;
import com.kodingkingdom.kodebuilder.schedule.KodeType;
import com.kodingkingdom.kodebuilder.var.IntVar;

public class IntVarType extends KodeType{

	private static Pattern getpattern = Pattern.compile("([a-z]+):(.+)");
	@Override
	public IntVarBlock makeKodeBlock(String kodeString) {
		String code = kodeString.replaceAll("\\s", "").toLowerCase();
		Matcher m = getpattern.matcher(code);
		if (!m.matches()) return null;
		if (!IntVar.isValidName(m.group(1)))return null;
		Parser<Expression> value = KodeParser.getExpressionParser(m.group(2));
		return new IntVarBlock(this,code,m.group(1),value);}

	@Override
	public void action(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof IntVarBlock)) throw new RuntimeException();
		IntVarBlock block = (IntVarBlock)blk;
		try{
			sch.getKodeVars().putIfAbsent(block.name, new IntVar(block.name));
			sch.getKodeVars().get(block.name).setdata(block.value.Parse().getEvaluator(sch).call());}
		catch(Exception e){throw new KodeException(block);}}
	
	public class IntVarBlock extends KodeBlock{
		String name;
		Parser<Expression> value;
		public KodeBlock clone() {
			return new IntVarBlock((IntVarType)blockType,kodeString,name,value);}
		private IntVarBlock(KodeType Type, String code, String Name, Parser<Expression> Value){blockType=Type;kodeString=code;name=Name;value=Value;}}}
