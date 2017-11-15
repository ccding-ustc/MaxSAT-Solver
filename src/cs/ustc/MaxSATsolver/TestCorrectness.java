package cs.ustc.MaxSATsolver;

import java.util.HashSet;
import java.util.Set;
/**
 * 
 * @ClassName: TestCorrectness
 *
 * @Description: 测试求出的解是否正确 
 *
 * @author: ccding
 * @date: 2016年11月7日 下午6:41:38
 *
 */
public class TestCorrectness {
	
	public static int getUnsatClasNumFromFormula(IFormula f){	
		int[] s = {-47 , -14 , -12 , -16 , -44 , -46 , -9 , -42 , 49 , 33 , 51 , 52 , 39 , 54 , 36 , 55 , 24 , 20 , -31 , -60 , -26 , -27 , -32 , 19 , -28 , -57 , -13 , 53 , 17 , -29 , 37 , -41 , 1 , 3 , 2 , 7 , 40 , 4 , 5 , 8 , 35 , -10 , 38 , -58 , 34 , 50 , -62 , -59 , -48 , -15 , -45 , -43 , -63 , -11 , 6 , 56 , 18 , 23 , 21 , 22 , -30 , -64 , -61 , -25 };
		Set<IClause> satClas = new HashSet<>(f.clauses.size());
		for(int ss: s){
			IVariable v = f.vars[Math.abs(ss)-1];
			if(ss > 0){
				satClas.addAll(v.lit.clas);
			}else{
				satClas.addAll(v.oppositeLit.clas);
			}
		}
		return (f.clauses.size() - satClas.size());
	}
}
