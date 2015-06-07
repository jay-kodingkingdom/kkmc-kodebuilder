package com.kodingkingdom.kodebuilder.type.scope;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kodingkingdom.kodebuilder.KodeDroid;
import com.kodingkingdom.kodebuilder.schedule.KodeBlock;
import com.kodingkingdom.kodebuilder.schedule.KodeException;
import com.kodingkingdom.kodebuilder.schedule.KodeSchedule;
import com.kodingkingdom.kodebuilder.schedule.KodeScope;

public class ElseScope extends KodeScope{

	private static final Pattern elsepattern = Pattern.compile("else\\{");
	@Override
	public ElseBlock makeKodeBlock(String kodeString) {
		Matcher matcher = elsepattern.matcher(kodeString.replaceAll("\\s", "").toLowerCase());
		if (!matcher.matches()) return null;
		return new ElseBlock(this,kodeString);}

	@Override
	public void action(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof ElseBlock)) throw new RuntimeException();
		ElseBlock Else = (ElseBlock)blk;
		if (sch.getKodeVars().get("_else").getdata()==null)
			throw new KodeException(blk).because("Cannot evaluate ELSE without IF!");
		Else.scopePointer=sch.timeSchedule;
		startScope(sch,Else);
		
		while (Else.elseLeft){
			sch.timeSchedule++;			
			if (sch.timeSchedule>=sch.getTasks().size()){throw new KodeException(Else).because("No proper braces } for ELSE!");}	
			if (sch.getTasks().get(sch.timeSchedule).getBlock() instanceof ScopeBlock){ScopeBlock scope = (ScopeBlock)sch.getTasks().get(sch.timeSchedule).getBlock().clone(); ((KodeScope)scope.getType()).startScope(sch,scope);}}}
	

	@Override
	public void startScope(KodeSchedule sch, ScopeBlock scope) {
		if (!(scope instanceof ElseBlock)) throw new RuntimeException();
		sch.getScope().push(scope);}

	@Override
	public void endScope(KodeSchedule sch, ScopeBlock scope) {
		if (!(scope instanceof ElseBlock)) throw new RuntimeException();
		ElseBlock Else = (ElseBlock)scope;
		if (Else.elseLeft) {
			Else.elseLeft=false;
			if (sch.getKodeVars().get("_else").getdata().equals(1L)) sch.timeSchedule=scope.scopePointer;
			else sch.getScope().pop();}
		else sch.getScope().pop();}	
	
	public class ElseBlock extends ScopeBlock{
		boolean elseLeft=true;
		public KodeBlock clone() {
			return new ElseBlock((ElseScope)blockType,kodeString);}
		private ElseBlock(ElseScope Type, String Code){blockType=Type;kodeString=Code;}}}
