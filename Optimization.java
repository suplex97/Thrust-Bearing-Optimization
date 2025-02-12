import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

class CW {
    
    // Calculates the value of the heart equation given coordinates (x, y, z).
    public static double heartEquation(double x, double y, double z) {
        return Math.pow((x * x + 2 * y * y + z * z - 1), 3) - (x * x * z * z * z) - 0.1 * y * y * z * z * z;
    }
    
    // Method to create a 3D grid and fill it with values from the heart equation.
    public static double[][][] create3DGrid(int size) {
        double[][][] grid = new double[size][size][size];
        double scale = 4.0 / size;
        // Fill the 3D grid with values from the heart equation.
        for (int i = 0; i < size; i++) {
            double x = (i - size / 2) * scale;
            for (int j = 0; j < size; j++) {
                double y = (j - size / 2) * scale;
                for (int k = 0; k < size; k++) {
                    double z = (k - size / 2) * scale;
                    grid[i][j][k] = heartEquation(x, y, z);
                }
            }
        }
        return grid;
    }
    
    // Method to perform trilinear interpolation, used to smooth the values in the grid.
    public static double trilinearInterpolation(double[][][] grid, double x, double y, double z, int gridSize) {
        // Calculate indices for interpolation.
        int x0 = (int) Math.floor(x);
        int x1 = Math.min(x0 + 1, gridSize - 1);
        int y0 = (int) Math.floor(y);
        int y1 = Math.min(y0 + 1, gridSize - 1);
        int z0 = (int) Math.floor(z);
        int z1 = Math.min(z0 + 1, gridSize - 1);

        // Calculate the differences for interpolation
        double xd = (x - x0) / (x1 - x0);
        double yd = (y - y0) / (y1 - y0);
        double zd = (z - z0) / (z1 - z0);
        
        // Interpolate along x, y, and z axes to get the interpolated value.
        double c00 = grid[x0][y0][z0] * (1 - xd) + grid[x1][y0][z0] * xd;
        double c01 = grid[x0][y0][z1] * (1 - xd) + grid[x1][y0][z1] * xd;
        double c10 = grid[x0][y1][z0] * (1 - xd) + grid[x1][y1][z0] * xd;
        double c11 = grid[x0][y1][z1] * (1 - xd) + grid[x1][y1][z1] * xd;
        
        // Combine the interpolated values to get the final interpolated value.
        double c0 = c00 * (1 - yd) + c10 * yd;
        double c1 = c01 * (1 - yd) + c11 * yd;

        return c0 * (1 - zd) + c1 * zd;
    }
    
    // Method to generate a 2D slice image from the 3D grid.
    public static BufferedImage generateSliceImage(double[][][] grid, int size, int ySlice, double threshold) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double value = Math.abs(grid[i][ySlice][j]);
                double normalizedValue = Math.min(value / threshold, 1);
                int intensity = (int)((1.0 - normalizedValue) * 255);
                int color = (intensity << 16) | (intensity << 8) | intensity;
                image.setRGB(i, size - j - 1, color);
            }
        }
        return image;
    }
    
    // Method simulates a ray being cast into the volume (3D space) to determine if it hits the surface of the heart.
    public int castRay(double[][][] grid, int gridSize, double rayDirectionX, double rayDirectionY, double rayDirectionZ, double[] lightDirection) {
        double step = 0.1; // Smaller step for more accurate intersection
        double t = 0;
        while (t < gridSize) {
            double x = 0.5 * gridSize + rayDirectionX * t;
            double y = 0.5 * gridSize + rayDirectionY * t;
            double z = 0.5 * gridSize - t; // Assuming camera is looking towards negative z
            
            // If the current point is outside the grid, stop checking this ray.
            if (x < 0 || x >= gridSize || y < 0 || y >= gridSize || z < 0 || z >= gridSize) {
                break; // Out of grid bounds
            }
            
            // Use trilinear interpolation to get a smoother estimate of the heart equation value at this point.
            double value = trilinearInterpolation(grid, x, y, z, gridSize);
            if (value < 0.001) {
                // Calculate the gradient at this point, which tells us the direction in which the surface is pointing.
                double[] normal = computeGradient(grid, x, y, z, gridSize);
                normalize(normal); // Normalizing the gradient vector
                // Calculate how much light this point would receive based on the angle between the light and the surface normal.
                double lightIntensity = Math.max(0, dot(normal, lightDirection));
                int intensity = (int)(255 * lightIntensity);
                return (intensity << 16) | (intensity << 8) | intensity;
            }
            t += step;
        }
        return 0; // Black for no intersection
    }
    // This method calculates the gradient vector at a given point in the grid.
    // The gradient vector points in the direction where the value of the heart equation increases the most rapidly.
    private static double[] computeGradient(double[][][] grid, double x, double y, double z, int gridSize) {
        int ix = (int) x;
        int iy = (int) y;
        int iz = (int) z;
        double gx = 0, gy = 0, gz = 0;
        if (ix > 0 && ix < gridSize - 1 && iy > 0 && iy < gridSize - 1 && iz > 0 && iz < gridSize - 1) {
            gx = (trilinearInterpolation(grid, x + 1, y, z, gridSize) - trilinearInterpolation(grid, x - 1, y, z, gridSize)) / 2;
            gy = (trilinearInterpolation(grid, x, y + 1, z, gridSize) - trilinearInterpolation(grid, x, y - 1, z, gridSize)) / 2;
            gz = (trilinearInterpolation(grid, x, y, z + 1, gridSize) - trilinearInterpolation(grid, x, y, z - 1, gridSize)) / 2;
        }
        return new double[]{gx, gy, gz};
    }
    
    // This method normalizes a vector so that its length is 1.
    // This is useful for converting vectors to a standard size while keeping their direction.
    private static void normalize(double[] vector) {
        double length = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
        vector[0] /= length;
        vector[1] /= length;
        vector[2] /= length;
    }
    
    // This method calculates the dot product of two vectors.
    // The dot product is a measure of how much one vector goes in the same direction as another.
    private static double dot(double[] v1, double[] v2) {
        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
    }
    
    // The 'main' method is where the program starts running.
    // Note on Integration:
    // Gradient shading and trilinear interpolation, crucial for realistic 3D renderings, are currently implemented but not utilized in the main method.
    // Proper integration would involve applying these techniques in the main method to enhance image quality and visual effects, particularly adjusting 'castRay' and 'trilinearInterpolation' usage for pixel color calculation and data smoothing.

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java CW <gridSize> <imageResolution>");
            System.exit(1);
        }
        
        int gridSize = Integer.parseInt(args[0]); // Parse grid size from the first argument
        int imageResolution = Integer.parseInt(args[1]); // Parse image resolution from the second argument
        
        // Proceed with existing code, using gridSize and imageResolution as needed
        double[][][] grid = create3DGrid(gridSize);
    
        // Image resolution refers to the resolution of the 2D slice
        BufferedImage image = generateSliceImage(grid, gridSize, gridSize / 2, 0.01); // Example threshold
        File outputfile = new File("result.tiff");
        try {
            ImageIO.write(image, "tiff", outputfile);
            System.out.println("Output image saved to " + outputfile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error: Unable to save the output image.");
            e.printStackTrace();
        }
    }
    
    
    
    
}

