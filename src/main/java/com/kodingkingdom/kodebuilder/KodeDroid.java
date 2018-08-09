package com.kodingkingdom.kodebuilder;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.kodingkingdom.craftercoordinator.CrafterRegion;
import com.kodingkingdom.craftercoordinator.CrafterSchool;
import com.kodingkingdom.kodebuilder.var.MatVar;
import com.kodingkingdom.kodebuilder.schedule.KodeSchedule;

public class KodeDroid implements Listener{
	private BlockState blockState;
	private Furnace furnaceState;
	KodeEnvironment env;

	BlockState getBlockState(){
		//env.kodeBuilder.lock.lock();//env.lock.lock();
		//try{
			return blockState;//}
		//finally{env.kodeBuilder.lock.lock();//env.lock.unlock();
		}//}
	void setBlockState(BlockState newBlockState){
		blockState=newBlockState;}
	
	public KodeDroid(BlockState prior_state, Furnace FurnaceState, KodeEnvironment Env){
		env=Env;furnaceState=FurnaceState;blockState=prior_state;
		/*furnaceState.update(true);*/}
	
	public Furnace getFurnace() throws InvalidDroidException{
		//env.lock.lock();try{
			try {return (Furnace)furnaceState.getBlock().getState();}
			catch (NullPointerException e){throw new InvalidDroidException();}
			catch (ClassCastException e){throw new InvalidDroidException();}}//finally{env.lock.unlock();}}
	
	public void get(Location loc, long slotNum){
		//env.lock.lock();try{
		checkSlotGet(slotNum);
		KodeSchedule schNow=(env.regentNow instanceof KodeSchedule?(KodeSchedule)env.regentNow:null);
		
			BlockState copyState=get(loc);
			MatVar matVar = new MatVar("s"+slotNum);matVar.setdata(copyState);
			if (schNow!=null) schNow.setMatVar((int)slotNum,matVar);
			ItemStack item = new ItemStack(copyState.getData().toItemStack(1));
			env.getOwner().getInventory().setItem((int)slotNum-1,item);
			env.getOwner().updateInventory();}//finally{env.lock.unlock();}}
	public void put(Location loc, long slotNum){
		//env.lock.lock();try{
		checkSlotPut(slotNum);	
		KodeSchedule schNow=(env.regentNow instanceof KodeSchedule?(KodeSchedule)env.regentNow:null);
			
			ItemStack item = env.getOwner().getInventory().getItem((int)slotNum-1);
			if (item==null)item=new ItemStack(Material.AIR);
			
			if (schNow!=null && schNow.getMatVar(slotNum)!=null)
				put(loc,schNow.getMatVar(slotNum).getdata().getType(),schNow.getMatVar(slotNum).getdata().getData(),true);
			else
				put(loc,item.getType(),item.getData(),true);}//finally{env.lock.unlock();}}

	public void checkSlotGet(long slotNum){
		if (slotNum<1 || slotNum>9) throw new IllegalArgumentException("Slot number must be between 1 and 9!");}
	public void checkSlotPut(long slotNum){
		if (slotNum<1 || slotNum>9) throw new IllegalArgumentException("Slot number must be between 1 and 9!");
		if (env.getOwner().getInventory().getItem((int)slotNum-1)!=null&&
				env.getOwner().getInventory().getItem((int)slotNum-1).getType().equals(Material.FURNACE)) throw new IllegalArgumentException("You cannot build more droids!");}
	public long checkSide(long side){
		if (side>=0) return side;
		else return -side;}
	public BlockFace checkFace(long side, BlockFace face){
		if (side>=0) return face;
		else return face.getOppositeFace();}

	public void move(Location loc, BlockState state){
		//env.lock.lock();try{
			boolean move = KodeBuilder.getCoordinator().checkPlayerLimit(env.getOwnerId());
			if (!move)for (CrafterRegion region : KodeBuilder.getCoordinator().getPlayerRegion(env.getOwner().getUniqueId()).values()){
				if (region.isIn(loc)) {move=true;break;}}
			if (!move) for (CrafterSchool school : KodeBuilder.getCoordinator().getSchools().values()){
				if (school.getPlayers().contains(env.getOwnerId()))
					for (CrafterRegion region : KodeBuilder.getCoordinator().getSchoolRegion(school.getName()).values()){
						if (region.isIn(loc)){move=true;break;}}}
			if (move) {				
				BlockState fromblock = furnaceState;
				BlockState toblock = state;
				blockState.update(true, false);
				blockState=state;
				toblock.setType(fromblock.getType());
				toblock.setData(fromblock.getData());
				toblock.update(true, false);
				furnaceState=(Furnace)(toblock.getBlock().getState());}}
	public void move(Location loc){
		this .move(loc, loc .getBlock() .getState());}//finally{env.lock.unlock();}}
	
	
	
	BlockState get(Location loc){
		if (loc.equals(blockState.getLocation())) {
			BlockState copyState = blockState.getBlock().getState();
			copyState.setType(blockState.getType());
			copyState.setData(blockState.getData());
			return copyState;}
		else return loc.getBlock().getState();}
	void put(Location loc, Material material, MaterialData data, boolean historical){
		boolean put = KodeBuilder.getCoordinator().checkPlayerLimit(env.getOwnerId());
		if (!put)for (CrafterRegion region : KodeBuilder.getCoordinator().getPlayerRegion(env.getOwnerId()).values()){
			if (region.isIn(loc)) {put=true;break;}}
		if (!put) for (CrafterSchool school : KodeBuilder.getCoordinator().getSchools().values()){
			if (school.getPlayers().contains(env.getOwnerId()))
				for (CrafterRegion region : KodeBuilder.getCoordinator().getSchoolRegion(school.getName()).values()){
					if (region.isIn(loc)){put=true;break;}}}
		if (put==false)return;
		
		if (loc.equals(blockState.getLocation())){
			if (material==Material.FURNACE||material==Material.BURNING_FURNACE)return;
			if (historical) env.droidHistory.putIfAbsent(loc, blockState);
			blockState=blockState.getBlock().getState();
			blockState.setType(material);
			blockState.setData(data);}
		else{
			BlockState block = loc.getBlock().getState();
			if (historical) env.droidHistory.putIfAbsent(loc, block);
			block = block.getBlock().getState();
			block.setType(material);
			block.setData(data);
			block.update(true, false);}}
	
	public class InvalidDroidException extends Exception{
		private static final long serialVersionUID = -6927840541868691753L;
		//throw new IllegalStateException("Furnace doesn't exist!");
	}}
