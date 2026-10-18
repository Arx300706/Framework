package com.app.test;

import com.app.model.Personne;
import com.app.repository.PersonneRepository;

import java.util.List;

public class PersonneRepositorySmokeTest {
    public static void main(String[] args) throws Exception {
        PersonneRepository repository = new PersonneRepository();
        repository.initializeDemoData();

        List<Personne> personnes = repository.findAll();
        if (personnes.isEmpty()) {
            throw new IllegalStateException("La liste des personnes est vide");
        }

        System.out.println("Smoke test OK");
        System.out.println("Nombre de personnes : " + personnes.size());

        for (Personne personne : personnes) {
            System.out.println(personne.getId() + " - " + personne.getNom() + " - " + personne.getAge());
        }
    }
}
