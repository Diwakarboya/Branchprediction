import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class BiModal_XOR_Folding {
	public int m;
	public int n;
	private String file_name;
	private int[] prediction_table;
	private int change_value;
	private final int saturation_value = 7;
	private int predictions;
	private int mispredictions;
	private String[] fileData;
	private final String NOT_TAKEN = "n";
	private final String TAKEN = "t";

	public BiModal_XOR_Folding(int m, int n, String file) {
		this.m = m;
		this.n = n;
		this.file_name = file;
		this.prediction_table = new int[(int) Math.pow(2, m)];
		Arrays.fill(this.prediction_table, 4);
		this.change_value = 4;
		this.predictions = 0;
		this.mispredictions = 0;
		readTraceFile(file);
	}

	private void readTraceFile(String file) {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			StringBuilder data = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				data.append(line).append("\n");
			}
			this.fileData = data.toString().split("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getIndex(String address) {
		int fullAddress = Integer.parseInt(address, 16);
		int foldedAddress = (fullAddress ^ (fullAddress >>> m)) & ((1 << m) - 1); // XOR folding
		return foldedAddress;
	}

	public String predictBranch(int index) {
		int index_counter = prediction_table[index];
		return index_counter >= change_value ? TAKEN : NOT_TAKEN;
	}

	public void updatePredictionTable(int index, String prediction) {
		if (prediction.equals(TAKEN)) {
			prediction_table[index] = Math.min(prediction_table[index] + 1, saturation_value);
		} else {
			prediction_table[index] = Math.max(prediction_table[index] - 1, 0);
		}
	}

	private void increaseMissPrediction(String ourPrediction, String actualPrediction) {
		if (!ourPrediction.equals(actualPrediction)) {
			mispredictions++;
		}
	}

	private double getMissPredictionRate() {
		return (double) mispredictions / predictions * 100;
	}

	public void makePrediction() {
		for (String line : fileData) {
			predictions++;
			String[] addressAndPrediction = line.split(" ");
			String address = addressAndPrediction[0];
			String prediction = addressAndPrediction[1];

			int index = getIndex(address);
			String ourPrediction = predictBranch(index);

			increaseMissPrediction(ourPrediction, prediction);

			updatePredictionTable(index, prediction);
		}
	}

	public void printContents() {
		if (n == 0) {
			System.out.println("FINAL BIMODAL CONTENTS");
		} else {
			System.out.println("FINAL GSHARE CONTENTS");
		}

		for (int i = 0; i < prediction_table.length; i++) {
			System.out.println(i + "\t" + prediction_table[i]);
		}
	}

	public void getCommand() {
		if (n == 0) {
			System.out.println("./sim bimodal " + m + " " + file_name);
		} else {
			System.out.println("./sim gshare " + m + " " + n + " " + file_name);
		}
	}

	public void displayResults() {
		System.out.println("COMMAND");
		getCommand();
		System.out.println("OUTPUT");
		System.out.println("number of predictions:   \t " + predictions);
		System.out.println("number of mispredictions:\t " + mispredictions);
		System.out.printf("misprediction rate:      \t%.2f%%\n", getMissPredictionRate());
		// printContents();
	}

}
