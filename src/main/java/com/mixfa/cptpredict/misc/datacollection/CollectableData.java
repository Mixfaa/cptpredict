package com.mixfa.cptpredict.misc.datacollection;

public interface CollectableData {
    String display();

    static CollectableData format(String format, Object... args) {
        return SimpleCollectableData.format(format, args);
    }

    record SimpleCollectableData(
            String text
    ) implements CollectableData {

        @Override
        public String display() {
            return text;
        }

        public static SimpleCollectableData format(String format, Object... args) {
            return new SimpleCollectableData(String.format(format, args));
        }
    }

    record RamLimitExceeded(
            long availableRam,
            long usedRam
    ) implements CollectableData {
        @Override
        public String display() {
            return String.format("Ram limit exceeded by: %d MB", (usedRam - availableRam) / 1024);
        }
    }
}
