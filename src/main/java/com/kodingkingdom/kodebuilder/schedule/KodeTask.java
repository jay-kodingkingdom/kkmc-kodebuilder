package com.kodingkingdom.kodebuilder.schedule;

import com.kodingkingdom.kodebuilder.KodeDroid;

public class KodeTask {
	KodeSchedule schedule;
	KodeBlock taskBlock;
	KodeTask(KodeSchedule sch){schedule=sch;}
	void setBlock(KodeBlock block){taskBlock=block;}
	void runtask() throws KodeException{taskBlock.blockType.action(this);}
	public KodeDroid getDroid(){return schedule.env.getKodeBuilder().getDroid(schedule.env.getOwnerId());}
	public KodeBlock getBlock(){return taskBlock;}
	public KodeSchedule getSchedule(){return schedule;}}