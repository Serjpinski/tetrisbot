package bot.neural;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DatasetGenerator {
	
	private static final String DATASET_BASENAME = "p";

	public static FileWriter[] initDataset() throws IOException {
		
		FileWriter[] dataset = new FileWriter[7];
		
		for (int i = 0; i < 7; i++) {
			
			File file = new File(System.getProperty("user.dir") + DATASET_BASENAME + i);
			dataset[i] = new FileWriter(file, true);
		}
		
		return dataset;
	}
}
