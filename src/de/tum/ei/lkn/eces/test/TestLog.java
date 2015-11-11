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

import de.tum.ei.lkn.eces.networkcalculus.genetic.TopologieSettings.Topologie;

public class TestLog {

	DecimalFormat df = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.GERMAN));
	
	private PrintWriter writer = null;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
	private Date date = new Date();
	private String filename = null;
	private String path = null;
	
	public TestLog(Topologie top)
	{
		String pathResultDirectory = "." + File.separator + ".." + File.separator + ".." + File.separator + "results";
		
		if(! new File(pathResultDirectory).exists())
		{
			new File(pathResultDirectory).mkdir();
		}
		
		filename = dateFormat.format(date).toString() + "_" + top.toString() + "_TEST" + ".csv";
		path = "." + File.separator + ".." + File.separator + ".." + File.separator + "results" + File.separator + filename;
		
		File f = new File(path);
		try {
			f.createNewFile();
			writer = new PrintWriter(path, "UTF-8");
			writer.println("RINGSIZE" + ";" + "LENGTH" + ";" + "QUEUES" + ";" + "FLOWS");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void log(int ringsize, int length, int queues, double flows)
	{
		String log = Integer.toString(ringsize) + ";" + Integer.toString(length) + ";" + Integer.toString(queues) + ";" + df.format(flows);
		writer.println(log);
		writer.flush();
	}
}
