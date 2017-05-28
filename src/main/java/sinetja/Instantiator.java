package sinetja;

public interface Instantiator {
    /**
     * If given a class, returns an instance from the class.
     * Otherwise, simply returns the input.
     */
    Object instantiate(Object target);
}
