package cs.ustc.MaxSATsolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * 
 * @ClassName: IGroup
 *
 * @Description:  
 *
 * @author: ccding
 * @date: 2016年10月19日 上午8:45:23
 *
 */
public class IGroup {
	Set<IVariable> agents;
	List<ILiteral> solution;
	Map<IGroup, Integer>  neighbors;
	
	public IGroup(Set<IVariable> agents){
		this.agents = new HashSet<>(agents); 
		solution = new ArrayList<>(agents.size());
		neighbors = new HashMap<>();
	}
	
	
	/**
	 * 求每个组对应最好的解
	 * @param randomCoefSolution 采用贪婪的随机性大小
	 */
	public void getSolution(double randomCoefSolution){
		solution.clear();

		for(IVariable var: agents){
			if(Math.random() < randomCoefSolution){
				if(var.lit.weight + var.lit.unsatClas.size() > 
				var.oppositeLit.weight + var.oppositeLit.unsatClas.size()){
					solution.add(var.lit);
				}else{
					solution.add(var.oppositeLit);
				}
			}else{
				if(Math.random()>0.5){
					solution.add(var.lit);
				}else{
					solution.add(var.oppositeLit);
				}
			}
		}
	}
	
	/**
	 * 设置每个 group 的邻居， 两个 group 是邻居当且仅当组中 agent 出现在同一个 clause 中
	 * 
	 * @param groups 
	 */
	public static void initGroupNeighbors(List<IGroup> groups){
		List<IVariable> tmp = new ArrayList<>();
		List<IVariable> tmpCopy = new ArrayList<>();
		IGroup groupTmp1 = null;
		IGroup groupTmp2 = null;
		for(int i=0; i<groups.size(); i++){
			groupTmp1 = groups.get(i);
			for(IVariable agent: groupTmp1.agents){
				tmp.addAll(agent.neighbors);
			}
		
			for(int j=i+1; j<groups.size(); j++){
				tmpCopy.addAll(tmp);
				groupTmp2 = groups.get(j);
				tmpCopy.retainAll(groupTmp2.agents);
				if(tmpCopy.size()>0){
					groupTmp1.neighbors.put(groupTmp2, tmpCopy.size());
					groupTmp2.neighbors.put(groupTmp1, tmpCopy.size());
				}
				tmpCopy.clear();
			}
			tmp.clear();
		}
	}
	
	

}
