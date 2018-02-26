package com.habitus.smartsit.postureRecognize;

public class CurrentPosture {

	private float[][] currentMat;

	public CurrentPosture() {
		currentMat = new float[DefaultValues.getM()][DefaultValues.getN()];

		for(int i = 0; i < currentMat.length; i++) {
			for(int j = 0; j < currentMat[0].length; j++) {
				currentMat[i][j] = 0;
			}
		}
	}

	public float[][] getCurrentMat() {
		return currentMat;
	}

	public void setCurrentMat(float[][] currentMat) {
		this.currentMat = currentMat;
	}

	public double similarityCalculation() {
		StandardPosture standardPosture = StandardPosture.getInstance();
		float[][] standardMat = standardPosture.getStandardMat();

		double similarityDegree = 0;
		double aux = 0;

		for(int i = 0; i < currentMat.length; i++) {
			for(int j = 0; j < currentMat[0].length; j++) {
				aux += Math.pow((currentMat[i][j] - standardMat[i][j]), 2);
			}
		}
		similarityDegree = Math.sqrt(aux);

		return similarityDegree;
	}

	private boolean similarityCalculation2() {
		StandardPosture standardPosture = StandardPosture.getInstance();
		float[][] standardMat = standardPosture.getStandardMat();

		boolean similar = false;
		int cont = 0;

		for(int i = 0; i < currentMat.length; i++) {
			for(int j = 0; j < currentMat[0].length; j++) {
				if((currentMat[i][j] < (standardMat[i][j] + DefaultValues.getERROR())) && (currentMat[i][j] > (standardMat[i][j] - DefaultValues.getERROR()))) {
					cont++;
				}
			}
		}

		if(cont == DefaultValues.getQTD_SENSORS()) {
			similar = true;
		}

		return similar;
	}

	public int[][][] getCurrentColor() {
		int[][][] colorMat = new int[DefaultValues.getM()][DefaultValues.getN()][3];

		for(int i = 0; i < colorMat.length; i++) {
			for(int j = 0; j < colorMat[0].length; j++) {
				colorMat[i][j][0] = (int)((DefaultValues.getMAX_COLOR() / (double)DefaultValues.getMAX_SENSOR_VALUE()) * currentMat[i][j]);
				colorMat[i][j][2] = DefaultValues.getMAX_COLOR() - (int)((DefaultValues.getMAX_COLOR() / (double)DefaultValues.getMAX_SENSOR_VALUE()) * currentMat[i][j]);

				if(similarityCalculation2()) {
					colorMat[i][j][0] = 0;
					colorMat[i][j][1] = DefaultValues.getMAX_COLOR();
					colorMat[i][j][2] = 0;
				}else {
					colorMat[i][j][1] = 0;
				}
			}
		}

		return colorMat;
	}
}
