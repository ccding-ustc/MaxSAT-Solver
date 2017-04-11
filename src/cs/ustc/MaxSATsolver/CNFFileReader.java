package cs.ustc.MaxSATsolver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;



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
		throws IOException, ParseFormatException {

		// 读取cnf文件问题规模一行，形如 p cnf vars clauses
		String line = in.readLine();
		if (line == null)
			throw new ParseFormatException(
				"premature end of file : problem line expected (p cnf ...) ");
		StringTokenizer stk = new StringTokenizer(line);
		if (!(stk.hasMoreTokens()
			&& stk.nextToken().equals("p")
			&& stk.hasMoreTokens()
			&& stk.nextToken().equals("cnf")))
			throw new ParseFormatException(
				"problem line expected (p cnf ...) on line ");
		int vars;
		// 读取变量个数
		vars = Integer.parseInt(stk.nextToken());
		assert vars > 0;
		// 读取句子个数
		expectedNbOfClauses = Integer.parseInt(stk.nextToken());
		assert expectedNbOfClauses > 0;
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
		throws IOException, ParseFormatException {		
		int realNbOfClauses = 0;
		int nbVars = formula.nbVar;
		assert nbVars != 0;

		while (true) {
			String line = in.readLine();
			// 读取文件结束
			if (line == null) {
				break;
			}
			List<ILiteral> literals = new LinkedList<>();
			String[] strs = line.split(" ");
			for(String str: strs) {
				int lit = Integer.parseInt(str);
				if (Math.abs(lit) > nbVars) {
					throw new ParseFormatException(
						"var id greater than maxvarid on line "+ "("+ lit+ nbVars+ ")");
				}
				if (lit != 0) {
					ILiteral literal = formula.getLiteral(lit);
   					literals.add(literal);
				} else {
					formula.addClause(literals);
					realNbOfClauses++;
				}
			}
		}
		if (expectedNbOfClauses != realNbOfClauses) {
			throw new ParseFormatException(
				"wrong nbclauses parameter. Found "+ realNbOfClauses+ ", "+ expectedNbOfClauses+ " expected");
		}
	}

	public void parseInstance(String filename, IFormula formula)
		throws FileNotFoundException, ParseFormatException, IOException {
			parseInstance(
				new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(filename)),"utf-8"), 100*1024*1024),
				formula);
	}

	private void parseInstance(BufferedReader in, IFormula formula)
		throws ParseFormatException {
		try {
			skipComments(in);
			readProblemLine(in, formula);
			readClauses(in, formula);
		} catch (IOException e) {
			throw new ParseFormatException(e);
		} catch (NumberFormatException e) {
			throw new ParseFormatException(
				"integer value expected on line ",
				e);
		}

	}
}

