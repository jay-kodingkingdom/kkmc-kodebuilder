package com.kodingkingdom.kodebuilder.schedule;

import com.kodingkingdom.kodebuilder.KodeDroid;


public abstract class KodeType{
	abstract public KodeBlock makeKodeBlock(String kodeString);
	abstract public void action(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException;
	public final void action(KodeTask kodeTask) throws KodeException{
		action(kodeTask.getSchedule(),kodeTask.getDroid(),kodeTask.getBlock().clone());}}
