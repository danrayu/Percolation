import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.StdStats;

public class PercolationStats {
    private final int trials;
    private final int n;
    private double[] attemptResults;

    private int remainingClosedSitesCount;

    // perform independent trials on an n-by-n grid
    public PercolationStats(int n, int trials) {
        if (n <= 0 || trials <= 0) {
            throw new IllegalArgumentException();
        }

        // Initializing data for opening random sites
        this.n = n;

        // Initializing simulation data
        attemptResults = new double[trials];
        this.trials = trials;
        for (int i = 0; i < trials; i++) {
            attemptResults[i] = runSimulation();
        }
    }

    private double runSimulation() {
        Percolation percolation = new Percolation(n);
        int[] remainingClosedSites;
        int[] closedSiteIndeces;
        remainingClosedSites = new int[n*n];
        closedSiteIndeces = new int[n*n+1];
        setRandomSelectorData(remainingClosedSites, closedSiteIndeces);

        while (!percolation.percolates()) {
            openRandomSite(remainingClosedSites, percolation, closedSiteIndeces);
        }
        double result = (double) (percolation.numberOfOpenSites())/(double) (n*n);
        return result;
    }

//    private void testFullness() {
//        var a1 = coordsFromId(6);
//        var a = percolation.isFull(a1[0], a1[1]);
//        var b1 = coordsFromId(20);
//        var b = percolation.isFull(a1[0], a1[1]);
//        var c1 = coordsFromId(14);
//        var c = percolation.isFull(a1[0], a1[1]);
//        var d1 = coordsFromId(24);
//        var d = percolation.isFull(a1[0], a1[1]);
//        var p = 1;
//    }

    private void setRandomSelectorData(int[] remainingClosedSites, int[] closedSiteIndeces) {
        int gridSiteCount = n*n;
        this.remainingClosedSitesCount = gridSiteCount;
        for (int i = 0; i < gridSiteCount; i++) {
            remainingClosedSites[i] = i + 1;
            closedSiteIndeces[i+1] = i+1;
        }
        closedSiteIndeces[0] = 0;
    }

    private void openRandomSite(int[] remainingClosedSites, Percolation percolation, int[] closedSiteIndeces) {
        int randomSite;
        int randomSiteIndex;
        // StdRandom.uniform can't handle zero delta ranges, so don't run it when only zero remains
        if (remainingClosedSitesCount == 1) {
            randomSiteIndex = 0;
            randomSite = remainingClosedSites[randomSiteIndex];
        }
        else {
            randomSiteIndex = StdRandom.uniformInt(0, remainingClosedSitesCount - 1);
            randomSite = remainingClosedSites[randomSiteIndex];
        }

        int[] coords = coordsFromId(randomSite);
        updateRemainingClosedSites(randomSiteIndex, remainingClosedSites, closedSiteIndeces);
        percolation.open(coords[0], coords[1]);
    }

    // sample mean of percolation threshold
    public double mean() {
        return StdStats.mean(attemptResults);
    }

    // sample standard deviation of percolation threshold
    public double stddev() {
        return StdStats.stddev(attemptResults);
    }

    // low endpoint of 95% confidence interval
    public double confidenceLo() {
        return mean() - 1.96*stddev() / Math.sqrt(trials);
    }

    // high endpoint of 95% confidence interval
    public double confidenceHi() {
        return mean() + 1.96*stddev() / Math.sqrt(trials);
    }

    // update the list of the remaining closed sites to reflect the change.
    private void updateRemainingClosedSites(int openedSiteIndex, int[] remainingClosedSites, int[] closedSiteIndeces) {
        // Replace the 'opened' site with a now innaccessible closed site
        remainingClosedSites[openedSiteIndex] = remainingClosedSites[remainingClosedSitesCount-1];
        // Update the index of the moved site
        closedSiteIndeces[remainingClosedSites[remainingClosedSitesCount-1]] = openedSiteIndex;

        remainingClosedSitesCount--;
    }

    private int[] coordsFromId(int id) {
        int col = id % n;
        int row = id / n+1;
        if (col == 0) {
            return new int[]{row-1, n};
        }
        return new int[]{row, col};
    }

    // test client (see below)
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int trials = Integer.parseInt(args[1]);
        PercolationStats percolationStats = new PercolationStats(n, trials);
        System.out.println("mean                    = " + percolationStats.mean());
        System.out.println("stddev                  = " + percolationStats.stddev());
        System.out.println("95% confidence interval = " + "["+ percolationStats.confidenceLo() + ", " + percolationStats.confidenceHi() + "]");
    }
}
