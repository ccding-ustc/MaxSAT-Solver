package tools;

import java.io.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cs.ustc.MaxSATsolver.*;

public class CalculateDegree {
	public static String calAvgDegreeOfVar(IFormula f) {
		int total = 0;
		for(IVariable v: f.vars) {
			for(int val: v.neighbors.values()) {
				total += val;
			}
		}
		total = total>>1;
		return String.format("%.0f", (double)total/(double)f.vars.length);
	}
	
	public static String calAvgDegreeOfLeague(List<ILeague> leagues) {
		int total = 0;
		for(ILeague l: leagues) {
			for(int val: l.neighbors.values()) {
				total += val;
			}
		}
		total = total>>1;
		return String.format("%.0f", (double)total/(double)leagues.size());
	}
	
	public static void main(String args[]) throws IOException {
		File rootPath = new File(args[0]);
		File[] paths = rootPath.listFiles();

		for(File path: paths){
			FileWriter fw = new FileWriter(new File(path.getName()+".txt"));
			fw.write("file name\tnbVar\tnbClas\tAvgDOV\tLS\tAvgDOL\r\n");
			List<File> files = FilesReader.getCNFFiles(path);
	 		for(File file: files){
	 			ILeague.COUNT = 0;
				IFormula formula = new CNFFileReader().initFormulaOfCNFFile(file.getPath());
				List<ILeague> leagues = formula.constructLeagues();
				formula.setNeighborsOfLeagues(leagues);
				fw.write(file.getName()+"\t"+formula.nbVar+"\t"+formula.nbClas+"\t"+calAvgDegreeOfVar(formula)+"\t");
				fw.write(leagues.size() + "\t"+calAvgDegreeOfLeague(leagues)+"\r\n");
				FileWriter fw2 = new FileWriter(new File(path+file.getName()+".txt"));
				fw2.write(file.getName()+"\tnbVar:"+formula.nbVar+"\tnbClas:"+formula.nbClas+"\t\r\n");
				for(ILeague l: leagues) {
					fw2.write(l.toString()+"\tagents size:"+l.agents.size()+"\tdegree:"+l.degree+"\t");
					fw2.write(l.neighbors.size()+"\t"+l.neighbors.toString()+"\r\n");
				}
				fw2.close();	
				
	 			System.out.println(file.getName());
	 		}	
	 		fw.close();
		}
		
	}
}
