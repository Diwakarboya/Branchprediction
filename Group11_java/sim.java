import java.util.Arrays;

public class sim {

	private static final String SMITH = "smith";
	private static final String BI_MODAL = "bimodal";
	private static final String GSHARE = "gshare";
	private static final String HYBRID = "hybrid";
	private static final String SIMIH_DIFFERENTIAL = "smith_differential";

	public static void simulator(String type, String[] arguments) {
		if (type.equals(SMITH)) {
			Smith smithPredictor = new Smith(Integer.parseInt(arguments[0]), arguments[1]);
			smithPredictor.run();
			smithPredictor.printResults();
		} else if (type.equals(BI_MODAL)) {
			GShare gsharePredictor = new GShare(Integer.parseInt(arguments[0]), 0, arguments[1]);
			gsharePredictor.run();
			gsharePredictor.printResults();
		} else if (type.equals(GSHARE)) {
			GShare gsharePredictor = new GShare(Integer.parseInt(arguments[0]), Integer.parseInt(arguments[1]),
					arguments[2]);
			gsharePredictor.run();
			gsharePredictor.printResults();
		} else if (type.equals(HYBRID)) {
			Hybrid hybridPredictor = new Hybrid(Integer.parseInt(arguments[0]), Integer.parseInt(arguments[1]),
					Integer.parseInt(arguments[2]), Integer.parseInt(arguments[3]), arguments[4]);
			hybridPredictor.run();
			hybridPredictor.printResults();
		} else if (type.equals(SIMIH_DIFFERENTIAL)) {
			Smith_Differential_Compression smithPredictor = new Smith_Differential_Compression(
					Integer.parseInt(arguments[0]), arguments[1]);
			smithPredictor.run();
			smithPredictor.printResults();
		}
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: java BranchPredictorSimulator <type> <args>");
			return;
		}
		if (args.length > 2) {
			String simType = args[0];
			String[] arguments = Arrays.copyOfRange(args, 1, args.length);
			simulator(simType, arguments);
		} else {
			System.out.println("Incorrect input params");
		}
	}
}
