package sinetja;

public class Instantiator {
    /**
     * If given a class, returns an instance from the class.
     * Otherwise, simply returns the input.
     */
    public Object instantiate(Object target) {
        if (target instanceof Class) {
            Class klass = (Class) target;
            try {
                return klass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return target;
    }
}
