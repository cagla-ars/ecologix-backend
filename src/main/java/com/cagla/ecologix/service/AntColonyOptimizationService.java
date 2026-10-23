package com.cagla.ecologix.service;

import com.cagla.ecologix.model.Shipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;


@Service
public class AntColonyOptimizationService implements RoutingAlgorithm {

    @Autowired
    private OptimizationService optimizationService;

    @Value("${aco.ant-count:50}")
    private int antCount;

    @Value("${aco.iterations:100}")
    private int iterations;

    @Value("${aco.alpha:1.0}")
    private double alpha;

    @Value("${aco.beta:2.0}")
    private double beta;

    @Value("${aco.evaporation:0.5}")
    private double evaporation; 

    @Value("${aco.q:500}")
    private double q; 

    @Override
    public String getName() {
        return "Karınca Kolonisi (ACO)";
    }

    @Override
    public List<Shipment> solve(List<Shipment> shipments, double startLat, double startLon) {
        int n = shipments.size();
        if (n == 0) return new ArrayList<>();

        double[][] distances = prepareDistanceMatrix(shipments, startLat, startLon);
        double[][] pheromones = new double[n + 1][n + 1];
        for (double[] row : pheromones) Arrays.fill(row, 0.1);

        List<Integer> globalBestRoute = null;
        double globalBestDist = Double.MAX_VALUE;

        for (int i = 0; i < iterations; i++) {
            List<List<Integer>> antRoutes = new ArrayList<>();
            
            for (int ant = 0; ant < antCount; ant++) {
                List<Integer> route = buildAntRoute(n, pheromones, distances, shipments);
                
                optimizeRouteWith2Opt(route, distances);
                
                antRoutes.add(route);
                
                double d = calculateRouteDistance(route, distances);
                if (d < globalBestDist) {
                    globalBestDist = d;
                    globalBestRoute = new ArrayList<>(route);
                }
            }
            
            updatePheromones(pheromones, antRoutes, distances, globalBestRoute, globalBestDist);
        }

        return convertToShipmentList(globalBestRoute, shipments);
    }

    private void optimizeRouteWith2Opt(List<Integer> route, double[][] distances) {
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 1; i < route.size() - 2; i++) {
                for (int j = i + 1; j < route.size() - 1; j++) {
                    double currentDist = distances[route.get(i-1)][route.get(i)] + distances[route.get(j)][route.get(j+1)];
                    double newDist = distances[route.get(i-1)][route.get(j)] + distances[route.get(i)][route.get(j+1)];
                    
                    if (newDist < currentDist) {
                        Collections.reverse(route.subList(i, j + 1));
                        improved = true;
                    }
                }
            }
        }
    }

    private void updatePheromones(double[][] pheromones, List<List<Integer>> routes, double[][] distances, List<Integer> bestRoute, double bestDist) {
        for (int i = 0; i < pheromones.length; i++) {
            for (int j = 0; j < pheromones.length; j++) {
                pheromones[i][j] *= (1.0 - evaporation);
            }
        }

        for (List<Integer> route : routes) {
            double dist = calculateRouteDistance(route, distances);
            double contribution = q / dist;
            depositPheromone(pheromones, route, contribution);
        }

        if (bestRoute != null) {
            depositPheromone(pheromones, bestRoute, (q / bestDist) * 2.0);
        }
    }

    private void depositPheromone(double[][] pheromones, List<Integer> route, double amount) {
        for (int i = 0; i < route.size() - 1; i++) {
            int from = route.get(i);
            int to = route.get(i + 1);
            pheromones[from][to] += amount;
            pheromones[to][from] += amount;
        }
    }



    private double[][] prepareDistanceMatrix(List<Shipment> shipments, double startLat, double startLon) {
        int size = shipments.size() + 1; 
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double lat1 = (i == 0) ? startLat : shipments.get(i - 1).getLat();
                double lon1 = (i == 0) ? startLon : shipments.get(i - 1).getLon();
                double lat2 = (j == 0) ? startLat : shipments.get(j - 1).getLat();
                double lon2 = (j == 0) ? startLon : shipments.get(j - 1).getLon();
                matrix[i][j] = optimizationService.calculateDistance(lat1, lon1, lat2, lon2);
            }
        }
        return matrix;
    }

    private List<Integer> buildAntRoute(int n, double[][] pheromones, double[][] distances, List<Shipment> shipments) {
        List<Integer> route = new ArrayList<>();
        boolean[] visited = new boolean[n + 1];
        int current = 0; 
        route.add(current);
        visited[current] = true;

        for (int i = 0; i < n; i++) {
            int next = selectNextCity(current, n, pheromones, distances, visited, shipments);
            if (next != -1) {
                route.add(next);
                visited[next] = true;
                current = next;
            }
        }
        return route;
    }

    private int selectNextCity(int current, int n, double[][] pheromones, double[][] distances, boolean[] visited, List<Shipment> shipments) {
        double[] probabilities = new double[n + 1];
        double sum = 0.0;
        for (int i = 1; i <= n; i++) {
            if (!visited[i]) {
                double eta = 1.0 / (distances[current][i] + 0.0001);
                double priorityWeight = Math.pow(4.0 - shipments.get(i - 1).getPriority(), 2.0);
                probabilities[i] = Math.pow(pheromones[current][i], alpha) * Math.pow(eta, beta) * priorityWeight;
                sum += probabilities[i];
            }
        }
        if (sum == 0) {
            for (int i = 1; i <= n; i++) if (!visited[i]) return i;
            return -1;
        }
        double random = Math.random() * sum;
        double currentSum = 0.0;
        for (int i = 1; i <= n; i++) {
            if (!visited[i]) {
                currentSum += probabilities[i];
                if (currentSum >= random) return i;
            }
        }
        return -1;
    }

    private double calculateRouteDistance(List<Integer> route, double[][] distances) {
        double total = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            total += distances[route.get(i)][route.get(i + 1)];
        }
        return total;
    }

    private List<Shipment> convertToShipmentList(List<Integer> route, List<Shipment> shipments) {
        List<Shipment> result = new ArrayList<>();
        if (route == null) return result;
        for (int i = 1; i < route.size(); i++) {
            result.add(shipments.get(route.get(i) - 1));
        }
        return result;
    }
}