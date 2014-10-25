package sinetja;

/** Functional interface for Java 8. */
public interface ErrorHandler {
  public void run(Request request, Response response, Exception e) throws Exception;
}
