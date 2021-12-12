package computable;

public interface Computable<A,V> {
    V compute(A args) throws Exception;
}
