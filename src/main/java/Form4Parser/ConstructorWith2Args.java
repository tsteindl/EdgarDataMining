package Form4Parser;
@FunctionalInterface
public interface ConstructorWith2Args<T, A, B> {
    T create(A arg1, B arg2);
}
