package de.heuboe.tls.iface.lib;


/**
 * A generic pair class
 * @param <F> The class of the first element
 * @param <S> The class of the second element
 */
public class Pair<F, S> {
    private F first;
    private S second;
    
    /**
     * Constructor taking the elements of the pair
     * @param first  first element of pair
     * @param second second element of pair
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }
    
    public F getFirst() {
        return first;
    }
    
    public void setFirst(F first) {
        this.first = first;
    }
    
    public S getSecond() {
        return second;
    }
    
    public void setSecond(S second) {
        this.second = second;
    }
    
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}