package com.kodingkingdom.kodebuilder;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class KodeBuilderPlugin extends JavaPlugin implements Listener{
	KodeBuilder kodeBuilder=new KodeBuilder(this);
	
	@Override
    public void onEnable(){
		kodeBuilder.Live();} 
	
    @Override
    public void onDisable(){
    	kodeBuilder.Die();}
    
    public KodeBuilder getKodeBuilder(){return kodeBuilder;}
    public static KodeBuilderPlugin getPlugin(){return instance;}
    static KodeBuilderPlugin instance;
    public KodeBuilderPlugin(){instance=this;}
    public static void debug(String msg){
    	instance.getLogger().fine(msg);}}