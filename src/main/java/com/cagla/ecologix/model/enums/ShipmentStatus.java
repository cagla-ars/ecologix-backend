package com.cagla.ecologix.model.enums;

public enum ShipmentStatus {
    HUB_TRANSFER,      // Şubeler/merkezler arası süreçte
    AT_BRANCH,         // Şubede, işlem bekliyor
    PENDING,           // Dağıtıma hazır
    IN_TRANSIT,        // Kurye dağıtımda
    FORWARDED,         // Başka merkeze aktarıldı
    DELIVERED          // Alıcıya teslim edildi
}