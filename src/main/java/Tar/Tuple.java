package Tar;
import org.apache.commons.lang3.builder.*;

class Tuple<K, V>
{
    public K first;
    public V second;

    public Tuple(K first, V second)
    {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
            return true;
        else if (!(o instanceof Tuple))
            return false;
        
        @SuppressWarnings("unckecked")
        Tuple<K, V> other  = (Tuple<K, V>)o;
        
        return new EqualsBuilder()
                .append(first, other.first)
                .append(second, other.second)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(first)
                .append(second)
                .toHashCode();
    }
}