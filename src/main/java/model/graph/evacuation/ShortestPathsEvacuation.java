package model.graph.evacuation;

import model.graph.Path;
import model.graph.PathOntology;
import model.graph.building.Building;
import model.graph.useragent.Person;

import java.util.HashSet;

public class ShortestPathsEvacuation extends EvacuationStrategy {

    PathOntology po;

    public ShortestPathsEvacuation(Building building) {
        super(building);
        po = PathOntology.getInstance(building);
    }

    @Override
    public Path getPath(Person person) {
        return po.getShortestPath(person.getIsOnCell(), null, person.isDisabled());
    }

    @Override
    public void startEvacuation() {
        HashSet<Person> personCopy = new HashSet<>(persons);
        for (Person person : personCopy) {

            if (person.getIsInRoom() == null) {
                person.remove();
            } else {
                person.startEvacuation();
                person.setPath(po.getShortestPath(person.getIsOnCell(), null, person.isDisabled()));
            }

        }
    }


}
