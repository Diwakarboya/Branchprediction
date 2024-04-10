import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Smith {
	private int bits;
	private int counter;
	private int changeValue;
	private int saturationCounter;
	private int mispredictions;
	private int predictions;
	private String fileName;
	private String[] lines;

	private static final String NOT_TAKEN = "n";
	private static final String TAKEN = "t";

	public Smith(int bits, String file) {
//		System.out.println("Received Bits : " + bits);
//		System.out.println("Received file : " + file);

		this.bits = bits;
		this.counter = (int) Math.pow(2, bits - 1);
		this.changeValue = (int) Math.pow(2, bits - 1);
		this.saturationCounter = (int) Math.pow(2, bits) - 1;
		this.mispredictions = 0;
		this.predictions = 0;
		this.fileName = file.substring(file.lastIndexOf('/') + 1);

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
		if (counter > saturationCounter) {
			counter = saturationCounter;
		}
	}

	public void decrementCounter() {
		counter--;
		if (counter < 0) {
			counter = 0;
		}
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