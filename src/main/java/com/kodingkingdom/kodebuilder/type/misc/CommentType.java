package com.kodingkingdom.kodebuilder.type.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kodingkingdom.kodebuilder.KodeDroid;
import com.kodingkingdom.kodebuilder.schedule.KodeBlock;
import com.kodingkingdom.kodebuilder.schedule.KodeException;
import com.kodingkingdom.kodebuilder.schedule.KodeSchedule;
import com.kodingkingdom.kodebuilder.schedule.KodeType;

public class CommentType extends KodeType{

	private static Pattern getpattern = Pattern.compile("(?:|#.*)");
	@Override
	public CommentBlock makeKodeBlock(String kodeString) {
		String code = kodeString.replaceAll("\\s", "");
		Matcher m = getpattern.matcher(code);
		if (!m.matches()) return null;
		return new CommentBlock(this,code);}

	@Override
	public void action(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof CommentBlock)) throw new RuntimeException();}
	
	public class CommentBlock extends KodeBlock{
		public KodeBlock clone() {
			return new CommentBlock((CommentType)blockType,kodeString);}
		private CommentBlock(KodeType Type, String code){blockType=Type;kodeString=code;}}}
