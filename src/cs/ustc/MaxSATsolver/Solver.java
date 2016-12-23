package cs.ustc.MaxSATsolver;
/**
 * incomplete MaxSAT solver
 * @author ccding  2016年3月7日 上午8:39:11
 */


import java.io.File;
import java.io.FileOutputStream;
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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class Solver  {
	static final String PATH_SHORT_OPTION = "p";
	static final String PATH_LONG_OPTION = "path";
	static final String PATH_DESCRIPTION = "cnf files absolute path";
	static final String SEARCH_STEPS_SHORT_OPTION = "ss";
	static final String SEARCH_STEPS_LONG_OPTION = "search_steps";
	static final String SEARCH_STEPS_DESCRIPTION = "maximum search steps";
	static final String NEWLEAGUES_STEPS_SHORT_OPTION = "nls";
	static final String NEWLEAGUES_STEPS_LONG_OPTION = "new_leagues_steps";
	static final String NEWLEAGUES_STEPS_DESCRIPTION = "maximum new leagues steps";
	static final String GREEDY_COEF_STRATEGY_SHORT_OPTION = "gcs";
	static final String GREEDY_COEF_STRATEGY_LONG_OPTION = "greedy_coef_strategy";
	static final String GREEDY_COEF_STRATEGY_DESCRIPTION = "greedy coefficient of league strategy";
	static final String GREEDY_COEF_LEAGUE_SHORT_OPTION = "gcl";
	static final String GREEDY_COEF_LEAGUE_LONG_OPTION = "greedy_coef_league";
	static final String GREEDY_COEF_LEAGUE_DESCRIPTION = "greedy coefficient of league construction";
	static final String STRATEGY_NEXT_LEAGUE_SHORT_OPTION = "snl";
	static final String STRATEGY_NEXT_LEAGUE_LONG_OPTION = "strategy_next_league";
	static final String STRATEGY_NEXT_LEAGUE_DESCRIPTION = "strategy of next league to set strategy";
	
	
	
	int  searchSteps;
	int newLeaguesSteps;
	double greedyCoefStrategy;
	double greedyCoefLeague;
	String strategyNextLeague;
	String path = null;
	List<ILiteral> bestSolution;
	int miniUnsatNum;
	
	/**
	 * 将 formula 中每个 literal 视作一个 agent，将所有 agents 按照一定规则分成若干个不相交的联盟
	 * 
	 * @param f 存储 cnf 文件信息的 formula
	 * @param randomCoefIndependentSet 寻找独立集时，采取贪婪策略的随机性大小
	 * @return 不相交的各个分组（联盟）
	 */
	public List<ILeague> getLeagues(IFormula f, double randomCoefIndependentSet) {
		List<ILeague> leagues = new ArrayList<>();
		while(true){
			ILeague league = new ILeague(f.getIndependentLeague(randomCoefIndependentSet));
			//jump out while loop
			if (league.agents.isEmpty()){
				break;
			}
			leagues.add(league);
			f.removeLeagueFromFormula(league.agents);
		}
		return leagues;
		
	}
	
	/**
	 * 
	 * 
	 * @param formula 存储 cnf 文件信息的 formula f
 	 * @param leagues  独立的各个分组
	 * @param randomCoefSolution 每个分组寻找该组最优解时，采取贪婪策略的概率
	 * @param randomCoefNextLeague 组间赋值顺序随机性
	 * @return
	 */
	public List<ILiteral> solveFormulaBasedOnLeagues(
			IFormula formula, 
			List<ILeague> leagues, 
			double randomCoefSolution, 
			String strategyNextLeague)
	{
		ILeague.initLeagueNeighbors(leagues);
		List<ILeague> tmpLeagues = new ArrayList<>(leagues);
		List<ILiteral> solution = new ArrayList<>();
		
		//初始组
		ILeague league = tmpLeagues.get((int)(Math.random()*tmpLeagues.size()));
		List<ILiteral> flipLits;
		while(true){
			//若存在与所有组都不相关的组，则需要随机从剩下的组中选择一个组
			if(league == null){
				league = leagues.get((int)(Math.random()*tmpLeagues.size()));
			}
			flipLits = league.getSolution(randomCoefSolution);
			
			if(!flipLits.isEmpty()){
				formula.announceSatLits(flipLits);
			}
			solution.addAll(league.solution);
			tmpLeagues.remove(league);
			
			if(tmpLeagues.isEmpty())
				break;
			if(!strategyNextLeague.equals("random")){
				//根据相关性寻找下一个组
				int tmpValue;
				if(strategyNextLeague.equals("max")){
					tmpValue = Integer.MIN_VALUE;
				}else{
					tmpValue = Integer.MAX_VALUE;
				}
				Iterator<Entry<ILeague, Integer>> it = league.neighbors.entrySet().iterator();
				Map.Entry<ILeague, Integer> entry = null;
				league = null;
				while(it.hasNext()){
					entry = it.next();
					if(strategyNextLeague.equals("max")){
						if(tmpLeagues.contains(entry.getKey()) && entry.getValue() > tmpValue){
							league = entry.getKey();
							tmpValue = entry.getValue();
						}
					}else{
						if(tmpLeagues.contains(entry.getKey()) && entry.getValue() < tmpValue){
							league = entry.getKey();
							tmpValue = entry.getValue();
						}
					}
					
				}
			}else{
				league = tmpLeagues.get((int)(Math.random()*tmpLeagues.size()));
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
	
	public long solve(File file) throws ParseFormatException, IOException{
		long beginTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		IFormula formula = this.getFormulaFromCNFFile(file.getPath());
		List<ILeague> leagues = null;
 		leagues = this.getLeagues(formula, this.greedyCoefLeague);
 		int iterations = this.searchSteps;
 		this.bestSolution = new ArrayList<>();
		List<ILiteral> solution = new ArrayList<>();
		formula.minUnsatNum = formula.clauses.size();
		int repeated = 0;
		while(iterations-- != 0){
			solution.clear();
			solution = this.solveFormulaBasedOnLeagues(formula, leagues, this.greedyCoefStrategy, this.strategyNextLeague);
			//增加未满足 clauses 的权重
			formula.increaseLitsWeightinUnsatClas();
			//找到更好的解，更新 bestSolution 
			if(formula.unsatClas.size() <formula.minUnsatNum){
				endTime = System.currentTimeMillis();
				formula.minUnsatNum = formula.unsatClas.size();
				this.miniUnsatNum = formula.minUnsatNum;
				bestSolution.clear();
				bestSolution.addAll(solution);
				System.out.println("o "+formula.minUnsatNum);
				repeated = 0;
			}else{
				if(++repeated > this.newLeaguesSteps){
					formula.unVisitedVars.addAll(formula.visitedVars);
					formula.visitedVars.clear();
					leagues = this.getLeagues(formula, this.greedyCoefLeague);
					repeated = 0;
				}
			}
		}
		return endTime-beginTime;
	}
	public void printConfigurations(){
		System.out.println("Max-SAT solver based on boolean game.");
		System.out.println("Cnf files absolute path: "+path);
		System.out.println("Maximum search steps: "+searchSteps);
		System.out.println("Maximum new leagues steps: "+newLeaguesSteps);
		System.out.println("Greedy coefficient of league strategy: "+greedyCoefLeague);
		System.out.println("Greedy coefficient of league construction: "+greedyCoefLeague);
		System.out.println("Strategy of next league to set strategy: "+strategyNextLeague);
	}
	
	/**
	 * 程序入口
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ParseFormatException
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, ParseFormatException, ParseException{
		Options opts = new Options();
		opts.addOption(PATH_SHORT_OPTION, PATH_LONG_OPTION, true, PATH_DESCRIPTION);
		opts.addOption(SEARCH_STEPS_SHORT_OPTION, SEARCH_STEPS_LONG_OPTION, true, SEARCH_STEPS_DESCRIPTION);
		opts.addOption(NEWLEAGUES_STEPS_SHORT_OPTION, NEWLEAGUES_STEPS_LONG_OPTION, true, NEWLEAGUES_STEPS_DESCRIPTION);
		opts.addOption(GREEDY_COEF_LEAGUE_SHORT_OPTION, GREEDY_COEF_LEAGUE_LONG_OPTION, true, GREEDY_COEF_LEAGUE_DESCRIPTION);
		opts.addOption(STRATEGY_NEXT_LEAGUE_SHORT_OPTION, STRATEGY_NEXT_LEAGUE_LONG_OPTION, true, STRATEGY_NEXT_LEAGUE_DESCRIPTION);
		opts.addOption(GREEDY_COEF_STRATEGY_SHORT_OPTION, GREEDY_COEF_STRATEGY_LONG_OPTION, true, GREEDY_COEF_STRATEGY_DESCRIPTION);
		DefaultParser defaultParser = new DefaultParser();
		CommandLine cmd = defaultParser.parse(opts, args);
		Solver solver = new Solver();
		
		if(cmd.hasOption(PATH_SHORT_OPTION)){
			solver.path = cmd.getOptionValue(PATH_SHORT_OPTION);
		}else {
			System.err.println("cnf files path not found!");
			System.exit(0);
		}
		
		if(cmd.hasOption(SEARCH_STEPS_SHORT_OPTION)){
			solver.searchSteps = Integer.parseInt(cmd.getOptionValue(SEARCH_STEPS_SHORT_OPTION));
		}else{
			solver.searchSteps = 1000000; //default search steps
		}
		
		if(cmd.hasOption(NEWLEAGUES_STEPS_SHORT_OPTION)){
			solver.newLeaguesSteps = Integer.parseInt(cmd.getOptionValue(NEWLEAGUES_STEPS_SHORT_OPTION));
		}else{
			solver.newLeaguesSteps = 1000; //default new leagues steps
		}
		
		if(cmd.hasOption(GREEDY_COEF_LEAGUE_SHORT_OPTION)){
			solver.greedyCoefLeague = Double.parseDouble(cmd.getOptionValue(GREEDY_COEF_LEAGUE_SHORT_OPTION));
		}else{
			solver.greedyCoefLeague = 0.8;
		}
		if(cmd.hasOption(STRATEGY_NEXT_LEAGUE_SHORT_OPTION)){
			solver.strategyNextLeague = cmd.getOptionValue(STRATEGY_NEXT_LEAGUE_SHORT_OPTION);
		}else{
			solver.strategyNextLeague = "random";
		}
		
		if(cmd.hasOption(GREEDY_COEF_STRATEGY_SHORT_OPTION)){
			solver.greedyCoefStrategy = Double.parseDouble(cmd.getOptionValue(GREEDY_COEF_STRATEGY_SHORT_OPTION));
		}else{
			solver.greedyCoefStrategy = 0.7;
		}
		
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");  
	    Date dt = new Date();  
	    SimpleDateFormat sdf = new SimpleDateFormat("MMdd_HHmm");  
	    String dataStr = sdf.format(dt);
	    String outResultFile = solver.path+"results_"+solver.searchSteps+"_"+solver.newLeaguesSteps+"_"+solver.strategyNextLeague+
	    		"_"+solver.greedyCoefLeague+"_"+solver.greedyCoefStrategy+"_"+dataStr+".xls";
	    
 		Workbook wb = new HSSFWorkbook();
		OutputStream os = null;
		
	
		File rootPath = new File(solver.path);
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
			Row r = null, r2 = null;;
	 		int rowNum = 0;
	 		for(File file: files){
	 			r = sheet.createRow(rowNum++);
	 			r2 = sheet.createRow(rowNum++);
				r.createCell(0).setCellValue(file.getName());
	 			System.out.println(file.getPath());
	 			solver.printConfigurations();
				int runs = 0;
				
	 			while(runs++ < 50){
					long time = solver.solve(file);
					System.out.println(time);
					System.out.println(solver.bestSolution.toString());
					r.createCell(runs).setCellValue(solver.miniUnsatNum);
					r2.createCell(runs).setCellValue((double)time/1000.0);
		 		}
	 		}	
		}
		
 		os = new FileOutputStream(outResultFile);
 		wb.write(os);
 		wb.close();
 		os.close();

	}
	
	
}
