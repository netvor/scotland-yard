package io.github.nejc92.sy.players;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.github.nejc92.sy.strategies.CoalitionReduction;
import io.github.nejc92.sy.strategies.MoveFiltering;
import io.github.nejc92.sy.strategies.Playouts;

public class PlayerProvider {
    private static final String DEFAULT_HIDER_NAME = "Fantom";
    private Stack<Player.Color> availableColors;
    private Stack<Integer> availableLocations;
    private Set<String> usedNames;
    private Set<Integer> usedLocations;
    private List<Player> players;
    private Playouts.Uses playouts;
    private CoalitionReduction.Uses coalitionReduction;
    private MoveFiltering.Uses moveFiltering;
    private int boardSize;

    public PlayerProvider() {
        usedNames = new HashSet<String>();
        usedLocations = new HashSet<Integer>();
        players = new ArrayList<Player>();
        availableColors = new Stack<Player.Color>();
        for (Player.Color color : Player.Color.values()) {
            availableColors.add(color);
        }
        Collections.shuffle(availableColors);
    }

    public void addPlayer(Player.Type type, Player.Operator operator) throws Exception {
        addPlayer(type, operator, "", 0);
    }

    public void addPlayer(Player.Type type, Player.Operator operator, String preferredName) throws Exception {
        addPlayer(type, operator, preferredName, 0);
    }

    public void addPlayer(Player.Type type, Player.Operator operator, int preferredLocation) throws Exception {
        addPlayer(type, operator, "", preferredLocation);
    }

    public void addPlayer(Player.Type type, Player.Operator operator, String preferredName, int preferredLocation) throws Exception {
        String name;
        int location;
        if (preferredName == null || preferredName.trim().length() <= 0) {
            if (type == Player.Type.HIDER) {
                name = DEFAULT_HIDER_NAME;
            } else {
                name = availableColors.pop().name();
            }
        } else {
            name = preferredName;
        }
        if (!usedNames.add(name)) {
            throw new Exception(String.format("Player with name '%s' already exists!", name));
        }
        if (preferredLocation<=0 || preferredLocation > boardSize) {
            location = availableLocations.pop();
        } else {
            if (!availableLocations.removeElement(preferredLocation))
                throw new Exception(String.format("Board position %d is not available!", preferredLocation));
            location = preferredLocation;
        }
        if (!usedLocations.add(location)) {
            throw new Exception(String.format("Board position %d is already occupied!", location));
        }
        switch (type) {
            case HIDER:
                // If we are adding a hider, add to the front of the list. This is to facilitate the use case where seekers pick their positions before the hider.
                players.add(0, new Hider(operator, name, location, playouts, coalitionReduction, moveFiltering));
                break;
            case SEEKER:
                players.add(new Seeker(operator, name, location, playouts, coalitionReduction, moveFiltering));
                break;
        }
    }

    public PlayerProvider setPlayouts(Playouts.Uses playouts) {
        this.playouts = playouts;
        return this;
    }

    public PlayerProvider setCoalitionReduction(CoalitionReduction.Uses coalitionReduction) {
        this.coalitionReduction = coalitionReduction;
        return this;
    }

    public PlayerProvider setMoveFiltering(MoveFiltering.Uses moveFiltering) {
        this.moveFiltering = moveFiltering;
        return this;
    }

    public PlayerProvider setBoardSize(int boardSize) {
        this.boardSize = boardSize;
        availableLocations = IntStream.rangeClosed(1,boardSize).boxed().collect(Collectors.toCollection(Stack::new));
        Collections.shuffle(availableLocations);
        return this;
    }

    public Player[] initializePlayers() {
        return players.toArray(new Player[0]);
    }
}
