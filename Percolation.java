public class Percolation {
    // Dimension of the grid
    private final int n;
    // Data structure representing connections between full sites
    private int[] tree;
    // Sizes of trees for weighting the algorythm
    private int[] sizes;
    private int remainingClosedSitesCount;
    private byte[][] coordMapper;

    // creates n-by-n grid, with all sites initially blocked
    public Percolation(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("Map must be greater than 0");
        }
        this.n = n;
        int gridSiteCount = n*n;
        // There are 2 extra sites - one above and one below. The one above is full by default, the one below is open by default
        this.remainingClosedSitesCount = gridSiteCount;
        this.sizes = new int[gridSiteCount];
        this.tree = new int[gridSiteCount+2];
        initCoordMapArray();

        for (int i = 0; i < gridSiteCount; i++) {
            sizes[i] = 1;
            tree[i+1] = -1;
        }
        tree[0] = 0;
        tree[gridSiteCount+1] = gridSiteCount+1;
    }

    private void initCoordMapArray() {
        coordMapper = new byte[4][];
        coordMapper[0] = new byte[]{1, 0};
        coordMapper[1] = new byte[]{0, 1};
        coordMapper[2] = new byte[]{-1, 0};
        coordMapper[3] = new byte[]{0, -1};
    }

    private int root(int site) {
        while (site != tree[site]) {
            tree[site] = tree[tree[site]];
            site = tree[site];
        }
        return site;
    }

    // is the site (row, col) full?
    public boolean isFull(int row, int col) {
        checkBounds(row, col);
        int id = idFromCoords(row, col);
        if (tree[id] == -1) {
            return false;
        }
        int root = root(id);
        return root == 0;
    }

    private boolean connected(int site1, int site2) {
        int root1 = root(site1);
        int root2 = root(site2);
        return root1 == root2;
    }

    private void union(int site1, int site2) {
        if (!connected(site1, site2)) {
            int root1 = root(site1);
            int root2 = root(site2);
            if (root1 == 0) {
                tree[root2] = root1;
            }
            else if (root2 == 0) {
                tree[root1] = root2;
            }
            else if (root1 == n*n+1)      {
                tree[root1] = root2;
                sizes[root2-1] += 1;
            }
            else if (root2 == n*n+1)      {
                tree[root2] = root1;
                sizes[root1-1] += 1;
            }
            else if (sizes[root1-1] > sizes[root2-1]) {
                tree[root2] = root1;
                sizes[root1-1] += sizes[root2-1];
            }
            else {
                tree[root1] = root2;
                sizes[root2-1] += sizes[root1-1];
            }
        }
    }

    public void open(int row, int col) {
        checkBounds(row, col);
        int siteId = idFromCoords(row, col);
        int[] siteCoords = coordsFromId(siteId);
        tree[siteId] = siteId;

        remainingClosedSitesCount--;

        // connect to top if first line
        if (siteCoords[0] == 1) {
            union(siteId, 0);
        }
        else {
            // search for fulls to connect to
            for (int i = 0; i < 4; i++) {
                int siteStatus = findNeighbour(row + coordMapper[i][0], col + coordMapper[i][1]);
                if (siteStatus == 2) {
                    union(idFromCoords(row + coordMapper[i][0], col + coordMapper[i][1]), siteId);
                    break;
                }
            }
        }
        // connect bottom to self if last line
        if (row == n) {
            union(siteId, n*n+1);
        }
        // At the end, search for any non-full opens to connect to itself
        for (int i = 0; i < 4; i++) {
            int siteStatus = findNeighbour(row + coordMapper[i][0], col + coordMapper[i][1]);
            if (siteStatus == 1) {
                union(idFromCoords(row + coordMapper[i][0], col + coordMapper[i][1]), siteId);
            }
        }
    }

    public boolean percolates() {
        return root(n*n+1) == 0;
    }

    public int numberOfOpenSites() {
        return n*n - remainingClosedSitesCount;
    }

    private int findNeighbour(int row, int col) {
        if (validLocation(row, col)) {
            if (isFull(row, col)) {
                return 2;
            }
            else if (isOpen(row, col)) {
                return 1;
            }
        }
        return 0;
    }

    // is the site (row, col) open?
    public boolean isOpen(int row, int col) {
        checkBounds(row, col);
        int id = idFromCoords(row, col);
        return tree[id] != -1;
    }

    private boolean validLocation(int row, int col) {
        return !(row < 1 || row > n || col < 1 || col > n);
    }

    private void checkBounds(int row, int col) {
        if (row < 1 || row > n || col < 1 || col > n) {
            throw new IllegalArgumentException("row or col out of bounds");
        }
    }

    private int idFromCoords(int row, int col) {
        return (row-1)*n+col;
    }

    private int[] coordsFromId(int id) {
        int col = id % n;
        int row = id / n+1;
        if (col == 0) {
            return new int[]{row-1, n};
        }
        return new int[]{row, col};
    }
}
