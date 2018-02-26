package com.habitus.smartsit.postureRecognize;

public final class StandardPosture {

	private static StandardPosture instance = null;
	private float[][] standardMat;

	public static StandardPosture getInstance() {
		if(instance == null) {
			instance = new StandardPosture();
		}

		return instance;
	}

	private StandardPosture() {
		standardMat = new float[DefaultValues.getM()][DefaultValues.getN()];

		for(int i = 0; i < standardMat.length; i++) {
			for(int j = 0; j < standardMat[0].length; j++) {
				standardMat[i][j] = 0;
			}
		}
	}

	public float[][] getStandardMat() {
		return standardMat;
	}

	public void setStandardMat(float[][] standardMat) {
		this.standardMat = standardMat;
	}
}
