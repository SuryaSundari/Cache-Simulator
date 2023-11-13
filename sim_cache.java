/* How to run the code
1. First we need to compile the code for that we have to use the command "make".
2. Now it will create all .class files.
3. Now you have to use the this command to run the progrrame 
        java sim_cache.java 16 1024 2 8192 4 0 0 gcc_trace.txt
        or
        make sim_cache ARGS="16 1024 2 8192 4 0 0 gcc_trace.txt"
4. Arguments should pass like this then only code will run.
*/

public class sim_cache {

    public static void main(String[] args) {
        // validating arguments. If arguments are not valid program will exit
        if(args.length < 7){
            System.out.println("You didnt gave enough arguments to run the code");
            System.exit(1);
        }
        validateArguments(args);
        //Reading argumets
        int block_size = Integer.parseInt(args[0]);
        int l1_size = Integer.parseInt(args[1]);
        int l1_assoc = Integer.parseInt(args[2]);
        int l2_size = Integer.parseInt(args[3]);
        int l2_assoc = Integer.parseInt(args[4]);
        String replacement_policy = (Integer.parseInt(args[5]) == 1) ? "FIFO" : "LRU";
        int inclussion_property = Integer.parseInt(args[6]);
        String trace_file = args[7];

        StringBuilder configBuilder = new StringBuilder();
        System.out.println("===== Simulator configuration =====");
        System.out.println("BLOCKSIZE:             " + block_size);
        System.out.println("L1_SIZE:               " + l1_size);
        System.out.println("L1_ASSOC:              " + l1_assoc);
        System.out.println("L2_SIZE:               " + l2_size);
        System.out.println("L2_ASSOC:              " + l2_assoc);
        System.out.println("REPLACEMENT POLICY:    " + replacement_policy);
        System.out.println("INCLUSION PROPERTY:    " + ((inclussion_property == 0) ? "non-inclusive" : "inclusive"));
        System.out.println("trace_file:            " + trace_file);

        configBuilder.append("===== Simulator configuration =====\n");
        configBuilder.append("BLOCKSIZE:             ").append(block_size).append("\n");
        configBuilder.append("L1_SIZE:               ").append(l1_size).append("\n");
        configBuilder.append("L1_ASSOC:              ").append(l1_assoc).append("\n");
        configBuilder.append("L2_SIZE:               ").append(l2_size).append("\n");
        configBuilder.append("L2_ASSOC:              ").append(l2_assoc).append("\n");
        configBuilder.append("REPLACEMENT POLICY:    ").append(replacement_policy).append("\n");
        configBuilder.append("INCLUSION PROPERTY:    ").append((inclussion_property==0) ? "non-inclusive":"inclusive").append("\n");
        configBuilder.append("trace_file:            ").append(trace_file).append("\n");
        //calaculating number of l1 and l2 sets
        int l1sets = (l1_size)/(l1_assoc * block_size);
        int l2sets = 0;
        if (l2_size != 0)
            l2sets = (l2_size)/(l2_assoc * block_size);
        
        //If l2 size is 0 calling single level cache simulator class else calling 2 level cache simulator class.
        if(l2_size == 0){
            CacheSimulator cacheSimulator = new CacheSimulator(l1sets, l2sets, block_size, l1_assoc, l2_assoc, inclussion_property, trace_file, configBuilder, replacement_policy);
            cacheSimulator.run();
        }else {
            Cache2LevelSimulator cache2LevelSimulator = new Cache2LevelSimulator(l1sets, l2sets, block_size, l1_assoc, l2_assoc, inclussion_property, trace_file, configBuilder, replacement_policy);
            cache2LevelSimulator.run();
        }
    }

    public static void validateArguments(String[] args){
        int block_size = Integer.parseInt(args[0]);
        int l1_size = Integer.parseInt(args[1]);
        int l1_assoc = Integer.parseInt(args[2]);
        int l2_size = Integer.parseInt(args[3]);
        int l2_assoc = Integer.parseInt(args[4]);
        int replacemnt_polacy = Integer.parseInt(args[5]);
        int inclussion_property = Integer.parseInt(args[6]);

        if(!isPowOfTwo(block_size) || !isPowOfTwo(l1_size) || !isPowOfTwo(l1_assoc) || !isPowOfTwo(l2_size) || !isPowOfTwo(l2_assoc)){
            System.out.println("l1 and l2 size, assoc should be power of 2");
            System.exit(1);
        }
        if(replacemnt_polacy != 0 && replacemnt_polacy != 1){
            System.out.println("Please mention valid replacement polecy");
            System.exit(1);
        }

        if(inclussion_property != 0 && inclussion_property != 1){
            System.out.println("Please mention valid inclusion property");
            System.exit(1);
        }

    }

    public static boolean isPowOfTwo(int num){
        if(num == 0)
            return true;
        int c1 = num & (num - 1);
        return (num > 0) && (c1 == 0);
    }
}
