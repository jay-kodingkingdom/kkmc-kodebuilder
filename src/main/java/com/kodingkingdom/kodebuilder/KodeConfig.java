package com.kodingkingdom.kodebuilder;

import org.bukkit.configuration.file.FileConfiguration;

public enum KodeConfig {

	LOADAMOUNT("kodebuilder.loadamount"),
	LOADTIME("kodebuilder.loadtime")
	;
	
	public final String config;
	
	private KodeConfig(String Config){
		config=Config;}
		
	public static void loadConfig(){
		KodeBuilderPlugin plugin = KodeBuilderPlugin.getPlugin();
		KodeBuilder kodeBuilder = plugin.getKodeBuilder();
		FileConfiguration config = plugin.getConfig();
		
		try{
			kodeBuilder.setLoadAmountLimit(config.getLong(LOADAMOUNT.config));
			kodeBuilder.setLoadTimeLimit(config.getLong(LOADTIME.config));
			plugin.getLogger().info("Config successfully loaded");}
		
		catch(Exception e){
			plugin.getLogger().severe("Could not load config!");
			plugin.getLogger().severe("ERROR MESSAGE: "+e.getMessage());
			e.printStackTrace();
			config.set("kodebuilder.ERROR", true);}}
			
	
	public static void saveConfig(){
		KodeBuilderPlugin plugin = KodeBuilderPlugin.getPlugin();
		KodeBuilder kodeBuilder = plugin.getKodeBuilder();
		FileConfiguration config = plugin.getConfig();

		if (config.isSet("craftercoordinator.ERROR")){
			plugin.getLogger().info("Config state invalid, will not save");
			return;}
		try{
			for(String key : config.getKeys(false)){
				 config.set(key,null);}
			config.set(LOADAMOUNT.config,kodeBuilder.getLoadAmountLimit());
			config.set(LOADTIME.config,kodeBuilder.getLoadTimeLimit());

			plugin.saveConfig();
			plugin.getLogger().info("Config successfully saved");}
		catch(Exception e){
			plugin.getLogger().severe("Could not save config!");
			plugin.getLogger().severe("ERROR MESSAGE: "+e.getMessage());
			e.printStackTrace();}}}
