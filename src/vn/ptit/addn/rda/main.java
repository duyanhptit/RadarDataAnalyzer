package vn.ptit.addn.rda;

import org.opencv.core.Core;

public class main {

	// private static final String PATH_RAW_DATA = "E:/Radar data/data";
	private static final String PATH_RAW_DATA = "D:/Radar data/data";
	public static final String PATH_DATA = "data/radarData.log";

	public static void main(String[] args) {
		// FilterData filterData = new FilterData(PATH_RAW_DATA);
		// filterData.filterRawData();

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// ProcessingData processingData = new ProcessingData(PATH_DATA);
		// processingData.readData();

		ImageProcess imageProcess = new ImageProcess();
		imageProcess.cannyProcess();
		// imageProcess.medianBackground();
		// imageProcess.process();
		// imageProcess.staticObjectProcess();

		// System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		// Mat gau = Imgproc.getGaussianKernel(5, 0);
		// System.out.println(gau.dump());

	}

}
