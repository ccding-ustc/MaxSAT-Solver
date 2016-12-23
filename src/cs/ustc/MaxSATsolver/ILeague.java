package cs.ustc.MaxSATsolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @ClassName: ILeague
 *
 * @Description:  
 *
 * @author: ccding
 * @date: 2016年10月19日 上午8:45:23
 *
 */
public class ILeague {
	List<IVariable> agents;
	List<ILiteral> solution;
	Map<ILeague, Integer>  neighbors;
	
	public ILeague(List<IVariable> agents){
		this.agents = new ArrayList<>(agents); 
		solution = new ArrayList<>(agents.size());
		neighbors = new HashMap<>();
	}
	
	
	/**
	 * 求每个组对应最好的解
	 * @param randomCoefSolution 采用贪婪的随机性大小
	 */
	public List<ILiteral> getSolution(double randomCoefSolution){
		//构造每组的初始解，贪婪策略
		if(solution.isEmpty()){
			for(IVariable var: agents){	
				solution.add(var.lit.unsatClas.size() > var.oppositeLit.unsatClas.size() ? var.lit : var.oppositeLit);
			}
			return solution;
		}
		
		
		List<ILiteral> flipLits = new ArrayList<>();
		Collections.sort(agents);
		for(IVariable var: agents){
			if(flipVariable(var) > 0){
				if(solution.contains(var.lit)){
					solution.remove(var.lit);
					solution.add(var.oppositeLit);
					flipLits.add(var.oppositeLit);
				}else{
					solution.remove(var.oppositeLit);
					solution.add(var.lit);
					flipLits.add(var.lit);
				}
			}
		}
		return flipLits;
	}
	
	public double flipVariable(IVariable var){
		ILiteral satLit = solution.contains(var.lit) ? var.lit : var.oppositeLit;
		ILiteral unsatLit = satLit.opposite;
		double increasdeWeight = unsatLit.unsatClas.size() + ((double)unsatLit.weight) * 0.1;
		double decreasedWeight = (double)satLit.weight * 0.1;
		for(IClause c: satLit.satClas){
			if(c.satLitsNum == 1)
				decreasedWeight+=1;
		}
		return increasdeWeight - decreasedWeight;
	} 

	/**
	 * 设置每个 league 的邻居， 两个 league 是邻居当且仅当组中 agent 出现在同一个 clause 中
	 * 
	 * @param leagues 
	 */
	public static void initLeagueNeighbors(List<ILeague> leagues){
		List<IVariable> tmp = new ArrayList<>();
		List<IVariable> tmpCopy = new ArrayList<>();
		ILeague leagueTmp1 = null;
		ILeague leagueTmp2 = null;
		for(int i=0; i<leagues.size(); i++){
			leagueTmp1 = leagues.get(i);
			for(IVariable agent: leagueTmp1.agents){
				tmp.addAll(agent.neighbors);
			}
		
			for(int j=i+1; j<leagues.size(); j++){
				tmpCopy.addAll(tmp);
				leagueTmp2 = leagues.get(j);
				tmpCopy.retainAll(leagueTmp2.agents);
				if(tmpCopy.size()>0){
					leagueTmp1.neighbors.put(leagueTmp2, tmpCopy.size());
					leagueTmp2.neighbors.put(leagueTmp1, tmpCopy.size());
				}
				tmpCopy.clear();
			}
			tmp.clear();
		}
	}
	
	

}
