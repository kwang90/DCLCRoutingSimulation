package de.tum.ei.lkn.eces.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TestLog {

	DecimalFormat df = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.GERMAN));
	
	private PrintWriter writer = null;
	private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private Date date = new Date();
	private String filename = null;
	private String path = null;
	
	public TestLog(String testName)
	{
		//create file path
		String pathResultDirectory = "." +  File.separator + "Tests_Data";
		if(! new File(pathResultDirectory).exists())
			new File(pathResultDirectory).mkdir();
		filename = "ROUTINGTEST_" + testName + "_" + dateFormat.format(date).toString() + ".csv";
		path = "." + File.separator  + "Tests_Data" + File.separator + filename;		
		//
		File f = new File(path);
		try {
			f.createNewFile();
			writer = new PrintWriter(path, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void log(String top, int NumberOfSendingNodes, int NumberOfReceivingNodes, String AUT)
	{
		String log = top + ";" + NumberOfSendingNodes + ";" + NumberOfReceivingNodes + ";" + AUT;
		writer.println(log);
		writer.flush();
	}
	
	public void log(String AUT, long src, long dest, double Cost, double delay, long runtime)
	{
		String log = AUT + ";" + src + ";"  + dest + ";" + Cost  + ";" + delay + ";" + runtime;
		writer.println(log);
		writer.flush();
	}
	
	public void log(String AUT, int cnt, double sumCost, double sumDelay, double sumRunTime){
		String log =  AUT + ";" + cnt + ";"  + sumCost + ";" + sumDelay  + ";" + sumRunTime;
		writer.println(log);
		writer.flush();
	}
	
	public void logSectionSeperater(){
		String log =  "==========================";
		writer.println(log);
		writer.flush();
	}
}
