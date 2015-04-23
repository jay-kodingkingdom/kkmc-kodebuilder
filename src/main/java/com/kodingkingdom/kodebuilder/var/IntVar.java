package com.kodingkingdom.kodebuilder.var;

import java.util.regex.Pattern;

public class IntVar extends NumVar{
	private static final Pattern slotVar = Pattern.compile("s([1-9])"); 
	public static boolean isValidName(String name){
		if ("random".equals(name.replaceAll("\\s","").toLowerCase())) return false;
		if (slotVar.matcher(name.replaceAll("\\s","").toLowerCase()).matches())return false;
		return true;}
	public IntVar(String Name){
		super(Name);
		if (!isValidName(Name)) throw new IllegalArgumentException();}
	long data;
	@Override
	public void setdata(Object Data) {
		if (Data instanceof Long) data=(Long)Data;
		else throw new RuntimeException();}
	@Override
	public Long getdata() {
		return data;}}
