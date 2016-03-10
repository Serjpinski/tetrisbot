package bot.neural;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DatasetBalancer {

	public static void main (String[] args) throws IOException {
		
		String baseName = args[0];
		boolean reduced = args[1].equals("r");
		
		BufferedReader[] brs = new BufferedReader[7];
		
		for (int i = 0; i < 7; i++)
			brs[i] = new BufferedReader(new FileReader(
					System.getProperty("user.dir") + "/" + baseName + "piece" + i + ".csv"));
		
		ArrayList<ArrayList<Sample>> dataset = readDataset(brs, reduced);
		balanceDataset(dataset);
		
		FileWriter[] fws = new FileWriter[7];
		for (int i = 0; i < 7; i++)
			fws[i] = new FileWriter(new File(
					System.getProperty("user.dir") + "/" + baseName + "piece" + i + "balanced.csv"));
		
		writeDataset(fws, dataset);
	}
	
	private static ArrayList<ArrayList<Sample>> readDataset(BufferedReader[] files, boolean reduced)
			throws IOException {
		
		ArrayList<ArrayList<Sample>> dataset = new ArrayList<ArrayList<Sample>>();
		
		for (int i = 0; i < 7; i++) {

			ArrayList<Sample> pieceDataset = new ArrayList<Sample>();

			files[i].readLine();
			
			String line = files[i].readLine();
			
			while (line != null) pieceDataset.add(Sample.parseSample(line, reduced));
		}
		
		return dataset;
	}
	
	private static void balanceDataset(ArrayList<ArrayList<Sample>> dataset) {
		
		
	}
	
	private static void writeDataset(FileWriter[] files, ArrayList<ArrayList<Sample>> dataset) {
		
		
	}
}
