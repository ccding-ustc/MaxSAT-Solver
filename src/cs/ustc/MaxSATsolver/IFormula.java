package cs.ustc.MaxSATsolver;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;


/**
 * 存储cnf文件中读取的信息，及其相应的一些方法
 * @author ccding 
 * 2016年3月5日下午8:06:07
 */
public class IFormula{
	List<IClause> clauses;  //所有的句子集合
	ILiteral[] vars;  //formula的所有变量集合
	int nbVar; //变量个数
	int nbClas; //句子个数
	List<IVariable> variables;
	Set<ILiteral> satLits;
	Set<IClause> satClas;
	Set<IClause> unsatClas;
	Set<ILiteral> unsatLits;
	List<IVariable> visVars;
	List<IVariable> unVisVars;
	int minUnsatNum;
	
	


	/**
	 * 设置vars和clauses的容量
	 * @param nbvars
	 * @param nbclauses
	 */
	public void init(int nbvars, int nbclauses) {
		nbVar = nbvars;
		nbClas = nbclauses;
		vars = new ILiteral[nbvars];
		clauses = new LinkedList<>();
		variables = new LinkedList<>();
		satLits = new HashSet<>(nbvars*2);
		unsatLits = new HashSet<>(nbvars*2);
		satClas = new HashSet<>(nbclauses);
		unsatClas = new HashSet<>(nbclauses);
		visVars = new LinkedList<>();
		unVisVars = new LinkedList<>();
	}
	
	/**
	 * 通过读取的id创建literal
	 * @param i
	 * @return
	 */	
	protected ILiteral getLiteral(int i) {
		ILiteral lit;
		int id = Math.abs(i) - 1; // maps from 1..n to 0..n-1
		if (vars[id] == null) {
			vars[id] = new ILiteral(id + 1);
		}
		if (i > 0) {
			lit = vars[id];
		} else {
			lit = vars[id].opposite();
		}
		return lit;
	}
	
	/**
	 *  通过vars添加clause
	 * @param vars
	 */
	public void addClause(List<ILiteral> lits) {
		// create the clause
		IClause clause = new IClause(lits);
		clauses.add(clause);
		for (ILiteral lit : lits) {
			lit.addClause(clause);
			lit.neighbors.addAll(lits);
			lit.neighbors.remove(lit);
		} 
	}
	
	/**
	 * 将 formula 中每个 variable 视作一个 agent，将所有 agents 按照一定规则分成若干个不相交的联盟
	 * 
	 * @param f 存储 cnf 文件信息的 formula
	 * @param gcl 寻找独立集时，采取贪婪策略的随机性大小
	 * @return 不相交的各个分组（联盟）
	 */
	public List<ILeague> constructLeagues(){
		List<ILeague> leagues = new LinkedList<>();
		while(unVisVars.size() != 0){
			List<IVariable> agents = new LinkedList<>();
			List<IVariable> tmp = new LinkedList<>(unVisVars);
			IVariable var;
			while(!tmp.isEmpty()){
//				var = Collections.min(tmp);
				var = tmp.get(0);
				agents.add(var);
				tmp.remove(var);
				tmp.removeAll(var.neighbors);
			}
			ILeague league = new ILeague(agents);
			leagues.add(league);
			unVisVars.removeAll(agents);
		}
		return leagues;
	}
	
	
	
	/**
	 * 
	 *  初始化每个 variable 的邻居，并设置相应的 degree
	 */
	public void setVarsNeighbors(){
		IVariable tmp = null;
		for(IVariable var: variables){
			for(ILiteral lit: var.lit.neighbors){
				tmp = this.getVariable(lit);
				if(!var.neighbors.contains(tmp)){
					var.neighbors.add(tmp);
				}	
			}
			for(ILiteral lit: var.oppositeLit.neighbors){
				tmp = this.getVariable(lit);
				if(!var.neighbors.contains(tmp)){
					var.neighbors.add(tmp);
				}
			}

		}
		unVisVars.addAll(variables);
		
	}
	
