package com.cagla.ecologix.service;

import com.cagla.ecologix.model.Shipment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;


@Service
public class SquidGameOptimizationService implements RoutingAlgorithm {

    @Autowired
    private OptimizationService optimizationService;

    @Value("${sgo.player-size:100}")
    private int playerSize;

    @Value("${sgo.max-games:200}")
    private int maxGames;

    @Override
    public String getName() {
        return "Squid Game Optimizer (SGO)";
    }

    @Override
    public List<Shipment> solve(List<Shipment> shipments, double startLat, double startLon) {
        int n = shipments.size();
        if (n <= 1) return shipments;

        // 1. Popülasyonu Başlatma
        List<List<Integer>> players = new ArrayList<>();
        for (int i = 0; i < playerSize; i++) {
            List<Integer> route = new ArrayList<>();
            for (int j = 0; j < n; j++) route.add(j);
            Collections.shuffle(route);
            players.add(route);
        }

        List<Integer> globalBest = new ArrayList<>(players.get(0));
        double globalMinDist = Double.MAX_VALUE;

        // 2. Oyun Döngüsü (İterasyonlar)
        for (int game = 0; game < maxGames; game++) {
            
            // Mevcut oyuncuları mesafeye göre sırala (Liderleri belirlemek için)
            players.sort(Comparator.comparingDouble(p -> calculateRouteDistance(p, shipments, startLat, startLon)));
            
            double currentMin = calculateRouteDistance(players.get(0), shipments, startLat, startLon);
            if (currentMin < globalMinDist) {
                globalMinDist = currentMin;
                globalBest = new ArrayList<>(players.get(0));
            }

            // Adaptif Katsayı: Oyun ilerledikçe keşif (exploration) azalır, sömürü (exploitation) artar.
            double adaptivePower = 1.0 - ((double) game / maxGames);

            // 3. Oyuncuları Güncelle (Saldırı ve Savunma Fazı)
            updatePlayersFinal(players, globalBest, adaptivePower);
        }

        return convertToShipmentList(globalBest, shipments);
    }

   
    private void updatePlayersFinal(List<List<Integer>> players, List<Integer> globalBest, double power) {
        Random rand = new Random();
        int half = players.size() / 2;

        // ELİTİZM: En iyi oyuncuyu (0. index) koru, diğerlerini güncelle
        for (int i = 1; i < players.size(); i++) {
            List<Integer> player = players.get(i);

            if (i < half) {
                // SALDIRI GRUBU: Çoklu Lider Stratejisi
                // İlk 5 oyuncu (Elite Set) içinden rastgele bir hedefe saldırı
                List<Integer> target = players.get(rand.nextInt(Math.min(5, players.size())));
                attackStrategy(player, target, power);
            } else {
                // SAVUNMA GRUBU: Dinamik İtme Gücü
                // Oyun başında segment kaydırma (büyük kaos), sonunda 2-Opt (ince ayar)
                defenseStrategy(player, power);
            }
        }
    }


     // Saldırı: Hedef rotadaki başarılı düğüm dizilimlerini kopyalar.
    
    private void attackStrategy(List<Integer> player, List<Integer> target, double power) {
        Random rand = new Random();
        int size = player.size();
        
        // Oyun sonuna yaklaştıkça saldırganlar hedefe daha büyük parçalarla yaklaşır.
        int segmentSize = rand.nextInt(Math.max(1, (int)(size * (1.1 - power)))) + 1;
        int start = rand.nextInt(size);

        for (int i = 0; i < segmentSize; i++) {
            int pos = (start + i) % size;
            int city = target.get(pos);
            int oldPos = player.indexOf(city);
            Collections.swap(player, pos, oldPos);
        }
    }

    /**
     * Savunma: Oyuncuyu mevcut konumundan uzaklaştırarak yeni yollar keşfetmesini sağlar.
     */
    private void defenseStrategy(List<Integer> player, double power) {
        Random rand = new Random();
        int size = player.size();
        
        if (rand.nextDouble() < power) {
            // SEGMENT SHIFT: Bir rotayı blok olarak kesip başka yere taşıma (Büyük İtme)
            int start = rand.nextInt(size);
            int length = rand.nextInt(Math.max(1, size / 3)) + 1;
            int targetPos = rand.nextInt(size);
            
            List<Integer> segment = new ArrayList<>();
            for(int i = 0; i < length; i++) segment.add(player.get((start + i) % size));
            
            player.removeAll(segment);
            int insertIndex = Math.min(player.size(), targetPos);
            player.addAll(insertIndex, segment);
        } else {
            // 2-OPT INVERSION: Yoldaki çaprazları çözen ters çevirme (İnce İtme)
            int i = rand.nextInt(size);
            int j = rand.nextInt(size);
            int start = Math.min(i, j);
            int end = Math.max(i, j);
            if (start != end) {
                Collections.reverse(player.subList(start, end));
            }
        }
    }

    private double calculateRouteDistance(List<Integer> route, List<Shipment> shipments, double startLat, double startLon) {
        double total = 0;
        double currentLat = startLat;
        double currentLon = startLon;

        for (int index : route) {
            Shipment s = shipments.get(index);
            total += optimizationService.calculateDistance(currentLat, currentLon, s.getLat(), s.getLon());
            currentLat = s.getLat();
            currentLon = s.getLon();
        }
        return total;
    }

    private List<Shipment> convertToShipmentList(List<Integer> route, List<Shipment> shipments) {
        List<Shipment> result = new ArrayList<>();
        for (int index : route) {
            result.add(shipments.get(index));
        }
        return result;
    }
}