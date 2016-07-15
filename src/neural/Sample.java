package neural;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class containing the representation of a training sample.
 */
public abstract class Sample {
	
	public int moveCode;
	
	/**
	 * Returns the header of the sample.
	 */
	public static String getHeader(boolean reduced) {
		
		if (reduced) return "i0, i1, i2, i3, i4, i5, i6, i7, i8, o";
		else {
			
			String header = "";
			
			for (int i = 0; i < 200; i++) header += "i" + i + ", ";
			
			return header + "o";
		}
	}
	
	/**
	 * Initializes and returns the dataset writing objects.
	 */
	public static FileWriter[] initDataset(String baseName, boolean reduced) throws IOException {

		FileWriter[] dataset = new FileWriter[7];

		for (int i = 0; i < 7; i++) {

			File file = new File(System.getProperty("user.dir") + "/" + baseName + "piece" + i + ".csv");

			if (file.exists()) dataset[i] = new FileWriter(file, true);
			else {

				dataset[i] = new FileWriter(file, true);
				dataset[i].write(getHeader(reduced) + "\n");
			}
		}

		return dataset;
	}

	/**
	 * Computes a sample from the given string representation.
	 */
	public static Sample parseSample(String string, boolean reduced) {
		
		if (reduced) return new ReducedSample(string);
		else return new FullSample(string);
	}
}
