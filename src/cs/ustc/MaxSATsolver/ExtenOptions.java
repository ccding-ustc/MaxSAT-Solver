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
	
	public ExtenOptions(){
		this.addOption(PATH_SHORT_OPTION, PATH_LONG_OPTION, true, PATH_DESCRIPTION);
		this.addOption(SEARCH_STEPS_SHORT_OPTION, SEARCH_STEPS_LONG_OPTION, true, SEARCH_STEPS_DESCRIPTION);
		this.addOption(NEWLEAGUES_STEPS_SHORT_OPTION, NEWLEAGUES_STEPS_LONG_OPTION, true, NEWLEAGUES_STEPS_DESCRIPTION);
		this.addOption(GREEDY_COEF_LEAGUE_SHORT_OPTION, GREEDY_COEF_LEAGUE_LONG_OPTION, true, GREEDY_COEF_LEAGUE_DESCRIPTION);
		this.addOption(STRATEGY_NEXT_LEAGUE_SHORT_OPTION, STRATEGY_NEXT_LEAGUE_LONG_OPTION, true, STRATEGY_NEXT_LEAGUE_DESCRIPTION);
		this.addOption(GREEDY_COEF_STRATEGY_SHORT_OPTION, GREEDY_COEF_STRATEGY_LONG_OPTION, true, GREEDY_COEF_STRATEGY_DESCRIPTION);
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
		
		if(cmd.hasOption(SEARCH_STEPS_SHORT_OPTION)){
			solver.ss = Integer.parseInt(cmd.getOptionValue(SEARCH_STEPS_SHORT_OPTION));
		}else{
			solver.ss = Solver.DEFAULT_SEARCH_STEPS; //default search steps
		}
		
		if(cmd.hasOption(NEWLEAGUES_STEPS_SHORT_OPTION)){
			solver.nls = Integer.parseInt(cmd.getOptionValue(NEWLEAGUES_STEPS_SHORT_OPTION));
		}else{
			solver.nls = Solver.NEW_LEAGUE_STEPS; //default new leagues steps
		}
		
		if(cmd.hasOption(GREEDY_COEF_LEAGUE_SHORT_OPTION)){
			solver.gcl = Double.parseDouble(cmd.getOptionValue(GREEDY_COEF_LEAGUE_SHORT_OPTION));
		}else{
			solver.gcl = Solver.GREEDY_COEF_LEAGUE;
		}
		if(cmd.hasOption(STRATEGY_NEXT_LEAGUE_SHORT_OPTION)){
			solver.snl = cmd.getOptionValue(STRATEGY_NEXT_LEAGUE_SHORT_OPTION);
		}else{
			solver.snl = Solver.STRATEGY_NEXT_LEAGUE;
		}
		
		if(cmd.hasOption(GREEDY_COEF_STRATEGY_SHORT_OPTION)){
			solver.gcs = Double.parseDouble(cmd.getOptionValue(GREEDY_COEF_STRATEGY_SHORT_OPTION));
		}else{
			solver.gcs = Solver.GREEDY_COEF_STRATEGY;
		}
	}

}
