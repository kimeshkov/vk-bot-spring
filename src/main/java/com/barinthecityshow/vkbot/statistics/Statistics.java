package com.barinthecityshow.vkbot.statistics;

public class Statistics {
    private final int allWantSticker;
    private final int allUsersInDialog;
    private final int allWinners;

    private Statistics(int allWantSticker, int allUsersInDialog, int allWinners) {
        this.allWantSticker = allWantSticker;
        this.allUsersInDialog = allUsersInDialog;
        this.allWinners = allWinners;
    }

    public String prettyPrint() {
        return "All Users want sticker: " + allWantSticker
                + "\n"
                + " All users in dialog: " + allUsersInDialog
                + "\n"
                + " All winners: " + allWinners;

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int allWantSticker;
        private int allUsersInDialog;
        private int allWinners;

        public Builder allWantSticker(int allWantSticker) {
            this.allWantSticker = allWantSticker;
            return this;
        }

        public Builder allUsersInDialog(int allUsersInDialog) {
            this.allUsersInDialog = allUsersInDialog;
            return this;
        }

        public Builder allWinners(int allWinners) {
            this.allWinners = allWinners;
            return this;
        }

        public Statistics build() {
            return new Statistics(allWantSticker, allUsersInDialog, allWinners);
        }
    }


}
