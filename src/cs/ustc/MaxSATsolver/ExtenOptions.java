package cs.ustc.MaxSATsolver;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * 通过 options 类提供的方法对命令行输入的参数进行处理
 * @author ccding
 *
 */
public class ExtenOptions extends Options {
	private static final long serialVersionUID = 1L;
	static final String PATH_SHORT_OPTION = "p";
	static final String PATH_LONG_OPTION = "path";
	static final String PATH_DESCRIPTION = "cnf files absolute path";
	static final String NEXT_LEAGUE_STRATEGY_SHORT_OPTION = "nls";
	static final String NEXT_LEAGUE_STRATEGY_LONG_OPTION = "next_league_strategy";
	static final String NEXT_LEAGUE_STRATEGY_DESCRIPTION = "next league strategy";
	static final String LEAGUE_FORMATION_STRATEGY_SHORT_OPTION = "lfs";
	static final String LEAGUE_FORMATION_STRATEGY_LONG_OPTION = "league_formation_startegy";
	static final String LEAGUE_FORMATION_STRATEGY_DESCRIPTION = "league formation strategy";
	static final String OUT_LOCAL_OPT_SHORT_OPTION = "olo";
	static final String OUT_LOCAL_OPT_LONG_OPTION = "jump_out_local_optimization";
	static final String OUT_LOCAL_OPT_DESCRIPTION = "jump out local optimization";
	
	public ExtenOptions(){
		this.addOption(PATH_SHORT_OPTION, PATH_LONG_OPTION, true, PATH_DESCRIPTION);
		this.addOption(NEXT_LEAGUE_STRATEGY_SHORT_OPTION, NEXT_LEAGUE_STRATEGY_LONG_OPTION, true, NEXT_LEAGUE_STRATEGY_DESCRIPTION);
		this.addOption(LEAGUE_FORMATION_STRATEGY_SHORT_OPTION, LEAGUE_FORMATION_STRATEGY_LONG_OPTION, true, LEAGUE_FORMATION_STRATEGY_DESCRIPTION);
		this.addOption(OUT_LOCAL_OPT_SHORT_OPTION, OUT_LOCAL_OPT_LONG_OPTION, true, OUT_LOCAL_OPT_DESCRIPTION);
	}
	
	public static void setParameters(Solver solver, String[] args) throws ParseException{
		Options opts = new ExtenOptions();
		DefaultParser defaultParser = new DefaultParser();
		CommandLine cmd = defaultParser.parse(opts, args);
		
		if(cmd.hasOption(ExtenOptions.PATH_SHORT_OPTION)){
			solver.path = cmd.getOptionValue(ExtenOptions.PATH_SHORT_OPTION);
		}else {
			System.err.println("input cnf file path!");
			System.exit(0);
		}
		
		if(cmd.hasOption(LEAGUE_FORMATION_STRATEGY_SHORT_OPTION)){
			solver.lfs = cmd.getOptionValue(LEAGUE_FORMATION_STRATEGY_SHORT_OPTION);
		}else{
			solver.lfs = Solver.LEAGUE_FORMATION_STRATEGY;
		}
		
		if(cmd.hasOption(NEXT_LEAGUE_STRATEGY_SHORT_OPTION)){
			solver.nls = cmd.getOptionValue(NEXT_LEAGUE_STRATEGY_SHORT_OPTION);
		}else{
			solver.nls = Solver.NEXT_LEAGUE_STRATEGY;
		}
		
		if(cmd.hasOption(OUT_LOCAL_OPT_SHORT_OPTION)){
			solver.olo = cmd.getOptionValue(OUT_LOCAL_OPT_SHORT_OPTION);
		}else{
			solver.olo = Solver.OUT_LOCAL_OPT;
		}
	}
}
