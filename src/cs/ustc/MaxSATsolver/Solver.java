package cs.ustc.MaxSATsolver;
/**
 * incomplete MaxSAT solver
 * @author ccding  2016年3月7日 上午8:39:11
 */


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook; 
import org.apache.poi.ss.usermodel.Row; 
import org.apache.poi.ss.usermodel.Sheet; 
import org.apache.poi.ss.usermodel.Workbook;


public class Solver  {
	static final int  MAX_ITERATIONS = 1000000;
	static final double RANDOM_COEF_SOLUTION = 0.7;
	static final double RANDOM_COEF_INDEPENDENTSET = 0.8;
	static final double RANDOM_COEF_NEXTGROUP = 0;
	
	/**
	 * 将 formula 中每个 literal 视作一个 agent，将所有 agents 按照一定规则分成若干个不相交的联盟
	 * 
	 * @param f 存储 cnf 文件信息的 formula
	 * @param randomCoefIndependentSet 寻找独立集时，采取贪婪策略的随机性大小
	 * @return 不相交的各个分组（联盟）
	 */
	public List<IGroup> getGroups(IFormula f, double randomCoefIndependentSet) {
		List<IGroup> groups = new ArrayList<>();
		while(true){
			IGroup group = new IGroup(f.getIndependentGroup(randomCoefIndependentSet));
			//jump out while loop
			if (group.agents.isEmpty()){
				break;
			}
			groups.add(group);
			f.removeGroupFromFormula(group.agents);
		}
		return groups;
		
	}
	
	/**
	 * 
	 * 
	 * @param formula 存储 cnf 文件信息的 formula f
 	 * @param groups  独立的各个分组
	 * @param randomCoefSolution 每个分组寻找该组最优解时，采取贪婪策略的概率
	 * @param randomCoefNextGroup 组间赋值顺序随机性
	 * @return
	 */
	public List<ILiteral> solveFormulaBasedOnGroups(
			IFormula formula, 
			List<IGroup> groups, 
			double randomCoefSolution, 
			double randomCoefNextGroup)
	{
		IGroup.initGroupNeighbors(groups);
		List<IGroup> tmpGroups = new ArrayList<>(groups);
		List<ILiteral> solution = new ArrayList<>();
		
		//初始组
		IGroup group = tmpGroups.get((int)(Math.random()*tmpGroups.size()));
		List<ILiteral> flipLits;
		while(true){
			//若存在与所有组都不相关的组，则需要随机从剩下的组中选择一个组
			if(group == null){
				group = groups.get((int)(Math.random()*tmpGroups.size()));
			}
			flipLits = group.getSolution(randomCoefSolution);
			
			if(!flipLits.isEmpty()){
				formula.announceSatLits(flipLits);
			}
			solution.addAll(group.solution);
			tmpGroups.remove(group);
			
			if(tmpGroups.isEmpty())
				break;
			if(Math.random() < randomCoefNextGroup){
				//根据相关性寻找下一个组
				int minValue = 0;
				Iterator<Entry<IGroup, Integer>> it = group.neighbors.entrySet().iterator();
				Map.Entry<IGroup, Integer> entry = null;
				while(it.hasNext()){
					entry = it.next();
					if(entry.getValue() >= minValue){
						group = entry.getKey();
						minValue = entry.getValue();
					}
				}
			}else{
				group = tmpGroups.get((int)(Math.random()*tmpGroups.size()));
			}	
		}
		return solution;
	}
	
	/**
	 *  读取 cnf 文件，并将信息存入到 formula 中
	 * @param cnfFile
	 * @return formula
	 * @throws ParseFormatException
	 * @throws IOException
	 */
	public IFormula getFormulaFromCNFFile(String cnfFile) throws ParseFormatException, IOException{
		IFormula f = new IFormula();
		CNFFileReader cnfFileReader = new CNFFileReader();
		cnfFileReader.parseInstance(cnfFile, f);
		f.setVariables();
		f.setVarsNeighbors();
		return f;
	}
	
