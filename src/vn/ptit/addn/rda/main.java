package vn.ptit.addn.rda;

import org.opencv.core.Core;

public class main {

	// private static final String PATH_RAW_DATA = "E:/Radar data/data";
	private static final String PATH_RAW_DATA = "D:/Radar data/data";
	public static final String PATH_DATA = "data/radarData.log";

	private static final double[] valueObject = { 0, 0, 255 };

	public static void main(String[] args) {
		// FilterData filterData = new FilterData(PATH_RAW_DATA);
		// filterData.filterRawData();

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// ProcessingData processingData = new ProcessingData(PATH_DATA);
		// processingData.readData();

		ImageProcess imageProcess = new ImageProcess();
		// imageProcess.filterProcess();
		// imageProcess.medianBackground();
		imageProcess.process();
		// imageProcess.staticObjectProcess();

		// System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		// Mat mat = new Mat(100, 100, CvType.CV_8UC3);
		// for (int i = 0; i < mat.rows(); i++) {
		// for (int j = 0; j < mat.cols(); j++) {
		// mat.put(i, j, valueObject);
		// }
		// }
		// Highgui.imwrite("data/demo.jpg", mat);

	}

}
