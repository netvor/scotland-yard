package io.github.nejc92.sy.players;

import io.github.nejc92.sy.game.Action;
import io.github.nejc92.sy.game.PlayersOnBoard;
import io.github.nejc92.sy.game.State;
import io.github.nejc92.sy.strategies.CoalitionReduction;
import io.github.nejc92.sy.strategies.MoveFiltering;
import io.github.nejc92.sy.strategies.Playouts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Hider extends Player {

    private static final int BLACK_FARE_TICKETS = 2;
    private static final int DOUBLE_MOVE_CARDS = 2;
    private static final int TAXI_TICKETS = 4;
    private static final int BUS_TICKETS = 3;
    private static final int UNDERGROUND_TICKETS = 3;

    private int doubleMoveCards;
    private int blackFareTickets;
    private Set<Integer> doubleMovesMadeInRounds;

    public Hider(Operator operator, String name, int startingPosition, Playouts.Uses playout, CoalitionReduction.Uses coalitionReduction,
                 MoveFiltering.Uses moveFiltering) {
        super(operator, Type.HIDER, name, startingPosition, TAXI_TICKETS, BUS_TICKETS, UNDERGROUND_TICKETS,
                playout, coalitionReduction, moveFiltering);
        this.doubleMoveCards = DOUBLE_MOVE_CARDS;
        this.blackFareTickets = BLACK_FARE_TICKETS;
        this.doubleMovesMadeInRounds = new HashSet<Integer>();
    }

    public void removeDoubleMoveCard(int round) {
        doubleMovesMadeInRounds.add(round);
        doubleMoveCards--;
    }

    public void removeBlackFareTicket() {
        blackFareTickets--;
    }

    public boolean hasDoubleMoveCard(int round) {
        return doubleMoveCards > 0 && !doubleMovesMadeInRounds.contains(round);
    }

    public int getDoubleMoveCards() {
        return doubleMoveCards;
    }

    public boolean hasBlackFareTicket() {
        return blackFareTickets > 0;
    }

    public int getBlackFareTickets() {
        return blackFareTickets;
    }

    @Override
    public void addTicket(Action.Transportation transportation) {
        super.addTicket(transportation);
    }

    @Override
    protected Action getActionForHiderFromStatesAvailableActionsForSimulation(State state) {
        if (this.usesBiasedPlayout())
            return Playouts.getGreedyBiasedActionForHider(state);
        else
            return Playouts.getRandomAction(state);
    }

    @Override
    protected Action getActionForSeekerFromStatesAvailableActionsForSimulation(State state) {
        if (this.usesBiasedPlayout())
            return Playouts.getGreedyBiasedActionForSeeker(state);
        else
            return Playouts.getRandomAction(state);
    }

    @Override
    public double getRewardFromTerminalState(State state) {
        if (state.hiderWon())
            return 1;
        else
            return 0;
    }

    public boolean shouldUseBlackfareTicket(int currentRound, List<Action> actions,
                                            boolean searchInvokingPlayerUsesMoveFiltering) {
        if (searchInvokingPlayerUsesMoveFiltering)
            return hasBlackFareTicket() && MoveFiltering.optimalToUseBlackFareTicket(currentRound, actions);
        else
            return hasBlackFareTicket() && MoveFiltering.shouldUseBlackFareTicketGreedy();
    }

    public boolean shouldUseDoubleMove(int currentRound, PlayersOnBoard playersOnBoard,
                                       boolean searchInvokingPlayerUsesMoveFiltering) {
        if (searchInvokingPlayerUsesMoveFiltering)
            return hasDoubleMoveCard(currentRound) && MoveFiltering.optimalToUseDoubleMoveCard(playersOnBoard);
        else
            return hasDoubleMoveCard(currentRound) && MoveFiltering.shouldUseDoubleMoveCardGreedy();
    }
}