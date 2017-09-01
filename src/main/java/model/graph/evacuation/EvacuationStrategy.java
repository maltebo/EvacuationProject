package model.graph.evacuation;

import model.graph.Path;
import model.graph.building.Building;
import model.graph.useragent.Person;

import java.util.HashSet;

public abstract class EvacuationStrategy {

    Building building;

    HashSet<Person> persons;


    public EvacuationStrategy(Building building) {
        this.building = building;
        this.persons = building.getPersonsInBuilding();
    }


    public abstract Path getPath(Person person);

    public abstract void startEvacuation();

}
