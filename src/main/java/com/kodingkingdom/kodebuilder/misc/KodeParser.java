package com.kodingkingdom.kodebuilder.misc;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

import com.kodingkingdom.kodebuilder.KodeBuilderPlugin;
import com.kodingkingdom.kodebuilder.schedule.KodeSchedule;
import com.kodingkingdom.kodebuilder.var.KodeVar;
import com.kodingkingdom.kodebuilder.var.MatVar;
import com.kodingkingdom.kodebuilder.var.NumVar;

public class KodeParser { private KodeParser(){}
	private static final KodeParser p = new KodeParser();
//	private static final Pattern ConstantPattern = Pattern.compile("([0-9]+)");
//	private static final Pattern VariablePattern = Pattern.compile("([a-zA-Z]+)");
//	private static final Pattern ValuePattern = Pattern.compile("("+ConstantPattern.pattern()+"|"+VariablePattern.pattern()+"")");
//	private static final Pattern OperatorPattern = Pattern.compile("([\\+-\\*/])");
//	private static final Pattern AssignmentPattern = Pattern.compile("("+VariablePattern.pattern()+"(\\:)"+")");
//	private static final Pattern ExpressionPattern = Pattern.compile("("+ValuePattern.pattern()+"("+OperatorPattern.pattern()+VariablePattern.pattern()+")*"+")");
//	private static final Pattern ExpressionAssignmentPattern = Pattern.compile("("+AssignmentPattern.pattern()+ExpressionPattern.pattern()+")");
	
	private static final Pattern badfrontparens = Pattern.compile("(([a-zA-Z0-9]\\()|(\\([\\+\\-\\*/])|(\\(\\)))");
	private static final Pattern badbackparens = Pattern.compile("((\\)[a-zA-Z0-9])|([\\+\\-\\*/]\\)))");
	private static final Pattern badchars = Pattern.compile("([^\\+\\-\\*/a-zA-Z0-9\\(\\)]|([a-zA-Z][0-9])|([0-9][a-zA-Z]))");
	private static final Pattern goodpattern = Pattern.compile("(([a-zA-Z0-9]+)([\\+\\-\\*/][a-zA-Z0-9]+)*)");
	private static final Pattern slotvar = Pattern.compile("s([1-9])"); 
	
	public static Parser<Expression> getConstantParser(final String expr){
		char ch;
		for (int i=0;i<expr.length();i++){
			ch=expr.charAt(i);
			if (ch >= '0' && ch <= '9')continue;
			return null;}
		return new Parser<Expression>(){
			public Expression Parse(){
				Expression temp = p.new Expression();temp.text=expr;temp.value=Long.valueOf(temp.text);
				return temp;}};}
	
