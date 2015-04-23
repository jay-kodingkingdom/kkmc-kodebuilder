package com.kodingkingdom.kodebuilder.var;

import org.bukkit.block.BlockState;

public class MatVar extends KodeVar{
	public MatVar(String Name){name=Name;}
	BlockState state;
	@Override
	public void setdata(Object Data){
		if (!(Data instanceof BlockState))throw new IllegalArgumentException();
		state=((BlockState)Data);}
	@Override
	public BlockState getdata(){
		return state;}}
