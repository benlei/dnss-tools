public enum Type {
  NULL(0),
  STRING(1),
  BOOL(2),
  INT(3),
  FLOAT(4),
  DOUBLE(5); //a double that is single precision isn't a double...

  private byte b;
  Type(int b) {
    this.b = (byte)b;
  }

  public static Type getType(byte b) {
    for(Type t : values()) {
      if(b == t.b) {
        return t;
      }
    }

    return NULL;
  }
}