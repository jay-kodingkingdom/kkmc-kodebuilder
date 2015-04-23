package com.kodingkingdom.kodebuilder;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

public class KodeUndo extends KodeRegent{

	boolean running;
	KodeEnvironment env;
	KodeDroid droid;
	
	Iterator<Entry<Location,BlockState>> undo;
	ReentrantLock lock=new ReentrantLock();
	
	public KodeUndo(KodeEnvironment Environment,KodeDroid Droid){env=Environment;droid=Droid;
		running=false;}
	
	@Override
	public void Live() {		
		if (!env.droidHistory.isEmpty()){
			running=true;
			env.scheduleSyncTask(new BukkitRunnable(){public void run(){nextUndo();}},(int) env.getKodeBuilder().getLoadAmountLimit());
			env.getOwner().sendMessage("Undoing code...");
			KodeBuilderPlugin.debug("debug: KodeUndo "+env.getOwner().getName());
			KodeBuilderPlugin.debug("debug: Undoing");
			undo=env.droidHistory.entrySet().iterator();}}

	@Override
	public void Die() {
		if (!running) return;
		running=false;}
	
	private void success(){
		env.getOwner().sendMessage("SUCCESS: Code undone");
		KodeBuilderPlugin.debug("debug: KodeUndo "+env.getOwner().getName());
		KodeBuilderPlugin.debug("debug: Undone");
		Die();}
	
	private void nextUndo(){
		lock.lock();try{
			if (!running)return;
			
			long count=env.getKodeBuilder().getLoadAmountLimit();int i=0;
			while (i<count){
				if (undo.hasNext()){
					Entry<Location,BlockState> blockEntry = undo.next();
					Location loc = blockEntry.getKey();
					BlockState blockState = blockEntry.getValue();
					droid.put(loc, blockState.getType(), blockState.getData(),false);
					undo.remove();
					i++;}
				else {
					success();
					return;}}
			
			if (running) throw env.new Rescheduling(new BukkitRunnable(){public void run(){
												nextUndo();}},(int) env.getKodeBuilder().getLoadAmountLimit());}
		finally{lock.unlock();}}
	
	@Override
	public void Stopped() {
		env.getOwner().sendMessage("Undo stopped");
		KodeBuilderPlugin.debug("debug: KodeUndo "+env.getOwner().getName());
		KodeBuilderPlugin.debug("debug: Stopped");}
	
	@Override
	public boolean isRunning() {
		return running;}}
