public class CacheEntry {
    public int address;
    public int tag;
    public int index;
    public boolean isDirty;

    public CacheEntry(int address, int tag, int index, boolean isDirty) {
        this.address = address;
        this.tag = tag;
        this.index = index;
        this.isDirty = isDirty;
    }
}
