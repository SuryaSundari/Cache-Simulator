import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class CacheSet {
    public LinkedList<int[]> blocks;
    public Queue<Integer> que;
    public int setAssoc;
    public CacheSet(int setAssoc){
        blocks = new LinkedList<>();
        this.que = new LinkedList<>();
        this.setAssoc = setAssoc;
    }
    public boolean isFull(){
        if (blocks.size() == setAssoc)
            return true;
        return false;
    }
    public boolean isEmpty(){
        if(this.blocks.size() == 0)
            return true;
        return false;
    }
    public void addBlockInCache(int tag, int number, int isDirty){
        this.blocks.add(new int[] {tag, number, isDirty});
        //this.que.add(this.blocks.size() - 1);
    }
    public int getIndexOfcacheBlock(int tag){
        //int r = -1;
        for (int i=0;i<this.blocks.size();i++){
            if(this.blocks.get(i)[0] == tag)
                return i;
        }
        return -1;
    }
    public void upDateQue(int pos){
        this.blocks.add(this.blocks.get(pos));
        this.blocks.remove(pos);
    }
    public void upDateDirtyBlock(int pos){
        this.blocks.get(pos)[1] = 1;
    }
}
