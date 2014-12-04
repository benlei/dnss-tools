import java.util.ArrayList;

class DNT {
  private ArrayList<Header> header;
  private String[][] entries;

  final private int rows;
  final private int cols;

  private int nextRow = 0;
  private int nextCol = 0;

  class Header {
    private String name;
    private Type type;
    private int index;
    public Header(String name, Type type, int index) {
      this.name = name;
      this.type = type;
      this.index = index;
    }

    public String getName() {
      return name;
    }

    public Type getType() {
      return type;
    }

    public int getIndex() {
      return index;
    }
  }


  public DNT(int numEntries, int numCols) {
    rows = numEntries;
    cols = numCols + 1;

    header = new ArrayList<Header>(cols);
    entries = new String[rows][cols];

    // setup first column because it's always _Id
    addHeader("_Id", Type.INT);
  }

  public void addHeader(String name, Type type) {
    if(header.size() == cols) {
      throw new RuntimeException("All headers have been filled.");
    }

    header.add(new Header(name, type, header.size()));
  }

  public void add(String entry) {
    entries[nextRow][nextCol] = entry;

    nextCol++;
    if(nextCol == cols) {
      nextRow++;
      nextCol = 0;
    }
  }

  public void add(boolean val) {
    add(String.valueOf(val));
  }

  public void add(int val) {
    add(String.valueOf(val));
  }

  public void add(float val) {
    add(String.valueOf(val));
  }

  public int getRows() {
    return rows;
  }

  public int getCols() {
    return cols;
  }

  public int size() {
    return nextRow;
  }

  public String[] getRow(int row) {
    return entries[row];
  }

  public ArrayList<Header> getHeader() {
    return header;
  }
}