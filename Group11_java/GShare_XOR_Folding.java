import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class GShare_XOR_Folding {
	public int m; // Number of bits used for indexing the prediction table
	public int n; // Number of bits in the global history register
	private String file_name; // File containing the branch traces
	private int[] prediction_table; // Table storing prediction counters
	private String global_history; // Binary string representing the global history
	private int change_value; // Threshold for deciding taken/not taken
	private final int saturation_value = 7; // Max value for counters in the table
	private int predictions; // Total number of predictions made
	private int mispredictions; // Total number of incorrect predictions
	private String[] lines; // Array of lines read from the trace file
	private final String NOT_TAKEN = "n";
	private final String TAKEN = "t";

	public GShare_XOR_Folding(int m, int n, String file) {
		this.m = m;
		this.n = n;
		this.file_name = file;
		this.prediction_table = new int[(int) Math.pow(2, m)];
		Arrays.fill(this.prediction_table, 4); // Initialize counters to mid-point for two-bit counters
		this.global_history = "0".repeat(n);
		this.change_value = 4; // Mid-point of saturation
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
			this.lines = data.toString().split("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getIndex(String address) {
		int addressInt = Integer.parseInt(address, 16);
		int addressBits = addressInt & ((1 << m) - 1); // Extract lower m bits
		int historyBits = Integer.parseInt(global_history, 2);

		// XOR fold to combine address and history into an m-bit index
		int combinedIndex = addressBits ^ historyBits;
		int foldedIndex = (combinedIndex ^ (combinedIndex >>> n)) & ((1 << m) - 1);
		return foldedIndex;
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

	public void updateGlobalHistory(String prediction) {
		global_history = (prediction.equals(TAKEN) ? "1" : "0") + global_history.substring(0, n - 1);
	}

	private void increaseMissPrediction(String ourPrediction, String actualPrediction) {
		if (!ourPrediction.equals(actualPrediction)) {
			mispredictions++;
		}
	}

	public void run() {
		for (String line : lines) {
			if (line.isEmpty())
				continue;
			String[] addressAndPrediction = line.split(" ");
			String address = addressAndPrediction[0];
			String prediction = addressAndPrediction[1];

			int index = getIndex(address);
			String ourPrediction = predictBranch(index);

			increaseMissPrediction(ourPrediction, prediction);
			updatePredictionTable(index, prediction);
			updateGlobalHistory(prediction);
			predictions++;
		}
	}

	public void printContents() {
		System.out.println("FINAL GSHARE CONTENTS");
		for (int i = 0; i < prediction_table.length; i++) {
			System.out.println(i + "\t" + prediction_table[i]);
		}
	}

	public void printResults() {
		System.out.println("COMMAND");
		System.out.println("./sim gshare " + m + " " + n + " " + file_name);
		System.out.println("OUTPUT");
		System.out.println("number of predictions: \t " + predictions);
		System.out.println("number of mispredictions:\t " + mispredictions);
		System.out.printf("misprediction rate: \t%.2f%%\n", getMissPredictionRate());
		// printContents();
	}

	private double getMissPredictionRate() {
		return (double) mispredictions / predictions * 100;
	}

}
