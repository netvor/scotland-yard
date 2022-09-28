package io.github.nejc92.sy.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.nejc92.sy.players.Player;

public class StateHistory {
    private class MoveRecord implements Comparable<MoveRecord> {
        private final int round;
        private final int playerIndex;
        private final long multiMoveIndex;
        private final Action action;

        public MoveRecord(int round, int playerIndex, long multiMoveIndex, Action action) {
            this.round = round;
            this.playerIndex = playerIndex;
            this.multiMoveIndex = multiMoveIndex;
            this.action = action;
        }

        public int getRound() {
            return round;
        }

        public int getPlayerIndex() {
            return playerIndex;
        }

        public long getMultiMoveIndex() {
            return multiMoveIndex;
        }

        public Action getAction() {
            return action;
        }

        @Override
        public int compareTo(MoveRecord other) {
            if (this.round < other.getRound())
                return -1;
            if (this.round > other.getRound())
                return 1;
            if (this.multiMoveIndex < other.getMultiMoveIndex())
                return -1;
            if (this.multiMoveIndex > other.getMultiMoveIndex())
                return 1;
            if (this.playerIndex < other.getPlayerIndex())
                return -1;
            if (this.playerIndex > other.getPlayerIndex())
                return 1;
            return this.action.compareTo(other.getAction());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            MoveRecord other = (MoveRecord) obj;
            return this.round == other.getRound()
                    && this.playerIndex == other.getPlayerIndex()
                    && this.multiMoveIndex == other.getMultiMoveIndex()
                    && this.action.equals(other.getAction());
        }
    }

    private final SortedSet<MoveRecord> history;
    private final Set<Integer> surfaceInRounds;
    private final Player[] players;

    private StateHistory(Player[] players) {
        this.players = players;
        this.history = new TreeSet<MoveRecord>();
        this.surfaceInRounds = new HashSet<Integer>();
    }

    public static StateHistory initialize(Player[] players) {
        return new StateHistory(players);
    }

    private long numberOfMovesFor(int round, int playerIndex) {
        return history.stream().filter(mr -> mr.getRound() == round && mr.getPlayerIndex() == playerIndex).count();
    }

    public boolean doubleMoveAvailable(int round, int playerIndex) {
        return numberOfMovesFor(round, playerIndex) < 2;
    }

    public void recordMove(int round, int playerIndex, Action action) {
        long multiMoveIndex = numberOfMovesFor(round, playerIndex);
        history.add(new MoveRecord(round, playerIndex, multiMoveIndex, action));
    }

    public void recordHiderSurfaces(int currentRound) {
        surfaceInRounds.add(currentRound);
    }

    public void printStateHistory(int untilRound) {
        int maxWidthOfPlayerName = Arrays.stream(players).mapToInt(p -> p.getName().length()).max().orElse(1);
        int maxWidthOfTransportation = Arrays.stream(Action.Transportation.values())
                .mapToInt(t -> t.toString().length()).max().orElse(1);
        int columnWidth = Math.max(maxWidthOfPlayerName, maxWidthOfTransportation + 4 + 3);
        final String DASH = "-";
        final String CROSS = "+";
        final String PIPE = "|";
        final String STAR = "*";
        final String SPACE = " ";
        final String CELLBORDER_FIRST = "-----";
        final String CELLBORDER_NEXT = String.join("", Collections.nCopies(columnWidth, DASH));
        final String ROWBORDER = Stream
                .concat(Stream.of(CELLBORDER_FIRST), Stream.generate(() -> CELLBORDER_NEXT).limit(players.length))
                .collect(Collectors.joining(CROSS, CROSS, CROSS));
        final String LEFTALIGNFORMAT = String.format("%%-%ds", columnWidth);
        final String ACTIONFORMAT = String.format("%%-%ds -> %%3d%%1s", columnWidth - 4 - 3);
        final String EMPTYCELL = String.format(LEFTALIGNFORMAT, "");
        final String FIRSTCOL_EMPTY = "     ";
        final String FIRSTCOLHEADER = "Round";

        System.out.println(ROWBORDER);
        System.out.println(Stream.concat(
            Stream.of(FIRSTCOL_EMPTY),
            Arrays.stream(players).map(p -> String.format(LEFTALIGNFORMAT, p.getType())))
                .collect(Collectors.joining(PIPE, PIPE, PIPE)));
        System.out.println(Stream.concat(
            Stream.of(FIRSTCOLHEADER),
            Arrays.stream(players).map(p -> String.format(LEFTALIGNFORMAT, p.getName())))
                .collect(Collectors.joining(PIPE, PIPE, PIPE)));

        boolean newRow = true;
        int rowNumber = history.stream().mapToInt(MoveRecord::getRound).min().orElseThrow() - 1;
        int colNumber = 0;
        for (MoveRecord moveRecord : history) {
            if (moveRecord.getRound() < rowNumber) {
                System.out.println();
                System.out.printf("ERROR! move record round %d is less than current row number %d!%n",
                        moveRecord.getRound(), rowNumber);
                return;
            }
            if (moveRecord.getPlayerIndex() < 0 || moveRecord.getPlayerIndex() >= players.length) {
                System.out.println();
                System.out.printf("ERROR! player index %d is out of bounds!%n", moveRecord.getPlayerIndex());
                return;
            }

            if (newRow) {
                newRow = false;
                colNumber = 0;
                if (moveRecord.getMultiMoveIndex() > 0) {
                    System.out.print(PIPE + FIRSTCOL_EMPTY + PIPE);
                } else {
                    rowNumber++;
                    String marker = surfaceInRounds.contains(rowNumber) ? SPACE + STAR : SPACE + SPACE;
                    System.out.println(ROWBORDER);
                    System.out.printf("%1s%3d%2s%1s", PIPE, rowNumber, marker, PIPE);
                }
            }
            while (rowNumber < moveRecord.getRound() || colNumber < moveRecord.getPlayerIndex()) {
                System.out.print(EMPTYCELL + PIPE);
                colNumber++;
                if (colNumber == players.length) {
                    System.out.println();
                    System.out.println(ROWBORDER);
                    rowNumber++;
                    colNumber = 0;
                    String marker = surfaceInRounds.contains(rowNumber) ? PIPE + STAR : PIPE + SPACE;
                    System.out.printf("%1s%3d%2s%1s", PIPE, rowNumber, marker, PIPE);
                }
            }
            if (rowNumber != moveRecord.getRound() && colNumber != moveRecord.getPlayerIndex()) {
                System.out.println();
                System.out.printf("ERROR! move record row/col %d/%d do not match expected values %d/%d!%n",
                        moveRecord.getRound(), moveRecord.getPlayerIndex(), rowNumber, colNumber);
                return;
            }
            System.out.printf(ACTIONFORMAT, moveRecord.action.getTransportation().toString(),
                    moveRecord.action.getDestination(), PIPE);
            colNumber++;
            if (colNumber == players.length) {
                System.out.println();
                newRow = true;
            }
        }
        while (rowNumber < untilRound || colNumber < players.length) {
            if (colNumber == players.length) {
                System.out.println(ROWBORDER);
                rowNumber++;
                colNumber = 0;
                String marker = surfaceInRounds.contains(rowNumber) ? PIPE + STAR : PIPE + SPACE;
                System.out.printf("%1s%3d%2s%1s", PIPE, rowNumber, marker, PIPE);
            }
            System.out.print(EMPTYCELL + PIPE);
            colNumber++;
            if (colNumber == players.length)
                System.out.println();
        }
        System.out.println(ROWBORDER);
        System.out.println();
    }

}
