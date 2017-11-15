package cs.ustc.MaxSATsolver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


/**
 * 从cnf文件中读取formula信息
 * 
 * @author ccding  2016年3月5日 下午7:09:07
 * 
 */
public class CNFFileReader {
	
	private int expectedNbOfClauses;
	
	/**
	 * 跳过注释部分
	 * @param in 文件读取
	 * @throws IOException
	 */
	void skipComments(BufferedReader in) throws IOException {
		int c;
		do {
			in.mark(4);
			c = in.read();
			if (c == 'c')
				in.readLine();
			else
				in.reset();  
		} while (c == 'c');
	}

	/**
	 * 
	 * @param in 文件读取
	 * @param formula  存储文件信息
	 * @throws IOException
	 * @throws ParseFormatException
	 */
	protected void readProblemLine(BufferedReader in, IFormula formula)
		throws IOException{

		// 读取cnf文件问题规模一行，形如 p cnf vars clauses
		String line = in.readLine();
		String[] strs = line.split("\\s+");
		int vars;
		// 读取变量个数
		vars = Integer.parseInt(strs[2]);
		// 读取句子个数
		expectedNbOfClauses = Integer.parseInt(strs[3]);
		formula.init(vars, expectedNbOfClauses);
	}

	/**
	 * 逐行读取句子
	 * @param in 文件读取
	 * @param formula 存储文件信息
	 * @throws IOException
	 * @throws ParseFormatException
	 */
	protected void readClauses(BufferedReader in, IFormula formula)
		throws IOException{	
		String line = null;
		while (true) {
			line = in.readLine();
			// 读取文件结束
			if (line == null) {
				break;
			}
			StringTokenizer st = new StringTokenizer(line, " ");
			List<ILiteral> literals = new ArrayList<>();
			
			while(st.hasMoreTokens()) {
				int id = Integer.parseInt(st.nextToken());
				if (id != 0) {
					ILiteral lit = formula.getLiteral(id);
   					literals.add(lit);
				} else {
					formula.addClause(literals);
				}
			}
		}
	}

	public void parseInstance(String filename, IFormula formula)
		throws FileNotFoundException, IOException {
			parseInstance(
				new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(filename)),"utf-8"), 100*1024*1024),
				formula);
	}

	private void parseInstance(BufferedReader in, IFormula formula) throws IOException{
		skipComments(in);
		readProblemLine(in, formula);
		readClauses(in, formula);

	}
	
	/**
	 *  读取 cnf 文件，并将信息存入到 formula 中
	 * @param cnfFile
	 * @return formula
	 * @throws ParseFormatException
	 * @throws IOException
	 */
	public IFormula initFormulaOfCNFFile(String cnfFile) throws IOException{
		IFormula f = new IFormula();
		parseInstance(cnfFile, f);
		return f;
	}
}

