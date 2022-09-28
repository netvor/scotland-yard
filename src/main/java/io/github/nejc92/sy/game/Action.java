package io.github.nejc92.sy.game;

public class Action implements Comparable<Action> {

    public enum Transportation {
        TAXI, BUS, UNDERGROUND, BLACK_FARE;

        public String toString() {
            switch(this) {
                case BLACK_FARE:
                    return "loď";
                case BUS:
                    return "drožka";
                case TAXI:
                    return "taxi";
                case UNDERGROUND:
                    return "tramvaj";
                default:
                    return null;
            }
        }
    }

    private final Transportation transportation;
    private final int destination;
    public Action(Transportation transportation, int destination) {
        this.transportation = transportation;
        this.destination = destination;
    }

    public Transportation getTransportation() {
        return transportation;
    }

    public int getDestination() {
        return destination;
    }

    public boolean isTransportationAction(Transportation transportation) {
        return this.transportation == transportation;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        Action action = (Action) object;
        return destination == action.destination && transportation == action.transportation;
    }

    @Override
    public int hashCode() {
        int result = transportation.hashCode();
        result = 31 * result + destination;
        return result;
    }

    @Override
    public String toString() {
        return transportation + " to " + destination;
    }

    @Override
    public int compareTo(Action arg0) {
        if (arg0.equals(this))
            return 0;
        else if (arg0.getDestination() > destination)
            return -1;
        else if (arg0.getDestination() < destination)
            return 1;
        else if (arg0.getTransportation().ordinal() > transportation.ordinal())
            return -1;
        else
            return 1;
    }
}