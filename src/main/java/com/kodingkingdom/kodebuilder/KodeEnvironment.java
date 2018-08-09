package com.kodingkingdom.kodebuilder;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.kodingkingdom.kodebuilder.KodeDroid.InvalidDroidException;
import com.kodingkingdom.kodebuilder.schedule.KodeException;
import com.kodingkingdom.kodebuilder.schedule.KodeSchedule;
import com.kodingkingdom.kodebuilder.schedule.KodeType;


public class KodeEnvironment implements Listener {
	KodeBuilder kodeBuilder;
	boolean alive=false;
	long nextRunKode=0;
	long nextRunUndo=0;
	//boolean inEvent=false;
	
	private HashSet<KodeType> blockTypes;
	
	UUID ownerId;
	
	KodeRegent regentNow=null;
	
	//ExecutorService asyncTaskPool;
	//ReentrantLock lock=new ReentrantLock();
	boolean cancelSignal = false;
	
	HashMap<Location,BlockState> droidHistory;
	ItemStack undoStack;
	
	long particleInterval=5L;
		
	//Queue<Map.Entry<BukkitRunnable,Map.Entry<Integer,Boolean>>> taskQueue;
	Queue<Map.Entry<BukkitRunnable,Integer>> taskQueue;
	//static ReentrantLock simplelock = new ReentrantLock();

	public long getParticleInterval(){return particleInterval;}
	public void setParticleInterval(long Interval){particleInterval=Interval;}

	public UUID getOwnerId(){return ownerId;}
	public Player getOwner(){return Bukkit.getPlayer(ownerId);}
	public KodeBuilder getKodeBuilder(){return kodeBuilder;}
	public HashSet<KodeType> getBlockTypes() {return blockTypes;}
	public void scheduleSyncTask(BukkitRunnable newTask, int load){
		//kodeBuilder.lock.lock();try{
				taskQueue.add(new AbstractMap.SimpleEntry<BukkitRunnable,Integer>(newTask,load));}
		//finally{kodeBuilder.lock.unlock();}}
	/*public void scheduleAsyncTask(BukkitRunnable newTask,int load){
		simplelock.simplelock();try{
			taskQueue.add(new AbstractMap.SimpleEntry<BukkitRunnable,Map.Entry<Integer,Boolean>>(newTask,new AbstractMap.SimpleEntry<Integer,Boolean>(load,false)));
		}finally{simplelock.unlock();}}*/
	
	public KodeEnvironment(
	KodeBuilder KodeBuilder,
	Collection<KodeType> BlockTypes,
	Player Owner){
		kodeBuilder=KodeBuilder;blockTypes=new HashSet<KodeType>(BlockTypes);ownerId=Owner.getUniqueId();
		taskQueue=new LinkedList<Map.Entry<BukkitRunnable,Integer>>();
		undoStack=new ItemStack(Material.BOOK);
		//asyncTaskPool=Executors.newFixedThreadPool(loadAmountLimit);
		ItemMeta item =undoStack.getItemMeta();
		item.setDisplayName("UndoBook");
		ArrayList<String> lore=new ArrayList<String>();
		lore.add("Click this book to undo Kode");
		item.setLore(lore);
		undoStack.setItemMeta(item);
		droidHistory= new HashMap<Location,BlockState>();
		getOwner().getInventory().setItem(9,undoStack);}
		
	public void Live(){
		kodeBuilder.registerEvents(this);
		alive=true;
		//runScheduler();
		}
	
	public void Die(){
		alive=false;		
		FurnaceBurnEvent.getHandlerList().unregister(this);
		FurnaceExtractEvent.getHandlerList().unregister(this);
		FurnaceSmeltEvent.getHandlerList().unregister(this);
		InventoryClickEvent.getHandlerList().unregister(this);
		InventoryDragEvent.getHandlerList().unregister(this);
		InventoryOpenEvent.getHandlerList().unregister(this);
		if (regentNow != null) regentNow.Die();}
	
