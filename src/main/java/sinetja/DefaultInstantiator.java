package sinetja;

public class DefaultInstantiator implements Instantiator {
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
