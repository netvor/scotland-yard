package io.github.nejc92.sy.game;

import io.github.nejc92.mcts.MctsDomainState;
import io.github.nejc92.sy.players.Hider;
import io.github.nejc92.sy.players.Player;
import io.github.nejc92.sy.players.Seeker;

import java.util.*;

public class State implements MctsDomainState<Action, Player> {

    private static final int MAX_NUMBER_OF_ROUNDS = 24;
    private static final List<Integer> HIDER_SURFACES_ROUNDS = new ArrayList<>(Arrays.asList(3, 8, 13, 18, 24));
    private static final int ALL_PLAYERS = 0;
    private static final int ONLY_SEEKERS = 1;

    private final PlayersOnBoard playersOnBoard;
    private final StateHistory stateHistory;
    private final int numberOfPlayers;
    private int currentRound;
    private int currentPlayerIndex;
    private int previousPlayerIndex;
    private Action.Transportation lastHidersTransportation;
    private boolean inSearch;
    private boolean searchInvokingPlayerIsHider;
    private boolean searchInvokingPlayerUsesCoalitionReduction;
    private boolean searchInvokingPlayerUsesMoveFiltering;

    public static State initialize(Player[] players) {
        PlayersOnBoard playersOnBoard = PlayersOnBoard.initialize(players);
        StateHistory stateHistory = StateHistory.initialize(players);
        return new State(playersOnBoard, stateHistory, players.length);
    }

