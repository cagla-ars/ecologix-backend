package com.cagla.ecologix.service;

import com.cagla.ecologix.model.Shipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GeneticAlgorithmService implements RoutingAlgorithm {

    @Autowired
    private OptimizationService optimizationService;

    @Value("${ga.population-size:50}")
    private int populationSize;

    @Value("${ga.generations:100}")
    private int generations;

    @Value("${ga.mutation-rate:0.05}")
    private double mutationRate;

    @Override
    public String getName() {
        return "Genetik Algoritma (GA)";
    }

    @Override
    public List<Shipment> solve(List<Shipment> shipments, double startLat, double startLon) {
        if (shipments == null || shipments.isEmpty()) {
            return new ArrayList<>();
        }

        if (shipments.size() == 1) {
            return new ArrayList<>(shipments);
        }

        List<List<Shipment>> population = createInitialPopulation(shipments);

        for (int generation = 0; generation < generations; generation++) {

            population.sort((r1, r2) -> {
                double f1 = calculateFitness(r1, startLat, startLon);
                double f2 = calculateFitness(r2, startLat, startLon);
                return Double.compare(f2, f1);
            });
            List<List<Shipment>> nextGeneration = new ArrayList<>();

            nextGeneration.add(new ArrayList<>(population.get(0)));
            nextGeneration.add(new ArrayList<>(population.get(1)));


            while (nextGeneration.size() < populationSize) {
                List<Shipment> parent1 = selection(population, startLat, startLon);
                List<Shipment> parent2 = selection(population, startLat, startLon);

                List<Shipment> child = crossover(parent1, parent2);
                child = mutate(child);

                nextGeneration.add(child);
            }

            population = nextGeneration;
        }

        population.sort((r1, r2) -> {
            double f1 = calculateFitness(r1, startLat, startLon);
            double f2 = calculateFitness(r2, startLat, startLon);
            return Double.compare(f2, f1);
        });

        return new ArrayList<>(population.get(0));
    }

    private List<List<Shipment>> createInitialPopulation(List<Shipment> shipments) {
        List<List<Shipment>> population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            List<Shipment> individual = new ArrayList<>(shipments);

            Collections.shuffle(individual);
            population.add(individual);
        }

        return population;
    }

    private List<Shipment> selection(
            List<List<Shipment>> population,
            double startLat,
            double startLon
    ) {
        Random random = new Random();
        int tournamentSize = 5;

        List<Shipment> bestRoute = null;
        double bestFitness = -1;

        for (int i = 0; i < tournamentSize; i++) {
            List<Shipment> candidate = population.get(random.nextInt(population.size()));

            double candidateFitness = calculateFitness(candidate, startLat, startLon);

            if (candidateFitness > bestFitness) {
                bestFitness = candidateFitness;
                bestRoute = candidate;
            }
        }

        return new ArrayList<>(bestRoute);
    }

    private double calculateFitness(
            List<Shipment> route,
            double startLat,
            double startLon
    ) {
        double totalDistance = 0;
        double currentLat = startLat;
        double currentLon = startLon;

        for (Shipment shipment : route) {
            totalDistance += optimizationService.calculateDistance(
                    currentLat,
                    currentLon,
                    shipment.getLat(),
                    shipment.getLon()
            );

            currentLat = shipment.getLat();
            currentLon = shipment.getLon();
        }

        return 1 / (totalDistance + 0.0001);
    }

    private List<Shipment> mutate(List<Shipment> route) {
        if (Math.random() < mutationRate) {
            Random random = new Random();

            int firstIndex = random.nextInt(route.size());
            int secondIndex = random.nextInt(route.size());

            int start = Math.min(firstIndex, secondIndex);
            int end = Math.max(firstIndex, secondIndex);

            if (start != end) {
                Collections.reverse(route.subList(start, end));
            }
        }

        return route;
    }

    private List<Shipment> crossover(
            List<Shipment> parent1,
            List<Shipment> parent2
    ) {
        List<Shipment> child =
                new ArrayList<>(Collections.nCopies(parent1.size(), null));

        int firstPoint = (int) (Math.random() * parent1.size());
        int secondPoint = (int) (Math.random() * parent1.size());

        int start = Math.min(firstPoint, secondPoint);
        int end = Math.max(firstPoint, secondPoint);

        for (int i = start; i <= end; i++) {
            child.set(i, parent1.get(i));
        }

        for (Shipment shipment : parent2) {
            if (!child.contains(shipment)) {
                for (int i = 0; i < child.size(); i++) {
                    if (child.get(i) == null) {
                        child.set(i, shipment);
                        break;
                    }
                }
            }
        }

        return child;
    }
}