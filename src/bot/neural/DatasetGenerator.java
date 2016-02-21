package bot.neural;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DatasetGenerator {

	public static FileWriter[] initDataset(String baseName) throws IOException {

		FileWriter[] dataset = new FileWriter[7];

		for (int i = 0; i < 7; i++) {

			File file = new File(System.getProperty("user.dir") + "/" + baseName + "piece" + i + ".csv");

			if (file.exists()) dataset[i] = new FileWriter(file, true);
			else {

				dataset[i] = new FileWriter(file, true);
				dataset[i].write(Instance.getHeader() + "\n");
			}
		}

		return dataset;
	}
}
