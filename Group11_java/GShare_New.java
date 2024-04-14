import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class GShare_New {
	public int m;
	public int n;
	private String file_name;
	private int[] prediction_table;
	private String global_history;
	private int change_value;
	private final int saturation_value = 7;
	private int predictions;
	private int mispredictions;
	private String[] lines;
	private final String NOT_TAKEN = "n";
	private final String TAKEN = "t";
	private long prime1;
	private long prime2;
	private long modulo = (1L << 31) - 1;

	public GShare_New(int m, int n, String file) {
		this.m = m;
		this.n = n;
		this.file_name = file;

		this.prediction_table = new int[(int) Math.pow(2, m)];
		Arrays.fill(this.prediction_table, 4);

		this.global_history = "0".repeat(n);

		this.change_value = 4;
		this.predictions = 0;
		this.mispredictions = 0;
		initializePrimes();

		// Read the file
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

	private void initializePrimes() {
		Random rand = new Random();
		prime1 = BigInteger.probablePrime(31, rand).longValue(); // Get a 31-bit prime
		prime2 = BigInteger.probablePrime(31, rand).longValue(); // Get another 31-bit prime
	}

	public int getIndex(String address) {
		int pc = Integer.parseInt(address, 16);
		int historyValue = Integer.parseInt(global_history, 2);

		long hash = (pc * prime1 + historyValue * prime2) % modulo; // Carter-Wegman hashing
		return (int) (hash % (int) Math.pow(2, m)); // Reduce the hash to fit the prediction table size
	}

	public String predictBranch(int index) {
		int index_counter = prediction_table[index];

		// If the counter is >= change_value, then the branch is taken

		if (index_counter >= change_value) {
			return TAKEN;
		} else {
			return NOT_TAKEN;
		}
	}

	public void updatePredictionTable(int index, String prediction) {
		if (prediction.equals(TAKEN)) {
			prediction_table[index] = Math.min(prediction_table[index] + 1, saturation_value);
		} else {
			prediction_table[index] = Math.max(prediction_table[index] - 1, 0);
		}
	}

	public void updateGlobalHistory(String prediction) {
		if (prediction.equals(TAKEN)) {
			if (global_history.length() > 0) {
				global_history = "1" + global_history.substring(0, global_history.length() - 1);
			}
		} else {
			if (global_history.length() > 0) {
				global_history = "0" + global_history.substring(0, global_history.length() - 1);
			}
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

	public void run() {
		for (String line : lines) {
			if (line.equals("")) {
				continue;
			}

			predictions++;

			// Split the line into the address and the prediction
			String[] addressAndPrediction = line.split(" ");
			String address = addressAndPrediction[0];
			String prediction = addressAndPrediction[1];

			// Get the index of the address and get the prediction
			int index = getIndex(address);
			String ourPrediction = predictBranch(index);

			// Compare the prediction to the actual prediction
			increaseMissPrediction(ourPrediction, prediction);

			// Update the prediction table and global history register
			updatePredictionTable(index, prediction);
			updateGlobalHistory(prediction);
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

	public void printResults() {
		System.out.println("COMMAND");
		getCommand();
		System.out.println("OUTPUT");
		System.out.println("number of predictions:   \t " + predictions);
		System.out.println("number of mispredictions:\t " + mispredictions);
		System.out.printf("misprediction rate:      \t%.2f%%\n", getMissPredictionRate());
		// printContents();
	}

}