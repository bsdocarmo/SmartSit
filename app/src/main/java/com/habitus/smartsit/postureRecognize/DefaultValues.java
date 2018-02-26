package com.habitus.smartsit.postureRecognize;

public class DefaultValues {

	private static int M;
	private static int N;
	private static int QTD_SENSORS;
	private static int ERROR = 100000;
	private static int MAX_COLOR = 255;
	private static int MAX_SENSOR_VALUE = 8000000;

	public static int getM() {
		return M;
	}

	public static void setM(int M) {
		DefaultValues.M = M;
	}

	public static int getN() {
		return N;
	}

	public static void setN(int N) {
		DefaultValues.N = N;
	}

	public static int getQTD_SENSORS() {
		setQTD_SENSORS();
		return QTD_SENSORS;
	}

	public static void setQTD_SENSORS() {
		DefaultValues.QTD_SENSORS = M * N;
	}

	public static float getERROR() {
		return ERROR;
	}

	public static void setERROR(int ERROR) {
		DefaultValues.ERROR = ERROR;
	}

	public static int getMAX_COLOR() {
		return MAX_COLOR;
	}

	public static void setMAX_COLOR(int MAX_COLOR) {
		DefaultValues.MAX_COLOR = MAX_COLOR;
	}

	public static int getMAX_SENSOR_VALUE() {
		return MAX_SENSOR_VALUE;
	}

	public static void setMAX_SENSOR_VALUE(int mAX_SENSOR_VALUE) {
		MAX_SENSOR_VALUE = mAX_SENSOR_VALUE;
	}
}