	public void error(KodeException e){
		ItemStack book = new ItemStack(Material.BOOK_AND_QUILL);
		KodeSchedule schNow = (KodeSchedule)regentNow;
		BookMeta bookMeta = schNow.getBook().getData().clone();
		
		List<String> pages=schNow.getBook().getData().getPages();
		int nextLines = schNow.timeSchedule;
		String[] lines=null;int linecount=0;int pageNum=0;
		for (pageNum=0;pageNum<pages.size();pageNum++){
			lines=pages.get(pageNum).split("\n");
			linecount=lines.length;			
			nextLines-=linecount;
			if (nextLines<0)break;}
		if (nextLines<0){
			lines[nextLines+linecount]="ERROR!!"+lines[nextLines+linecount];
			String page = lines[0];for (int i=1;i<linecount;i++){page=page+"\n"+lines[i];}
			bookMeta.setPage(pageNum+1, page);				
			book.setItemMeta(bookMeta);}
		
		getOwner().getInventory().setItem(18, book.clone());
		cancelSignal=false;errortask(e);}

	@EventHandler(priority = EventPriority.MONITOR)
	public void hookUndo(InventoryOpenEvent e){
		if (e.isCancelled())return;
		if (!e.getPlayer().equals(getOwner()))return;
		//kodeBuilder.lock.lock();
		try{
			KodeDroid droid = kodeBuilder.getDroid(ownerId);
			if (droid==null)return;
			if(!droid.getFurnace().equals(e.getInventory().getHolder()))return;
			getOwner().getInventory().setItem(9,undoStack);}
		catch (InvalidDroidException ex) {}
		//finally{kodeBuilder.lock.unlock();}
		}
	@EventHandler(priority = EventPriority.MONITOR)
	public void hookUndo(InventoryClickEvent e){
		//kodeBuilder.lock.lock();
		try{
			if (!undoStack.equals(e.getCursor()) &&
				!undoStack.equals(e.getCurrentItem()))return;
			e.setCancelled(true);
			KodeDroid droid = kodeBuilder.getDroid(ownerId);
			if (droid==null)return;
			if(!(e.getInventory().getHolder()instanceof Furnace))return;
			if(!droid.getFurnace().equals((Furnace)e.getInventory().getHolder()))return;
			getOwner().getInventory().setItem(9,undoStack);
			KodeBuilderPlugin.debug("debug: click UNDO");
			runUndo();}
		catch (InvalidDroidException ex) {}
		//finally{kodeBuilder.lock.unlock();}
		}
	@EventHandler(priority = EventPriority.MONITOR)
	public void hookUndo(InventoryDragEvent e){
		//kodeBuilder.lock.lock();
		try{
			if (!undoStack.equals(e.getOldCursor()) &&
				!undoStack.equals(e.getCursor()))return;
			e.setCancelled(true);
			KodeDroid droid = kodeBuilder.getDroid(ownerId);
			if (droid==null)return;
			if(!droid.getFurnace().equals(e.getInventory().getHolder()))return;
			getOwner().getInventory().setItem(9,undoStack);
			KodeBuilderPlugin.debug("debug: drag UNDO");
			runUndo();}
		catch (InvalidDroidException ex) {}
		//finally{kodeBuilder.lock.unlock();}
		}
	
	private void runUndo(){
		new BukkitRunnable(){public void run(){
			//kodeBuilder.lock.lock();
			try{
				KodeDroid droid = kodeBuilder.getDroid(ownerId);
				if (droid==null) return;
				droid.getFurnace();
				
				if (nextRunUndo > System.currentTimeMillis()) return;
				nextRunUndo =System.currentTimeMillis()+300;
				
				getOwner().closeInventory();
					
				stop();
				regentNow=new KodeUndo(KodeEnvironment.this,droid);
				regentNow.Live();}
			catch (InvalidDroidException e) {}
			//finally{kodeBuilder.lock.unlock();}
			}}
		.runTaskLater(getKodeBuilder().plugin, 2L);}