    private State(PlayersOnBoard playersOnBoard, StateHistory stateHistory, int numberOfPlayers) {
        this.playersOnBoard = playersOnBoard;
        this.stateHistory = stateHistory;
        this.numberOfPlayers = numberOfPlayers;
        this.currentRound = 1;
        this.currentPlayerIndex = 0;
        this.previousPlayerIndex = numberOfPlayers - 1;
        this.lastHidersTransportation = null;
        this.inSearch = false;
        this.searchInvokingPlayerIsHider = false;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public PlayersOnBoard getPlayersOnBoard() {
        return playersOnBoard;
    }

    public StateHistory getStateHistory() {
        return stateHistory;
    }

    public int getPreviousPlayerIndex() {
        return previousPlayerIndex;
    }

    @Override
    public Player getCurrentAgent() {
        return playersOnBoard.getPlayerAtIndex(currentPlayerIndex);
    }

    @Override
    public Player getPreviousAgent() {
        return playersOnBoard.getPlayerAtIndex(previousPlayerIndex);
    }

    public void setSearchModeOn() {
        inSearch = true;
        searchInvokingPlayerIsHider = playersOnBoard.playerIsHider(currentPlayerIndex);
        searchInvokingPlayerUsesCoalitionReduction = playersOnBoard.playerUsesCoalitionReduction(currentPlayerIndex);
        searchInvokingPlayerUsesMoveFiltering = playersOnBoard.playerUsesMoveFiltering(currentPlayerIndex);
    }

    public boolean searchInvokingPlayerUsesCoalitionReduction() {
        return searchInvokingPlayerUsesCoalitionReduction;
    }

    public void setSearchModeOff() {
        inSearch = false;
    }

    public boolean currentPlayerIsHider() {
        return playersOnBoard.playerIsHider(currentPlayerIndex);
    }

    public boolean previousPlayerIsHider() {
        return playersOnBoard.playerIsHider(previousPlayerIndex);
    }

    public boolean currentPlayerIsHuman() {
        return playersOnBoard.playerIsHuman(currentPlayerIndex);
    }

    public boolean currentPlayerIsRandom() {
        return playersOnBoard.playerIsRandom(currentPlayerIndex);
    }

    public boolean previousPlayerIsHuman() {
        return playersOnBoard.playerIsHuman(previousPlayerIndex);
    }

    private boolean inSearchFromSeekersPov() {
        return inSearch && !searchInvokingPlayerIsHider;
    }

    public void printNewRound() {
        if (currentPlayerIsHider()) {
            System.out.println("ROUND: " + currentRound);
            if (isHiderSurfacesRound()) {
                stateHistory.recordHiderSurfaces(currentRound);
                System.out.println("HIDER SURFACES!");
            }
            System.out.println("----------");
        }
    }

    public boolean isHiderSurfacesRound() {
        return HIDER_SURFACES_ROUNDS.contains(currentRound);
    }

    @Override
    public boolean isTerminal() {
        return seekersWon() || hiderWon();
    }

    public boolean seekersWon() {
        if (inSearchFromSeekersPov())
            return playersOnBoard.anySeekerOnHidersMostProbablePosition();
        else
            return playersOnBoard.anySeekerOnHidersActualPosition();
    }

    public boolean hiderWon() {
        return currentRound == MAX_NUMBER_OF_ROUNDS + 1;
    }

    public boolean seekerWon(Seeker seeker) {
        if (inSearchFromSeekersPov())
            return playersOnBoard.seekerOnHidersMostProbablePosition(seeker);
        else
            return playersOnBoard.seekerOnHidersActualPosition(seeker);
    }

    @Override
    public MctsDomainState<Action,Player> performActionForCurrentAgent(Action action) {
        validateIsAvailableAction(action);
        if (inSearchFromSeekersPov())
            playersOnBoard.movePlayerFromSeekersPov(currentPlayerIndex, action);
        else
            playersOnBoard.movePlayerFromActualPosition(currentPlayerIndex, action);
        if (currentPlayerIsHider())
            lastHidersTransportation = action.getTransportation();
        setHidersMostProbablePosition(lastHidersTransportation);
        if (!inSearch)
            stateHistory.recordMove(currentRound, currentPlayerIndex, action);
        prepareStateForNextPlayer();
        performDoubleMoveIfShould();
        return this;
    }

    private void validateIsAvailableAction(Action action) {
        if (!isAvailableAction(action)) {
            throw new IllegalArgumentException("Error: invalid action passed as function parameter");
        }
    }

    private boolean isAvailableAction(Action action) {
        return getAvailableActionsForCurrentAgent().contains(action);
    }

    private void setHidersMostProbablePosition(Action.Transportation transportation) {
        if (currentPlayerIsHider())
            setHidersMostProbablePositionAfterHider(transportation);
        else
            playersOnBoard.removeCurrentSeekersPositionFromPossibleHidersPositions(currentPlayerIndex);
    }

    private void setHidersMostProbablePositionAfterHider(Action.Transportation transportation) {
        if (isHiderSurfacesRound())
            playersOnBoard.setHidersActualAsMostProbablePosition();
        else
            playersOnBoard.recalculateHidersMostProbablePosition(transportation);
    }

    private void performDoubleMoveIfShould() {
        if (shouldCheckForHidersDoubleMoveAutomatically()) {
            Hider hider = (Hider) getPreviousAgent();
            if (!stateHistory.doubleMoveAvailable(currentRound, previousPlayerIndex)
                    && hider.shouldUseDoubleMove(currentRound, playersOnBoard, searchInvokingPlayerUsesMoveFiltering)) {
                skipAllSeekers();
                hider.removeDoubleMoveCard();
            }
        }
    }

    private boolean shouldCheckForHidersDoubleMoveAutomatically() {
        return previousPlayerIsHider() && (previousPlayerIsHuman() && inSearch || !previousPlayerIsHuman());
    }

    public void skipAllSeekers() {
        currentPlayerIndex--;
        // currentRound++;
    }

    @Override
    public MctsDomainState<Action,Player> skipCurrentAgent() {
        prepareStateForNextPlayer();
        return this;
    }

    @Override
    public int getNumberOfAvailableActionsForCurrentAgent() {
        return getAvailableActionsForCurrentAgent().size();
    }

    @Override
    public List<Action> getAvailableActionsForCurrentAgent() {
        List<Action> availableActions;
        if (inSearchFromSeekersPov())
            availableActions = playersOnBoard.getAvailableActionsFromSeekersPov(currentPlayerIndex);
        else
            availableActions = playersOnBoard.getAvailableActionsForActualPosition(currentPlayerIndex);
        return availableActions;
    }

    private boolean notHumanInSearch() {
        return currentPlayerIsHuman() && !inSearch;
    }

    private void prepareStateForNextPlayer() {
        if (isLastPlayerOfRound())
            currentRound++;
        previousPlayerIndex = currentPlayerIndex;
        currentPlayerIndex = ++currentPlayerIndex % playersOnBoard.getNumberOfPlayers();
    }

    private boolean isLastPlayerOfRound() {
        return currentPlayerIndex == numberOfPlayers - 1;
    }

    public void printAllPositions() {
        playersOnBoard.printPlayers(ALL_PLAYERS);
    }

    public void printSeekersPositions() {
        playersOnBoard.printPlayers(ONLY_SEEKERS);
    }

    public void updateHidersProbablePosition() {
        playersOnBoard.fixHidersProbablePosition();
    }
}