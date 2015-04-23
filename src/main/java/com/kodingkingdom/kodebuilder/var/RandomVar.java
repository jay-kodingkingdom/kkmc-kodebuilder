package com.kodingkingdom.kodebuilder.var;

import java.util.Random;

import com.kodingkingdom.kodebuilder.schedule.KodeSchedule;

public class RandomVar extends NumVar{
	final int limit=100;
	Random random = new Random();
	public static boolean isValidName(String name){
		if ("random".equalsIgnoreCase(name)) return true;
		return false;}
	private RandomVar(String Name){
		super(Name);
		if (!isValidName(Name)) throw new IllegalArgumentException();}
	@Override
	public void setdata(Object Data) {
		return;}
	@Override
	public Long getdata() {
		return (long)random.nextInt(limit);}
	
	public static void putRandVar(KodeSchedule sch){
		sch.getKodeVars().put("random", new RandomVar("random"));}}
