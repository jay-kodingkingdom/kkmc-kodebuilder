package com.kodingkingdom.kodebuilder.schedule;

import java.io.PrintWriter;

import com.kodingkingdom.kodebuilder.KodeDroid;



public class KodeException extends Exception {
	private static KodeException e = new KodeException();private KodeException(){}
	public static ExceptionType kodeType = e.new ExceptionType();
	private static final long serialVersionUID = 1L;
	private static String defaultReason="Double check your code!";
	private Exception innerException=null;
	KodeBlock block;
	String reason=defaultReason;
	public void printStackTrace(){
		if (innerException==null)super.printStackTrace();
		else innerException.printStackTrace();}
	public void printStackTrace(PrintWriter p){
		if (innerException==null)super.printStackTrace(p);
		else innerException.printStackTrace(p);}
	public String getReason(){
		if (!reason.equals(defaultReason)) return reason;
		else if (innerException != null) {
			if (innerException instanceof KodeException) return ((KodeException)innerException).getReason();
			else return innerException.getMessage();}
		else return defaultReason;}
	public KodeException because(String Reason){reason=Reason;return this;}
	public KodeException from(Exception e){innerException=e;return this;}
	public KodeException(KodeBlock Block){block=Block;}

public class ExceptionType extends KodeType {
	public KodeBlock makeKodeBlock(String kodeString, KodeException exception) {
		return new ExceptionBlock(this, kodeString, exception);}
	@Override
	public KodeBlock makeKodeBlock(String kodeString) {
		return makeKodeBlock(kodeString, null);}
	@Override
	public void action(KodeSchedule sch, KodeDroid droid, KodeBlock blk) throws KodeException {
		if (!(blk instanceof ExceptionBlock)) throw new RuntimeException();
		KodeException exception = ((ExceptionBlock)blk).kodeException;
		if (exception==null) throw new KodeException(blk).because("Cannot understand command!");
		else throw exception;}
	private ExceptionType(){}
	class ExceptionBlock extends KodeBlock{
		KodeException kodeException;
		public KodeBlock clone() {
			return new ExceptionBlock(blockType,kodeString,kodeException);}
		private ExceptionBlock(KodeType BlockType, String KodeString, KodeException exception){
			blockType=BlockType;kodeString=KodeString;kodeException=exception;}}}}
