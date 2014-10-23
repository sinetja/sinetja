package sinetja;

/** Functional interface for Java 8. */
public interface Action {
  /**
   * If <code>run</code> throws <code>MissingParam</code> exception when
   * calling <code>request.param</code>, <code>response.respondMissingParam</code>
   * will automatically be called, which by default responds "400 Bad Request".
   *
   * If <code>run</code> throws other exception, <code>response.respondServerError</code>
   * will automatically be called, which by default responds "500 Internal Server Error".
   */
  public void run(Request request, Response response) throws Exception;
}
