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
	private int differentialCounter;
	private int adaptiveChangeCounter; // Counter to adjust changeValue based on performance

	private static final String NOT_TAKEN = "n";
	private static final String TAKEN = "t";

	public Smith_Differential_Compression(int bits, String file) {
		this.bits = bits;
		this.counter = (int) Math.pow(2, bits - 1);
		this.changeValue = (int) Math.pow(2, bits - 1);
		this.mispredictions = 0;
		this.predictions = 0;
		this.fileName = file.substring(file.lastIndexOf('/') + 1);
		this.differentialCounter = 0;
		this.adaptiveChangeCounter = 0;

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
		differentialCounter++;
		adaptiveChangeCounter++;
	}

	public void decrementCounter() {
		counter--;
		differentialCounter--;
		adaptiveChangeCounter--;
	}

	private void adjustThreshold() {
		if (predictions % 10 == 0) { // Every 10 predictions, evaluate performance
			if (adaptiveChangeCounter < -5) { // Too conservative, decrease threshold
				changeValue = Math.max(changeValue - 1, 0);
			} else if (adaptiveChangeCounter > 5) { // Too aggressive, increase threshold
				changeValue = Math.min(changeValue + 1, (int) Math.pow(2, bits) - 1);
			}
			adaptiveChangeCounter = 0; // Reset after adjustment
		}
	}

	public void run() {
		for (String line : lines) {
			if (line.equals("")) {
				continue;
			}

			predictions++;

			String[] addressAndPrediction = line.split(" ");
			String prediction = addressAndPrediction[1];
			String ourPrediction = predictBranch();

			if (!ourPrediction.equals(prediction)) {
				mispredictions++;
			}

			if (prediction.equals(TAKEN)) {
				incrementCounter();
			} else if (prediction.equals(NOT_TAKEN)) {
				decrementCounter();
			}

			adjustThreshold();

			if (differentialCounter > 3) {
				differentialCounter = 3;
			} else if (differentialCounter < -4) {
				differentialCounter = -4;
			}

			counter += differentialCounter;
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
