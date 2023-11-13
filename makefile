# makefile begins 

JFLAGS = -g
JC = javac
JVM= java
FILE=


.SUFFIXES: .java .class



.java.class:
	$(JC) $(JFLAGS) $*.java



CLASSES = \
        sim_cache.java \
        CacheEntry.java \
        Cache2LevelSimulator.java \
        CacheSet.java \
        CacheSimulator.java \
        Utills.java


MAIN = sim_cache


default: classes


classes: $(CLASSES:.java=.class)


sim_cache: $(MAIN).class
	$(JVM) $(MAIN) $(FILE) $(ARGS)


clean:
	$(RM) *.class