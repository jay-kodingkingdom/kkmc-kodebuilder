package com.kodingkingdom.kodebuilder;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.scheduler.BukkitRunnable;

import com.kodingkingdom.craftercoordinator.CrafterCoordinator;
import com.kodingkingdom.craftercoordinator.CrafterCoordinatorPlugin;
import com.kodingkingdom.kodebuilder.KodeDroid.InvalidDroidException;
import com.kodingkingdom.kodebuilder.KodeEnvironment.Rescheduling;
import com.kodingkingdom.kodebuilder.schedule.KodeType;
import com.kodingkingdom.kodebuilder.type.construction.build.BuildCircleType;
import com.kodingkingdom.kodebuilder.type.construction.build.BuildCubeType;
import com.kodingkingdom.kodebuilder.type.construction.build.BuildLineType;
import com.kodingkingdom.kodebuilder.type.construction.build.BuildSphereType;
import com.kodingkingdom.kodebuilder.type.construction.build.BuildSquareType;
import com.kodingkingdom.kodebuilder.type.construction.fill.FillCircleType;
import com.kodingkingdom.kodebuilder.type.construction.fill.FillCubeType;
import com.kodingkingdom.kodebuilder.type.construction.fill.FillSphereType;
import com.kodingkingdom.kodebuilder.type.construction.fill.FillSquareType;
import com.kodingkingdom.kodebuilder.type.construction.misc.GetType;
import com.kodingkingdom.kodebuilder.type.construction.misc.GoType;
import com.kodingkingdom.kodebuilder.type.construction.misc.PutType;
import com.kodingkingdom.kodebuilder.type.misc.CommentType;
import com.kodingkingdom.kodebuilder.type.misc.SayType;
import com.kodingkingdom.kodebuilder.type.scope.EndScope;
import com.kodingkingdom.kodebuilder.type.scope.IfScope;
import com.kodingkingdom.kodebuilder.type.scope.ElseScope;
import com.kodingkingdom.kodebuilder.type.scope.LoopScope;
import com.kodingkingdom.kodebuilder.type.var.IntVarType;

public class KodeBuilder implements Listener {
	KodeBuilderPlugin plugin;
	
	HashMap<UUID,KodeEnvironment> envs;
	HashMap<UUID,KodeDroid> droids;
	HashSet<KodeType> blktypes;
	FurnaceRecipe bookRecipe;
	
	
	//ReentrantLock lock=new ReentrantLock(true);
	volatile long loadAmountLimit=1000*5;
	volatile long loadTimeLimit=7;
	
	public long getLoadAmountLimit(){return loadAmountLimit;}
	public long getLoadTimeLimit(){return loadTimeLimit;}
	public void setLoadAmountLimit(long LoadAmountLimit){loadAmountLimit=LoadAmountLimit;}
	public void setLoadTimeLimit(long LoadTimeLimit){loadTimeLimit=LoadTimeLimit;}
	public HashMap<UUID,KodeEnvironment> getEnvironments(){
    	return envs;}	
	
	public KodeBuilder(KodeBuilderPlugin Plugin) {
		plugin=Plugin;
		envs=new HashMap<UUID,KodeEnvironment>();
		droids=new HashMap<UUID,KodeDroid>();
		bookRecipe=new FurnaceRecipe(new ItemStack(Material.ENCHANTED_BOOK), Material.BOOK_AND_QUILL);
		
		blktypes = new HashSet<KodeType>();
		blktypes.add(new GetType());
		blktypes.add(new PutType());
		blktypes.add(new GoType());
		blktypes.add(new CommentType());
		blktypes.add(new SayType());
		blktypes.add(new BuildLineType());	
		blktypes.add(new BuildSquareType());
		blktypes.add(new BuildCubeType());		
		blktypes.add(new BuildCircleType());
		blktypes.add(new BuildSphereType());
		blktypes.add(new FillSquareType());
		blktypes.add(new FillCubeType());		
		blktypes.add(new FillCircleType());
		blktypes.add(new FillSphereType());
		blktypes.add(new IntVarType());
		blktypes.add(new LoopScope());
		blktypes.add(new IfScope());	
		blktypes.add(new ElseScope());		
		blktypes.add(new EndScope());}

	public void Live(){
		KodeConfig.loadConfig();
		for (Player player : Bukkit.getOnlinePlayers()){
			envs.put(player.getUniqueId(), new KodeEnvironment(this, blktypes, player));
			envs.get(player.getUniqueId()).Live();}
		
		registerEvents(this);
		Bukkit.getServer().addRecipe(bookRecipe);
		runScheduler();} 
	
    public void Die(){
    	PlayerJoinEvent.getHandlerList().unregister(this);
    	PlayerQuitEvent.getHandlerList().unregister(this);
    	BlockPlaceEvent.getHandlerList().unregister(this);
    	BlockBreakEvent.getHandlerList().unregister(this);
    	Iterator<Recipe> recipeIterator = Bukkit.getServer().recipeIterator();
    	while (recipeIterator .hasNext()) {
    		  Recipe recipe = recipeIterator.next();
    		  if (recipe.equals(bookRecipe)) {
    			  recipeIterator.remove();
    			  break;}}
    	
    	for (KodeDroid droid : droids.values()) {
        	droid.getBlockState().update(true);}
    	for (KodeEnvironment env : envs.values()) {
        	env.Die();}
		KodeConfig.saveConfig();}