	/**
	 * 
	 *  通过 lit 找到对应的 variable
	 * @param lit
	 * @return
	 */
	private IVariable getVariable(ILiteral lit){
		for(IVariable var: variables){
			if(lit.id == var.lit.id || lit.id == var.oppositeLit.id)
				return var;
		}
		return null;
	}
	

	
	/**
	 *  set literals
	 */
	public void setVariables(){
		for (int i = 0; i < vars.length; i++) {
			if(vars[i]!=null){
				vars[i].unsatClas.addAll(vars[i].clauses);
				vars[i].opposite.unsatClas.addAll(vars[i].opposite.clauses);
				variables.add(new IVariable(vars[i]));
			}
		}
	}
	

	
	
	
	public void plusWeight(){
		for(IClause c: unsatClas){
			for(ILiteral l: c.literals){
				l.weight++;
			}
		}
	}
	
		
	
	/**
	 * 
	 * 将 lit 设置为满足，并更新 formula 中的信息
	 * @param lit
	 * @throws IOException 
	 */
	
	public void announceSatLit(ILiteral lit){
		if(this.unsatLits.contains(lit)){
			for(IClause c: lit.clauses){
				c.satLitsNum++;
				c.unsatLitsNum--;
			}
			for(IClause c: lit.opposite.clauses){
				c.satLitsNum--;
				c.unsatLitsNum++;
			}
			this.satLits.add(lit);
			this.satLits.remove(lit.opposite);
			this.unsatLits.remove(lit);
			this.unsatLits.add(lit.opposite);
		}
		
		
		
		for(IClause c: lit.clauses){
			if(!this.satLits.contains(lit))
				c.satLitsNum++;
			if(this.unsatLits.contains(lit))
				c.unsatLitsNum--;
			this.satClas.add(c);
			if(this.unsatClas.contains(c))
				this.unsatClas.remove(c);
			
			for(ILiteral l : c.literals){
				//对 clause c 中所有 lits 通知  c 已满足
				if(! l.satClas.contains(c))
					l.satClas.add(c);
				if(l.unsatClas.contains(c))
					l.unsatClas.remove(c);
			}
			
		}
		for(IClause c: lit.opposite.clauses){
			if(! this.unsatLits.contains(lit.opposite))
				c.unsatLitsNum++;
			if(this.satLits.contains(lit.opposite))
				c.satLitsNum--;
			if(c.unsatLitsNum == c.literals.size()){
				this.unsatClas.add(c);
				if(this.satClas.contains(c))
					this.satClas.remove(c);
				//对 clause c 中所有 lits 通知  c 不满足
				for(ILiteral l : c.literals){
					if(! l.unsatClas.contains(c))
						l.unsatClas.add(c);
					if(l.satClas.contains(c))
						l.satClas.remove(c);
				}
			}		
		}
		
		this.satLits.add(lit);
		if(this.satLits.contains(lit.opposite))
			this.satLits.remove(lit.opposite);
		this.unsatLits.add(lit.opposite);
		if(this.unsatLits.contains(lit))
			this.unsatLits.remove(lit);
	
	}
	/**
	 * 
	 * 将 lits 中 所有 literal 都设置为满足，并更新 formula 中的信息
	 * @param lits
	 * @throws IOException 
	 */
	public void announceSatLits(List<ILiteral> lits){
		for (ILiteral lit : lits) { 
			announceSatLit(lit);
		}
	}
	
	
	public void updateVisVars(ILeague league){
		this.visVars.addAll(league.agents);
		this.unVisVars.removeAll(league.agents);
	}
	
	public void resetFormula(){
		this.unVisVars.addAll(this.variables);
		this.visVars.clear();
		this.satClas.clear();
		this.unsatClas.clear();
		this.satLits.clear();
		this.unsatLits.clear();
		for(IVariable v: this.variables){
			v.visited = false;
			v.lit.satClas.clear();
			v.lit.unsatClas.addAll(v.lit.clauses);
			v.oppositeLit.satClas.clear();
			v.oppositeLit.unsatClas.addAll(v.oppositeLit.clauses);
		}
		for(IClause c: this.clauses){
			c.satLitsNum = 0;
			c.unsatLitsNum = 0;
		}
			
	}
	
}
