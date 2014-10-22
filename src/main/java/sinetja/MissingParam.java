package sinetja;

public class MissingParam extends Exception {
  private static final long serialVersionUID = 363262906609529388L;

  private final String param;

  public MissingParam(String param) {
    this.param = param;
  }

  public String param() {
    return param;
  }
}
