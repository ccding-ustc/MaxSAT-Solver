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
	static final int  MAX_ITERATIONS = 500000;
	static final double RANDOM_COEF_SOLUTION = 0.7;
	static final double RANDOM_COEF_INDEPENDENTSET = 0.8;
	static final double RANDOM_COEF_NEXTGROUP = 0.8;
	
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
		/*
		Solver stmp = new Solver();
		IFormula ftmp = stmp.getFormulaFromCNFFile("D:\\Max-SAT2016 benchmarks\\all_instances\\ms_crafted\\maxcut\\abrame-habet\\v220\\s2v220c2500-2.cnf");
		int[] s = {-169 , 186 , -56 , -167 , -13 , 62 , 145 , -212 , -14 , 101 , -124 , -40 , -29 , 184 , 132 , -204 , -51 , 39 , 7 , 2 , 92 , 151 , 106 , -200 , 77 , -121 , 129 , -66 , -110 , -19 , 199 , 12 , 24 , -104 , 42 , -176 , 179 , -135 , 163 , -55 , -195 , -74 , 189 , -34 , -86 , -207 , -82 , 65 , 79 , -68 , -160 , -60 , 220 , -30 , -183 , -131 , 112 , -154 , 178 , -54 , 164 , 133 , 93 , -127 , -38 , -119 , -44 , -162 , -152 , 159 , 61 , -15 , -206 , -48 , 33 , -211 , 202 , 210 , 137 , 156 , 146 , 88 , 217 , -102 , 117 , 168 , 185 , 123 , 59 , -16 , -190 , 8 , -149 , 193 , 70 , 173 , 181 , -17 , 26 , -85 , 161 , 205 , 1 , -41 , 3 , -87 , 140 , 25 , 49 , 219 , 97 , 31 , -141 , -177 , 187 , -158 , -209 , -37 , 122 , -201 , -192 , 46 , -194 , -142 , -113 , 116 , -11 , 35 , 114 , 18 , -32 , 95 , -144 , -182 , 75 , -197 , -28 , 105 , -96 , 10 , 57 , 134 , -147 , 22 , -196 , 108 , 143 , 175 , -188 , -71 , 43 , -69 , -23 , -78 , -76 , 198 , 216 , -215 , 47 , -98 , 136 , -150 , 72 , -213 , -5 , -166 , -63 , -90 , 99 , -171 , 172 , 130 , -180 , -118 , -111 , -191 , -165 , 125 , 58 , 4 , 20 , 109 , -155 , 50 , -214 , 53 , 9 , 203 , -170 , -73 , -84 , -115 , -64 , 67 , -100 , 138 , -120 , 208 , 81 , -80 , 36 , 27 , -126 , 89 , -148 , 21 , 103 , -6 , -218 , 83 , -107 , -139 , 128 , -45 , -157 , 52 , 153 , 91 , 94 , -174};
		Set<IClause> satClas = new HashSet<>(ftmp.clauses.size());
		for(int ss: s){
			IVariable v = ftmp.variables.get(Math.abs(ss)-1);
			if(ss > 0){
				satClas.addAll(v.lit.getClas());
			}else{
				satClas.addAll(v.oppositeLit.getClas());
			}
		}
		System.out.println(satClas.size());
		System.out.println(ftmp.clauses.size() - satClas.size());
		*/
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
						repeated = 0;
					}else{
						if(++repeated > 3000){
							formula.unVisitedVars.addAll(formula.visitedVars);
							formula.visitedVars.clear();
							groups = solver.getGroups(formula, RANDOM_COEF_INDEPENDENTSET);
							repeated = 0;
						}
					}
				}
		 		
				long time = endTime-beginTime;
				System.out.println(time);
				System.out.println(solution.toString());
				r.createCell(1).setCellValue(formula.minUnsatNum);
				r.createCell(2).setCellValue(time/1000);
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
