package com.kodingkingdom.kodebuilder.schedule;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.kodingkingdom.kodebuilder.KodeBook;
import com.kodingkingdom.kodebuilder.KodeBuilderPlugin;
import com.kodingkingdom.kodebuilder.KodeEnvironment;
import com.kodingkingdom.kodebuilder.KodeRegent;
import com.kodingkingdom.kodebuilder.schedule.KodeScope.ScopeBlock;
import com.kodingkingdom.kodebuilder.type.misc.CommentType.CommentBlock;
import com.kodingkingdom.kodebuilder.type.scope.EndScope.EndBlock;
import com.kodingkingdom.kodebuilder.var.MatVar;
import com.kodingkingdom.kodebuilder.var.NumVar;
import com.kodingkingdom.kodebuilder.var.RandomVar;

public class KodeSchedule extends KodeRegent{
	KodeEnvironment env;
	
	int timeNow;
		

	volatile HashMap<String, NumVar> kodeVars=new HashMap<String, NumVar>();
	volatile MatVar[] kodeMats=new MatVar[9];
	
	volatile Stack<ScopeBlock> scopeStack;
	ArrayList<KodeTask> tasks;
	KodeBook kodeBook;
	
	volatile boolean running=false;
	volatile boolean error=false;

	public boolean isRunning(){return running;}
	public boolean isError(){return error;}
	public KodeBook getBook(){return kodeBook;}

	volatile public int timeSchedule;
	public KodeEnvironment getEnvironment(){return env;}
	public Stack<ScopeBlock> getScope(){return scopeStack;}
	public ArrayList<KodeTask> getTasks() {return tasks;}
	public HashMap<String, NumVar> getKodeVars(){return kodeVars;}
	public MatVar getMatVar(long slotNum){
		if (slotNum<1||slotNum>9)throw new IllegalArgumentException();
		ItemStack item = env.getOwner().getInventory().getItem((int)slotNum-1);
		if (item !=null &&
				kodeMats[(int)slotNum-1]!=null){
			ItemStack copyStack = kodeMats[(int)slotNum-1].getdata().getData().toItemStack(1);
			if (copyStack.getType().equals(item.getType())){
				return kodeMats[(int)slotNum-1];}}
		return null;}
	public void setMatVar(long slotNum,MatVar var){
		kodeMats[(int)slotNum-1]=var;}
	
	public KodeSchedule(KodeEnvironment KodeEnvironment, KodeBook KodeBook){
		kodeBook=KodeBook;
		env=KodeEnvironment;timeNow=0;timeSchedule=0;scopeStack=new Stack<ScopeBlock>();tasks=new ArrayList<KodeTask>();
		RandomVar.putRandVar(this);
		
		List<String> pages = kodeBook.getData().getPages();
		
		for (int pageNum=0;pageNum<pages.size();pageNum++){
			String[] lines = pages.get(pageNum).split("\n");
			for (int lineNum=0;lineNum<lines.length;lineNum++){
				lines[lineNum]=lines[lineNum].replaceAll("§0", "");
				KodeBuilderPlugin.debug(lines[lineNum]);					
				KodeTask taskNow=null;KodeBlock blockNow=null;
				for (KodeType type : env.getBlockTypes()){
					blockNow=type.makeKodeBlock(lines[lineNum]);
					if (blockNow!=null)break;}
				taskNow=new KodeTask(KodeSchedule.this);
				if (blockNow!=null){
					if (blockNow instanceof ScopeBlock){
						ScopeBlock scope = (ScopeBlock)blockNow;
						try{
							if ((scope instanceof EndBlock)){
								if (getScope().isEmpty()) throw new IllegalStateException("Alone brace } !");
								scopeStack.pop();}
							else {scopeStack.push(scope);}}
						catch(Exception e){
							timeSchedule=getTasks().size();
							blockNow=(KodeException.kodeType.makeKodeBlock(lines[lineNum], new KodeException(blockNow).from(e)));}}
											
					taskNow.setBlock(blockNow);
					getTasks().add(taskNow);}
				else {
					timeSchedule=getTasks().size();
					taskNow.setBlock(KodeException.kodeType.makeKodeBlock(lines[lineNum]));
					getTasks().add(taskNow);
					return;}}}	

		if (!scopeStack.isEmpty()){
			timeSchedule=getTasks().size();
			ScopeBlock errorScope=scopeStack.pop();
			KodeTask taskNow=new KodeTask(KodeSchedule.this);
			taskNow.setBlock(KodeException.kodeType.makeKodeBlock(errorScope.getString(),
					new KodeException(errorScope).
					because("Braces } is missing!+\nYou forgot braces for:\n"+errorScope.getString())));
			getTasks().add(taskNow);}}
	
	public void Live(){
		running=true;
		env.getOwner().sendMessage("Running your code...");
		KodeBuilderPlugin.debug("debug: KodeSch "+env.getOwner().getName());
		KodeBuilderPlugin.debug("debug: running");
		env.scheduleSyncTask(new BukkitRunnable(){public void run(){

			nexttick();}},1);}

	void error(KodeException e){
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		KodeBuilderPlugin.debug("debug: env sch returned error");
		KodeBuilderPlugin.debug("debug: sch of "+env.getOwner().getName());
		KodeBuilderPlugin.debug(e.getMessage());
		KodeBuilderPlugin.debug(sw.toString());
		error=true;
		env.getOwner().sendMessage("ERROR: Your code has errors!");
		env.getOwner().sendMessage(e.getReason());
		Die();
		env.error(e);}

	void success(){
		env.getOwner().sendMessage("SUCCESS: Your code run successfully!");
		KodeBuilderPlugin.debug("debug: KodeSch "+env.getOwner().getName());
		KodeBuilderPlugin.debug("debug: success");Die();}
	
	public void Die(){
		if (!running) return;
		KodeBuilderPlugin.debug("debug: sch"+(getEnvironment().getOwner()==null?getEnvironment().getOwnerId():getEnvironment().getOwner().getName())+" die signal");
		KodeBuilderPlugin.debug("debug: stacktrace dump");
		for (StackTraceElement e : Thread.currentThread().getStackTrace()){
			KodeBuilderPlugin.debug("debug: stacktrace"+e.toString());}
		running=false;}
	
	void nexttick(){
		if (!running)return;
		KodeBuilderPlugin.debug("SCH: TICKS="+timeNow);
		KodeBuilderPlugin.debug("debug: sch nexttick");
		if (timeSchedule>=getTasks().size()){
			success();
			return;}	
		KodeTask taskNow=getTasks().get(timeSchedule);		
		if (taskNow.getBlock() instanceof CommentBlock) {
			timeNow++;timeSchedule++;
			nexttick();
			return;}
		else{
			try {
				KodeBuilderPlugin.debug("debug: "+taskNow.taskBlock.kodeString);
				taskNow.runtask();}
			catch(KodeException e){
				error(e);return;}}
		KodeBuilderPlugin.debug("debug: sch run tasks");
		timeNow++;timeSchedule++;
			env.scheduleSyncTask(new BukkitRunnable(){public void run(){
				nexttick();}},1);}
	
	@Override
	public void Stopped() {
		env.getOwner().sendMessage("Code stopped");
		KodeBuilderPlugin.debug("debug: KodeSch "+env.getOwner().getName());
		KodeBuilderPlugin.debug("debug: stopped");}}