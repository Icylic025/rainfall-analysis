/******************************************
 * Author: Kylie Wang
 * Date: 2023/04/12
 * Purpose: Reads weather data from spreadsheets for Victoria
 * and Gon and analyzes it to plot the maximum monthly rainfall
 * and maximum rainfall over a 2-day period for each year from 
 * 1995 to 2023. It also calculates the number of days per year
 * with heavy rain and finds the top ten monthly rainfall amounts
 * for each of the two categories. The main purpose of this code
 * is to provide insights into the historical rainfall patterns 
 * in the area and identify trends or anomalies in the data. 
 * ****************************************/

import ptolemy.plot.Plot;
import ptolemy.plot.PlotApplication;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
	public static final double Xmin = 1995;
	public static final double Xmax = 2023; // Graph domain
	public static final int Npoint = 348;

	public static void main(String[] args) throws FileNotFoundException {
		ArrayList<double[][]> vic = new ArrayList<double[][]>(); // contains a list of int[][] that stores the data for
																	// Vic Station
		ArrayList<double[][]> gon = new ArrayList<double[][]>(); // contains a list of int[][] that stores the data for
																	// Gon Station
		BufferedReader readerGon = null;
		BufferedReader readerVic = null;
		double[] maxMonth = null;
		double[] maxTwoDays = null;
		int year = 1995;
		int[] heavyRain = null;

		// reads in all data from spreadsheets and sorts and stores in 2 array lists
		do {
			readerGon = new BufferedReader(
					new FileReader("src/weather/en_climate_daily_BC_1018611_" + year + "_P1D.csv"));
			readerVic = new BufferedReader(
					new FileReader("src/weather/en_climate_daily_BC_1018598_" + year + "_P1D.csv"));
			vic.add(makeDataArr(year, readerVic));
			gon.add(makeDataArr(year, readerGon));
			year++;
		} while (year <= 2023);

		maxMonth = maxMonthData(vic, gon);
		maxTwoDays = maxTwoDays(vic, gon);

		// plots
		plot(maxMonth, "Maximum Monthly Rainfall", "Year", "Amount (mm)");
		plot(maxTwoDays, "Maximum Monthly Rainfall Over a 2-Day Period", "Year", "Amount (mm)");
		;

		// find and print top data
		String[] topM = topTenMonth(maxMonth);
		String[] topT = topTenMonth(maxTwoDays);
		System.out.println("Top 10 Monthly Rainfall");
		for (int i = 0; i < topM.length; i++) {
			System.out.println(topM[i]);
		}
		System.out.println();

		System.out.println("Top 10 Monthly Rainfall Over a 2-Day Period");
		for (int i = 0; i < topM.length; i++) {
			System.out.println(topT[i]);
		}

		System.out.println("");
		heavyRain = heavyRain(vic, gon);
		System.out.println("Number of Heavy Rain Days Every Year");
		for (int i = 0; i < heavyRain.length; i++) {
			System.out.println(1995 + i + " " + heavyRain[i]);
		}
	}

	// Takes in points and plot accordingly
	public static void plot(double[] points, String title, String xLabel, String yLabel) {
		int counter = 0;
		Plot p = new Plot(); // Create Plot object
		p.setTitle(title);
		p.setXLabel(xLabel);
		p.setYLabel(yLabel);
		double xStep = (Xmax - Xmin) / Npoint;

		// Plotting loop
		for (double x = Xmin; x <= Xmax; x += xStep) {
			if (counter == 348) {
				break;
			}
			double y = points[counter];
			p.addPoint(0, x, y, true);
			counter++;
		}

		PlotApplication app = new PlotApplication(p); // Display

	}

	// finds the top ten greatest points and calculates the date they are from
	public static String[] topTenMonth(double[] data) {
		String[] ten = new String[10];
		int counter = 0;
		int index = 0;
		double[] sort = new double[data.length];
		String[] str = new String[data.length];

		// makes a copy of data to be sorted without deleting previous
		for (int i = 0; i < data.length; i++) {
			sort[i] = data[i];
			str[i] = Double.toString(data[i]);
		}
		Arrays.sort(sort);

		// cycles through the last ten elements of the sorted array
		for (int i = sort.length - 1; i >= sort.length - 10; i--) {
			// finds the index of the 10 max data in the original array for the date
			for (int j = 0; j < data.length; j++) {
				if (data[j] == sort[i]) {
					index = j;
					break;
				}
			}
			String year = Integer.toString(1995 + index / 12);
			String month = Integer.toString(index % 12 + 1);
			String date = year + "/" + month;
			ten[counter] = date + " " + sort[i] + "mm";
			if (counter < 10) {
				counter++;
			}
		}
		return ten;
	}

	// calculates number of days per year that has heavy rain (<20mm)
	public static int[] heavyRain(ArrayList<double[][]> vic, ArrayList<double[][]> gon) {
		int[] data = new int[29];
		double[][] vYear = null; // one year of data as a 2d array for Victoria station
		double[][] gYear = null;

		double[] vMonth = null;
		double[] gMonth = null;

		// cycles through years (1995 - 2023) is 29 years
		for (int i = 0; i < 29; i++) {
			int counter = 0;
			vYear = (double[][]) vic.get(i); // stores a year of data from UVic station each time it cycles (variable
												// changes each cycle)
			gYear = (double[][]) gon.get(i); // same thing as vYear but for data from Gon station

			// breaks down data further by cycling through the double[][] storing data from
			// each year and stores data from each month in a double[]
			for (int j = 0; j < 12; j++) {
				vMonth = vYear[j];
				gMonth = gYear[j];

				// breaks down data further by cycling through the double [] storing data from
				// each month and makes calculations with data from each day
				for (int k = 0; k < 31; k++) {

					// if the data in that day is -2 , it means that it doesn't not exist and is
					// skipped (Ex Feb 31st)
					if (vMonth[k] == -2 && gMonth[k] == -2) {
						break;
					} else {
						if (vMonth[k] > 20 || gMonth[k] > 20) {
							counter++;
						}
					}
				}
			}
			data[i] = counter;
		}
		return data;
	}

	// finds the total rainfall per year and returns as a double array with 348
	// elements
	public static double[] maxMonthData(ArrayList<double[][]> vic, ArrayList<double[][]> gon) {
		int counter = 0;
		double[] data = new double[348]; // final data storage of max rainfall every month in all the years, to be
											// returned
		double[][] vYear = null;
		double[][] gYear = null;

		double[] vMonth = null;
		double[] gMonth = null;

		double total = 0;

		// cycles through years (1995 - 2023) is 29 years
		for (int i = 0; i < 29; i++) {
			vYear = (double[][]) vic.get(i); // stores a year of data from UVic station each time it cycles
			gYear = (double[][]) gon.get(i); // same thing as vYear but for data from Gon station

			// breaks down data further by cycling through the double[][] storing data from
			// each year and stores data from each month in a double[]
			for (int j = 0; j < 12; j++) {
				total = 0;
				vMonth = vYear[j];
				gMonth = gYear[j];

				// breaks down data further by cycling through the double [] storing data from
				// each month and makes calculations with data from each day
				for (int k = 0; k < 31; k++) {

					// if the data in that day is -2 , it means that it doesn't not exist and is
					// skipped (Ex Feb 31st)
					if (vMonth[k] == -2 && gMonth[k] == -2) {
						break;
					} else {

						// total rainfall of every day in each month
						// if data in that day is -1, it means there is no data from this station at
						// that day
						// use the data from the other station
						// if both stations have data, store average
						if (vMonth[k] == -1 && gMonth[k] == -1) {
							total += 0;
						} else if (vMonth[k] == -1) {
							total += gMonth[k];
						} else if (gMonth[k] == -1) {
							total += vMonth[k];
						} else {
							total += (vMonth[k] + gMonth[k]) / 2;
						}
						data[counter] = total;
						// make a 2d array of date and data, go through all data find smallest one,
						// if current data is bigger than that, replace it with current data
					}

				}
				// keep track of when data runs out
				counter++;
			}
		}
		return data;
	}

	// Look at both stations and find max two day rainfall from both stations
	// If both stations have data, use the average
	// If only one station has data, use that station
	// skip if neither has data
	public static double[] maxTwoDays(ArrayList<double[][]> vic, ArrayList<double[][]> gon) {
		double[] data = new double[348]; // stores final data and is returned to be plotted
		int counter = 0; // keeps track of the index of data
		double[][] vYear = null; // one year of data as a 2d array for Victoria station
		double[][] gYear = null;

		double[] vMonth = null;
		double[] gMonth = null;
		double max = 0;

		// cycles through the 29 years
		for (int i = 0; i < 29; i++) {
			// breaks if there is no data to be stored
			if (counter == 348) {
				break;
			}
			vYear = vic.get(i);
			gYear = gon.get(i);
			// cycles through the months of each data
			for (int j = 0; j < 12; j++) {
				vMonth = vYear[j];
				gMonth = gYear[j];
				double[] temp = new double[31];

				// cycles through days
				for (int k = 0; k < 31; k++) {
					if (counter == 348) {
						break;
					}

					// break if date doesn't exist
					if (vMonth[k] == -2 && gMonth[k] == -2) {
						break;
					} else {

						// find correct data depending on if data exists
						if (vMonth[k] == -1) {
							temp[k] = gMonth[k];
						} else if (gMonth[k] == -1) {
							temp[k] = vMonth[k];
						} else {
							temp[k] = (vMonth[k] + gMonth[k]) / 2;
						}
					}
				}

				// calculate the max two day
				for (int t = 0; t < 30; t++) {
					if (max < temp[t] + temp[t + 1]) {
						max = temp[t] + temp[t + 1];
					}
				}
				data[counter] = max;
				counter++;
				max = 0;
			}
		}
		return data;
	}

	// read and sort the data into appropriate arrays
	// in arr, 0.0 means 0 is recorded in the data, -1 means the cell is empty (""),
	// -2 means the date doesn't exist (Feb 31st)
	public static double[][] makeDataArr(int year, BufferedReader in) throws FileNotFoundException {
		String[] lines = in.lines().toArray(String[]::new);
		double[][] arr = new double[12][31];
		int lineCounter = 1;
		int counter = 23;
		String[] temp = null;
		String word = "";

		// initializes all elements as -2 and the dates that exist will be changed later
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				arr[i][j] = -2;
			}
		}
		
		// for leap years since there is more lines of data in the spreadsheet
		if (isLeap(year)) {

			// cycles through all 12 months in a year
			for (int i = 0; i < 12; i++) {
				for (int j = 0; j < 31; j++) {
					if (lineCounter > 366) {
						break;
					}
					// if month has 30 days break early so no data for 31 (index + 1 is month)
					if (i == 3 || i == 5 || i == 8 || i == 10) {
						if (j == 30) {
							break;
						}
					} else if (i == 1) { // leap year
						if (j == 29) {
							break;
						}
					}
					// splits data accordingly
					temp = lines[lineCounter].split(",");
					word = temp[counter].replaceAll("\"", "");
					// stores -1 if no data is offered by that station
					if (word.equals("")) {
						arr[i][j] = -1;
					} else {
						arr[i][j] = Double.parseDouble(word);
					}
					lineCounter++;
				}
			}
			return arr;
			
			// same thing but for non leap years
		} else {
			for (int i = 0; i < 12; i++) {
				for (int j = 0; j < 31; j++) {
					if (lineCounter > 365) {
						break;
					}
					if (i == 3 || i == 5 || i == 8 || i == 10) {
						if (j == 30) {
							break;
						}
					} else if (i == 1) {
						if (j == 28) {
							break;
						}
					}
					temp = lines[lineCounter].split("\",\"");
					word = temp[counter];
					if (word.equals("")) {
						arr[i][j] = -1;
					} else {
						arr[i][j] = Double.parseDouble(word);
					}
					lineCounter++;
				}
			}
			return arr;
		}
	}

	// isLeap
	// calculates if it is a leap year which will have an extra line of
	// data to read from the spreadsheet
	// --------------------------
	public static boolean isLeap(int year) {
		return (year % 4 == 0 && year % 100 != 0 || year % 400 == 0);
	}// ifLeap

}