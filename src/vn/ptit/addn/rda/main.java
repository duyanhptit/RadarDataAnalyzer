package vn.ptit.addn.rda;

public class main {

	// private static final String PATH_INPUT = "E:/Radar data/data";
	private static final String PATH_INPUT = "D:/Radar data/data";
	public static final String PATH_OUTPUT = "data/radarData.log";

	public static void main(String[] args) {
		FilterData filterData = new FilterData(PATH_INPUT);
		filterData.filterRawData();
	}

}
