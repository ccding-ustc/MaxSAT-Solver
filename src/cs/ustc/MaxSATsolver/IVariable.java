package cs.ustc.MaxSATsolver;

import java.util.HashSet;
import java.util.Set;
/**
 * 
 * @ClassName: IVariable
 *
 * @Description: 
 *
 * @author: ccding
 * @date: 2016年10月6日 上午9:30:17
 *
 */
public class IVariable implements Comparable<IVariable>{
	ILiteral lit;
	ILiteral oppositeLit;
//	Set<IClause> clauses;
	Set<IVariable> neighbors;
	
	boolean visited;
	public IVariable(ILiteral lit){
		this.neighbors = new HashSet<>();
		visited = false;
		this.lit = lit;
		this.oppositeLit = lit.opposite;
	}
	@Override
	public int compareTo(IVariable var) {
		//  Auto-generated method stub
		int num1,num2;
		if(var.lit.unsatClas.size() == 0){
			num1 = var.oppositeLit.unsatClas.size() + var.oppositeLit.weight;
		}else{
			num1 = var.lit.unsatClas.size() + var.lit.weight;
		}
		if(this.lit.unsatClas.size() == 0){
			num2 = this.oppositeLit.unsatClas.size() + this.oppositeLit.weight;
		}else{
			num2 = this.lit.unsatClas.size() + this.lit.weight;
		}
		return num2-num1;
	}
	
	@Override
	public String toString(){
		return lit.id+" "+oppositeLit.id;
	}

}
