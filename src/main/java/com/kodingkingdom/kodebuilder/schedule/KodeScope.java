package com.kodingkingdom.kodebuilder.schedule;


public abstract class KodeScope extends KodeType{
	@Override
	public abstract ScopeBlock makeKodeBlock(String kodeString);
	
	public abstract void startScope(KodeSchedule sch, ScopeBlock scope) throws KodeException;
	public abstract void endScope(KodeSchedule sch, ScopeBlock scope) throws KodeException;
	
	public abstract class ScopeBlock extends KodeBlock{
		public int scopePointer;}}