	@EventHandler(priority=EventPriority.MONITOR)
    public void playerjoin(PlayerJoinEvent e){
		if (!envs.containsKey(e.getPlayer().getUniqueId())){
			envs.put(e.getPlayer().getUniqueId(), new KodeEnvironment(this, blktypes, e.getPlayer()));
			envs.get(e.getPlayer().getUniqueId()).Live();}}

	/*@EventHandler(priority=EventPriority.HIGHEST)
    public void playerquit(PlayerQuitEvent e){
		if (!envs.containsKey(e.getPlayer().getUniqueId())) return;
    	envs.get(e.getPlayer().getUniqueId()).Die();
		envs.remove(e.getPlayer().getUniqueId());}*/

	@EventHandler(priority = EventPriority.MONITOR)
	public void placeblock(BlockPlaceEvent e){
		KodeBuilderPlugin.debug("pla blk contend");
		//lock.lock();//envs.get(e.getPlayer().getUniqueId()).lock.lock();
//		try{
			if (Material.FURNACE.equals(e.getBlock().getType())){
				if (droids.containsKey(e.getPlayer().getUniqueId())){
					envs.get(e.getPlayer().getUniqueId()).stop();
					e.getBlockPlaced().setType(Material.AIR);
					droids.get(e.getPlayer().getUniqueId()).move(e.getBlockPlaced().getLocation());}
				else droids.put(e.getPlayer().getUniqueId(), new KodeDroid((Furnace)e.getBlock().getState(),envs.get(e.getPlayer().getUniqueId())));}//}
		//finally{lock.unlock();//envs.get(e.getPlayer().getUniqueId()).lock.unlock();}
		}
	@EventHandler(priority=EventPriority.MONITOR)
	public void breakblock(BlockBreakEvent e){
		KodeBuilderPlugin.debug("brk blk contend");
		//lock.lock();
		try{
			if (Material.FURNACE.equals(e.getBlock().getType())||
					Material.BURNING_FURNACE.equals(e.getBlock().getType())) {
				e.setCancelled(true);
				if (droids.containsKey(e.getPlayer().getUniqueId())){
					if(getDroid(e.getPlayer().getUniqueId()).getFurnace().equals(e.getBlock().getState())){
						envs.get(e.getPlayer().getUniqueId()).stop();
						droids.get(e.getPlayer().getUniqueId()).getBlockState().update(true,true);}}}}
		catch(InvalidDroidException ex){}
		//finally{lock.unlock();}
		}
	
    public KodeDroid getDroid(UUID playerId){
		return droids.get(playerId);}
    
    void registerEvents(Listener listener){
    	plugin.getServer().getPluginManager().registerEvents(listener, plugin);}
    
	private void flushScheduler(){
		long loadLeft=loadAmountLimit;
		ArrayList<Map.Entry<KodeEnvironment,Queue<Map.Entry<BukkitRunnable,Integer>>>> taskQueues = new ArrayList<Map.Entry<KodeEnvironment,Queue<Map.Entry<BukkitRunnable,Integer>>>> (); 
		for (KodeEnvironment env : getEnvironments().values()){
			if (!env.taskQueue.isEmpty()) taskQueues.add(new AbstractMap.SimpleEntry<KodeEnvironment,Queue<Map.Entry<BukkitRunnable,Integer>>>(env,env.taskQueue));}
		int currQueueNum = -1;
		
		while (loadLeft>0&&!taskQueues.isEmpty()){
			currQueueNum=(currQueueNum+1)%taskQueues.size();
			Map.Entry<BukkitRunnable,Integer> task=taskQueues.get(currQueueNum).getValue().poll();
			try{
				//if (task.getValue().getValue())
					try{task.getKey().run();}
					catch(Rescheduling rescheduling){
						taskQueues.get(currQueueNum).getKey().scheduleSyncTask(rescheduling.rescheduleTask,rescheduling.rescheduleLoad);}
				//else asyncTaskPool.execute(task.getKey());
					}
			catch(Exception e){
				Bukkit.getLogger().log(Level.SEVERE, "Exception encountered in task!");
				Bukkit.getLogger().log(Level.SEVERE, task.getKey().toString());
				e.printStackTrace();}
			loadLeft-=task.getValue();			
			if (taskQueues.get(currQueueNum).getValue().isEmpty())taskQueues.remove(currQueueNum);}}

	private void runScheduler(){
		KodeBuilderPlugin.debug("sch contend");
		long beginTime = System.currentTimeMillis();
			//lock.lock();
			try{
				flushScheduler();}
			catch(Exception e){
				Bukkit.getLogger().log(Level.SEVERE, "Exception encountered in scheduler!");
				e.printStackTrace();}
			//finally{lock.unlock();}
		//if (alive) 
		long endTime = System.currentTimeMillis();
		long tickOffset = (endTime-beginTime)/50;
		(new BukkitRunnable(){public void run(){runScheduler();}}).runTaskLater(KodeBuilderPlugin.instance, loadTimeLimit+tickOffset);}
	    
    private static CrafterCoordinator coordinator;
    public static CrafterCoordinator getCoordinator(){
    	if (coordinator==null)coordinator=CrafterCoordinatorPlugin.getPlugin().getCoordinator();
    	return coordinator;}}
