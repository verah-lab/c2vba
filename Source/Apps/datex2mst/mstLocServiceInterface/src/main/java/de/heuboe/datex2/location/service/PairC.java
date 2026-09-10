package de.heuboe.datex2.location.service;


/**
 * 
 * Vergleichbares Paar.
 * 
 * @author peters
 *
 * @param <T> 		Erster Member
 * @param <U>		Zweiter Member
 */
public class PairC<T extends Comparable<T>, 
                  U extends Comparable<U> > implements Comparable<PairC<T,U>>
{
	T first;
	U second;

	/**
	 * 
	 * Konstruktor.
	 * 
	 * @param first		Erster Member
	 * @param second	Zweiter Member
	 */
	public PairC(T first, U second)
	{
		this.first = first;
		this.second = second;
	}

	public PairC()
	{
		this.first = null;
		this.second = null;
	}	
	
	public T getFirst()				// NOSONAR
	{
		return first;
	}

	public U getSecond()			// NOSONAR
	{
		return second;
	}
	
	@Override
	public int compareTo( PairC<T, U> other )
	{
		int result = first.compareTo( other.first );
		
		if( result != 0 )
		{
			return result;
		}
		
		return second.compareTo( other.second );
	}

    @SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		
		if (obj.getClass() != this.getClass() )
		{ 
			return false;
		}
		
		PairC<T,U> pair = (PairC<T,U>) obj;
		
		return this.getFirst().equals(pair.getFirst()) && 
			   this.getSecond().equals(pair.getSecond());
	}

	@Override
	public int hashCode()
	{
		return first.hashCode() ^ second.hashCode();
	}

	@Override
	public String toString()
	{
		return "Pair(" + first + ", " + second + ")";
	}
}
