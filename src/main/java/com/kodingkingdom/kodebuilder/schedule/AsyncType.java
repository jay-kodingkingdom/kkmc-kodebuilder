package com.kodingkingdom.kodebuilder.schedule;

import org.bukkit.scheduler.BukkitRunnable;

import com.kodingkingdom.kodebuilder.KodeDroid;
import com.kodingkingdom.kodebuilder.KodeEnvironment;

public abstract class AsyncType extends KodeType{

	private KodeEnvironment env;
	private KodeSchedule sch;
	
	abstract protected void preprocess(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException;
	
	abstract protected boolean process();
		
	private final void doProcess(){
			if (sch.isRunning())
				if (process()) throw env.new Rescheduling(new BukkitRunnable(){public void run(){
					doProcess();
					}}, 1);
				else  throw env.new Rescheduling(new BukkitRunnable(){public void run(){
					sch.timeNow++;sch.timeSchedule++;
					if (sch.isRunning()) env.scheduleSyncTask(new BukkitRunnable(){public void run(){sch.nexttick();}},1);}}, 1);}
	
	@Override
	public final void action(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		this.sch=sch;
		env=sch.getEnvironment();
		preprocess(sch, droid, blk);
		doProcess();}}