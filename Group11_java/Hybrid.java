import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Hybrid {
	private GShare bimodal;
	private GShare gshare;
	private String fileName;
	private int k;
	private int[] chooserTable;
	private int predictions;
	private int mispredictions;
	private String[] lines;

	public Hybrid(int k, int m1, int n, int m2, String file) {
		try {
			bimodal = new GShare(m2, 0, file);
			gshare = new GShare(m1, n, file);
			fileName = new File(file).getName();

			this.k = k;
			chooserTable = new int[(int) Math.pow(2, k)];

			for (int i = 0; i < chooserTable.length; i++) {
				chooserTable[i] = 1;
			}

			predictions = 0;
			mispredictions = 0;

			StringBuilder data = new StringBuilder();
			File fileObj = new File(file);
			Scanner scanner = new Scanner(fileObj);
			while (scanner.hasNextLine()) {
				data.append(scanner.nextLine()).append("\n");
			}
			scanner.close();
			lines = data.toString().split("\n");
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + file);
		}
	}

	public String[] getGSharePrediction(String address, String prediction) {
		int index = gshare.getIndex(address);
		gshare.updateGlobalHistory(prediction);

		return new String[] { gshare.predictBranch(index), String.valueOf(index) };
	}

	public String[] getBimodalPrediction(String address, String prediction) {
		int index = bimodal.getIndex(address);

		return new String[] { bimodal.predictBranch(index), String.valueOf(index) };
	}

	public int getIndex(String address) {
		String binaryAddress = Integer.toBinaryString(Integer.parseInt(address, 16));
		String indexAddress = binaryAddress.substring(binaryAddress.length() - k - 2, binaryAddress.length() - 2);
		return Integer.parseInt(indexAddress, 2);
	}

	public void prediction(String address, String prediction) {
		int index = getIndex(address);
		int chooserValue = chooserTable[index];

		String[] gsharePrediction = getGSharePrediction(address, prediction);
		String[] bimodalPrediction = getBimodalPrediction(address, prediction);

		String ourPrediction;
		if (chooserValue >= 2) {
			ourPrediction = gsharePrediction[0];
			gshare.updatePredictionTable(Integer.parseInt(gsharePrediction[1]), prediction);
		} else {
			ourPrediction = bimodalPrediction[0];
			bimodal.updatePredictionTable(Integer.parseInt(bimodalPrediction[1]), prediction);
		}

		if (!ourPrediction.equals(prediction)) {
			mispredictions++;
		}

		if (gsharePrediction[0].equals(prediction) == bimodalPrediction[0].equals(prediction)
				|| (gsharePrediction[0].equals(bimodalPrediction[0]) && !gsharePrediction[0].equals(prediction))) {
			chooserTable[index] = chooserTable[index];
		} else if (gsharePrediction[0].equals(prediction) && !bimodalPrediction[0].equals(prediction)) {
			chooserTable[index] = Math.min(chooserTable[index] + 1, 3);
		} else if (!gsharePrediction[0].equals(prediction) && bimodalPrediction[0].equals(prediction)) {
			chooserTable[index] = Math.max(chooserTable[index] - 1, 0);
		}
	}

	public void run() {
		for (String line : lines) {
			if (line.isEmpty()) {
				continue;
			}

			predictions++;

			String[] addressAndPrediction = line.split(" ");
			String address = addressAndPrediction[0];
			String prediction = addressAndPrediction[1];

			prediction(address, prediction);
		}
	}

	public void printContents() {
		System.out.println("FINAL CHOOSER CONTENTS");
		for (int i = 0; i < chooserTable.length; i++) {
			System.out.println(i + "\t" + chooserTable[i]);
		}
	}

	public void printResults() {
		System.out.println("COMMAND");
		System.out.println("./sim hybrid " + k + " " + gshare.m + " " + gshare.n + " " + bimodal.m + " " + fileName);
		System.out.println("OUTPUT");
		System.out.println("number of predictions:   \t " + predictions);
		System.out.println("number of mispredictions:\t " + mispredictions);
		// System.out.println("misprediction rate: \t" + ((double) mispredictions /
		// predictions * 100) + "%");
		System.out.println("misprediction rate:      \t"
				+ String.format("%.2f", ((double) mispredictions / predictions * 100)) + "%");

		printContents();
		gshare.printContents();
		bimodal.printContents();
	}

}
