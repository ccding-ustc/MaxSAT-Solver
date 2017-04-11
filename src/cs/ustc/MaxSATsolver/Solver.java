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
import org.apache.commons.cli.ParseException;


public class Solver  {
	final static int DEFAULT_SEARCH_STEPS = 1000000;
	final static int NEW_LEAGUE_STEPS = 1000;
	final static double GREEDY_COEF_LEAGUE = 0.8;
	final static double GREEDY_COEF_STRATEGY = 0.7;
	final static String STRATEGY_NEXT_LEAGUE = "random";
	
	//变量名说明
	int  ss; //search steps 
	int nls; //construct new leagues steps
	double gcs; // greedy coefficients on choosing strategy
	double gcl; //greedy coefficients on constructing leagues
	String snl; //strategy on choosing next league, (include random, max, min)
	String path = null; //absolute path of cnf file
	List<ILiteral> optSolution; //optimal assignment to all variables
	int optUnsatNum; //optimal unsatisfied clauses number
	
	/**
	 * 将 formula 中每个 variable 视作一个 agent，将所有 agents 按照一定规则分成若干个不相交的联盟
	 * 
	 * @param f 存储 cnf 文件信息的 formula
	 * @param gcl 寻找独立集时，采取贪婪策略的随机性大小
	 * @return 不相交的各个分组（联盟）
	 */
	public List<ILeague> constructLeagues(IFormula f, double gcl) {
		List<ILeague> leagues = new LinkedList<>();
		while(true){
			List<IVariable> agents = f.getAgents(gcl);
			if(agents.isEmpty())
				break;
			ILeague league = new ILeague(agents);
			leagues.add(league);
			f.updateVisVars(league);
		}
		return leagues;
		
	}
	
	/**
	 * 
	 * 
	 * @param formula 存储 cnf 文件信息的 formula f
 	 * @param leagues  独立的各个分组
	 * @param gcs 每个分组寻找该组最优解时，采取贪婪策略的概率
	 * @param snl 组间赋值顺序策略
	 * @return
	 */
	public List<ILiteral> solveFormula(IFormula formula, List<ILeague> leagues, double gcs, String snl){
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
			flipLits = league.getSolution(gcs);
			
			if(!flipLits.isEmpty()){
				formula.announceSatLits(flipLits);
			}
			solution.addAll(league.solution);
			tmpLeagues.remove(league);
			
			if(tmpLeagues.isEmpty())
				break;
			if(!snl.equals("random")){
				//根据相关性寻找下一个组
				int tmpValue;
				if(snl.equals("max")){
					tmpValue = Integer.MIN_VALUE;
				}else{
					tmpValue = Integer.MAX_VALUE;
				}
				Iterator<Entry<ILeague, Integer>> it = league.neighbors.entrySet().iterator();
				Map.Entry<ILeague, Integer> entry = null;
				league = null;
				while(it.hasNext()){
					entry = it.next();
					if(snl.equals("max")){
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
	public IFormula initFormulaOfCNFFile(String cnfFile) throws ParseFormatException, IOException{
		IFormula f = new IFormula();
		CNFFileReader cnfFileReader = new CNFFileReader();
		cnfFileReader.parseInstance(cnfFile, f);
		f.setVariables();
		f.setVarsNeighbors();
		return f;
	}
	
	public long solve(File file) throws ParseFormatException, IOException{
		long beginTime = System.currentTimeMillis();
		long endTime = 0;
		IFormula formula = this.initFormulaOfCNFFile(file.getPath());
		List<ILeague> leagues = this.constructLeagues(formula, this.gcl);
 		int iterations = this.ss;//search steps
 		this.optSolution = new ArrayList<>();
		List<ILiteral> solution = new ArrayList<>();
		formula.minUnsatNum = formula.clauses.size();
		int repeated = 0;
		while(iterations-- != 0){
			solution.clear();
			solution = this.solveFormula(formula, leagues, this.gcs, this.snl);
			//增加未满足 clauses 的权重
			formula.increaseLitsWeightinUnsatClas();
			//找到更好的解，更新 bestSolution 
			
			if(formula.unsatClas.size() <formula.minUnsatNum){
				endTime = System.currentTimeMillis();
				formula.minUnsatNum = formula.unsatClas.size();
				this.optUnsatNum = formula.minUnsatNum;
				optSolution.clear();
				optSolution.addAll(solution);
				System.out.println("o "+formula.minUnsatNum);
				repeated = 0;
			}else{
				if(++repeated > this.nls){
					formula.unVisVars.addAll(formula.visVars);
					formula.visVars.clear();
					leagues = this.constructLeagues(formula, this.gcl);
					repeated = 0;
				}
			}
		}
		return endTime-beginTime;
	}
	
	/**
	 * 打印参数信息
	 */
	public void printConfigurations(){
		System.out.println("Max-SAT solver based on Boolean Game.");
		System.out.println("Cnf files absolute path: "+path);
		System.out.println("Maximum search steps: "+ss);
		System.out.println("Maximum new leagues steps: "+nls);
		System.out.println("Greedy coefficient of league strategy: "+gcl);
		System.out.println("Greedy coefficient of league construction: "+gcl);
		System.out.println("Strategy of next league to set strategy: "+snl);
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
		Solver solver = new Solver();
		ExtenOptions.setParameters(solver, args); //根据命令行输入，设置相关参数
		//格式化时间日期
	    Date dt = new Date();  
	    SimpleDateFormat sdf = new SimpleDateFormat("MMdd_HHmm");  
	    String dataStr = sdf.format(dt);
	    //建立 excel 文件，存入结果
	    String outFile = solver.path+"results_ss"+solver.ss+"_nls"+solver.nls+"_snl"+solver.snl+
	    		"_gcl"+solver.gcl+"_gcs"+solver.gcs+"_date"+dataStr+".xls";
 		Workbook wb = new HSSFWorkbook();
		OutputStream os = null;
		//列出目录下所有子目录
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
					System.out.println(solver.optSolution.toString());
					r.createCell(runs).setCellValue(solver.optUnsatNum);
					r2.createCell(runs).setCellValue((double)time/1000.0);
		 		}
	 		}	
		}
		
 		os = new FileOutputStream(outFile);
 		wb.write(os);
 		wb.close();
 		os.close();

	}
	
	
}