	/**
	 * 程序入口
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ParseFormatException
	 */
	public static void main(String[] args) throws IOException, ParseFormatException{
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");  
		
		Solver stmp = new Solver();
		IFormula ftmp = stmp.getFormulaFromCNFFile("D:\\Max-SAT2016 benchmarks\\all_instances\\ms_crafted\\bipartite\\maxcut-140-630-0.7\\maxcut-140-630-0.7-16.cnf");
		int[] s = {75 , -53 , -101 , 133 , -99 , 37 , -119 , 113 , -130 , 49 , 47 , -52 , -63 , 74 , -34 , -103 , 127 , -140 , 138 , 35 , 42 , 51 , 50 , -58 , 16 , 80 , -61 , -29 , 9 , 24 , -123 , 2 , 73 , -124 , -12 , 68 , -72 , 78 , -135 , 39 , -7 , -104 , -122 , 36 , -109 , -107 , 105 , 54 , 83 , 57 , -59 , -46 , 71 , 86 , 93 , -45 , 118 , 40 , -96 , -11 , -3 , -132 , -116 , -10 , -8 , -131 , 13 , -91 , 48 , -95 , 112 , -100 , -115 , 14 , -85 , -120 , 28 , -15 , -108 , -106 , 6 , -26 , -30 , -82 , 25 , 90 , 33 , 66 , 128 , 94 , -121 , -31 , 64 , 41 , 19 , 21 , -38 , -110 , 69 , 55 , 4 , 137 , 17 , -87 , 43 , 92 , 134 , 18 , 62 , 76 , 44 , -20 , 97 , -102 , -111 , 1 , -88 , 136 , -114 , -65 , -32 , -56 , -67 , -89 , -98 , 84 , -27 , -5 , 117 , -22 , -126 , -23 , 81 , -79 , 77 , -70 , -139 , -129 , 125 , -60 };
		Set<IClause> satClas = new HashSet<>(ftmp.clauses.size());
		Set<IClause> unsatClas = new HashSet<>(ftmp.clauses);
		for(int ss: s){
			IVariable v = ftmp.variables.get(Math.abs(ss)-1);
			if(ss > 0){
				satClas.addAll(v.lit.getClas());
			}else{
				satClas.addAll(v.oppositeLit.getClas());
			}
		}
		unsatClas.removeAll(satClas);
		System.out.println(satClas.size());
		System.out.println(ftmp.clauses.size() - satClas.size());
	
	    Date dt = new Date();  
	    FileWriter fw = null;
	    SimpleDateFormat sdf = new SimpleDateFormat("MMdd_HHmm");  
	    String dataStr = sdf.format(dt);
	    String outResultPath = args[1]+"results_synchronous_group0.8"+dataStr+".xls";
//	    String outResultAnalysisPath = args[1]+"results_analysis_"+dataStr+".txt";
		
//		fw = new FileWriter(new File(outResultAnalysisPath));
 		Workbook wb = new HSSFWorkbook();
		OutputStream os = null;
		
		Solver solver = new Solver();
		File rootPath = new File(args[0]);
		File[] paths = rootPath.listFiles();
		
		for(File path: paths){
			//跳过 industrial instances
			if(path.getName().equals("ms_industrial"))
				continue;
			
			//获取 path 目录下的所有 .cnf 文件路径
			Path filesPath = Paths.get(path.getAbsolutePath());
	 		final List<File> files = new ArrayList<File>();
	 		SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>(){
	 		    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException{
	 		    	if(file.toFile().getName().endsWith(".cnf"))
	 		    		files.add(file.toFile());
	 		        return super.visitFile(file, attrs);
	 		    }
	 		};
	 		java.nio.file.Files.walkFileTree(filesPath, finder);
			Sheet sheet = wb.createSheet(path.getName());
			Row r = null;
	 		int rowNum = 0;
	 		for(File file: files){
	 			r = sheet.createRow(rowNum++);
				r.createCell(0).setCellValue(file.getName());
	 			System.out.println(file.getPath());
	 			
				long beginTime = System.currentTimeMillis();
				long endTime = System.currentTimeMillis();
				IFormula formula = solver.getFormulaFromCNFFile(file.getPath());
				List<IGroup> groups = null;
		 		groups = solver.getGroups(formula, RANDOM_COEF_INDEPENDENTSET);
		 		int iterations = MAX_ITERATIONS;
		 		List<ILiteral> bestSolution = new ArrayList<>();
				List<ILiteral> solution = new ArrayList<>();
				formula.minUnsatNum = formula.clauses.size();
				int repeated = 0;
				while(iterations-- != 0){
					solution.clear();
					solution = solver.solveFormulaBasedOnGroups(formula, groups, RANDOM_COEF_SOLUTION, RANDOM_COEF_NEXTGROUP);
					//增加未满足 clauses 的权重
					formula.increaseLitsWeightinUnsatClas();
					//找到更好的解，更新 bestSolution 
					if(formula.unsatClas.size() <formula.minUnsatNum){
						endTime = System.currentTimeMillis();
						formula.minUnsatNum = formula.unsatClas.size();
						bestSolution.clear();
						bestSolution.addAll(solution);
						System.out.println("o "+formula.minUnsatNum);
						System.out.println(bestSolution.toString());
						repeated = 0;
					}else{
						if(++repeated > 1000){
							formula.unVisitedVars.addAll(formula.visitedVars);
							formula.visitedVars.clear();
							groups = solver.getGroups(formula, RANDOM_COEF_INDEPENDENTSET);
							repeated = 0;
						}
					}
				}
		 		
				long time = endTime-beginTime;
				System.out.println(time);
				System.out.println(bestSolution.toString());
				r.createCell(1).setCellValue(formula.minUnsatNum);
				r.createCell(2).setCellValue((double)time/1000.0);
//				System.out.println(formula.minUnsatNum);
	 		}
			
		}
		
 		os = new FileOutputStream(outResultPath);
 		wb.write(os);
 		wb.close();
 		os.close();
// 		fw.close();

	}
}