	@EventHandler(priority = EventPriority.MONITOR)
	public void hookRunKode(InventoryClickEvent e){
		if (e.isCancelled())return;
		//kodeBuilder.lock.lock();
		try{
			if (e.getRawSlot()>2)return;
			KodeDroid droid = kodeBuilder.getDroid(ownerId);
			if (droid==null) return;
			if(!droid.getFurnace().equals(e.getInventory().getHolder()))return;
			
			final Block block = droid.getFurnace().getBlock();
			final FurnaceInventory inventory = ((Furnace)block.getState()).getInventory();
			if (!e.getInventory().getName().equals(inventory.getName()))return;
			if (inventory.getResult()!=null)return;
			
			final ItemStack sourceItem;

			if (e.getSlot()==0){
				if (e.getCursor()==null)return;
				sourceItem = e.getCursor().clone();}
			else {
				if(inventory.getSmelting()==null)return;
				sourceItem = inventory.getSmelting().clone();}

			if (sourceItem.getType().equals(Material.BOOK_AND_QUILL)){
				KodeBuilderPlugin.debug("debug: clickevent");
				runKode();}}
		catch (InvalidDroidException ex) {}
		//finally{kodeBuilder.lock.unlock();}
		}

	@EventHandler(priority = EventPriority.MONITOR)
	public void hookRunKode(InventoryDragEvent e){
		if (e.isCancelled())return;
		//kodeBuilder.lock.lock();
		try{
			if (!e.getRawSlots().contains(0)&&
				!e.getRawSlots().contains(1)&&
				!e.getRawSlots().contains(2)) return;
			KodeDroid droid = kodeBuilder.getDroid(ownerId);
			if (droid==null) return;
			if(!droid.getFurnace().equals(e.getInventory().getHolder()))return;
			
			final Block block = droid.getFurnace().getBlock();
			final FurnaceInventory inventory = ((Furnace)block.getState()).getInventory();
			if (!e.getInventory().getName().equals(inventory.getName()))return;
			if (inventory.getResult()!=null)return;
			
			final ItemStack sourceItem;
			
			if (e.getRawSlots().contains(0)){
				if (e.getCursor()==null)return;
				sourceItem = e.getCursor().clone();}
			else {
				if(inventory.getSmelting()==null)return;
				sourceItem = inventory.getSmelting().clone();}

			if (sourceItem.getType().equals(Material.BOOK_AND_QUILL)){
				KodeBuilderPlugin.debug("debug: dragevent");
				runKode();}}
		catch (InvalidDroidException ex) {}
		//finally{kodeBuilder.lock.unlock();}
		}

	@EventHandler(priority = EventPriority.MONITOR)
	public void hookRunKode(FurnaceSmeltEvent e){
		if (e.isCancelled())return;
		//kodeBuilder.lock.lock()
		;try{
			KodeDroid droid = kodeBuilder.getDroid(ownerId);
			if (droid==null) throw new RuntimeException ("");
			if(!droid.getFurnace().equals(e.getBlock().getState()))return;
			
			final Block block = droid.getFurnace().getBlock();
			final FurnaceInventory inventory = ((Furnace)block.getState()).getInventory();
			if (inventory.getResult()!=null)return;
			
			final ItemStack sourceItem = inventory.getSmelting().clone();
			
			if (sourceItem.getType().equals(Material.BOOK_AND_QUILL)){
				KodeBuilderPlugin.debug("debug: smeltevent");
				e.setCancelled(true);
				inventory.setSmelting(sourceItem.clone());
				runKode();}}
		catch (InvalidDroidException ex) {
			e.setCancelled(true);
		}
		//finally{kodeBuilder.lock.unlock();}
		}

