package cs.ustc.MaxSATsolver;

import java.util.*;


/**
 * 存储cnf文件中读取的信息，及其相应的一些方法
 * @author ccding 
 * 2016年3月5日下午8:06:07
 */
public class IFormula{
	List<IClause> clauses;  //所有的句子集合
	ILiteral[] lits;  //formula的所有变量集合
	public int nbVar; //变量个数
	public int nbClas; //句子个数
	public IVariable[] vars;
	Set<ILiteral> satLits;
	Set<IClause> satClas;
	Set<IClause> unsatClas;
	Set<ILiteral> unsatLits;
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
		lits = new ILiteral[nbvars];
		vars = new IVariable[nbvars];
		clauses = new LinkedList<>();
		satLits = new HashSet<>(nbvars*2);
		unsatLits = new HashSet<>(nbvars*2);
		satClas = new HashSet<>(nbclauses);
		unsatClas = new HashSet<>(nbclauses);
//		visVars = new LinkedList<>();
		unVisVars = new LinkedList<>();
		for(int i=0; i<nbvars; i++) {
			lits[i] = new ILiteral(i+1);
			vars[i] = new IVariable(lits[i]);
			unVisVars.add(vars[i]);
		}
	}
	
	/**
	 * 通过读取的id创建literal
	 * @param i
	 * @return
	 */	
	public ILiteral getLiteral(int i) {
		int id = Math.abs(i) - 1; // maps from 1..n to 0..n-1
		if (i > 0) {
			return lits[id];
		} else {
			return lits[id].opposite();
		}
	}
	
	/**
	 *  通过vars添加clause
	 * @param vars
	 */
	public void addClause(List<ILiteral> lits) {
		// create the clause
		IClause clause = new IClause(lits);
		clauses.add(clause);
		for(ILiteral lit: lits) {
			lit.clas.add(clause);
			lit.unsatClas.add(clause);
		}
		for (int i=0; i<lits.size(); i++) {
			IVariable var1 = getVariable(lits.get(i));
			for(int j=i+1; j<lits.size(); j++) {
				IVariable var2 = getVariable(lits.get(j));
				//将var2记录至var1邻居
				if(var1.neighbors.get(var2) == null) {
					var1.neighbors.put(var2, 1);
				}else {
					int val = var1.neighbors.get(var2);
					var1.neighbors.put(var2, val+1);
				}
				//将var1记录至var2邻居
				if(var2.neighbors.get(var1)==null) {
					var2.neighbors.put(var1, 1);
				}else {
					int val = var2.neighbors.get(var1);
					var2.neighbors.put(var1, val+1);
				}
					
			}
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
//		for(IVariable v: unVisVars) {
//			List<IVariable> agents = new LinkedList<>();
//			agents.add(v);
//			leagues.add(new ILeague(agents));
//		}
		while(unVisVars.size()!=0) {
			List<IVariable> agents = new LinkedList<>();
			List<IVariable> tmp = new LinkedList<>(unVisVars);
			IVariable var;
			while(!tmp.isEmpty()){
				var = tmp.remove(0);
				agents.add(var);
				tmp.removeAll(var.neighbors.keySet());
			}
			if(agents.size() != 0) {
				ILeague league = new ILeague(agents);
				leagues.add(league);
				unVisVars.removeAll(agents);
			}

		}
		return leagues;
	}
	
	public void setNeighborsOfLeagues(List<ILeague> leagues) {
		int size = leagues.size();
		for(int i=0; i<size; i++) {
			ILeague l1 = leagues.get(i);
			for(int j=i+1; j<size; j++) {
				ILeague l2 = leagues.get(j);
				int val = 0;
				for(IVariable a1: l1.agents) {
					for(IVariable a2: l2.agents) {
						if(a1.neighbors.containsKey(a2))
							val += a1.neighbors.get(a2);
					}
				}
				l1.neighbors.put(l2, val);
				l1.degree+=val;
				l2.neighbors.put(l1, val);
				l2.degree+=val;
			}
		}
	}
	
	
	/**
	 * 
	 *  通过 lit 找到对应的 variable
	 * @param lit
	 * @return
	 */
	private IVariable getVariable(ILiteral lit){
		return vars[Math.abs(lit.id)-1];
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
			for(IClause c: lit.clas){
				c.satLitsNum++;
				c.unsatLitsNum--;
			}
			for(IClause c: lit.opposite.clas){
				c.satLitsNum--;
				c.unsatLitsNum++;
			}
			this.satLits.add(lit);
			this.satLits.remove(lit.opposite);
			this.unsatLits.remove(lit);
			this.unsatLits.add(lit.opposite);
		}
		
		
		
		for(IClause c: lit.clas){
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
		for(IClause c: lit.opposite.clas){
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
	
	
//	public void updateVisVars(ILeague league){
////		this.visVars.addAll(league.agents);
//		this.unVisVars.removeAll(league.agents);
//	}
	
//	public void resetFormula(){
//		this.unVisVars.addAll(Arrays.asList(this.vars));
//		this.visVars.clear();
//		this.satClas.clear();
//		this.unsatClas.clear();
//		this.satLits.clear();
//		this.unsatLits.clear();
//		for(IVariable v: this.vars){
//			v.visited = false;
//			v.lit.satClas.clear();
//			v.lit.unsatClas.addAll(v.lit.clas);
//			v.oppositeLit.satClas.clear();
//			v.oppositeLit.unsatClas.addAll(v.oppositeLit.clas);
//		}
//		for(IClause c: this.clauses){
//			c.satLitsNum = 0;
//			c.unsatLitsNum = 0;
//		}
//			
//	}
	
}
