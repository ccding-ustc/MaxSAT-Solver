package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import cs.ustc.MaxSATsolver.*;

public class GenCVS {
	public static void main(String[] args) throws IOException {
		File rootPath = new File(args[0]);
		File[] paths = rootPath.listFiles();
		FileWriter fw = new FileWriter(new File("density_industrial.txt"));
		for(File path: paths){
			List<File> files = FilesReader.getCNFFiles(path);
	 		for(File file: files){
	 			System.out.println(file.getName());
	 			IFormula f = new CNFFileReader().initFormulaOfCNFFile(file.getPath());
	 			int edgeCount = 0;
	 			for(IVariable v1: f.vars) {
	 				edgeCount+=v1.neighbors.keySet().size();
	 			}
	 			fw.write(file.getName()+"  "+String.format("%.2f",((double)edgeCount/(double)(f.nbVar*f.nbVar-f.nbVar)))+"\r\n");
	 		}	
		}
		fw.close();
	}
	public static void genNodesXLS(File file) throws IOException {
		Workbook wb = new HSSFWorkbook();
		Sheet s = wb.createSheet();
		IFormula f = new CNFFileReader().initFormulaOfCNFFile(file.getPath());
		Row r = s.createRow(0);
		r.createCell(0).setCellValue("id");
		r.createCell(1).setCellValue("label");
		for(int i=1; i<=f.nbVar; i++) {
			r = s.createRow(i);
			r.createCell(0).setCellValue(i);
			r.createCell(1).setCellValue(i);
		}
		OutputStream os = new FileOutputStream(file.getName()+"_nodes.xls");
 		wb.write(os);
 		wb.close();
 		os.close();
	}
	
	
	public static void genCVS(File file, IFormula f, List<ILeague> leagues, List<IClause> unsatClas) throws IOException{
		Workbook wb = new HSSFWorkbook();
		Sheet s = wb.createSheet();
		int rowNum = 0;
		Row r = s.createRow(rowNum++);
		r.createCell(0).setCellValue("source");
		r.createCell(1).setCellValue("target");
		for(IVariable v1: f.vars) {
			for(IVariable v2: v1.neighbors.keySet()) {
				if(v1.lit.id<v2.lit.id) {
					r = s.createRow(rowNum++);
					r.createCell(0).setCellValue(v1.lit.id);
					r.createCell(1).setCellValue(v2.lit.id);
				}
			}
		}
		OutputStream os = new FileOutputStream(file.getName()+".xls");
 		wb.write(os);
 		wb.close();
 		os.close();

		FileWriter fw = new FileWriter(new File(file.getName()+".txt"));
		for(ILeague l: leagues) {
			fw.write(l.id+": {"+l.toString()+"}\r\n");
		}
		for(IClause c: unsatClas) {
			fw.write(c.toString()+" ");
		}
		fw.close();
	}

}
