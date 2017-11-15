package tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * 读取目录下所有文件
 * @author ccding
 *
 */
public class FilesReader {
	public static final List<File> getCNFFiles(File path) throws IOException{
		//获取 path 目录下的所有 .cnf 文件路径
		Path filesPath = Paths.get(path.getAbsolutePath());
 		final List<File> files = new ArrayList<File>();
 		SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>(){
 		    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException{
 		    	if(file.toFile().getName().endsWith(".cnf"))
 		    		files.add(file.toFile());
 		        return super.visitFile(file, attrs);
 		    }
 		};
 		java.nio.file.Files.walkFileTree(filesPath, finder);
 		return files;
	}
}
