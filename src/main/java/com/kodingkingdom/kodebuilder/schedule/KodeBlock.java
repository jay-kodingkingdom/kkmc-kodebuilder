package com.kodingkingdom.kodebuilder.schedule;


public abstract class KodeBlock{
	protected KodeType blockType;
	protected String kodeString;
	public abstract KodeBlock clone();
	public KodeType getType(){return blockType;}
	public String getString(){return kodeString;}}