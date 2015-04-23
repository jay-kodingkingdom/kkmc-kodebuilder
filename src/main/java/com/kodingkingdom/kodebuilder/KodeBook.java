package com.kodingkingdom.kodebuilder;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;


public class KodeBook{	
	ItemStack bookStack;
	
	public BookMeta getData(){return (BookMeta)bookStack.getItemMeta();}
	private KodeBook(ItemStack BookStack){
		if (!(BookStack.getItemMeta() instanceof BookMeta)) throw new IllegalArgumentException();
		bookStack=BookStack.clone();}	
	public static KodeBook makeKodeBook(ItemStack BookStack) {
		return new KodeBook(BookStack);}}