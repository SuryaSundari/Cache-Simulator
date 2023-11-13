import java.io.*;
import java.lang.ref.PhantomReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Cache2LevelSimulator {
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
    Utills utills = new Utills();
    private StringBuilder configBuilder;
    private String replacementPolicy;
    private CacheSet[] l1;
    private CacheSet[] l2;
    private ArrayList<String> commands;
    private int l1tag;
    private int l2tag;
    private int l1index;
    private int l2index;
    public int l1indexlenght;
    public int l1taglength;
    public int l2indexlenght;
    public int l2taglength;
    public int blocksize;
    public int l1memtraffic;

    public Cache2LevelSimulator(int l1sets, int l2sets, int blockSize, int l1Assoc, int l2Assoc, int inclusionProperty, String trace_file, StringBuilder configBuilder, String replacementPolicy){
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
        this.commands = null;
        l1tag = 0;
        l2tag = 0;
        l1index = 0;
        l2index = 0;
        l1taglength = 0;
        l1indexlenght = 0;
        l2taglength = 0;
        l2indexlenght = 0;
        blocksize = 0;
        l1memtraffic = 0;
        l1 = new CacheSet[l1sets];
        l2 = new CacheSet[l2sets];
    }

    public void run(){
        //reading the trace file and adding commands into arralist
//        String filePath = "/Users/suryasundari/IdeaProjects/Machineproblem/src/main/resources/traces/" + traceFile;
        String filePath = traceFile;
        commands = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                commands.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        implement();
    }

    public void implement(){
        int j = 0;
        // Creating cachesets
        for (int i=0;i<l1sets;i++){
            l1[i] = new CacheSet(l1Assoc);
        }
        for (int i=0;i<l2sets;i++){
            l2[i] = new CacheSet(l2Assoc);
        }
        // calculating index, tag length
//        int l1memtraffic = 0;
        l1indexlenght = (int) (Math.log(l1sets) / Math.log(2));
        blocksize = (int) (Math.log(blockSize) / Math.log(2));
        l1taglength = 32 - l1indexlenght - blocksize;
        l2indexlenght = (int) (Math.log(l2sets) / Math.log(2));
        l2taglength = 32 - l2indexlenght - blocksize;
        for (String coms : commands){
            j++;
            String[] c = coms.split(" ");
            String cmd = c[0].trim();
            String add = c[1].trim();
            int number = Integer.parseInt(add, 16);
            // calculating tag, index, bynaryaddress for both l1 and l2
            String binaryAddress = utills.getBinary(number);
            l1tag = utills.getSubString(0, l1taglength, binaryAddress);
            l1index = utills.getSubString(l1taglength, l1taglength + l1indexlenght, binaryAddress);
            l2tag = utills.getSubString(0, l2taglength, binaryAddress);
            l2index = utills.getSubString(l2taglength, l2taglength + l2indexlenght, binaryAddress);
            //First checking wether tag is present in l1. If it is there then we are updating LRU.
            // If not checking wether we a place to keep in set. If it is there we are adding it to cache.If not evvicting the block 
            // and writing that evvict block into l2cache and then performing l2read.
            int index1 = l1[l1index].getIndexOfcacheBlock(l1tag);
            if(cmd.equals("r"))
                l1_reads++;
            else
                l1_writes++;
//            configBuilder.append("----------------------------------------").append("\n");
//            configBuilder.append(j + " " +coms).append("\n");
//            configBuilder.append("l1 " + utills.convertToHex(l1tag) + " " + l1index).append("\n");
            if(index1 >= 0){
//                configBuilder.append("L1 hit").append("\n");
                if(cmd.equals("w"))
                    l1[l1index].blocks.get(index1)[2] = 1;
                if(replacementPolicy.equals("LRU")){
//                    configBuilder.append("L1 update LRU\n");
                    l1[l1index].upDateQue(index1);
                }
            }else {
//                configBuilder.append("L1 miss").append("\n");
                if(cmd.equals("r"))
                    l1_reads_miss++;
                else
                    l1_writes_miss++;
                if(!l1[l1index].isFull()){
//                    configBuilder.append("L1 victim none").append("\n");
                    l1[l1index].addBlockInCache(l1tag, number, (cmd.equals("r") ? 0:1));
                }else {
                    boolean isDirty = (l1[l1index].blocks.get(0)[2] == 1) ? true:false;
//                    configBuilder.append("L1 victim:" + utills.convertToHex(l1[l1index].blocks.get(0)[0]) + " " + l1index + " " + ((!isDirty) ? "clean\n" : "Dirty\n"));
                    if(isDirty){
                        l1_write_backs++;
                        l2_writes++;
                        perFormL2Write(l2taglength, l2indexlenght, j);
                    }
                    l1[l1index].blocks.remove(0);
                    l1[l1index].addBlockInCache(l1tag, number, (cmd.equals("r") ? 0:1));
                }
                l2_reads++;
                perFormL2Read(number, j);
            }
        }
//        System.out.println(cnt);
        printL1Contents();
        printL2Contents();
        printFinalResults();
    }

    public void perFormL2Read(int number, int j){
//        configBuilder.append("L2 read " + utills.convertToHex(l2tag) + " " + l2index).append("\n");
        int index2 = l2[l2index].getIndexOfcacheBlock(l2tag);
        if(index2 >= 0){
//            configBuilder.append("L2 hit").append("\n");
            if(replacementPolicy.equals("LRU")){
                l2[l2index].upDateQue(index2);
//                configBuilder.append("L2 update LRU\n");
            }

        }else {
//            configBuilder.append("L2 miss").append("\n");
            l2_reads_miss++;
            if(!l2[l2index].isFull()){
//                configBuilder.append("L2 victim none").append("\n");
                l2[l2index].addBlockInCache(l2tag, number, 0);
            }else {
                boolean isDirty = l2[l2index].blocks.get(0)[2] == 1 ? true:false;
//                configBuilder.append("L2 victime " + utills.convertToHex(l2[l2index].blocks.get(0)[0]) + " " + l2index + " " + ((!isDirty) ? "clean\n" : "Dirty\n"));
                if(inclusionProperty == 1){
                    performl1WritebackIfpresent(l2[l2index].blocks.get(0)[0], l2index, l2[l2index].blocks.get(0)[1]);
                }
//                System.out.println(l1memtraffic);
                if(isDirty){
//                    System.out.println(j);
                    l2_write_backs++;
                }
                l2[l2index].blocks.remove(0);
                l2[l2index].addBlockInCache(l2tag, number, 0);
            }
        }
    }

// This will perform write on l2
    public void perFormL2Write(int l2taglength, int l2indexlenght, int j){
        int n = l1[l1index].blocks.get(0)[1];
        String address = utills.getBinary(n);
        int corresl2tag = utills.getSubString(0, l2taglength, address);
        int corresl2index = utills.getSubString(l2taglength, l2taglength + l2indexlenght, address);
        int index = l2[corresl2index].getIndexOfcacheBlock(corresl2tag);
//        configBuilder.append("l2 write " + corresl2tag + " " + corresl2index).append("\n");
        if(index >= 0){
//            configBuilder.append("l2 hit\n");
            l2[corresl2index].blocks.get(index)[2] = 1;
            if(replacementPolicy.equals("LRU")){
//                configBuilder.append("L2 update LRU\n");
                l2[corresl2index].upDateQue(index);
            }
//            l2[corresl2index].blocks.get(index)[2] = 1;
        }else {
//            configBuilder.append("l2 write miss\n");
            l2_writes_miss++;
            handleL2Miss(corresl2tag, corresl2index, l2taglength, l2indexlenght, n);
//            System.out.println("l2 write miss");
        }
    }
//This will handle if there is any miss in l2 during writing it to l2
    public void handleL2Miss(int cl2tag, int cl2index, int l2taglength, int l2indexlength, int n){
        boolean isDirty = l2[cl2index].blocks.get(0)[2] == 1 ? true:false;
        if(inclusionProperty == 1){
            performl1WritebackIfpresent(l2[cl2index].blocks.get(0)[1], cl2index, l2[cl2index].blocks.get(0)[1]);
        }
        if(isDirty){
//            System.out.println(n);
            l2_write_backs++;
        }
        l2[cl2index].blocks.remove(0);
        l2[cl2index].addBlockInCache(cl2tag, n, 1);
    }

//This is used when it is inclusive just checking wether the evvcited block l2 is present l1 or not. If it is there then removing from l1
    public void performl1WritebackIfpresent(int evvictl2Tag, int evictl2Index, int n){
        String add = utills.getBinary(n);
        int corresevictl1tag = utills.getSubString(0, l1taglength, add);
        int corresevictl1Index = utills.getSubString(l1taglength, l1taglength + l1indexlenght, add);
        int ind = l1[corresevictl1Index].getIndexOfcacheBlock(corresevictl1tag);
        if(ind >= 0){
            if(l1[corresevictl1Index].blocks.get(ind)[2] == 1){
                l1memtraffic++;
//                System.out.println(l1memtraffic);
            }
            l1[corresevictl1Index].blocks.remove(ind);
        }
    }
    public void printL1Contents(){
        System.out.println("===== L1 contents =====");
//        configBuilder.append("===== L1 contents =====").append("\n");
        for(int row = 0 ; row<l1sets; row++){
            System.out.print("Set     " + row + ":     ");
//            configBuilder.append("Set     " + row + ":     ");
            for (int col =0; col<l1Assoc; col++){
                System.out.print(utills.convertToHex(l1[row].blocks.get(col)[0]) + " " + ((l1[row].blocks.get(col)[2] == 1) ? "D  " : "   "));
//                configBuilder.append(utills.convertToHex(l1[row].blocks.get(col)[0]) + " " + ((l1[row].blocks.get(col)[2] == 1) ? "D  ":"   "));
            }
            System.out.println();
//            configBuilder.append("\n");
        }
    }

    public void printL2Contents(){
        System.out.println("===== L2 contents =====");
//        configBuilder.append("===== L2 contents =====").append("\n");
        for(int row = 0 ; row<l2sets; row++){
            System.out.print("Set     " + row + ":     ");
//            configBuilder.append("Set     " + row + ":     ");
            for (int col =0; col<l2Assoc; col++){
                System.out.print(utills.convertToHex(l2[row].blocks.get(col)[0]) + " " + ((l2[row].blocks.get(col)[2] == 1) ? "D  " : "   "));
//                configBuilder.append(utills.convertToHex(l2[row].blocks.get(col)[0]) + " " +((l2[row].blocks.get(col)[2] == 1) ? "D  ":"   "));
            }
            System.out.println();
//            configBuilder.append("\n");
        }
    }

    public void printFinalResults(){
        float l2missrate = (float) l2_reads_miss/l2_reads;
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
        System.out.println("k. L2 miss rate:              " + l2missrate);
        System.out.println("l. number of L2 writebacks:   " + l2_write_backs);
// System.out.println(l1memtraffic);
        System.out.println("m. total memory traffic:      " + (utills.getMemoryTraffic(l2_reads_miss, l2_writes_miss, l2_write_backs) + l1memtraffic));

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
//
//        configBuilder.append("k. L2 miss rate:              "+l2missrate).append("\n");
//        configBuilder.append("l. number of L2 writebacks:   "+l2_write_backs).append("\n");
////        System.out.println(l1memtraffic);
//        configBuilder.append("m. total memory traffic:      "+(utills.getMemoryTraffic(l2_reads_miss, l2_writes_miss, l2_write_backs) + l1memtraffic));
//        String filename = "/Users/suryasundari/IdeaProjects/Machineproblem/src/main/resources/output.txt";
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
//            writer.write(configBuilder.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
