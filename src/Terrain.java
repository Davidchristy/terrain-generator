import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

public class Terrain {

	public static void main(String[] args) {
		Random r = new Random();
		int SEED = r.nextInt();

		for (int i = 1; i < 11; i++) {
			SEED = r.nextInt();
			float[][] grid = perlin(1024, (float) 0.65, i, SEED);
			makeGrayScalePicture(grid, "cloud "+i);
			makeColoredMap(grid, "map "+i);
		
		}

//		float[][] grid = perlin(1024, (float) 0.55, 10, SEED);
	}



	private static float[][] perlin(int size, float persistence, int octaves,
			int SEED) {
		float[][] finalGrid = new float[size][size];

		for (int octave = 1; octave <= octaves; octave++) {
			int temp = (int) Math.round(Math.pow(2, octave));
			float[][] subgrid = makeNoise(temp, SEED);
			// I need to grow the grid more slowly
			float[][] grid = growGrid(subgrid, size);

			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid.length; j++) {
					float tempFloat = (float) (grid[i][j] * Math.pow(
							persistence, octave));
					finalGrid[i][j] += tempFloat;
				}
			}
		}

		scaleGrid(finalGrid);

		return finalGrid;

	}

	private static void scaleGrid(float[][] finalGrid) {
		float high = -1, low = Float.MAX_VALUE;
		for (int i = 0; i < finalGrid.length; i++) {
			for (int j = 0; j < finalGrid[0].length; j++) {
				if (finalGrid[i][j] < low) {
					low = finalGrid[i][j];
				}
				if (finalGrid[i][j] > high) {
					high = finalGrid[i][j];
				}
			}
		}

		for (int i = 0; i < finalGrid.length; i++) {
			for (int j = 0; j < finalGrid.length; j++) {
				float temp = finalGrid[i][j] - low;
				temp = temp / (high - low);
				finalGrid[i][j] = temp;
			}
		}

	}

	private static float[][] growGrid(float[][] subgrid, Integer size) {
		float[][] tempGrid = subgrid;
		while (tempGrid.length < size) {
			tempGrid = growGridOnce(tempGrid, tempGrid.length*2);			
		}
		return tempGrid;
	}

	/**
	 * @param subgrid
	 * @param size
	 * @return
	 */
	private static float[][] growGridOnce(float[][] subgrid, Integer size) {
		float[][] grid = new float[size][size];

		for (Integer i = 0; i < grid.length; i++) {
			for (Integer j = 0; j < grid[0].length; j++) {
				int iLocation = Math.round((i.floatValue() / (size - 1))
						* (subgrid.length - 1));
				int jLocation = Math.round((j.floatValue() / (size - 1))
						* (subgrid[0].length - 1));

				float north = -1;
				float east = -1;
				float west = -1;
				float south = -1;
				float current = -1;
				// setting the spaces aruond if avilable
				if (iLocation - 1 >= 0) {
					north = subgrid[iLocation - 1][jLocation];
				}
				if (iLocation + 1 < subgrid.length) {
					south = subgrid[iLocation + 1][jLocation];
				}
				if (jLocation - 1 >= 0) {
					west = subgrid[iLocation][jLocation - 1];
				}
				if (jLocation + 1 < subgrid[0].length) {
					east = subgrid[iLocation][jLocation + 1];
				}
				// setting current
				if (north == -1 && west == -1) {
					current = (east + south) / 2;
				} else if (north == -1 && east == -1) {
					current = (west + south) / 2;
				} else if (north == -1) {
					current = (west + south + east) / 3;
				}
				// north is done
				else if (east == -1 && south == -1) {
					current = (west + north) / 2;
				} else if (east == -1) {
					current = (west + north + south) / 3;
				}
				// east is done
				else if (west == -1 && south == -1) {
					current = (east + north) / 2;
				} else if (west == -1) {
					current = (east + north + south) / 3;
				}
				// west is done
				else if (south == -1) {
					current = (west + north + east) / 3;
				} else {
					current = (west + north + east + south) / 4;
				}

				grid[i][j] = current;
				// grid[i][j] = subgrid[iLocation][jLocation];
			}
		}
		return grid;
	}

	private static float[][] makeNoise(int size, int SEED) {
		float[][] grid = new float[size][size];
		Random r = new Random(SEED);

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid.length; j++) {
				grid[i][j] = r.nextFloat();
				// grid[i][j] = 10;
			}
		}

		return grid;
	}

	private static void print(float[][] grid) {
		String output = "";

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				output += "[" + grid[i][j] + "] ";
			}
			output += "\n";
		}
		System.out.print(output + "\n");
	}

	private static void makeGrayScalePicture(float[][] floatGrid, String name) {
		int imgXSize, imgYSize;
		imgXSize = floatGrid.length;
		imgYSize = floatGrid[0].length;
		int[][] grid = new int[imgXSize][imgYSize];
		for (int i = 0; i < imgXSize; i++) {
			for (int j = 0; j < imgYSize; j++) {
				grid[i][j] = Math.round(floatGrid[i][j] * 255);
			}
		}

		for (int i = 0; i < imgXSize; i++) {
			for (int j = 0; j < imgYSize; j++) {
				int color = grid[i][j];
				int r = color, b = color, g = color;
				grid[i][j] = (r << 16) | (g << 8) | b;
			}
		}

		BufferedImage img = new BufferedImage(imgXSize, imgYSize,
				BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < imgXSize; i++) {
			for (int j = 0; j < imgYSize; j++) {
				img.setRGB(i, j, grid[i][j]);
			}
		}

		File f = new File(name + ".png");
		try {
			ImageIO.write(img, "PNG", f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void makeColoredMap(float[][] grid, String name) {
		int imgXSize, imgYSize;
		imgXSize = grid.length;
		imgYSize = grid[0].length;
		
		BufferedImage img = new BufferedImage(imgXSize, imgYSize,
				BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < imgXSize; i++) {
			for (int j = 0; j < imgYSize; j++) {
				
				if(grid[i][j] <0.2){
					//dark blue
					img.setRGB(i, j, 150);
				}
				else if(grid[i][j] <0.3){
					//light blue
					img.setRGB(i, j, 6619135);
				}
				else if(grid[i][j] <0.35){
					//Sand (?)
					img.setRGB(i, j, 14873231);
				}
				else if(grid[i][j] <0.5){
					//grass
					img.setRGB(i, j, 65280);
				}
				else if(grid[i][j] < 0.7){
					//dark green (forest)
					img.setRGB(i, j, 25600);
				}
				else if(grid[i][j] < 0.9){
					//stone
					img.setRGB(i, j, 8026746);
				}
				else if(grid[i][j] >= 0.9){
					//snow
					img.setRGB(i, j,16777215);
				}
				
			}
		}

		File f = new File(name + ".png");
		try {
			ImageIO.write(img, "PNG", f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}
