/*
 * BasicRandom - Implementation of some basic random function to generate workload.
 *
 * Copyright (C) 2003, Raul Barbosa
 * Copyright (C) 2004-2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.random;

import java.util.Random;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class BasicRandom {

	private final char[] aStringChars = {
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
			'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
			'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

	private final String[] cLastTokens = {
			"BAR", "OUGHT", "ABLE", "PRI", "PRES",
			"ESE", "ANTI", "CALLY", "ATION", "EING" };

	private static long nURandCLast;
	private static long nURandCCID;
	private static long nURandCIID;
	private static ZipFianRandom zrnd;
	private Random random;
	
	public BasicRandom() {
		random = new Random(System.nanoTime());
		nURandCLast = nextLong(0, 255);
		nURandCCID = nextLong(0, 1023);
		nURandCIID = nextLong(0, 8191);
	}

	// create the master BasicRandom instance for running a benchmark load
	public BasicRandom(long CLoad) {
		random = new Random(System.nanoTime());
		nURandCCID = nextLong(0, 1023);
		nURandCIID = nextLong(0, 8191);
		long delta;
		do {
			nURandCLast = nextLong(0, 255);
			delta = Math.abs(nURandCLast - CLoad);
			if (delta == 96 || delta == 112 || delta < 65 || delta > 119) {
				continue;
			}
			break;
		} while (true);
	}

	private BasicRandom(BasicRandom parent) {
		random = new Random(System.nanoTime());
	}

	// create a derived random data generator to be used in another thread of the current benchmark load or run process
	public BasicRandom newRandom() {
		return new BasicRandom(this);
	}

	// produce a random int uniformly distributed in [x .. y]
	public int nextInt(int x, int y) {
		return (int) (random.nextDouble() * (y - x + 1) + x);
	}

	// produce a random long uniformly distributed in [x .. y]
	public long nextLong(long x, long y) {
		return (long) (random.nextDouble() * (y - x + 1) + x);
	}

	// produce a random double uniformly distributed in [x .. y]
	public double nextDouble(double x, double y) {
		return random.nextDouble() * (y - x) + x;
	}

	// produce a pair of warehouse id and district id according to the given zipfian distribution
	// distribution in the format of zipfian:^(0|[1-9][0-9]*)$
	public Pair<Integer, Integer> getZipfianWarehouseDistrict(int warehouse, String distribution, int leftBound, int rightBound) throws RuntimeException {
		double zipfian = 0;
		String[] splits = distribution.split(":");
		try {
			zipfian = Double.parseDouble(splits[1]);
		} catch (Exception e) {
			throw new RuntimeException("fail to parse zipfian factor, check current distribution: " + distribution);
		}
		if (zrnd == null) {
			zrnd = new ZipFianRandom(10 * warehouse, zipfian);
		}
		int compound = zrnd.nextValue() - 1;
		return new ImmutablePair<Integer, Integer>(compound / 10 + 1, compound % 10 + 1);
	}

	// produce a uniform random warehouse id
	public int getWarehouseID(int leftBound, int rightBound) {
		return nextInt(leftBound, rightBound);
	}

	// produce a uniform random district id
	public int getDistrictID() {
		return nextInt(1, 10);
	}

	// produce a uniform dynamic district id for each warehouse 
	public int getDynamicDistrictID(int warehouse, HashMap<Integer, Integer> warehouseDistrictNum) throws RuntimeException {
		if (warehouseDistrictNum == null) {
			throw new RuntimeException("hash map from warehouse id to district num not initialized");
		} else {
			return nextInt(1, warehouseDistrictNum.get(warehouse));
		}
	}

	// produce a non uniform random item id
	public int getItemID() {
		return (int) ((((nextLong(0, 8191) | nextLong(1, 100000)) + nURandCIID) % 100000) + 1);
	}

	// produce a non uniform random customer id
	public int getCustomerID() {
		return (int) ((((nextLong(0, 1023) | nextLong(1, 3000)) + nURandCCID) % 3000) + 1);
	}

	// produce a random alphanumeric string of length [x .. y]
	public String getAString(long x, long y) {
		String result = new String();
		long len = nextLong(x, y);
		long have = 1;
		if (y <= 0) {
			return result;
		}
		result += aStringChars[(int) nextLong(0, 51)];
		while (have < len) {
			result += aStringChars[(int) nextLong(0, 61)];
			have++;
		}
		return result;
	}

	// produce a random numeric string of length [x .. y]
	public String getNString(long x, long y) {
		String result = new String();
		long len = nextLong(x, y);
		long have = 0;
		while (have < len) {
			result += (char) (nextLong((long) '0', (long) '9'));
			have++;
		}
		return result;
	}

	// produce the syllable representation for c_last of [0 .. 999]
	public String getCLast(int num) {
		String result = new String();
		for (int i = 0; i < 3; i++) {
			result = cLastTokens[num % 10] + result;
			num /= 10;
		}
		return result;
	}

	// produce a non uniform random customer last name
	public String getCLast() {
		long num;
		num = (((nextLong(0, 255) | nextLong(0, 999)) + nURandCLast) % 1000);
		return getCLast((int) num);
	}

	public String getState() {
		String result = new String();
		result += (char) nextInt((int) 'A', (int) 'Z');
		result += (char) nextInt((int) 'A', (int) 'Z');
		return result;
	}

	public String getCPhone(int w_id, int d_id, int c_id) {
		String strWID = String.format("%8d", w_id).replace(" ", "0");
		String strDID = String.format("%4d", d_id).replace(" ", "0");
		String strCID = String.format("%4d", c_id).replace(" ", "0");
		return strCID + strDID + strWID;
	}

	public long getNURandCLast() {
		return nURandCLast;
	}

	public long getNURandCCID() {
		return nURandCCID;
	}

	public long getNURandCIID() {
		return nURandCIID;
	}
} // end BasicRandom