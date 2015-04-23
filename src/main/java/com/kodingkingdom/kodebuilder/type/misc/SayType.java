package com.kodingkingdom.kodebuilder.type.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kodingkingdom.kodebuilder.KodeDroid;
import com.kodingkingdom.kodebuilder.schedule.KodeBlock;
import com.kodingkingdom.kodebuilder.schedule.KodeException;
import com.kodingkingdom.kodebuilder.schedule.KodeSchedule;
import com.kodingkingdom.kodebuilder.schedule.KodeType;

public class SayType extends KodeType{

	private static Pattern getpattern = Pattern.compile("\\s*[Ss]\\s*[Aa]\\s*[Yy](.*)");
	@Override
	public SayBlock makeKodeBlock(String kodeString) {
		String code = kodeString;
		Matcher m = getpattern.matcher(code);
		if (!m.matches()) return null;
		return new SayBlock(this,code,m.group(1));}

	@Override
	public void action(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof SayBlock)) throw new RuntimeException();
		SayBlock b = (SayBlock)blk;
		sch.getEnvironment().getOwner().sendMessage(b.words);}
	
	public class SayBlock extends KodeBlock{
		String words;
		public KodeBlock clone() {
			return new SayBlock((SayType)blockType,kodeString,words);}
		private SayBlock(KodeType Type, String code, String Words){blockType=Type;kodeString=code;words=Words;}}}
