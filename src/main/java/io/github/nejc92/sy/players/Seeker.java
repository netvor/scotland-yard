package io.github.nejc92.sy.players;

import io.github.nejc92.sy.game.Action;
import io.github.nejc92.sy.game.State;
import io.github.nejc92.sy.strategies.CoalitionReduction;
import io.github.nejc92.sy.strategies.MoveFiltering;
import io.github.nejc92.sy.strategies.Playouts;

public class Seeker extends Player {

    private static final int TAXI_TICKETS = 9;
    private static final int BUS_TICKETS = 10;
    private static final int UNDERGROUND_TICKETS = 5;
    private static final double COALITION_REDUCTION_PARAMETER = 0.25;

    public Seeker(Operator operator, String name, int startingPosition, Playouts.Uses playout, CoalitionReduction.Uses coalitionReduction,
                  MoveFiltering.Uses moveFiltering) {
        super(operator, Type.SEEKER, name, startingPosition, TAXI_TICKETS, BUS_TICKETS, UNDERGROUND_TICKETS, playout,
                coalitionReduction, moveFiltering);
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
        if (state.searchInvokingPlayerUsesCoalitionReduction())
            return CoalitionReduction.getCoalitionReductionRewardFromTerminalState(state, this);
        else
            return CoalitionReduction.getNormalRewardFromTerminalState(state);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    protected void reset() {
        busTickets = BUS_TICKETS;
        taxiTickets = TAXI_TICKETS;
        undergroundTickets = UNDERGROUND_TICKETS;
    }
}