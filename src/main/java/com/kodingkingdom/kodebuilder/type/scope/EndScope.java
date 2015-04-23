package com.kodingkingdom.kodebuilder.type.scope;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kodingkingdom.kodebuilder.KodeDroid;
import com.kodingkingdom.kodebuilder.schedule.KodeBlock;
import com.kodingkingdom.kodebuilder.schedule.KodeException;
import com.kodingkingdom.kodebuilder.schedule.KodeSchedule;
import com.kodingkingdom.kodebuilder.schedule.KodeScope;

public class EndScope extends KodeScope{

	private static final Pattern endpattern = Pattern.compile("\\}");
	@Override
	public EndBlock makeKodeBlock(String kodeString) {
		Matcher matcher = endpattern.matcher(kodeString.replaceAll("\\s", ""));
		if (!matcher.matches()) return null;
		return new EndBlock(this,kodeString,1);}

	@Override
	public void action(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof EndBlock)) throw new RuntimeException();
		try{
			if (sch.getScope().isEmpty()) throw new IllegalStateException("Alone brace } !");
			((KodeScope)sch.getScope().peek().getType()).endScope(sch, (ScopeBlock)sch.getScope().peek());}
		catch(Exception e){
			throw new KodeException(blk).from(e);}}
		/*for (int i=0;i<endBlock.ends;i++){
			try{
				if (sch.getScope().isEmpty()) throw new IllegalStateException("Alone brace } !");
				((KodeScope)sch.getScope().peek().getType()).endScope(sch, (ScopeBlock)sch.getScope().peek());}
			catch(Exception e){
				throw new KodeException(scope).from(e);}
			if (pointer!=sch.timeSchedule)return;}*/
	

	@Override
	public void startScope(KodeSchedule sch, ScopeBlock scope) throws KodeException {
		action(sch,null,scope);}
	@Override
	public void endScope(KodeSchedule sch, ScopeBlock scope) throws KodeException {
		action(sch,null,scope);}	
	
	public class EndBlock extends ScopeBlock{
		int ends;
		public KodeBlock clone() {
			return new EndBlock((EndScope)blockType,kodeString,ends);}
		private EndBlock(EndScope Type, String Code, int Ends){blockType=Type;kodeString=Code;ends=Ends;}}}
