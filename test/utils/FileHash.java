package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class FileHash {

	public static String getMD5Hash(String fileLocation) throws FileNotFoundException{
		File f = new File(fileLocation);
		if(!f.exists()){
			throw new FileNotFoundException();
		}
		HashCode code = null;
		try {
			code = Files.hash(f, Hashing.md5());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return code.toString();
	}
	
	public static boolean hashesEqual(String file, String hash) throws FileNotFoundException{
		return getMD5Hash(file).equals(hash);
	}
	public static void main(String[] args) {
		String file = "TestData/dependencies/multiple-shared.krss";
		try {
			System.out.println(FileHash.getMD5Hash(file));
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
	}
}
