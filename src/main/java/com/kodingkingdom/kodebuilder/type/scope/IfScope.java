package com.kodingkingdom.kodebuilder.type.scope;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kodingkingdom.kodebuilder.KodeDroid;
import com.kodingkingdom.kodebuilder.misc.KodeParser;
import com.kodingkingdom.kodebuilder.misc.KodeParser.Condition;
import com.kodingkingdom.kodebuilder.misc.KodeParser.Parser;
import com.kodingkingdom.kodebuilder.schedule.KodeBlock;
import com.kodingkingdom.kodebuilder.schedule.KodeException;
import com.kodingkingdom.kodebuilder.schedule.KodeSchedule;
import com.kodingkingdom.kodebuilder.schedule.KodeScope;
import com.kodingkingdom.kodebuilder.var.IntVar;

public class IfScope extends KodeScope{

	private static final Pattern ifpattern = Pattern.compile("if([^\\{]+)\\{");
	@Override
	public IfBlock makeKodeBlock(String kodeString) {
		Matcher matcher = ifpattern.matcher(kodeString.replaceAll("\\s", "").toLowerCase());
		if (!matcher.matches()) return null;
		Parser<Condition> cond = KodeParser.getConditionParser(matcher.group(1));
		if (cond==null)return null;
		return new IfBlock(this,kodeString,cond);}

	@Override
	public void action(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof IfBlock)) throw new RuntimeException();
		IfBlock iF = (IfBlock)blk;
		try {iF.cond=iF.condParser.Parse().getEvaluator(sch).call();}
		catch (Exception e){throw new KodeException(blk).from(e).because("Cannot evaluate IF!\nMaybe variable not declared?");}
		iF.scopePointer=sch.timeSchedule;
		startScope(sch,iF);
		
		while (iF.ifLeft){
			sch.timeSchedule++;			
			if (sch.timeSchedule>=sch.getTasks().size()){throw new KodeException(iF).because("No proper braces } for IF!");}	
			if (sch.getTasks().get(sch.timeSchedule).getBlock() instanceof ScopeBlock){ScopeBlock scope = (ScopeBlock)sch.getTasks().get(sch.timeSchedule).getBlock().clone(); ((KodeScope)scope.getType()).startScope(sch,scope);}}}
	

	@Override
	public void startScope(KodeSchedule sch, ScopeBlock scope) {
		if (!(scope instanceof IfBlock)) throw new RuntimeException();
		sch.getScope().push(scope);}

	@Override
	public void endScope(KodeSchedule sch, ScopeBlock scope) {
		if (!(scope instanceof IfBlock)) throw new RuntimeException();
		IfBlock iF = (IfBlock)scope;
		if (iF.ifLeft) {
			iF.ifLeft=false;
			sch.getKodeVars().putIfAbsent("_else", new IntVar("_else"));
			sch.getKodeVars().get("_else").setdata(iF.cond?0L:1L);
			if (iF.cond) sch.timeSchedule=scope.scopePointer;
			else sch.getScope().pop();}
		else sch.getScope().pop();}	
	
	public class IfBlock extends ScopeBlock{
		Parser<Condition> condParser;
		boolean cond;
		boolean ifLeft=true;
		public KodeBlock clone() {
			return new IfBlock((IfScope)blockType,kodeString,condParser);}
		private IfBlock(IfScope Type, String Code, Parser<Condition> CondParser){blockType=Type;kodeString=Code;condParser=CondParser;}}}
