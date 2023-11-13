// This class is used to calculate some important functions like getting bynary from number, convertin hexa to integer and vice verse

public class Utills {
    public String getBinary(int number){
        String binaryAddress = Integer.toBinaryString(number);
        binaryAddress = binaryAddress.length() < 32 ? appendZeros(binaryAddress) : binaryAddress;
        return binaryAddress;
    }

    public String appendZeros(String address){
        int intialLength = address.length();
        for(int i = 0; i < 32 - intialLength; i++){
            address = "0" + address;
        }
        return address;
    }

    public int getSubString(int start, int end, String st){
        return Integer.parseInt(st.substring(start, end),2);
    }

    public String convertToHex(int num){
        return Integer.toHexString(num);
    }

    public String getVictim(CacheEntry victimCache){
        String victimAddress = convertToHex(victimCache.address);
        String victimTag = convertToHex(victimCache.tag);
        int victimInd = victimCache.index;
        String dirty = (victimCache.isDirty) ? "dirty":"clean";
        return victimAddress + " (tag" + victimTag + ", index " + victimInd + ", " + dirty;
    }

    public double calculateMissRate(int readMisses, int writeMisses, int reads, int writes){
        if (reads == 0)
            return 0;
        double misRate = (double)(readMisses + writeMisses)/(reads + writes);
        return misRate;
    }

    public int getMemoryTraffic(int read_miss, int write_miss, int write_back){
        return read_miss + write_miss + write_back;
    }

    public double calculateL2MissRate(int readmisses, int reads){
        if(reads == 0 && readmisses == 0){
//            System.out.println("why");
            return 0;
        }
        double missrate = readmisses/reads;
        return missrate;
    }
}
