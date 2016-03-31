package neural;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import core.Move;

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
					System.getProperty("user.dir") + "/" + baseName + "_balanced_piece" + i + ".csv"));
		
		writeDataset(fws, dataset, reduced);
	}
	
	private static ArrayList<ArrayList<Sample>> readDataset(BufferedReader[] files, boolean reduced)
			throws IOException {
		
		ArrayList<ArrayList<Sample>> dataset = new ArrayList<ArrayList<Sample>>();
		
		for (int i = 0; i < 7; i++) {
			
			System.out.print("Parsing dataset for piece " + i + "... ");

			ArrayList<Sample> pieceDataset = new ArrayList<Sample>();

			files[i].readLine();
			
			String line = files[i].readLine();
			
			while (line != null) {
				
				pieceDataset.add(Sample.parseSample(line, reduced));
				line = files[i].readLine();
			}
			
			dataset.add(pieceDataset);
			files[i].close();
			System.out.println("done");
		}
		
		return dataset;
	}
	
	private static void balanceDataset(ArrayList<ArrayList<Sample>> dataset) {
			
		System.out.print("Balancing dataset... ");
		
		for (int i = 0; i < 7; i++) {
			
			ArrayList<Sample> pieceDataset = dataset.get(i);
			int[] maxSamples = new int[Move.COL_VAR_SUM_LIST[i]];
			
			// Counts the number of samples of each class
			for (int j = 0; j < pieceDataset.size(); j++)
				maxSamples[pieceDataset.get(j).moveCode]++;
			
			int minSamples = pieceDataset.size();
			
			// Computes the minimum number of samples for any class
			for (int j = 0; j < maxSamples.length; j++)
				if (maxSamples[j] < minSamples) minSamples = maxSamples[j];
			
			// Computes the number of samples that will be saved for each class
			for (int j = 0; j < maxSamples.length; j++)
				maxSamples[j] = Math.round((maxSamples[j] *
						((pieceDataset.size() - maxSamples[j] + minSamples) / (float) pieceDataset.size())));
			
			int[] savedSamples = new int[Move.COL_VAR_SUM_LIST[i]];
			int index = 0;
			
			// Removes the leftover samples
			while (index < pieceDataset.size()) {
				
				int moveCode = pieceDataset.get(index).moveCode;
				
				if (savedSamples[moveCode] < maxSamples[moveCode]) {
					
					savedSamples[moveCode]++;
					index++;
				}
				else pieceDataset.remove(index);
			}
			
			// Ramdomizes the dataset
			Collections.shuffle(pieceDataset);
		}
		
		System.out.println("done");
	}
	
	private static void writeDataset(FileWriter[] files, ArrayList<ArrayList<Sample>> dataset, boolean reduced)
			throws IOException {
		
		System.out.print("Writing new dataset... ");
		
		for (int i = 0; i < 7; i++) {
			
			files[i].write(Sample.getHeader(reduced) + "\n");
			
			ArrayList<Sample> pieceDataset = dataset.get(i);
			
			for (int j = 0; j < pieceDataset.size(); j++)
				files[i].write(pieceDataset.get(j).toString() + "\n");
			
			files[i].close();
		}
		
		System.out.println("done");
	}
}
