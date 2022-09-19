package io.github.nejc92.sy.game;

public class Action {

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

    protected Action generateBlackFareAction() {
        return new Action(Transportation.BLACK_FARE, this.destination);
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
}