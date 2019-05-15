/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.util;

/**
 * Class that represents data packet being received from Pixy
 */
public class BoundingBox implements Comparable<BoundingBox>{
	private final int X;
	private final int Y;
	private final int WIDTH;
	private final int HEIGHT;

	public BoundingBox(int nX, int nY, int nW, int nH) {
		X = nX;
		Y = nY;
		WIDTH = nW;
		HEIGHT = nH;
	}

	public int getArea() {
		return WIDTH * HEIGHT;
	}

	public int getX() {
		return X;
	}

	public int getY() {
		return Y;
	}

	public int getWidth() {
		return WIDTH;
	}

	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public int compareTo(BoundingBox boundingBox) {
		int areaA = getArea(), areaB = boundingBox.getArea();
		return (int)Math.signum(areaA - areaB);
	}
}