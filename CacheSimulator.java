import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class CacheSimulator {
    private int l1sets;
    private int l2sets;
    private int blockSize;
    private int l1Assoc;
    private int l2Assoc;
    private int inclusionProperty;
    private String traceFile;
    private int l1_reads;
    private int l1_reads_miss;
    private int l1_writes;
    private int l1_writes_miss;
    private int l1_write_backs;

    private int l2_reads;
    private int l2_reads_miss;
    private int l2_writes;
    private int l2_writes_miss;
    private int l2_write_backs;
    private int memory_traffic;
    public CacheEntry[][] l1cache;
    public CacheEntry[][] l2cache;
    public ArrayList<Queue<Integer>> l1Queue;
    public ArrayList<Queue<Integer>> l2Queue;
    private boolean isTwolevels;
    private ArrayList<String> commands;
    Utills utills = new Utills();
    private StringBuilder configBuilder;
    private String replacementPolicy;
    private ArrayList<HashMap<Integer, Integer>> l2map;
    ArrayList<HashMap<Integer, Integer>> l1map;
    public CacheSimulator(int l1sets, int l2sets, int blockSize, int l1Assoc, int l2Assoc, int inclusionProperty, String trace_file, StringBuilder configBuilder, String replacementPolicy) {
        this.l1sets = l1sets;
        this.l2sets = l2sets;
        this.blockSize = blockSize;
        this.l1Assoc = l1Assoc;
        this.l2Assoc = l2Assoc;
        this.inclusionProperty = inclusionProperty;
        this.traceFile = trace_file;
        this.configBuilder = configBuilder;
        this.replacementPolicy = replacementPolicy;
        this.l1_writes = 0;
        this.l1_writes_miss = 0;
        this.l1_write_backs = 0;
        this.l1_reads = 0;
        this.l1_reads_miss = 0;
        this.l2_writes = 0;
        this.l2_writes_miss = 0;
        this.l2_write_backs = 0;
        this.l2_reads = 0;
        this.l2_reads_miss = 0;
        this.memory_traffic = 0;
        this.isTwolevels = false;
        this.commands = null;
    }

    public void run(){
        isTwolevels = (l2sets == 0) ? false : true;
        //reading the trace file and adding commands into arralist
//        String filePath = "/Users/suryasundari/IdeaProjects/Machineproblem/src/main/resources/traces/" + traceFile;
        String filePath =  traceFile;
        commands = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                commands.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!isTwolevels){
            implementlevel1Cache();
        }else {
            if (inclusionProperty == 0)
                implement2LevelCacheNon();
        }
    }

    public void implement2LevelCacheNon(){

    }

    //This implement function is used to implement single level chache 

    public void implementlevel1Cache(){
        // I am getting commands by reading the file and adding it to arraylist
        int n = commands.size();
        l1Queue = new ArrayList<>();
        for (int k = 0; k < l1sets; k++) {
            Queue<Integer> emptyQueue = new LinkedList<>();
            l1Queue.add(emptyQueue);
        }
        l1cache = new CacheEntry[l1sets][l1Assoc];
        for (int i=0;i<l1sets;i++)
            for (int j =0;j<l1Assoc;j++)
                l1cache[i][j] = null;
        
        // Iterating each command here
        for(String coms : commands){
            String[] c = coms.split(" ");
            String cmd = c[0].trim();
            String add = c[1].trim();
            int number = Integer.parseInt(add, 16);
            //Calculating address, taglength, indexlength, tag, index
            String binaryAddress = utills.getBinary(number);
            int index_length = (int) (Math.log(l1sets) / Math.log(2));
            int block_size_length = (int) (Math.log(blockSize) / Math.log(2));
            int tag_length = 32 - index_length - block_size_length;
            int tag = utills.getSubString(0, tag_length, binaryAddress);
            int index = utills.getSubString(tag_length, tag_length + index_length, binaryAddress);
            //If the command is read performing read operation in cache else perfroming write operation cache
            if (cmd.equals("r")) {
                l1_reads++;
                for (int k = 0; k < l1Assoc; k++) {
                    //If it is empty adding it to cache. Else checking if it is there and updating LRU if it is LRU 
                    if (l1cache[index][k] == null) {
                        l1_reads_miss++;
                        addBlockInCache(number, tag, index, false, k, true);
                        break;
                    } else {
                        CacheEntry cacheBlock = l1cache[index][k];
                        if (tag == cacheBlock.tag){
                            if(replacementPolicy.equals("LRU")){
                                Queue<Integer> q = l1Queue.get(index);
                                q.remove(k);
                                q.add(k);
                                l1Queue.set(index, q);
                            }
                            break;
                        }
                        // If the cache set is full removing the block and adding the new one
                        if (tag != cacheBlock.tag && k == l1Assoc - 1) {
                            l1_reads_miss++;
                            evictBlockInCache(number, tag, index, false, true);
                        }
                    }
                }
            } else {
                l1_writes++;
                for (int k = 0; k < l1Assoc; k++) {
                    if (l1cache[index][k] == null) {
                        l1_writes_miss++;
                        addBlockInCache(number, tag, index, true, k, true);
                        break;
                    } else {
                        CacheEntry cacheBlock = l1cache[index][k];
                        if (tag == cacheBlock.tag){
                            if(!cacheBlock.isDirty){
                                l1cache[index][k].isDirty = true;
                            }
                            if(replacementPolicy.equals("LRU")){
                                Queue<Integer> q = l1Queue.get(index);
                                q.remove(k);
                                q.add(k);
                                l1Queue.set(index, q);
                            }
                            break;
                        }
                        if (tag != cacheBlock.tag && k == l1Assoc - 1) {
                            l1_writes_miss++;
                            evictBlockInCache(number, tag, index, true, true);
                        }
                    }
                }
            }
        }
        printL1Contents();
        printFinalResults();
    }


    //Not using this method any where
    public void evicblocksInmultiplelevels(int number, int index, int tag, String cmd, HashMap<Integer, Integer> l1, HashMap<Integer, Integer> l2){
        Queue<Integer> q = l1Queue.get(index);
        int updateCol = q.poll();
        if (l1cache[index][updateCol].isDirty) {
            l1_write_backs++;
            Queue<Integer> q2 = l2Queue.get(l1cache[index][updateCol].index);
            int updateCol2 = q2.poll();
            if (l2cache[index][updateCol2].isDirty) {
                l2_write_backs++;
            }
            l2.remove(l1cache[index][updateCol2].tag);
            l2.put(l1cache[index][updateCol].tag, updateCol);
            l2map.set(index, l2);
            l2cache[index][updateCol2] = new CacheEntry(l1cache[index][updateCol].address, l1cache[index][updateCol].tag, l1cache[index][updateCol].index, cmd.equals("w"));
            q2.add(updateCol);
            l2Queue.set(index, q2);
        }
        l1.remove(l1cache[index][updateCol].tag);
        l1.put(tag, updateCol);
        l1map.set(index, l1);
        l1cache[index][updateCol] = new CacheEntry(number, tag, index, (cmd.equals("w")));
        q.add(updateCol);
        l1Queue.set(index, q);
    }

    // This will add block in cache
    public void addBlockInCache(int number, int tag, int index, boolean isDirty, int col, boolean isL1){
        if(isL1){
            l1cache[index][col] = new CacheEntry(number, tag, index, isDirty);
            Queue<Integer> q = l1Queue.get(index);
            q.add(col);
            l1Queue.set(index, q);
        }else {
            l2cache[index][col] = new CacheEntry(number, tag, index, isDirty);
            Queue<Integer> q = l2Queue.get(index);
            q.add(col);
            l2Queue.set(index, q);
        }
    }

    // This will remove blcok in cache according to replacement polecy

    public void evictBlockInCache(int number, int tag, int index, boolean isDirty, boolean isL1){
        if(isL1){
            Queue<Integer> q = l1Queue.get(index);
            int updateCol = q.poll();
            if (l1cache[index][updateCol].isDirty) {
                l1_write_backs++;
            }
            l1cache[index][updateCol] = new CacheEntry(number, tag, index, isDirty);
            q.add(updateCol);
            l1Queue.set(index, q);
        }else {
            Queue<Integer> q = l2Queue.get(index);
            int updateCol = q.poll();
            if (l2cache[index][updateCol].isDirty) {
                l2_write_backs++;
            }
            l2cache[index][updateCol] = new CacheEntry(number, tag, index, isDirty);
            q.add(updateCol);
            l2Queue.set(index, q);
        }
    }

    // Printing outputs

    public void printL1Contents(){
        System.out.println("===== L1 contents =====");
//        configBuilder.append("===== L1 contents =====").append("\n");
        for(int row = 0 ; row<l1sets; row++){
            System.out.print("Set     " + row + ":     ");
//            configBuilder.append("Set     " + row + ":     ");
            for (int col =0; col<l1Assoc; col++){
                System.out.print((utills.convertToHex(l1cache[row][col].tag) + " " +((l1cache[row][col].isDirty) ? "D  ":"   ")));
            }
            System.out.println();
//            configBuilder.append("\n");
        }
    }

    public void printL2Contents(){
        configBuilder.append("===== L2 contents =====").append("\n");
        for(int row = 0 ; row<l2sets; row++){
            configBuilder.append("Set     " + row + ":     ");
            for (int col =0; col<l2Assoc; col++){
                configBuilder.append(utills.convertToHex(l2cache[row][col].tag) + " " +((l2cache[row][col].isDirty) ? "D  ":"   "));
            }
            configBuilder.append("\n");
        }
    }

    public void printFinalResults(){
        System.out.println("===== Simulation results (raw) =====");
        System.out.println("a. number of L1 reads:        " + l1_reads);
        System.out.println("b. number of L1 read misses:  " + l1_reads_miss);
        System.out.println("c. number of L1 writes:       " + l1_writes);
        System.out.println("d. number of L1 write misses: " + l1_writes_miss);
        System.out.println("e. L1 miss rate:              " + utills.calculateMissRate(l1_reads_miss, l1_writes_miss, l1_reads, l1_writes));
        System.out.println("f. number of L1 writebacks:   " + l1_write_backs);
        System.out.println("g. number of L2 reads:        " + l2_reads);
        System.out.println("h. number of L2 read misses:  " + l2_reads_miss);
        System.out.println("i. number of L2 writes:       " + l2_writes);
        System.out.println("j. number of L2 write misses: " + l2_writes_miss);
//        float l2missrate = (float) l2_reads_miss / l2_reads;
        System.out.println("k. L2 miss rate:              " + utills.calculateMissRate(l2_reads_miss, l2_writes_miss, l2_reads, l2_writes));
        System.out.println("l. number of L2 writebacks:   " + l2_write_backs);
// System.out.println(l1memtraffic);
        System.out.println("m. total memory traffic:      " + (utills.getMemoryTraffic(l1_reads_miss, l1_writes_miss, l1_write_backs)));

//        configBuilder.append("===== Simulation results (raw) =====").append("\n");
//        configBuilder.append("a. number of L1 reads:        "+l1_reads).append("\n");
//        configBuilder.append("b. number of L1 read misses:  "+l1_reads_miss).append("\n");
//        configBuilder.append("c. number of L1 writes:       "+l1_writes).append("\n");
//        configBuilder.append("d. number of L1 write misses: "+l1_writes_miss).append("\n");
//        configBuilder.append("e. L1 miss rate:              "+utills.calculateMissRate(l1_reads_miss, l1_writes_miss, l1_reads, l1_writes)).append("\n");
//        configBuilder.append("f. number of L1 writebacks:   "+l1_write_backs).append("\n");
//        configBuilder.append("g. number of L2 reads:        "+l2_reads).append("\n");
//        configBuilder.append("h. number of L2 read misses:  "+l2_reads_miss).append("\n");
//        configBuilder.append("i. number of L2 writes:       "+l2_writes).append("\n");
//        configBuilder.append("j. number of L2 write misses: "+l2_writes_miss).append("\n");
//        configBuilder.append("k. L2 miss rate:              "+ utills.calculateMissRate(l2_reads_miss, l2_writes_miss, l2_reads, l2_writes)).append("\n");
//        configBuilder.append("l. number of L2 writebacks:   "+l2_write_backs).append("\n");
////        System.out.println(memory_traffic);
//        configBuilder.append("m. total memory traffic:      "+((!isTwolevels) ? utills.getMemoryTraffic(l1_reads_miss, l1_writes_miss, l1_write_backs) : utills.getMemoryTraffic(l2_reads_miss, l2_writes_miss, l2_write_backs)));
//        String filename = "/Users/suryasundari/IdeaProjects/Machineproblem/src/main/resources/output.txt";
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
//            writer.write(configBuilder.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

}