	public static Parser<Expression> getVariableParser(final String expr){
		char ch;
		for (int i=0;i<expr.length();i++){
			ch=expr.charAt(i);
			if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))continue;
			return null;}
		return new Parser<Expression>(){
			public Expression Parse(){
				Expression temp = p.new Expression();temp.text=expr;
				return temp;}};}
					
	public static Parser<Expression> getValueParser(final String expr){
		Boolean v = null;char ch;
		for (int  i=0;i<expr.length();i++){
			ch=expr.charAt(i);
			if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')){if (v==true)return null; v=false;continue;}
			if (ch >= '0' && ch <= '9'){if (v==false)return null; v=true;continue;}
			return null;}
		final Boolean V = v;
		return new Parser<Expression>(){
			public Expression Parse(){
				Expression temp = p.new Expression();temp.text=expr;if(V==true)temp.value=Long.getLong(temp.text);
				return temp;}};}
		
	public static Parser<Condition> getConditionParser(String cond){
		final String compars = cond.replaceAll("[^=><]", "");
		if (compars.length()!=1)return null;
		KodeBuilderPlugin.debug("debug: compars="+compars);
		Matcher condpattern = Pattern.compile("([^=><]+)"+compars+"([^=><]+)").matcher(cond);
		KodeBuilderPlugin.debug("debug: groupcount="+condpattern.groupCount());
		if (!condpattern.matches())return null;
		
		if (	compars.equals("=") &&
				slotvar.matcher(condpattern.group(1)).matches() &&
				slotvar.matcher(condpattern.group(2)).matches()){
			final String leftSlot=condpattern.group(1);
			final String rightSlot=condpattern.group(2);
			return new Parser<Condition>(){
				public Condition Parse(){
					Condition c = p.new Condition();
					c.comparison=Comparison.EQUALTO;
					c.leftExpression=p.new Expression();c.leftExpression.text=leftSlot;
					c.rightExpression=p.new Expression();c.rightExpression.text=rightSlot;			
					return c;}};}
		
		final Parser<Expression> leftexprparser = getExpressionParser(condpattern.group(1));
		if (leftexprparser==null)return null;
		final Parser<Expression> rightexprparser = getExpressionParser(condpattern.group(2));
		if (rightexprparser==null)return null;		
		return new Parser<Condition>(){
			public Condition Parse(){
				Comparison comp												=Comparison.values()[0];
				for (Comparison c : Comparison.values()){
					if (!String.valueOf(c.getChar()).equals(compars)) continue;
					comp=c;break;}
				Condition c = p.new Condition();
				c.comparison=comp;c.leftExpression=leftexprparser.Parse();c.rightExpression=rightexprparser.Parse();			
				return c;}};}
	
	public static Parser<Expression> getExpressionParser(final String expr){
		if (badchars.matcher(expr).find())return null;
		if (!goodpattern.matcher(expr.replaceAll("[\\(\\)]", "")).matches())return null;
		if (badfrontparens.matcher(expr).find())return null;
		if (badbackparens.matcher(expr).find())return null;
		String parens = expr.replaceAll("[^\\(\\)]", "");long l=0;
		for (int i=0;i<parens.length();i++){
			if(parens.charAt(i)=='('){l++;continue;}
			if(parens.charAt(i)==')'){l--;if (l<0) return null;}}
		if (l!=0)return null;		
		return new Parser<Expression>(){
			public Expression Parse(){
				return ParseLine(expr);}};}		
		private static Expression ParseLine(String expr){
		Expression temp;char ch;
		int l=0;int lastpos=0;
		Boolean v=null;StringBuilder sb = new StringBuilder();
		
		ArrayList<Expression> result = new ArrayList<Expression>();
		
		for (int i = 0;i<expr.length();i++){
			ch=expr.charAt(i);
			if (ch=='('){  if (l==0){lastpos=i;if (sb.length()>0){temp = p.new Expression();temp.text=sb.toString();result.add(temp);if(v==true)temp.value=Long.valueOf(temp.text); v=null;sb.setLength(0);}}
				l++;continue;}
			if (ch==')'){  l--;
				if (l==0){temp = ParseLine(expr.substring(lastpos+1, i));result.add(temp);}continue;}
			if (l>0) continue;
			if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')){v=false;sb.append(ch);continue;}
			if (ch >= '0' && ch <= '9'){v=true;sb.append(ch);continue;}
			if (sb.length()>0){temp = p.new Expression();temp.text=sb.toString();result.add(temp);if(v==true)temp.value=Long.valueOf(temp.text); v=null;sb.setLength(0);}
			temp = p.new Expression();temp.text=String.valueOf(ch);result.add(temp);}if (sb.length()>0){temp = p.new Expression();temp.text=sb.toString();result.add(temp);if(v==true)temp.value=Long.valueOf(temp.text);}
		
		while (result.size()>1){
			for (int j=0; j<Operation.values().length;j++){
				for (int i = 1; i<result.size()-1;i++){
					if (! String.valueOf(Operation.values()[j].getChar()).equals(  result.get(i).text)) {continue;}
						temp=result.get(i);temp.text=null;temp.operation=Operation.values()[j];
						temp.leftSubexpression=result.get(i-1);temp.rightSubexpression=result.get(i+1);
						result.remove(i+1);result.remove(i-1);
						i=i-1;}}}
		return result.iterator().next();}
	
	
	public static Expression getExpression(KodeVar Var){
		Expression expr = p.new Expression();
		expr.text=Var.getName();
		return expr;}
		
	private enum Operation{
		DIVISION('/', new operation(){public long perform(long leftvalue, long rightvalue){return leftvalue/rightvalue;}}),
		MULTIPLICATION('*', new operation(){public long perform(long leftvalue, long rightvalue){return leftvalue*rightvalue;}}),
		ADDITION('+', new operation(){public long perform(long leftvalue, long rightvalue){return leftvalue+rightvalue;}}),
		SUBTRACTION('-', new operation(){public long perform(long leftvalue, long rightvalue){return leftvalue-rightvalue;}});
		
		private char charVal;
		private operation func;
		
		char getChar(){return charVal;}
		long perform(long leftvalue, long rightvalue){return func.perform(leftvalue, rightvalue);}
		
		private interface operation {public long perform(long leftvalue, long rightvalue);}
		private Operation(char CharVal, operation Func){charVal=CharVal;func=Func;}}
	private enum Comparison{
		EQUALTO('=', new comparison(){public boolean perform(Expression leftvalue, Expression rightvalue, KodeSchedule sch) throws Exception{
			if (leftvalue.text!=null && leftvalue.value==null && 
				rightvalue.text!=null && rightvalue.value==null){
				MatVar leftVar,rightVar;int slotNum;Matcher leftMatcher,rightMatcher;
				if ((leftMatcher=slotvar.matcher(leftvalue.text)).matches()){
					slotNum=Integer.valueOf(leftMatcher.group(1));ItemStack leftItem;
					if ((leftVar=sch.getMatVar(slotNum))!=null){
						leftItem=((BlockState)leftVar.getdata()).getData().toItemStack(1);}
					else{
						leftItem = sch.getEnvironment().getOwner().getInventory().getItem(slotNum-1);
						leftItem = (leftItem ==null? new ItemStack(Material.AIR):leftItem);}
					
					if ((rightMatcher=slotvar.matcher(rightvalue.text)).matches()){
						slotNum=Integer.valueOf(rightMatcher.group(1));ItemStack rightItem;
						if ((rightVar=sch.getMatVar(slotNum))!=null){
							rightItem=((BlockState)rightVar.getdata()).getData().toItemStack(1);}
						else{
							rightItem = sch.getEnvironment().getOwner().getInventory().getItem(slotNum-1);
							rightItem = (rightItem ==null? new ItemStack(Material.AIR):rightItem);}

						KodeBuilderPlugin.debug("left data is "+leftItem.getData().toString());
						KodeBuilderPlugin.debug("right data is "+rightItem.getData().toString());
						KodeBuilderPlugin.debug("same? "+leftItem.isSimilar(rightItem));
						return leftItem.isSimilar(rightItem);}}}
			
			return leftvalue.getEvaluator(sch).call()==rightvalue.getEvaluator(sch).call();}}),
		GREATERTHAN('>', new comparison(){public boolean perform(Expression leftvalue, Expression rightvalue, KodeSchedule sch) throws Exception{
			return leftvalue.getEvaluator(sch).call()>rightvalue.getEvaluator(sch).call();}}),
		SMALLERTHAN('<', new comparison(){public boolean perform(Expression leftvalue, Expression rightvalue, KodeSchedule sch) throws Exception{
			return leftvalue.getEvaluator(sch).call()<rightvalue.getEvaluator(sch).call();}});
		
		private char charVal;
		private comparison func;

		char getChar(){return charVal;}
		boolean perform (Expression leftvalue, Expression rightvalue, KodeSchedule sch)throws Exception{return func.perform(leftvalue, rightvalue, sch);}
		
		private interface comparison {public boolean perform(Expression leftvalue, Expression rightvalue, KodeSchedule sch) throws Exception;}
		private Comparison(char CharVal, comparison Func){charVal=CharVal;func=Func;}}
		
	public interface Parser<T>{
		public T Parse();}
	
	public class Condition{ private Condition(){}
		private Expression leftExpression;
		private Expression rightExpression;
		private Comparison comparison;
		public Callable<Boolean> getEvaluator(final KodeSchedule sch){
			return new Callable<Boolean>(){
				public Boolean call() throws Exception{					
					return comparison.perform(leftExpression, rightExpression, sch);}};}}
		
	public class Expression{ private Expression(){}
		private Expression leftSubexpression;
		private Expression rightSubexpression;
		private Operation operation;
		private String text=null;
		private Long value=null;
		public Callable<Long> getEvaluator(final KodeSchedule sch){			
			return new Callable<Long>(){
				public Long call() throws Exception{				
					if (text == null) return operation.perform(leftSubexpression.getEvaluator(sch).call(), rightSubexpression.getEvaluator(sch).call());
					if (value != null) return value;
					if (!sch.getKodeVars().containsKey(text)) new IllegalStateException("Cannot find variable!");
					if (sch.getKodeVars().get(text) instanceof NumVar) return ((NumVar)sch.getKodeVars().get(text)).getdata();
					KodeBuilderPlugin.debug("debug: text is"+text);
					throw new IllegalStateException("Cannot understand variable!");}};}}}
