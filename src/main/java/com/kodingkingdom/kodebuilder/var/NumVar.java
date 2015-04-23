package com.kodingkingdom.kodebuilder.var;


public abstract class NumVar extends KodeVar{
	public NumVar(String Name){name=Name;}
	@Override
	public abstract void setdata(Object Data);
	@Override
	public abstract Long getdata();}
