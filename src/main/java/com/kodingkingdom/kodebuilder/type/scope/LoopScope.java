package com.kodingkingdom.kodebuilder.type.scope;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kodingkingdom.kodebuilder.KodeBuilderPlugin;
import com.kodingkingdom.kodebuilder.KodeDroid;
import com.kodingkingdom.kodebuilder.misc.KodeParser;
import com.kodingkingdom.kodebuilder.misc.KodeParser.Expression;
import com.kodingkingdom.kodebuilder.misc.KodeParser.Parser;
import com.kodingkingdom.kodebuilder.schedule.KodeBlock;
import com.kodingkingdom.kodebuilder.schedule.KodeException;
import com.kodingkingdom.kodebuilder.schedule.KodeSchedule;
import com.kodingkingdom.kodebuilder.schedule.KodeScope;

public class LoopScope extends KodeScope{

	private static final Pattern looppattern = Pattern.compile("loop([^\\{]+)\\{");
	@Override
	public LoopBlock makeKodeBlock(String kodeString) {
		Matcher matcher = looppattern.matcher(kodeString.replaceAll("\\s", "").toLowerCase());
		if (!matcher.matches()) return null;
		Parser<Expression> loops = KodeParser.getExpressionParser(matcher.group(1));
		KodeBuilderPlugin.debug("debug: loops is null?"+(loops==null));
		if (loops==null)return null;
		return new LoopBlock(this,kodeString,loops);}

	@Override
	public void action(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof LoopBlock)) throw new RuntimeException();
		LoopBlock loop = (LoopBlock)blk;
		try {loop.loopsNow=loop.loopParser.Parse().getEvaluator(sch).call();
		} catch (Exception e){throw new KodeException(blk);}
		loop.scopePointer=sch.timeSchedule;
		startScope(sch,loop);
		
		while (loop.loopLeft){
			sch.timeSchedule++;			
			if (sch.timeSchedule>=sch.getTasks().size()){throw new KodeException(loop).because("No proper braces } for LOOP!");}	
			if (sch.getTasks().get(sch.timeSchedule).getBlock() instanceof ScopeBlock){ScopeBlock scope = (ScopeBlock)sch.getTasks().get(sch.timeSchedule).getBlock().clone(); ((KodeScope)scope.getType()).startScope(sch,scope);}}}
	

	@Override
	public void startScope(KodeSchedule sch, ScopeBlock scope) {
		if (!(scope instanceof LoopBlock)) throw new RuntimeException();
		sch.getScope().push(scope);}

	@Override
	public void endScope(KodeSchedule sch, ScopeBlock scope) {
		if (!(scope instanceof LoopBlock)) throw new RuntimeException();
		LoopBlock loop = (LoopBlock)scope;
		if (loop.loopLeft) {
			loop.loopLeft=false;
			if (loop.loopsNow>0) sch.timeSchedule=loop.scopePointer;
			else sch.getScope().pop();}
		else {
			loop.loopsNow--;
			if (loop.loopsNow>0) sch.timeSchedule=loop.scopePointer;
			else sch.getScope().pop();}}	
	
	public class LoopBlock extends ScopeBlock{
		Parser<Expression> loopParser;
		long loopsNow;
		boolean loopLeft=true;
		public KodeBlock clone() {
			return new LoopBlock((LoopScope)blockType,kodeString,loopParser);}
		private LoopBlock(LoopScope Type, String Code, Parser<Expression> LoopParser){blockType=Type;kodeString=Code;loopParser=LoopParser;}}}
