package cs.ustc.MaxSATsolver;

import java.util.*;



/**
 * 
 * @author ccding 
 * 2016年3月5日下午8:51:47
 */

public class ILiteral implements Comparable<ILiteral>{
	final List<IClause> clas; //含有该literal的所有clauses
	Set<IClause> satClas;
	Set<IClause> unsatClas;
	ILiteral opposite; 
	public final int id;
	int weight;
//	boolean forbid;
//	boolean lastModified;

	
	/**
	 * 构造literal
	 * @param id
	 */
	protected ILiteral(int id){
		this.id = id;
		this.opposite = new ILiteral(this);
		clas = new LinkedList<>();
//		neighbors = new HashMap<>();
		satClas = new HashSet<>();
		unsatClas = new HashSet<>();
		weight = 0;
	}
	
	 /**
     * This constructor is to be used only to create the opposite literal.
     * 
     * @param opposite
     */
    private ILiteral(ILiteral opposite) {
        id = -opposite.id;
        this.opposite = opposite;
        clas = new LinkedList<>();
//        neighbors = new HashMap<>();
        satClas = new HashSet<>();
		unsatClas = new HashSet<>();
        weight = 0;
    }
    
    
    public int flipIncrease(){
    	int satNum = 0;
    	int unsatNum = 0;
    	for(IClause c: clas)
    		if(c.satLitsNum==1)
    			unsatNum++;
    	for(IClause c: opposite.clas)
    		if(c.satLitsNum==0)
    			satNum++;
    	return satNum-unsatNum;
    }
    
    
    public ILiteral opposite() {
		return opposite;
	}
    
    public String toString(){
    	return id+" ";
    }

	@Override
	public int compareTo(ILiteral lit) {
		return Math.abs(this.id) - Math.abs(lit.id);
	}
}
