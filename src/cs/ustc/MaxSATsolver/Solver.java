package cs.ustc.MaxSATsolver;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import tools.FilesReader;

import org.apache.commons.cli.ParseException;

/**
 * incomplete MaxSAT solver
 * @author ccding  2016年3月7日 上午8:39:11
 */
public class Solver  {
	final static String LEAGUE_FORMATION_STRATEGY = "MIS";
	final static String OUT_LOCAL_OPT = "shuff";
	final static String NEXT_LEAGUE_STRATEGY = "random";
	
	String lfs; // league formation methods(include MC, MIS)
	String nls; //strategy on choosing next league, (include RANDOM, MAX, MIN)
	String path = null; //absolute path of cnf file
	String olo; //strategy of jumping out of local optimization
	List<ILiteral> optSolution; //optimal assignment to all variables
	int optUnsatNum; //optimal unsatisfied clauses number
	
	
	/**
	 * 
	 * 
	 * @param formula 存储 cnf 文件信息的 formula f
 	 * @param leagues  独立的各个分组
	 * @param gcs 每个分组寻找该组最优解时，采取贪婪策略的概率
	 * @param snl 组间赋值顺序策略
	 * @return
	 */
	public List<ILiteral> solveFormula(IFormula formula, List<ILeague> leagues){
		List<ILiteral> solution = new ArrayList<>();
		List<ILiteral> flipLits;
		
		ILeague league = leagues.get((int)(Math.random()*leagues.size()));
		while(league != null) {
			league.vis = true;
			flipLits = league.getSolutionMIS();
			if(!flipLits.isEmpty()){
				formula.announceSatLits(flipLits);
			}
			solution.addAll(Arrays.asList(league.solution));
			switch (this.nls) {
				case "random":
					league = getRandom(leagues);
					break;
				case "max":
					league = league.getMax();
					break;
				case "min":
					league = league.getMin();
					break;
				case "max_size":
					league = getMaxSize(leagues);
					break;
				case "min_size":
					league = getMinSize(leagues);
					break;
				default:
					league = null;
					break;
			}
		}
		return solution;
	}
	
	public ILeague getRandom(List<ILeague> leagues) {
		for(ILeague l: leagues)
			if(!l.vis)
				return l;
		return null;
	}
	
	public ILeague getMaxSize(List<ILeague> leagues) {
		int max_size = -1;
		ILeague l = null;
		for(ILeague tmp: leagues)
			if(tmp.agents.size()>max_size && !tmp.vis) {
				l = tmp;
				max_size = tmp.agents.size();
			}
		return l;
	}
	
	public ILeague getMinSize(List<ILeague> leagues) {
		int min_size = Integer.MAX_VALUE;
		ILeague l = null;
		for(ILeague tmp: leagues)
			if(tmp.agents.size()<min_size && !tmp.vis) {
				l = tmp;
				min_size = tmp.agents.size();
			}
		return l;
	}
	
	public long solve(File file) throws IOException{
		long endTime = 0;
		//读取cnf文件，将信息存入formula中
		IFormula formula = new CNFFileReader().initFormulaOfCNFFile(file.getPath());
		//根据图中顶点（变量）之间是否存在边，构建联盟（通过寻找独立集的方法）
		List<ILeague> leagues = formula.constructLeagues();
//		formula.setNeighborsOfLeagues(leagues);
		System.out.println("leagues size: " + leagues.size());
 		this.optSolution = new ArrayList<>();
		formula.minUnsatNum = formula.clauses.size();
		int repeated = 0;
		long beginTime = System.currentTimeMillis();
		long limitTime = 5*60*1000;
		while(System.currentTimeMillis()-beginTime < limitTime){
			if(leagues.isEmpty()) {
				System.out.println("empty");
			}
			List<ILiteral> solution = this.solveFormula(formula, leagues);
			//增加未满足 clauses 的权重
			formula.plusWeight();
			for(ILeague l: leagues)
				l.vis = false;
			//找到更好的解，更新 bestSolution 
			if(formula.unsatClas.size() < formula.minUnsatNum){
				endTime = System.currentTimeMillis();
				formula.minUnsatNum = formula.unsatClas.size();
				
				this.optUnsatNum = formula.minUnsatNum;
				optSolution.clear();
				optSolution.addAll(solution);
				System.out.println("o "+formula.minUnsatNum);
				repeated = 0;
				if(formula.minUnsatNum==0)
					break;
				
			}else{
				if(++repeated > 1000){
					if(olo.equals("SHUFF")){
						Collections.shuffle(leagues);
					}else{
						formula.unVisVars.addAll(Arrays.asList(formula.vars));
						leagues = formula.constructLeagues();
//						formula.setNeighborsOfLeagues(leagues);
					}
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
		System.out.println("League formation strategy: "+lfs);
		System.out.println("Out of local optimization strategy: "+olo);
		System.out.println("Strategy of next league to set strategy: "+nls);
	}
	
	/**
	 * 程序入口
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ParseFormatException
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, ParseException{
		Solver solver = new Solver();
		ExtenOptions.setParameters(solver, args); //根据命令行输入，设置相关参数
		//格式化时间日期
	    Date dt = new Date();  
	    SimpleDateFormat sdf = new SimpleDateFormat("MMdd_HHmm");  
	    String dateStr = sdf.format(dt);

 		Workbook wb = new HSSFWorkbook();
		OutputStream os = null;
		//列出目录下所有子目录
		File rootPath = new File(solver.path);
		File[] paths = rootPath.listFiles();
	    
		for(File path: paths){
			List<File> files = FilesReader.getCNFFiles(path);
			Sheet sheet = wb.createSheet(path.getName());
			Row r1 = null;
	 		int rowNum = 0;
	 		for(File file: files){
	 			r1 = sheet.createRow(rowNum++);
				r1.createCell(0).setCellValue(file.getName());
	 			System.out.println(file.getPath());
	 			solver.printConfigurations();
	 			long time = solver.solve(file);
	 			System.out.println("optTime:"+(double)time/1000.0);
	 			System.out.println(time);
	 			Collections.sort(solver.optSolution);
				r1.createCell(1).setCellValue(solver.optUnsatNum);
				r1.createCell(2).setCellValue((double)time/1000.0);
	 		}	
		}
	    //建立 excel 文件，存入结果
		String outFile = solver.path+"_lfs"+solver.lfs+"_olo"+solver.olo+"_nls"+solver.nls+"_date"+dateStr+".xls";
 		os = new FileOutputStream(outFile);
 		wb.write(os);
 		wb.close();
 		os.close();

	}
	
	
}
