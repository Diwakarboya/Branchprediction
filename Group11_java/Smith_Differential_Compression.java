import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Smith_Differential_Compression {
	private int bits;
	private int counter;
	private int changeValue;
	private int mispredictions;
	private int predictions;
	private String fileName;
	private String[] lines;
	private int differentailCounter;

	private static final String NOT_TAKEN = "n";
	private static final String TAKEN = "t";

	public Smith_Differential_Compression(int bits, String file) {
//		System.out.println("Received Bits : " + bits);
//		System.out.println("Received file : " + file);

		this.bits = bits;
		this.counter = (int) Math.pow(2, bits - 1);
		this.changeValue = (int) Math.pow(2, bits - 1);
		this.mispredictions = 0;
		this.predictions = 0;
		this.fileName = file.substring(file.lastIndexOf('/') + 1);
		this.differentailCounter = 0;

		// Read the file
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			StringBuilder dataBuilder = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				dataBuilder.append(line).append("\n");
			}
			this.lines = dataBuilder.toString().split("\n");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String predictBranch() {
		if (counter >= changeValue) {
			return TAKEN;
		} else {
			return NOT_TAKEN;
		}
	}

	public void incrementCounter() {
		counter++;
		differentailCounter++;
	}

	public void decrementCounter() {
		counter--;
		differentailCounter--;
	}

	public void run() {
		for (String line : lines) {
			if (line.equals("")) {
				continue;
			}

			predictions++;

			// Split the line into the address and the prediction
			String[] addressAndPrediction = line.split(" ");
			String prediction = addressAndPrediction[1];
			String ourPrediction = predictBranch();

			// Compare the prediction to the actual prediction
			if (!ourPrediction.equals(prediction)) {
				mispredictions++;
			}

			// Update the counter
			if (prediction.equals(TAKEN)) {
				incrementCounter();
			} else if (prediction.equals(NOT_TAKEN)) {
				decrementCounter();
			}

			if (differentailCounter > 3) {
				differentailCounter = 3;
			} else if (differentailCounter < -4) {
				differentailCounter = -4;
			}
			counter += differentailCounter;
		}
	}

	public double getMispredictionRate() {
		return ((double) mispredictions / predictions) * 100;
	}

	public void printResults() {
		System.out.println("COMMAND");
		System.out.println("./sim smith " + bits + " " + fileName);
		System.out.println("OUTPUT");
		System.out.println("number of predictions:     \t" + predictions);
		System.out.println("number of mispredictions:  \t" + mispredictions);
		System.out.printf("misprediction rate:        \t%.2f%%\n", getMispredictionRate());
		System.out.println("FINAL COUNTER CONTENT:     \t" + counter);
	}
}