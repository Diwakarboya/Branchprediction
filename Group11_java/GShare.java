import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class GShare {
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

	public GShare(int m, int n, String file) {
		this.m = m;
		this.n = n;
		this.file_name = file;

		this.prediction_table = new int[(int) Math.pow(2, m)];
		Arrays.fill(this.prediction_table, 4);

		this.global_history = "0".repeat(n);

		this.change_value = 4;
		this.predictions = 0;
		this.mispredictions = 0;

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

	public int getIndex(String address) {
		String binary_address = Integer.toBinaryString(Integer.parseInt(address, 16));
		String index_address = binary_address.substring(binary_address.length() - m - 2, binary_address.length() - 2);

		String final_address;
		if (n == 0) {
			// Bimodal
			final_address = index_address;
		} else {
			// Gshare
			String n_bit_address = index_address.substring(index_address.length() - n);
			int xor_n_bit = Integer.parseInt(n_bit_address, 2) ^ Integer.parseInt(global_history, 2);

			final_address = index_address.substring(0, m - n)
					+ String.format("%" + n + "s", Integer.toBinaryString(xor_n_bit)).replace(' ', '0');
		}
		return Integer.parseInt(final_address, 2);
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
		printContents();
	}

}