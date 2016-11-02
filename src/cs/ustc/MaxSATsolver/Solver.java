package cs.ustc.MaxSATsolver;
/**
 * MaxSAT 求解器
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
import java.util.*;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook; 
import org.apache.poi.ss.usermodel.Row; 
import org.apache.poi.ss.usermodel.Sheet; 
import org.apache.poi.ss.usermodel.Workbook;


public class Solver  {
	static final int  MAX_ITERATIONS1 = 50;
	static final int  MAX_ITERATIONS2 = 100;
	static final double RANDOM_COEF_SOLUTION = 0;
	static final double RANDOM_COEF_INDEPENDENTSET = 0;
	static final double RANDOM_COEF_NEXTGROUP = 0;
	static final long TIME_LIMIT = 3*60*1000;
	public Solver(){
		
	}
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
	 * 迭代寻找各个分组
	 * 
	 * @param f 存储 cnf 文件信息的formula f
	 * @param maxIterations 最大迭代次数
	 * @param randomCoefIndependentset 参见getGroups()函数
	 * @param randomCoefSolution 参见solveFormulaBasedOnGroups() 函数
	 * @param randomCoefNextGroup 参见solveFormulaBasedOnGroups() 函数
	 * @return 初始解最好的分组
	 */
	public List<IGroup> iteratedGetGroups(
			IFormula f, 
			int maxIterations, 
			double randomCoefIndependentset, 
			double randomCoefSolution, 
			double randomCoefNextGroup )
	{
		List<IGroup> groups = new ArrayList<>();
		List<IGroup> bestGroups = new ArrayList<>();
		List<ILiteral> solution = new ArrayList<>();
		List<ILiteral> bestSolution = new ArrayList<>();
		int miniUnsatNum = f.clauses.size();
		while(maxIterations-- != 0){
			groups = this.getGroups(f, randomCoefIndependentset);
			solution = this.solveFormulaBasedOnGroups(f, groups, randomCoefSolution, randomCoefNextGroup);
			//找到更好的分组，更新 bestSolution 和 bestGroups 并将 repeated 置为 0
			if(f.unsatClas.size() < miniUnsatNum){
				bestSolution.clear();
				bestSolution.addAll(solution);
				bestGroups.clear();
				bestGroups.addAll(groups);
				miniUnsatNum = f.unsatClas.size();
				f.minUnsatNum = miniUnsatNum;
			}
			f.resetFormula();
		}
		for(IGroup g: bestGroups){
			g.neighbors.clear();
		}
		return bestGroups;
	}
	
	
	
	/**
	 * 根据权重累加的策略， 迭代求解 formula 直到找到解或者达到预设的迭代次数
	 * 
	 * @param formula  存储 cnf 文件信息的 formula 
	 * @param groups 独立的各个分组
	 * @param iterations 预设的迭代次数
	 * @param randomCoefSolution 参见solveFormulaBasedOnGroups() 函数
	 * @param randomCoefNextGroup 参见solveFormulaBasedOnGroups() 函数
	 * @return
	 */
	public List<ILiteral> iteratedSolveFormulaBasedOnGroups(
			IFormula formula,
			List<IGroup> groups,
			int iterations,
			double randomCoefSolution,
			double randomCoefNextGroup)
	{
		List<ILiteral> bestSolution = new ArrayList<>();
		List<ILiteral> solution = new ArrayList<>();
		formula.minUnsatNum = formula.clauses.size();
		int repeated = 0;
		while(iterations-- != 0){
			solution.clear();
			solution = this.solveFormulaBasedOnGroups(formula, groups, randomCoefSolution, randomCoefNextGroup);
			//增加未满足 clauses 的权重
			formula.increaseLitsWeightinUnsatClas();
			//找到更好的解，更新 bestSolution 并将 repeated 置为 0
			if(formula.unsatClas.size() <formula.minUnsatNum){
				formula.minUnsatNum = formula.unsatClas.size();
				bestSolution.clear();
				bestSolution.addAll(solution);
				repeated = 0;
			}
			//所有 clauses 都满足， 或者迭代过程中连续超过30次找不到更好的解，就跳出循环
			if(formula.minUnsatNum == 0 || repeated++ > 30)
				break;
		}
		return bestSolution;


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
		
		while(true){
			
			//若存在与所有组都不相关的组，则需要循环遍历
			if(group == null){
				group = groups.get((int)(Math.random()*tmpGroups.size()));
			}
			
			group.getSolution(randomCoefSolution);
			formula.announceSatLits(group.solution);
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
		return f;
	}
	/**
	 * 
	 * @param args args[0] cnf file path, args[1] text file path that store results
	 * @throws IOException
	 * @throws ParseFormatException
	 */
	public static void main(String[] args) throws IOException, ParseFormatException{
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		/*
		Solver stmp = new Solver();
		IFormula ftmp = stmp.getFormulaFromCNFFile("D:\\GitHub\\Max-SAT-Solver\\Max-SAT2016 benchmarks\\ms_crafted\\maxcut\\dimacs-mod\\hamming6-4.clq.cnf");
		int[] s = {-47 , -14 , -12 , -16 , -44 , -46 , -9 , -42 , 49 , 33 , 51 , 52 , 39 , 54 , 36 , 55 , 24 , 20 , -31 , -60 , -26 , -27 , -32 , 19 , -28 , -57 , -13 , 53 , 17 , -29 , 37 , -41 , 1 , 3 , 2 , 7 , 40 , 4 , 5 , 8 , 35 , -10 , 38 , -58 , 34 , 50 , -62 , -59 , -48 , -15 , -45 , -43 , -63 , -11 , 6 , 56 , 18 , 23 , 21 , 22 , -30 , -64 , -61 , -25 };
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
		FileWriter fw = new FileWriter(new File("D:\\result.txt"));
		
 		Workbook wb = new HSSFWorkbook();
		OutputStream os = null;
		Solver solver = new Solver();
		File rootPath = new File(args[0]);
		File[] paths = rootPath.listFiles();
		for(File path: paths){
			if(path.getName().equals("ms_industrial"))
				continue;
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
				long begin = System.currentTimeMillis();
				IFormula formula = solver.getFormulaFromCNFFile(file.getPath());
				List<IGroup> groups = null;
				List<ILiteral> solution = null;
				int runs = 1000;
				while(runs-- != 0){
		 			groups = solver.getGroups(formula, RANDOM_COEF_INDEPENDENTSET);
		 			solution = solver.iteratedSolveFormulaBasedOnGroups(formula, groups, MAX_ITERATIONS2, RANDOM_COEF_SOLUTION, RANDOM_COEF_NEXTGROUP);
		 			formula.resetFormula();
				}
				long time = System.currentTimeMillis()-begin;
				System.out.println(time);
				r.createCell(1).setCellValue(formula.minUnsatNum);
				r.createCell(2).setCellValue(time);
				fw.write(file.getName()+"\r\n");
				fw.write(solution.toString()+"\r\n");
				System.out.println(formula.minUnsatNum);
	 		}
			
		}
		
 		os = new FileOutputStream(args[1]);
 		wb.write(os);
 		wb.close();
 		os.close();
 		fw.close();

	}
}