	@EventHandler(priority = EventPriority.MONITOR)
	public void hookRunKode(FurnaceExtractEvent e){
		//kodeBuilder.lock.lock();
		try{
			KodeDroid droid = kodeBuilder.getDroid(ownerId);
			if (droid==null) return;
			if(!droid.getFurnace().equals(e.getBlock().getState()))return;
			
			final Block block = droid.getFurnace().getBlock();
			final FurnaceInventory inventory = ((Furnace)block.getState()).getInventory();
			
			final ItemStack sourceItem = inventory.getSmelting().clone();
			
			if (sourceItem.getType().equals(Material.BOOK_AND_QUILL)){
				KodeBuilderPlugin.debug("debug: extractevent");
				runKode();}}
		catch (InvalidDroidException ex) {}
		//finally{kodeBuilder.lock.unlock();}
		}

	@EventHandler(priority = EventPriority.MONITOR)
	public void hookRunKode(FurnaceBurnEvent e){
		if (e.isCancelled())return;
		//kodeBuilder.lock.lock();
		try{
			KodeDroid droid = kodeBuilder.getDroid(ownerId);
			if (droid==null) return;
			if(!droid.getFurnace().equals(e.getBlock().getState()))return;
			
			final Block block = droid.getFurnace().getBlock();
			final FurnaceInventory inventory = ((Furnace)block.getState()).getInventory();
			if (inventory.getResult()!=null)return;
			
			final ItemStack sourceItem = inventory.getSmelting().clone();
						
			if (sourceItem.getType().equals(Material.BOOK_AND_QUILL)){
				KodeBuilderPlugin.debug("debug: burnevent");
				runKode();}}
		catch (InvalidDroidException ex) {}
		//finally{kodeBuilder.lock.unlock();}
		}
	
	
	private void runKode(){
		if (nextRunKode > System.currentTimeMillis()) return;
		nextRunKode =System.currentTimeMillis()+300;
		new BukkitRunnable(){public void run(){
			//kodeBuilder.lock.lock();
			try{
			
			KodeDroid droid = kodeBuilder.getDroid(ownerId);
			Furnace furnace = droid.getFurnace();
			if (furnace.getType().equals(Material.BURNING_FURNACE)){
				ItemStack sourceItem = furnace.getInventory().getSmelting();
				if (sourceItem != null) {
					sourceItem=sourceItem.clone();
					if (sourceItem.getType().equals(Material.BOOK_AND_QUILL)){
						getOwner().closeInventory();		
						getOwner().getInventory().setItem(18, sourceItem.clone());
						furnace.getInventory().setSmelting(null);
						runKode(sourceItem);}}}}
			catch (InvalidDroidException e) {}
			//finally{kodeBuilder.lock.unlock();}
		}}.runTaskLater(getKodeBuilder().plugin, 2L);}
	
	private void runKode(ItemStack sourceItem){
		KodeBuilderPlugin.debug("debug: running book to kode");
		stop();
		KodeBook kodeBook = KodeBook.makeKodeBook(sourceItem);
		KodeBuilderPlugin.debug("debug: env make sch");
		regentNow=new KodeSchedule(KodeEnvironment.this,kodeBook);
		droidHistory.clear();
		KodeBuilderPlugin.debug("debug: env start sch");
		regentNow.Live();}
	
	
	void stop(){
		if (regentNow != null) {
		if (regentNow.isRunning()) regentNow.Stopped();
		taskQueue.clear();
		cancelSignal=true;
		regentNow.Die();
		regentNow=null;}}
	
	private void errortask(final KodeException e){
		try{			
		if (!cancelSignal){
			KodeDroid droid = kodeBuilder.getDroid(ownerId);
			droid.getFurnace().getWorld().playEffect(droid.getFurnace().getLocation().add(0.5, 0.5, 0.5), Effect.SMOKE,0);
			new BukkitRunnable(){
						public void run(){errortask(e);}}.runTaskLater(kodeBuilder.plugin, particleInterval);}
		else cancelSignal=false;}
		catch (InvalidDroidException ex){}}
	public class Rescheduling extends RuntimeException{
		private static final long serialVersionUID = -1008622808641002410L;
		BukkitRunnable rescheduleTask;
		int rescheduleLoad;
		public Rescheduling(BukkitRunnable RescheduleTask, int RescheduleLoad){rescheduleTask=RescheduleTask;rescheduleLoad=RescheduleLoad;}}}


 


