package cs.ustc.MaxSATsolver;

import java.util.*;

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
	public static int COUNT = 0;
	public int id;
	public List<IVariable> agents;
	ILiteral[] solution;
	Set<IClause> clas;
	boolean isInit;
	public Map<ILeague, Integer> neighbors;
	public int degree;
	public boolean vis;
	
	
	public ILeague(List<IVariable> agents){
		id = COUNT++;
		this.agents = agents; 
		solution = new ILiteral[agents.size()];
		this.clas = new HashSet<>();
		neighbors = new HashMap<>();
		for(IVariable agent: agents){
			clas.addAll(agent.lit.clas);
			clas.addAll(agent.oppositeLit.clas);
		}
		vis = false;
	}
	
	
	/**
	 * 迭代时，求反转变量的集合
	 */
	public List<ILiteral> getSolutionMIS(){
		//构造每组的初始解，贪婪策略
		if(!isInit){
			for(int i=0; i<agents.size(); i++){
				IVariable var = agents.get(i);
				solution[i] = var.lit.unsatClas.size() > var.oppositeLit.unsatClas.size() ? var.lit : var.oppositeLit;
			}
			isInit = true;
			return Arrays.asList(solution);
		}
		List<ILiteral> flipLits = new LinkedList<>();
		for(int i=0; i<agents.size(); i++){
			IVariable var = agents.get(i);
			if(flipVariable(i) > 0){
				if(solution[i] == var.lit){
					solution[i] = var.oppositeLit;
					flipLits.add(var.oppositeLit);
				}else{
					solution[i] = var.lit;
					flipLits.add(var.lit);
				}
			}
		}
		return flipLits;
	}
	
	public ILeague getMax() {
		int val = -1;
		ILeague l = null;
		for(ILeague tmp: neighbors.keySet()) {
			if(neighbors.get(tmp) > val && !tmp.vis) {
				l = tmp;
				val = neighbors.get(tmp);
			}
		}
		return l;
	}
	
	
	public ILeague getMin() {
		int val = Integer.MAX_VALUE;
		ILeague l = null;
		for(ILeague tmp: neighbors.keySet()) {
			if(neighbors.get(tmp) < val && !tmp.vis) {
				l = tmp;
				val = neighbors.get(tmp);
			}
		}
		return l;
	}

	public List<ILiteral> getSolutionMC(){
		int varNum = agents.size();
		double optUnsatNum = Integer.MAX_VALUE;
		int[] optBinaryInt = null;
		for(int i=0; i<Math.pow(2, varNum); i++){
			String binaryStr = Integer.toBinaryString(i);
			int[] binaryInt = new int[varNum];
			for(int j=0; j<binaryStr.length(); j++)
				binaryInt[j] = binaryStr.charAt(j)== '0' ? 0 : 1;
			double unsatNum = calUnsatNum(binaryInt);
			if(unsatNum < optUnsatNum){
				optUnsatNum = unsatNum;
				optBinaryInt = binaryInt;
			}
		}
		if(!isInit){
			for(int i=0; i<optBinaryInt.length; i++){
				IVariable var = agents.get(i);
				solution[i] = optBinaryInt[i] == 0 ? var.oppositeLit : var.lit;
			}
			isInit = true;
			return Arrays.asList(solution);
		}
		List<ILiteral> flipLits = new ArrayList<>();
		for(int i=0; i<optBinaryInt.length; i++){
			IVariable var = agents.get(i);
			if(optBinaryInt[i]==0){
				if(solution[i] == var.lit){
					flipLits.add(var.oppositeLit);
					solution[i] = var.oppositeLit;
				}
			}else{
				//opt is lit
				if(solution[i] == var.oppositeLit){
					flipLits.add(var.lit);
					solution[i] = var.lit;
				}
			}
		}
		return flipLits;
	}
	
	private double calUnsatNum(int[] binaryInt){
		Set<IClause> tmp = new HashSet<>();
		for(int i=0; i<agents.size(); i++){
			IVariable var = agents.get(i);
			if(solution[i] == var.lit){
				if(binaryInt[i] == 0 ){
					//1 -> -1
					for(IClause c: var.lit.clas){
						if(c.satLitsNum <= 1)
							tmp.add(c);
					}
				}else{
					//1 -> 1
					tmp.addAll(var.oppositeLit.unsatClas);
				}
			}else{
				if(binaryInt[i] == 1){
					//-1 -> 1 or null -> 1
					for(IClause c: var.oppositeLit.clas){
						if(c.satLitsNum <= 1)
							tmp.add(c);
					}
				}else{
					//-1 -> -1 or null -> -1
					tmp.addAll(var.lit.unsatClas);
				}
			}
		}
		for(int i=0; i<binaryInt.length; i++){
			IVariable var = agents.get(i);
			tmp.removeAll(binaryInt[i]==1 ? var.lit.clas : var.oppositeLit.clas);
		}
		double res = 0;
		for(IClause c: tmp)
			res = res + 1 + c.hardCoef;
		return res;
	}
	
	
	private double flipVariable(int idx){
		IVariable var = agents.get(idx);
		ILiteral satLit = solution[idx] == var.lit ? var.lit : var.oppositeLit;
		ILiteral unsatLit = satLit.opposite;
		double incPayoff = unsatLit.unsatClas.size() + ((double)unsatLit.weight) * 0.1;
		double decPayoff = (double)satLit.weight * 0.1;
		for(IClause c: satLit.satClas){
			if(c.satLitsNum == 1)
				decPayoff+=1;
		}
		return incPayoff - decPayoff;
	} 	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(IVariable v: agents)
			sb.append(v.lit.id+" ");
		return sb.toString();
	}

}
